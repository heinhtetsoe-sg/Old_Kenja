-- $Id: 5444c5d96142e6e74621e99e9795ca1300a8963b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
-- ★保健室利用状況表用授業日数登録データ

drop table NURSEOFF_LESSON_DAT \

create table NURSEOFF_LESSON_DAT \
( \
    YEAR                  varchar(4) not null , \
    MONTH                 varchar(2) not null , \
    LESSON                varchar(2) , \
    REGISTERCD            varchar(10) , \
    UPDATED               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table NURSEOFF_LESSON_DAT add constraint PK_NURSEOFF_LES_D primary key (YEAR, MONTH)
