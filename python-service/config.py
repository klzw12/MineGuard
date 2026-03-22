import os
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "MineGuard Python Service"
    debug: bool = False
    
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_db: int = 0
    
    mongo_uri: str = "mongodb://localhost:27017"
    mongo_db: str = "mineguard"
    
    java_service_url: str = "http://localhost:8080"
    
    class Config:
        env_file = ".env"


settings = Settings()
