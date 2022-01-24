-- kanji=漢字
-- $Id: 688166c4ac23bc8eee0611231d3806bb4b4b4663 $

DROP TABLE ENTEXAM_HONORDIV_MST

CREATE TABLE ENTEXAM_HONORDIV_MST \
( \
    ENTEXAMYEAR                 VARCHAR(4)  NOT NULL, \
    APPLICANTDIV                VARCHAR(1)  NOT NULL, \
    HONORDIV                    VARCHAR(2)  NOT NULL, \
    HONORDIV_NAME               VARCHAR(60), \
    HONORDIV_ABBV               VARCHAR(30), \
    CLUB_FLG                    VARCHAR(1), \
    REMARK                      VARCHAR(150), \
    NOTICE_CLASS                VARCHAR(2), \
    NOTICE_KIND                 VARCHAR(2), \
    ENROLL_FEES                 VARCHAR(60), \
    SCHOOL_FEES                 VARCHAR(60), \
    SCHOLARSHIP1                VARCHAR(60), \
    SCHOLARSHIP2                VARCHAR(60), \
    REGISTERCD                  VARCHAR(10), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
    HONOR_TYPE                  VARCHAR(1), \
    PRIORITY                    VARCHAR(2), \
    ENROLL_FEES2                VARCHAR(60) \

) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_HONORDIV_MST ADD CONSTRAINT PK_ENTEXAM_HONORDIV PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,HONORDIV)
