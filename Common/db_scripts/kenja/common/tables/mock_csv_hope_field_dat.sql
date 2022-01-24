-- $Id: be2fed26204c22b73857a5568c0a2cbbb7f2eb1c $

drop table MOCK_CSV_HOPE_FIELD_DAT
create table MOCK_CSV_HOPE_FIELD_DAT( \
    YEAR        varchar(4)  not null, \
    MOCKCD      varchar(9)  not null, \
    GRADE       varchar(2)  not null, \
    FIELD_CNT   smallint, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_HOPE_FIELD_DAT add constraint PK_HOPE_FIELD primary key (YEAR, MOCKCD, GRADE)
