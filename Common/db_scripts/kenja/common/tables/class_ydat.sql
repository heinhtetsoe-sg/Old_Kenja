-- $Id: 95d507759ad35e70cfecde2fd6e8c13a0bc8e8a3 $

drop   table CLASS_YDAT
create table CLASS_YDAT ( \
    YEAR            VARCHAR(4) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table CLASS_YDAT add constraint PK_CLASS_YDAT primary key (YEAR, CLASSCD, SCHOOL_KIND)

