drop table EDBOARD_MEDEXAM_STAT_URINALYSIS2_FIXED_DAT

create table EDBOARD_MEDEXAM_STAT_URINALYSIS2_FIXED_DAT( \
    EDBOARD_SCHOOLCD    varchar(12) not null, \
    YEAR                varchar(4)  not null, \
    FIXED_DATE          date        not null, \
    SCHREGNO            varchar(8)  not null, \
    SCHOOLNAME          varchar(90), \
    GRADE               varchar(2), \
    NAME                varchar(120), \
    URI2_DIV_NAME       varchar(75), \
    URI2_REMARK         varchar(120), \
    URI2_SIDOU_DIV_NAME varchar(75), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

COMMENT ON TABLE EDBOARD_MEDEXAM_STAT_URINALYSIS2_FIXED_DAT IS 'EDBOARD_MEDEXAM_STAT_URINALYSIS2_FIXED_DAT'

COMMENT ON EDBOARD_MEDEXAM_STAT_URINALYSIS2_FIXED_DAT \
    (EDBOARD_SCHOOLCD IS '教育委員会学校コード', \
     YEAR IS '年度', \
     FIXED_DATE IS '確定日', \
     SCHOOLNAME IS '学校名', \
     SCHREGNO IS '学籍番号', \
     GRADE IS '学年の名称', \
     NAME IS '生徒氏名', \
     URI2_DIV_NAME IS '精密検査の区分名称', \
     URI2_REMARK IS '精密検査の所見、全角20文字まで', \
     URI2_SIDOU_DIV_NAME IS '精密検査の指導区分の名称', \
     REGISTERCD IS '最終更新者', \
     UPDATED IS '最終更新日時' \
     )

alter table EDBOARD_MEDEXAM_STAT_URINALYSIS2_FIXED_DAT add constraint PK_EDBOARD_MEDEXAM_STAT_URINALYSIS2_FIXED_DAT primary key (EDBOARD_SCHOOLCD, YEAR, FIXED_DATE, SCHREGNO)
