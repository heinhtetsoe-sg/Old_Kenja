-- kanji=漢字
-- $Id: bank_result_tmp_data_replace.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table BANK_RESULT_TMP_DATA_REPLACE

create table BANK_RESULT_TMP_DATA_REPLACE \
( \
    "PAID_MONEY_DATE"       date  not null, \
    "REPLACE_MONEY_DATE"    date  not null, \
    "REGISTERCD"            varchar(8), \
    "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table BANK_RESULT_TMP_DATA_REPLACE \
add constraint PK_DATA_REPLACE \
primary key \
(PAID_MONEY_DATE)

comment on table BANK_RESULT_TMP_DATA_REPLACE IS '入金日変換テーブル 2009/03/28'
