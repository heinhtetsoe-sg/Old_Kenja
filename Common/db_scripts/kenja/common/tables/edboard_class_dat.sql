-- $Id: cd68537a30d6238957dfecdace3f3ab415a81e5c $

DROP   TABLE EDBOARD_CLASS_DAT
CREATE TABLE EDBOARD_CLASS_DAT ( \
    EDBOARD_SCHOOLCD VARCHAR(12) NOT NULL, \
    CLASSCD          VARCHAR(2) NOT NULL, \
    SCHOOL_KIND      VARCHAR(2) NOT NULL, \
    EDBOARD_FLG      VARCHAR(1), \
    REGISTERCD       VARCHAR(8), \
    UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

alter table EDBOARD_CLASS_DAT add constraint PK_ED_CLASS_DAT primary key (EDBOARD_SCHOOLCD, CLASSCD, SCHOOL_KIND)

