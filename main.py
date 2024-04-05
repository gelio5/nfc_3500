# from Crypto.Hash import HMAC, MD2, MD4, MD5, RIPEMD, SHA, SHA224, SHA256, SHA384, SHA512
import base64

# from Crypto.PublicKey import


uid = b"\xE0\x07\x80\x91\x80\x23\x0C\x66"

clear_data = "POP4,3751709,0060423,28330379,28085020,20160,00500960004408160005498"
first_part_hash = "icnv4v"
second_part_hash = "lrrhtdm7"


def main():
    # print(first_part_hash.encode("utf-8").hex())
    # print(MD2.new(uid, clear_data.encode()).digest()) # PY_SSIZE_T_CLEAN macro must be defined for '#' formats
    # print(MD4.new(clear_data.encode()).digest()) # PY_SSIZE_T_CLEAN macro must be defined for '#' formats
    # md5 = MD5.new(uid)
    # md5.update(clear_data.encode())
    # print(md5.digest())  # NOT THIS
    # md5_2 = MD5.new(clear_data.encode())
    # md5_2.update(uid)
    # print(md5_2.digest())
    # print(md5.digest() == md5_2.digest())
    # # print((HMAC.new(clear_data.encode()).digest())) # No module named MD 5
    # # print((RIPEMD.new(clear_data.encode()).digest())) # PY_SSIZE_T_CLEAN macro must be defined for '#' formats
    # # print(SHA.new(clear_data.encode()).digest()) # NOT THIS
    # # print(SHA224.new(clear_data.encode()).digest())  # NOT THIS
    # # print(SHA256.new(clear_data.encode()).digest())  # NOT THIS
    # # print(SHA384.new(clear_data.encode()).digest())  # NOT THIS
    # # print(SHA512.new(clear_data.encode()).digest())  # NOT THIS
    print(base64.standard_b64decode(clear_data.encode()))


if __name__ == "__main__":
    main()
