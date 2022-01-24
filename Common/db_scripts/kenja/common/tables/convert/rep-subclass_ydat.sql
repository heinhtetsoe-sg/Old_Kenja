-- $Id: e398d493cc59340b7f9f03e956397de10a472d1e $

DROP VIEW V_SUBCLASS_MST
DROP TABLE SUBCLASS_YDAT_OLD
RENAME TABLE SUBCLASS_YDAT TO SUBCLASS_YDAT_OLD
CREATE TABLE SUBCLASS_YDAT( \
       YEAR             VARCHAR(4) NOT NULL, \
       CLASSCD          VARCHAR(2) NOT NULL, \
       SCHOOL_KIND      VARCHAR(2) NOT NULL, \
       CURRICULUM_CD    VARCHAR(2) NOT NULL, \
       SUBCLASSCD       VARCHAR(6) NOT NULL, \
       REGISTERCD       VARCHAR(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

INSERT INTO SUBCLASS_YDAT \
    SELECT \
        YEAR, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        REGISTERCD, \
        UPDATED \
    FROM \
        SUBCLASS_YDAT_OLD

alter table SUBCLASS_YDAT add constraint pk_subclass_ydat \
      primary key (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
