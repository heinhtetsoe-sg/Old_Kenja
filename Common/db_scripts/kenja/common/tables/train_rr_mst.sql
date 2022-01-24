-- kanji=漢字
-- $Id: 29a848e164097e3a60c0421364ab34ee8d99cdb3 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table TRAIN_RR_MST

create table TRAIN_RR_MST \
(  \
    TRAIN_RR_CD       varchar(2)   not null, \
    TRAIN_RR_NAME     varchar(45)  not null, \
    REGISTERCD        varchar(8), \
    UPDATED           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table TRAIN_RR_MST add constraint PK_TRAIN_RR_MST \
primary key (TRAIN_RR_CD)
