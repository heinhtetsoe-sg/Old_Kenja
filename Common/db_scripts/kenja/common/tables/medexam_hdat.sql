-- kanji=漢字
-- $Id: 33be92c725222bf11587c576b2eddd9f1664e35e $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table MEDEXAM_HDAT

create table MEDEXAM_HDAT ( \
    YEAR                    VARCHAR    (4) not null, \
    SCHREGNO                VARCHAR    (8) not null, \
    DATE                    DATE, \
    TOOTH_DATE              DATE, \
    REGISTERCD              VARCHAR (8), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MEDEXAM_HDAT add constraint PK_MEDEXAM_HDAT primary key (YEAR, SCHREGNO)
