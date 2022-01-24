-- $Id: text_order_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table TEXT_ORDER_HIST_DAT
create table TEXT_ORDER_HIST_DAT ( \
     ORDER_SEQ   int not null, \
     ORDER_DATE  date not null, \
     REGISTERCD  varchar(8), \
     UPDATED     timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table TEXT_ORDER_HIST_DAT add constraint pk_order_hist_dat primary key(ORDER_SEQ)
