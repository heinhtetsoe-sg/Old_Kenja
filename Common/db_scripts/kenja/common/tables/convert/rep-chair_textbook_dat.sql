-- $Id: 5b49b81088232657864cd55a773c92331fae0ebe $

drop table CHAIR_TEXTBOOK_DAT_OLD
create table CHAIR_TEXTBOOK_DAT_OLD like CHAIR_TEXTBOOK_DAT
insert into CHAIR_TEXTBOOK_DAT_OLD select * from CHAIR_TEXTBOOK_DAT

drop table CHAIR_TEXTBOOK_DAT

create table CHAIR_TEXTBOOK_DAT \
      (YEAR          varchar(4)      not null, \
       SEMESTER      varchar(1)      not null, \
       CHAIRCD       varchar(7)      not null, \
       TEXTBOOKCD    varchar(12)     not null, \
       REGISTERCD    varchar(8), \
       UPDATED       timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CHAIR_TEXTBOOK_DAT add constraint pk_chair_book_dat primary key \
      (YEAR,SEMESTER,CHAIRCD,TEXTBOOKCD)

insert into CHAIR_TEXTBOOK_DAT \
select \
    YEAR, \
    SEMESTER, \
    CHAIRCD, \
    right(rtrim('000000000000'||TEXTBOOKCD),12) as TEXTBOOKCD, \
    REGISTERCD, \
    UPDATED \
from CHAIR_TEXTBOOK_DAT_OLD
