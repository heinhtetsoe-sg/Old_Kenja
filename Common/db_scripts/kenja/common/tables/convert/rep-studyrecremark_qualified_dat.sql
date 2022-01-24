-- $Id: dc9a32931a13dc474c8ddd211822c38f94be10f8 $

drop   table STUDYRECREMARK_QUALIFIED_DAT_OLD
rename table STUDYRECREMARK_QUALIFIED_DAT to STUDYRECREMARK_QUALIFIED_DAT_OLD

create table STUDYRECREMARK_QUALIFIED_DAT( \
         YEAR               VARCHAR(4) NOT NULL, \
         SCHREGNO           VARCHAR(8) NOT NULL, \
         SEQ                SMALLINT   NOT NULL, \
         REGDDATE           DATE       NOT NULL, \
         CLASSCD            VARCHAR(2) NOT NULL, \
         SCHOOL_KIND        VARCHAR(2) NOT NULL, \
         CURRICULUM_CD      VARCHAR(2) NOT NULL, \
         SUBCLASSCD         VARCHAR(6) NOT NULL, \
         CONDITION_DIV      VARCHAR(1), \
         CONTENTS           VARCHAR(90), \
         REMARK             VARCHAR(90), \
         CREDITS            SMALLINT, \
         REGISTERCD         VARCHAR(8), \
         UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
        ) in usr1dms index in idx1dms

insert into STUDYRECREMARK_QUALIFIED_DAT \
    select \
        YEAR, \
        SCHREGNO, \
        SEQ, \
        REGDDATE, \
        SUBSTR(SUBCLASSCD, 1, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        CONDITION_DIV, \
        CONTENTS, \
        REMARK, \
        CREDITS, \
        REGISTERCD, \
        UPDATED \
    from \
        STUDYRECREMARK_QUALIFIED_DAT_OLD

alter table STUDYRECREMARK_QUALIFIED_DAT add constraint PK_STREM_QUAL_DAT primary key (YEAR,SCHREGNO,SEQ)

