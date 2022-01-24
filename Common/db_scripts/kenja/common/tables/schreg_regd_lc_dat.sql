-- kanji=漢字
-- $Id: 35fc5581d533e124bd4d577b77a576895e7b5aed $

drop table SCHREG_REGD_LC_DAT
create table SCHREG_REGD_LC_DAT \
      (SCHREGNO     varchar(8) not null, \
       YEAR         varchar(4) not null, \
       SEMESTER     varchar(1) not null, \
       GRADE        varchar(2), \
       LC_CLASS     varchar(3), \
       ATTENDNO     varchar(3), \
       ANNUAL       varchar(2), \
       SEAT_ROW     varchar(2), \
       SEAT_COL     varchar(2), \
       COURSECD     varchar(1), \
       MAJORCD      varchar(3), \
       COURSECODE   varchar(4), \
       REGISTERCD   varchar(10), \
       UPDATED      timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_REGD_LC_DAT add constraint PK_SCH_R_LC_DAT primary key (SCHREGNO, YEAR, SEMESTER)
