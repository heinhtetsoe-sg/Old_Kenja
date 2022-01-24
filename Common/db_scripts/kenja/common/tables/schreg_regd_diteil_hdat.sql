-- $Id: 37d43a3a75d3663e47902200fff83ad07f5f8345 $

drop   table SCHREG_REGD_DITEIL_HDAT

create table SCHREG_REGD_DITEIL_HDAT \
      (YEAR              varchar(4)      not null, \
       SEMESTER          varchar(1)      not null, \
       GRADE             varchar(2)      not null, \
       HR_CLASS          varchar(3)      not null, \
       COURSECD          varchar(1), \
       MAJORCD           varchar(3), \
       COURSECODE        varchar(4), \
       REGISTERCD        varchar(8), \
       UPDATED           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_REGD_DITEIL_HDAT add constraint PK_SCHREG_R_HDAT primary key (YEAR, SEMESTER, GRADE, HR_CLASS)

