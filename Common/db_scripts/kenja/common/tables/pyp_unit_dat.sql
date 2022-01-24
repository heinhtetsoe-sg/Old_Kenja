-- $Id: 91d83951fae945112541ab53bacfb492c4930aca $

drop table PYP_UNIT_DAT
create table PYP_UNIT_DAT ( \
    YEAR            VARCHAR (4) not null, \
    SEMESTER        VARCHAR (1) not null, \
    GRADE           VARCHAR (2) not null, \
    UNIT_CD         VARCHAR (1) not null, \
    UNIT_THEME      VARCHAR (270), \
    UNIT_IDEA       VARCHAR (450), \
    REGISTERCD      VARCHAR (10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PYP_UNIT_DAT add constraint PK_PYP_UNIT_DAT primary key (YEAR, SEMESTER, GRADE, UNIT_CD)
