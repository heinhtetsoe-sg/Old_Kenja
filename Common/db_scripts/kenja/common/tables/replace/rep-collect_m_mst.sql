-- $Id: 13595320131cb862459d9b2e9bebe6abf4456707 $

drop table COLLECT_M_MST_OLD

create table COLLECT_M_MST_OLD like COLLECT_M_MST

insert into COLLECT_M_MST_OLD select * from COLLECT_M_MST

drop table COLLECT_M_MST \

create table COLLECT_M_MST \
( \
        SCHOOLCD            varchar(12) not null, \
        SCHOOL_KIND         varchar(2)  not null, \
        YEAR                varchar(4)  not null, \
        COLLECT_L_CD        varchar(2)  not null, \
        COLLECT_M_CD        varchar(2)  not null, \
        COLLECT_M_NAME      varchar(90), \
        COLLECT_S_EXIST_FLG varchar(1), \
        COLLECT_M_MONEY     integer, \
        KOUHI_SHIHI         varchar(1), \
        IS_JUGYOURYOU       varchar(1), \
        REDUCTION_DIV       varchar(1), \
        IS_REDUCTION_SCHOOL varchar(1), \
        IS_CREDITCNT        varchar(1), \
        IS_REFUND           varchar(1), \
        IS_REPAY            varchar(1), \
        CLASSCD             varchar(2), \
        TEXTBOOKDIV         varchar(1), \
        SHOW_ORDER          varchar(2), \
        LMS_GRP_CD          varchar(6), \
        REMARK              varchar(60), \
        DIVIDE_PROCESS      varchar(1) , \
        ROUND_DIGIT         varchar(1) , \
        REGISTERCD          varchar(10), \
        UPDATED             timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

alter table COLLECT_M_MST \
add constraint PK_COL_M_MST \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, COLLECT_L_CD, COLLECT_M_CD)

INSERT INTO COLLECT_M_MST \
SELECT \
    SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    COLLECT_L_CD, \
    COLLECT_M_CD, \
    COLLECT_M_NAME, \
    COLLECT_S_EXIST_FLG, \
    COLLECT_M_MONEY, \
    KOUHI_SHIHI, \
    IS_JUGYOURYOU, \
    REDUCTION_DIV, \
    IS_REDUCTION_SCHOOL, \
    IS_CREDITCNT, \
    IS_REFUND, \
    IS_REPAY, \
    CLASSCD, \
    cast(null AS varchar(1)) AS TEXTBOOKDIV, \
    SHOW_ORDER, \
    LMS_GRP_CD, \
    REMARK, \
    DIVIDE_PROCESS, \
    ROUND_DIGIT, \
    REGISTERCD, \
    UPDATED \
FROM COLLECT_M_MST_OLD
