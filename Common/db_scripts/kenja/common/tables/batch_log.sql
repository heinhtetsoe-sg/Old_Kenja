-- kanji=����
-- $Id: 0b36c991a2d92d5f495762cd232547205a6a0d99 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table BATCH_LOG

create table BATCH_LOG \
	(DATETIME             timestamp not null, \
	 PGID                 varchar(128) not null, \
	 CATEGORY             varchar(32), \
	 MESSAGE              varchar(512), \
	 REGISTERCD           varchar(8), \
	 UPDATED              timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table BATCH_LOG \
add constraint PK_BATCH_LOG \
primary key (DATETIME,PGID)
