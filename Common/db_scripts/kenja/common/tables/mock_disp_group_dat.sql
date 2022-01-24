-- kanji=漢字
-- $Id: 92a4647d79a2acecb3c7044efe81d30b99447641 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table MOCK_DISP_GROUP_DAT

create table MOCK_DISP_GROUP_DAT \
    (YEAR           varchar(4) not null, \
     GROUPCD        varchar(4) not null, \
     MOCKCD         varchar(9) not null, \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_DISP_GROUP_DAT add constraint PK_MOCK_DISP_GROUP_DAT primary key (YEAR, GROUPCD, MOCKCD)


