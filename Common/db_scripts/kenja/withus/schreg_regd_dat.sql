-- $Id: schreg_regd_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $


drop   table SCHREG_REGD_DAT
create table SCHREG_REGD_DAT \
      (SCHREGNO    varchar(8)   not null, \
       YEAR        varchar(4)   not null, \
       SEMESTER    varchar(1)   not null, \
       GRADE       varchar(3), \
       HR_CLASS    varchar(3), \
       ATTENDNO    varchar(3), \
       ANNUAL      varchar(2), \
       ATTACH1     varchar(8), \
       ATTACH2     varchar(8), \
       PARTNER_CD  varchar(4), \
       SEAT_ROW    varchar(2), \
       SEAT_COL    varchar(2), \
       COURSECD    varchar(1), \
       MAJORCD     varchar(3), \
       COURSECODE  varchar(4), \
       COURSE_DIV  varchar(1), \
       STUDENT_DIV varchar(2), \
       REGISTERCD  varchar(8), \
       UPDATED     timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_REGD_DAT add constraint PK_SCHREG_REGD_DAT primary key (SCHREGNO, YEAR, SEMESTER)


