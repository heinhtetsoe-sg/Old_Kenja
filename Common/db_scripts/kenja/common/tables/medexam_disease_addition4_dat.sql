-- kanji=漢字
-- $Id: 2779b946e1a55619d181fe2961008a4190d1a944 $

DROP TABLE MEDEXAM_DISEASE_ADDITION4_DAT

CREATE TABLE MEDEXAM_DISEASE_ADDITION4_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4) NOT NULL, \
       DISEASECD            VARCHAR(3) NOT NULL, \
       GRADE                VARCHAR(2) NOT NULL, \
       HR_CLASS             VARCHAR(3) NOT NULL, \
       ACTION_S_DATE        DATE NOT NULL, \
       ACTION_E_DATE        DATE, \
       PATIENT_COUNT        SMALLINT, \
       ABSENCE_COUNT        SMALLINT, \
       PRESENCE_COUNT       SMALLINT, \
       SYMPTOM01            VARCHAR(1), \
       SYMPTOM01_REMARK     VARCHAR(150), \
       SYMPTOM02            VARCHAR(1), \
       SYMPTOM03            VARCHAR(1), \
       SYMPTOM04            VARCHAR(1), \
       SYMPTOM05            VARCHAR(1), \
       SYMPTOM06            VARCHAR(1), \
       SYMPTOM07            VARCHAR(1), \
       SYMPTOM08            VARCHAR(1), \
       SYMPTOM09            VARCHAR(1), \
       SYMPTOM10            VARCHAR(1), \
       SYMPTOM11            VARCHAR(1), \
       SYMPTOM12            VARCHAR(1), \
       SYMPTOM12_REMARK     VARCHAR(150), \
       REMARK               VARCHAR(150), \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table MEDEXAM_DISEASE_ADDITION4_DAT add constraint PK_MEDEXAM_D_A4_D primary key (EDBOARD_SCHOOLCD, YEAR, DISEASECD, GRADE, HR_CLASS, ACTION_S_DATE)
