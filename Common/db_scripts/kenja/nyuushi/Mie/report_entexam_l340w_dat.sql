-- $Id: 4ca9e6aacfb5b7faa2cd0b995189272b7ddb87e5 $

drop table REPORT_ENTEXAM_L340W_DAT
create table REPORT_ENTEXAM_L340W_DAT ( \
  EDBOARD_SCHOOLCD  varchar(12) not null, \
  ENTEXAMYEAR       varchar(4)  not null, \
  CSV_PRG           smallint    not null, \
  CSVDIV            smallint    not null, \
  EXECUTE_DATE      timestamp, \
  REGISTERCD        varchar(10), \
  UPDATED           timestamp default current timestamp \
)

alter table REPORT_ENTEXAM_L340W_DAT \
add constraint PK_REPORT_ENTL340W primary key (EDBOARD_SCHOOLCD, ENTEXAMYEAR, CSV_PRG, CSVDIV)
