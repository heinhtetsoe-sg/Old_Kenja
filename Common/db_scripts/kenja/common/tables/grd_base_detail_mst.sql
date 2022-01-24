-- $Id: 54606b91b872e8d5e76793add27b0a30695b3168 $

DROP TABLE GRD_BASE_DETAIL_MST
CREATE TABLE GRD_BASE_DETAIL_MST( \
    SCHREGNO                varchar(8)    not null, \
    SEQ                     varchar(3)    not null, \
    REMARK1                 varchar(225), \
    REMARK2                 varchar(225), \
    REMARK3                 varchar(225), \
    REMARK4                 varchar(225), \
    REMARK5                 varchar(225), \
    REMARK6                 varchar(225), \
    REMARK7                 varchar(225), \
    REMARK8                 varchar(225), \
    REMARK9                 varchar(225), \
    REMARK10                varchar(225), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table GRD_BASE_DETAIL_MST add constraint PK_GRD_BASE_DET primary key(SCHREGNO, SEQ)
