-- $Id: subclass_hint_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $
-- 自動コマ入れ(KNJB0090)で参照するテーブル
-- 作成日: 2005/01/29 19:39:00 - JST
-- 作成者: tamura

-- スクリプトの使用方法: db2 +c -f <thisfile>

--/* 備考
-- *   ・カラム HINTDIV の値と意味
-- *       0 : 自動コマ入れを行なわない科目(校時コードは使わない)
-- *       1 : 校時に「独占」でコマ入れする科目。
-- *       2 : 校時に「優先」でコマ入れする科目。
-- */

drop   table SUBCLASS_HINT_DAT

create table SUBCLASS_HINT_DAT ( \
    YEAR        varchar(4) not null, \
    SUBCLASSCD  varchar(6) not null, \
    HINTDIV     smallint not null check (HINTDIV in (0, 1, 2)), \
    PERIODCD    varchar(1) not null, \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp, \
    primary key ( YEAR, SUBCLASSCD, HINTDIV, PERIODCD ) \
) in usr1dms index in idx1dms

