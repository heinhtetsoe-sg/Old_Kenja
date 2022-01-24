-- $Id: text_order_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table TEXT_ORDER_MST
create table TEXT_ORDER_MST ( \
     ORDER_CD    varchar(4) not null, \
     ORDER_NAME  varchar(60), \
     ORDER_ABBV  varchar(30), \
     ORDER_ZIPCD varchar(8), \
     ORDER_PREF_CD varchar(8), \
     ORDER_ADDR1 varchar(75), \
     ORDER_ADDR2 varchar(75), \
     ORDER_ADDR3 varchar(75), \
     ORDER_TELNO varchar(14), \
     REGISTERCD  varchar(8), \
     UPDATED     timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table TEXT_ORDER_MST add constraint pk_text_order_mst primary key(ORDER_CD)
