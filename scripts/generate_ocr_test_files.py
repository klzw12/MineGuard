#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
OCR测试文件生成脚本
根据人员类型批量生成对应证件的测试图像文件
支持的人员类型：
- admin: 管理员，仅生成身份证
- driver: 司机，生成身份证和驾驶证
- safety: 安全员，生成身份证和应急救援证
- repair: 维修员，生成身份证和维修资格证
"""

import argparse
import os
import random
from datetime import datetime, timedelta
from PIL import Image, ImageDraw, ImageFont
from faker import Faker

# 初始化Faker实例
fake = Faker('zh_CN')

# 生成随机字符串
def generate_random_string(length=8):
    return fake.pystr(min_chars=length, max_chars=length)

# 生成随机身份证号码
def generate_id_number():
    """生成随机身份证号码，确保70%概率为男性，30%概率为女性"""
    # 生成基础身份证号码
    id_number = fake.ssn()
    
    # 根据概率调整性别（第17位数字，奇数为男，偶数为女）
    if random.random() < 0.7:
        # 70%概率为男性，确保第17位是奇数
        if int(id_number[16]) % 2 == 0:
            # 将偶数改为奇数
            new_digit = int(id_number[16]) + 1 if int(id_number[16]) < 9 else 1
            id_number = id_number[:16] + str(new_digit) + id_number[17:]
    else:
        # 30%概率为女性，确保第17位是偶数
        if int(id_number[16]) % 2 == 1:
            # 将奇数改为偶数
            new_digit = int(id_number[16]) + 1 if int(id_number[16]) < 9 else 0
            id_number = id_number[:16] + str(new_digit) + id_number[17:]
    
    return id_number

# 从身份证号中提取出生日期
def extract_birth_date(id_number):
    """从身份证号中提取出生日期，格式为YYYYMMDD"""
    return id_number[6:14]

# 从身份证号中提取性别
def extract_gender(id_number):
    """从身份证号中提取性别，奇数为男，偶数为女"""
    gender_digit = int(id_number[16])
    return '男' if gender_digit % 2 == 1 else '女'

# 格式化日期，将YYYYMMDD格式化为YYYY年MM月DD日
def format_date(date_str):
    """将YYYYMMDD格式的日期字符串格式化为YYYY年MM月DD日"""
    if len(date_str) != 8:
        return date_str
    # 将09之类的月份和日期格式化为空格9
    month = ' ' + date_str[5:6] if int(date_str[4:6]) < 10 else date_str[4:6]
    day = ' ' + date_str[7:8] if int(date_str[6:8]) < 10 else date_str[6:8]
    return f"{date_str[:4]} 年 {month} 月 {day} 日"

# 格式化有效期，将YYYY.MM.DD-YYYY.MM.DD格式化为YYYY年MM月DD日-YYYY年MM月DD日
def format_valid_period(period_str):
    """格式化有效期字符串"""
    if '-' not in period_str:
        return period_str
    start_date, end_date = period_str.split('-')
    # 移除日期中的点
    start_date = start_date.replace('.', '')
    end_date = end_date.replace('.', '')
    return f"{format_date(start_date)}-{format_date(end_date)}"

# 生成随机姓名
def generate_name():
    return fake.name()

# 生成随机地址
def generate_address():
    return fake.address()

# 生成随机日期
def generate_date(start_year=1970, end_year=2000):
    # 使用faker生成指定范围内的随机日期
    from datetime import date
    import calendar
    
    year = random.randint(start_year, end_year)
    month = random.randint(1, 12)
    # 获取当月的天数
    day = random.randint(1, calendar.monthrange(year, month)[1])
    
    return f"{year}{month:02d}{day:02d}"

# 生成随机有效期
def generate_valid_period():
    start = datetime.now()
    end = start + timedelta(days=365 * random.randint(1, 10))
    return f"{start.strftime('%Y.%m.%d')}-{end.strftime('%Y.%m.%d')}"

# 生成随机民族
def generate_nation():
    nations = ['汉族', '满族', '侗族', '瑶族', '白族', '土家族', '哈尼族',
                '哈萨克族', '傣族', '黎族', '傈僳族', '佤族', '畲族', '高山族',
                '拉祜族', '水族', '东乡族', '纳西族', '景颇族', '柯尔克孜族', '土族',
                '达斡尔族', '仫佬族', '羌族', '布朗族', '撒拉族', '毛难族', '仡佬族',
                '锡伯族', '阿昌族', '普米族', '塔吉克族', '怒族', '乌孜别克族', '俄罗斯族',
                '鄂温克族', '崩龙族', '保安族', '裕固族', '京族', '塔塔尔族', '独龙族',
                '鄂伦春族', '赫哲族', '门巴族', '珞巴族', '基诺族', '蒙古族', '回族',
                '藏族', '维吾尔族', '苗族', '彝族', '壮族', '布依族', '朝鲜族']
    return random.choice(nations)

# 从地址提取省份和城市用于签发机关
def extract_city_from_address(address):
    """从地址提取省份和城市信息，用于生成签发机关"""
    import re
    # 使用正则表达式提取省份和城市
    # 匹配常见的省份和城市格式，将"自治区"作为整体匹配
    pattern = r'^(.*?(?:省|市|自治区))(.*?(?:市|县|区))'
    match = re.match(pattern, address)
    
    if match:
        province_part = match.group(1)
        city_part = match.group(2)
        
        # 处理省份部分，移除后缀
        province = province_part.replace('省', '').replace('市', '').replace('自治区', '')
        
        # 处理城市部分，移除后缀
        city = city_part.replace('市', '').replace('区', '').replace('县', '')
        
        # 生成签发机关
        return f"{province}省{city}市公安局"
    return generate_issue_authority()  #  fallback to original method

# 生成随机签发机关
def generate_issue_authority():
    """生成随机签发机关"""
    provinces = ['北京', '上海', '广东', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南', 
                 '湖北', '湖南', '广西', '海南', '四川', '贵州', '云南', '陕西', '甘肃', '青海', 
                 '宁夏', '新疆', '黑龙江', '吉林', '辽宁', '内蒙古', '天津', '河北', '山西', '重庆']
    province = random.choice(provinces)
    return f"{province}省{fake.city()}公安局"

# 生成身份证有效期
def generate_id_valid_period():
    """生成身份证有效期"""
    start_date = datetime.now() - timedelta(days=random.randint(365*1, 365*10))
    # 16-25岁：10年；26-45岁：20年；46岁以上：长期
    age = random.randint(16, 60)
    if age >= 46:
        return f"{start_date.strftime('%Y.%m.%d')}-长期"
    elif age >= 26:
        end_date = start_date + timedelta(days=365*20)
        return f"{start_date.strftime('%Y.%m.%d')}-{end_date.strftime('%Y.%m.%d')}"
    else:
        end_date = start_date + timedelta(days=365*10)
        return f"{start_date.strftime('%Y.%m.%d')}-{end_date.strftime('%Y.%m.%d')}"

# 生成身份证测试数据
def generate_id_card_data():
    name = generate_name()
    nation = generate_nation()
    address = generate_address()
    id_number = generate_id_number()
    birth = extract_birth_date(id_number)
    gender = extract_gender(id_number)
    issue_authority = extract_city_from_address(address)
    valid_period = generate_id_valid_period()
    
    return {
        'type': 'idcard',
        'name': name,
        'gender': gender,
        'nation': nation,
        'birth': birth,
        'address': address,
        'idNumber': id_number,
        'issueAuthority': issue_authority,
        'validPeriod': valid_period
    }

# 生成驾驶证测试数据
def generate_driving_license_data(id_card_data=None):
    if id_card_data:
        # 使用身份证数据中的信息
        name = id_card_data['name']
        gender = id_card_data['gender']
        birth = id_card_data['birth']
        address = id_card_data['address']
    else:
        # 如果没有提供身份证数据，生成默认数据
        name = generate_name()
        id_number = generate_id_number()
        gender = extract_gender(id_number)
        birth = extract_birth_date(id_number)
        address = generate_address()
    
    nation = '中国'
    first_issue_date = generate_date(2010, 2020)
    driving_type = random.choice(['C1', 'B2', 'A1', 'A2'])
    valid_period = generate_valid_period()
    
    return {
        'type': 'driving',
        'name': name,
        'gender': gender,
        'nation': nation,
        'address': address,
        'birth': birth,
        'firstIssueDate': first_issue_date,
        'drivingType': driving_type,
        'validPeriod': valid_period
    }

# 生成维修资格证测试数据
def generate_repair_cert_data(id_card_data=None):
    cert_number = f"WX{random.randint(100000, 999999)}"
    
    if id_card_data:
        # 使用身份证数据中的信息
        name = id_card_data['name']
        id_number = id_card_data['idNumber']
        gender = id_card_data['gender']
        birth = id_card_data['birth']
    else:
        # 如果没有提供身份证数据，生成默认数据
        name = generate_name()
        id_number = generate_id_number()
        gender = extract_gender(id_number)
        birth = extract_birth_date(id_number)
    
    level = random.choice(['初级', '中级', '高级'])
    repair_type = random.choice(['汽车维修', '机械维修', '电气维修'])
    issue_date = generate_date(2015, 2025)
    valid_until = (datetime.now() + timedelta(days=365 * 5)).strftime('%Y%m%d')
    
    return {
        'type': 'repair',
        'certNumber': cert_number,
        'name': name,
        'gender': gender,
        'birth': birth,
        'idNumber': id_number,
        'level': level,
        'repairType': repair_type,
        'issueDate': issue_date,
        'validUntil': valid_until
    }

# 生成应急救援证测试数据
def generate_emergency_cert_data(id_card_data=None):
    cert_number = f"YJ{random.randint(100000, 999999)}"
    
    if id_card_data:
        # 使用身份证数据中的信息
        name = id_card_data['name']
        id_number = id_card_data['idNumber']
        gender = id_card_data['gender']
        birth = id_card_data['birth']
    else:
        # 如果没有提供身份证数据，生成默认数据
        name = generate_name()
        id_number = generate_id_number()
        gender = extract_gender(id_number)
        birth = extract_birth_date(id_number)
    
    training_project = random.choice(['矿山应急救援', '消防应急救援', '医疗应急救援'])
    valid_period = generate_valid_period()
    
    return {
        'type': 'emergency',
        'certNumber': cert_number,
        'name': name,
        'gender': gender,
        'birth': birth,
        'idNumber': id_number,
        'trainingProject': training_project,
        'validPeriod': valid_period
    }

# 生成行驶证测试数据
def generate_vehicle_license_data():
    # 生成符合标准的车牌号，包括矿山的救援车和维修车
    provinces = ['京', '沪', '粤', '苏', '浙', '皖', '闽', '赣', '鲁', '豫', '鄂', '湘', '桂', '琼', '川', '贵', '云', '陕', '甘', '青', '宁', '新', '黑', '吉', '辽', '蒙', '津', '冀', '晋', '渝']
    province = random.choice(provinces)
    
    # 随机选择车辆类型：普通车辆、救援车、维修车
    vehicle_type = random.choice(['normal', 'rescue', 'repair'])
    
    if vehicle_type == 'normal':
        # 普通车辆车牌号
        license_number = f"{province}{chr(random.randint(65, 90))}{random.randint(10000, 99999)}"
        usage = "非营运"
    elif vehicle_type == 'rescue':
        # 救援车车牌号（矿山救援）
        license_number = f"{province}J{chr(random.randint(65, 90))}{random.randint(1000, 9999)}"
        usage = "矿山救援"
    else:
        # 维修车车牌号
        license_number = f"{province}W{chr(random.randint(65, 90))}{random.randint(1000, 9999)}"
        usage = "矿山维修"
    
    owner = "MineGuard Org"  # 固定所有人为MineGuard Org
    address = generate_address()
    
    # 车辆品牌名列表
    vehicle_brands = ['北京', '一汽', '东风', '上汽', '广汽', '奇瑞', '吉利', '比亚迪', '长城', '长安', '江淮', '江铃', '福田', '解放', '重汽']
    brand = random.choice(vehicle_brands)
    
    # 品牌型号采用车辆品牌名-随机代码形式
    vehicle_model = f"{brand}-{generate_random_string(4)}"
    
    # 车辆型号
    vehicle_type_name = random.choice(['重型卡车', '中型卡车', '轻型卡车', '越野车', '轿车'])
    
    # 发动机型号为随机字符串
    engine_number = f"{generate_random_string(10)}"
    
    vehicle_identification_number = f"VIN{generate_random_string(17)}"
    register_date = generate_date(2015, 2023)
    issue_date = register_date
    
    return {
        'type': 'vehicle',
        'licenseNumber': license_number,
        'owner': owner,
        'address': address,
        'vehicleModel': vehicle_model,
        'vehicleTypeName': vehicle_type_name,
        'usage': usage,
        'engineNumber': engine_number,
        'vehicleIdentificationNumber': vehicle_identification_number,
        'registerDate': register_date,
        'issueDate': issue_date
    }

# 生成图像文件
def create_image(width, height, background_color=(255, 255, 255)):
    """创建一个空白图像"""
    return Image.new('RGB', (width, height), background_color)

# 获取字体
def get_font(size=12):
    """获取系统字体，如果找不到则使用默认字体"""
    # 首先尝试从脚本所在目录的font子目录加载字体
    script_dir = os.path.dirname(os.path.abspath(__file__))
    font_dir = os.path.join(script_dir, 'font')
    
    # 检查font目录是否存在，如果存在则尝试加载其中的字体
    if os.path.exists(font_dir):
        font_files = [f for f in os.listdir(font_dir) if f.endswith(('.ttf', '.ttc'))]
        for font_file in font_files:
            font_path = os.path.join(font_dir, font_file)
            try:
                return ImageFont.truetype(font_path, size)
            except IOError:
                continue
    
    # 然后尝试Windows系统中常用的中文字体
    fonts = [
        'c:\\Windows\\Fonts\\simsun.ttc',  # 宋体
        'c:\\Windows\\Fonts\\simhei.ttf',  # 黑体
        'c:\\Windows\\Fonts\\msyh.ttf',    # 微软雅黑
        'c:\\Windows\\Fonts\\msyhbd.ttf',  # 微软雅黑 Bold
        'arial.ttf'  # 最后尝试英文字体
    ]
    
    for font_path in fonts:
        try:
            return ImageFont.truetype(font_path, size)
        except IOError:
            continue
    
    # 如果所有字体都失败，使用默认字体
    return ImageFont.load_default()

# 绘制身份证正面
def draw_id_card(data, output_path):
    """绘制身份证正面图像"""
    img = create_image(600, 400)
    draw = ImageDraw.Draw(img)
    
    # 绘制边框
    draw.rectangle([10, 10, 590, 390], outline=(0, 0, 0), width=2)
    
    # 绘制标题
    font_title = get_font(24)
    draw.text((170, 30), "中华人民共和国居民身份证", fill=(0, 0, 0), font=font_title)

    # 绘制个人信息
    font = get_font(16)
    y_offset = 80
    line_height = 30
    
    draw.text((50, y_offset), f"姓名：  {data['name']}", fill=(0, 0, 0), font=font)
    y_offset += line_height

    draw.text((50, y_offset), f"性别：  {data['gender']}", fill=(0, 0, 0), font=font)
    draw.text((350, y_offset), f"民族：  {data['nation']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 修改出生日期格式为"xxxx年xx月xx日"
    birth_date = format_date(data['birth'])
    draw.text((50, y_offset), f"出生日期：  {birth_date}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"住址：  {data['address']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    y_offset += line_height
    
    draw.text((50, y_offset), f"公民身份号码：  {data['idNumber']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 保存图像
    img.save(output_path)

# 绘制身份证背面
def draw_id_card_back(data, output_path):
    """绘制身份证背面图像"""
    img = create_image(600, 400)
    draw = ImageDraw.Draw(img)
    
    # 绘制边框
    draw.rectangle([10, 10, 590, 390], outline=(0, 0, 0), width=2)
    
    # 绘制标题
    font_title = get_font(24)
    draw.text((170, 30), "中华人民共和国居民身份证", fill=(0, 0, 0), font=font_title)
    
    # 绘制副标题
    font_subtitle = get_font(18)
    draw.text((260, 70), "（背面）", fill=(0, 0, 0), font=font_subtitle)

    # 绘制个人信息
    font = get_font(16)
    y_offset = 120
    line_height = 35
    
    # 签发机关
    draw.text((50, y_offset), f"签发机关：  {data['issueAuthority']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 有效期限
    valid_period = data['validPeriod']
    draw.text((50, y_offset), f"有效期限：  {valid_period}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 绘制底部说明文字
    font_small = get_font(12)
    draw.text((50, 320), "公民身份号码：", fill=(100, 100, 100), font=font_small)
    # 身份证号在一行显示
    draw.text((150, 320), data['idNumber'], fill=(0, 0, 0), font=font)
    
    # 保存图像
    img.save(output_path)

# 绘制驾驶证
def draw_driving_license(data, output_path):
    """绘制驾驶证图像"""
    img = create_image(600, 400)
    draw = ImageDraw.Draw(img)
    
    # 绘制边框
    draw.rectangle([10, 10, 590, 390], outline=(0, 0, 0), width=2)
    
    # 绘制标题
    font_title = get_font(24)
    draw.text((170, 30), "中华人民共和国机动车驾驶证", fill=(0, 0, 0), font=font_title)
    
    # 绘制个人信息
    font = get_font(16)
    y_offset = 80
    line_height = 30
    
    draw.text((50, y_offset), f"姓名：  {data['name']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"性别：  {data['gender']}", fill=(0, 0, 0), font=font)
    draw.text((350, y_offset), f"国籍：  {data['nation']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"住址：  {data['address']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    y_offset += line_height
    
    # 格式化出生日期
    birth_date = format_date(data['birth'])
    draw.text((50, y_offset), f"出生日期：  {birth_date}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 格式化初次领证日期
    first_issue_date = format_date(data['firstIssueDate'])
    draw.text((50, y_offset), f"初次领证日期：  {first_issue_date}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"准驾车型：  {data['drivingType']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 格式化有效期限
    valid_period = format_valid_period(data['validPeriod'])
    draw.text((50, y_offset), f"有效期限：  {valid_period}", fill=(0, 0, 0), font=font)
    
    # 保存图像
    img.save(output_path)

# 绘制维修资格证
def draw_repair_cert(data, output_path):
    """绘制维修资格证图像"""
    # 创建资格证尺寸的图像（简化版）
    img = create_image(600, 400)
    draw = ImageDraw.Draw(img)
    
    # 绘制边框
    draw.rectangle([10, 10, 590, 390], outline=(0, 0, 0), width=2)
    
    # 绘制标题
    font_title = get_font(24)
    draw.text((230, 30), "维修资格证书", fill=(0, 0, 0), font=font_title)
    
    # 绘制证书信息
    font = get_font(16)
    y_offset = 80
    line_height = 30
    
    draw.text((50, y_offset), f"证书编号：  {data['certNumber']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"姓名：  {data['name']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"身份证号：  {data['idNumber']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"资格等级：  {data['level']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"维修类别：  {data['repairType']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 格式化发证日期
    issue_date = format_date(data['issueDate'])
    draw.text((50, y_offset), f"发证日期：  {issue_date}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 格式化有效期至
    valid_until = format_date(data['validUntil'])
    draw.text((50, y_offset), f"有效期至：  {valid_until}", fill=(0, 0, 0), font=font)
    
    # 保存图像
    img.save(output_path)

# 绘制应急救援证
def draw_emergency_cert(data, output_path):
    """绘制应急救援证图像"""
    # 创建应急救援证尺寸的图像（简化版）
    img = create_image(600, 400)
    draw = ImageDraw.Draw(img)
    
    # 绘制边框
    draw.rectangle([10, 10, 590, 390], outline=(0, 0, 0), width=2)
    
    # 绘制标题
    font_title = get_font(24)
    draw.text((200, 30), "应急救援培训证书", fill=(0, 0, 0), font=font_title)
    
    # 绘制证书信息
    font = get_font(16)
    y_offset = 80
    line_height = 30
    
    draw.text((50, y_offset), f"证书编号：  {data['certNumber']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"持证人姓名：  {data['name']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"身份证号：  {data['idNumber']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"培训项目：  {data['trainingProject']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 格式化有效期
    valid_period = format_valid_period(data['validPeriod'])
    draw.text((50, y_offset), f"有效期：  {valid_period}", fill=(0, 0, 0), font=font)
    
    # 保存图像
    img.save(output_path)

# 绘制行驶证
def draw_vehicle_license(data, output_path):
    """绘制行驶证图像"""
    # 创建行驶证尺寸的图像（简化版）
    img = create_image(600, 400)
    draw = ImageDraw.Draw(img)
    
    # 绘制边框
    draw.rectangle([10, 10, 590, 390], outline=(0, 0, 0), width=2)
    
    # 绘制标题
    font_title = get_font(24)
    draw.text((170, 30), "中华人民共和国机动车行驶证", fill=(0, 0, 0), font=font_title)
    
    # 绘制车辆信息
    font = get_font(16)
    y_offset = 80
    line_height = 30
    
    draw.text((50, y_offset), f"号牌号码：  {data['licenseNumber']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"所有人：  {data['owner']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"住址：  {data['address']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"品牌型号：  {data['vehicleModel']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"车辆型号：  {data['vehicleTypeName']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"发动机号：  {data['engineNumber']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"车辆识别代号：  {data['vehicleIdentificationNumber']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"使用性质：  {data['usage']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 格式化注册日期
    register_date = format_date(data['registerDate'])
    draw.text((50, y_offset), f"注册日期：  {register_date}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 格式化发证日期
    issue_date = format_date(data['issueDate'])
    draw.text((50, y_offset), f"发证日期：  {issue_date}", fill=(0, 0, 0), font=font)
    
    # 保存图像
    img.save(output_path)

# 绘制行驶证背面
def draw_vehicle_license_back(data, output_path):
    """绘制行驶证背面图像"""
    # 创建行驶证尺寸的图像（简化版）
    img = create_image(600, 400)
    draw = ImageDraw.Draw(img)
    
    # 绘制边框
    draw.rectangle([10, 10, 590, 390], outline=(0, 0, 0), width=2)
    
    # 绘制标题
    font_title = get_font(24)
    draw.text((120, 30), "中华人民共和国机动车行驶证（副页）", fill=(0, 0, 0), font=font_title)
    
    # 绘制车辆信息
    font = get_font(16)
    y_offset = 80
    line_height = 30
    
    draw.text((50, y_offset), f"号牌号码：  {data['licenseNumber']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"车辆类型：  {data['vehicleTypeName']}", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 生成随机的总质量、整备质量、核定载质量等技术参数
    curb_weight = random.randint(1500, 8000)
    # 确保总质量大于整备质量
    total_mass = curb_weight + random.randint(500, 2000)
    # 确保核定载重量为正数
    rated_load = total_mass - curb_weight - random.randint(100, 300)
    # 确保核定载重量为正数
    if rated_load < 0:
        rated_load = 0
    
    draw.text((50, y_offset), f"总质量：  {total_mass} kg", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"整备质量：  {curb_weight} kg", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    draw.text((50, y_offset), f"核定载质量：  {rated_load} kg", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 生成随机的核定载客人数
    rated_passengers = random.randint(2, 5)
    draw.text((50, y_offset), f"核定载客：  {rated_passengers} 人", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 生成随机的检验记录
    draw.text((50, y_offset), "检验记录：", fill=(0, 0, 0), font=font)
    y_offset += line_height
    
    # 生成一条检验记录
    current_year = datetime.now().year
    year = current_year
    month = random.randint(1, 12)
    draw.text((80, y_offset), f"{year}年{month}月{random.randint(1, 28)}日 检验合格", fill=(0, 0, 0), font=font)
    
    # 保存图像
    img.save(output_path)

# 生成OCR测试文件
def generate_ocr_test_files(person_type, base_output_dir):
    """根据人员类型生成对应证件的测试文件"""
    # 生成身份证数据，用于获取身份证号作为目录名的一部分
    id_card_data = generate_id_card_data()
    id_number = id_card_data['idNumber']
    
    # 根据人员类型生成对应证件
    if person_type == 'admin':
        # 创建输出目录，格式为 base_output_dir/{type}/{id_number}
        output_dir = os.path.join(base_output_dir, person_type, id_number)
        os.makedirs(output_dir, exist_ok=True)
        
        # 管理员：生成身份证正面和背面
        id_card_front_path = os.path.join(output_dir, 'idcard_front.png')
        draw_id_card(id_card_data, id_card_front_path)
        
        id_card_back_path = os.path.join(output_dir, 'idcard_back.png')
        draw_id_card_back(id_card_data, id_card_back_path)
        
        print(f"生成管理员测试文件：")
        print(f"  - {id_card_front_path}")
        print(f"  - {id_card_back_path}")
    
    elif person_type == 'driver':
        # 创建输出目录，格式为 base_output_dir/{type}/{id_number}
        output_dir = os.path.join(base_output_dir, person_type, id_number)
        os.makedirs(output_dir, exist_ok=True)
        
        # 司机：生成身份证（正反面）和驾驶证
        # 将身份证数据传递给驾驶证数据生成函数
        driving_data = generate_driving_license_data(id_card_data)
        
        # 生成身份证正面
        id_card_front_path = os.path.join(output_dir, 'idcard_front.png')
        draw_id_card(id_card_data, id_card_front_path)
        
        # 生成身份证背面
        id_card_back_path = os.path.join(output_dir, 'idcard_back.png')
        draw_id_card_back(id_card_data, id_card_back_path)
        
        # 生成驾驶证
        driving_path = os.path.join(output_dir, 'driving.png')
        draw_driving_license(driving_data, driving_path)
        
        print(f"生成司机测试文件：")
        print(f"  - {id_card_front_path}")
        print(f"  - {id_card_back_path}")
        print(f"  - {driving_path}")
    
    elif person_type == 'safety':
        # 创建输出目录，格式为 base_output_dir/{type}/{id_number}
        output_dir = os.path.join(base_output_dir, person_type, id_number)
        os.makedirs(output_dir, exist_ok=True)
        
        # 安全员：生成身份证（正反面）和应急救援证
        # 将身份证数据传递给应急救援证数据生成函数
        emergency_data = generate_emergency_cert_data(id_card_data)
        
        # 生成身份证正面
        id_card_front_path = os.path.join(output_dir, 'idcard_front.png')
        draw_id_card(id_card_data, id_card_front_path)
        
        # 生成身份证背面
        id_card_back_path = os.path.join(output_dir, 'idcard_back.png')
        draw_id_card_back(id_card_data, id_card_back_path)
        
        # 生成应急救援证
        emergency_path = os.path.join(output_dir, 'emergency.png')
        draw_emergency_cert(emergency_data, emergency_path)
        
        print(f"生成安全员测试文件：")
        print(f"  - {id_card_front_path}")
        print(f"  - {id_card_back_path}")
        print(f"  - {emergency_path}")
    
    elif person_type == 'repair':
        # 创建输出目录，格式为 base_output_dir/{type}/{id_number}
        output_dir = os.path.join(base_output_dir, person_type, id_number)
        os.makedirs(output_dir, exist_ok=True)
        
        # 维修员：生成身份证（正反面）和维修资格证
        # 将身份证数据传递给维修资格证数据生成函数
        repair_data = generate_repair_cert_data(id_card_data)
        
        # 生成身份证正面
        id_card_front_path = os.path.join(output_dir, 'idcard_front.png')
        draw_id_card(id_card_data, id_card_front_path)
        
        # 生成身份证背面
        id_card_back_path = os.path.join(output_dir, 'idcard_back.png')
        draw_id_card_back(id_card_data, id_card_back_path)
        
        # 生成维修资格证
        repair_path = os.path.join(output_dir, 'repair.png')
        draw_repair_cert(repair_data, repair_path)
        
        print(f"生成维修员测试文件：")
        print(f"  - {id_card_front_path}")
        print(f"  - {id_card_back_path}")
        print(f"  - {repair_path}")
    
    elif person_type == 'carId':
        # 车辆：生成行驶证
        # 生成行驶证数据
        vehicle_data = generate_vehicle_license_data()
        license_number = vehicle_data['licenseNumber']
        
        # 创建输出目录，格式为 base_output_dir/{type}/{license_number}
        output_dir = os.path.join(base_output_dir, person_type, license_number)
        os.makedirs(output_dir, exist_ok=True)
        
        # 生成行驶证正面
        vehicle_front_path = os.path.join(output_dir, 'vehicle_front.png')
        draw_vehicle_license(vehicle_data, vehicle_front_path)
        
        # 生成行驶证背面
        vehicle_back_path = os.path.join(output_dir, 'vehicle_back.png')
        draw_vehicle_license_back(vehicle_data, vehicle_back_path)
        
        print(f"生成行驶证测试文件：")
        print(f"  - {vehicle_front_path}")
        print(f"  - {vehicle_back_path}")
        
        # 生成生图提示词并追加到 output/prompt.txt 文件
        prompt_file_path = os.path.join(base_output_dir, 'prompt.txt')
        prompt = f"一辆车辆型号为{vehicle_data['vehicleTypeName']}，使用性质是{vehicle_data['usage']},车牌为{vehicle_data['licenseNumber']}的车辆在平地停放着，侧重车牌清晰"
        
        # 采用追加模式写入文件
        with open(prompt_file_path, 'a', encoding='utf-8') as f:
            f.write(prompt + '\n')
        
        print(f"追加生图提示词到：{prompt_file_path}")
        print(f"提示词：{prompt}")
    
    else:
        print(f"不支持的人员类型：{person_type}")
        return False
    
    return True

# 主函数
def main():
    # 获取脚本所在目录
    script_dir = os.path.dirname(os.path.abspath(__file__))
    default_output = os.path.join(script_dir, 'output')
    
    parser = argparse.ArgumentParser(description='生成OCR测试文件')
    parser.add_argument('-t', '--type', choices=['admin', 'driver', 'safety', 'repair', 'carId'], 
                        required=True, help='人员类型')
    parser.add_argument('-o', '--output', default=default_output, 
                        help='输出目录')
    parser.add_argument('-n', '--number', type=int, default=1, 
                        help='生成数量')
    
    args = parser.parse_args()
    
    # 生成测试文件
    for i in range(args.number):
        success = generate_ocr_test_files(args.type, args.output)
        if not success:
            break
    
    print("\n测试文件生成完成！")

if __name__ == '__main__':
    main()
