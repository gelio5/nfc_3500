from datetime import date, datetime

from fastapi import APIRouter
from src.rfid_util.consumableRFIDUtilities import ConsumableRFIDUtilities

rfid = APIRouter()


@rfid.get("/anode_buffer_blob")
def generate_anode_buffer_blob(buffer_type: str, part_number: str, lot_number: str, expiration_date: date):
    expiration_date = int(datetime.fromisoformat(expiration_date.isoformat()).timestamp())
    print(expiration_date)
    blob = ConsumableRFIDUtilities.create_anode_buffer_blob(tag_uid="",
                                                            buffer_type=buffer_type,
                                                            part_num=part_number,
                                                            lot_num=lot_number,
                                                            expiration_date=expiration_date,
                                                            installation_date=0,
                                                            life_on_instrument=1209600,
                                                            runs_allowed=0,
                                                            runs_remaining=0)
    return blob
