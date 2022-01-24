drop table UNIT_SCH_CHR_RANK_DAT

create table UNIT_SCH_CHR_RANK_DAT \
      (EXECUTEDATE        date         not null, \
       PERIODCD           varchar(1)   not null, \
       CHAIRCD            varchar(7)   not null, \
       YEAR               varchar(4), \
       SEMESTER           varchar(1), \
       RANK               smallint, \
       REGISTERCD         varchar(8), \
       UPDATED            timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table UNIT_SCH_CHR_RANK_DAT add constraint PK_UNIT_SCH_CR_DAT primary key \
      (EXECUTEDATE, PERIODCD, CHAIRCD)
