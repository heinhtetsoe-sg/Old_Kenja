
-- $Id: 0eb61be94db2214fb4c1a6fd2a92ffaec9c49499 $

DROP TABLE KAIKIN_EXEC_DAT_OLD
RENAME TABLE KAIKIN_EXEC_DAT TO KAIKIN_EXEC_DAT_OLD

create table KAIKIN_EXEC_DAT( \
    YEAR        varchar(4) not null, \
    GRADE       varchar(2) not null, \
    HR_CLASS    varchar(3) not null, \
    EXEC_TIME   timestamp  not null, \
    BASE_DATE   date               , \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table KAIKIN_EXEC_DAT add constraint PK_KAIKIN_EXEC_DAT primary key (YEAR, GRADE, HR_CLASS, EXEC_TIME)

INSERT INTO KAIKIN_EXEC_DAT \
    SELECT \
        KOLD.YEAR        , \
        KOLD.GRADE       , \
        '000' as HR_CLASS    , \
        KOLD.EXEC_TIME   , \
        SEM.EDATE as BASE_DATE   , \
        KOLD.REGISTERCD  , \
        KOLD.UPDATED       \
    FROM                    \
        KAIKIN_EXEC_DAT_OLD AS KOLD \
    LEFT JOIN \
        SEMESTER_MST SEM ON KOLD.YEAR = SEM.YEAR \
                        AND SEM.SEMESTER = '9'
