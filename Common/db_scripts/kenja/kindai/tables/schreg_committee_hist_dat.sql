--³ØÀÒ°Ñ°÷²ñÍúÎò¥Ç¡¼¥¿ 2004/7/15
--COMMITTEECD size 2¢ª4 2004/11/26 m-yama
DROP TABLE SCHREG_COMMITTEE_HIST_DAT

CREATE TABLE SCHREG_COMMITTEE_HIST_DAT \
(  \
        "YEAR"                  VARCHAR(4)      NOT NULL, \
        "SEQ"                   INTEGER         NOT NULL, \
        "SCHREGNO"              VARCHAR(8), \
        "GRADE"                 VARCHAR(2), \
        "COMMITTEE_FLG"         VARCHAR(1), \
        "COMMITTEECD"           VARCHAR(4), \
        "CHARGENAME"            VARCHAR(30), \
        "EXECUTIVECD"           VARCHAR(2), \
        "REGISTERCD"            VARCHAR(8), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_COMMITTEE_HIST_DAT  \
ADD CONSTRAINT PK_SCH_COMMIT_H_DT  \
PRIMARY KEY  \
(YEAR,SEQ)
