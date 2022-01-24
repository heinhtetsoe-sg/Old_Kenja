-- kanji=漢字
-- $Id: 528833f06f0c9b3788e465a1af22bb84a66678da $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table MOCK_GROUP_DAT

create table MOCK_GROUP_DAT \
    (YEAR           varchar(4) not null, \
     GROUP_DIV      varchar(1) not null, \
     TARGET_DIV     varchar(1) not null, \
     STF_AUTH_CD    varchar(8) not null, \
     GROUPCD        varchar(4) not null, \
     MOCK_TARGET_CD varchar(9) not null, \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_GROUP_DAT add constraint pk_mock_group_dat primary key (YEAR, GROUP_DIV, TARGET_DIV, STF_AUTH_CD, GROUPCD, MOCK_TARGET_CD)


