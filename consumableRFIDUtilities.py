import binascii
import time
import zlib


class ConsumableRFIDUtilities:
    SEPARATOR_CHAR = ","
    INSTALL_SEPARATOR = "/"
    CHECKSUM_SEPARATOR = "#"
    UID_TAGINFO_SEPARATOR = "::"
    DATE_DIVISOR = 60000
    MILLISECOND_IN_A_DAY = 86400000
    MAX_RFID_SIZE = 256
    SECRET_CHECKSUM_STRING = "#rF1d*d2Ta&"
    ORIGINAL_SECRET_CHECKSUM_STRING = "&rFId*dAtA%"

    def __init__(self):
        pass

    @staticmethod
    def _calculate_v1_0_checksum(
        tag_uid: str, str_start_code: str, blob: str
    ) -> str:
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
                ConsumableRFIDUtilities.SECRET_CHECKSUM_STRING,
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
        pieces: list[str] = blob.split(
            ConsumableRFIDUtilities.CHECKSUM_SEPARATOR
        )
        return pieces[0]

    @staticmethod
    def check_checksum(tag_uid: str, blob: str) -> bool:
        pieces: list[str] = blob.split(
            ConsumableRFIDUtilities.CHECKSUM_SEPARATOR
        )
        test_checksum: str = (
            ConsumableRFIDUtilities.calculate_checksum(pieces[0])
            if tag_uid is None
            else ConsumableRFIDUtilities.calculate_checksum(
                tag_uid,
                ConsumableRFIDUtilities.SECRET_CHECKSUM_STRING,
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
        pieces: list[str] = blob.split(
            ConsumableRFIDUtilities.CHECKSUM_SEPARATOR
        )
        test_checksum: str = (
            ConsumableRFIDUtilities._calculate_v1_0_checksum(pieces[0])
            if tag_uid is None
            else ConsumableRFIDUtilities._calculate_v1_0_checksum(
                tag_uid,
                ConsumableRFIDUtilities.ORIGINAL_SECRET_CHECKSUM_STRING,
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
