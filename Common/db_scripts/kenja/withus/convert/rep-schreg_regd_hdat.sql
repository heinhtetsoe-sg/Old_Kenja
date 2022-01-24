-- $Id: rep-schreg_regd_hdat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SCHREG_REGD_HDAT_OLD

create table SCHREG_REGD_HDAT_OLD like SCHREG_REGD_HDAT

insert into SCHREG_REGD_HDAT_OLD select * from SCHREG_REGD_HDAT

drop table SCHREG_REGD_HDAT
create table SCHREG_REGD_HDAT \
      (YEAR        varchar(4)       not null, \
       SEMESTER    varchar(1)       not null, \
       GRADE       varchar(3)       not null, \
       HR_CLASS    varchar(3)       not null, \
       HR_NAME     varchar(45) , \
       HR_NAMEABBV varchar(15) , \
       HR_FACCD    varchar(4) , \
       TR_CD1      varchar(8) , \
       TR_CD2      varchar(8) , \
       TR_CD3      varchar(8) , \
       SUBTR_CD1   varchar(8) , \
       SUBTR_CD2   varchar(8) , \
       SUBTR_CD3   varchar(8) , \
       CLASSWEEKS  smallint , \
       CLASSDAYS   smallint , \
       REGISTERCD  varchar(8), \
       UPDATED     timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_REGD_HDAT add constraint PK_SCHREG_R_HDAT primary key (YEAR, SEMESTER, GRADE, HR_CLASS)

insert into SCHREG_REGD_HDAT \
select \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        HR_NAME, \
        HR_NAMEABBV, \
        HR_FACCD, \
        TR_CD1, \
        TR_CD2, \
        TR_CD3, \
        SUBTR_CD1, \
        SUBTR_CD2, \
        SUBTR_CD3, \
        CLASSWEEKS, \
        CLASSDAYS, \
        REGISTERCD, \
        UPDATED \
from SCHREG_REGD_HDAT_OLD
