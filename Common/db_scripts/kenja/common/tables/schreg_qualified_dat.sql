-- $Id: 3819fde54cf36ca83a062f92b9f7ce7e73d4e315 $

drop   table SCHREG_QUALIFIED_DAT

create table SCHREG_QUALIFIED_DAT( \
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

alter table SCHREG_QUALIFIED_DAT add constraint PK_SCH_QUAL_DAT primary key (YEAR,SCHREGNO,SEQ)

