from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
import pandas as pd
import numpy as np
from datetime import datetime

clean_router = APIRouter()


class DrivingData(BaseModel):
    driver_id: int
    vehicle_id: int
    track_points: List[Dict[str, Any]]
    start_time: Optional[str] = None
    end_time: Optional[str] = None


class StatisticsData(BaseModel):
    data_type: str
    records: List[Dict[str, Any]]
    date_range: Optional[Dict[str, str]] = None


class CleanResult(BaseModel):
    cleaned_data: Dict[str, Any]
    cleaning_report: Dict[str, Any]
    status: str = "success"


@clean_router.post("/driving-data", response_model=CleanResult)
async def clean_driving_data(data: DrivingData):
    """
    清洗驾驶轨迹数据
    - 移除异常GPS点
    - 平滑速度数据
    - 检测急加速/急减速
    - 计算统计数据
    """
    try:
        df = pd.DataFrame(data.track_points)
        
        if df.empty:
            return CleanResult(
                cleaned_data={"track_points": []},
                cleaning_report={"message": "无数据需要清洗"}
            )
        
        original_count = len(df)
        
        # 1. 移除缺失关键数据的点
        df = df.dropna(subset=['latitude', 'longitude', 'timestamp'])
        
        # 2. 移除异常GPS点（经纬度超出合理范围）
        df = df[
            (df['latitude'] >= -90) & (df['latitude'] <= 90) &
            (df['longitude'] >= -180) & (df['longitude'] <= 180)
        ]
        
        # 3. 移除速度异常点（速度超过200km/h视为异常）
        if 'speed' in df.columns:
            df = df[df['speed'] <= 200]
        
        # 4. 按时间排序
        df = df.sort_values('timestamp')
        
        # 5. 移除重复点
        df = df.drop_duplicates(subset=['timestamp'])
        
        # 6. 计算统计数据
        stats = calculate_driving_stats(df)
        
        cleaned_count = len(df)
        removed_count = original_count - cleaned_count
        
        cleaned_data = {
            "driver_id": data.driver_id,
            "vehicle_id": data.vehicle_id,
            "track_points": df.to_dict('records'),
            "statistics": stats
        }
        
        cleaning_report = {
            "original_points": original_count,
            "cleaned_points": cleaned_count,
            "removed_points": removed_count,
            "removal_rate": round(removed_count / original_count * 100, 2) if original_count > 0 else 0,
            "cleaning_time": datetime.now().isoformat(),
            "operations": [
                "移除缺失数据点",
                "移除异常GPS坐标",
                "移除速度异常点",
                "按时间排序",
                "移除重复点"
            ]
        }
        
        return CleanResult(
            cleaned_data=cleaned_data,
            cleaning_report=cleaning_report
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@clean_router.post("/statistics-data", response_model=CleanResult)
async def clean_statistics_data(data: StatisticsData):
    """
    清洗统计数据
    - 处理缺失值
    - 检测异常值
    - 数据标准化
    """
    try:
        df = pd.DataFrame(data.records)
        
        if df.empty:
            return CleanResult(
                cleaned_data={"records": []},
                cleaning_report={"message": "无数据需要清洗"}
            )
        
        original_count = len(df)
        report = {
            "original_count": original_count,
            "operations": []
        }
        
        # 1. 处理数值列的缺失值
        numeric_cols = df.select_dtypes(include=[np.number]).columns
        for col in numeric_cols:
            missing_count = df[col].isna().sum()
            if missing_count > 0:
                df[col] = df[col].fillna(df[col].median())
                report["operations"].append(f"列 {col} 填充了 {missing_count} 个缺失值")
        
        # 2. 检测并处理异常值（使用IQR方法）
        for col in numeric_cols:
            Q1 = df[col].quantile(0.25)
            Q3 = df[col].quantile(0.75)
            IQR = Q3 - Q1
            lower_bound = Q1 - 1.5 * IQR
            upper_bound = Q3 + 1.5 * IQR
            
            outliers = df[(df[col] < lower_bound) | (df[col] > upper_bound)]
            if len(outliers) > 0:
                df.loc[(df[col] < lower_bound), col] = lower_bound
                df.loc[(df[col] > upper_bound), col] = upper_bound
                report["operations"].append(f"列 {col} 处理了 {len(outliers)} 个异常值")
        
        # 3. 移除完全重复的行
        duplicates = df.duplicated().sum()
        if duplicates > 0:
            df = df.drop_duplicates()
            report["operations"].append(f"移除了 {duplicates} 个重复行")
        
        report["cleaned_count"] = len(df)
        report["cleaning_time"] = datetime.now().isoformat()
        
        cleaned_data = {
            "data_type": data.data_type,
            "records": df.to_dict('records'),
            "date_range": data.date_range
        }
        
        return CleanResult(
            cleaned_data=cleaned_data,
            cleaning_report=report
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@clean_router.post("/cost-data", response_model=CleanResult)
async def clean_cost_data(data: StatisticsData):
    """
    清洗成本数据
    - 验证金额格式
    - 处理负值
    - 分类统计
    """
    try:
        df = pd.DataFrame(data.records)
        
        if df.empty:
            return CleanResult(
                cleaned_data={"records": []},
                cleaning_report={"message": "无数据需要清洗"}
            )
        
        original_count = len(df)
        report = {
            "original_count": original_count,
            "operations": []
        }
        
        # 1. 处理金额列
        if 'amount' in df.columns:
            df['amount'] = pd.to_numeric(df['amount'], errors='coerce')
            negative_count = (df['amount'] < 0).sum()
            if negative_count > 0:
                df['amount'] = df['amount'].abs()
                report["operations"].append(f"修正了 {negative_count} 个负值金额")
            
            df['amount'] = df['amount'].fillna(0)
        
        # 2. 标准化日期格式
        if 'cost_date' in df.columns:
            df['cost_date'] = pd.to_datetime(df['cost_date'], errors='coerce')
            df['cost_date'] = df['cost_date'].fillna(method='ffill')
        
        # 3. 按类型汇总
        if 'cost_type' in df.columns:
            type_summary = df.groupby('cost_type')['amount'].sum().to_dict()
            report["type_summary"] = type_summary
        
        report["cleaned_count"] = len(df)
        report["total_amount"] = float(df['amount'].sum()) if 'amount' in df.columns else 0
        report["cleaning_time"] = datetime.now().isoformat()
        
        cleaned_data = {
            "data_type": "cost",
            "records": df.to_dict('records')
        }
        
        return CleanResult(
            cleaned_data=cleaned_data,
            cleaning_report=report
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def calculate_driving_stats(df: pd.DataFrame) -> Dict[str, Any]:
    """计算驾驶统计数据"""
    stats = {}
    
    if 'speed' in df.columns:
        stats['avg_speed'] = float(df['speed'].mean())
        stats['max_speed'] = float(df['speed'].max())
        stats['speed_std'] = float(df['speed'].std())
        
        # 检测超速（假设限速60km/h）
        speeding_count = (df['speed'] > 60).sum()
        stats['speeding_count'] = int(speeding_count)
        stats['speeding_rate'] = float(speeding_count / len(df) * 100) if len(df) > 0 else 0
    
    # 计算急加速/急减速
    if 'speed' in df.columns and len(df) > 1:
        speed_diff = df['speed'].diff()
        stats['rapid_acceleration_count'] = int((speed_diff > 10).sum())
        stats['rapid_deceleration_count'] = int((speed_diff < -10).sum())
    
    # 计算行驶距离
    if 'latitude' in df.columns and 'longitude' in df.columns:
        distances = []
        for i in range(1, len(df)):
            lat1, lon1 = df.iloc[i-1]['latitude'], df.iloc[i-1]['longitude']
            lat2, lon2 = df.iloc[i]['latitude'], df.iloc[i]['longitude']
            dist = haversine_distance(lat1, lon1, lat2, lon2)
            distances.append(dist)
        stats['total_distance'] = sum(distances)
    
    # 计算驾驶评分
    score = 100
    if 'speeding_count' in stats:
        score -= stats['speeding_count'] * 2
    if 'rapid_acceleration_count' in stats:
        score -= stats['rapid_acceleration_count'] * 1
    if 'rapid_deceleration_count' in stats:
        score -= stats['rapid_deceleration_count'] * 1
    stats['driving_score'] = max(0, min(100, score))
    
    return stats


def haversine_distance(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """计算两点间的距离（公里）"""
    from math import radians, sin, cos, sqrt, atan2
    
    R = 6371  # 地球半径（公里）
    
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * atan2(sqrt(a), sqrt(1-a))
    
    return R * c
