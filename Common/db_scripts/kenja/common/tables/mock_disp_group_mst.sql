-- kanji=漢字
-- $Id: a8a3cc1696d70a9495711a7f984871ed141b5ba4 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table MOCK_DISP_GROUP_MST

create table MOCK_DISP_GROUP_MST \
    (YEAR          varchar(4) not null, \
     GROUPCD       varchar(4) not null, \
     GROUPNAME     varchar(60), \
     GROUPABBV     varchar(30), \
     REGISTERCD    varchar(10), \
     UPDATED       timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_DISP_GROUP_MST add constraint PK_MOCK_DISP_GROUP_MST primary key (YEAR, GROUPCD)


