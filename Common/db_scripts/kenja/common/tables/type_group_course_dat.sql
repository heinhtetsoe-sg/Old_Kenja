-- kanji=漢字
-- $Id: 6b5d12ff9c4228535e0a5653cd4e6b59e581370c $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table TYPE_GROUP_COURSE_DAT

create table TYPE_GROUP_COURSE_DAT \
    (YEAR               varchar(4)    not null, \
     TYPE_GROUP_CD      varchar(6)    not null, \
     GRADE              varchar(2)    not null, \
     COURSECD           varchar(1)    not null, \
     MAJORCD            varchar(3)    not null, \
     COURSECODE         varchar(4)    not null, \
     REGISTERCD         varchar(8), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table TYPE_GROUP_COURSE_DAT add constraint PK_TYPE_GROUP_C_D primary key (YEAR, TYPE_GROUP_CD, GRADE, COURSECD, MAJORCD, COURSECODE)
