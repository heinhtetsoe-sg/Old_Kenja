-- $Id: entexam_commodity_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $
drop table entexam_commodity_mst

create table entexam_commodity_mst \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    ITEMCD              varchar(2)  not null, \
    ITEMNAME            varchar(60), \
    MONEY_BOY           int, \
    MONEY_GIRL          int, \
    REGISTERCD          varchar(8),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_commodity_mst add constraint \
pk_entexam_commod primary key (entexamyear, itemcd)
