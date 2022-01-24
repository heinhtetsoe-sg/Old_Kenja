-- kanji=漢字
-- $Id: e16d068ae7af18e275bdc13ca5790aa721fd4e8e $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CLUB_ITEM_KIND_MST

create table CLUB_ITEM_KIND_MST \
( \
    SCHOOLCD       VARCHAR (12) not null, \
    SCHOOL_KIND    VARCHAR (2) not null, \
    ITEMCD         VARCHAR (3) not null, \
    KINDCD         VARCHAR (3) not null, \
    KINDNAME       VARCHAR (60), \
    REGISTERCD     VARCHAR (10), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLUB_ITEM_KIND_MST add constraint PK_CLB_ITM_KND_MST \
primary key (SCHOOLCD, SCHOOL_KIND, ITEMCD, KINDCD)
