-- kanji=漢字
-- $Id: 137bba3c2a0bf40538f127ff4bf239ce73ca8e81 $

-- テスト時間割から講座の実施日を格納（成績入力で使用）
-- 作成日: 2005/05/16 20:09:00 - JST
-- 作成者: tamura

-- スクリプトの使用方法: db2 +c -f <thisfile>
-- 注意:このファイルは EUC/LFのみ でなければならない。

drop   table SCH_CHR_TEST

create table SCH_CHR_TEST ( \
    EXECUTEDATE date not null, \
    PERIODCD    varchar(1) not null, \
    CHAIRCD     varchar(7) not null, \
    TESTKINDCD  varchar(2), \
    TESTITEMCD  varchar(2), \
    EXECUTED    varchar(1), \
    YEAR        varchar(4), \
    SEMESTER    varchar(1), \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp, \
    primary key ( EXECUTEDATE, PERIODCD, CHAIRCD ) \
) in usr1dms index in idx1dms

