-- kanji=漢字
-- $Id: 3831c5c8aaaf7595bcc70418c654ee9c983b0ae2 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ENTEXAM_DEVIATION_LEVEL_MST
create table ENTEXAM_DEVIATION_LEVEL_MST \
(  \
    ENTEXAMYEAR         varchar(4)  not null, \
    DEV_CD              varchar(3)  not null, \
    DEV_MARK            varchar(6)          , \
    DEV_LOW             decimal(4, 1)       , \
    DEV_HIGH            decimal(4, 1)       , \
    REGISTERCD          varchar(10)         , \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_DEVIATION_LEVEL_MST add constraint PK_ENTEXAM_DEV_LEVEL_M \
primary key (ENTEXAMYEAR, DEV_CD)
