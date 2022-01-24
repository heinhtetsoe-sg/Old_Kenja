-- kanji=漢字
-- $Id: c10e1e55240e78d43dcbc1d8d8ae730fe0d68fe7 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table GRD_MEDEXAM_HDAT_OLD

create table GRD_MEDEXAM_HDAT_OLD like GRD_MEDEXAM_HDAT

insert into GRD_MEDEXAM_HDAT_OLD select * from GRD_MEDEXAM_HDAT

drop table GRD_MEDEXAM_HDAT

create table GRD_MEDEXAM_HDAT ( \
    YEAR                    VARCHAR (4) not null, \
    SCHREGNO                VARCHAR (8) not null, \
    DATE                    DATE, \
    TOOTH_DATE              DATE, \
    REGISTERCD              VARCHAR (8), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

insert into GRD_MEDEXAM_HDAT \
  select \
    YEAR, \
    SCHREGNO, \
    DATE, \
    cast(NULL as DATE) as TOOTH_DATE, \
    REGISTERCD, \
    UPDATED \
  FROM \
    GRD_MEDEXAM_HDAT_OLD

alter table GRD_MEDEXAM_HDAT add constraint pk_grd_medex_hdat primary key (YEAR, SCHREGNO)
