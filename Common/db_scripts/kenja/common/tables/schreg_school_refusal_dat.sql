-- kanji=漢字
-- $Id: efb8b515d192530e1f4dfc2941c678c339d57a22 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SCHREG_SCHOOL_REFUSAL_DAT

create table SCHREG_SCHOOL_REFUSAL_DAT \
(  \
    YEAR                VARCHAR(4)  not null, \
    SCHREGNO            VARCHAR(8)  not null, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_SCHOOL_REFUSAL_DAT add constraint PK_SCHREG_SCHOOL_REFUSAL_D \
primary key (YEAR, SCHREGNO)
