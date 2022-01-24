-- kanji=漢字
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HEXAM_ENTREMARK_SEQ_DAT

create table HEXAM_ENTREMARK_SEQ_DAT ( \
    YEAR                       varchar(4) not null, \
    SCHREGNO                   varchar(8) not null, \
    PATTERN_SEQ                varchar(1) not null, \
    ANNUAL                     varchar(2) not null, \
    ATTENDREC_REMARK           varchar(238), \
    SPECIALACTREC              varchar(700), \
    TRAIN_REF                  varchar(1248), \
    TRAIN_REF1                 varchar(1740), \
    TRAIN_REF2                 varchar(520), \
    TRAIN_REF3                 varchar(800), \
    TOTALSTUDYACT              varchar(746), \
    TOTALSTUDYVAL              varchar(746), \
    CALSSACT                   varchar(300), \
    STUDENTACT                 varchar(218), \
    CLUBACT                    varchar(225), \
    SCHOOLEVENT                varchar(218), \
    TOTALSTUDYACT_SLASH_FLG    varchar(1), \
    TOTALSTUDYVAL_SLASH_FLG    varchar(1), \
    ATTENDREC_REMARK_SLASH_FLG varchar(1), \
    REGISTERCD                 varchar(10), \
    UPDATED                    timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HEXAM_ENTREMARK_SEQ_DAT add constraint PK_HEXAM_ENTREMARK_SEQ_DAT primary key (YEAR, SCHREGNO, PATTERN_SEQ)
