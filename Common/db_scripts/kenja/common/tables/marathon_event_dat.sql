-- kanji=漢字
-- $Id: e5da81dd961bf9a519265a4adfd30bb43d81e06c $

-- 支部マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table MARATHON_EVENT_DAT

create table MARATHON_EVENT_DAT ( \
    YEAR                varchar(4)  not null, \
    SEQ                 varchar(2)  not null, \
    SCHREGNO            varchar(8)  not null, \
    TIME_H              smallint, \
    TIME_M              smallint, \
    TIME_S              smallint, \
    ATTEND_CD           varchar(2), \
    REMARK              varchar(60), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MARATHON_EVENT_DAT add constraint PK_MARATHON_EVENT_DAT primary key (YEAR, SEQ, SCHREGNO)
