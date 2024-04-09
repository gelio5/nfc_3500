import binascii
import time

from src.settings import settings


class ConsumableRFIDUtilities:

    @staticmethod
    def _format_number_to_4_digit_str(digit: int) -> str:
        current_str = str(digit)
        while len(current_str) < 4:
            current_str = "0" + current_str
        return current_str

    @staticmethod
    def _convert_base(num, to_base=10, from_base=10):
        # first convert to decimal number
        if isinstance(num, str):
            n = int(num, from_base)
        else:
            n = int(num)
        # now convert decimal to 'to_base' base
        alphabet = "0123456789abcdefghijklmnopqrstuvwxyz"
        if n < to_base:
            return alphabet[n]
        else:
            return ConsumableRFIDUtilities._convert_base(n // to_base, to_base) + alphabet[n % to_base]

    # ANODE BUFFER FUNCTIONS
    @staticmethod
    def create_buffer_blob(
            tag_uid: str,
            buffer_type: str,
            part_num: str,
            lot_num: str,
            expiration_date: int,
            installation_date: int,
            life_on_instrument: int,
            runs_allowed: int,
            runs_remaining: int,
    ) -> str:
        pass

        blob = ""
        blob = blob + buffer_type + ","
        blob = blob + part_num + ","
        blob = blob + lot_num + ","
        blob = blob + str(int(expiration_date / 60)) + ","
        blob = blob + str(int(installation_date / 60)) + ","
        blob = blob + str(int(life_on_instrument / 60)) + ","

        runs_allowed = ConsumableRFIDUtilities._format_number_to_4_digit_str(runs_allowed)
        blob = blob + runs_allowed

        runs_remaining = ConsumableRFIDUtilities._format_number_to_4_digit_str(runs_remaining)
        blob = blob + runs_remaining
        blob = ConsumableRFIDUtilities.add_checksum(tag_uid, blob)
        return blob

    @staticmethod
    def add_checksum(tag_uid: str, blob: str) -> str:
        checksum = (
            ConsumableRFIDUtilities.calculate_checksum(
                tag_uid,
                settings.SECRET_CHECKSUM_STRING,
                blob
            )
        )
        timestamp = int(time.time())
        blob = f"{blob}#{checksum}#{timestamp}#"
        return blob

    @staticmethod
    def calculate_checksum(tag_uid: str, start_code: str, blob: str) -> str:
        checksum = binascii.crc32(tag_uid.encode("utf-8"))
        checksum = binascii.crc32(start_code.encode("utf-8"), checksum)
        checksum = binascii.crc32(blob.encode("utf-8"), checksum)
        return ConsumableRFIDUtilities._convert_base(checksum, 36, 10)
