from src.consumableRFIDUtilities import ConsumableRFIDUtilities


def test_calculate_v1_0_checksum():
    assert ConsumableRFIDUtilities._calculate_v1_0_checksum(None, None, None) == ""
# TODO: add real test cases for all functions which need to be written
