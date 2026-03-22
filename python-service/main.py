from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from routers import clean_router, export_router, analysis_router

app = FastAPI(
    title="MineGuard Python Service",
    description="数据清洗、报表导出、数据分析服务",
    version="1.0.0"
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
    return {"status": "healthy", "service": "python-service"}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8008)
