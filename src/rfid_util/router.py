from datetime import date, datetime
from enum import Enum

from fastapi import APIRouter
from pydantic import Field, BaseModel

from src.rfid_util.consumableRFIDUtilities import ConsumableRFIDUtilities

rfid = APIRouter()


class BufferTagInfo(BaseModel):
    tag_uid: str = Field(pattern="[\\dA-F]{2}( [\\dA-F]{2}){7}")
    part_number: str
    lot_number: str
    expiration_date: date


class PolymerTagInfo(BaseModel):
    polymer_type: str = Field(pattern="POP[467]")
    tag_uid: str = Field(pattern="[\\dA-F]{2}( [\\dA-F]{2}){7}")
    part_number: str
    lot_number: str
    expiration_date: date


@rfid.post("/polymer_blob")
async def generate_polymer_blob(tag_info: PolymerTagInfo):
    tag_uid = "".join(tag_info.tag_uid.split())
    expiration_date = int(datetime.fromisoformat(tag_info.expiration_date.isoformat()).timestamp())
    blob = ConsumableRFIDUtilities.create_polymer_blob(
        tag_uid=tag_uid,
        polymer_type=tag_info.polymer_type,
        part_num=tag_info.part_number,
        lot_num=tag_info.lot_number,
        expiration_date=expiration_date,
        installation_date=0,
        life_on_instrument=1209600,
        runs_allowed=0,
        samples_allowed=960,
        runs_remaining=0,
        samples_remaining=960,
        bubble_wizard_executions=0,
        array_fill_executions=0,
        micro_liters_remaining=6000,
    )
    return blob


@rfid.post("/anode_buffer_blob")
async def generate_anode_buffer_blob(tag_info: BufferTagInfo):
    tag_uid = "".join(tag_info.tag_uid.split())
    expiration_date = int(datetime.fromisoformat(tag_info.expiration_date.isoformat()).timestamp())
    blob = ConsumableRFIDUtilities.create_buffer_blob(
        tag_uid=tag_uid,
        buffer_type="ABC",
        part_num=tag_info.part_number,
        lot_num=tag_info.lot_number,
        expiration_date=expiration_date,
        installation_date=0,
        life_on_instrument=1209600,
        runs_allowed=0,
        runs_remaining=0,
    )
    return blob


@rfid.post("/cathode_buffer_blob")
def generate_cathode_buffer_blob(tag_info: BufferTagInfo):
    tag_uid = "".join(tag_info.tag_uid.split())
    expiration_date = int(datetime.fromisoformat(tag_info.expiration_date.isoformat()).timestamp())
    blob = ConsumableRFIDUtilities.create_buffer_blob(
        tag_uid=tag_uid,
        buffer_type="CBC",
        part_num=tag_info.part_number,
        lot_num=tag_info.lot_number,
        expiration_date=expiration_date,
        installation_date=0,
        life_on_instrument=1209600,
        runs_allowed=0,
        runs_remaining=0,
    )
    return blob
