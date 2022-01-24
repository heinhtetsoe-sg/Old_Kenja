-- kanji=漢字
-- $Id: 56418c10acbef6e2525bb4c985a8296a1610218c $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table MOCK_MST_OLD
create table MOCK_MST_OLD like MOCK_MST
insert into MOCK_MST_OLD select * from MOCK_MST

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

insert into MOCK_MST \
select \
    MOCKCD, \
    MOCKNAME1, \
    MOCKNAME2, \
    MOCKNAME3, \
    '00000000' AS COMPANYCD, \
    '00000000' AS COMPANYMOSI_CD, \
    CAST(NULL AS VARCHAR(60)) AS TUUCHIHYOU_MOSI_NAME, \
    CAST(NULL AS VARCHAR(60)) AS SINROSIDOU_MOSI_NAME, \
    CAST(NULL AS VARCHAR(2)) AS MOSI_DIV, \
    CAST(NULL AS date) AS MOSI_DATE, \
    CAST(NULL AS VARCHAR(150)) AS FILE_NAME, \
    REGISTERCD, \
    UPDATED \
from \
    MOCK_MST_OLD
