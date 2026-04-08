#!/usr/bin/env python3
"""
简单的HTTP服务器，用于模拟后端API
支持基本的认证和报表API
"""

from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import jwt
import datetime
from urllib.parse import urlparse, parse_qs
import base64

# 简单的内存数据存储
users = {
    'admin': {'password': '123456', 'role': 'ADMIN'},
    'maker1': {'password': '123456', 'role': 'MAKER'},
    'l1_checker1': {'password': '123456', 'role': 'L1_CHECKER'},
    'l2_checker1': {'password': '123456', 'role': 'L2_CHECKER'}
}

reports = [
    {
        'id': 1,
        'name': 'Customer Transaction Analysis',
        'sql': 'SELECT * FROM customers',
        'description': 'Customer transaction analysis report'
    },
    {
        'id': 2,
        'name': 'VIP Customer Revenue Report',
        'sql': 'SELECT * FROM vip_customers',
        'description': 'VIP customer revenue analysis'
    }
]

report_runs = []
report_run_id = 1
audit_events = []

# JWT密钥
JWT_SECRET = 'your-secret-key'

def generate_token(username, role):
    """生成JWT token"""
    payload = {
        'username': username,
        'role': role,
        'exp': datetime.datetime.utcnow() + datetime.timedelta(hours=24)
    }
    return jwt.encode(payload, JWT_SECRET, algorithm='HS256')

def verify_token(token):
    """验证JWT token"""
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=['HS256'])
        return payload
    except:
        return None

class ReportAPIHandler(BaseHTTPRequestHandler):
    def do_OPTIONS(self):
        """处理CORS预检请求"""
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()

    def do_POST(self):
        """处理POST请求"""
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)
        
        # 解析路径
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        
        # 设置CORS头
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        
        try:
            if path == '/api/auth/login':
                # 登录处理
                data = json.loads(post_data.decode('utf-8'))
                username = data.get('username')
                password = data.get('password')
                
                if username in users and users[username]['password'] == password:
                    token = generate_token(username, users[username]['role'])
                    response = {
                        'token': token,
                        'user': {
                            'username': username,
                            'role': users[username]['role']
                        }
                    }
                    self.wfile.write(json.dumps(response).encode('utf-8'))
                else:
                    self.send_response(401)
                    self.end_headers()
                    self.wfile.write(json.dumps({'error': 'Invalid credentials'}).encode('utf-8'))
                    
            elif path.startswith('/api/reports/') and path.endswith('/execute'):
                # 执行报表
                token = self.headers.get('Authorization', '').replace('Bearer ', '')
                user_data = verify_token(token)
                if user_data:
                    report_id = int(path.split('/')[3])
                    # 模拟报表执行
                    global report_run_id
                    new_run = {
                        'id': report_run_id,
                        'reportId': report_id,
                        'reportName': next((r['name'] for r in reports if r['id'] == report_id), ''),
                        'status': 'Generated',
                        'makerUsername': user_data['username'],
                        'generatedAt': datetime.datetime.now().isoformat(),
                        'currentApprovalStage': 0
                    }
                    report_runs.append(new_run)
                    report_run_id += 1
                    
                    # 模拟报表数据
                    report_data = [
                        {'id': 1, 'name': 'Customer 1', 'amount': 1000},
                        {'id': 2, 'name': 'Customer 2', 'amount': 2000}
                    ]
                    self.wfile.write(json.dumps(report_data).encode('utf-8'))
                else:
                    self.send_response(401)
                    self.end_headers()
                    self.wfile.write(json.dumps({'error': 'Unauthorized'}).encode('utf-8'))
                    
            elif path.startswith('/api/reports/runs/') and path.endswith('/submit'):
                # 提交审批
                token = self.headers.get('Authorization', '').replace('Bearer ', '')
                user_data = verify_token(token)
                if user_data:
                    run_id = int(path.split('/')[4])
                    for run in report_runs:
                        if run['id'] == run_id:
                            run['status'] = 'Submitted'
                            run['submittedAt'] = datetime.datetime.now().isoformat()
                            run['currentApprovalStage'] = 1
                            
                            # 添加审计事件
                            audit_events.append({
                                'id': len(audit_events) + 1,
                                'reportRunId': run_id,
                                'reportId': run['reportId'],
                                'actorUsername': user_data['username'],
                                'actorRole': user_data['role'],
                                'eventType': 'Submitted',
                                'eventTime': datetime.datetime.now().isoformat()
                            })
                            break
                    self.wfile.write(json.dumps({'success': True}).encode('utf-8'))
                else:
                    self.send_response(401)
                    self.end_headers()
                    self.wfile.write(json.dumps({'error': 'Unauthorized'}).encode('utf-8'))
                    
            else:
                # 其他POST请求
                self.wfile.write(json.dumps({'message': 'API endpoint not found'}).encode('utf-8'))
                
        except Exception as e:
            self.wfile.write(json.dumps({'error': str(e)}).encode('utf-8'))

    def do_GET(self):
        """处理GET请求"""
        # 设置CORS头
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        
        try:
            parsed_path = urlparse(self.path)
            path = parsed_path.path
            
            if path == '/api/reports':
                # 获取报表列表
                self.wfile.write(json.dumps(reports).encode('utf-8'))
                
            elif path.startswith('/api/reports/runs/') and path.endswith('/audit'):
                # 获取审计轨迹
                run_id = int(path.split('/')[4])
                run_audits = [event for event in audit_events if event['reportRunId'] == run_id]
                self.wfile.write(json.dumps(run_audits).encode('utf-8'))
                
            elif path.startswith('/api/reports/pending-approval'):
                # 获取待审批列表
                token = self.headers.get('Authorization', '').replace('Bearer ', '')
                user_data = verify_token(token)
                if user_data:
                    pending_runs = []
                    for run in report_runs:
                        if run['status'] in ['Submitted', 'L2Submitted']:
                            pending_runs.append(run)
                    
                    response = {
                        'success': True,
                        'data': {
                            'items': pending_runs
                        }
                    }
                    self.wfile.write(json.dumps(response).encode('utf-8'))
                else:
                    self.send_response(401)
                    self.end_headers()
                    self.wfile.write(json.dumps({'error': 'Unauthorized'}).encode('utf-8'))
                    
            else:
                # 其他GET请求
                self.wfile.write(json.dumps({'message': 'API endpoint not found'}).encode('utf-8'))
                
        except Exception as e:
            self.wfile.write(json.dumps({'error': str(e)}).encode('utf-8'))

def run_server():
    """启动HTTP服务器"""
    server_address = ('', 8080)
    httpd = HTTPServer(server_address, ReportAPIHandler)
    print(f"Starting server on http://localhost:8080")
    print("Available endpoints:")
    print("  POST /api/auth/login - 用户登录")
    print("  GET /api/reports - 获取报表列表")
    print("  POST /api/reports/{id}/execute - 执行报表")
    print("  POST /api/reports/runs/{id}/submit - 提交审批")
    print("  GET /api/reports/runs/{id}/audit - 获取审计轨迹")
    print("  GET /api/reports/pending-approval - 获取待审批列表")
    print()
    print("Test users:")
    print("  admin/123456 (ADMIN)")
    print("  maker1/123456 (MAKER)")
    print("  l1_checker1/123456 (L1_CHECKER)")
    print("  l2_checker1/123456 (L2_CHECKER)")
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down server...")
        httpd.server_close()

if __name__ == '__main__':
    run_server()
