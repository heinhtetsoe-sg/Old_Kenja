-- $Id: 56bf63455f48a2178d70085e3c98b906903a5f08 $

drop table RECRUIT_VISIT_MOCK_DAT_OLD

RENAME TABLE RECRUIT_VISIT_MOCK_DAT TO RECRUIT_VISIT_MOCK_DAT_OLD

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

INSERT INTO RECRUIT_VISIT_MOCK_DAT \
    SELECT \
        YEAR, \
        RECRUIT_NO, \
        MONTH, \
        SUBCLASSCD01, \
        SUBCLASSCD02, \
        SUBCLASSCD03, \
        SUBCLASSCD04, \
        SUBCLASSCD05, \
        AVG3, \
        AVG5, \
        COMPANYCD, \
        CAST(NULL AS VARCHAR(60)) AS COMPANY_TEXT, \
        TOP1_AVG3, \
        TOP1_AVG5, \
        TOP1_COMPANYCD, \
        CAST(NULL AS VARCHAR(60)) AS TOP1_COMPANY_TEXT, \
        TOP2_AVG3, \
        TOP2_AVG5, \
        TOP2_COMPANYCD, \
        CAST(NULL AS VARCHAR(60)) AS TOP2_COMPANY_TEXT, \
        TOP_AVG, \
        REGISTERCD, \
        UPDATED \
    FROM \
        RECRUIT_VISIT_MOCK_DAT_OLD

alter table RECRUIT_VISIT_MOCK_DAT add constraint PK_RECRUIT_VIS_MOC primary key (YEAR, RECRUIT_NO, MONTH)
