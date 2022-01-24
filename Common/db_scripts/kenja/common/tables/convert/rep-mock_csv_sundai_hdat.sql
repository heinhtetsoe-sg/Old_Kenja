-- $Id: f323801d33be73283ff143c50714385d9c395d7f $

drop table MOCK_CSV_SUNDAI_HDAT_OLD
create table MOCK_CSV_SUNDAI_HDAT_OLD like MOCK_CSV_SUNDAI_HDAT
insert into  MOCK_CSV_SUNDAI_HDAT_OLD select * from MOCK_CSV_SUNDAI_HDAT

drop table MOCK_CSV_SUNDAI_HDAT
create table MOCK_CSV_SUNDAI_HDAT( \
    YEAR            varchar(4)  not null, \
    MOSI_CD         varchar(4)  not null, \
    MOCKCD          varchar(9)  not null, \
    MOSI_NAME       varchar(45), \
    KANA            varchar(120), \
    HR_CLASS        varchar(3), \
    ATTENDNO        varchar(3), \
    EXAMNO          varchar(6)  not null, \
    SCHREGNO        varchar(8), \
    SCHOOL_CD       varchar(6), \
    SCHOOL_EDA      varchar(2), \
    SCHOOL_NAME     varchar(30), \
    BUNRI_DIV       varchar(6), \
    SEX             varchar(3), \
    GRADE           varchar(2), \
    GEN_SOTU        varchar(6), \
    KAMOKU_EIGO     varchar(1), \
    KAMOKU_SUUGAKU  varchar(1), \
    KAMOKU_KOKUGO   varchar(1), \
    KAMOKU_RI1      varchar(1), \
    KAMOKU_RI2      varchar(1), \
    KAMOKU_RI3      varchar(1), \
    KAMOKU_REKIKOU1 varchar(1), \
    KAMOKU_REKIKOU2 varchar(1), \
    RIKA_FIRST      varchar(30), \
    REKIKOU_FIRST   varchar(30), \
    DEVIATION       decimal(5,1), \
    RANK            integer, \
    CNT             integer, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_SUNDAI_HDAT add constraint PK_SUNDAI_H primary key (YEAR, MOSI_CD, EXAMNO)

insert into MOCK_CSV_SUNDAI_HDAT \
    SELECT \
        YEAR, \
        MOSI_CD, \
        MOCKCD, \
        MOSI_NAME, \
        KANA, \
        HR_CLASS, \
        ATTENDNO, \
        EXAMNO, \
        SCHREGNO, \
        SCHOOL_CD, \
        SCHOOL_EDA, \
        SCHOOL_NAME, \
        BUNRI_DIV, \
        SEX, \
        GRADE, \
        GEN_SOTU, \
        KAMOKU_EIGO, \
        KAMOKU_SUUGAKU, \
        KAMOKU_KOKUGO, \
        KAMOKU_RI1, \
        KAMOKU_RI2, \
        CAST(NULL AS varchar(1)) AS KAMOKU_RI3, \
        KAMOKU_REKIKOU1, \
        KAMOKU_REKIKOU2, \
        CAST(NULL AS varchar(30)) AS RIKA_FIRST, \
        CAST(NULL AS varchar(30)) AS REKIKOU_FIRST, \
        DEVIATION, \
        RANK, \
        CNT, \
        REGISTERCD, \
        UPDATED \
    FROM \
        MOCK_CSV_SUNDAI_HDAT_OLD
