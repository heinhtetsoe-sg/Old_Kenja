-- kanji=����
-- $Id: 3c9e9ef1131b38dd4d5611c48b8c7d8f65750b6e $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table SCH_PTRN_SUBCLASS_HDAT

create table SCH_PTRN_SUBCLASS_HDAT ( \
    YEAR            varchar(4)  not null, \
    SEQ             smallint    not null, \
    TITLE           varchar(45) not null, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCH_PTRN_SUBCLASS_HDAT add constraint PK_SCH_PTRN_SUBH primary key (YEAR, SEQ)
