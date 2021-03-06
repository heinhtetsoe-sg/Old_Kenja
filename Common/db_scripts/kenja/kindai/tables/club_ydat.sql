--部クラブ年度データ 2004/7/15
--2004/07/20 ARAKAKI 項目追加(ADVISER1,ADVISER2,ADVISER3)
--2004/09/13 minei   項目削除(ADVISER1,ADVISER2,ADVISER3)
DROP TABLE CLUB_YDAT

CREATE TABLE CLUB_YDAT \
(  \
        "YEAR"                  VARCHAR(4)      NOT NULL, \
        "CLUBCD"                VARCHAR(4)      NOT NULL, \
        "REGISTERCD"            VARCHAR(8), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CLUB_YDAT  \
ADD CONSTRAINT PK_CLUB_YDAT  \
PRIMARY KEY  \
(YEAR,CLUBCD)
