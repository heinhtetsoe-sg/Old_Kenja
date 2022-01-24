-- kanji=漢字
-- $Id: 51eb3e20332cacf343dc0a83e9303f4c76a85d24 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table NYUGAKU_LIST_ANYTIME_DAT_OLD
create table NYUGAKU_LIST_ANYTIME_DAT_OLD like NYUGAKU_LIST_ANYTIME_DAT
insert into NYUGAKU_LIST_ANYTIME_DAT_OLD select * from NYUGAKU_LIST_ANYTIME_DAT

drop table NYUGAKU_LIST_ANYTIME_DAT
create table NYUGAKU_LIST_ANYTIME_DAT( \
    ENTERYEAR               varchar(4)    not null, \
    SCHREGNO                varchar(8)    not null, \
    SEMESTER                varchar(1), \
    ENT_DATE                date, \
    ENT_DIV                 varchar(1), \
    GRADE                   varchar(2), \
    HR_CLASS                varchar(3), \
    ATTENDNO                varchar(3), \
    INOUTCD                 varchar(1), \
    COURSECD                varchar(1), \
    MAJORCD                 varchar(3), \
    COURSECODE              varchar(4), \
    NAME                    varchar(120), \
    NAME_KANA               varchar(240), \
    BIRTHDAY                date, \
    SEX                     varchar(1), \
    FS_CD                   varchar(7), \
    FINSCHOOLGRADDATE       date, \
    PRISCHOOLCD             varchar(7), \
    DORMITORY_FLG           varchar(1), \
    ZIPCD                   varchar(8), \
    ADDRESS1                varchar(150), \
    ADDRESS2                varchar(150), \
    TELNO                   varchar(14), \
    FAXNO                   varchar(14), \
    EMAIL                   varchar(20), \
    EMERGENCYCALL           varchar(60), \
    EMERGENCYTELNO          varchar(14), \
    SCALASHIPDIV            varchar(2), \
    KATEI_ENTEXAMYEAR       varchar(4), \
    TIKUCD                  varchar(2), \
    EXAMNO                  varchar(10), \
    TESTDIV                 varchar(2), \
    EXAM_TYPE               varchar(2), \
    RELATIONSHIP            varchar(2), \
    GNAME                   varchar(120), \
    GKANA                   varchar(240), \
    GUARD_SEX               varchar(1), \
    GUARD_BIRTHDAY          date, \
    GZIPCD                  varchar(8), \
    GADDRESS1               varchar(150), \
    GADDRESS2               varchar(150), \
    GTELNO                  varchar(14), \
    GFAXNO                  varchar(14), \
    GEMAIL                  varchar(50), \
    GUARD_JOBCD             varchar(2), \
    GUARD_WORK_NAME         varchar(120), \
    GUARD_WORK_TELNO        varchar(14), \
    GUARANTOR_RELATIONSHIP  varchar(2), \
    GUARANTOR_NAME          varchar(120), \
    GUARANTOR_KANA          varchar(240), \
    GUARANTOR_SEX           varchar(1), \
    GUARANTOR_ZIPCD         varchar(8), \
    GUARANTOR_ADDR1         varchar(150), \
    GUARANTOR_ADDR2         varchar(150), \
    GUARANTOR_TELNO         varchar(14), \
    GUARANTOR_JOBCD         varchar(2), \
    PUBLIC_OFFICE           varchar(30), \
    REGISTERCD              varchar(10),  \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table NYUGAKU_LIST_ANYTIME_DAT add constraint PK_NYU_LIST_ANY_D primary key (ENTERYEAR, SCHREGNO)

insert into NYUGAKU_LIST_ANYTIME_DAT \
select \
        ENTERYEAR, \
        SCHREGNO, \
        SEMESTER, \
        ENT_DATE, \
        ENT_DIV, \
        GRADE, \
        HR_CLASS, \
        ATTENDNO, \
        INOUTCD, \
        COURSECD, \
        MAJORCD, \
        COURSECODE, \
        NAME, \
        NAME_KANA, \
        BIRTHDAY, \
        SEX, \
        FS_CD, \
        FINSCHOOLGRADDATE, \
        PRISCHOOLCD, \
        DORMITORY_FLG, \
        ZIPCD, \
        ADDRESS1, \
        ADDRESS2, \
        TELNO, \
        FAXNO, \
        EMAIL, \
        EMERGENCYCALL, \
        EMERGENCYTELNO, \
        SCALASHIPDIV, \
        KATEI_ENTEXAMYEAR, \
        TIKUCD, \
        EXAMNO, \
        cast(null as varchar(2)) as TESTDIV, \
        cast(null as varchar(2)) as EXAM_TYPE, \
        RELATIONSHIP, \
        GNAME, \
        GKANA, \
        GUARD_SEX, \
        GUARD_BIRTHDAY, \
        GZIPCD, \
        GADDRESS1, \
        GADDRESS2, \
        GTELNO, \
        GFAXNO, \
        GEMAIL, \
        GUARD_JOBCD, \
        GUARD_WORK_NAME, \
        GUARD_WORK_TELNO, \
        GUARANTOR_RELATIONSHIP, \
        GUARANTOR_NAME, \
        GUARANTOR_KANA, \
        GUARANTOR_SEX, \
        GUARANTOR_ZIPCD, \
        GUARANTOR_ADDR1, \
        GUARANTOR_ADDR2, \
        GUARANTOR_TELNO, \
        GUARANTOR_JOBCD, \
        PUBLIC_OFFICE, \
        REGISTERCD, \
        UPDATED \
from NYUGAKU_LIST_ANYTIME_DAT_OLD
