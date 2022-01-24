-- $Id: tsushinhi_keikaku_dat.sql 76138 2020-08-21 08:38:16Z yamashiro $

drop table TSUSHINHI_KEIKAKU_DAT
create table TSUSHINHI_KEIKAKU_DAT( \
    SHUUGAKU_NO              varchar(7)    not null, \
    SHINSEI_YEAR             varchar(4)    not null, \
    SEQ                      smallint      not null, \
    YEAR                     varchar(4)    not null, \
    MONTH                    varchar(2)    not null, \
    KOJIN_NO                 varchar(7)    not null, \
    SHIKIN_SHUBETSU          varchar(1)    not null, \
    FURIKOMI_YOTEI_DATE      date, \
    FURIKOMI_DATE            date, \
    SHIHARAI_PLAN_GK         int not null, \
    SHISHUTSU_YOTEI_GK       int, \
    SHISHUTSU_GK             int, \
    HENNOU_YOTEI_GK          int, \
    KARI_TEISHI_FLG          varchar(1), \
    TEISHI_FLG               varchar(1), \
    REGISTERCD               varchar(8), \
    UPDATED                  timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table TSUSHINHI_KEIKAKU_DAT add constraint PK_TSUSHINHI_KE_DAT primary key (SHUUGAKU_NO, SHINSEI_YEAR, SEQ, YEAR, MONTH)
