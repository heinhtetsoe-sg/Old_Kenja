-- $Id: fb0307a0307ae140cb97ce6a4fae6451a9ae82e7 $

drop   table ATTEND_DAY_HRATE_DAT
create table ATTEND_DAY_HRATE_DAT ( \
    ATTENDDATE        DATE NOT NULL, \
    GRADE             VARCHAR(2) NOT NULL, \
    HR_CLASS          VARCHAR(3) NOT NULL, \
    EXECUTED          VARCHAR(1), \
    ATTESTOR          VARCHAR(10), \
    REGISTERCD        VARCHAR(10), \
    UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table ATTEND_DAY_HRATE_DAT add constraint PK_ATTE_DAY_HRATE primary key (ATTENDDATE, GRADE, HR_CLASS)

