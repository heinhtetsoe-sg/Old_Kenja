-- kanji=漢字
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HEXAM_ENTREMARK_TRAINREF_SEQ_DAT
create table HEXAM_ENTREMARK_TRAINREF_SEQ_DAT( \
    YEAR        varchar(4) not null, \
    SCHREGNO    varchar(8) not null, \
    PATTERN_SEQ varchar(1) not null, \
    TRAIN_SEQ   varchar(3) not null, \
    REMARK      varchar(3110), \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HEXAM_ENTREMARK_TRAINREF_SEQ_DAT add constraint PK_HEXAM_ENTREMARK_TRAINREF_SEQ_DAT primary key(YEAR, SCHREGNO, PATTERN_SEQ, TRAIN_SEQ)
