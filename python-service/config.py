import yaml
import os
import re
from nacos import NacosClient

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

# 从Nacos拉取配置
def get_config_from_nacos():
    try:
        # 首先使用本地配置文件中的Nacos地址
        local_config_path = os.path.join(os.path.dirname(__file__), 'config.yml')
        with open(local_config_path, 'r', encoding='utf-8') as f:
            local_config = yaml.safe_load(f)
        
        # 先解析环境变量
        local_config = resolve_env_vars(local_config)
        
        # 从解析后的配置中获取Nacos连接信息
        nacos_server_addr = local_config['nacos']['server-addr']
        namespace = local_config['nacos']['namespace']
        service_name = local_config['nacos']['service-name']
        username = local_config['nacos'].get('username', 'nacos')
        password = local_config['nacos'].get('password', 'nacos')
        
        print(f"连接Nacos: {nacos_server_addr}, namespace: {namespace}")
        print(f"Nacos用户名: {username}")
        
        # 连接Nacos
        nacos_client = NacosClient(
            nacos_server_addr, 
            namespace=namespace,
            username=username,
            password=password
        )
        
        # 从Nacos导入多个配置文件，类似Java的config.import
        config_files = [
            {'data_id': 'python-service.yml', 'group': 'Service_Dev'},
            {'data_id': 'common-database.yml', 'group': 'Shared_Dev'},
            {'data_id': 'common-core.yml', 'group': 'Shared_Dev'},
            {'data_id': 'common-redis.yml', 'group': 'Shared_Dev'},
            {'data_id': 'common-web.yml', 'group': 'Shared_Dev'},
            {'data_id': 'common-mongo.yml', 'group': 'Shared_Dev'}
        ]
        
        # 合并配置
        merged_config = local_config.copy()
        
        for config_file in config_files:
            data_id = config_file['data_id']
            group = config_file['group']
            
            try:
                config_content = nacos_client.get_config(data_id, group)
                if config_content:
                    print(f"从Nacos拉取配置成功: {data_id} (group: {group})")
                    file_config = yaml.safe_load(config_content)
                    # 解析环境变量
                    file_config = resolve_env_vars(file_config)
                    # 深度合并配置
                    merged_config = merge_configs(merged_config, file_config)
                else:
                    print(f"从Nacos拉取配置失败: {data_id} (group: {group})")
            except Exception as e:
                print(f"拉取配置 {data_id} 异常: {e}")
        
        return merged_config
    except Exception as e:
        print(f"从Nacos拉取配置异常: {e}")
        # 失败时使用本地配置
        local_config_path = os.path.join(os.path.dirname(__file__), 'config.yml')
        with open(local_config_path, 'r', encoding='utf-8') as f:
            local_config = yaml.safe_load(f)
        # 解析环境变量
        local_config = resolve_env_vars(local_config)
        return local_config

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
config = get_config_from_nacos()

# 服务配置
SERVICE_NAME = config['service']['name']
SERVICE_VERSION = config['service']['version']
SERVICE_DESCRIPTION = config['service']['description']

# Redis配置 - 优先使用Nacos中的配置
REDIS_HOST = config.get('spring', {}).get('redis', {}).get('host', config['redis']['host'])
REDIS_PORT = config.get('spring', {}).get('redis', {}).get('port', config['redis']['port'])
REDIS_PASSWORD = config.get('spring', {}).get('redis', {}).get('password', config['redis']['password'])
REDIS_DB = config.get('spring', {}).get('redis', {}).get('database', config['redis']['db'])

# MongoDB配置 - 优先使用Nacos中的配置
MONGODB_URI = config.get('spring', {}).get('data', {}).get('mongodb', {}).get('uri', config['mongodb']['uri'])
MONGODB_DATABASE = config.get('spring', {}).get('data', {}).get('mongodb', {}).get('database', config['mongodb']['database'])

# Nacos配置
NACOS_SERVER_ADDR = config['nacos']['server-addr']
NACOS_NAMESPACE = config['nacos']['namespace']
NACOS_SERVICE_NAME = config['nacos']['service-name']
NACOS_CLUSTER_NAME = config['nacos']['cluster-name']
NACOS_WEIGHT = config['nacos']['weight']

# 服务器配置
SERVER_HOST = config['server']['host']
SERVER_PORT = config['server']['port']
