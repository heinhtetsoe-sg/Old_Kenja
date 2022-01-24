-- $Id: 47d91728f22abfc3afaf7c8131d969ecf0b537dd $

DROP   TABLE SCH_CHR_NOTIFY_COUNTFLG
CREATE TABLE SCH_CHR_NOTIFY_COUNTFLG \
( \
        YEAR          VARCHAR(4)      NOT NULL, \
        SEMESTER      VARCHAR(1)      NOT NULL, \
        PARENTSEQ     INTEGER         NOT NULL, \
        GRADE         VARCHAR(2)      NOT NULL, \
        HR_CLASS      VARCHAR(3)      NOT NULL, \
        COUNTFLG      VARCHAR(1)      NOT NULL, \
        LESSON_MODE   VARCHAR(2)      NOT NULL, \
        REGISTERCD    VARCHAR(8), \
        UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCH_CHR_NOTIFY_COUNTFLG \
ADD CONSTRAINT PK_SCH_CHR_NTF_CF  \
PRIMARY KEY  \
( \
YEAR, \
SEMESTER, \
PARENTSEQ, \
GRADE, \
HR_CLASS \
)

