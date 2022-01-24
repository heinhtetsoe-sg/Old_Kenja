-- kanji=漢字
-- $Id: 7dd1545172e009ac95530be916fb065294f281e9 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table HREPORTREMARK_DETAIL_DAT

create table HREPORTREMARK_DETAIL_DAT \
    (YEAR                 varchar(4) not null, \
     SEMESTER             varchar(1) not null, \
     SCHREGNO             varchar(8) not null, \
     DIV                  varchar(2) not null, \
     CODE                 varchar(2) not null, \
     REMARK1              varchar(1500), \
     REMARK2              varchar(1500), \
     REMARK3              varchar(1500), \
     REMARK4              varchar(3000), \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HREPORTREMARK_DETAIL_DAT \
add constraint PK_HREP_DETAIL_DAT \
primary key \
(YEAR, SEMESTER, SCHREGNO, DIV, CODE)
