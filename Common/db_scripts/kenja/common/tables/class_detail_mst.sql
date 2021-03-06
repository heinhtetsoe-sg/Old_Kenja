-- $Id: a75852630fa1f053c87c6fc5091ebbd4c0c669dd $

drop table CLASS_DETAIL_MST

create table CLASS_DETAIL_MST ( \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CLASS_SEQ       VARCHAR(3) NOT NULL, \
    CLASS_REMARK1   VARCHAR(15), \
    CLASS_REMARK2   VARCHAR(15), \
    CLASS_REMARK3   VARCHAR(15), \
    CLASS_REMARK4   VARCHAR(15), \
    CLASS_REMARK5   VARCHAR(15), \
    CLASS_REMARK6   VARCHAR(15), \
    CLASS_REMARK7   VARCHAR(15), \
    CLASS_REMARK8   VARCHAR(15), \
    CLASS_REMARK9   VARCHAR(15), \
    CLASS_REMARK10  VARCHAR(15), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table CLASS_DETAIL_MST add constraint PK_CLASS_DETAIL primary key (CLASSCD, SCHOOL_KIND, CLASS_SEQ)
