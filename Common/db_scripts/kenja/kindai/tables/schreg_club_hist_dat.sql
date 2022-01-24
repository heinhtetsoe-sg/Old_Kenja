--学籍部クラブ履歴データ 2004/7/15
DROP TABLE SCHREG_CLUB_HIST_DAT

CREATE TABLE SCHREG_CLUB_HIST_DAT \
(  \
        "SCHREGNO"              VARCHAR(8)      NOT NULL, \
        "CLUBCD"                VARCHAR(4)      NOT NULL, \
        "SDATE"                 DATE            NOT NULL, \
        "EDATE"                 DATE , \
        "EXECUTIVECD"           VARCHAR(2) , \
        "REMARK"                VARCHAR(60) , \
        "REGISTERCD"            VARCHAR(8), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_CLUB_HIST_DAT  \
ADD CONSTRAINT PK_SCH_CLUB_H_DAT  \
PRIMARY KEY  \
(SCHREGNO,CLUBCD,SDATE)
