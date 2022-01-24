-- $Id: 462104ffa2fd941c4cbd7fa8a109423b8171cf4c $

drop table schreg_textbook_dat

create table schreg_textbook_dat \
      (schregno      varchar(8)      not null, \
       year          varchar(4)      not null, \
       semester      varchar(1)      not null, \
       chaircd       varchar(7)      not null, \
       textbookcd    varchar(12)     not null, \
       registercd    varchar(8), \
       updated       timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table schreg_textbook_dat add constraint pk_sch_textbk_dat primary key \
      (schregno,year,semester,chaircd,textbookcd)
