-- $Id: 8eb0ff835c65657d346002b55877b3928f178914 $

drop   table SCH_PTRN_COUNTFLG_DAT

create table SCH_PTRN_COUNTFLG_DAT ( \
         YEAR          varchar(4) not null, \
         SEMESTER      varchar(1) not null, \
         BSCSEQ        smallint not null, \
         DAYCD         varchar(1) not null, \
         PERIODCD      varchar(1) not null, \
         CHAIRCD       varchar(7) not null, \
         GRADE         varchar(2) not null, \
         HR_CLASS      varchar(3) not null, \
         COUNTFLG      varchar(1), \
         REGISTERCD    varchar(8), \
         UPDATED       timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table SCH_PTRN_COUNTFLG_DAT add constraint PK_SCHPTRNCFLG_DAT primary key (YEAR,SEMESTER,BSCSEQ,DAYCD,PERIODCD,CHAIRCD,GRADE,HR_CLASS)

