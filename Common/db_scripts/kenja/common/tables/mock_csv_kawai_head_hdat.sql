-- $Id: 11070a454ad381dc0fa8742f93f0241359039b44 $

drop table MOCK_CSV_KAWAI_HEAD_HDAT
create table MOCK_CSV_KAWAI_HEAD_HDAT( \
    YEAR            varchar(4)  not null, \
    MOSI_CD         varchar(5)  not null, \
    MOCKCD          varchar(9)  not null, \
    TYOUSHI_NO      varchar(120), \
    TYEAR           varchar(120), \
    TMOSI_CD        varchar(120), \
    TSCHOOL_CD      varchar(120), \
    TGRADE          varchar(30), \
    THR_CLASS       varchar(30), \
    TATTENDNO       varchar(30), \
    TKANA           varchar(30), \
    TEXAM_TYPE      varchar(120), \
    TBUN_RI_CD      varchar(120), \
    TREMARK_A       varchar(120), \
    TREMARK_B       varchar(120), \
    TREMARK_C       varchar(120), \
    TREMARK_D       varchar(120), \
    TREMARK_E       varchar(120), \
    TREMARK_F       varchar(120), \
    TREMARK_G       varchar(120), \
    TREMARK_H       varchar(120), \
    TREMARK_I       varchar(120), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_KAWAI_HEAD_HDAT add constraint PK_KAWAI_HH primary key (YEAR, MOSI_CD)
