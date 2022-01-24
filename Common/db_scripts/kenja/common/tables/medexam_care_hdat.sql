-- $Id: fc554094f4a50607032893a458348dcce8093b7e $

DROP TABLE MEDEXAM_CARE_HDAT

CREATE TABLE MEDEXAM_CARE_HDAT \
      (YEAR                 VARCHAR(4) NOT NULL, \
       SCHREGNO             VARCHAR(8) NOT NULL, \
       CARE_DIV             VARCHAR(2) NOT NULL, \
       CARE_FLG             VARCHAR(1), \
       EMERGENCYNAME        VARCHAR(30), \
       EMERGENCYTELNO       VARCHAR(14), \
       EMERGENCYNAME2       VARCHAR(120), \
       EMERGENCYTELNO2      VARCHAR(14), \
       DATE                 DATE, \
       DOCTOR               VARCHAR(30), \
       HOSPITAL             VARCHAR(120), \
       REMARK               VARCHAR(2100), \
       REGISTERCD           VARCHAR(10), \
       UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table MEDEXAM_CARE_HDAT add constraint PK_MEDEXAM_C_H primary key (YEAR, SCHREGNO, CARE_DIV)
