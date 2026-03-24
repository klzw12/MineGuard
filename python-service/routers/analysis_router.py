from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

analysis_router = APIRouter()


class AnalysisRequest(BaseModel):
    analysis_type: str
    data: Dict[str, Any]
    parameters: Optional[Dict[str, Any]] = None


class AnalysisResult(BaseModel):
    result: Dict[str, Any]
    insights: List[str]
    recommendations: List[str]
    status: str = "success"


@analysis_router.post("/driving-behavior", response_model=AnalysisResult)
async def analyze_driving_behavior(request: AnalysisRequest):
    """
    分析驾驶行为数据
    为AI提供结构化的驾驶行为摘要
    """
    try:
        track_points = request.data.get('track_points', [])
        df = pd.DataFrame(track_points)
        
        if df.empty:
            return AnalysisResult(
                result={},
                insights=["无驾驶数据"],
                recommendations=["请提供驾驶轨迹数据"]
            )
        
        insights = []
        recommendations = []
        result = {}
        
        # 1. 速度分析
        if 'speed' in df.columns:
            speed_stats = analyze_speed(df)
            result['speed_analysis'] = speed_stats
            
            if speed_stats['avg_speed'] > 50:
                insights.append(f"平均速度较高({speed_stats['avg_speed']:.1f}km/h)，建议控制车速")
            if speed_stats['max_speed'] > 80:
                insights.append(f"最高速度达到{speed_stats['max_speed']:.1f}km/h，存在超速风险")
            if speed_stats['speeding_rate'] > 10:
                insights.append(f"超速比例{speed_stats['speeding_rate']:.1f}%，需加强安全意识")
                recommendations.append("建议参加安全驾驶培训")
        
        # 2. 加减速分析
        acceleration_stats = analyze_acceleration(df)
        result['acceleration_analysis'] = acceleration_stats
        
        if acceleration_stats['rapid_acceleration_count'] > 5:
            insights.append(f"急加速{acceleration_stats['rapid_acceleration_count']}次，驾驶风格较激进")
            recommendations.append("建议平稳加速，降低油耗")
        if acceleration_stats['rapid_deceleration_count'] > 5:
            insights.append(f"急减速{acceleration_stats['rapid_deceleration_count']}次，可能存在安全隐患")
            recommendations.append("建议保持安全车距，提前预判路况")
        
        # 3. 轨迹分析
        if 'latitude' in df.columns and 'longitude' in df.columns:
            trajectory_stats = analyze_trajectory(df)
            result['trajectory_analysis'] = trajectory_stats
            
            if trajectory_stats['total_distance'] > 200:
                insights.append(f"单次行驶{trajectory_stats['total_distance']:.1f}公里，注意疲劳驾驶")
                recommendations.append("建议中途休息")
        
        # 4. 时间分析
        if 'timestamp' in df.columns:
            time_stats = analyze_time(df)
            result['time_analysis'] = time_stats
            
            if time_stats.get('is_night_drive'):
                insights.append("夜间行驶，需格外注意安全")
            if time_stats['duration_minutes'] > 240:
                insights.append(f"连续驾驶{time_stats['duration_minutes']//60}小时，存在疲劳风险")
                recommendations.append("建议立即休息")
        
        # 5. 综合评分
        score = calculate_driving_score(result)
        result['overall_score'] = score
        
        if score >= 90:
            insights.append(f"驾驶评分{score}分，表现优秀")
        elif score >= 70:
            insights.append(f"驾驶评分{score}分，表现良好")
        else:
            insights.append(f"驾驶评分{score}分，需要改进")
            recommendations.append("建议参加驾驶技能培训")
        
        # 6. 生成AI摘要
        result['ai_summary'] = generate_ai_summary(result, insights)
        
        return AnalysisResult(
            result=result,
            insights=insights,
            recommendations=recommendations
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@analysis_router.get("/driving-behavior/{tripId}")
async def analyze_driving_behavior_by_trip_id(tripId: int):
    """
    通过行程ID分析驾驶行为并返回评分
    """
    try:
        # 这里应该根据tripId从数据库获取驾驶轨迹数据
        # 为了演示，我们生成一些模拟数据
        import random
        
        # 生成模拟的驾驶轨迹数据
        track_points = []
        for i in range(100):
            track_points.append({
                'latitude': 30.0 + random.random() * 0.1,
                'longitude': 114.0 + random.random() * 0.1,
                'speed': random.uniform(20, 80),
                'timestamp': (datetime.now() - timedelta(minutes=i)).isoformat()
            })
        
        df = pd.DataFrame(track_points)
        
        # 分析驾驶行为
        result = {}
        
        # 1. 速度分析
        if 'speed' in df.columns:
            speed_stats = analyze_speed(df)
            result['speed_analysis'] = speed_stats
        
        # 2. 加减速分析
        acceleration_stats = analyze_acceleration(df)
        result['acceleration_analysis'] = acceleration_stats
        
        # 3. 轨迹分析
        if 'latitude' in df.columns and 'longitude' in df.columns:
            trajectory_stats = analyze_trajectory(df)
            result['trajectory_analysis'] = trajectory_stats
        
        # 4. 时间分析
        if 'timestamp' in df.columns:
            time_stats = analyze_time(df)
            result['time_analysis'] = time_stats
        
        # 5. 计算综合评分
        score = calculate_driving_score(result)
        
        return score
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@analysis_router.post("/cost-analysis", response_model=AnalysisResult)
async def analyze_cost(request: AnalysisRequest):
    """
    分析成本数据
    为AI提供结构化的成本分析摘要
    """
    try:
        records = request.data.get('records', [])
        df = pd.DataFrame(records)
        
        if df.empty:
            return AnalysisResult(
                result={},
                insights=["无成本数据"],
                recommendations=[]
            )
        
        insights = []
        recommendations = []
        result = {}
        
        # 1. 成本结构分析
        if 'cost_type' in df.columns and 'amount' in df.columns:
            type_analysis = df.groupby('cost_type')['amount'].agg(['sum', 'mean', 'count'])
            type_analysis = type_analysis.sort_values('sum', ascending=False)
            
            result['cost_structure'] = type_analysis.to_dict()
            
            total_cost = df['amount'].sum()
            for cost_type, row in type_analysis.iterrows():
                percentage = row['sum'] / total_cost * 100
                if percentage > 50:
                    insights.append(f"{cost_type}成本占比{percentage:.1f}%，是主要成本来源")
        
        # 2. 成本趋势分析
        if 'cost_date' in df.columns:
            df['date'] = pd.to_datetime(df['cost_date'])
            daily_cost = df.groupby(df['date'].dt.date)['amount'].sum()
            
            result['daily_trend'] = daily_cost.to_dict()
            
            if len(daily_cost) > 7:
                recent_avg = daily_cost.tail(7).mean()
                previous_avg = daily_cost.head(7).mean()
                change_rate = (recent_avg - previous_avg) / previous_avg * 100
                
                if change_rate > 20:
                    insights.append(f"近期成本上涨{change_rate:.1f}%，需要关注")
                    recommendations.append("建议分析成本上涨原因")
                elif change_rate < -20:
                    insights.append(f"近期成本下降{abs(change_rate):.1f}%，控制效果良好")
        
        # 3. 异常成本检测
        if 'amount' in df.columns:
            Q1 = df['amount'].quantile(0.25)
            Q3 = df['amount'].quantile(0.75)
            IQR = Q3 - Q1
            upper_bound = Q3 + 1.5 * IQR
            
            anomalies = df[df['amount'] > upper_bound]
            if len(anomalies) > 0:
                result['anomalies'] = anomalies.to_dict('records')
                insights.append(f"发现{len(anomalies)}笔异常高额成本支出")
                recommendations.append("建议核实异常支出")
        
        # 4. 生成AI摘要
        result['ai_summary'] = generate_cost_summary(result, insights)
        
        return AnalysisResult(
            result=result,
            insights=insights,
            recommendations=recommendations
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@analysis_router.post("/vehicle-efficiency", response_model=AnalysisResult)
async def analyze_vehicle_efficiency(request: AnalysisRequest):
    """
    分析车辆效率数据
    """
    try:
        records = request.data.get('records', [])
        df = pd.DataFrame(records)
        
        if df.empty:
            return AnalysisResult(
                result={},
                insights=["无车辆效率数据"],
                recommendations=[]
            )
        
        insights = []
        recommendations = []
        result = {}
        
        # 1. 利用率分析
        if 'utilization_rate' in df.columns:
            avg_utilization = df['utilization_rate'].mean()
            result['avg_utilization'] = avg_utilization
            
            if avg_utilization < 60:
                insights.append(f"平均利用率仅{avg_utilization:.1f}%，存在资源浪费")
                recommendations.append("建议优化调度，提高车辆利用率")
            elif avg_utilization > 90:
                insights.append(f"平均利用率{avg_utilization:.1f}%，接近满负荷")
                recommendations.append("建议考虑增加车辆")
        
        # 2. 空载率分析
        if 'idle_rate' in df.columns:
            avg_idle = df['idle_rate'].mean()
            result['avg_idle_rate'] = avg_idle
            
            if avg_idle > 30:
                insights.append(f"平均空载率{avg_idle:.1f}%，空驶成本较高")
                recommendations.append("建议优化路线规划，减少空驶")
        
        # 3. 能耗分析
        if 'fuel_consumption' in df.columns and 'distance' in df.columns:
            df['fuel_per_km'] = df['fuel_consumption'] / df['distance']
            avg_fuel_efficiency = df['fuel_per_km'].mean()
            result['fuel_efficiency'] = avg_fuel_efficiency
            
            high_consumption = df[df['fuel_per_km'] > avg_fuel_efficiency * 1.2]
            if len(high_consumption) > 0:
                insights.append(f"{len(high_consumption)}辆车辆油耗偏高")
                recommendations.append("建议对高油耗车辆进行检修")
        
        return AnalysisResult(
            result=result,
            insights=insights,
            recommendations=recommendations
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@analysis_router.post("/ai-prompt")
async def generate_ai_prompt(request: AnalysisRequest):
    """
    根据分析结果生成AI提示词
    """
    try:
        analysis_type = request.analysis_type
        data = request.data
        
        if analysis_type == "driving":
            prompt = generate_driving_prompt(data)
        elif analysis_type == "cost":
            prompt = generate_cost_prompt(data)
        elif analysis_type == "dispatch":
            prompt = generate_dispatch_prompt(data)
        elif analysis_type == "vehicle":
            prompt = generate_vehicle_prompt(data)
        else:
            prompt = generate_general_prompt(data)
        
        return {"prompt": prompt, "status": "success"}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def analyze_speed(df: pd.DataFrame) -> Dict[str, Any]:
    """分析速度数据"""
    return {
        'avg_speed': float(df['speed'].mean()),
        'max_speed': float(df['speed'].max()),
        'min_speed': float(df['speed'].min()),
        'speed_std': float(df['speed'].std()),
        'speeding_count': int((df['speed'] > 60).sum()),
        'speeding_rate': float((df['speed'] > 60).sum() / len(df) * 100)
    }


def analyze_acceleration(df: pd.DataFrame) -> Dict[str, Any]:
    """分析加减速数据"""
    if 'speed' not in df.columns or len(df) < 2:
        return {'rapid_acceleration_count': 0, 'rapid_deceleration_count': 0}
    
    speed_diff = df['speed'].diff()
    return {
        'rapid_acceleration_count': int((speed_diff > 10).sum()),
        'rapid_deceleration_count': int((speed_diff < -10).sum()),
        'avg_acceleration': float(speed_diff[speed_diff > 0].mean()) if (speed_diff > 0).any() else 0,
        'avg_deceleration': float(speed_diff[speed_diff < 0].mean()) if (speed_diff < 0).any() else 0
    }


def analyze_trajectory(df: pd.DataFrame) -> Dict[str, Any]:
    """分析轨迹数据"""
    total_distance = 0
    for i in range(1, len(df)):
        lat1, lon1 = df.iloc[i-1]['latitude'], df.iloc[i-1]['longitude']
        lat2, lon2 = df.iloc[i]['latitude'], df.iloc[i]['longitude']
        total_distance += haversine_distance(lat1, lon1, lat2, lon2)
    
    return {
        'total_distance': round(total_distance, 2),
        'point_count': len(df),
        'avg_interval_distance': round(total_distance / len(df), 2) if len(df) > 0 else 0
    }


def analyze_time(df: pd.DataFrame) -> Dict[str, Any]:
    """分析时间数据"""
    df['timestamp'] = pd.to_datetime(df['timestamp'])
    start_time = df['timestamp'].min()
    end_time = df['timestamp'].max()
    duration = (end_time - start_time).total_seconds() / 60
    
    return {
        'start_time': start_time.isoformat(),
        'end_time': end_time.isoformat(),
        'duration_minutes': int(duration),
        'is_night_drive': start_time.hour >= 22 or start_time.hour <= 5
    }


def calculate_driving_score(result: Dict[str, Any]) -> int:
    """计算驾驶评分"""
    score = 100
    
    if 'speed_analysis' in result:
        if result['speed_analysis']['speeding_rate'] > 20:
            score -= 20
        elif result['speed_analysis']['speeding_rate'] > 10:
            score -= 10
    
    if 'acceleration_analysis' in result:
        score -= result['acceleration_analysis']['rapid_acceleration_count'] * 2
        score -= result['acceleration_analysis']['rapid_deceleration_count'] * 2
    
    if 'time_analysis' in result:
        if result['time_analysis'].get('is_night_drive'):
            score -= 5
        if result['time_analysis']['duration_minutes'] > 240:
            score -= 10
    
    return max(0, min(100, score))


def haversine_distance(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """计算两点间的距离（公里）"""
    from math import radians, sin, cos, sqrt, atan2
    
    R = 6371
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * atan2(sqrt(a), sqrt(1-a))
    
    return R * c


def generate_ai_summary(result: Dict[str, Any], insights: List[str]) -> str:
    """生成AI摘要"""
    summary_parts = [
        f"驾驶评分: {result.get('overall_score', 0)}分",
        f"平均速度: {result.get('speed_analysis', {}).get('avg_speed', 0):.1f}km/h",
        f"行驶距离: {result.get('trajectory_analysis', {}).get('total_distance', 0):.1f}km",
        f"急加速次数: {result.get('acceleration_analysis', {}).get('rapid_acceleration_count', 0)}",
        f"急减速次数: {result.get('acceleration_analysis', {}).get('rapid_deceleration_count', 0)}"
    ]
    return " | ".join(summary_parts)


def generate_cost_summary(result: Dict[str, Any], insights: List[str]) -> str:
    """生成成本摘要"""
    total = sum(result.get('cost_structure', {}).get('sum', {}).values())
    return f"总成本: {total:.2f}元"


def generate_driving_prompt(data: Dict[str, Any]) -> str:
    """生成驾驶行为分析提示词"""
    return f"""请分析以下驾驶行为数据并提供专业建议:

驾驶数据摘要:
{data.get('ai_summary', '暂无数据')}

关键指标:
- 速度分析: {data.get('speed_analysis', {})}
- 加减速分析: {data.get('acceleration_analysis', {})}
- 轨迹分析: {data.get('trajectory_analysis', {})}

请从以下方面进行分析:
1. 驾驶安全性评估
2. 驾驶习惯分析
3. 改进建议
4. 风险预警"""


def generate_cost_prompt(data: Dict[str, Any]) -> str:
    """生成成本分析提示词"""
    return f"""请分析以下成本数据并提供优化建议:

成本数据摘要:
{data.get('ai_summary', '暂无数据')}

成本结构:
{data.get('cost_structure', {})}

请从以下方面进行分析:
1. 成本结构分析
2. 成本控制建议
3. 优化方向
4. 预算建议"""


def generate_dispatch_prompt(data: Dict[str, Any]) -> str:
    """生成调度分析提示词"""
    return f"""请分析以下调度数据并提供优化建议:

调度数据:
{data}

请从以下方面进行分析:
1. 调度效率评估
2. 资源配置建议
3. 优化方案"""


def generate_vehicle_prompt(data: Dict[str, Any]) -> str:
    """生成车辆分析提示词"""
    return f"""请分析以下车辆运营数据并提供优化建议:

车辆数据:
{data}

请从以下方面进行分析:
1. 车辆效率评估
2. 维护建议
3. 资源优化"""


def generate_general_prompt(data: Dict[str, Any]) -> str:
    """生成通用分析提示词"""
    return f"""请分析以下数据并提供专业建议:

数据内容:
{data}

请提供详细的分析和建议。"""
