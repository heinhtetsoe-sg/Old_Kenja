-- kanji=漢字
-- $Id:

DROP TABLE MEDEXAM_DISEASE_ADDITION2_FIXED_DAT

CREATE TABLE MEDEXAM_DISEASE_ADDITION2_FIXED_DAT \
      (EDBOARD_SCHOOLCD      VARCHAR(12) NOT NULL, \
       YEAR                  VARCHAR(4) NOT NULL, \
       FIXED_DATE            date not null, \
       ADDITION_DATE         DATE    NOT NULL, \
       GRADE                 VARCHAR(2) NOT NULL, \
       HR_CLASS              VARCHAR(3) NOT NULL, \
       ABSENCE01             SMALLINT, \
       ABSENCE02             SMALLINT, \
       ABSENCE03             SMALLINT, \
       ABSENCE04             SMALLINT, \
       ABSENCE05             SMALLINT, \
       ABSENCE06             SMALLINT, \
       ABSENCE07             SMALLINT, \
       ABSENCE08             SMALLINT, \
       ATTENDSUSPEND01       SMALLINT, \
       ATTENDSUSPEND02       SMALLINT, \
       ATTENDSUSPEND03       SMALLINT, \
       ATTENDSUSPEND04       SMALLINT, \
       ATTENDSUSPEND05       SMALLINT, \
       ATTENDSUSPEND06       SMALLINT, \
       ATTENDSUSPEND07       SMALLINT, \
       ATTENDSUSPEND08       SMALLINT, \
       ATTENDSUSPEND09       SMALLINT, \
       ATTENDSUSPEND10       SMALLINT, \
       ATTENDSUSPEND11       SMALLINT, \
       TOTALSUM01            SMALLINT, \
       TOTALSUM02            SMALLINT, \
       TOTALSUM03            SMALLINT, \
       TOTALSUM04            SMALLINT, \
       TOTALSUM04_PERCENT    DECIMAL(4,1), \
       TOTALSUM05            SMALLINT, \
       TOTALSUM05_PERCENT    DECIMAL(4,1), \
       TOTALSUM06            SMALLINT, \
       REGISTERCD            VARCHAR(10), \
       UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table MEDEXAM_DISEASE_ADDITION2_FIXED_DAT add constraint PK_MEDEXAM_D_A2_F primary key (EDBOARD_SCHOOLCD, YEAR, FIXED_DATE, ADDITION_DATE, GRADE, HR_CLASS)
