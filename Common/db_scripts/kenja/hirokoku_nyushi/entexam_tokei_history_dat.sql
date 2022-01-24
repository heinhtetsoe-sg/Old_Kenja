
create table ENTEXAM_TOKEI_HISTORY_DAT( \
    ENTEXAMYEAR   VARCHAR(4)   NOT NULL, \
    APPLICANTDIV  VARCHAR(1)   NOT NULL, \
    TESTDIV       VARCHAR(1)   NOT NULL, \
    SHDIV         VARCHAR(1)   NOT NULL, \
    SEX           VARCHAR(1)   NOT NULL, \
    KIND_CD       VARCHAR(1)   NOT NULL, \
    REMARK1       VARCHAR(10) , \
    REMARK2       VARCHAR(10) , \
    REMARK3       VARCHAR(10) , \
    REMARK4       VARCHAR(10) , \
    REMARK5       VARCHAR(10) , \
    REMARK6       VARCHAR(10) , \
    REMARK7       VARCHAR(10) , \
    REMARK8       VARCHAR(10) , \
    REMARK9       VARCHAR(10) , \
    REMARK10      VARCHAR(10) , \
    REGISTERCD    VARCHAR(10) , \
    UPDATED       TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table ENTEXAM_TOKEI_HISTORY_DAT add constraint PK_ENTEXAM_TH_D primary key (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,SHDIV,SEX,KIND_CD)
