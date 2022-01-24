-- kanji=漢字
-- $Id: 660268ea40edc4e5518cb3d1935719133430a16c $

-- 支部マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table MARATHON_EVENT_MST

create table MARATHON_EVENT_MST ( \
    YEAR                varchar(4)  not null, \
    SEQ                 varchar(2)  not null, \
    NUMBER_OF_TIMES     varchar(15), \
    EVENT_NAME          varchar(30), \
    EVENT_DATE          date, \
    MAN_METERS          DECIMAL(6, 3), \
    WOMEN_METERS        DECIMAL(6, 3), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MARATHON_EVENT_MST add constraint PK_MARATHON_EVENT_MST primary key (YEAR, SEQ)
