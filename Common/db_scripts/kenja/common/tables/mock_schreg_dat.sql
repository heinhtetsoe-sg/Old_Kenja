-- kanji=漢字
-- $Id: a78c2d61202ba7843fc11923d8d245a0d6861cb9 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table MOCK_SCHREG_DAT

create table MOCK_SCHREG_DAT \
    (YEAR               varchar(4) not null, \
     MOCKCD             varchar(9) not null, \
     SCHREGNO           varchar(8) not null, \
     TEXT_REMARK1       varchar(120), \
     TEXT_REMARK2       varchar(120), \
     TEXT_REMARK3       varchar(120), \
     TEXT_REMARK4       varchar(120), \
     TEXT_REMARK5       varchar(120), \
     SINT_REMARK1       smallint, \
     SINT_REMARK2       smallint, \
     SINT_REMARK3       smallint, \
     SINT_REMARK4       smallint, \
     SINT_REMARK5       smallint, \
     DECI_REMARK1       decimal (8,5), \
     DECI_REMARK2       decimal (8,5), \
     DECI_REMARK3       decimal (8,5), \
     DECI_REMARK4       decimal (8,5), \
     DECI_REMARK5       decimal (8,5), \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_SCHREG_DAT add constraint PK_MOCK_SCHREG primary key (YEAR, MOCKCD, SCHREGNO)


