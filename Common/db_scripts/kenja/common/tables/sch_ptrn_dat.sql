-- $Id: 6118639a54301eaa06d7028285300999a67a5d98 $

drop   table SCH_PTRN_DAT

create table SCH_PTRN_DAT \
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

alter table SCH_PTRN_DAT add constraint PK_SCH_PTRN_DAT primary key (YEAR,SEMESTER,BSCSEQ,DAYCD,PERIODCD,CHAIRCD)

