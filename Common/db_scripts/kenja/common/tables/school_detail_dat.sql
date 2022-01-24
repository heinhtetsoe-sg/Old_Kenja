-- kanji=漢字
-- $Id: 00f95b11fae19bcf6183a3279bce300aae560ab4 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SCHOOL_DETAIL_DAT

create table SCHOOL_DETAIL_DAT \
(  \
    YEAR            varchar(4)  not null, \
    SCHOOLCD        varchar(12) not null, \
    SCHOOL_KIND     varchar(2)  not null, \
    SCHOOL_SEQ      varchar(3)  not null, \
    SCHOOL_REMARK1  varchar(90), \
    SCHOOL_REMARK2  varchar(90), \
    SCHOOL_REMARK3  varchar(90), \
    SCHOOL_REMARK4  varchar(90), \
    SCHOOL_REMARK5  varchar(90), \
    SCHOOL_REMARK6  varchar(90), \
    SCHOOL_REMARK7  varchar(90), \
    SCHOOL_REMARK8  varchar(90), \
    SCHOOL_REMARK9  varchar(90), \
    SCHOOL_REMARK10 varchar(90), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHOOL_DETAIL_DAT add constraint PK_SCHOOL_DTL_DAT \
primary key (YEAR, SCHOOLCD, SCHOOL_KIND, SCHOOL_SEQ)


