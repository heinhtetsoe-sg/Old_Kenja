-- $Id: entexam_applicantconfrpt_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_APPLICANTCONFRPT_DAT
CREATE TABLE ENTEXAM_APPLICANTCONFRPT_DAT( \
    ENTEXAMYEAR        VARCHAR(4)    NOT NULL, \
    EXAMNO             VARCHAR(5)    NOT NULL, \
    CONFIDENTIAL_RPT01 SMALLINT, \
    CONFIDENTIAL_RPT02 SMALLINT, \
    CONFIDENTIAL_RPT03 SMALLINT, \
    CONFIDENTIAL_RPT04 SMALLINT, \
    CONFIDENTIAL_RPT05 SMALLINT, \
    CONFIDENTIAL_RPT06 SMALLINT, \
    CONFIDENTIAL_RPT07 SMALLINT, \
    CONFIDENTIAL_RPT08 SMALLINT, \
    CONFIDENTIAL_RPT09 SMALLINT, \
    CONFIDENTIAL_RPT10 SMALLINT, \
    CONFIDENTIAL_RPT11 SMALLINT, \
    CONFIDENTIAL_RPT12 SMALLINT, \
    ABSENCE_DAYS       SMALLINT, \
    ABSENCE_DAYS2      SMALLINT, \
    ABSENCE_DAYS3      SMALLINT, \
    AVERAGE3           DECIMAL(4,2), \
    AVERAGE5           DECIMAL(4,2), \
    AVERAGE_ALL        DECIMAL(4,2), \
    TOTAL3             SMALLINT, \
    TOTAL5             SMALLINT, \
    TOTAL_ALL          SMALLINT, \
    KASANTEN_ALL       SMALLINT, \
    ABSENCE_REMARK     VARCHAR(90), \
    ABSENCE_REMARK2    VARCHAR(90), \
    ABSENCE_REMARK3    VARCHAR(90), \
    BASE_FLG           VARCHAR(1), \
    HEALTH_FLG         VARCHAR(1), \
    ACTIVE_FLG         VARCHAR(1), \
    RESPONSIBLE_FLG    VARCHAR(1), \
    ORIGINAL_FLG       VARCHAR(1), \
    MIND_FLG           VARCHAR(1), \
    NATURE_FLG         VARCHAR(1), \
    WORK_FLG           VARCHAR(1), \
    JUSTICE_FLG        VARCHAR(1), \
    PUBLIC_FLG         VARCHAR(1), \
    SPECIALACTREC      VARCHAR(90), \
    TOTALSTUDYTIME     VARCHAR(90), \
    SPECIALREPORT      VARCHAR(90), \
    REMARK1            VARCHAR(240), \
    REMARK2            VARCHAR(240), \
    REGISTERCD         VARCHAR(8), \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_APPLICANTCONFRPT_DAT ADD CONSTRAINT PK_ENTEXAM_APCNRPT PRIMARY KEY (ENTEXAMYEAR,EXAMNO)