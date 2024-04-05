from src.consumableRFIDUtilities import ConsumableRFIDUtilities
import unittest

class TestConsumableRFIDUtilities(unittest.TestCase):
    def test_get_anode_buffer_remaining_runs_in_blob(self):
        self.assertEqual(ConsumableRFIDUtilities.get_anode_buffer_remaining_runs_in_blob("POP4,3751709,0060423,28330379,28085020,20160,00500960004408160005498#icnv4v#lrrhtdm7#1619999999"), 0)

# TODO: add real test cases for all functions which need to be written
