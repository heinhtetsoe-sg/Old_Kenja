-- $Id: adad4d24814cf3e1949675afb772c45be53276b9 $

DROP TABLE JVIEWSTAT_INPUTSEQ_DAT_OLD
RENAME TABLE JVIEWSTAT_INPUTSEQ_DAT TO JVIEWSTAT_INPUTSEQ_DAT_OLD
create table JVIEWSTAT_INPUTSEQ_DAT(  \
        YEAR          VARCHAR(4)  NOT NULL, \
        CLASSCD       VARCHAR(2)  NOT NULL, \
        SCHOOL_KIND   VARCHAR(2)  NOT NULL, \
        CURRICULUM_CD VARCHAR(2)  NOT NULL, \
        SUBCLASSCD    VARCHAR(6)  NOT NULL, \
        VIEWCD        VARCHAR(4)  NOT NULL, \
        GRADE         VARCHAR(2)  NOT NULL, \
        SEMESTER      VARCHAR(1)  NOT NULL, \
        VIEWFLG       VARCHAR(1),  \
        REGISTERCD    VARCHAR(8),  \
        UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) in usr1dms index in idx1dms

INSERT INTO JVIEWSTAT_INPUTSEQ_DAT \
    SELECT \
        YEAR, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
	    VIEWCD, \
        GRADE, \
        SEMESTER, \
        VIEWFLG, \
        REGISTERCD, \
        UPDATED \
    FROM \
        JVIEWSTAT_INPUTSEQ_DAT_OLD

alter table JVIEWSTAT_INPUTSEQ_DAT  \
add constraint PK_JVS_INP_DAT  \
primary key  \
(YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD, GRADE, SEMESTER)
