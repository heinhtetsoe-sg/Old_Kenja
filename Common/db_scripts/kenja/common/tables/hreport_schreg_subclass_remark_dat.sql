-- $Id: 6675f07784e1903b688334b237cc03059c9e6732 $

drop table HREPORT_SCHREG_SUBCLASS_REMARK_DAT

create table HREPORT_SCHREG_SUBCLASS_REMARK_DAT( \
    YEAR           varchar(4)     not null, \
    SEMESTER       varchar(1)     not null, \
    SCHREGNO       varchar(8)     not null, \
    CLASSCD        varchar(2)     not null, \
    SCHOOL_KIND    varchar(2)     not null, \
    CURRICULUM_CD  varchar(2)     not null, \
    SUBCLASSCD     varchar(6)     not null, \
    UNITCD         varchar(2)     not null, \
    SEQ            smallint       not null, \
    REMARK         varchar(6100)          , \
    REGISTERCD     varchar(10)            , \
    UPDATED        timestamp      default current timestamp \
 ) in usr1dms index in idx1dms

alter table HREPORT_SCHREG_SUBCLASS_REMARK_DAT add constraint PK_HR_SCH_SUB_REMARK_D \
primary key (YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, UNITCD, SEQ)
