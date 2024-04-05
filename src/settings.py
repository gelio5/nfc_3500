from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    SEPARATOR_CHAR: str
    INSTALL_SEPARATOR: str
    CHECKSUM_SEPARATOR: str
    UID_TAGINFO_SEPARATOR: str
    DATE_DIVISOR: int
    MILLISECOND_IN_A_DAY: int
    MAX_RFID_SIZE: int
    SECRET_CHECKSUM_STRING: str
    ORIGINAL_SECRET_CHECKSUM_STRING: str

    model_config = SettingsConfigDict(env_file=".env")


settings = Settings()
