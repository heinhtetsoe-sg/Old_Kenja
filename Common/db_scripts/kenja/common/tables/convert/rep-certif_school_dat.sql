-- ´Á»ú
-- $Id: d8f88626fb93428a8c5ad478a75dc94b73807bd1 $

drop table CERTIF_SCHOOL_DAT_OLD
create table CERTIF_SCHOOL_DAT_OLD like CERTIF_SCHOOL_DAT
insert into CERTIF_SCHOOL_DAT_OLD select * from CERTIF_SCHOOL_DAT

drop table CERTIF_SCHOOL_DAT

create table CERTIF_SCHOOL_DAT \
      (YEAR             varchar(4) not null, \
       CERTIF_KINDCD    varchar(3) not null, \
       KINDNAME         varchar(24), \
       CERTIF_NO        varchar(1), \
       SYOSYO_NAME      varchar(150), \
       SYOSYO_NAME2     varchar(30), \
       SCHOOL_NAME      varchar(90), \
       JOB_NAME         varchar(135), \
       PRINCIPAL_NAME   varchar(90), \
       REMARK1          varchar(150), \
       REMARK2          varchar(150), \
       REMARK3          varchar(150), \
       REMARK4          varchar(150), \
       REMARK5          varchar(150), \
       REMARK6          varchar(150), \
       REMARK7          varchar(150), \
       REMARK8          varchar(150), \
       REMARK9          varchar(150), \
       REMARK10         varchar(150), \
       REGISTERCD       varchar(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CERTIF_SCHOOL_DAT add constraint PK_CERTSCL_DAT primary key \
      (YEAR,CERTIF_KINDCD)

insert into CERTIF_SCHOOL_DAT \
select \
    YEAR, \
    CERTIF_KINDCD, \
    KINDNAME, \
    CERTIF_NO, \
    SYOSYO_NAME, \
    cast(null as varchar(30)), \
    SCHOOL_NAME, \
    JOB_NAME, \
    PRINCIPAL_NAME, \
    REMARK1, \
    REMARK2, \
    REMARK3, \
    REMARK4, \
    REMARK5, \
    REMARK6, \
    cast(null as varchar(150)), \
    cast(null as varchar(150)), \
    cast(null as varchar(150)), \
    cast(null as varchar(150)), \
    REGISTERCD, \
    UPDATED \
from \
    CERTIF_SCHOOL_DAT_OLD
