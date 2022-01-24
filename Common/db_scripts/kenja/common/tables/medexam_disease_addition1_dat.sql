-- kanji=´Á»ú
-- $Id:

DROP TABLE MEDEXAM_DISEASE_ADDITION1_DAT

CREATE TABLE MEDEXAM_DISEASE_ADDITION1_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4) NOT NULL, \
       GRADE                VARCHAR(2) NOT NULL, \
       SEX                  VARCHAR(1) NOT NULL, \
       NUTRITIONCD01           INT, \
       NUTRITIONCD02           INT, \
       NUTRITIONCD03           INT, \
       SPINERIBCD01            INT, \
       SPINERIBCD02            INT, \
       SPINERIBCD03            INT, \
       SPINERIBCD99            INT, \
       SKINDISEASECD01         INT, \
       SKINDISEASECD02         INT, \
       SKINDISEASECD03         INT, \
       SKINDISEASECD99         INT, \
       OTHERDISEASECD01        INT, \
       OTHERDISEASECD02        INT, \
       OTHERDISEASECD03        INT, \
       OTHERDISEASECD04        INT, \
       OTHERDISEASECD05        INT, \
       OTHERDISEASECD99        INT, \
       REGISTERCD   VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table MEDEXAM_DISEASE_ADDITION1_DAT add constraint PK_MEDEXAM_D_A1_D primary key (EDBOARD_SCHOOLCD, YEAR, GRADE, SEX)
