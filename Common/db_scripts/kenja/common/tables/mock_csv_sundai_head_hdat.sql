-- $Id: 5d40325b5d6ecdc930ae4592c89ba3f8123903f1 $

drop table MOCK_CSV_SUNDAI_HEAD_HDAT
create table MOCK_CSV_SUNDAI_HEAD_HDAT( \
    YEAR                varchar(4)  not null, \
    MOSI_CD             varchar(4)  not null, \
    MOCKCD              varchar(9)  not null, \
    TYEAR               varchar(120), \
    TMOSI_CD            varchar(120), \
    TMOSI_NAME          varchar(120), \
    TKANA               varchar(120), \
    THR_CLASS           varchar(120), \
    TATTENDNO           varchar(120), \
    TEXAMNO             varchar(120), \
    TSCHOOL_CD          varchar(120), \
    TSCHOOL_EDA         varchar(120), \
    TSCHOOL_NAME        varchar(120), \
    TBUNRI_DIV          varchar(120), \
    TSEX                varchar(120), \
    TGRADE              varchar(120), \
    TGEN_SOTU           varchar(120), \
    TKAMOKU_EIGO        varchar(120), \
    TKAMOKU_SUUGAKU     varchar(120), \
    TKAMOKU_KOKUGO      varchar(120), \
    TKAMOKU_RI1         varchar(120), \
    TKAMOKU_RI2         varchar(120), \
    TKAMOKU_RI3         varchar(120), \
    TKAMOKU_REKIKOU1    varchar(120), \
    TKAMOKU_REKIKOU2    varchar(120), \
    TRIKA_FIRST         varchar(120), \
    TREKIKOU_FIRST      varchar(120), \
    TDEVIATION          varchar(120), \
    TRANK               varchar(120), \
    TCNT                varchar(120), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_SUNDAI_HEAD_HDAT add constraint PK_SUNDAI_HH primary key (YEAR, MOSI_CD)
