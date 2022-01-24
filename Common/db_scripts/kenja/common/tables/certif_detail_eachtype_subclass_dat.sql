-- $Id: a1d4fa3f5949fce59d17f24da5941b313bb6887f $

drop table CERTIF_DETAIL_EACHTYPE_SUBCLASS_DAT
create table CERTIF_DETAIL_EACHTYPE_SUBCLASS_DAT( \
    YEAR            varchar(4) not null, \
    CERTIF_INDEX    varchar(5) not null, \
    CLASSCD         varchar(2) not null, \
    SCHOOL_KIND     varchar(2) not null, \
    CURRICULUM_CD   varchar(2) not null, \
    SUBCLASSCD      varchar(6) not null, \
    SCHREGNO        varchar(8), \
    TYPE            varchar(1), \
    REMARK1         varchar(60), \
    REMARK2         varchar(60), \
    REMARK3         varchar(60), \
    REMARK4         varchar(60), \
    REMARK5         varchar(60), \
    REMARK6         varchar(60), \
    REMARK7         varchar(60), \
    REMARK8         varchar(60), \
    REMARK9         varchar(60), \
    REMARK10        varchar(60), \
    REMARK11        varchar(60), \
    REMARK12        varchar(60), \
    REMARK13        varchar(60), \
    REMARK14        varchar(60), \
    REMARK15        varchar(60), \
    REMARK16        varchar(60), \
    REMARK17        varchar(60), \
    REMARK18        varchar(60), \
    REMARK19        varchar(60), \
    REMARK20        varchar(60), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CERTIF_DETAIL_EACHTYPE_SUBCLASS_DAT add constraint PK_CERTIF_D_E_SD primary key (YEAR, CERTIF_INDEX)
