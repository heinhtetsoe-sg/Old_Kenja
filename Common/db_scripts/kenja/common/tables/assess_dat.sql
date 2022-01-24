-- kanji=漢字
-- $Id: c4d4e7c05b82e4cee5afaa09876bbee821fe3188 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ASSESS_DAT

create table ASSESS_DAT \
(  \
    YEAR            varchar (4) not null, \
    GRADE           varchar (2) not null, \
    SEMESTER        varchar (1) not null, \
    ASSESSLEVEL     smallint not null, \
    ASSESSMARK      varchar (9), \
    ASSESSLOW       decimal (4,1), \
    ASSESSHIGH      decimal (4,1), \
    REGISTERCD      varchar (8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESS_DAT add constraint PK_ASSESS_DAT \
primary key (YEAR,GRADE,SEMESTER,ASSESSLEVEL)
