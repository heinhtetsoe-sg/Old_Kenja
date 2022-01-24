-- $Id: ec8879e951982a005f629a89106ad65747aac7d3 $

drop table SCH_PTRN_PRE_CHR_DAT

create table SCH_PTRN_PRE_CHR_DAT \
        (YEAR          varchar(4) not null, \
         SEMESTER      varchar(1) not null, \
         PRESEQ        smallint   not null, \
         COURSECD      varchar(1) not null, \
         MAJORCD       varchar(3) not null, \
         COURSECODE    varchar(4) not null, \
         GRADE         varchar(2) not null, \
         HR_CLASS      varchar(3) not null, \
         PRE_ORDER     smallint   not null, \
         CHAIRCD       varchar(7) not null, \
         REGISTERCD    varchar(10), \
         UPDATED       timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table SCH_PTRN_PRE_CHR_DAT add constraint PK_SCHPTRN_PCHRD primary key (YEAR,SEMESTER,PRESEQ,COURSECD,MAJORCD,COURSECODE,GRADE,HR_CLASS,PRE_ORDER,CHAIRCD)
