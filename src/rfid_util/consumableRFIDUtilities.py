import binascii
import time

from src.settings import settings


class ConsumableRFIDUtilities:

    @staticmethod
    def _format_number(digit: int, characters: int = 4) -> str:
        current_str = str(digit)
        while len(current_str) < characters:
            current_str = "0" + current_str
        return current_str

    @staticmethod
    def _convert_base(num, to_base=10, from_base=10):
        if isinstance(num, str):
            n = int(num, from_base)
        else:
            n = int(num)
        alphabet = "0123456789abcdefghijklmnopqrstuvwxyz"
        if n < to_base:
            return alphabet[n]
        else:
            return ConsumableRFIDUtilities._convert_base(n // to_base, to_base) + alphabet[n % to_base]

    @staticmethod
    def create_polymer_blob(
        tag_uid: str,
        polymer_type: str,
        part_num: str,
        lot_num: str,
        expiration_date: int,
        installation_date: int,
        life_on_instrument: int,
        runs_allowed: int,
        samples_allowed: int,
        runs_remaining: int,
        samples_remaining: int,
        bubble_wizard_executions: int,
        array_fill_executions: int,
        micro_liters_remaining: int,
    ) -> str:
        blob = (
            f"{polymer_type},{part_num},{lot_num},{int(expiration_date / 60)},"
            f"{int(installation_date / 60)},{int(life_on_instrument / 60)},"
            f"{ConsumableRFIDUtilities._format_number(runs_allowed)}"
            f"{ConsumableRFIDUtilities._format_number(samples_allowed)}"
            f"{ConsumableRFIDUtilities._format_number(runs_remaining)}"
            f"{ConsumableRFIDUtilities._format_number(samples_remaining)}"
            f"{bubble_wizard_executions if 0 <= bubble_wizard_executions <= 9 else 9}"
            f"{array_fill_executions if 0 <= array_fill_executions <= 9 else 9}"
            f"{ConsumableRFIDUtilities._format_number(micro_liters_remaining, 5)}"
        )
        return ConsumableRFIDUtilities.add_checksum(tag_uid, blob)

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
        blob = (
            f"{buffer_type},{part_num},{lot_num},{int(expiration_date / 60)},"
            f"{int(installation_date / 60)},{int(life_on_instrument / 60)},"
            f"{ConsumableRFIDUtilities._format_number(runs_allowed)}"
            f"{ConsumableRFIDUtilities._format_number(runs_remaining)}"
        )

        return ConsumableRFIDUtilities.add_checksum(tag_uid, blob)

    @staticmethod
    def add_checksum(tag_uid: str, blob: str) -> str:
        checksum = ConsumableRFIDUtilities.calculate_checksum(tag_uid, settings.SECRET_CHECKSUM_STRING, blob)
        timestamp = int(time.time() * 1000)
        timestamp = ConsumableRFIDUtilities._convert_base(timestamp, 36, 10)
        blob = f"{blob}#{checksum}#{timestamp}#"
        return blob

    @staticmethod
    def calculate_checksum(tag_uid: str, start_code: str, blob: str) -> str:
        checksum = binascii.crc32(tag_uid.encode("utf-8"))
        checksum = binascii.crc32(start_code.encode("utf-8"), checksum)
        checksum = binascii.crc32(blob.encode("utf-8"), checksum)
        return ConsumableRFIDUtilities._convert_base(checksum, 36, 10)
