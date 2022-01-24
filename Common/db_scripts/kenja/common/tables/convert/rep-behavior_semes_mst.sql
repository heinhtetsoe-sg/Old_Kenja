-- $Id: ee810fd555f67baff47788565ac6ff92ae0835e5 $

DROP   TABLE BEHAVIOR_SEMES_MST_OLD
RENAME TABLE BEHAVIOR_SEMES_MST TO BEHAVIOR_SEMES_MST_OLD

CREATE TABLE BEHAVIOR_SEMES_MST ( \
   YEAR             varchar(4) not null, \
   GRADE            varchar(2) not null, \
   CODE             varchar(2) not null, \
   CODENAME         varchar(45), \
   VIEWNAME         varchar(210), \
   STUDYREC_CODE    varchar(2), \
   REGISTERCD       varchar(10), \
   UPDATED          timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO BEHAVIOR_SEMES_MST \
    SELECT \
        YEAR, \
        GRADE, \
        CODE, \
        CODENAME, \
        VIEWNAME, \
        STUDYREC_CODE, \
        REGISTERCD, \
        UPDATED \
    FROM \
        BEHAVIOR_SEMES_MST_OLD

ALTER TABLE BEHAVIOR_SEMES_MST ADD CONSTRAINT PK_BEHAVIOR_SEM_M \
        PRIMARY KEY (YEAR, GRADE, CODE)
