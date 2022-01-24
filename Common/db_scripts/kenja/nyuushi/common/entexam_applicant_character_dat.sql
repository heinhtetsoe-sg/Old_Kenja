-- $Id: 61a850f1c6f2096a657bafd80f2083e077918640 $

DROP TABLE ENTEXAM_APPLICANT_CHARACTER_DAT
CREATE TABLE ENTEXAM_APPLICANT_CHARACTER_DAT( \
    ENTEXAMYEAR             varchar(4)  not null, \
    APPLICANTDIV            varchar(1)  not null, \
    EXAMNO                  varchar(10) not null, \
    REMARK1                 varchar(750), \
    REMARK2                 varchar(750), \
    REMARK3                 varchar(750), \
    REMARK4                 varchar(750), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_APPLICANT_CHARACTER_DAT ADD CONSTRAINT PK_EXAM_CHARA PRIMARY KEY (ENTEXAMYEAR, APPLICANTDIV, EXAMNO)
