-- kanji=����
-- $Id: 4da738df909683a325c096e607b7e08e55ad1947 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table DETAIL_TABLE_MEMO_DAT

create table DETAIL_TABLE_MEMO_DAT \
      (Z010_NAME1     varchar(60) not null, \
       TABLE_NAME     varchar(100) not null, \
       SEQ1           varchar(10) not null, \
       SEQ2           varchar(10) not null, \
       SEQ1_MEAN      varchar(300), \
       SEQ2_MEAN      varchar(300), \
       MEMO           varchar(1500) \
      ) in usr1dms index in idx1dms

alter table DETAIL_TABLE_MEMO_DAT add constraint PK_DETAIL_MEMO \
      primary key (Z010_NAME1, TABLE_NAME, SEQ1, SEQ2)
