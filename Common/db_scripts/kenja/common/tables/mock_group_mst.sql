-- kanji=漢字
-- $Id: 76e97e6c919609b3436e72f563e4243ff2ab0640 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table MOCK_GROUP_MST

create table MOCK_GROUP_MST \
    (GROUP_DIV     varchar(1) not null, \
     STF_AUTH_CD   varchar(8) not null, \
     GROUPCD       varchar(4) not null, \
     GROUPNAME1    varchar(60), \
     GROUPNAME2    varchar(60), \
     GROUPNAME3    varchar(60), \
     REGISTERCD    varchar(8), \
     UPDATED       timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_GROUP_MST add constraint pk_mock_group_mst primary key (GROUP_DIV, STF_AUTH_CD, GROUPCD)


