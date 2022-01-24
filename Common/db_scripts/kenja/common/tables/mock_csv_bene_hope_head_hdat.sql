-- $Id: 64cc9949b03fc0f22c2f3e7fd7abe0ff5670ab1f $

drop table MOCK_CSV_BENE_HOPE_HEAD_HDAT
create table MOCK_CSV_BENE_HOPE_HEAD_HDAT( \
    YEAR            varchar(4)  not null, \
    KYOUZAICD       varchar(2)  not null, \
    MOCKCD          varchar(9)  not null, \
    TYEAR           varchar(120), \
    TKYOUZAI        varchar(120), \
    TTYPE            varchar(120), \
    TKYOUZAINAME    varchar(120), \
    TGAKKACD        varchar(120), \
    TGAKKANAME      varchar(120), \
    TBENEID         varchar(120), \
    THR_CLASS       varchar(120), \
    TATTENDNO       varchar(120), \
    TNAME           varchar(120), \
    TBUNRI_DIV      varchar(120), \
    TBIRTHDAY       varchar(120), \
    TSEX            varchar(120), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_BENE_HOPE_HEAD_HDAT add constraint PK_BENE_HOPE_HH primary key (YEAR, KYOUZAICD)
