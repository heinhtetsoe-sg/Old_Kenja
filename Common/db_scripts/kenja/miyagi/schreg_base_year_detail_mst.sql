-- $Id: schreg_base_year_detail_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SCHREG_BASE_YEAR_DETAIL_MST

create table SCHREG_BASE_YEAR_DETAIL_MST \
    (SCHREGNO            varchar(8)    not null, \
     YEAR                varchar(4)    not null, \
     BASE_SEQ            varchar(3)    not null, \
     BASE_REMARK1        varchar(768), \
     BASE_REMARK2        varchar(768), \
     BASE_REMARK3        varchar(768), \
     BASE_REMARK4        varchar(768), \
     BASE_REMARK5        varchar(768), \
     BASE_REMARK6        varchar(768), \
     REGISTERCD          varchar(8), \
     UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SCHREG_BASE_YEAR_DETAIL_MST add constraint PK_SCHREG_BASE_Y_D primary key (SCHREGNO, YEAR, BASE_SEQ)
