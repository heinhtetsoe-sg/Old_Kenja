-- kanji=漢字
-- $Id: 5db0afaa1f487b6d2cae62850a677b7f52544660 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HEALTH_INVEST_OTHER_DAT

create table HEALTH_INVEST_OTHER_DAT \
        (SCHREGNO               varchar(8)      not null, \
         ALLERGY_MEDICINE       varchar(120), \
         ALLERGY_FOOD           varchar(120), \
         ALLERGY_OTHER          varchar(120), \
         BLOOD                  varchar(2), \
         RH                     varchar(1), \
         MEASLES_AGE            varchar(2), \
         G_MEASLES_AGE          varchar(2), \
         VARICELLA_AGE          varchar(2), \
         OTITIS_MEDIA_AGE       varchar(2), \
         TB_AGE                 varchar(2), \
         KAWASAKI_AGE           varchar(2), \
         INFECTION_AGE          varchar(2), \
         MUMPS_AGE              varchar(2), \
         HEART_DISEASE          varchar(45), \
         HEART_S_AGE            varchar(2), \
         HEART_SITUATION        varchar(2), \
         HEART_E_AGE            varchar(2), \
         KIDNEY_DISEASE         varchar(45), \
         KIDNEY_S_AGE           varchar(2), \
         KIDNEY_SITUATION       varchar(2), \
         KIDNEY_E_AGE           varchar(2), \
         ASTHMA_S_AGE           varchar(2), \
         ASTHMA_SITUATION       varchar(2), \
         ASTHMA_E_AGE           varchar(2), \
         CONVULSIONS_S_AGE      varchar(2), \
         CONVULSIONS_SITUATION  varchar(2), \
         CONVULSIONS_E_AGE      varchar(2), \
         OTHER_DISEASE          varchar(120), \
         TUBERCULIN             varchar(2), \
         TUBERCULIN_YEAR        varchar(4), \
         TUBERCULIN_MONTH       varchar(2), \
         TUBERCULIN_JUDGE       varchar(1), \
         BCG                    varchar(2), \
         BCG_YEAR               varchar(4), \
         BCG_MONTH              varchar(2), \
         POLIO                  varchar(2), \
         POLIO_YEAR             varchar(4), \
         POLIO_MONTH            varchar(2), \
         G_MEASLES              varchar(2), \
         G_MEASLES_YEAR         varchar(4), \
         G_MEASLES_MONTH        varchar(2), \
         VARICELLA              varchar(2), \
         VARICELLA_YEAR         varchar(4), \
         VARICELLA_MONTH        varchar(2), \
         MUMPS                  varchar(2), \
         MUMPS_YEAR             varchar(4), \
         MUMPS_MONTH            varchar(2), \
         ENCEPHALITIS           varchar(2), \
         ENCEPHALITIS_YEAR1     varchar(4), \
         ENCEPHALITIS_MONTH1    varchar(2), \
         ENCEPHALITIS_YEAR2     varchar(4), \
         ENCEPHALITIS_MONTH2    varchar(2), \
         ENCEPHALITIS_YEAR3     varchar(4), \
         ENCEPHALITIS_MONTH3    varchar(2), \
         ENCEPHALITIS_YEAR4     varchar(4), \
         ENCEPHALITIS_MONTH4    varchar(2), \
         ENCEPHALITIS_YEAR5     varchar(4), \
         ENCEPHALITIS_MONTH5    varchar(2), \
         MIXED                  varchar(2), \
         MIXED_YEAR1            varchar(4), \
         MIXED_MONTH1           varchar(2), \
         MIXED_YEAR2            varchar(4), \
         MIXED_MONTH2           varchar(2), \
         MIXED_YEAR3            varchar(4), \
         MIXED_MONTH3           varchar(2), \
         MIXED_YEAR4            varchar(4), \
         MIXED_MONTH4           varchar(2), \
         MIXED_YEAR5            varchar(4), \
         MIXED_MONTH5           varchar(2), \
         MEASLES                varchar(1), \
         MEASLES_TIMES          varchar(2), \
         MEASLES_YEAR1          varchar(4), \
         MEASLES_MONTH1         varchar(2), \
         MEASLES_YEAR2          varchar(4), \
         MEASLES_MONTH2         varchar(2), \
         MEASLES_YEAR3          varchar(4), \
         MEASLES_MONTH3         varchar(2), \
         VACCINE                varchar(2), \
         LOT_NO                 varchar(10), \
         CONFIRMATION           varchar(2), \
         A_MEASLES              varchar(1), \
         A_MEASLES_AGE          varchar(2), \
         A_CONFIRMATION         varchar(2), \
         ANTIBODY               varchar(1), \
         ANTIBODY_YEAR          varchar(4), \
         ANTIBODY_MONTH         varchar(2), \
         ANTIBODY_POSITIVE      varchar(1), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table HEALTH_INVEST_OTHER_DAT add constraint pk_hea_invo_dat primary key \
        (SCHREGNO)
