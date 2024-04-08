import binascii
import datetime
import time
import zlib

# from builtins import int

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
    def create_anode_buffer_blob(
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
        blob = blob + str(expiration_date / 60) + ","
        blob = blob + str(installation_date / 60) + ","
        blob = blob + str(life_on_instrument / 60) + ","

        runs_allowed = ConsumableRFIDUtilities._format_number_to_4_digit_str(runs_allowed)
        blob = blob + runs_allowed

        runs_remaining = ConsumableRFIDUtilities._format_number_to_4_digit_str(runs_remaining)
        blob = blob + runs_remaining
        blob = ConsumableRFIDUtilities.add_checksum(tag_uid, blob)
        return blob

    @staticmethod
    def get_anode_buffer_type_from_blob(blob: str) -> str:
        buffer_type: str = ""
        pieces: list[str] = blob.split(settings.SEPARATOR_CHAR)
        if len(pieces) > 0:
            buffer_type = pieces[0]
        return buffer_type

    @staticmethod
    def get_anode_buffer_part_number_from_blob(blob: str) -> str:
        part_num = ""
        pieces: list[str] = blob.split(settings.SEPARATOR_CHAR)
        if len(pieces) > 1:
            part_num = pieces[1]
        return part_num

    @staticmethod
    def get_anode_buffer_lot_number_from_blob(blob: str) -> str:
        lot_num = ""
        pieces = blob.split(settings.SEPARATOR_CHAR)
        if len(pieces):
            lot_num = pieces[2]
        return lot_num

    # public
    # static
    # long
    # getAnodeBufferExpirationDateFromBlob(String
    # blob) {
    #     long
    # expDate = 0L;
    # String[]
    # pieces = blob.split(",");
    # if (pieces.length > 3)
    # {
    # try {
    # expDate = new Long(pieces[3]);
    # expDate *= 60000L;
    # } catch (NumberFormatException var5) {
    # }
    # }
    #
    # return expDate;
    # }

    # public
    # static
    # long
    # getAnodeBufferInstallationDateFromBlob(String
    # blob) {
    # long
    # insDate = 0L;
    # String[]
    # pieces = blob.split(",");
    # if (pieces.length > 4) {
    # try {
    # insDate = new Long(pieces[4]);
    # insDate *= 60000L;
    # } catch (NumberFormatException var5) {
    # }
    # }
    #
    # return insDate;
    # }

    # public
    # static
    # String
    # setAnodeBufferInstallationDateInBlob(String
    # tagUID, String
    # blob, long
    # insDate) {
    # blob = removeChecksum(blob);
    # String[]
    # pieces = blob.split(",");
    # pieces[4] = String.valueOf(insDate / 60000L);
    # blob = "";
    #
    # for (int i = 0; i < pieces.length - 1; ++i) {
    #     blob = blob + pieces[i] + ",";
    # }
    #
    # blob = blob + pieces[pieces.length - 1];
    # blob = addChecksum(tagUID, blob);
    # return blob;

    @staticmethod
    def get_anode_buffer_life_on_instrument_from_blob(blob: str) -> int:
        loi = 0
        pieces: list[str] = blob.split(",")
        if len(pieces) > 5:
            try:
                loi = int(pieces[5])
                loi *= 60000
            except ValueError as e:
                print(e)
        return loi

    @staticmethod
    def set_anode_buffer_life_on_instrument_in_blob(cls, tag_uid: str, blob: str, loi: int) -> str:
        blob = cls._remove_checksum(blob)
        pieces = blob.split(settings.SEPARATOR_CHAR)
        pieces[5] = str(loi / 60000)
        blob = settings.SEPARATOR_CHAR.join(blob)
        blob = cls.add_checksum(tag_uid, blob)
        return blob

    @staticmethod
    def get_anode_buffer_allowed_runs_in_blob(blob: str) -> int:
        allowed_runs = 0
        pieces: list[str] = blob.split(settings.SEPARATOR_CHAR)
        if len(pieces) > 6 and len(pieces) > 4:
            try:
                allowed_runs = int(pieces[6][:4])
            except ValueError as e:
                print(e)
        return allowed_runs

    @staticmethod
    def set_anode_buffer_allowed_runs_ib_blob(cls, tag_uid: str, blob: str, allowed_runs: int) -> str:
        blob = cls._remove_checksum(blob)
        allowed_runs = max(allowed_runs, 0)
        pieces: list[str] = blob.split(settings.SEPARATOR_CHAR)
        current_str = cls._format_number_to_4_digit_str(allowed_runs)
        pieces[6] = pieces[6][4:] + current_str
        blob = settings.SEPARATOR_CHAR.join(pieces)
        blob = cls.add_checksum(tag_uid, blob)
        return blob

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
    def set_anode_buffer_remaining_runs_in_blob(cls, tag_uid: str, blob: str, remaining_runs: int) -> str:
        blob = cls._remove_checksum(blob)
        remaining_runs = max(remaining_runs, 0)
        pieces: list[str] = blob.split(settings.SEPARATOR_CHAR)

        current_str = str(remaining_runs)
        while len(current_str) < 4:
            current_str = "0" + current_str

        pieces[6] = pieces[6][:4] + current_str
        blob = settings.SEPARATOR_CHAR.join(pieces)
        blob = cls.add_checksum(tag_uid, blob)
        return blob

    #  CHECKSUM FUNCTIONS

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
    # def add_checksum(blob: str) -> str:
    #     return ConsumableRFIDUtilities.add_checksum(None, blob)

    @staticmethod
    def _remove_checksum(blob: str) -> str:
        pieces: list[str] = blob.split(settings.CHECKSUM_SEPARATOR)
        return pieces[0]

    @staticmethod
    def check_checksum(cls, blob: str, tag_uid: str = None) -> bool:
        pieces: list[str] = blob.split(settings.CHECKSUM_SEPARATOR)
        test_checksum: str = (
            cls.calculate_checksum(pieces[0])
            if tag_uid is None
            else cls.calculate_checksum(
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
    def check_timestamp(blob: str) -> bool:
        pieces = blob.split("#")
        if len(pieces) < 3:
            return True
        else:
            timestamp = int(pieces[2], 36)
            return int(datetime.datetime.now().strftime("milliseconds")) >= timestamp

    @classmethod
    def calculate_checksum(cls, blob: str) -> str:
        return cls._convert_base(binascii.crc32(blob.encode("utf-8")), 36, 10)
