-- kanji=漢字
-- $Id: 776c1c23e5afee5a8a5d4b39be22f4507493d0ef $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table BEHAVIOR_SEMES_DAT

create table BEHAVIOR_SEMES_DAT \
	(YEAR                 varchar(4) not null, \
	 SEMESTER             varchar(1) not null, \
	 SCHREGNO             varchar(8) not null, \
	 CODE                 varchar(2) not null, \
	 RECORD               varchar(1), \
	 REGISTERCD           varchar(8), \
	 UPDATED              timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table BEHAVIOR_SEMES_DAT \
add constraint PK_BEHAVIOR_SEMES \
primary key \
(YEAR, SEMESTER, SCHREGNO, CODE)
