-- kanji=漢字
-- $Id: 3c9e9ef1131b38dd4d5611c48b8c7d8f65750b6e $

-- 支部マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table SCH_PTRN_SUBCLASS_HDAT

create table SCH_PTRN_SUBCLASS_HDAT ( \
    YEAR            varchar(4)  not null, \
    SEQ             smallint    not null, \
    TITLE           varchar(45) not null, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCH_PTRN_SUBCLASS_HDAT add constraint PK_SCH_PTRN_SUBH primary key (YEAR, SEQ)
