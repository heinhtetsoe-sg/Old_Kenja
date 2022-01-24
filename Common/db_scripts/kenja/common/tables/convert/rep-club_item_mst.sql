-- kanji=漢字
-- $Id: 7ec03943be13ab6478877e4dc874aa4da6b855ed $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CLUB_ITEM_MST_OLD
create table CLUB_ITEM_MST_OLD like CLUB_ITEM_MST
insert into CLUB_ITEM_MST_OLD select * from CLUB_ITEM_MST

drop table CLUB_ITEM_MST

create table CLUB_ITEM_MST \
( \
    SCHOOLCD       VARCHAR (12) not null, \
    SCHOOL_KIND    VARCHAR (2) not null, \
    ITEMCD         VARCHAR (3) not null, \
    ITEMNAME       VARCHAR (60), \
    REGISTERCD     VARCHAR (10), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLUB_ITEM_MST add constraint PK_CLUB_ITEM_MST \
primary key (SCHOOLCD, SCHOOL_KIND, ITEMCD)

insert into CLUB_ITEM_MST \
select \
        '000000000000' AS SCHOOLCD, \
        'H' AS SCHOOL_KIND, \
        ITEMCD, \
        ITEMNAME, \
        REGISTERCD, \
        UPDATED \
from CLUB_ITEM_MST_OLD
