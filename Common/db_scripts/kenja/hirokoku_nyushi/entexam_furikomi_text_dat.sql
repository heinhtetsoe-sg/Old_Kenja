-- $Id: entexam_furikomi_text_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

create table ENTEXAM_FURIKOMI_TEXT_DAT( \
    ENTEXAMYEAR            VARCHAR(4)    NOT NULL, \
    APPLICANTDIV           VARCHAR(1)    NOT NULL, \
    TESTDIV                VARCHAR(1)    NOT NULL, \
    SEQ                    VARCHAR(1)    NOT NULL, \
    TESTDIV0               VARCHAR(1)   , \
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
    REGISTERCD             VARCHAR(8)   , \
    UPDATED                TIMESTAMP     DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table ENTEXAM_FURIKOMI_TEXT_DAT add constraint PK_ENTEXAM_F_T_D primary key (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,SEQ)
