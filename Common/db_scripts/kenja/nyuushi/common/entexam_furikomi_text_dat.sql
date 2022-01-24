-- kanji=Š¿Žš
-- $Id: d361060d1700a96a245e26ca48f2ec413562af0e $

DROP TABLE ENTEXAM_FURIKOMI_TEXT_DAT

create table ENTEXAM_FURIKOMI_TEXT_DAT( \
    ENTEXAMYEAR            VARCHAR(4)    NOT NULL, \
    APPLICANTDIV           VARCHAR(1)    NOT NULL, \
    TESTDIV                VARCHAR(2)    NOT NULL, \
    SEQ                    VARCHAR(1)    NOT NULL, \
    TESTDIV0               VARCHAR(2)   , \
    FORM                   VARCHAR(30)  , \
    COMMENT                VARCHAR(100) , \
    NOUNYU_KIGEN1          VARCHAR(100) , \
    NOUNYU_KIGEN2          VARCHAR(100) , \
    TETSUZUKI_KIGEN1       VARCHAR(100) , \
    TETSUZUKI_KIGEN2       VARCHAR(100) , \
    TEST_NICHIJI           VARCHAR(150) , \
    TEST_BASHO             VARCHAR(100) , \
    SETSUMEIKAI_NICHIJI    VARCHAR(150) , \
    SETSUMEIKAI_BASHO      VARCHAR(100) , \
    HANBAI_NICHIJI         VARCHAR(150) , \
    HANBAI_BASHO           VARCHAR(100) , \
    STUDY_SUPPORT_NICHIJI  VARCHAR(150) , \
    REMARK1                VARCHAR(150) , \
    REMARK2                VARCHAR(150) , \
    REMARK3                VARCHAR(150) , \
    REMARK4                VARCHAR(150) , \
    REMARK5                VARCHAR(150) , \
    REGISTERCD             VARCHAR(10)   , \
    UPDATED                TIMESTAMP     DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table ENTEXAM_FURIKOMI_TEXT_DAT add constraint PK_ENTEXAM_F_T_D primary key (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,SEQ)
