-- kanji=����
-- $Id: entexam_course_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table ENTEXAM_COURSE_HIST_DAT

create table ENTEXAM_COURSE_HIST_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    TESTDIV             varchar(1)  not null, \
    EXAMNO              varchar(4)  not null, \
    SEQ                 smallint    not null, \
    JUDGEMENT           varchar(1), \
    JUDGEMENT_GROUP_NO  varchar(2), \
    SUC_COURSECD        varchar(1), \
    SUC_MAJORCD         varchar(3), \
    SUC_COURSECODE      varchar(4), \
    REGISTERCD          varchar(8),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_COURSE_HIST_DAT add constraint \
pk_entexam_app primary key (ENTEXAMYEAR,TESTDIV,EXAMNO,SEQ)

COMMENT ON TABLE ENTEXAM_COURSE_HIST_DAT IS '��ʥ���������ǡ���'
