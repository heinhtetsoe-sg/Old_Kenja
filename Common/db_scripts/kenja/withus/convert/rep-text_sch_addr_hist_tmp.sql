-- $Id: rep-text_sch_addr_hist_tmp.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table TEXT_SCH_ADDR_HIST_TMP_OLD
create table TEXT_SCH_ADDR_HIST_TMP_OLD like TEXT_SCH_ADDR_HIST_TMP
insert into  TEXT_SCH_ADDR_HIST_TMP_OLD select * from TEXT_SCH_ADDR_HIST_TMP

drop   table TEXT_SCH_ADDR_HIST_TMP
create table TEXT_SCH_ADDR_HIST_TMP ( \
    YEAR                varchar(4) not null, \
    SCHREGNO            varchar(8) not null, \
    ZIPCD               varchar(8), \        
    PREF_CD             varchar(2), \
    ADDR1               varchar(75), \        
    ADDR2               varchar(75), \        
    ADDR3               varchar(75), \        
    TELNO               varchar(14), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table TEXT_SCH_ADDR_HIST_TMP add constraint PK_TEXT_SCH_HIST_T primary key(YEAR,SCHREGNO)

insert into TEXT_SCH_ADDR_HIST_TMP \
  select \
        YEAR, \
        SCHREGNO, \
        ZIPCD, \
        '', \
        ADDR1, \
        ADDR2, \
        ADDR3, \
        TELNO, \
        REGISTERCD, \
        UPDATED \
  from TEXT_SCH_ADDR_HIST_TMP_OLD
