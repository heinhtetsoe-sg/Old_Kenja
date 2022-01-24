drop table EDBOARD_MEDEXAM_STAT_TB2_DAT

create table EDBOARD_MEDEXAM_STAT_TB2_DAT( \
    EDBOARD_SCHOOLCD    varchar(12) not null, \
    YEAR                varchar(4)  not null, \
    SCHREGNO            varchar(8)  not null, \
    GRADE               varchar(2), \
    NAME                varchar(120), \
    JUDGEMENT           varchar(75), \
    JUDGEMENT_DATE      date, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

COMMENT ON TABLE EDBOARD_MEDEXAM_STAT_TB2_DAT IS 'EDBOARD_MEDEXAM_STAT_TB2_DAT'

COMMENT ON EDBOARD_MEDEXAM_STAT_TB2_DAT \
    (EDBOARD_SCHOOLCD IS '教育委員会学校コード', \
     YEAR IS '年度', \
     SCHREGNO IS '学籍番号', \
     GRADE IS '学年の名称(SCHREG_REGD_GDAT.GRADECD)', \
     NAME IS '生徒氏名', \
     JUDGEMENT IS '再検査判定区分の名称', \
     JUDGEMENT_DATE IS '再検査受検日（形式：YYYY/MM/DD）', \
     REGISTERCD IS '最終更新者', \
     UPDATED IS '最終更新日時' \
     )

alter table EDBOARD_MEDEXAM_STAT_TB2_DAT add constraint PK_EDBOARD_MEDEXAM_STAT_TB2_DAT primary key (EDBOARD_SCHOOLCD, YEAR, SCHREGNO)
