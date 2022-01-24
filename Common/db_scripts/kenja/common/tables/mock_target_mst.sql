-- kanji=漢字
-- $Id: 5649673372b6b522671dffa75c51497fac8a376e $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table MOCK_TARGET_MST

create table MOCK_TARGET_MST \
    (TARGET_DIV    varchar(1) not null, \
     STF_AUTH_CD   varchar(8) not null, \
     TARGETCD      varchar(9) not null, \
     TARGETNAME1   varchar(60), \
     TARGETNAME2   varchar(60), \
     TARGETNAME3   varchar(60), \
     REGISTERCD    varchar(8), \
     UPDATED       timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_TARGET_MST add constraint pk_mock_target_mst primary key (TARGET_DIV, STF_AUTH_CD, TARGETCD)


