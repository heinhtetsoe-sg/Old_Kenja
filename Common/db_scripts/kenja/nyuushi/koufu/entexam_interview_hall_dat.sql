-- $Id: ef775662f79d40b5eead4ae4645931dbc3ffab53 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ENTEXAM_INTERVIEW_HALL_DAT
CREATE TABLE ENTEXAM_INTERVIEW_HALL_DAT( \
    ENTEXAMYEAR               VARCHAR(4)    NOT NULL, \
    APPLICANTDIV              VARCHAR(1)    NOT NULL, \
    TESTDIV                   VARCHAR(1)    NOT NULL, \
    EXAMNO                    VARCHAR(5)    NOT NULL, \
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
