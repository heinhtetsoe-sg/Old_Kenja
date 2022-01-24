-- $Id: 7b7b928b0c937bd8b9d07df022ac04a78659e9a2 $
drop table CHAIR_LC_DAT

create table CHAIR_LC_DAT \
      (YEAR             varchar(4) not null, \
       SEMESTER         varchar(1) not null, \
       CHAIRCD          varchar(7) not null, \
       GROUPCD          varchar(4) not null, \
       LCGRADE          varchar(2) not null, \
       LCCLASS          varchar(3) not null, \
       REGISTERCD       varchar(10), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CHAIR_LC_DAT add constraint PK_CHAIR_LC_DAT primary key \
      (YEAR, SEMESTER, CHAIRCD, GROUPCD, LCGRADE, LCCLASS)
