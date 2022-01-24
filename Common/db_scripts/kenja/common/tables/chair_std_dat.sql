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
