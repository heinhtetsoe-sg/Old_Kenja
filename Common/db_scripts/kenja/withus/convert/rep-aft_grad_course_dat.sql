-- kanji=漢字
-- $Id: rep-aft_grad_course_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback


drop table AFT_GRAD_COURSE_DAT_OLD

create table AFT_GRAD_COURSE_DAT_OLD like AFT_GRAD_COURSE_DAT

insert into AFT_GRAD_COURSE_DAT_OLD select * from AFT_GRAD_COURSE_DAT

drop table AFT_GRAD_COURSE_DAT

create table AFT_GRAD_COURSE_DAT \
( \
    YEAR                char(4) not null, \
    SEQ                 int not null, \
    SCHREGNO            char(8), \
    STAT_KIND           char(1), \
    SENKOU_KIND         char(1), \
    STAT_CD             char(11), \
    STAT_NAME           varchar(120), \
    BUNAME              varchar(120), \
    KANAME              varchar(120), \
    SCHOOL_SORT         char(2), \
    ZIPCD               varchar(8), \
    ADDR1               varchar(90), \
    ADDR2               varchar(90), \
    TELNO               varchar(16), \
    HOWTOEXAM           char(2), \
    HOWTOEXAM_REMARK    varchar(120), \
    HAND_DATE           date, \
    DECISION            char(1), \
    PLANSTAT            char(1), \
    PRINT_DATE          date, \
    SENKOU_NO           int, \
    TOROKU_DATE         date, \
    JUKEN_HOWTO         char(2), \
    RECOMMEND           varchar(120), \
    ATTEND              smallint, \
    AVG                 decimal(2,1), \
    TEST                decimal(3,1), \
    SEISEKI             decimal(4,1), \
    SENKOU_KAI          char(2), \
    SENKOU_FIN          char(1), \
    REMARK              varchar(60), \
    STAT_DATE1          date, \
    STAT_STIME          time, \
    STAT_ETIME          time, \
    AREA_NAME           varchar(30), \
    STAT_DATE2          date, \
    CONTENTEXAM         varchar(120), \
    REASONEXAM          varchar(242), \
    THINKEXAM           varchar(486), \
    JOB_DATE1           date, \
    JOB_STIME           time, \
    JOB_ETIME           time, \
    SHUSHOKU_ADDR       varchar(120), \
    JOB_REMARK          varchar(120), \
    JOB_CONTENT         varchar(242), \
    JOB_THINK           varchar(486), \
    JOBEX_DATE1         date, \
    JOBEX_STIME         time, \
    JOBEX_ETIME         time, \
    JOBEX_REMARK        varchar(120), \
    JOBEX_CONTENT       varchar(242), \
    JOBEX_THINK         varchar(486), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_GRAD_COURSE_DAT add constraint PK_AFT_GRAD_COURSE primary key (YEAR,SEQ)

insert into AFT_GRAD_COURSE_DAT \
    select \
        YEAR, \
        SEQ, \
        SCHREGNO, \
        STAT_KIND, \
        SENKOU_KIND, \
        STAT_CD, \
        STAT_NAME, \
        BUNAME, \
        cast(NULL as varchar(120)) as KANAME, \
        SCHOOL_SORT, \
        cast(NULL as varchar(2)) as ZIPCD, \
        cast(NULL as varchar(2)) as ADDR1, \
        cast(NULL as varchar(2)) as ADDR2, \
        TELNO, \
        HOWTOEXAM, \
        HOWTOEXAM_REMARK, \
        HAND_DATE, \
        DECISION, \
        PLANSTAT, \
        PRINT_DATE, \
        SENKOU_NO, \
        TOROKU_DATE, \
        JUKEN_HOWTO, \
        RECOMMEND, \
        ATTEND, \
        AVG, \
        TEST, \
        SEISEKI, \
        SENKOU_KAI, \
        SENKOU_FIN, \
        REMARK, \
        STAT_DATE1, \
        STAT_STIME, \
        STAT_ETIME, \
        AREA_NAME, \
        STAT_DATE2, \
        CONTENTEXAM, \
        REASONEXAM, \
        THINKEXAM, \
        JOB_DATE1, \
        JOB_STIME, \
        JOB_ETIME, \
        SHUSHOKU_ADDR, \
        JOB_REMARK, \
        JOB_CONTENT, \
        JOB_THINK, \
        JOBEX_DATE1, \
        JOBEX_STIME, \
        JOBEX_ETIME, \
        JOBEX_REMARK, \
        JOBEX_CONTENT, \
        JOBEX_THINK, \
        REGISTERCD, \
        UPDATED \
    from \
        AFT_GRAD_COURSE_DAT_OLD
