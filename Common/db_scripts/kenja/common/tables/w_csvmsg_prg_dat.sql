-- kanji=����
-- $Id: 5d2831c24c8ffb93e1867a3620179b28416e0e36 $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table W_CSVMSG_PRG_DAT

create table W_CSVMSG_PRG_DAT ( \
    PROGRAMID           varchar(10) not null, \
    MSGROW              integer not null, \
    MSGREMARK           varchar(120) \
) in usr1dms index in idx1dms

alter table W_CSVMSG_PRG_DAT add constraint PK_W_CSVMSG_PRG \
      primary key (PROGRAMID, MSGROW)
