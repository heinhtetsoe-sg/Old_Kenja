-- $Id: a3958cfee979dd71a78a434766b6113a2abc8225 $

drop table MOCK_CSV_BENE_HOPE_HEAD_DAT
create table MOCK_CSV_BENE_HOPE_HEAD_DAT( \
    YEAR            varchar(4)  not null, \
    KYOUZAICD       varchar(2)  not null, \
    SEQ             varchar(3)  not null, \
    TYEAR           varchar(120), \
    TKYOUZAI        varchar(120), \
    TSCHOOL_CD      varchar(120), \
    TBOSYUTANNI     varchar(120), \
    TSCHOOL_NAME    varchar(120), \
    TGAKUBU_NAME    varchar(120), \
    TGAKKA_NAME     varchar(120), \
    TNITTEI         varchar(120), \
    THOUSIKI        varchar(120), \
    TALL_JUDGE      varchar(120), \
    TJUDGE1         varchar(120), \
    TJUDGE2         varchar(120), \
    TALL_JUDGE_SS   varchar(120), \
    TJUDGE1_SS      varchar(120), \
    TJUDGE2_SS      varchar(120), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_BENE_HOPE_HEAD_DAT add constraint PK_BENE_HOPE_HD primary key (YEAR, KYOUZAICD, SEQ)
