-- $Id: f4681aea0e2f3f703da1b71258bd1de28bd44b7e $

drop table CHAIR_STD_DAT_OLD
create table CHAIR_STD_DAT_OLD like CHAIR_STD_DAT
insert into CHAIR_STD_DAT_OLD select * from CHAIR_STD_DAT

drop table CHAIR_STD_DAT

create table CHAIR_STD_DAT \
      (YEAR          varchar(4)      not null, \
       SEMESTER      varchar(1)      not null, \
       CHAIRCD       varchar(7)      not null, \
       SCHREGNO      varchar(8)      not null, \
       APPDATE       date            not null, \
       APPENDDATE    date, \
       ROW           varchar(3), \
       COLUMN        varchar(3), \
       REGISTERCD    varchar(10), \
       UPDATED       timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CHAIR_STD_DAT add constraint PK_CHAIR_STD_DAT primary key \
      (YEAR, SEMESTER, CHAIRCD, SCHREGNO, APPDATE)

create index ix_chair_Std_dat on CHAIR_STD_DAT (YEAR, CHAIRCD, SCHREGNO)

insert into CHAIR_STD_DAT \
select \
        YEAR, \
        SEMESTER, \
        CHAIRCD, \
        SCHREGNO, \
        APPDATE, \
        APPENDDATE, \
        right(rtrim('000' || ROW),3) as ROW, \
        right(rtrim('000' || COLUMN),3) as COLUMN, \
        REGISTERCD, \
        UPDATED \
from CHAIR_STD_DAT_OLD
