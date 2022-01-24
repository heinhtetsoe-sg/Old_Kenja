-- $Id: 75cee2de6d0d06f0293604e7e156bba18da059ab $

drop table SCHREG_BASE_DETAIL_MST

create table SCHREG_BASE_DETAIL_MST \
    (SCHREGNO            varchar(8)    not null, \
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

alter table SCHREG_BASE_DETAIL_MST add constraint PK_SCHREG_BASE_D primary key (SCHREGNO, BASE_SEQ)
