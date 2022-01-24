-- $Id: 9192a85293064bc1e50eb2a464575183029ba252 $

drop   table TMP_SCH_PTRN_DAT
create table TMP_SCH_PTRN_DAT \
        (YEAR          varchar(4) not null, \
         SEMESTER      varchar(1) not null, \
         BSCSEQ        SMALLINT not null, \
         DAYCD         varchar(1) not null, \
         PERIODCD      varchar(1) not null, \
         CHAIRCD       varchar(7) not null, \
         DECISIONDIV   varchar(1), \
         REGISTERCD    varchar(8), \
         UPDATED       timestamp default current timestamp \
        ) in usr1dms index in idx1dms

insert into TMP_SCH_PTRN_DAT \
  select \
         YEAR       , \
         SEMESTER   , \
         BSCSEQ     , \
         DAYCD      , \
         PERIODCD   , \
         CHAIRCD    , \
         DECISIONDIV, \
         REGISTERCD , \
         UPDATED    \
  from SCH_PTRN_DAT

drop table SCH_PTRN_DAT_OLD

rename table     SCH_PTRN_DAT to SCH_PTRN_DAT_OLD

rename table TMP_SCH_PTRN_DAT to SCH_PTRN_DAT

alter table SCH_PTRN_DAT add constraint PK_SCH_PTRN_DAT primary key (YEAR,SEMESTER,BSCSEQ,DAYCD,PERIODCD,CHAIRCD)

