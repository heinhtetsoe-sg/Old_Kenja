--管理者コントロールデータ
DROP TABLE ADMIN_CONTROL_DAT
CREATE TABLE ADMIN_CONTROL_DAT  \
(  \
        "YEAR"          VARCHAR(4)      NOT NULL, \
        "CONTROL_FLG"   VARCHAR(1)      NOT NULL, \
        "CONTROL_CODE"  VARCHAR(8)      NOT NULL, \
        "REGISTERCD"    VARCHAR(8),  \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS


ALTER TABLE ADMIN_CONTROL_DAT  \
ADD CONSTRAINT PK_ADMIN_CONL_DAT  \
PRIMARY KEY  \
( \
YEAR, \
CONTROL_FLG, \
CONTROL_CODE \
)

