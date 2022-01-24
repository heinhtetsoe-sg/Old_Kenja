-- kanji=漢字
-- $Id: 9f5b0de354754f5d06933c1de73e4d89e1ba1796 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

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
