-- kanji=����
-- $Id: a381617a74c495d9078f1ca3f7790e39f8b0b5e8 $
-- ���Һ��ҥǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--


drop table SCHREG_REGD_DETAIL

create table SCHREG_REGD_DETAIL \
      (SCHREGNO          varchar(8)      not null, \
       YEAR              varchar(4)      not null, \
       SEMESTER          varchar(1)      not null, \
       COUNTFLG          varchar(1), \
       REGISTERCD        varchar(8), \
       UPDATED           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_REGD_DETAIL add constraint PK_SRG_RGD_DTL primary key (SCHREGNO, YEAR, SEMESTER)
