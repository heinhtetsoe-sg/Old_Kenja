-- kanji=����
-- $Id: 5c23d406625cbad583be5b9fc95b42b277133d8b $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ENTEXAM_ASSESS_HDAT

create table ENTEXAM_ASSESS_HDAT ( \
    ENTEXAMYEAR             varchar(4)  not null, \
    ASSESSCD                varchar(1)  not null, \
    ASSESSMEMO              varchar(30), \
    ASSESSLEVELCNT          varchar(2), \
    MODIFY_FLG              varchar(1), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_ASSESS_HDAT add constraint PK_ENTEXM_ASSES_H \
      primary key (ENTEXAMYEAR, ASSESSCD)
