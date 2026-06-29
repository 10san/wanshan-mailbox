package com.wanshan.filter;

import com.wanshan.mapper.SensitiveWordMapper;
import com.wanshan.model.entity.SensitiveWord;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 敏感词过滤器 — 基于 Trie 树 + 正则模式
 * 支持精确匹配和正则匹配，毫秒级检测
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    private final SensitiveWordMapper sensitiveWordMapper;

    /** Trie 树根节点 */
    private TrieNode root = new TrieNode();

    /** 正则匹配词列表 */
    private List<Pattern> regexPatterns = new ArrayList<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public SensitiveWordFilter(SensitiveWordMapper sensitiveWordMapper) {
        this.sensitiveWordMapper = sensitiveWordMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // 先加载代码内嵌基础词库（避免数据库编码问题）
        loadBuiltinWords();
        // 再从数据库加载
        refreshFromDb();
    }

    /**
     * 代码内嵌基础敏感词库
     */
    private void loadBuiltinWords() {
        String[] builtin = {
            "赌博", "彩票", "六合彩", "色情", "裸聊", "约炮",
            "毒品", "冰毒", "大麻", "枪支", "炸药",
            "广告", "加微信", "加QQ", "微信号", "QQ号",
            "兼职", "刷单", "返利", "代购", "微商", "代理"
        };
        lock.writeLock().lock();
        try {
            for (String word : builtin) {
                insertToTrie(word.toLowerCase());
            }
        } finally {
            lock.writeLock().unlock();
        }
        log.info("Builtin sensitive words loaded: {} words", builtin.length);
    }

    /**
     * 从数据库刷新词库（追加到已有词库）
     */
    public void refreshFromDb() {
        List<SensitiveWord> words = sensitiveWordMapper.selectList(null);
        lock.writeLock().lock();
        try {
            root = new TrieNode();
            regexPatterns = new ArrayList<>();
            // 先加载内置词
            String[] builtin = {
                "赌博", "彩票", "六合彩", "色情", "裸聊", "约炮",
                "毒品", "冰毒", "大麻", "枪支", "炸药",
                "广告", "加微信", "加QQ", "微信号", "QQ号",
                "兼职", "刷单", "返利", "代购", "微商", "代理"
            };
            for (String w : builtin) insertToTrie(w.toLowerCase());
            // 追加数据库词
            for (SensitiveWord sw : words) {
                if (sw.getMatchType() == 2) {
                    try { regexPatterns.add(Pattern.compile(sw.getWord())); }
                    catch (Exception e) { log.warn("Invalid regex: {}", sw.getWord()); }
                } else {
                    insertToTrie(sw.getWord().toLowerCase());
                }
            }
            log.info("Sensitive words: {} exact + {} regex", countTrieWords(), regexPatterns.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 检测文本是否包含敏感词
     */
    public boolean containsSensitive(String text) {
        if (text == null || text.isEmpty()) return false;

        lock.readLock().lock();
        try {
            String lower = text.toLowerCase();

            // 1. Trie 精确匹配
            for (int i = 0; i < lower.length(); i++) {
                TrieNode node = root;
                for (int j = i; j < lower.length(); j++) {
                    char c = lower.charAt(j);
                    // 跳过常见的分隔字符
                    if (c == ' ' || c == '*' || c == '.' || c == '_' || c == '-') continue;

                    node = node.children.get(c);
                    if (node == null) break;
                    if (node.isEnd) return true;
                }
            }

            // 2. 正则匹配
            for (Pattern pattern : regexPatterns) {
                if (pattern.matcher(lower).find()) return true;
            }

            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    private void insertToTrie(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEnd = true;
    }

    private int countTrieWords() {
        return countWords(root);
    }

    private int countWords(TrieNode node) {
        int count = node.isEnd ? 1 : 0;
        for (TrieNode child : node.children.values()) {
            count += countWords(child);
        }
        return count;
    }

    private List<String> getSampleWords(int limit) {
        List<String> samples = new ArrayList<>();
        collectWords(root, new StringBuilder(), samples, limit);
        return samples;
    }

    private void collectWords(TrieNode node, StringBuilder prefix, List<String> samples, int limit) {
        if (samples.size() >= limit) return;
        if (node.isEnd) samples.add(prefix.toString());
        for (var entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            collectWords(entry.getValue(), prefix, samples, limit);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd;
    }
}
