-- $Id: ee1fffdb4e7a2e48d2560cae763bac89a62e9097 $

DROP TABLE MEDEXAM_DET_DETAIL_DAT
CREATE TABLE MEDEXAM_DET_DETAIL_DAT( \
    YEAR            varchar(4)    not null, \
    SCHREGNO        varchar(8)    not null, \
    DET_SEQ         varchar(3)    not null, \
    DET_REMARK1     varchar(300), \
    DET_REMARK2     varchar(300), \
    DET_REMARK3     varchar(300), \
    DET_REMARK4     varchar(300), \
    DET_REMARK5     varchar(300), \
    DET_REMARK6     varchar(300), \
    DET_REMARK7     varchar(300), \
    DET_REMARK8     varchar(300), \
    DET_REMARK9     varchar(300), \
    DET_REMARK10    varchar(300), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MEDEXAM_DET_DETAIL_DAT ADD CONSTRAINT PK_MEDEXAM_DET_DE PRIMARY KEY (YEAR, SCHREGNO, DET_SEQ)