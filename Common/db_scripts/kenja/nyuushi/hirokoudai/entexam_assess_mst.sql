-- kanji=����
-- $Id: 7a2e4bd00ab77e183013887eaf5bae953f0eed1a $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ENTEXAM_ASSESS_MST

create table ENTEXAM_ASSESS_MST ( \
    ENTEXAMYEAR             varchar(4)  not null, \
    ASSESSCD                varchar(1)  not null, \
    ASSESSLEVEL             smallint    not null, \
    ASSESSMARK              varchar(6), \
    ASSESSLOW               decimal, \
    ASSESSHIGH              decimal, \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_ASSESS_MST add constraint PK_ENTEXM_ASSES_M \
      primary key (ENTEXAMYEAR, ASSESSCD, ASSESSLEVEL)
