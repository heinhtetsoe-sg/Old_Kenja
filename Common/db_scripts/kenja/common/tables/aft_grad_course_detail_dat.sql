-- $Id: 22c0f0c2b71ac7a3e41a2ccd79087dc2b0c854a0 $

drop table AFT_GRAD_COURSE_DETAIL_DAT
create table AFT_GRAD_COURSE_DETAIL_DAT( \
    YEAR            varchar(4) not null, \
    SEQ             integer not null, \
    DETAIL_SEQ      integer not null, \
    REMARK1         varchar(100), \
    REMARK2         varchar(100), \
    REMARK3         varchar(100), \
    REMARK4         varchar(100), \
    REMARK5         varchar(100), \
    REMARK6         varchar(100), \
    REMARK7         varchar(100), \
    REMARK8         varchar(100), \
    REMARK9         varchar(100), \
    REMARK10        varchar(100), \
    REMARK11        varchar(100), \
    REMARK12        varchar(100), \
    REMARK13        varchar(100), \
    REMARK14        varchar(100), \
    REMARK15        varchar(100), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_GRAD_COURSE_DETAIL_DAT add constraint PK_AFT_GR_DETAIL primary key (YEAR, SEQ, DETAIL_SEQ)
