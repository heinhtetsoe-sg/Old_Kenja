-- $Id: 27c08ab27a825d431bedc7bc856f2cb28703f0a5 $

drop table MEDICAL_PROSTHETICS_NAME_MST_OLD

rename table MEDICAL_PROSTHETICS_NAME_MST to MEDICAL_PROSTHETICS_NAME_MST_OLD

create table MEDICAL_PROSTHETICS_NAME_MST( \
    NAMECD      VARCHAR(3)   NOT NULL, \
    NAME        VARCHAR(90) , \
    DIV         VARCHAR(5)  , \
    REGISTERCD  VARCHAR(10) , \
    UPDATED     TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table MEDICAL_PROSTHETICS_NAME_MST add constraint SQL180322091303470 primary key (NAMECD)

insert into MEDICAL_PROSTHETICS_NAME_MST \
 select  \
    NAMECD, \
    NAME, \
    CAST(NULL AS VARCHAR(1)), \
    REGISTERCD, \
    UPDATED \
 from MEDICAL_PROSTHETICS_NAME_MST_OLD

