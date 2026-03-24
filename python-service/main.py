from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import yaml
import os
import re
from routers import clean_router, export_router, analysis_router

# 加载.env文件
try:
    from dotenv import load_dotenv
    # 尝试加载.env文件
    env_path = os.path.join(os.path.dirname(__file__), '.env')
    if os.path.exists(env_path):
        load_dotenv(env_path)
        print(f"加载.env文件成功: {env_path}")
    else:
        print("未找到.env文件，使用系统环境变量")
except ImportError:
    print("未安装python-dotenv，使用系统环境变量")

# 解析环境变量占位符
def resolve_env_vars(value):
    if isinstance(value, str):
        # 匹配 ${VAR:default} 格式
        pattern = r'\$\{([^:]+):?([^}]+)?\}'
        def replace_var(match):
            var_name = match.group(1)
            default_value = match.group(2) if match.group(2) else ''
            return os.environ.get(var_name, default_value)
        return re.sub(pattern, replace_var, value)
    elif isinstance(value, dict):
        return {k: resolve_env_vars(v) for k, v in value.items()}
    elif isinstance(value, list):
        return [resolve_env_vars(item) for item in value]
    else:
        return value

# 从本地加载配置
def get_config_from_local():
    try:
        # 从本地配置文件加载
        local_config_path = os.path.join(os.path.dirname(__file__), 'config.yml')
        with open(local_config_path, 'r', encoding='utf-8') as f:
            local_config = yaml.safe_load(f)
        
        # 解析环境变量
        local_config = resolve_env_vars(local_config)
        print("从本地加载配置成功")
        return local_config
    except Exception as e:
        print(f"从本地加载配置异常: {e}")
        import traceback
        traceback.print_exc()
        # 返回默认配置
        return {
            'service': {
                'name': 'python-service',
                'description': 'MineGuard Python Service',
                'version': '1.0.0'
            },
            'server': {
                'host': '0.0.0.0',
                'port': 8090
            }
        }

# 深度合并配置
def merge_configs(target, source):
    if isinstance(target, dict) and isinstance(source, dict):
        for key, value in source.items():
            if key in target:
                target[key] = merge_configs(target[key], value)
            else:
                target[key] = value
        return target
    elif isinstance(target, list) and isinstance(source, list):
        return target + source
    else:
        return source

# 加载配置
config = get_config_from_local()

app = FastAPI(
    title="MineGuard Python Service",
    description=config['service']['description'],
    version=config['service']['version']
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(clean_router, prefix="/api/clean", tags=["数据清洗"])
app.include_router(export_router, prefix="/api/export", tags=["报表导出"])
app.include_router(analysis_router, prefix="/api/analysis", tags=["数据分析"])


@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": config['service']['name']}

# 服务状态检查
def check_service_status():
    try:
        print("\n=== 检查服务状态 ===")
        print(f"服务名称: {config['service']['name']}")
        print(f"服务版本: {config['service']['version']}")
        print(f"服务地址: {config['server']['host']}:{config['server']['port']}")
        print("\n=== 服务状态检查完成 ===")
    except Exception as e:
        print(f"检查服务状态异常: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    # 检查服务状态
    check_service_status()
    
    # 启动服务
    print(f"启动FastAPI服务: {config['server']['host']}:{config['server']['port']}")
    uvicorn.run(
        "main:app", 
        host=config['server']['host'], 
        port=config['server']['port']
    )
