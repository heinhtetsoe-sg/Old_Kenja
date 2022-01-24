-- kanji=漢字
-- $Id: 980394af330c87ae9c997834281deb63c33b0aa0 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table GRD_MEDEXAM_HDAT

create table GRD_MEDEXAM_HDAT ( \
    YEAR                    VARCHAR (4) not null, \
    SCHREGNO                VARCHAR (8) not null, \
    DATE                    DATE, \
    TOOTH_DATE              DATE, \
    REGISTERCD              VARCHAR (8), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table GRD_MEDEXAM_HDAT add constraint pk_grd_medex_hdat primary key (YEAR, SCHREGNO)
