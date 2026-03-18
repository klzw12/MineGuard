from fastapi import FastAPI, HTTPException
import pandas as pd
import json
import numpy as np

app = FastAPI(title="MineGuard Python Services", description="数据分析和目标检测服务")

@app.post("/api/clean/statistics-data")
async def clean_statistics_data(data: dict):
    """
    统计数据清洗接口
    """
    try:
        # 提取数据
        statistics_data = data.get("data", [])
        
        if not statistics_data:
            raise HTTPException(status_code=400, detail="No data provided")
        
        # 转换为 DataFrame
        df = pd.DataFrame(statistics_data)
        
        # 数据清洗
        # 1. 移除空值
        df = df.dropna()
        
        # 2. 处理异常值
        if "distance" in df.columns:
            df = df[(df["distance"] >= 0) & (df["distance"] <= 10000)]
        
        if "duration" in df.columns:
            df = df[(df["duration"] >= 0) & (df["duration"] <= 86400)]  # 最大24小时
        
        # 3. 处理日期时间
        if "date" in df.columns:
            df["date"] = pd.to_datetime(df["date"])
        
        # 4. 转换为字典格式
        cleaned_data = df.to_dict(orient="records")
        
        # 5. 生成清洗报告
        cleaning_report = {
            "original_count": len(statistics_data),
            "cleaned_count": len(cleaned_data),
            "removed_count": len(statistics_data) - len(cleaned_data),
            "data_quality": {
                "completeness": round(len(cleaned_data) / len(statistics_data) * 100, 2),
                "validity": "数据清洗完成"
            }
        }
        
        return {
            "cleaned_data": cleaned_data,
            "cleaning_report": cleaning_report,
            "status": "success",
            "message": "统计数据清洗完成"
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/clean/cost-data")
async def clean_cost_data(data: dict):
    """
    成本数据清洗接口
    """
    try:
        # 提取数据
        cost_data = data.get("data", [])
        
        if not cost_data:
            raise HTTPException(status_code=400, detail="No data provided")
        
        # 转换为 DataFrame
        df = pd.DataFrame(cost_data)
        
        # 数据清洗
        # 1. 移除空值
        df = df.dropna()
        
        # 2. 处理异常值
        if "amount" in df.columns:
            df = df[(df["amount"] >= 0) & (df["amount"] <= 1000000)]
        
        # 3. 处理日期时间
        if "date" in df.columns:
            df["date"] = pd.to_datetime(df["date"])
        
        # 4. 转换为字典格式
        cleaned_data = df.to_dict(orient="records")
        
        # 5. 生成清洗报告
        cleaning_report = {
            "original_count": len(cost_data),
            "cleaned_count": len(cleaned_data),
            "removed_count": len(cost_data) - len(cleaned_data),
            "data_quality": {
                "completeness": round(len(cleaned_data) / len(cost_data) * 100, 2),
                "validity": "数据清洗完成"
            }
        }
        
        return {
            "cleaned_data": cleaned_data,
            "cleaning_report": cleaning_report,
            "status": "success",
            "message": "成本数据清洗完成"
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/generate/report")
async def generate_report(data: dict):
    """
    生成 Excel 报表
    """
    try:
        # 提取数据
        report_data = data.get("data", [])
        report_type = data.get("type", "statistics")
        
        if not report_data:
            raise HTTPException(status_code=400, detail="No data provided")
        
        # 转换为 DataFrame
        df = pd.DataFrame(report_data)
        
        # 生成文件名
        filename = f"{report_type}_report.xlsx"
        
        # 保存为 Excel
        df.to_excel(filename, index=False)
        
        return {
            "status": "success",
            "file": filename,
            "message": f"报表已生成: {filename}"
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/detect/objects")
async def detect_objects(data: dict):
    """
    YOLO 目标检测接口（模拟实现）
    """
    try:
        # 提取数据
        image_url = data.get("image_url")
        video_url = data.get("video_url")
        
        if not image_url and not video_url:
            raise HTTPException(status_code=400, detail="No image or video URL provided")
        
        # 模拟 YOLO 检测结果
        # 实际实现中，这里应该调用真实的 YOLO 模型
        detected_objects = [
            {"class": "vehicle", "confidence": 0.95, "bounding_box": [100, 200, 300, 400]},
            {"class": "person", "confidence": 0.88, "bounding_box": [50, 150, 150, 250]}
        ]
        
        return {
            "detected_objects": detected_objects,
            "status": "success",
            "message": "目标检测完成"
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/clean/driving-data")
async def clean_driving_data(data: dict):
    """
    驾驶数据清洗接口
    """
    try:
        # 提取轨迹数据
        track_data = data.get("track_data", [])
        driver_id = data.get("driver_id")
        
        if not track_data:
            raise HTTPException(status_code=400, detail="No track data provided")
        
        # 转换为 DataFrame
        df = pd.DataFrame(track_data)
        
        # 数据清洗
        # 1. 移除空值
        df = df.dropna()
        
        # 2. 处理异常值
        if "speed" in df.columns:
            # 移除速度为负值或过大的值
            df = df[(df["speed"] >= 0) & (df["speed"] <= 200)]
        
        if "longitude" in df.columns and "latitude" in df.columns:
            # 移除无效的经纬度
            df = df[(df["longitude"] >= -180) & (df["longitude"] <= 180)]
            df = df[(df["latitude"] >= -90) & (df["latitude"] <= 90)]
        
        # 3. 计算衍生特征
        if "speed" in df.columns and len(df) > 1:
            # 计算速度变化
            df["speed_diff"] = df["speed"].diff()
            # 计算加速度
            if "timestamp" in df.columns:
                df["timestamp"] = pd.to_datetime(df["timestamp"])
                df["time_diff"] = df["timestamp"].diff().dt.total_seconds()
                df["acceleration"] = df["speed_diff"] / df["time_diff"]
                # 移除无效的加速度值
                df = df[(df["acceleration"] >= -20) & (df["acceleration"] <= 20)]
        
        # 4. 转换为字典格式
        cleaned_data = df.to_dict(orient="records")
        
        # 5. 生成清洗报告
        cleaning_report = {
            "driver_id": driver_id,
            "original_count": len(track_data),
            "cleaned_count": len(cleaned_data),
            "removed_count": len(track_data) - len(cleaned_data),
            "data_quality": {
                "completeness": round(len(cleaned_data) / len(track_data) * 100, 2),
                "validity": "数据清洗完成"
            }
        }
        
        return {
            "cleaned_data": cleaned_data,
            "cleaning_report": cleaning_report,
            "status": "success",
            "message": "驾驶数据清洗完成"
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health_check():
    """
    健康检查接口
    """
    return {"status": "healthy", "message": "Python service is running"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8008)
