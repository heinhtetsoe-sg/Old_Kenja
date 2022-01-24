-- kanji=漢字
-- $Id: 0b36c991a2d92d5f495762cd232547205a6a0d99 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

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
