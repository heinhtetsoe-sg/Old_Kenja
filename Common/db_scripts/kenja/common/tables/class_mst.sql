-- $Id: 87c545110e08a5269de62c788d7b5f30e9d5620d $

drop   table CLASS_MST
create table CLASS_MST ( \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
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
