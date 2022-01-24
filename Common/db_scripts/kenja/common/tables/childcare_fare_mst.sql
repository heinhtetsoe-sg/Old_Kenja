-- $Id: 3926a6e3ce62c2596d6df7f539048249e2b1e6fc $

drop   table CHILDCARE_FARE_MST
create table CHILDCARE_FARE_MST ( \
    FARE_CD         VARCHAR(2) NOT NULL, \
    FARE            INT, \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table CHILDCARE_FARE_MST add constraint PK_CHILD_FARE_MST primary key (FARE_CD)

