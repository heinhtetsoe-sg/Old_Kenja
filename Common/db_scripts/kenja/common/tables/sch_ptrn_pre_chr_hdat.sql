-- $Id: b4b871c5a2f97455669b0686cd26b62a26a3e68f $

drop table SCH_PTRN_PRE_CHR_HDAT

create table SCH_PTRN_PRE_CHR_HDAT \
        (YEAR          varchar(4) not null, \
         SEMESTER      varchar(1) not null, \
         PRESEQ        smallint   not null, \
         TITLE         varchar(45), \
         REGISTERCD    varchar(10), \
         UPDATED       timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table SCH_PTRN_PRE_CHR_HDAT add constraint PK_SCHPTRN_PCHRH primary key (YEAR,SEMESTER,PRESEQ)
