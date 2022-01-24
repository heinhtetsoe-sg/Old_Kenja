-- kanji=����
-- $Id: 9f5b0de354754f5d06933c1de73e4d89e1ba1796 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table BATCH_LOCK

create table BATCH_LOCK \
	(PGID                 varchar(128) not null, \
	 STATUS               varchar(8) not null, \
	 REGISTERCD           varchar(8), \
	 UPDATED              timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table BATCH_LOCK \
add constraint PK_BATCH_LOCK \
primary key (PGID)
