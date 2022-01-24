-- $Id: 5b46e8b85611721ef68b222e06bab7d75f360722 $

DROP TABLE EDBOARD_ENTEXAM_APPLICANTCONFRPT_DAT_OLD

RENAME TABLE EDBOARD_ENTEXAM_APPLICANTCONFRPT_DAT TO EDBOARD_ENTEXAM_APPLICANTCONFRPT_DAT_OLD

CREATE TABLE EDBOARD_ENTEXAM_APPLICANTCONFRPT_DAT( \
    EDBOARD_SCHOOLCD   archar(12)    not null, \
    ENTEXAMYEAR        varchar(4)    not null, \
    APPLICANTDIV       varchar(1)    not null, \
    EXAMNO             varchar(10)   not null, \
    CONFIDENTIAL_RPT01 smallint, \
    CONFIDENTIAL_RPT02 smallint, \
    CONFIDENTIAL_RPT03 smallint, \
    CONFIDENTIAL_RPT04 smallint, \
    CONFIDENTIAL_RPT05 smallint, \
    CONFIDENTIAL_RPT06 smallint, \
    CONFIDENTIAL_RPT07 smallint, \
    CONFIDENTIAL_RPT08 smallint, \
    CONFIDENTIAL_RPT09 smallint, \
    CONFIDENTIAL_RPT10 smallint, \
    CONFIDENTIAL_RPT11 smallint, \
    CONFIDENTIAL_RPT12 smallint, \
    ABSENCE_DAYS       smallint, \
    ABSENCE_DAYS2      smallint, \
    ABSENCE_DAYS3      smallint, \
    AVERAGE3           decimal(4,1), \
    AVERAGE5           decimal(4,1), \
    AVERAGE_ALL        decimal(4,1), \
    TOTAL3             smallint, \
    TOTAL5             smallint, \
    TOTAL_ALL          smallint, \
    KASANTEN_ALL       smallint, \
    ABSENCE_REMARK     varchar(90), \
    ABSENCE_REMARK2    varchar(90), \
    ABSENCE_REMARK3    varchar(90), \
    BASE_FLG           varchar(1), \
    HEALTH_FLG         varchar(1), \
    ACTIVE_FLG         varchar(1), \
    RESPONSIBLE_FLG    varchar(1), \
    ORIGINAL_FLG       varchar(1), \
    MIND_FLG           varchar(1), \
    NATURE_FLG         varchar(1), \
    WORK_FLG           varchar(1), \
    JUSTICE_FLG        varchar(1), \
    PUBLIC_FLG         varchar(1), \
    SPECIALACTREC      varchar(90), \
    TOTALSTUDYTIME     varchar(90), \
    SPECIALREPORT      varchar(90), \
    REMARK1            varchar(240), \
    REMARK2            varchar(1680), \
    REGISTERCD         varchar(10), \
    UPDATED            timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO EDBOARD_ENTEXAM_APPLICANTCONFRPT_DAT \
    SELECT \
        EDBOARD_SCHOOLCD, \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        EXAMNO, \
        CONFIDENTIAL_RPT01, \
        CONFIDENTIAL_RPT02, \
        CONFIDENTIAL_RPT03, \
        CONFIDENTIAL_RPT04, \
        CONFIDENTIAL_RPT05, \
        CONFIDENTIAL_RPT06, \
        CONFIDENTIAL_RPT07, \
        CONFIDENTIAL_RPT08, \
        CONFIDENTIAL_RPT09, \
        CONFIDENTIAL_RPT10, \
        CONFIDENTIAL_RPT11, \
        CONFIDENTIAL_RPT12, \
        ABSENCE_DAYS, \
        ABSENCE_DAYS2, \
        ABSENCE_DAYS3, \
        AVERAGE3, \
        AVERAGE5, \
        AVERAGE_ALL, \
        TOTAL3, \
        TOTAL5, \
        TOTAL_ALL, \
        KASANTEN_ALL, \
        ABSENCE_REMARK, \
        ABSENCE_REMARK2, \
        ABSENCE_REMARK3, \
        BASE_FLG, \
        HEALTH_FLG, \
        ACTIVE_FLG, \
        RESPONSIBLE_FLG, \
        ORIGINAL_FLG, \
        MIND_FLG, \
        NATURE_FLG, \
        WORK_FLG, \
        JUSTICE_FLG, \
        PUBLIC_FLG, \
        SPECIALACTREC, \
        TOTALSTUDYTIME, \
        SPECIALREPORT, \
        REMARK1, \
        REMARK2, \
        REGISTERCD, \
        UPDATED \
    FROM \
        EDBOARD_ENTEXAM_APPLICANTCONFRPT_DAT_OLD

ALTER TABLE EDBOARD_ENTEXAM_APPLICANTCONFRPT_DAT ADD CONSTRAINT PK_ED_EE_APCNRPT PRIMARY KEY (EDBOARD_SCHOOLCD, ENTEXAMYEAR, APPLICANTDIV, EXAMNO)