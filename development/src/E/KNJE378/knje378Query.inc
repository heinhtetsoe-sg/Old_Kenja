<?php

require_once('for_php7.php');

class knje378Query extends Query {

    //テスト種別取得
    function getDataDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    function getProName($model)
    {
        $query  = " SELECT ";
        $query .= "     PROFICIENCYCD AS VALUE, ";
        $query .= "     PROFICIENCYCD || ':' || PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCYDIV = '".$model->proficiencydiv."' ";
        
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //テスト名称取得(直接)
    function getProNameDirect($model)
    {
        $query .= " SELECT ";
        $query .= "     PROFICIENCYNAME1 ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCYDIV = '".$model->proficiencydiv."' ";
        $query .= "     and PROFICIENCYCD = '".$model->proficiencycd."' ";
        
        return $query;
    }
    
    //CSVメインデータ
    function getMainData($model) {
        $query = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     HOPE_D.ENTRYDATE, ";
        $query .= "     MAX(HOPE_D.SEQ) AS SEQ, ";
        $query .= "     HOPE_D.SCHREGNO, ";
        $query .= "     HOPE_D.COURSE_KIND ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DAT HOPE_D ";
        $query .= "     LEFT JOIN COURSE_HOPE_DETAIL_DAT HOPE_DETAIL ON HOPE_DETAIL.ENTRYDATE = HOPE_D.ENTRYDATE ";
        $query .= "          AND HOPE_DETAIL.SEQ = HOPE_D.SEQ ";
        $query .= "          AND HOPE_DETAIL.SCHREGNO = HOPE_D.SCHREGNO ";
        $query .= "          AND HOPE_DETAIL.HOPE_NUM = 3 ";
        $query .= " WHERE ";
        $query .= "     HOPE_D.COURSE_KIND = '1' ";
        $query .= "     AND HOPE_D.YEAR = '" . $model->year . "' ";
        $query .= "     AND (EXISTS( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     NAME_MST E002 ";
        $query .= "                 WHERE ";
        $query .= "                     E002.NAMECD1 = 'E002' ";
        $query .= "                     AND NAMESPARE1 = '1' ";
        $query .= "                     AND (HOPE_D.HOWTOEXAM1 = int(E002.NAMECD2) ";
        $query .= "                          OR ";
        $query .= "                          HOPE_D.HOWTOEXAM2 = int(E002.NAMECD2)) ";
        $query .= "                ) ";
        $query .= "          OR ";
        $query .= "          EXISTS( ";
        $query .= "                  SELECT ";
        $query .= "                      'X' ";
        $query .= "                  FROM ";
        $query .= "                      NAME_MST E002 ";
        $query .= "                  WHERE ";
        $query .= "                      E002.NAMECD1 = 'E002' ";
        $query .= "                      AND NAMESPARE1 = '1' ";
        $query .= "                      AND HOPE_DETAIL.HOWTOEXAM = int(E002.NAMECD2)) ";
        $query .= "                ) ";
        $query .= " GROUP BY ";
        $query .= "     HOPE_D.ENTRYDATE, ";
        $query .= "     HOPE_D.SCHREGNO, ";
        $query .= "     HOPE_D.COURSE_KIND ";
        $query .= " ) ";
        $query .= " select ";
        $query .= "      TBL1.SCHREGNO, ";
        $query .= "      TBL2.HR_CLASS, ";
        $query .= "      TBL2.ATTENDNO, ";
        $query .= "      TBL3.NAME, ";
        $query .= "      TBL4.ABBV1, ";
        $query .= "      TBL5.RANK, ";
        $query .= "      '' as SUM, ";
        $query .= "      TBL6.DEPARTMENTNAME as DEPARTMENTNAME1, ";
        $query .= "      TBL7.DEPARTMENTNAME as DEPARTMENTNAME2, ";
        $query .= "      TBL8.DEPARTMENTNAME as DEPARTMENTNAME3, ";
        $query .= "      TBL9.VALUATION, ";
        $query .= "      TBL9.VALUATION_AVG, ";
        $query .= "      '' as SYUSSEKI_SUM, ";
        $query .= "      TBL10.REQUIREPRESENT - TBL10.SICK - TBL10.ACCIDENTNOTICE - TBL10.NOACCIDENTNOTICE as SYUSSEKI1, ";
        $query .= "      TBL11.REQUIREPRESENT - TBL11.SICK - TBL11.ACCIDENTNOTICE - TBL11.NOACCIDENTNOTICE as SYUSSEKI2, ";
        $query .= "      TBL12.REQUIREPRESENT - TBL12.SICK - TBL12.ACCIDENTNOTICE - TBL12.NOACCIDENTNOTICE as SYUSSEKI3, ";
        $query .= "      TBL10.SICK + TBL10.ACCIDENTNOTICE + TBL10.NOACCIDENTNOTICE as SYUKKETU1, ";
        $query .= "      TBL11.SICK + TBL11.ACCIDENTNOTICE + TBL11.NOACCIDENTNOTICE as SYUKKETU2, ";
        $query .= "      TBL12.SICK + TBL12.ACCIDENTNOTICE + TBL12.NOACCIDENTNOTICE as SYUKKETU3, ";
        $query .= "      '' as TOKUBETU1, ";
        $query .= "      '' as TOKUBETU2, ";
        $query .= "      '' as TOKUBETU3, ";
        $query .= "      TBL13.COMMITTEENAME as COMMITTEENAME1, ";
        $query .= "      TBL14.COMMITTEENAME as COMMITTEENAME2, ";
        $query .= "      TBL15.COMMITTEENAME as COMMITTEENAME3, ";
        $query .= "      TBL16.CLUBNAME as CLUBNAME1, ";
        $query .= "      TBL17.CLUBNAME as CLUBNAME2, ";
        $query .= "      TBL18.CLUBNAME as CLUBNAME3, ";
        $query .= "      TBL5.SCORE ";
        $query .= " from  ";
        $query .= " (SELECT ";
        $query .= "      HOPE_D.SCHREGNO, ";
        $query .= "      HOPE_D.SCHOOL_CD1, ";
        $query .= "      HOPE_D.FACULTYCD1, ";
        $query .= "      HOPE_D.DEPARTMENTCD1, ";
        $query .= "      HOPE_D.SCHOOL_CD2, ";
        $query .= "      HOPE_D.FACULTYCD2, ";
        $query .= "      HOPE_D.DEPARTMENTCD2, ";
        $query .= "      HOPE_DETAIL.SCHOOL_CD, ";
        $query .= "      HOPE_DETAIL.FACULTYCD, ";
        $query .= "      HOPE_DETAIL.DEPARTMENTCD, ";
        $query .= "      HOPE_D.YEAR ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= "     LEFT JOIN COURSE_HOPE_DAT HOPE_D ON MAIN_T.ENTRYDATE = HOPE_D.ENTRYDATE ";
        $query .= "          AND MAIN_T.SEQ = HOPE_D.SEQ ";
        $query .= "          AND MAIN_T.SCHREGNO = HOPE_D.SCHREGNO ";
        $query .= "          AND MAIN_T.COURSE_KIND = HOPE_D.COURSE_KIND ";
        $query .= "     LEFT JOIN COURSE_HOPE_DETAIL_DAT HOPE_DETAIL ON HOPE_DETAIL.ENTRYDATE = HOPE_D.ENTRYDATE ";
        $query .= "          AND HOPE_DETAIL.SEQ = HOPE_D.SEQ ";
        $query .= "          AND HOPE_DETAIL.SCHREGNO = HOPE_D.SCHREGNO ";
        $query .= "          AND HOPE_DETAIL.HOPE_NUM = 3 ) as TBL1 ";
        $query .= " left join ";
        $query .= "     SCHREG_REGD_DAT TBL2 ";
        $query .= "     on TBL1.SCHREGNO = TBL2.SCHREGNO ";
        $query .= "     and TBL1.YEAR = TBL2.YEAR ";
        $query .= "     and TBL2.SEMESTER='1' ";
        $query .= " left join ";
        $query .= "     SCHREG_BASE_MST TBL3 ";
        $query .= "     on TBL1.SCHREGNO = TBL3.SCHREGNO ";
        $query .= " left join ";
        $query .= "     DB2INST1.NAME_MST TBL4 ";
        $query .= "     on TBL3.SEX = TBL4.NAMECD2 ";
        $query .= "     and TBL4.NAMECD1 = 'Z002' ";
        $query .= " inner join ";
        $query .= "     PROFICIENCY_RANK_DAT TBL5 ";
        $query .= "     on TBL5.RANK_DATA_DIV = '01' ";
        $query .= "     and TBL5.PROFICIENCY_SUBCLASS_CD = '999999' ";
        $query .= "     and TBL5.RANK_DIV = '01' ";
        $query .= "     and TBL1.SCHREGNO = TBL5.SCHREGNO ";
        $query .= "     and TBL1.YEAR = TBL5.YEAR ";
        $query .= "     and TBL5.PROFICIENCYDIV = '{$model->proficiencydiv}' ";
        $query .= "     and TBL5.PROFICIENCYCD = '{$model->proficiencycd}' ";
        $query .= " left join  ";
        $query .= "     (select ";
        $query .= "         L2.SCHOOL_CD, ";
        $query .= "         L2.FACULTYCD, ";
        $query .= "         L2.DEPARTMENTCD, ";
        $query .= "         DEPARTMENTNAME ";
        $query .= "     from ";
        $query .= "         COLLEGE_MST T1 ";
        $query .= "     inner join ";
        $query .= "         COLLEGE_FACULTY_MST L1 ";
        $query .= "         on L1.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "     inner join ";
        $query .= "         COLLEGE_DEPARTMENT_MST L2 ";
        $query .= "         on L2.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "         and L2.FACULTYCD = L1.FACULTYCD) as TBL6 ";
        $query .= "     on TBL1.SCHOOL_CD1 = TBL6.SCHOOL_CD ";
        $query .= "     and TBL1.FACULTYCD1 = TBL6.FACULTYCD ";
        $query .= "     and TBL1.DEPARTMENTCD1 = TBL6.DEPARTMENTCD ";
        $query .= " left join  ";
        $query .= "     (select ";
        $query .= "         L2.SCHOOL_CD, ";
        $query .= "         L2.FACULTYCD, ";
        $query .= "         L2.DEPARTMENTCD, ";
        $query .= "         DEPARTMENTNAME ";
        $query .= "     from ";
        $query .= "         COLLEGE_MST T1 ";
        $query .= "     inner join ";
        $query .= "         COLLEGE_FACULTY_MST L1 ";
        $query .= "         on L1.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "     inner join ";
        $query .= "         COLLEGE_DEPARTMENT_MST L2 ";
        $query .= "         on L2.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "         and L2.FACULTYCD = L1.FACULTYCD) as TBL7 ";
        $query .= "     on TBL1.SCHOOL_CD2 = TBL7.SCHOOL_CD ";
        $query .= "     and TBL1.FACULTYCD2 = TBL7.FACULTYCD ";
        $query .= "     and TBL1.DEPARTMENTCD2 = TBL7.DEPARTMENTCD ";
        $query .= " left join  ";
        $query .= "     (select ";
        $query .= "         L2.SCHOOL_CD, ";
        $query .= "         L2.FACULTYCD, ";
        $query .= "         L2.DEPARTMENTCD, ";
        $query .= "         DEPARTMENTNAME ";
        $query .= "     from ";
        $query .= "         COLLEGE_MST T1 ";
        $query .= "     inner join ";
        $query .= "         COLLEGE_FACULTY_MST L1 ";
        $query .= "         on L1.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "     inner join ";
        $query .= "         COLLEGE_DEPARTMENT_MST L2 ";
        $query .= "         on L2.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "         and L2.FACULTYCD = L1.FACULTYCD) as TBL8 ";
        $query .= "     on TBL1.SCHOOL_CD = TBL8.SCHOOL_CD ";
        $query .= "     and TBL1.FACULTYCD = TBL8.FACULTYCD ";
        $query .= "     and TBL1.DEPARTMENTCD = TBL8.DEPARTMENTCD ";
        $query .= "      ";
        $query .= " left join ";
        $query .= "     (select ";
        $query .= "         SCHREG_STUDYREC_DAT.SCHREGNO, ";
        $query .= "         sum(SCHREG_STUDYREC_DAT.VALUATION) * 100 as VALUATION, ";
        $query .= "         sum(SCHREG_STUDYREC_DAT.VALUATION) as VALUATION_AVG ";
        $query .= "     from  ";
        $query .= "         SCHREG_STUDYREC_DAT ";
        $query .= "     where ";
        $query .= "         SCHREG_STUDYREC_DAT.ANNUAL = '01' ";
        $query .= "          or SCHREG_STUDYREC_DAT.ANNUAL = '02' ";
        $query .= "          or SCHREG_STUDYREC_DAT.ANNUAL = '03' ";
        $query .= "          ";
        $query .= "     group by ";
        $query .= "         SCHREG_STUDYREC_DAT.SCHREGNO) as TBL9 ";
        $query .= "      on TBL9.SCHREGNO = TBL1.SCHREGNO ";
        $query .= " left join ";
        $query .= "     SCHREG_ATTENDREC_DAT TBL10 ";
        $query .= "     on TBL10.SCHREGNO = TBL1.SCHREGNO ";
        $query .= "     and TBL10.ANNUAL = '01' ";
        $query .= " left join ";
        $query .= "     SCHREG_ATTENDREC_DAT TBL11 ";
        $query .= "     on TBL11.SCHREGNO = TBL1.SCHREGNO ";
        $query .= "     and TBL11.ANNUAL = '02' ";
        $query .= " left join ";
        $query .= "     SCHREG_ATTENDREC_DAT TBL12 ";
        $query .= "     on TBL12.SCHREGNO = TBL1.SCHREGNO ";
        $query .= "     and TBL12.ANNUAL = '03' ";
        $query .= " left join ";
        $query .= " (select ";
        $query .= "     SCHREGNO, ";
        $query .= "     COMMITTEENAME ";
        $query .= " from ";
        $query .= "     DB2INST1.NAME_MST ";
        $query .= " inner join ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT ";
        $query .= "     on SCHREG_COMMITTEE_HIST_DAT.COMMITTEE_FLG = NAME_MST.NAMESPARE1 ";
        $query .= "     and NAME_MST.NAMESPARE2 = SCHREG_COMMITTEE_HIST_DAT.COMMITTEECD ";
        $query .= " inner join ";
        $query .= "     COMMITTEE_MST ";
        $query .= "     on COMMITTEE_MST.COMMITTEECD = SCHREG_COMMITTEE_HIST_DAT.COMMITTEECD ";
        $query .= "     and COMMITTEE_MST.SCHOOL_KIND = 'H' ";
        $query .= " inner join ";
        $query .= "     DB2INST1.SCHREG_REGD_GDAT ";
        $query .= "     on SCHREG_REGD_GDAT.YEAR = SCHREG_COMMITTEE_HIST_DAT.YEAR ";
        $query .= "     and SCHREG_REGD_GDAT.GRADE = SCHREG_COMMITTEE_HIST_DAT.GRADE ";
        $query .= "     and SCHREG_REGD_GDAT.SCHOOL_KIND = 'H' ";
        $query .= "     and SCHREG_REGD_GDAT.GRADE_CD = '01' ";
        $query .= " where ";
        $query .= "     NAME_MST.NAMECD1 = 'J009') as TBL13 ";
        $query .= " on ";
        $query .= "     TBL13.SCHREGNO = TBL1.SCHREGNO ";
        $query .= " left join ";
        $query .= " (select ";
        $query .= "     SCHREGNO, ";
        $query .= "     COMMITTEENAME ";
        $query .= " from ";
        $query .= "     DB2INST1.NAME_MST ";
        $query .= " inner join ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT ";
        $query .= "     on SCHREG_COMMITTEE_HIST_DAT.COMMITTEE_FLG = NAME_MST.NAMESPARE1 ";
        $query .= "     and NAME_MST.NAMESPARE2 = SCHREG_COMMITTEE_HIST_DAT.COMMITTEECD ";
        $query .= " inner join ";
        $query .= "     COMMITTEE_MST ";
        $query .= "     on COMMITTEE_MST.COMMITTEECD = SCHREG_COMMITTEE_HIST_DAT.COMMITTEECD ";
        $query .= "     and COMMITTEE_MST.SCHOOL_KIND = 'H' ";
        $query .= " inner join ";
        $query .= "     DB2INST1.SCHREG_REGD_GDAT ";
        $query .= "     on SCHREG_REGD_GDAT.YEAR = SCHREG_COMMITTEE_HIST_DAT.YEAR ";
        $query .= "     and SCHREG_REGD_GDAT.GRADE = SCHREG_COMMITTEE_HIST_DAT.GRADE ";
        $query .= "     and SCHREG_REGD_GDAT.SCHOOL_KIND = 'H' ";
        $query .= "     and SCHREG_REGD_GDAT.GRADE_CD = '02' ";
        $query .= " where ";
        $query .= "     NAME_MST.NAMECD1 = 'J009') as TBL14 ";
        $query .= " on ";
        $query .= "     TBL14.SCHREGNO = TBL1.SCHREGNO ";
        $query .= " left join ";
        $query .= " (select ";
        $query .= "     SCHREGNO, ";
        $query .= "     COMMITTEENAME ";
        $query .= " from ";
        $query .= "     DB2INST1.NAME_MST ";
        $query .= " inner join ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT ";
        $query .= "     on SCHREG_COMMITTEE_HIST_DAT.COMMITTEE_FLG = NAME_MST.NAMESPARE1 ";
        $query .= "     and NAME_MST.NAMESPARE2 = SCHREG_COMMITTEE_HIST_DAT.COMMITTEECD ";
        $query .= " inner join ";
        $query .= "     COMMITTEE_MST ";
        $query .= "     on COMMITTEE_MST.COMMITTEECD = SCHREG_COMMITTEE_HIST_DAT.COMMITTEECD ";
        $query .= "     and COMMITTEE_MST.SCHOOL_KIND = 'H' ";
        $query .= " inner join ";
        $query .= "     DB2INST1.SCHREG_REGD_GDAT ";
        $query .= "     on SCHREG_REGD_GDAT.YEAR = SCHREG_COMMITTEE_HIST_DAT.YEAR ";
        $query .= "     and SCHREG_REGD_GDAT.GRADE = SCHREG_COMMITTEE_HIST_DAT.GRADE ";
        $query .= "     and SCHREG_REGD_GDAT.SCHOOL_KIND = 'H' ";
        $query .= "     and SCHREG_REGD_GDAT.GRADE_CD = '03' ";
        $query .= " where ";
        $query .= "     NAME_MST.NAMECD1 = 'J009') as TBL15 ";
        $query .= " on ";
        $query .= "     TBL15.SCHREGNO = TBL1.SCHREGNO ";
        $query .= " left join ";
        $query .= "     (select distinct ";
        $query .= "             SCHREG_CLUB_HIST_DAT.SCHREGNO, ";
        $query .= "             CLUBNAME ";
        $query .= "         from ";
        $query .= "             SCHREG_CLUB_HIST_DAT  ";
        $query .= "         inner join ";
        $query .= "             (select ";
        $query .= "                 SCHREG_REGD_GDAT.YEAR, ";
        $query .= "                 SCHREG_REGD_DAT.SCHREGNO ";
        $query .= "             from ";
        $query .= "                 SCHREG_REGD_DAT  ";
        $query .= "             inner join ";
        $query .= "                 SCHREG_REGD_GDAT ";
        $query .= "                 on SCHREG_REGD_DAT.YEAR = SCHREG_REGD_GDAT.YEAR ";
        $query .= "                 and SCHREG_REGD_DAT.GRADE = SCHREG_REGD_GDAT.GRADE ";
        $query .= "                 and SCHREG_REGD_GDAT.SCHOOL_KIND = 'H' ";
        $query .= "                 and SCHREG_REGD_GDAT.GRADE_CD = '01') REGD_TBL ";
        $query .= "             on REGD_TBL.SCHREGNO = SCHREG_CLUB_HIST_DAT.SCHREGNO ";
        $query .= "         inner join ";
        $query .= "             CLUB_MST ";
        $query .= "             on SCHREG_CLUB_HIST_DAT.CLUBCD = CLUB_MST.CLUBCD ";
        $query .= "             and CLUB_MST.SCHOOL_KIND = 'H' ";
        $query .= "         where ";
        $query .= "             SCHREG_CLUB_HIST_DAT.SDATE > date(concat(REGD_TBL.YEAR,'-4-1')) ";
        $query .= "             and SCHREG_CLUB_HIST_DAT.SDATE < date(concat(rtrim(char(int(REGD_TBL.YEAR)+1)),'-3-31'))) TBL16 ";
        $query .= "     on TBL16.SCHREGNO = TBL1.SCHREGNO ";
        $query .= " left join ";
        $query .= "     (select distinct ";
        $query .= "             SCHREG_CLUB_HIST_DAT.SCHREGNO, ";
        $query .= "             CLUBNAME ";
        $query .= "         from ";
        $query .= "             SCHREG_CLUB_HIST_DAT  ";
        $query .= "         inner join ";
        $query .= "             (select ";
        $query .= "                 SCHREG_REGD_GDAT.YEAR, ";
        $query .= "                 SCHREG_REGD_DAT.SCHREGNO ";
        $query .= "             from ";
        $query .= "                 SCHREG_REGD_DAT  ";
        $query .= "             inner join ";
        $query .= "                 SCHREG_REGD_GDAT ";
        $query .= "                 on SCHREG_REGD_DAT.YEAR = SCHREG_REGD_GDAT.YEAR ";
        $query .= "                 and SCHREG_REGD_DAT.GRADE = SCHREG_REGD_GDAT.GRADE ";
        $query .= "                 and SCHREG_REGD_GDAT.SCHOOL_KIND = 'H' ";
        $query .= "                 and SCHREG_REGD_GDAT.GRADE_CD = '02') REGD_TBL ";
        $query .= "             on REGD_TBL.SCHREGNO=SCHREG_CLUB_HIST_DAT.SCHREGNO ";
        $query .= "         inner join ";
        $query .= "             CLUB_MST ";
        $query .= "             on SCHREG_CLUB_HIST_DAT.CLUBCD = CLUB_MST.CLUBCD ";
        $query .= "             and CLUB_MST.SCHOOL_KIND = 'H' ";
        $query .= "         where ";
        $query .= "             SCHREG_CLUB_HIST_DAT.SDATE > date(concat(REGD_TBL.YEAR,'-4-1')) ";
        $query .= "             and SCHREG_CLUB_HIST_DAT.SDATE < date(concat(rtrim(char(int(REGD_TBL.YEAR)+1)),'-3-31'))) TBL17 ";
        $query .= "     on TBL17.SCHREGNO = TBL1.SCHREGNO ";
        $query .= " left join ";
        $query .= "     (select distinct ";
        $query .= "             SCHREG_CLUB_HIST_DAT.SCHREGNO, ";
        $query .= "             CLUBNAME ";
        $query .= "         from ";
        $query .= "             SCHREG_CLUB_HIST_DAT  ";
        $query .= "         inner join ";
        $query .= "             (select ";
        $query .= "                 SCHREG_REGD_GDAT.YEAR, ";
        $query .= "                 SCHREG_REGD_DAT.SCHREGNO ";
        $query .= "             from ";
        $query .= "                 SCHREG_REGD_DAT  ";
        $query .= "             inner join ";
        $query .= "                 SCHREG_REGD_GDAT ";
        $query .= "                 on SCHREG_REGD_DAT.YEAR = SCHREG_REGD_GDAT.YEAR ";
        $query .= "                 and SCHREG_REGD_DAT.GRADE = SCHREG_REGD_GDAT.GRADE ";
        $query .= "                 and SCHREG_REGD_GDAT.SCHOOL_KIND = 'H' ";
        $query .= "                 and SCHREG_REGD_GDAT.GRADE_CD = '03') REGD_TBL ";
        $query .= "             on REGD_TBL.SCHREGNO=SCHREG_CLUB_HIST_DAT.SCHREGNO ";
        $query .= "         inner join ";
        $query .= "             CLUB_MST ";
        $query .= "             on SCHREG_CLUB_HIST_DAT.CLUBCD = CLUB_MST.CLUBCD ";
        $query .= "             and CLUB_MST.SCHOOL_KIND='H' ";
        $query .= "         where ";
        $query .= "             SCHREG_CLUB_HIST_DAT.SDATE > date(concat(REGD_TBL.YEAR,'-4-1')) ";
        $query .= "             and SCHREG_CLUB_HIST_DAT.SDATE < date(concat(rtrim(char(int(REGD_TBL.YEAR)+1)),'-3-31'))) TBL18 ";
        $query .= "     on TBL18.SCHREGNO = TBL1.SCHREGNO ";
        
        return $query;
    }
    //CSVクラス委員情報
    function getSubData($model,$schregNo,$gradeCd) {
        $query = " select ";
        $query .= "     SCHREGNO, ";
        $query .= "     COMMITTEENAME ";
        $query .= " from ";
        $query .= "     DB2INST1.NAME_MST ";
        $query .= " left join ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT ";
        $query .= "     on SCHREG_COMMITTEE_HIST_DAT.COMMITTEE_FLG != NAME_MST.NAMESPARE1 ";
        $query .= " left join ";
        $query .= "     COMMITTEE_MST ";
        $query .= "     on COMMITTEE_MST.COMMITTEECD = SCHREG_COMMITTEE_HIST_DAT.COMMITTEECD ";
        $query .= " left join ";
        $query .= "     DB2INST1.SCHREG_REGD_GDAT ";
        $query .= "     on SCHREG_REGD_GDAT.YEAR = SCHREG_COMMITTEE_HIST_DAT.YEAR ";
        $query .= "     and SCHREG_REGD_GDAT.GRADE = SCHREG_COMMITTEE_HIST_DAT.GRADE ";
        $query .= "     and SCHREG_REGD_GDAT.SCHOOL_KIND='H' ";
        $query .= "     and SCHREG_REGD_GDAT.GRADE_CD = '{$gradeCd}' ";
        $query .= " where ";
        $query .= "     NAME_MST.NAMECD1 = 'J009' ";
        $query .= "     and SCHREGNO = '{$schregNo}'; ";
        
        return $query;
    }
    //CSV科目情報
    function getSubData2($model) {
        $query = " select ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUBCLASS_NAME, ";
        $query .= "     sum(SCORE) as SCORE ";
        $query .= " from ";
        $query .= "     PROFICIENCY_RANK_DAT ";
        $query .= " left join ";
        $query .= "       PROFICIENCY_SUBCLASS_MST ";
        $query .= "       on PROFICIENCY_SUBCLASS_MST.PROFICIENCY_SUBCLASS_CD = PROFICIENCY_RANK_DAT.PROFICIENCY_SUBCLASS_CD ";
        $query .= " where ";
        $query .= "     RANK_DATA_DIV = '01' ";
        $query .= "     and RANK_DIV = '01' ";
        $query .= "     and SUBCLASS_NAME is not null ";
        $query .= " group by ";
        $query .= "     PROFICIENCY_RANK_DAT.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     PROFICIENCY_SUBCLASS_MST.SUBCLASS_NAME, ";
        $query .= "     SCHREGNO ";
        $query .= " order by ";
        $query .= "     PROFICIENCY_RANK_DAT.PROFICIENCY_SUBCLASS_CD ";
        
        return $query;
    }
}
?>