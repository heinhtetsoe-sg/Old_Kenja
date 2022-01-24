-- kanji=漢字
-- $Id: dd9fe855ea47fc1a40d7a45928108e266961f9f9 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table TRAIN_LINE_MST

create table TRAIN_LINE_MST \
(  \
    RR_CD         varchar(2)  not null, \
    AREA_CD       varchar(1)  not null, \
    LINE_CD       varchar(5)  not null, \
    LINE_SORT     varchar(5), \
    LINE_NAME     varchar(192), \
    REGISTERCD    varchar(8), \
    UPDATED       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table TRAIN_LINE_MST add constraint PK_TRAIN_LINE_MST \
primary key (LINE_CD)

