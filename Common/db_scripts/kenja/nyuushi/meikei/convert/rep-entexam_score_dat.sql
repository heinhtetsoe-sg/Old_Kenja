-- kanji=����
-- $Id: ea9eba0e45c7ed7a081bd2a8c1bf5527891473b9 $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback

drop table ENTEXAM_SCORE_DAT_OLD
create table ENTEXAM_SCORE_DAT_OLD like ENTEXAM_SCORE_DAT
insert into ENTEXAM_SCORE_DAT_OLD select * from ENTEXAM_SCORE_DAT

DROP TABLE ENTEXAM_SCORE_DAT
CREATE TABLE ENTEXAM_SCORE_DAT( \
    ENTEXAMYEAR    VARCHAR(4)    NOT NULL, \
    APPLICANTDIV   VARCHAR(1)    NOT NULL, \
    TESTDIV        VARCHAR(2)    NOT NULL, \
    EXAM_TYPE      VARCHAR(2)    NOT NULL, \
    RECEPTNO       VARCHAR(20)   NOT NULL, \
    TESTSUBCLASSCD VARCHAR(1)    NOT NULL, \
    ATTEND_FLG     VARCHAR(1), \
    SCORE          SMALLINT, \
    STD_SCORE      DECIMAL(5,2), \
    RANK           SMALLINT, \
    SCORE2         SMALLINT, \
    SCORE3         SMALLINT, \
    REGISTERCD     VARCHAR(10), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_SCORE_DAT ADD CONSTRAINT PK_ENTEXAM_SCORE PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO,TESTSUBCLASSCD)

insert into ENTEXAM_SCORE_DAT \
select \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        '0' || TESTDIV, \
        EXAM_TYPE, \
        RECEPTNO, \
        TESTSUBCLASSCD, \
        ATTEND_FLG, \
        SCORE, \
        STD_SCORE, \
        RANK, \
        SCORE2, \
        SCORE3, \
        REGISTERCD, \
        UPDATED \
from ENTEXAM_SCORE_DAT_OLD
