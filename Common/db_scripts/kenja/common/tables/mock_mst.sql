-- kanji=漢字
-- $Id: b45567010905e8f478bdab3bb70d90b593d5e533 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table MOCK_MST

create table MOCK_MST \
    (MOCKCD               varchar(9) not null, \
     MOCKNAME1            varchar(60), \
     MOCKNAME2            varchar(60), \
     MOCKNAME3            varchar(60), \
     COMPANYCD            varchar(8), \
     COMPANYMOSI_CD       varchar(8), \
     TUUCHIHYOU_MOSI_NAME varchar(60), \
     SINROSIDOU_MOSI_NAME varchar(60), \
     MOSI_DIV             varchar(2), \
     MOSI_DATE            date, \
     FILE_NAME            varchar(150), \
     REGISTERCD           varchar(10), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_MST add constraint pk_mock_mst primary key (MOCKCD)
