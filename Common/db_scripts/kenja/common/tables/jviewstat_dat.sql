-- kanji=漢字
-- $Id: 28bba3dc2241b51fb667407535fd1d439660928b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table JVIEWSTAT_DAT

create table JVIEWSTAT_DAT  \
(  \
        "YEAR"          varchar(4)  not null, \
        "SEMESTER"      varchar(1)  not null, \
        "SCHREGNO"      varchar(8)  not null, \
        "VIEWCD"        varchar(4)  not null, \
        "STATUS"        varchar(1),  \
        "REGISTERCD"    varchar(8),  \
        "UPDATED"       timestamp default current timestamp  \
) in usr1dms index in idx1dms


alter table JVIEWSTAT_DAT  \
add constraint pk_jviewstat_dat  \
primary key  \
( \
YEAR, \
SEMESTER, \
SCHREGNO, \
VIEWCD \
)

