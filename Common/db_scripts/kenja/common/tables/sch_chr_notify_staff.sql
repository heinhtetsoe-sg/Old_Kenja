-- $Id: 4a41cd40e7b0004616bf30f14ac7b9fb76d5327d $

DROP   TABLE SCH_CHR_NOTIFY_STAFF
CREATE TABLE SCH_CHR_NOTIFY_STAFF \
( \
        YEAR          VARCHAR(4)      NOT NULL, \
        SEMESTER      VARCHAR(1)      NOT NULL, \
        PARENTSEQ     INTEGER         NOT NULL, \
        STAFFCD       VARCHAR(8)      NOT NULL, \
        REGISTERCD    VARCHAR(8), \
        UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCH_CHR_NOTIFY_STAFF \
ADD CONSTRAINT PK_SCH_CHR_NTF_STF  \
PRIMARY KEY  \
( \
YEAR, \
SEMESTER, \
PARENTSEQ, \
STAFFCD \
)


