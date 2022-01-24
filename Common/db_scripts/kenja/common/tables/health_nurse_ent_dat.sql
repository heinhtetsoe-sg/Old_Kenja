-- kanji=漢字
-- $Id: 6f306eb3c0c04223d239a571face9eeefe20a514 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HEALTH_NURSE_ENT_DAT

create table HEALTH_NURSE_ENT_DAT \
        (SCHREGNO               varchar(8)      not null, \
         INSURED_NAME           varchar(60), \
         INSURED_MARK           varchar(60), \
         INSURED_NO             varchar(20), \
         INSURANCE_NAME         varchar(60), \
         INSURANCE_NO           varchar(20), \
         VALID_DATE             date, \
         AUTHORIZE_DATE         date, \
         RELATIONSHIP           varchar(2), \
         REMARK                 varchar(1200), \
         ATTENTION              varchar(90), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table HEALTH_NURSE_ENT_DAT add constraint pk_hea_nure_dat primary key \
        (SCHREGNO)
