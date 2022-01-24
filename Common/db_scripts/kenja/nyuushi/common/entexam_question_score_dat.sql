-- kanji=����
-- $Id: b6479e9441d785efa9b447e22f09b40fee69c8a9 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ENTEXAM_QUESTION_SCORE_DAT

create table ENTEXAM_QUESTION_SCORE_DAT ( \
    ENTEXAMYEAR             varchar(4)  not null, \
    APPLICANTDIV            varchar(4)  not null, \
    SUBCLASS_CD             varchar(1)  not null, \
    EXAMNO                  varchar(10) not null, \
    LARGE_QUESTION          varchar(2)  not null, \
    QUESTION                varchar(2)  not null, \
    QUESTION_ORDER          varchar(3)  not null, \
    PATTERN_CD              varchar(1)  not null, \
    SELECT1                 varchar(1), \
    SELECT2                 varchar(1), \
    SELECT3                 varchar(1), \
    SELECT4                 varchar(1), \
    SELECT5                 varchar(1), \
    SELECT6                 varchar(1), \
    SELECT7                 varchar(1), \
    SELECT8                 varchar(1), \
    SELECT9                 varchar(1), \
    SELECT10                varchar(1), \
    DOUBLE_MARK_FLG         varchar(1), \
    NO_MARK_FLG             varchar(1), \
    POINT                   smallint, \
    POINT_SYMBOL            varchar(3), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_QUESTION_SCORE_DAT add constraint PK_ENT_QU_SCORE_D \
      primary key (ENTEXAMYEAR, APPLICANTDIV, SUBCLASS_CD, EXAMNO, LARGE_QUESTION, QUESTION)
