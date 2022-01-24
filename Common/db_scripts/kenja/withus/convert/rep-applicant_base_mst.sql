-- kanji=漢字
-- $Id: rep-applicant_base_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table APPLICANT_BASE_MST_BACK

create table APPLICANT_BASE_MST_BACK like APPLICANT_BASE_MST

insert into APPLICANT_BASE_MST_BACK select * from APPLICANT_BASE_MST

drop table APPLICANT_BASE_MST

create table APPLICANT_BASE_MST \
(  \
        YEAR                    varchar(4) not null, \
        APPLICANTNO             varchar(7) not null, \
        APPLICANT_DIV           varchar(1), \
        COURSE_DIV              varchar(1), \
        BELONGING_DIV           varchar(3), \
        APPLICATION_DATE        date, \
        APPLICATION_FORM        varchar(1), \
        PROCEDURE_DIV           varchar(1), \
        ENT_SCHEDULE_DATE       date, \
        ENT_ANNUAL              varchar(2), \
        CURRICULUM_YEAR         varchar(4), \
        CLAIM_SEND              varchar(1), \
        MANNER_PAYMENT          varchar(1), \
        CREDIT                  smallint, \
        REMARK1                 varchar(9), \
        REMARK2                 varchar(9), \
        GRD_SCHEDULE_DATE       date, \
        SCHREGNO                varchar(8), \
        STUDENT_DIV             varchar(2), \
        COURSECD                varchar(1), \
        MAJORCD                 varchar(3), \
        COURSECODE              varchar(4), \
        NAME                    varchar(60), \
        NAME_SHOW               varchar(30), \
        NAME_KANA               varchar(120), \
        NAME_KANA_SHOW          varchar(60), \
        SEX                     varchar(1), \
        BIRTHDAY                date, \
        NATIONALITY             varchar(69), \
        ADDRESSCD               varchar(2), \
        ZIPCD                   varchar(8), \
        PREF_CD                 varchar(2), \
        ADDR1                   varchar(75), \
        ADDR2                   varchar(75), \
        ADDR3                   varchar(75), \
        TELNO                   varchar(14), \
        TELNO_SEARCH            varchar(14), \
        LOCATIONCD              varchar(2), \
        NATPUBPRIDIV            varchar(1), \
        FS_CD                   varchar(11), \
        FS_GRDDATE              date, \
        FS_GRD_DIV              varchar(1), \
        GNAME                   varchar(60), \
        GKANA                   varchar(120), \
        GSEX                    varchar(1), \
        GRELATIONSHIP           varchar(2), \
        GZIPCD                  varchar(8), \
        GPREF_CD                varchar(2), \
        GADDR1                  varchar(75), \
        GADDR2                  varchar(75), \
        GADDR3                  varchar(75), \
        GTELNO                  varchar(14), \
        GTELNO_SEARCH           varchar(14), \
        GUARANTOR_NAME          varchar(60), \
        GUARANTOR_KANA          varchar(120), \
        GUARANTOR_SEX           varchar(1), \
        GUARANTOR_RELATIONSHIP  varchar(2), \
        GUARANTOR_ZIPCD         varchar(8), \
        GUARANTOR_PREF_CD       varchar(2), \
        GUARANTOR_ADDR1         varchar(75), \
        GUARANTOR_ADDR2         varchar(75), \
        GUARANTOR_ADDR3         varchar(75), \
        GUARANTOR_TELNO         varchar(14), \
        GUARANTOR_TELNO_SEARCH  varchar(14), \
        REMARK                  varchar(45), \
        REGISTERCD              varchar(8), \
        UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table APPLICANT_BASE_MST \
add constraint PK_APPLICANT_BASE \
primary key  \
(YEAR, APPLICANTNO)


insert into APPLICANT_BASE_MST \
select \
    YEAR, \
    APPLICANTNO, \
    APPLICANT_DIV, \
    COURSE_DIV, \
    BELONGING_DIV, \
    APPLICATION_DATE, \
    APPLICATION_FORM, \
    PROCEDURE_DIV, \
    ENT_SCHEDULE_DATE, \
    ENT_ANNUAL, \
    CURRICULUM_YEAR, \
    CLAIM_SEND, \
    MANNER_PAYMENT, \
    CREDIT, \
    REMARK1, \
    REMARK2, \
    GRD_SCHEDULE_DATE, \
    SCHREGNO, \
    STUDENT_DIV, \
    COURSECD, \
    MAJORCD, \
    COURSECODE, \
    NAME, \
    NAME_SHOW, \
    NAME_KANA, \
    NAME_KANA_SHOW, \
    SEX, \
    BIRTHDAY, \
    NATIONALITY, \
    ADDRESSCD, \
    ZIPCD, \
    PREF_CD, \
    ADDR1, \
    ADDR2, \
    ADDR3, \
    TELNO, \
    TELNO_SEARCH, \
    LOCATIONCD, \
    NATPUBPRIDIV, \
    FS_CD, \
    FS_GRDDATE, \
    FS_GRD_DIV, \
    GNAME, \
    GKANA, \
    GSEX, \
    GRELATIONSHIP, \
    GZIPCD, \
    GPREF_CD, \
    GADDR1, \
    GADDR2, \
    GADDR3, \
    GTELNO, \
    GTELNO_SEARCH, \
    GUARANTOR_NAME, \
    GUARANTOR_KANA, \
    GUARANTOR_SEX, \
    GUARANTOR_RELATIONSHIP, \
    GUARANTOR_ZIPCD, \
    GUARANTOR_PREF_CD, \
    GUARANTOR_ADDR1, \
    GUARANTOR_ADDR2, \
    GUARANTOR_ADDR3, \
    GUARANTOR_TELNO, \
    GUARANTOR_TELNO_SEARCH, \
    cast(null as varchar(45)) as REMARK, \
    REGISTERCD, \
    UPDATED \
FROM \
    APPLICANT_BASE_MST_BACK
