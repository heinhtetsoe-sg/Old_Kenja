-- $Id: baac79bf3bda81a4b5e4b3a6997008f4e94fd9ac $

drop   table SCHREG_QUALIFIED_DAT_OLD
create table SCHREG_QUALIFIED_DAT_OLD like SCHREG_QUALIFIED_DAT
insert into  SCHREG_QUALIFIED_DAT_OLD select * from SCHREG_QUALIFIED_DAT

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

insert into SCHREG_QUALIFIED_DAT \
  select \
     YEAR, \
     SCHREGNO, \
     SEQ, \
     REGDDATE, \
     LEFT(SUBCLASSCD, 2) AS CLASSCD, \
     'H' AS SCHOOL_KIND, \
     '2' AS CURRICULUM_CD, \
     SUBCLASSCD, \
     CONDITION_DIV, \
     CONTENTS, \
     REMARK, \
     CREDITS, \
     REGISTERCD, \
     UPDATED \
  from SCHREG_QUALIFIED_DAT_OLD \

alter table SCHREG_QUALIFIED_DAT add constraint PK_SCH_QUAL_DAT primary key (YEAR,SCHREGNO,SEQ)

