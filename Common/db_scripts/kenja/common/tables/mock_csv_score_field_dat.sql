-- $Id: 39d8bcae0774886fc04ebabdf042d6cf17c5dc11 $

drop table MOCK_CSV_SCORE_FIELD_DAT
create table MOCK_CSV_SCORE_FIELD_DAT( \
    YEAR            varchar(4)  not null, \
    MOCKCD          varchar(9)  not null, \
    GRADE           varchar(2)  not null, \
    FIELD_CNT       smallint, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_SCORE_FIELD_DAT add constraint PK_SCORE_FIELD primary key (YEAR, MOCKCD, GRADE)
