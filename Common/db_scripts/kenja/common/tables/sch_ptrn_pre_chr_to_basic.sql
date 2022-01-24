-- $Id: a4f8ec8c50090231e96350c4845d4e5811eef360 $

drop   table SCH_PTRN_PRE_CHR_TO_BASIC_DAT

create table SCH_PTRN_PRE_CHR_TO_BASIC_DAT \
        (YEAR          varchar(4) not null, \
         SEMESTER      varchar(1) not null, \
         BSCSEQ        SMALLINT not null, \
         DAYCD         varchar(1) not null, \
         PERIODCD      varchar(1) not null, \
         PRESEQ        SMALLINT not null, \
         PRE_ORDER     SMALLINT, \
         REGISTERCD    varchar(10), \
         UPDATED       timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table SCH_PTRN_PRE_CHR_TO_BASIC_DAT add constraint PK_SCH_PTRN_PRE_CHR_TO_BASIC_DAT primary key (YEAR, SEMESTER, BSCSEQ, DAYCD, PERIODCD)

