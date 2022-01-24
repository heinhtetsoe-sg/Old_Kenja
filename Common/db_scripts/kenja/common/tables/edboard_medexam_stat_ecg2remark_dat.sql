drop table EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT

create table EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT( \
    EDBOARD_SCHOOLCD    varchar(12) not null, \
    YEAR                varchar(4)  not null, \
    SCHREGNO            varchar(8)  not null, \
    GRADE               varchar(2), \
    NAME                varchar(120), \
    DETAIL_RESULT       varchar(75), \
    DETAIL_REMARK       varchar(120), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

COMMENT ON TABLE EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT IS 'EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT'

COMMENT ON EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT \
    (EDBOARD_SCHOOLCD IS '教育委員会学校コード', \
     YEAR IS '年度', \
     SCHREGNO IS '学籍番号', \
     GRADE IS '学年の名称(SCHREG_REGD_GDAT.GRADECD)', \
     NAME IS '生徒氏名', \
     DETAIL_RESULT IS '精密検査区分の名称', \
     DETAIL_REMARK IS '精密検査の所見', \
     REGISTERCD IS '最終更新者', \
     UPDATED IS '最終更新日時' \
     )

alter table EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT add constraint PK_EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT primary key (EDBOARD_SCHOOLCD, YEAR, SCHREGNO)
