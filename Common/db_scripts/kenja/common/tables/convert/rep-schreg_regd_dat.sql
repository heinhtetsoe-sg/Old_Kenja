-- $Id: 66e14f40b12cf75d8665f5890072b174136b72e0 $

drop   table TMP_schreg_regd_dat
create table TMP_schreg_regd_dat \
      (schregno          varchar(8)      not null, \
       year              varchar(4)      not null, \
       semester          varchar(1)      not null, \
       grade             varchar(2), \
       hr_class          varchar(3), \
       attendno          varchar(3), \
       annual            varchar(2), \
       seat_row          varchar(2), \   
       seat_col          varchar(2), \
       coursecd          varchar(1), \
       majorcd           varchar(3), \
       coursecode        varchar(4), \
       registercd        varchar(8), \
       updated           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

insert into TMP_schreg_regd_dat \
  select \
        SCHREGNO, \
        YEAR, \
        SEMESTER, \
        GRADE, \
        RIGHT(RTRIM('000'||HR_CLASS),3) as HR_CLASS, \
        ATTENDNO, \
        ANNUAL, \
        SEAT_ROW, \
        SEAT_COL, \
        COURSECD, \
        MAJORCD, \
        COURSECODE, \
        REGISTERCD, \
        UPDATED \
  from schreg_regd_dat

drop table schreg_regd_dat_OLD

rename table     schreg_regd_dat to schreg_regd_dat_OLD

rename table TMP_schreg_regd_dat to schreg_regd_dat

alter table schreg_regd_dat add constraint pk_schreg_regd_dat primary key (schregno,year,semester)

