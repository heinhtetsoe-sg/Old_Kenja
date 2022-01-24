-- $Id: c39fe3a3c0a81ddb6c5511f989198b205105fd7c $

drop table SCHREG_REGD_LC_HDAT
create table SCHREG_REGD_LC_HDAT \
      (YEAR              varchar(4)      not null, \
       SEMESTER          varchar(1)      not null, \
       GRADE             varchar(2)      not null, \
       LC_CLASS          varchar(3)      not null, \
       LC_NAME           varchar(15), \
       LC_NAMEABBV       varchar(5), \
       GRADE_NAME        varchar(30), \
       LC_CLASS_NAME1    varchar(30), \
       LC_CLASS_NAME2    varchar(30), \
       LC_FACCD          varchar(4), \
       TR_CD1            varchar(10), \
       TR_CD2            varchar(10), \
       TR_CD3            varchar(10), \
       SUBTR_CD1         varchar(10), \
       SUBTR_CD2         varchar(10), \
       SUBTR_CD3         varchar(10), \
       CLASSWEEKS        smallint, \
       CLASSDAYS         smallint, \
       REGISTERCD        varchar(10), \
       UPDATED           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_REGD_LC_HDAT add constraint PK_SCH_R_LC_HDAT primary key (YEAR, SEMESTER, GRADE, LC_CLASS)
