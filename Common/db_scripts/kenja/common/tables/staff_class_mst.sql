-- $Id: ac4ffcdab78aea860dfe3b67aef6d3e744ef4099 $
drop table STAFF_CLASS_YDAT
drop view V_STAFF_CLASS_MST

drop table STAFF_CLASS_MST
create table STAFF_CLASS_MST( \
    STAFFCD         VARCHAR(8) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    SDATE           DATE NOT NULL, \
    EDATE           DATE NOT NULL, \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table STAFF_CLASS_MST add constraint PK_STAFF_CLASS_MST primary key(STAFFCD, CLASSCD, SCHOOL_KIND, SDATE)

