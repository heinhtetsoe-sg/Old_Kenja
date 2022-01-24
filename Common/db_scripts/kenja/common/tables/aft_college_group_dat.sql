-- kanji=漢字
-- $Id: 97092f11a7d5c303ce22b634e73bb5b9f1e75e67 $

-- 支部マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table AFT_COLLEGE_GROUP_DAT

create table AFT_COLLEGE_GROUP_DAT ( \
    YEAR                varchar(4)   not null, \
    COLLEGE_GRP_CD      varchar(2)   not null, \
    SCHOOL_CD           varchar(8)   not null, \
    REGISTERCD          varchar(10),           \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_COLLEGE_GROUP_DAT add constraint PK_AFT_COL_GRP_D primary key (YEAR, COLLEGE_GRP_CD, SCHOOL_CD)
