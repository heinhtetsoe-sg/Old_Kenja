-- kanji=����
-- $Id: a259e8c2cb4f714001e72a1ab98b93b771e4a53a $
-- ���Һ��ҥǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop   table TMP_SCHREG_REGD_DETAIL
create table TMP_SCHREG_REGD_DETAIL \
      (SCHREGNO          varchar(8)      not null, \
       YEAR              varchar(4)      not null, \
       SEMESTER          varchar(1)      not null, \
       COUNTFLG          varchar(1), \
       REGISTERCD        varchar(8), \
       UPDATED           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

insert into TMP_SCHREG_REGD_DETAIL \
  select \
        SCHREGNO, \
        YEAR, \
        SEMESTER, \
    	'1' AS COUNTFLG, \
        REGISTERCD, \
        UPDATED \
  from SCHREG_REGD_DETAIL

drop table SCHREG_REGD_DETAIL_OLD

rename table     SCHREG_REGD_DETAIL to SCHREG_REGD_DETAIL_OLD

rename table TMP_SCHREG_REGD_DETAIL to SCHREG_REGD_DETAIL

alter table SCHREG_REGD_DETAIL add constraint PK_SCHREG_REGD_DETAIL primary key (SCHREGNO,YEAR,SEMESTER)
