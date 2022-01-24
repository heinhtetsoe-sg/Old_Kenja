-- kanji=漢字
-- $Id: db27fa8510624a180d38c8df64710f40af77a7ea $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

--年度末精算実行確認テーブル

drop table LEVY_CLOSE_GRADE_DAT

create table LEVY_CLOSE_GRADE_DAT( \
        SCHOOLCD        varchar(12) not null, \
        SCHOOL_KIND     varchar(2)  not null, \
        YEAR            varchar(4)  not null, \
        GRADE           varchar(2)  not null, \
        CLOSE_FLG       varchar(1)  , \
        REGISTERCD      varchar(10) , \
        UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_CLOSE_GRADE_DAT \
add constraint PK_LEVY_CLOSE_G_D \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, GRADE)
