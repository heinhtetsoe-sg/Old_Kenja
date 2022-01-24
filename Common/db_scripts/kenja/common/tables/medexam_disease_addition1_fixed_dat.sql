-- kanji=´Á»ú
-- $Id:

DROP TABLE MEDEXAM_DISEASE_ADDITION1_FIXED_DAT

CREATE TABLE MEDEXAM_DISEASE_ADDITION1_FIXED_DAT \
      (EDBOARD_SCHOOLCD     varchar(12) not null, \
       YEAR                 varchar(4) not null, \
       FIXED_DATE           date not null, \
       GRADE                varchar(2) not null, \
       SEX                  varchar(1) not null, \
       NUTRITIONCD01        int, \
       NUTRITIONCD02        int, \
       NUTRITIONCD03        int, \
       SPINERIBCD01         int, \
       SPINERIBCD02         int, \
       SPINERIBCD03         int, \
       SPINERIBCD99         int, \
       SKINDISEASECD01      int, \
       SKINDISEASECD02      int, \
       SKINDISEASECD03      int, \
       SKINDISEASECD99      int, \
       OTHERDISEASECD01     int, \
       OTHERDISEASECD02     int, \
       OTHERDISEASECD03     int, \
       OTHERDISEASECD04     int, \
       OTHERDISEASECD05     int, \
       OTHERDISEASECD99     int, \
       REGISTERCD           varchar(10), \
       UPDATED              timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table MEDEXAM_DISEASE_ADDITION1_FIXED_DAT add constraint PK_MEDEX_D_A1_FD primary key (EDBOARD_SCHOOLCD, YEAR, FIXED_DATE, GRADE, SEX)
