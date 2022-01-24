-- kanji=漢字
-- $Id: rec_graduate_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table REC_GRADUATE_DAT

create table REC_GRADUATE_DAT \
(  \
    SCHREGNO        varchar(8) not null, \
    GRD_FLG         varchar(1), \
    PAY_FLG         varchar(1), \
    REQUIRE_FLG     varchar(1), \
    GET_CREDITS     smallint, \
    SPECIAL_COUNT   smallint, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REC_GRADUATE_DAT  \
add constraint PK_REC_GRADUATE \
primary key  \
(SCHREGNO)
