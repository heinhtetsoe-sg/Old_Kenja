-- $Id: 14024e398a3f57248835e1727fb1976329b5e470 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

DROP TABLE ENTEXAM_INTERVIEW_HALL_DAT
CREATE TABLE ENTEXAM_INTERVIEW_HALL_DAT( \
    ENTEXAMYEAR               VARCHAR(4)    NOT NULL, \
    APPLICANTDIV              VARCHAR(1)    NOT NULL, \
    TESTDIV                   VARCHAR(2)    NOT NULL, \
    EXAMNO                    VARCHAR(10)   NOT NULL, \
    TEST_ROOM                 VARCHAR(2), \
    INTERVIEW_SETTIME         VARCHAR(2), \
    INTERVIEW_ENDTIME         varchar(2), \
    INTERVIEW_WAITINGROOM     VARCHAR(2), \
    INTERVIEW_ROOM            VARCHAR(2), \
    INTERVIEW_GROUP           VARCHAR(2), \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_INTERVIEW_HALL_DAT ADD CONSTRAINT PK_INTERVIEW_HALL PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAMNO)
