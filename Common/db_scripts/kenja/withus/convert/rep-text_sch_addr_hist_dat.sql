-- $Id: rep-text_sch_addr_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table TEXT_SCH_ADDR_HIST_DAT_OLD
create table TEXT_SCH_ADDR_HIST_DAT_OLD like TEXT_SCH_ADDR_HIST_DAT
insert into  TEXT_SCH_ADDR_HIST_DAT_OLD select * from TEXT_SCH_ADDR_HIST_DAT

drop   table TEXT_SCH_ADDR_HIST_DAT
create table TEXT_SCH_ADDR_HIST_DAT ( \
    YEAR                varchar(4) not null, \
    SCHREGNO            varchar(8) not null, \
    ORDER_SEQ           int not null, \    
    ZIPCD               varchar(8), \        
    PREF_CD             varchar(2), \
    ADDR1               varchar(75), \        
    ADDR2               varchar(75), \        
    ADDR3               varchar(75), \        
    TELNO               varchar(14), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table TEXT_SCH_ADDR_HIST_DAT add constraint pk_text_sch_hist primary key(YEAR,SCHREGNO,ORDER_SEQ)

insert into TEXT_SCH_ADDR_HIST_DAT \
  select \
        YEAR, \
        SCHREGNO, \
        ORDER_SEQ, \
        ZIPCD, \
        '', \
        ADDR1, \
        ADDR2, \
        ADDR3, \
        TELNO, \
        REGISTERCD, \
        UPDATED \
  from TEXT_SCH_ADDR_HIST_DAT_OLD
