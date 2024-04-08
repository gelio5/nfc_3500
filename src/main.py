from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from src.rfid_util import rfid_router

app = FastAPI()
app.include_router(rfid_router, prefix="/rfid", tags=["RFID"])


@app.get("/")
def read_root():
    return {"Hello": "World"}
