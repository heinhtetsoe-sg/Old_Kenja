-- $Id: 840db9ec4a4307684ca181c6311d353d1cdf8619 $

drop table chair_textbook_dat

create table chair_textbook_dat \
      (year          varchar(4)      not null, \
       semester      varchar(1)      not null, \
       chaircd       varchar(7)      not null, \
       textbookcd    varchar(12)     not null, \
       registercd    varchar(8), \
       updated       timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table chair_textbook_dat add constraint pk_chair_book_dat primary key \
      (year,semester,chaircd,textbookcd)
