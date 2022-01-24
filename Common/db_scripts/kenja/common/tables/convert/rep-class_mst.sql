-- $Id: 39e75c7380f63a5452a46a1d8027dd9ff1fc8343 $

drop table CLASS_MST_OLD
create table CLASS_MST_OLD like CLASS_MST
insert into  CLASS_MST_OLD select * from CLASS_MST

drop   table CLASS_MST
create table CLASS_MST ( \
    CLASSCD         VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND     VARCHAR(2)    NOT NULL, \
    CLASSNAME       VARCHAR(90), \
    CLASSABBV       VARCHAR(90), \
    CLASSNAME_ENG   VARCHAR(40), \
    CLASSABBV_ENG   VARCHAR(30), \
    CLASSORDERNAME1 VARCHAR(60), \
    CLASSORDERNAME2 VARCHAR(60), \
    CLASSORDERNAME3 VARCHAR(60), \
    SUBCLASSES      SMALLINT, \
    SHOWORDER       SMALLINT, \
    SHOWORDER2      SMALLINT, \
    SHOWORDER3      SMALLINT, \
    SHOWORDER4      SMALLINT, \
    ELECTDIV        VARCHAR(1), \
    SPECIALDIV      VARCHAR(1), \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table CLASS_MST add constraint PK_CLASS_MST primary key (CLASSCD, SCHOOL_KIND)

insert into CLASS_MST \
    SELECT \
        CLASSCD, \
        SCHOOL_KIND, \
        CLASSNAME, \
        CLASSABBV, \
        CLASSNAME_ENG, \
        CLASSABBV_ENG, \
        CLASSORDERNAME1, \
        CLASSORDERNAME2, \
        CLASSORDERNAME3, \
        SUBCLASSES, \
        SHOWORDER, \
        SHOWORDER2, \
        SHOWORDER3, \
        SHOWORDER4, \
        ELECTDIV, \
        SPECIALDIV, \
        REGISTERCD, \
        UPDATED \
    FROM \
        CLASS_MST_OLD