-- $Id: kojin_tsushinhi_gk_kettei_hist_dat.sql 76138 2020-08-21 08:38:16Z yamashiro $

DROP TABLE KOJIN_TSUSHINHI_GK_KETTEI_HIST_DAT
create table KOJIN_TSUSHINHI_GK_KETTEI_HIST_DAT( \
    KOJIN_NO                 varchar(7)    not null, \
    SHINSEI_YEAR             varchar(4)    not null, \
    SEQ                      smallint      not null, \
    SHORI_DIV                varchar(1)    not null, \
    KETTEI_DATE              date          not null, \
    HENKOU_UKE_YEAR          varchar(4), \
    HENKOU_UKE_NO            varchar(4), \
    HENKOU_UKE_EDABAN        varchar(3), \
    TSUSHINHI_KETTEI_GK      int, \
    REGISTERCD               varchar(8), \
    UPDATED                  timestamp default current timestamp \ 
) in usr1dms index in idx1dms

alter table KOJIN_TSUSHINHI_GK_KETTEI_HIST_DAT add constraint PK_K_T_GK_K_H_DAT primary key (KOJIN_NO, SHINSEI_YEAR, SEQ, SHORI_DIV, KETTEI_DATE)

