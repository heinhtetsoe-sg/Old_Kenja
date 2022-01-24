-- kanji=漢字
-- $Id: 197dc757172fdccbc6e315698e3cd564d47f8ea2 $

-- 入試設定マスタ(入試用名称マスタ)
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table ENTEXAM_SETTINGCDDESC_MST

create table ENTEXAM_SETTINGCDDESC_MST ( \
    SETTING_CD      varchar(4) not null, \
    CDMEMO          varchar(120), \
    MODIFY_FLG      varchar(1), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SETTINGCDDESC_MST add constraint PK_ENTEXAM_SETTINGCDDESC_MST primary key (SETTING_CD)
