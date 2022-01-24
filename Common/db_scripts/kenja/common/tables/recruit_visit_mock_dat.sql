-- $Id: 81f9da65693cdf80ecbb200284d6c368c640c549 $

drop table RECRUIT_VISIT_MOCK_DAT

create table RECRUIT_VISIT_MOCK_DAT( \
    YEAR                varchar(4) not null, \
    RECRUIT_NO          varchar(8) not null, \
    MONTH               varchar(2) not null, \
    SUBCLASSCD01        DECIMAL(4,1), \
    SUBCLASSCD02        DECIMAL(4,1), \
    SUBCLASSCD03        DECIMAL(4,1), \
    SUBCLASSCD04        DECIMAL(4,1), \
    SUBCLASSCD05        DECIMAL(4,1), \
    AVG3                DECIMAL(4,1), \
    AVG5                DECIMAL(4,1), \
    COMPANYCD           varchar(8), \
    COMPANY_TEXT        varchar(60), \
    TOP1_AVG3           DECIMAL(4,1), \
    TOP1_AVG5           DECIMAL(4,1), \
    TOP1_COMPANYCD      varchar(8), \
    TOP1_COMPANY_TEXT   varchar(60), \
    TOP2_AVG3           DECIMAL(4,1), \
    TOP2_AVG5           DECIMAL(4,1), \
    TOP2_COMPANYCD      varchar(8), \
    TOP2_COMPANY_TEXT   varchar(60), \
    TOP_AVG             DECIMAL(4,1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_VISIT_MOCK_DAT add constraint PK_RECRUIT_VIS_MOC primary key (YEAR, RECRUIT_NO, MONTH)
