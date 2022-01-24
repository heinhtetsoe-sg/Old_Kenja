-- kanji=漢字
-- $Id: 0503937677a9942ef4d523bf1193aec45bb6f55f $

-- 支部マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table TOEFL_MST

create table TOEFL_MST ( \
    YEAR                           varchar(4)  not null, \
    BASE_SCORE                     integer     , \
    RANGE_F                        integer     , \
    RANGE_T                        integer     , \
    REGISTERCD                     varchar(10) , \
    UPDATED                        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table TOEFL_MST add constraint PK_TOEFL_M primary key (YEAR)
