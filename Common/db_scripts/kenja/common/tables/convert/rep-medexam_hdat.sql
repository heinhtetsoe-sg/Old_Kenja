-- kanji=漢字
-- $Id: 42d85350ea31e6eab76d3a539222cb8ea94ae4c5 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table MEDEXAM_HDAT_OLD

create table MEDEXAM_HDAT_OLD like MEDEXAM_HDAT

insert into MEDEXAM_HDAT_OLD select * from MEDEXAM_HDAT

drop table MEDEXAM_HDAT

create table MEDEXAM_HDAT ( \
    YEAR                    VARCHAR    (4) not null, \
    SCHREGNO                VARCHAR    (8) not null, \
    DATE                    DATE, \
    TOOTH_DATE              DATE, \
    REGISTERCD              VARCHAR (8), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

insert into MEDEXAM_HDAT \
  select \
    YEAR, \
    SCHREGNO, \
    DATE, \
    cast(NULL as DATE) as TOOTH_DATE, \
    REGISTERCD, \
    UPDATED \
  FROM \
    MEDEXAM_HDAT_OLD

alter table MEDEXAM_HDAT add constraint pk_medexam_hdat primary key (YEAR, SCHREGNO)
