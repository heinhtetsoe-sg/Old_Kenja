-- $Id: 4b1fbf93d6fc5de7551553f9d4df02e51630907a $

DROP TABLE SCHREG_CHALLENGED_STATUSSHEET_TRAINING_DAT
CREATE TABLE SCHREG_CHALLENGED_STATUSSHEET_TRAINING_DAT( \
    SCHREGNO            varchar(8)      not null, \
    START_DATE          date            not null, \
    FINISH_DATE         date            , \
    RECORD_DATE         date            not null, \
    COMPANY_NAME        varchar(150)    , \
    REMARK              varchar(150)    , \
    REGISTERCD          varchar(10)     , \
    UPDATED             timestamp default current timestamp \ 
) IN usr1dms index in idx1dms

ALTER TABLE SCHREG_CHALLENGED_STATUSSHEET_TRAINING_DAT ADD CONSTRAINT PK_SCH_STATUSSHEET_TRAINING_DAT PRIMARY KEY (SCHREGNO, START_DATE)
