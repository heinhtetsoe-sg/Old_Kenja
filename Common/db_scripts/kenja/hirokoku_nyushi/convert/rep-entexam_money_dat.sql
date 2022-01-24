-- $Id: rep-entexam_money_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_MONEY_DAT_OLD

RENAME TABLE ENTEXAM_MONEY_DAT TO ENTEXAM_MONEY_DAT_OLD

CREATE TABLE ENTEXAM_MONEY_DAT \
( \
    ENTEXAMYEAR             VARCHAR(4)  NOT NULL, \
    APPLICANTDIV            VARCHAR(1)  NOT NULL, \
    EXAMNO                  VARCHAR(5)  NOT NULL, \
    EXAM_PAY_DIV            VARCHAR(1), \
    EXAM_PAY_DATE           DATE, \
    EXAM_PAY_CHAK_DATE      DATE, \
    EXAM_PAY_MONEY          INTEGER, \
    ENT_DUE_DATE            DATE, \
    ENT_DUE_MONEY           INTEGER, \
    ENT_PAY_DIV             VARCHAR(1), \
    ENT_PAY_DATE            DATE, \
    ENT_PAY_CHAK_DATE       DATE, \
    ENT_PAY_MONEY           INTEGER, \
    EXP_DUE_DATE            DATE, \
    EXP_DUE_MONEY           INTEGER, \
    EXP_PAY_DIV             VARCHAR(1), \
    EXP_PAY_DATE            DATE, \
    EXP_PAY_CHAK_DATE       DATE, \
    EXP_PAY_MONEY           INTEGER, \
    ENTRANCE_FLG            VARCHAR(1), \
    ENTRANCE_DUE_DATE       DATE, \
    ENTRANCE_DUE_MONEY      INTEGER, \
    ENTRANCE_PAY_DIV        VARCHAR(1), \
    ENTRANCE_PAY_DATE       DATE, \
    ENTRANCE_PAY_CHAK_DATE  DATE, \
    ENTRANCE_PAY_MONEY      INTEGER, \
    ENT_REF_DATE            DATE, \
    ENT_REF_MONEY           INTEGER, \
    REGISTERCD              VARCHAR(10),  \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_MONEY_DAT ADD CONSTRAINT PK_ENTEXAM_MONEY_D PRIMARY KEY (ENTEXAMYEAR, APPLICANTDIV, EXAMNO)

INSERT INTO ENTEXAM_MONEY_DAT \
    SELECT \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        EXAMNO, \
        EXAM_PAY_DIV, \
        EXAM_PAY_DATE, \
        EXAM_PAY_CHAK_DATE, \
        EXAM_PAY_MONEY, \
        ENT_DUE_DATE, \
        ENT_DUE_MONEY, \
        ENT_PAY_DIV, \
        ENT_PAY_DATE, \
        ENT_PAY_CHAK_DATE, \
        ENT_PAY_MONEY, \
        EXP_DUE_DATE, \
        EXP_DUE_MONEY, \
        EXP_PAY_DIV, \
        EXP_PAY_DATE, \
        EXP_PAY_CHAK_DATE, \
        EXP_PAY_MONEY, \
        ENTRANCE_FLG, \
        ENTRANCE_DUE_DATE, \
        ENTRANCE_DUE_MONEY, \
        ENTRANCE_PAY_DIV, \
        ENTRANCE_PAY_DATE, \
        ENTRANCE_PAY_CHAK_DATE, \
        ENTRANCE_PAY_MONEY, \
        CAST(NULL AS DATE) AS ENT_REF_DATE, \
        CAST(NULL AS INTEGER) AS ENT_REF_MONEY, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ENTEXAM_MONEY_DAT_OLD
