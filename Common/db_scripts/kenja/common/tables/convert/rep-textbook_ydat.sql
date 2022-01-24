-- $Id: b8c2488d2b78cd5c7edd2e5986441dfbe123c455 $

drop   table TMP_TEXTBOOK_YDAT
create table TMP_TEXTBOOK_YDAT \
        (YEAR            VARCHAR(4) not null, \
         TEXTBOOKCD      VARCHAR(12) not null, \
         REGISTERCD      varchar(8), \
         UPDATED         timestamp default current timestamp \
        ) in usr1dms index in idx1dms

insert into TMP_TEXTBOOK_YDAT \
  select \
     YEAR      , \
     RIGHT(RTRIM('000000000000'||TEXTBOOKCD),12) as TEXTBOOKCD, \
     REGISTERCD    , \
     UPDATED \
  from TEXTBOOK_YDAT

drop table TEXTBOOK_YDAT_OLD

rename table     TEXTBOOK_YDAT to TEXTBOOK_YDAT_OLD

rename table TMP_TEXTBOOK_YDAT to TEXTBOOK_YDAT

alter table TEXTBOOK_YDAT add constraint PK_TEXTBOOK_YDAT primary key (YEAR,TEXTBOOKCD)

