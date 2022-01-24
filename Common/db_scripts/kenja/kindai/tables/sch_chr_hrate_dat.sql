--時間割講座HR出欠データ
DROP TABLE SCH_CHR_HRATE_DAT

CREATE TABLE SCH_CHR_HRATE_DAT  \
(  \
        "EXECUTEDATE"   DATE            NOT NULL,  \
        "PERIODCD"      VARCHAR(1)      NOT NULL,  \
        "CHAIRCD"       VARCHAR(7)      NOT NULL,  \
        "GRADE"         VARCHAR(2)      NOT NULL,  \
        "HR_CLASS"      VARCHAR(3)      NOT NULL,  \
        "EXECUTED"      VARCHAR(1),  \
        "ATTESTOR"      VARCHAR(8),  \
        "REGISTERCD"    VARCHAR(8),  \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCH_CHR_HRATE_DAT  \
ADD CONSTRAINT PK_SCH_CHR_HAT_DAT  \
PRIMARY KEY  \
(EXECUTEDATE,PERIODCD,CHAIRCD,GRADE,HR_CLASS)

