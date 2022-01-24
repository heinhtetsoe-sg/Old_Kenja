drop table EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT

create table EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT( \
    EDBOARD_SCHOOLCD    varchar(12) not null, \
    YEAR                varchar(4)  not null, \
    FIXED_DATE          date        not null, \
    SCHOOLNAME          varchar(90), \
    TARGETS1            int, \
    EXAMINEE1           int, \
    PERCENT1            decimal(4, 1), \
    REMARK1             int, \
    URICSUGAR1          int, \
    ALBUMINURIA1        int, \
    URICBLEED1          int, \
    TARGETS2            int, \
    EXAMINEE2           int, \
    PERCENT2            decimal(4, 1), \
    TARGETS3            int, \
    EXAMINEE3           int, \
    NORMAL              int, \
    CAREFUL             int, \
    TREATMENT           int, \
    PERCENT3            decimal(4, 1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

COMMENT ON TABLE EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT IS 'EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT'

COMMENT ON EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT \
    (EDBOARD_SCHOOLCD IS '教育委員会学校コード', \
     YEAR IS '年度', \
     FIXED_DATE IS '確定日', \
     SCHOOLNAME IS '学校名', \
     TARGETS1 IS '1次検査対象者', \
     EXAMINEE1 IS '1次検査受検者', \
     PERCENT1 IS '1次検査受検率', \
     REMARK1 IS '1次検査有所見', \
     URICSUGAR1 IS '1次検査糖', \
     ALBUMINURIA1 IS '1次検査蛋白', \
     URICBLEED1 IS '1次検査潜血', \
     TARGETS2 IS '再検査対象者', \
     EXAMINEE2 IS '再検査実施数', \
     PERCENT2 IS '再検査実施率', \
     TARGETS3 IS '精密検査該当者', \
     EXAMINEE3 IS '精密検査受検者', \
     NORMAL IS '精密検査異常なし', \
     CAREFUL IS '精密検査要観察', \
     TREATMENT IS '精密検査要治療', \
     PERCENT3 IS '精密検査受診率', \
     REGISTERCD IS '最終更新者', \
     UPDATED IS '最終更新日時' \
     )

alter table EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT add constraint PK_EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT primary key (EDBOARD_SCHOOLCD, YEAR, FIXED_DATE)
