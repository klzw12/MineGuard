import os
import argparse
import uuid
import time
from datetime import datetime

# 豆包根据prompt.txt生图后默认保存在Downloads目录下
BasePath = "C:/Users/31783/Downloads/"

# 获取脚本所在目录
script_dir = os.path.dirname(os.path.abspath(__file__))
BaseDstPath = os.path.join(script_dir, "output", "carAssets")

def filter_file(file_path, file_name_pattern=None, file_type_pattern=None, min_mtime=None, max_mtime=None):
    """
    根据条件过滤文件
    :param file_path: 文件路径
    :param file_name_pattern: 文件名过滤模式（支持 * 通配符）
    :param file_type_pattern: 文件类型过滤模式（例如: .png, .jpg）
    :param min_mtime: 最小修改时间（Unix时间戳）
    :param max_mtime: 最大修改时间（Unix时间戳）
    :return: 是否通过过滤
    """
    # 检查文件是否存在且是文件
    if not os.path.isfile(file_path):
        return False
    
    # 获取文件名
    file_name = os.path.basename(file_path)
    
    # 文件名过滤
    if file_name_pattern:
        import fnmatch
        if not fnmatch.fnmatch(file_name, file_name_pattern):
            return False
    
    # 文件类型过滤
    if file_type_pattern:
        if not file_name.lower().endswith(file_type_pattern.lower()):
            return False
    
    # 时间过滤
    file_mtime = os.path.getmtime(file_path)
    if min_mtime and file_mtime < min_mtime:
        return False
    if max_mtime and file_mtime > max_mtime:
        return False
    
    return True

def mv_files(src_dir, dst_dir, file_name_pattern=None, file_type_pattern=None, min_mtime=None, max_mtime=None):
    if not os.path.exists(dst_dir):
        os.makedirs(dst_dir, exist_ok=True)
        print(f"Created destination directory: {dst_dir}")
    
    for file_name in os.listdir(src_dir):
        src_file = os.path.join(src_dir, file_name)
        
        # 应用过滤条件
        if not filter_file(src_file, file_name_pattern, file_type_pattern, min_mtime, max_mtime):
            print(f"Skipped {src_file} (filtered out)")
            continue
        
        # 使用UUID作为文件名，保持源文件的后缀名
        file_ext = os.path.splitext(file_name)[1]
        dst_filename = "car" + str(uuid.uuid4()) + file_ext
        dst_file = os.path.join(dst_dir, dst_filename)
        os.rename(src_file, dst_file)
        print(f"Moved {src_file} to {dst_file}")

def main():
    argparser = argparse.ArgumentParser(description="Move files to dst_dir with filtering options")
    argparser.add_argument("--s", "-s",type=str, default=BasePath, help="原目录")
    argparser.add_argument("--d", "-d",type=str, default=BaseDstPath, help="目标目录")
    argparser.add_argument("--name", "-n",type=str, default=None, help="文件名过滤模式（支持 * 通配符）")
    argparser.add_argument("--type", "-t",type=str, default=None, help="文件类型过滤模式（例如: .png, .jpg）")
    argparser.add_argument("--min-time", "-min",type=str, default=None, help="最小修改时间（格式: YYYY-MM-DD HH:MM:SS）")
    argparser.add_argument("--max-time", "-max",type=str, default=None, help="最大修改时间（格式: YYYY-MM-DD HH:MM:SS）")
    args = argparser.parse_args()
    
    # 转换时间字符串为Unix时间戳
    min_mtime = None
    if args.min_time:
        try:
            min_mtime = time.mktime(time.strptime(args.min_time, "%Y-%m-%d %H:%M:%S"))
        except ValueError:
            print(f"Invalid min-time format: {args.min_time}. Please use YYYY-MM-DD HH:MM:SS")
            return
    else:
        # 默认设置为今日的开始时间（00:00:00）
        today_start = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
        min_mtime = time.mktime(today_start.timetuple())
        print(f"Default min-time set to: {today_start.strftime('%Y-%m-%d %H:%M:%S')}")
    
    max_mtime = None
    if args.max_time:
        try:
            max_mtime = time.mktime(time.strptime(args.max_time, "%Y-%m-%d %H:%M:%S"))
        except ValueError:
            print(f"Invalid max-time format: {args.max_time}. Please use YYYY-MM-DD HH:MM:SS")
            return
    
    mv_files(args.s, args.d, args.name, args.type, min_mtime, max_mtime)

if __name__ == "__main__":
    main() 
