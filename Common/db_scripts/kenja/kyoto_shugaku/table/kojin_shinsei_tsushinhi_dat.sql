-- $Id: kojin_shinsei_tsushinhi_dat.sql 76138 2020-08-21 08:38:16Z yamashiro $

drop table KOJIN_SHINSEI_TSUSHINHI_DAT
create table KOJIN_SHINSEI_TSUSHINHI_DAT( \
    KOJIN_NO                 varchar(7)    not null, \
    SHINSEI_YEAR             varchar(4)    not null, \
    SEQ                      smallint      not null, \
    SHINSEI_DATE             date          not null, \
    UKE_YEAR                 varchar(4), \
    UKE_NO                   varchar(4), \
    UKE_EDABAN               varchar(3), \
    SHORI_JYOUKYOU           varchar(2), \
    KANRYOU_FLG              varchar(1), \
    TSUSHINHI_YOTEI_GK       int, \
    KETTEI_CANCEL_UKE_YEAR   varchar(4), \
    KETTEI_CANCEL_UKE_NO     varchar(4), \
    KETTEI_CANCEL_UKE_EDABAN varchar(3), \
    KETTEI_CANCEL_DATE       date, \
    CANCEL_FLG               varchar(1), \
    KETTEI_DATE              date, \
    KETTEI_FLG               varchar(1), \
    REGISTERCD               varchar(8), \
    UPDATED                  timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table KOJIN_SHINSEI_TSUSHINHI_DAT add constraint PK_K_SHIN_T_DAT primary key (KOJIN_NO, SHINSEI_YEAR, SEQ)

