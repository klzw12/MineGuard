from fastapi import APIRouter, HTTPException, Query
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import io
import json

export_router = APIRouter()


class ExportRequest(BaseModel):
    data_type: str
    records: List[Dict[str, Any]]
    format: str = "xlsx"
    filename: Optional[str] = None
    include_summary: bool = True
    date_range: Optional[Dict[str, str]] = None


class MultiSheetExportRequest(BaseModel):
    sheets: List[Dict[str, Any]]
    filename: str
    format: str = "xlsx"


@export_router.post("/statistics")
async def export_statistics(request: ExportRequest):
    """
    导出统计数据报表
    支持 xlsx, csv 格式
    """
    try:
        df = pd.DataFrame(request.records)
        
        if df.empty:
            raise HTTPException(status_code=400, detail="无数据可导出")
        
        filename = request.filename or f"statistics_{request.data_type}_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        
        if request.format == "csv":
            return export_csv(df, filename)
        else:
            return export_excel(df, filename, request.include_summary, request.data_type)
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@export_router.post("/trip-report")
async def export_trip_report(request: ExportRequest):
    """
    导出行程报表
    包含详细统计和汇总
    """
    try:
        df = pd.DataFrame(request.records)
        
        if df.empty:
            raise HTTPException(status_code=400, detail="无数据可导出")
        
        filename = request.filename or f"trip_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        
        # 创建多sheet报表
        output = io.BytesIO()
        
        with pd.ExcelWriter(output, engine='openpyxl') as writer:
            # Sheet 1: 原始数据
            df.to_excel(writer, sheet_name='行程明细', index=False)
            
            # Sheet 2: 按车辆汇总
            if 'vehicle_id' in df.columns:
                vehicle_summary = df.groupby('vehicle_id').agg({
                    'id': 'count',
                    'distance': 'sum' if 'distance' in df.columns else 'count',
                    'cargo_weight': 'sum' if 'cargo_weight' in df.columns else 'count'
                }).reset_index()
                vehicle_summary.columns = ['车辆ID', '行程数', '总里程(km)', '总货运量(吨)']
                vehicle_summary.to_excel(writer, sheet_name='车辆汇总', index=False)
            
            # Sheet 3: 按司机汇总
            if 'driver_id' in df.columns:
                driver_summary = df.groupby('driver_id').agg({
                    'id': 'count',
                    'distance': 'sum' if 'distance' in df.columns else 'count',
                    'cargo_weight': 'sum' if 'cargo_weight' in df.columns else 'count'
                }).reset_index()
                driver_summary.columns = ['司机ID', '行程数', '总里程(km)', '总货运量(吨)']
                driver_summary.to_excel(writer, sheet_name='司机汇总', index=False)
            
            # Sheet 4: 按日期汇总
            if 'start_time' in df.columns:
                df['date'] = pd.to_datetime(df['start_time']).dt.date
                date_summary = df.groupby('date').agg({
                    'id': 'count',
                    'distance': 'sum' if 'distance' in df.columns else 'count',
                    'cargo_weight': 'sum' if 'cargo_weight' in df.columns else 'count'
                }).reset_index()
                date_summary.columns = ['日期', '行程数', '总里程(km)', '总货运量(吨)']
                date_summary.to_excel(writer, sheet_name='日期汇总', index=False)
        
        output.seek(0)
        
        headers = {
            'Content-Disposition': f'attachment; filename="{filename}.xlsx"'
        }
        
        return StreamingResponse(
            output,
            media_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            headers=headers
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@export_router.post("/cost-report")
async def export_cost_report(request: ExportRequest):
    """
    导出成本报表
    包含成本分析和预算对比
    """
    try:
        df = pd.DataFrame(request.records)
        
        if df.empty:
            raise HTTPException(status_code=400, detail="无数据可导出")
        
        filename = request.filename or f"cost_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        
        output = io.BytesIO()
        
        with pd.ExcelWriter(output, engine='openpyxl') as writer:
            # Sheet 1: 成本明细
            df.to_excel(writer, sheet_name='成本明细', index=False)
            
            # Sheet 2: 按类型汇总
            if 'cost_type' in df.columns:
                type_summary = df.groupby('cost_type').agg({
                    'amount': ['sum', 'mean', 'count']
                }).reset_index()
                type_summary.columns = ['成本类型', '总金额', '平均金额', '记录数']
                type_summary.to_excel(writer, sheet_name='类型汇总', index=False)
            
            # Sheet 3: 按车辆汇总
            if 'vehicle_id' in df.columns:
                vehicle_summary = df.groupby('vehicle_id').agg({
                    'amount': ['sum', 'mean', 'count']
                }).reset_index()
                vehicle_summary.columns = ['车辆ID', '总成本', '平均成本', '记录数']
                vehicle_summary.to_excel(writer, sheet_name='车辆成本', index=False)
            
            # Sheet 4: 按日期汇总
            if 'cost_date' in df.columns:
                df['date'] = pd.to_datetime(df['cost_date']).dt.date
                date_summary = df.groupby('date').agg({
                    'amount': 'sum'
                }).reset_index()
                date_summary.columns = ['日期', '总成本']
                date_summary.to_excel(writer, sheet_name='日期汇总', index=False)
            
            # Sheet 5: 成本趋势
            if 'cost_date' in df.columns:
                df['month'] = pd.to_datetime(df['cost_date']).dt.to_period('M')
                monthly_trend = df.groupby('month').agg({
                    'amount': 'sum'
                }).reset_index()
                monthly_trend['month'] = monthly_trend['month'].astype(str)
                monthly_trend.columns = ['月份', '总成本']
                monthly_trend.to_excel(writer, sheet_name='月度趋势', index=False)
        
        output.seek(0)
        
        headers = {
            'Content-Disposition': f'attachment; filename="{filename}.xlsx"'
        }
        
        return StreamingResponse(
            output,
            media_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            headers=headers
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@export_router.post("/vehicle-report")
async def export_vehicle_report(request: ExportRequest):
    """
    导出车辆运营报表
    """
    try:
        df = pd.DataFrame(request.records)
        
        if df.empty:
            raise HTTPException(status_code=400, detail="无数据可导出")
        
        filename = request.filename or f"vehicle_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        
        output = io.BytesIO()
        
        with pd.ExcelWriter(output, engine='openpyxl') as writer:
            # Sheet 1: 车辆明细
            df.to_excel(writer, sheet_name='车辆明细', index=False)
            
            # Sheet 2: 运营统计
            stats_data = []
            if 'trip_count' in df.columns:
                stats_data.append(['总行程数', df['trip_count'].sum()])
            if 'total_distance' in df.columns:
                stats_data.append(['总里程(km)', df['total_distance'].sum()])
            if 'cargo_weight' in df.columns:
                stats_data.append(['总货运量(吨)', df['cargo_weight'].sum()])
            if 'fuel_consumption' in df.columns:
                stats_data.append(['总油耗(升)', df['fuel_consumption'].sum()])
            
            if stats_data:
                stats_df = pd.DataFrame(stats_data, columns=['指标', '数值'])
                stats_df.to_excel(writer, sheet_name='运营统计', index=False)
        
        output.seek(0)
        
        headers = {
            'Content-Disposition': f'attachment; filename="{filename}.xlsx"'
        }
        
        return StreamingResponse(
            output,
            media_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            headers=headers
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@export_router.post("/driver-report")
async def export_driver_report(request: ExportRequest):
    """
    导出司机绩效报表
    """
    try:
        df = pd.DataFrame(request.records)
        
        if df.empty:
            raise HTTPException(status_code=400, detail="无数据可导出")
        
        filename = request.filename or f"driver_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        
        output = io.BytesIO()
        
        with pd.ExcelWriter(output, engine='openpyxl') as writer:
            # Sheet 1: 司机明细
            df.to_excel(writer, sheet_name='司机明细', index=False)
            
            # Sheet 2: 绩效排名
            if 'performance_score' in df.columns:
                ranking = df[['user_name', 'performance_score', 'trip_count', 'cargo_weight']].copy()
                ranking = ranking.sort_values('performance_score', ascending=False)
                ranking.columns = ['司机姓名', '绩效分数', '行程数', '货运量(吨)']
                ranking.to_excel(writer, sheet_name='绩效排名', index=False)
            
            # Sheet 3: 违规统计
            if 'violation_count' in df.columns or 'over_speed_count' in df.columns:
                violation_data = []
                for _, row in df.iterrows():
                    if row.get('violation_count', 0) > 0 or row.get('over_speed_count', 0) > 0:
                        violation_data.append({
                            '司机': row.get('user_name', ''),
                            '违规次数': row.get('violation_count', 0),
                            '超速次数': row.get('over_speed_count', 0),
                            '预警次数': row.get('warning_count', 0)
                        })
                if violation_data:
                    violation_df = pd.DataFrame(violation_data)
                    violation_df.to_excel(writer, sheet_name='违规统计', index=False)
        
        output.seek(0)
        
        headers = {
            'Content-Disposition': f'attachment; filename="{filename}.xlsx"'
        }
        
        return StreamingResponse(
            output,
            media_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            headers=headers
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@export_router.post("/multi-sheet")
async def export_multi_sheet(request: MultiSheetExportRequest):
    """
    导出多sheet报表
    """
    try:
        output = io.BytesIO()
        
        with pd.ExcelWriter(output, engine='openpyxl') as writer:
            for sheet in request.sheets:
                df = pd.DataFrame(sheet.get('data', []))
                sheet_name = sheet.get('name', 'Sheet')
                df.to_excel(writer, sheet_name=sheet_name, index=False)
        
        output.seek(0)
        
        headers = {
            'Content-Disposition': f'attachment; filename="{request.filename}.xlsx"'
        }
        
        return StreamingResponse(
            output,
            media_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            headers=headers
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def export_csv(df: pd.DataFrame, filename: str) -> StreamingResponse:
    """导出CSV文件"""
    output = io.StringIO()
    df.to_csv(output, index=False, encoding='utf-8-sig')
    output.seek(0)
    
    # 转换为字节流
    byte_output = io.BytesIO()
    byte_output.write(output.getvalue().encode('utf-8-sig'))
    byte_output.seek(0)
    
    headers = {
        'Content-Disposition': f'attachment; filename="{filename}.csv"'
    }
    
    return StreamingResponse(
        byte_output,
        media_type='text/csv',
        headers=headers
    )


def export_excel(df: pd.DataFrame, filename: str, include_summary: bool, data_type: str) -> StreamingResponse:
    """导出Excel文件"""
    output = io.BytesIO()
    
    with pd.ExcelWriter(output, engine='openpyxl') as writer:
        df.to_excel(writer, sheet_name='数据明细', index=False)
        
        if include_summary:
            # 添加汇总sheet
            summary_data = []
            numeric_cols = df.select_dtypes(include=[np.number]).columns
            
            for col in numeric_cols:
                summary_data.append({
                    '字段': col,
                    '总计': df[col].sum(),
                    '平均': df[col].mean(),
                    '最大': df[col].max(),
                    '最小': df[col].min(),
                    '记录数': df[col].count()
                })
            
            if summary_data:
                summary_df = pd.DataFrame(summary_data)
                summary_df.to_excel(writer, sheet_name='数据汇总', index=False)
    
    output.seek(0)
    
    headers = {
        'Content-Disposition': f'attachment; filename="{filename}.xlsx"'
    }
    
    return StreamingResponse(
        output,
        media_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        headers=headers
    )
