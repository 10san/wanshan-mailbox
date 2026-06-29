#!/usr/bin/env python3
"""统一静态服务器 — 前端 + 管理后台 + API 代理"""
import http.server
import urllib.request
import urllib.error
import os
import json

FRONTEND_DIR = "/workspace/wanshan-mailbox/frontend/dist"
ADMIN_DIR = "/workspace/wanshan-mailbox/admin/dist"
BACKEND = "http://127.0.0.1:8080"

class ProxyHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=FRONTEND_DIR, **kwargs)

    def do_GET(self):
        # API 代理
        if self.path.startswith("/api/"):
            self.proxy_request("GET")
            return

        # 管理后台
        if self.path.startswith("/admin"):
            self.serve_admin()
            return

        # C 端 SPA fallback
        file_path = os.path.join(FRONTEND_DIR, self.path.lstrip("/"))
        if not os.path.exists(file_path) or os.path.isdir(file_path):
            self.path = "/index.html"
        super().do_GET()

    def do_POST(self):
        if self.path.startswith("/api/"):
            self.proxy_request("POST")
            return
        self.send_error(404)

    def do_DELETE(self):
        if self.path.startswith("/api/"):
            self.proxy_request("DELETE")
            return
        self.send_error(404)

    def do_PUT(self):
        if self.path.startswith("/api/"):
            self.proxy_request("PUT")
            return
        self.send_error(404)

    def proxy_request(self, method):
        url = BACKEND + self.path
        headers = {k: v for k, v in self.headers.items()
                   if k.lower() not in ('host', 'connection')}
        body = None
        # POST/PUT/DELETE 都可能带 body
        if method in ("POST", "PUT", "DELETE"):
            length = int(self.headers.get("Content-Length", 0))
            if length > 0:
                body = self.rfile.read(length)

        req = urllib.request.Request(url, data=body, headers=headers, method=method)
        try:
            resp = urllib.request.urlopen(req, timeout=30)
            self.send_response(resp.status)
            for k, v in resp.getheaders():
                if k.lower() not in ('transfer-encoding', 'connection'):
                    self.send_header(k, v)
            self.end_headers()
            self.wfile.write(resp.read())
        except urllib.error.HTTPError as e:
            self.send_response(e.code)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.end_headers()
            self.wfile.write(e.read())
        except Exception as e:
            self.send_error(502, str(e))

    def serve_admin(self):
        admin_path = self.path.replace("/admin", "", 1) or "/"
        if admin_path.startswith("/"):
            admin_path = admin_path[1:]
        file_path = os.path.join(ADMIN_DIR, admin_path)

        if os.path.isfile(file_path):
            ext = os.path.splitext(file_path)[1]
            ctype = {
                ".html": "text/html", ".js": "application/javascript",
                ".css": "text/css", ".json": "application/json",
                ".png": "image/png", ".svg": "image/svg+xml"
            }.get(ext, "application/octet-stream")
            self.send_response(200)
            self.send_header("Content-Type", ctype)
            self.end_headers()
            with open(file_path, "rb") as f:
                self.wfile.write(f.read())
        else:
            # SPA fallback
            index = os.path.join(ADMIN_DIR, "index.html")
            self.send_response(200)
            self.send_header("Content-Type", "text/html")
            self.end_headers()
            with open(index, "rb") as f:
                self.wfile.write(f.read())

if __name__ == "__main__":
    port = 8000
    server = http.server.HTTPServer(("0.0.0.0", port), ProxyHandler)
    print(f"晚山信箱 http://0.0.0.0:{port}")
    print(f"管理后台 http://0.0.0.0:{port}/admin")
    server.serve_forever()
