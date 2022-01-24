-- $Id: 3af3b55455430b8ca6813c326da0956952c0ab99 $

drop table SCHREG_TEXTBOOK_DAT_OLD
create table SCHREG_TEXTBOOK_DAT_OLD like SCHREG_TEXTBOOK_DAT
insert into SCHREG_TEXTBOOK_DAT_OLD select * from SCHREG_TEXTBOOK_DAT

drop table SCHREG_TEXTBOOK_DAT

create table SCHREG_TEXTBOOK_DAT \
      (SCHREGNO      varchar(8)      not null, \
       YEAR          varchar(4)      not null, \
       SEMESTER      varchar(1)      not null, \
       CHAIRCD       varchar(7)      not null, \
       TEXTBOOKCD    varchar(12)     not null, \
       REGISTERCD    varchar(8), \
       UPDATED       timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_TEXTBOOK_DAT add constraint pk_sch_textbk_dat primary key \
      (SCHREGNO,YEAR,SEMESTER,CHAIRCD,TEXTBOOKCD)

insert into SCHREG_TEXTBOOK_DAT \
select \
    SCHREGNO, \
    YEAR, \
    SEMESTER, \
    CHAIRCD, \
    right(rtrim('000000000000'||TEXTBOOKCD),12) as TEXTBOOKCD, \
    REGISTERCD, \
    UPDATED \
from SCHREG_TEXTBOOK_DAT_OLD
