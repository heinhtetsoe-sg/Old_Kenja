-- kanji=漢字
-- $Id: adff3a3bcf93be73f1e98e4dd0a8b627ec394cf2 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table TYPE_GROUP_COURSE_MST

create table TYPE_GROUP_COURSE_MST \
    (YEAR               varchar(4)    not null, \
     TYPE_GROUP_CD      varchar(6)    not null, \
     GRADE              varchar(2)    not null, \
     TYPE_GROUP_NAME    varchar(60), \
     REGISTERCD         varchar(8), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table TYPE_GROUP_COURSE_MST add constraint PK_TYPE_GROUP_C_M primary key (YEAR, TYPE_GROUP_CD, GRADE)
