-- kanji=����
-- $Id:

DROP TABLE MEDEXAM_DISEASE_ADDITION_DAT

CREATE TABLE MEDEXAM_DISEASE_ADDITION_DAT \
      (SCHOOLCD     VARCHAR(4) NOT NULL, \
       YEAR         VARCHAR(4) NOT NULL, \
       TYPE         VARCHAR(2) NOT NULL, \
       GRADE        VARCHAR(2) NOT NULL, \
       HR_CLASS     VARCHAR(3) NOT NULL, \
       SEX          VARCHAR(1) NOT NULL, \
       LARGE_DIV    VARCHAR(2) NOT NULL, \
       SMALL_DIV    VARCHAR(2) NOT NULL, \
       COUNT        SMALLINT, \
       REGISTERCD   VARCHAR(8), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table MEDEXAM_DISEASE_ADDITION_DAT add constraint PK_MEDEXAM_DI_A_D primary key (SCHOOLCD, YEAR, TYPE, GRADE, HR_CLASS, SEX, LARGE_DIV, SMALL_DIV)
