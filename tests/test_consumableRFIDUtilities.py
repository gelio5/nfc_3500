from src.rfid_util.consumableRFIDUtilities import ConsumableRFIDUtilities
import unittest

ANODE_BUFFER_VALID_BLOB = "ABC,3751713,0080623,28428480,0,20160,01000050#1rimda3#1qqh288#"
ANODE_BUFFER_TAG_UID = ""


class TestConsumableRFIDUtilities(unittest.TestCase):
    def test_get_anode_buffer_remaining_runs_in_blob(self):
        self.assertEqual(ConsumableRFIDUtilities.get_anode_buffer_remaining_runs_in_blob(ANODE_BUFFER_VALID_BLOB), 50)
        self.assertNotEqual(
            ConsumableRFIDUtilities.get_anode_buffer_remaining_runs_in_blob(ANODE_BUFFER_VALID_BLOB), 100
        )

    def test_get_anode_buffer_allowed_runs_in_blob(self):
        self.assertEqual(ConsumableRFIDUtilities.get_anode_buffer_allowed_runs_in_blob(ANODE_BUFFER_VALID_BLOB), 100)
        self.assertNotEqual(ConsumableRFIDUtilities.get_anode_buffer_allowed_runs_in_blob(ANODE_BUFFER_VALID_BLOB), 50)

    def test_check_timestamp(self):
        self.assertEqual(ConsumableRFIDUtilities.check_timestamp(ANODE_BUFFER_VALID_BLOB), True)

    def test_generate_blob(self):
        self.assertEqual(
            ConsumableRFIDUtilities.check_checksum(
                ConsumableRFIDUtilities.create_anode_buffer_blob(
                    tag_uid="\x74\x51\x23\x80\x91\x80\x07\xE0",
                    buffer_type="ABC",
                    part_num="A053",
                    lot_num="b343",
                    expiration_date=1712437200,
                    installation_date=0,
                    life_on_instrument=0,
                    runs_allowed=0,
                    runs_remaining=0,
                )
            ),
            True,
        )
    def test_get_anode_buffer_part_number_from_blob(self):
        self.assertEqual(ConsumableRFIDUtilities.get_anode_buffer_part_number_from_blob(ANODE_BUFFER_VALID_BLOB), "3751713")
        self.assertEqual(ConsumableRFIDUtilities.get_anode_buffer_lot_number_from_blob(ANODE_BUFFER_VALID_BLOB), "0080623")
        # self.assertNotEqual(ConsumableRFIDUtilities.get_anode_buffer_life_on_instrument_in_blob(ANODE_BUFFER_VALID_BLOB), 60000)

# TODO: add real test cases for all functions which need to be written

# TIP tag this line
