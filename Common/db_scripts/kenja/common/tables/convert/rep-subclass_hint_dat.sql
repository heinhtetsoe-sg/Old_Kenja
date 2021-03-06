-- $Id: 66c69cdecf1b56fac11782fa183b7ccf03c0e191 $

drop table SUBCLASS_HINT_DAT_OLD
create table SUBCLASS_HINT_DAT_OLD like SUBCLASS_HINT_DAT
insert into  SUBCLASS_HINT_DAT_OLD select * from SUBCLASS_HINT_DAT

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

insert into SUBCLASS_HINT_DAT \
    SELECT \
        YEAR, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        HINTDIV, \
        PERIODCD, \
        REGISTERCD, \
        UPDATED \
    FROM \
        SUBCLASS_HINT_DAT_OLD