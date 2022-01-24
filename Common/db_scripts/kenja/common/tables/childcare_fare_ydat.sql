-- $Id: 6bfd4263e5d14bbe6d5ba8dd64bbbcc773194121 $

drop   table CHILDCARE_FARE_YDAT
create table CHILDCARE_FARE_YDAT ( \
    YEAR            VARCHAR(4) NOT NULL, \
    FARE_CD         VARCHAR(2) NOT NULL, \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table CHILDCARE_FARE_YDAT add constraint PK_CHILD_FARE_YDAT primary key (YEAR, FARE_CD)

