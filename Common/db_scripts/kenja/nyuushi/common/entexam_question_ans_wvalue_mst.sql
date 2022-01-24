-- kanji=����
-- $Id: dfca3a71e4c6dfe9680bda28ad5189ebf66f236c $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ENTEXAM_QUESTION_ANS_WVALUE_MST

create table ENTEXAM_QUESTION_ANS_WVALUE_MST ( \
    VALUE                   varchar(3)  not null, \
    ANSWER1                 varchar(2), \
    ANSWER2                 varchar(2), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_QUESTION_ANS_WVALUE_MST add constraint PK_ENT_Q_ANS_WV_M \
      primary key (VALUE)
