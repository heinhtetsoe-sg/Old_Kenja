-- $Id: 6b294ededda6e59a0cececf14af8d7c0627ea065 $

drop table CLASS_YDAT_OLD
create table CLASS_YDAT_OLD like CLASS_YDAT
insert into  CLASS_YDAT_OLD select * from CLASS_YDAT

drop   table CLASS_YDAT
create table CLASS_YDAT ( \
    YEAR            VARCHAR(4) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table CLASS_YDAT add constraint PK_CLASS_YDAT primary key (YEAR, CLASSCD, SCHOOL_KIND)

insert into CLASS_YDAT \
    SELECT \
        YEAR, \
        CLASSCD, \
        SCHOOL_KIND, \
        REGISTERCD, \
        UPDATED \
    FROM \
        CLASS_YDAT_OLD \
    WHERE \
        CURRICULUM_CD = '2'
