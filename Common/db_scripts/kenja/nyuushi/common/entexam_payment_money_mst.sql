DROP TABLE ENTEXAM_PAYMENT_MONEY_MST

CREATE TABLE ENTEXAM_PAYMENT_MONEY_MST \
( \
    APPLICANTDIV        VARCHAR(1)  NOT NULL, \
    JUDGE_KIND          VARCHAR(1)  NOT NULL, \
    SCHOLARSHIP_DIV     VARCHAR(1), \
    JUDGE_KIND_NAME     VARCHAR(60), \
    ENT_MONEY           INTEGER, \
    ENT_MONEY_NAME      VARCHAR(60), \
    FAC_MONEY           INTEGER, \
    FAC_MONEY_NAME      VARCHAR(60), \
    LESSON_MONEY        INTEGER, \
    LESSON_MONEY_NAME   VARCHAR(60), \
    FAC_MNT_MONEY       INTEGER, \
    FAC_MNT_MONEY_NAME  VARCHAR(60), \
    REGISTERCD          VARCHAR(10),  \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_PAYMENT_MONEY_MST ADD CONSTRAINT PK_ENTEXAM_PAYMENT PRIMARY KEY (APPLICANTDIV, JUDGE_KIND)
