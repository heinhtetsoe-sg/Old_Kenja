-- $Id: 367cbe41b1be16a8a8dc19242cded8d32371580b $

-- スクリプトの使用方法: db2 +c -f <thisfile>

drop   table SUBCLASS_HINT_DAT

create table SUBCLASS_HINT_DAT ( \
    YEAR            VARCHAR(4) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CURRICULUM_CD   VARCHAR(2) NOT NULL, \
    SUBCLASSCD      VARCHAR(6) NOT NULL, \
    HINTDIV         SMALLINT NOT NULL CHECK (HINTDIV IN (0, 1, 2)), \
    PERIODCD        VARCHAR(1) NOT NULL, \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

ALTER TABLE SUBCLASS_HINT_DAT ADD CONSTRAINT PK_SUBCLASS_H_DAT PRIMARY KEY (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, HINTDIV, PERIODCD )