-- kanji=漢字
-- $Id: rep-text_order_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table TEXT_ORDER_MST_OLD
create table TEXT_ORDER_MST_OLD like TEXT_ORDER_MST
insert into TEXT_ORDER_MST_OLD select * from TEXT_ORDER_MST

drop table TEXT_ORDER_MST
create table TEXT_ORDER_MST \
( \
     ORDER_CD    varchar(4) not null, \
     ORDER_NAME  varchar(60), \
     ORDER_ABBV  varchar(30), \
     ORDER_ZIPCD varchar(8), \
     ORDER_PREF_CD varchar(2), \
     ORDER_ADDR1 varchar(75), \
     ORDER_ADDR2 varchar(75), \
     ORDER_ADDR3 varchar(75), \
     ORDER_TELNO varchar(14), \
     REGISTERCD  varchar(8), \
     UPDATED     timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table TEXT_ORDER_MST add constraint pk_text_order_mst primary key(ORDER_CD)


insert into TEXT_ORDER_MST \
    select \
        ORDER_CD, \
        ORDER_NAME, \
        ORDER_ABBV, \
        ORDER_ZIPCD, \
        cast(null as varchar(2)), \
        ORDER_ADDR1, \
        ORDER_ADDR2, \
        ORDER_ADDR3, \
        ORDER_TELNO, \
        REGISTERCD, \
        UPDATED \
    from TEXT_ORDER_MST_OLD

