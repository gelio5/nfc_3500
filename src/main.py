from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from starlette.requests import Request
from starlette.responses import HTMLResponse
from starlette.staticfiles import StaticFiles
from starlette.templating import Jinja2Templates

from src.rfid_util import rfid_router

app = FastAPI()
app.include_router(rfid_router, prefix="/rfid", tags=["RFID"])
app.mount("/static", StaticFiles(directory="src/static"), name="static")
templates = Jinja2Templates(directory="src/templates")


@app.get("/", response_class=HTMLResponse, tags=["RFID"])
async def root_html(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})
