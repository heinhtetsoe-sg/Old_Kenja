-- $Id: 6fece638033d6a4f9df711bde73dc2ce85edfaf7 $

DROP TABLE ENTEXAM_HALL_GROUP_YMST
CREATE TABLE ENTEXAM_HALL_GROUP_YMST( \
    ENTEXAMYEAR             VARCHAR(4)  NOT NULL, \
    APPLICANTDIV            VARCHAR(1)  NOT NULL, \
    TESTDIV                 VARCHAR(2)  NOT NULL, \
    EXAMHALL_TYPE           VARCHAR(1)  NOT NULL, \
    EXAMHALLCD              VARCHAR(4)  NOT NULL, \
    EXAMHALLGROUPCD         VARCHAR(3)  NOT NULL, \
    EXAMHALLGROUP_NAME      VARCHAR(30), \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_HALL_GROUP_YMST ADD CONSTRAINT PK_ENT_HALL_GR_Y PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAMHALL_TYPE,EXAMHALLCD,EXAMHALLGROUPCD)