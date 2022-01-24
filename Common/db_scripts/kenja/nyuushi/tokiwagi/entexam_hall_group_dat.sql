-- $Id: 1d741e4a6311ec68651672cb68bf1aaf218d9dcb $

DROP TABLE ENTEXAM_HALL_GROUP_DAT
CREATE TABLE ENTEXAM_HALL_GROUP_DAT( \
    ENTEXAMYEAR             VARCHAR(4)  NOT NULL, \
    APPLICANTDIV            VARCHAR(1)  NOT NULL, \
    TESTDIV                 VARCHAR(2)  NOT NULL, \
    EXAMHALL_TYPE           VARCHAR(1)  NOT NULL, \
    EXAMNO                  VARCHAR(10) NOT NULL, \
    EXAMHALLCD              VARCHAR(4), \
    EXAMHALLGROUPCD         VARCHAR(3), \
    EXAMHALLGROUP_ORDER     VARCHAR(2), \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_HALL_GROUP_DAT ADD CONSTRAINT PK_ENT_HALL_GR_D PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAMHALL_TYPE,EXAMNO)