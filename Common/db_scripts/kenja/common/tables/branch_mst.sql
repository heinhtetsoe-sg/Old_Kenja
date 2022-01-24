-- kanji=漢字
-- $Id: a489de2d5311470e6f1b8ea36ce46b667946a6c7 $

-- 支部マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table BRANCH_MST

create table BRANCH_MST ( \
    BRANCHCD            varchar(2)  not null, \
    BRANCHNAME          varchar(75), \
    ABBV                varchar(75), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table BRANCH_MST add constraint PK_BRANCH_MST primary key (BRANCHCD)
