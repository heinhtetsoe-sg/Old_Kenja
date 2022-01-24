-- kanji=漢字
-- $Id: f8cb974e5a898455c7dec2ca6e3048862c56a5e4 $

-- 入試設定マスタ(入試用名称マスタ)
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table ENTEXAM_SETTING_MST

create table ENTEXAM_SETTING_MST ( \
    ENTEXAMYEAR     varchar(4) not null, \
    APPLICANTDIV    varchar(1) not null, \
    SETTING_CD      varchar(4) not null, \
    SEQ             varchar(4) not null, \
    NAME1           varchar(120), \
    NAME2           varchar(120), \
    NAME3           varchar(120), \
    ABBV1           varchar(60), \
    ABBV2           varchar(60), \
    ABBV3           varchar(60), \
    NAMESPARE1      varchar(60), \
    NAMESPARE2      varchar(60), \
    NAMESPARE3      varchar(60), \
    NAME1MEMO       varchar(120), \
    NAME2MEMO       varchar(120), \
    NAME3MEMO       varchar(120), \
    ABBV1MEMO       varchar(120), \
    ABBV2MEMO       varchar(120), \
    ABBV3MEMO       varchar(120), \
    NAMESPARE1MEMO  varchar(120), \
    NAMESPARE2MEMO  varchar(120), \
    NAMESPARE3MEMO  varchar(120), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SETTING_MST add constraint PK_ENTEXAM_SETTING_MST primary key (ENTEXAMYEAR, APPLICANTDIV, SETTING_CD, SEQ)
