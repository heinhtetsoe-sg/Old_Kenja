drop table EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT

create table EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT( \
    EDBOARD_SCHOOLCD    varchar(12) not null, \
    YEAR                varchar(4)  not null, \
    FIXED_DATE          date        not null, \
    SCHOOLNAME          varchar(90), \
    TARGET1             int, \
    EXAMINEE1           int, \
    PERCENT1            decimal(4,1), \
    HAVE_REMARK1        int, \
    REMARK1_CNT1        int, \
    REMARK1_CNT2        int, \
    REMARK1_CNT3        int, \
    REMARK1_CNT4        int, \
    HAVE_REMARK_PERCENT decimal(4,1), \
    TARGET2             int, \
    EXAMINEE2           int, \
    PERCENT2            decimal(4,1), \
    REMARK2_CNT1        int, \
    REMARK2_CNT2        int, \
    REMARK2_CNT3        int, \
    REMARK2_CNT4        int, \
    REMARK2_CNT5        int, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

COMMENT ON TABLE EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT IS 'EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT'

COMMENT ON EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT \
    (EDBOARD_SCHOOLCD IS '教育委員会学校コード', \
     YEAR IS '年度', \
     FIXED_DATE IS '確定日', \
     SCHOOLNAME IS '学校名', \
     TARGET1 IS '一次対象者', \
     EXAMINEE1 IS '一次受検者', \
     PERCENT1 IS '一次受検率％', \
     HAVE_REMARK1 IS '有所見者数', \
     REMARK1_CNT1 IS '要精検', \
     REMARK1_CNT2 IS '主治医管理', \
     REMARK1_CNT3 IS '放置可', \
     REMARK1_CNT4 IS 'その他', \
     HAVE_REMARK_PERCENT IS '有所見者率％', \
     TARGET2 IS '精密検査該当者', \
     EXAMINEE2 IS '精密検査受検者数', \
     PERCENT2 IS '精密検査受検率％', \
     REMARK2_CNT1 IS '異常なし', \
     REMARK2_CNT2 IS '要医療', \
     REMARK2_CNT3 IS '要観察', \
     REMARK2_CNT4 IS '放置可', \
     REMARK2_CNT5 IS 'その他', \
     REGISTERCD IS '最終更新者', \
     UPDATED IS '最終更新日時' \
     )

alter table EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT add constraint PK_EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT primary key (EDBOARD_SCHOOLCD, YEAR, FIXED_DATE)
