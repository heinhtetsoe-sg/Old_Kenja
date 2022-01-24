-- $Id: 991633082f40b358550f042f449e3c2ac80b91b0 $

DROP TABLE SCHREG_OTHER_SYSTEM_USER_DAT_OLD
RENAME TABLE SCHREG_OTHER_SYSTEM_USER_DAT TO SCHREG_OTHER_SYSTEM_USER_DAT_OLD
CREATE TABLE SCHREG_OTHER_SYSTEM_USER_DAT( \
    SYSTEMID            VARCHAR(8)      not null, \
    SCHREGNO            VARCHAR(8)      not null, \
    LOGINID             VARCHAR(26), \
    PASSWORD            VARCHAR(32), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

alter table SCHREG_OTHER_SYSTEM_USER_DAT add constraint PK_SCH_OTHER_SY_D \
primary key (SYSTEMID, SCHREGNO)

INSERT INTO SCHREG_OTHER_SYSTEM_USER_DAT \
    SELECT \
        SYSTEMID        , \
        SCHREGNO        , \
        LOGINID         , \
        PASSWORD        , \
        REGISTERCD      , \
        UPDATED           \
    FROM \
        SCHREG_OTHER_SYSTEM_USER_DAT_OLD
