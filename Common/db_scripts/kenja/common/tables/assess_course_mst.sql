-- kanji=漢字
-- $Id: fc8fd32e859847aecad5c1d5bfe84ff650897392 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ASSESS_COURSE_MST

create table ASSESS_COURSE_MST \
(  \
    ASSESSCD        varchar (1) not null, \
    COURSECD        varchar (1) not null, \
    MAJORCD         varchar (3) not null, \
    COURSECODE      varchar (4) not null, \
    ASSESSLEVEL     smallint not null, \
    ASSESSMARK      varchar (6), \
    ASSESSLOW       decimal (4,1), \
    ASSESSHIGH      decimal (4,1), \
    REGISTERCD      varchar (8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESS_COURSE_MST add constraint PK_ASS_COURSE_MST \
primary key (ASSESSCD,COURSECD,MAJORCD,COURSECODE,ASSESSLEVEL)
