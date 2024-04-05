import binascii
import time
import zlib

from src.settings import settings


class ConsumableRFIDUtilities:

    def __init__(self): ...

    # ANODE BUFFER FUNCTIONS
    @staticmethod
    def get_anode_buffer_remaining_runs_in_blob(blob: str) -> int:
        remaining_runs = 0
        pieces: list[str] = blob.split(settings.SEPARATOR_CHAR)
        if len(pieces) > 6 and len(pieces[6]) > 8:
            try:
                remaining_runs = int(pieces[6][4:8])
            except ValueError as e:
                print(e)
        return remaining_runs

    @staticmethod
    def set_anode_buffer_remaining_runs_in_blob(tag_uid: str, blob: str, remaining_runs: int) -> str:
        blob = ConsumableRFIDUtilities._remove_checksum(blob)
        remaining_runs = max(remaining_runs, 0)
        pieces: list[str] = blob.split(settings.SEPARATOR_CHAR)

        current_str = str(remaining_runs)
        while len(current_str) < 4:
            current_str = "0" + current_str

        pieces[6] = pieces[6][:4] + current_str
        blob = settings.SEPARATOR_CHAR.join(pieces)
        blob = ConsumableRFIDUtilities.add_checksum(tag_uid, blob)
        return blob

    #  CHECKSUM FUNCTIONS
    @staticmethod
    def _calculate_v1_0_checksum(tag_uid: str, str_start_code: str, blob: str) -> str:
        checksum = 0

        try:
            if tag_uid is not None:
                checksum = binascii.crc32(tag_uid.encode("utf-8"))

            checksum = binascii.crc32(str_start_code.encode("utf-8"), checksum)
            checksum = binascii.crc32(blob.encode("utf-8"), checksum)
        except UnicodeEncodeError as e:
            print(e)

        return str(checksum)

    @staticmethod
    def add_checksum(tag_uid: str, blob: str) -> str:
        checksum = (
            ConsumableRFIDUtilities.calculate_checksum(blob)
            if tag_uid is None
            else ConsumableRFIDUtilities.calculate_checksum(
                tag_uid,
                settings.SECRET_CHECKSUM_STRING,
                blob,
            )
        )
        timestamp = int(time.time())
        blob = f"{blob}#{checksum}#{timestamp}#"
        return blob

    @staticmethod
    def add_checksum(blob: str) -> str:
        return ConsumableRFIDUtilities.add_checksum(None, blob)

    @staticmethod
    def _remove_checksum(blob: str) -> str:
        pieces: list[str] = blob.split(settings.CHECKSUM_SEPARATOR)
        return pieces[0]

    @staticmethod
    def check_checksum(tag_uid: str, blob: str) -> bool:
        pieces: list[str] = blob.split(settings.CHECKSUM_SEPARATOR)
        test_checksum: str = (
            ConsumableRFIDUtilities.calculate_checksum(pieces[0])
            if tag_uid is None
            else ConsumableRFIDUtilities.calculate_checksum(
                tag_uid,
                settings.SECRET_CHECKSUM_STRING,
                pieces[0],
            )
        )
        assert len(pieces) >= 2

        if len(pieces) < 2:
            return False
        else:
            checksum_valid = test_checksum == pieces[1]
            return checksum_valid

    @staticmethod
    def check_v1_0_checksum(tag_uid: str, blob: str) -> bool:
        pieces: list[str] = blob.split(settings.CHECKSUM_SEPARATOR)
        test_checksum: str = (
            ConsumableRFIDUtilities._calculate_v1_0_checksum(pieces[0])
            if tag_uid is None
            else ConsumableRFIDUtilities._calculate_v1_0_checksum(
                tag_uid,
                settings.ORIGINAL_SECRET_CHECKSUM_STRING,
                pieces[0],
            )
        )

        assert len(pieces) >= 2

        if len(pieces) < 2:
            return False
        else:
            checksum_valid = test_checksum == pieces[1]
            return checksum_valid

    @staticmethod
    def check_checksum(blob: str) -> bool:
        return ConsumableRFIDUtilities.check_checksum(None, blob)

    @staticmethod
    def check_timestamp(blob: str) -> bool:
        pieces = blob.split("#")
        if len(pieces) < 3:
            return True
        else:
            timestamp = int(pieces[2], 36)
            return time.time() >= timestamp
