-- kanji=漢字
-- $Id: 15a5747c8f39739fe430378426e82e473a8dc6b2 $

-- 支部マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table ABSENCE_PATTERN_DAT

create table ABSENCE_PATTERN_DAT ( \
    YEAR                varchar(4)  not null, \
    PATTERNCD           varchar(2)  not null, \
    ASSESSLEVEL         smallint    not null, \
    ASSESSMARK          varchar(3), \
    RATE                decimal (5,2), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ABSENCE_PATTERN_DAT add constraint PK_ABSENCE_PATT_D primary key (YEAR, PATTERNCD, ASSESSLEVEL)
