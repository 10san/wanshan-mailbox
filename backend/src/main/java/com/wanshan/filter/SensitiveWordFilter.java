package com.wanshan.filter;

import com.wanshan.mapper.SensitiveWordMapper;
import com.wanshan.model.entity.SensitiveWord;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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

    @PostConstruct
    public void init() {
        refresh();
    }

    /**
     * 从数据库刷新词库
     */
    public void refresh() {
        List<SensitiveWord> words = sensitiveWordMapper.selectList(null);
        lock.writeLock().lock();
        try {
            root = new TrieNode();
            List<Pattern> patterns = new ArrayList<>();
            for (SensitiveWord sw : words) {
                if (sw.getMatchType() == 2) {
                    try {
                        patterns.add(Pattern.compile(sw.getWord()));
                    } catch (Exception e) {
                        log.warn("Invalid regex pattern: {}", sw.getWord());
                    }
                } else {
                    insertToTrie(sw.getWord().toLowerCase());
                }
            }
            this.regexPatterns = patterns;
            log.info("Sensitive word library refreshed: {} exact + {} regex patterns",
                    countTrieWords(), patterns.size());
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

    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd;
    }
}
