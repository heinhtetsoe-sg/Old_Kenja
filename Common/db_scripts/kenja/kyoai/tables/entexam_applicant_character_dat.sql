-- $Id: entexam_applicant_character_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_APPLICANT_CHARACTER_DAT
CREATE TABLE ENTEXAM_APPLICANT_CHARACTER_DAT( \
    ENTEXAMYEAR             varchar(4)  not null, \
    EXAMNO                  varchar(5)  not null, \
    REMARK1                 varchar(750), \
    REMARK2                 varchar(750), \
    REMARK3                 varchar(750), \
    REMARK4                 varchar(750), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_APPLICANT_CHARACTER_DAT ADD CONSTRAINT PK_EXAM_CHARA PRIMARY KEY (ENTEXAMYEAR, EXAMNO)
