drop table EDBOARD_MEDEXAM_STAT_ECG1REMARK_FIXED_DAT

create table EDBOARD_MEDEXAM_STAT_ECG1REMARK_FIXED_DAT( \
    EDBOARD_SCHOOLCD    varchar(12) not null, \
    YEAR                varchar(4)  not null, \
    FIXED_DATE          date        not null, \
    SCHREGNO            varchar(8)  not null, \
    SCHOOLNAME          varchar(90), \
    GRADE               varchar(2), \
    NAME                varchar(120), \
    RESULT              varchar(75), \
    REMARK              varchar(120), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

COMMENT ON TABLE EDBOARD_MEDEXAM_STAT_ECG1REMARK_FIXED_DAT IS 'EDBOARD_MEDEXAM_STAT_ECG1REMARK_FIXED_DAT'

COMMENT ON EDBOARD_MEDEXAM_STAT_ECG1REMARK_FIXED_DAT \
    (EDBOARD_SCHOOLCD IS '教育委員会学校コード', \
     YEAR IS '年度', \
     FIXED_DATE IS '確定日', \
     SCHOOLNAME IS '学校名(SCHOOL_MST.SCHOOLNAME1)', \
     SCHREGNO IS '学籍番号', \
     GRADE IS '学年の名称(SCHREG_REGD_GDAT.GRADECD)', \
     NAME IS '生徒氏名', \
     RESULT IS '検査区分の名称', \
     REMARK IS '心電図所見の文言', \
     REGISTERCD IS '最終更新者', \
     UPDATED IS '最終更新日時' \
     )

alter table EDBOARD_MEDEXAM_STAT_ECG1REMARK_FIXED_DAT add constraint PK_EDBOARD_MEDEXAM_STAT_ECG1REMARK_FIXED_DAT primary key (EDBOARD_SCHOOLCD, YEAR, FIXED_DATE, SCHREGNO)
