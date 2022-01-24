-- $Id: 07451a5167beab385016ace0ee53b803715a4e2e $

drop table SCH_PTRN_PRE_HDAT

create table SCH_PTRN_PRE_HDAT \
        (YEAR          varchar(4) not null, \
         SEMESTER      varchar(1) not null, \
         PRESEQ        smallint   not null, \
         TITLE         varchar(45), \
         REGISTERCD    varchar(10), \
         UPDATED       timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table SCH_PTRN_PRE_HDAT add constraint PK_SCH_PTRN_PREH primary key (YEAR,SEMESTER,PRESEQ)
