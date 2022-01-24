<?php

require_once('for_php7.php');

class knjh400_singakuQuery extends Query
{
    //名称マスタ取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //生徒名取得
    public function getName($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //受験報告データ（進学）取得
    public function selectQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     D4.REMARK1 AS SEARCH10, ";
        $query .= "     T2.SCHOOL_CD, ";
        $query .= "     T2.SCHOOL_NAME, ";
        $query .= "     T1.FACULTYCD, ";
        $query .= "     T3.FACULTYNAME, ";
        $query .= "     T1.DEPARTMENTCD, ";
        $query .= "     T4.DEPARTMENTNAME, ";
        $query .= "     CASE WHEN T1.PREF_CD IS NULL THEN '' ELSE T1.PREF_CD END || '-' || CASE WHEN T1.CITY_CD IS NULL THEN '' ELSE T1.CITY_CD END AS PREF_CD, ";
        $query .= "     T1.TOROKU_DATE, ";
        $query .= "     T1.STAT_DATE1, ";
        $query .= "     T1.STAT_DATE3, ";
        $query .= "     T1.HOWTOEXAM, ";
        $query .= "     D3.REMARK1 AS SHDIV, ";
        $query .= "     T1.CONTENTEXAM, ";
        $query .= "     T1.REASONEXAM, ";
        $query .= "     T1.THINKEXAM, ";
        $query .= "     T1.DECISION, ";
        $query .= "     D5.REMARK1 AS TOKUMEI, ";
        $query .= "     CASE WHEN VALUE(E1.REMARK13,'') = '' THEN '' ELSE '1' END AS ISSUE, ";
        $query .= "     T1.PLANSTAT, ";
        $query .= "     T5.NAME1 AS SCHOOL_GROUP, ";
        $query .= "     T3.CAMPUS_ADDR_CD, ";
        $query .= "     D1.REMARK1 AS ADVERTISE_DIV, ";
        $query .= "     D1.REMARK2 AS PROGRAM_CD, ";
        $query .= "     D1.REMARK3 AS FORM_CD, ";
        $query .= "     D1.REMARK4 AS L_CD, ";
        $query .= "     D1.REMARK5 AS S_CD, ";
        $query .= "     D1.REMARK6 AS LIMIT_DATE_WINDOW, ";
        $query .= "     D1.REMARK7 AS LIMIT_DATE_MAIL, ";
        $query .= "     D1.REMARK8 AS LIMIT_MAIL_DIV, ";
        $query .= "     D1.REMARK9 AS EXAMNO ";
        //東京都集計用
        if ($model->Properties["useTokyotoShinroTyousasyo"] == "1") {
            $query .= "     ,SYSD.SCHOOL_CATEGORY_CD ";
            $query .= "     ,SYSD.TOKYO_L_CD ";
            $query .= "     ,SYSD.TOKYO_M_CD ";
        }
        //選考、志望順位
        if ($model->Properties["KNJE360_SENKOUKOUMOKU"] == "1") {
            $query .= "     ,D6.REMARK1 AS SELECT_CATEGORY ";
            $query .= "     ,D6.REMARK2 AS SELECT_RESULT ";
            $query .= "     ,D6.REMARK3 AS DESIRED_RANK ";
        }
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D1 ON ";
        $query .= "             T1.YEAR         = D1.YEAR AND ";
        $query .= "             T1.SEQ          = D1.SEQ AND ";
        $query .= "             D1.DETAIL_SEQ   = 1 ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D3 ON ";
        $query .= "             T1.YEAR         = D3.YEAR AND ";
        $query .= "             T1.SEQ          = D3.SEQ AND ";
        $query .= "             D3.DETAIL_SEQ   = 3 ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D4 ON ";
        $query .= "             T1.YEAR         = D4.YEAR AND ";
        $query .= "             T1.SEQ          = D4.SEQ AND ";
        $query .= "             D4.DETAIL_SEQ   = 4 ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D5 ON ";
        $query .= "             T1.YEAR         = D5.YEAR AND ";
        $query .= "             T1.SEQ          = D5.SEQ AND ";
        $query .= "             D5.DETAIL_SEQ   = 5 ";
        //選考、志望順位
        if ($model->Properties["KNJE360_SENKOUKOUMOKU"] == "1") {
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D6 ON ";
            $query .= "             T1.YEAR         = D6.YEAR AND ";
            $query .= "             T1.SEQ          = D6.SEQ AND ";
            $query .= "             D6.DETAIL_SEQ   = 6 ";
        }
        $query .= "     LEFT JOIN COLLEGE_MST T2 ON T1.STAT_CD = T2.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST T3 ON ";
        $query .= "             T1.STAT_CD      = T3.SCHOOL_CD AND ";
        $query .= "             T1.FACULTYCD    = T3.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST T4 ON ";
        $query .= "             T1.STAT_CD      = T4.SCHOOL_CD AND ";
        $query .= "             T1.FACULTYCD    = T4.FACULTYCD AND ";
        $query .= "             T1.DEPARTMENTCD = T4.DEPARTMENTCD ";
        $query .= "     LEFT JOIN NAME_MST T5 ON ";
        $query .= "             T5.NAMECD1      = 'E012' AND ";
        $query .= "             T5.NAMECD2      = T2.SCHOOL_GROUP ";
        $query .= "     LEFT JOIN CERTIF_DETAIL_EACHTYPE_DAT E1 ON ";
        $query .= "             T1.YEAR         = E1.YEAR AND ";
        $query .= "             T1.SEQ          = INT(E1.REMARK13) AND ";
        $query .= "             E1.TYPE         = '1' ";
        //東京都集計用
        if ($model->Properties["useTokyotoShinroTyousasyo"] == "1") {
            $query .= "     LEFT JOIN COLLEGE_FACULTY_SYSTEM_MST SYSD ON T1.STAT_CD   = SYSD.SCHOOL_CD ";
            $query .= "                                              AND T1.FACULTYCD = SYSD.FACULTYCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     T1.SENKOU_KIND = '0' ";
        if ($model->gouhi == '2' || $model->gouhi == '3') {
            $query .= "     AND T1.DECISION = '" . ($model->gouhi - 1) . "' ";
        }
        if ($model->sinro == '2') {
            $query .= "     AND T1.PLANSTAT = '1' ";
        }

        return $query;
    }

    //方式取得
    public function getFormCd($Row)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     FORM_CD AS VALUE, ";
        $query .= "     FORM_CD || '：' || FORM_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_CALENDAR ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_CD       = '".$Row["SCHOOL_CD"]."' AND ";
        $query .= "     FACULTYCD       = '".$Row["FACULTYCD"]."' AND ";
        $query .= "     DEPARTMENTCD    = '".$Row["DEPARTMENTCD"]."' AND ";
        $query .= "     ADVERTISE_DIV   = '".$Row["ADVERTISE_DIV"]."' AND ";
        $query .= "     PROGRAM_CD      = '".$Row["PROGRAM_CD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //進路調査データ一覧取得
    public function selectQuery2($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.SCHOOL_NAME AS SCHOOL_NAME1, ";
        $query .= "     L2.SCHOOL_NAME AS SCHOOL_NAME2, ";
        $query .= "     L3.JOBTYPE_LNAME AS JOBTYPE_LNAME1, ";
        $query .= "     L4.JOBTYPE_LNAME AS JOBTYPE_LNAME2, ";
        $query .= "     L6.JOBTYPE_SNAME AS JOBTYPE_SNAME1, ";
        $query .= "     L7.JOBTYPE_SNAME AS JOBTYPE_SNAME2, ";
        $query .= "     T1.QUESTIONNAIRECD || '：' || L5.QUESTIONNAIRENAME AS QUESTIONNAIRENAME ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DAT T1 ";
        $query .= "     LEFT JOIN COLLEGE_MST L1 ON T1.SCHOOL_CD1 = L1.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_MST L2 ON T1.SCHOOL_CD2 = L2.SCHOOL_CD ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST L3 ON T1.JOBTYPE_LCD1 = L3.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST L4 ON T1.JOBTYPE_LCD2 = L4.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST L5 ON T1.QUESTIONNAIRECD = L5.QUESTIONNAIRECD ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST L6 ON T1.JOBTYPE_LCD1 = L6.JOBTYPE_LCD AND  T1.JOBTYPE_MCD1 = L6.JOBTYPE_MCD AND  T1.JOBTYPE_SCD1 = L6.JOBTYPE_SCD ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST L7 ON T1.JOBTYPE_LCD2 = L7.JOBTYPE_LCD AND  T1.JOBTYPE_MCD2 = L7.JOBTYPE_MCD AND  T1.JOBTYPE_SCD2 = L7.JOBTYPE_SCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     T1.YEAR     = '{$model->year}' AND ";
        $query .= "     T1.COURSE_KIND = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSE_KIND, ";
        $query .= "     T1.ENTRYDATE DESC, ";
        $query .= "     T1.SEQ DESC ";

        return $query;
    }

    //学校名取得
    public function getSchoolName($model, $entrydate, $seq)
    {
        $query  = " SELECT ";
        $query .= "     T1.HOPE_NUM, ";
        $query .= "     T2.SCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DETAIL_DAT T1, ";
        $query .= "     COLLEGE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTRYDATE    = '".$entrydate."' AND ";
        $query .= "     T1.SEQ          = ".$seq." AND ";
        $query .= "     T1.SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     T1.SCHOOL_CD    = T2.SCHOOL_CD  ";
        $query .= " ORDER BY ";
        $query .= "     T1.HOPE_NUM ";

        return $query;
    }
    //進路調査データ取得
    public function getSubQuery1($model, $entrydate, $seq)
    {
        $entrydate = str_replace("/", "-", $entrydate);

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     S1.JOBTYPE_SNAME AS JOBTYPE_SNAME1, ";
        $query .= "     S2.JOBTYPE_SNAME AS JOBTYPE_SNAME2 ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DAT T1 ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST S1 ON T1.JOBTYPE_LCD1 = S1.JOBTYPE_LCD AND T1.JOBTYPE_MCD1 = S1.JOBTYPE_MCD AND T1.JOBTYPE_SCD1 = S1.JOBTYPE_SCD ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST S2 ON T1.JOBTYPE_LCD2 = S2.JOBTYPE_LCD AND T1.JOBTYPE_MCD2 = S2.JOBTYPE_MCD AND T1.JOBTYPE_SCD2 = S2.JOBTYPE_SCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     T1.ENTRYDATE = '{$entrydate}' AND ";
        $query .= "     T1.SEQ = '{$seq}' AND ";
        $query .= "     T1.YEAR = '{$model->year}' ";

        return $query;
    }

    //進路調査詳細データ取得
    public function getSubQuery1Detail($model, $entrydate, $seq, $hope_num)
    {
        $entrydate = str_replace("/", "-", $entrydate);

        $query  = " SELECT ";
        $query .= "     SCHOOL_GROUP AS SCHOOL_GROUP".$hope_num.", ";
        $query .= "     FACULTY_GROUP AS FACULTY_GROUP".$hope_num.", ";
        $query .= "     DEPARTMENT_GROUP AS DEPARTMENT_GROUP".$hope_num.", ";
        $query .= "     SCHOOL_CD AS SCHOOL_CD".$hope_num.", ";
        $query .= "     FACULTYCD AS FACULTYCD".$hope_num.", ";
        $query .= "     DEPARTMENTCD AS DEPARTMENTCD".$hope_num.", ";
        $query .= "     HOWTOEXAM AS HOWTOEXAM".$hope_num." ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '{$model->schregno}' AND ";
        $query .= "     ENTRYDATE   = '{$entrydate}' AND ";
        $query .= "     SEQ         = '{$seq}' AND ";
        $query .= "     HOPE_NUM    = '{$hope_num}' ";

        return $query;
    }
    //大学・専門学校情報取得
    public function getCollegeInfo($schoolcd, $facultycd, $departmentcd, $addr_cd = "", $model = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_CD, ";
        $query .= "     T1.SCHOOL_NAME, ";
        $query .= "     T5.ZIPCD, ";
        $query .= "     T5.ADDR1, ";
        $query .= "     T5.ADDR2, ";
        $query .= "     T5.TELNO, ";
        $query .= "     T1.SCHOOL_GROUP, ";
        $query .= "     T2.NAME1 AS SCHOOL_GROUP_NAME, ";
        $query .= "     T3.FACULTYCD, ";
        $query .= "     T3.FACULTYNAME, ";
        $query .= "     T4.DEPARTMENTCD, ";
        $query .= "     T4.DEPARTMENTNAME ";
        //東京都集計用
        if ($model->Properties["useTokyotoShinroTyousasyo"] == "1") {
            $query .= "     ,SYSD.PROTECTION_FLG ";
            $query .= "     ,SYSD.SCHOOL_CATEGORY_CD ";
            $query .= "     ,SYSD.TOKYO_L_CD ";
            $query .= "     ,SYSD.TOKYO_M_CD ";
        }
        $query .= " FROM ";
        $query .= "     COLLEGE_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST T2 ON  ";
        $query .= "             T2.NAMECD1 = 'E012' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOL_GROUP ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST T3 ON  ";
        $query .= "             T3.SCHOOL_CD = T1.SCHOOL_CD AND ";
        $query .= "             T3.FACULTYCD = '".$facultycd."' ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST T4 ON  ";
        $query .= "             T4.SCHOOL_CD = T1.SCHOOL_CD AND ";
        $query .= "             T4.FACULTYCD = T3.FACULTYCD AND ";
        $query .= "             T4.DEPARTMENTCD = '".$departmentcd."' ";
        $query .= "     LEFT JOIN COLLEGE_CAMPUS_ADDR_DAT T5 ON  ";
        $query .= "             T5.SCHOOL_CD = T1.SCHOOL_CD AND ";
        if ($addr_cd) {
            $query .= "             T5.CAMPUS_ADDR_CD = T3.CAMPUS_ADDR_CD ";
        } else {
            $query .= "             T5.CAMPUS_ADDR_CD = T1.CAMPUS_ADDR_CD ";
        }
        //東京都集計用
        if ($model->Properties["useTokyotoShinroTyousasyo"] == "1") {
            $query .= "     LEFT JOIN COLLEGE_FACULTY_SYSTEM_MST SYSD ON T1.SCHOOL_CD = SYSD.SCHOOL_CD ";
            $query .= "                                              AND T3.FACULTYCD = SYSD.FACULTYCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_CD = '".$schoolcd."' ";

        return $query;
    }
    //学部系列一覧取得
    public function getFacultyGroup()
    {
        $query  = " SELECT ";
        $query .= "     FACULTY_GROUP AS VALUE, ";
        $query .= "     FACULTY_GROUP || '：' || FACULTY_GROUPNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     FACULTY_GROUP_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //学科一覧系列取得
    public function getDepartmentGroup()
    {
        $query  = " SELECT ";
        $query .= "     DEPARTMENT_GROUP AS VALUE, ";
        $query .= "     DEPARTMENT_GROUP || '：' || DEPARTMENT_GROUPNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     DEPARTMENT_GROUP_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
