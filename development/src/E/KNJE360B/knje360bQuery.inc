<?php

require_once('for_php7.php');

class knje360bQuery extends Query
{
    /***************メイン画面***************/

    //生徒情報取得
    public function getSchInfo($model)
    {
        //卒業生
        if ($model->mode == "grd") {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T2.HR_NAME, ";
            $query .= "     INT(T1.GRD_ATTENDNO) AS ATTENDNO, ";
            $query .= "     T1.NAME_SHOW ";
            $query .= " FROM ";
            $query .= "     GRD_BASE_MST T1, ";
            $query .= "     GRD_REGD_HDAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
            $query .= " AND FISCALYEAR(T1.GRD_DATE) = T2.YEAR ";
            $query .= " AND T1.GRD_SEMESTER         = T2.SEMESTER ";
            $query .= " AND T1.GRD_GRADE            = T2.GRADE ";
            $query .= " AND T1.GRD_HR_CLASS         = T2.HR_CLASS ";

        //在学生
        } else {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T3.HR_NAME, ";
            $query .= "     INT(T1.ATTENDNO) AS ATTENDNO, ";
            $query .= "     T2.NAME_SHOW ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ";
            $query .= "            ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ";
            $query .= "            ON T1.YEAR     = T3.YEAR ";
            $query .= "           AND T1.SEMESTER = T3.SEMESTER ";
            $query .= "           AND T1.GRADE    = T3.GRADE ";
            $query .= "           AND T1.HR_CLASS = T3.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR               = '".CTRL_YEAR."' ";
            $query .= " AND T1.SEMESTER           = '".CTRL_SEMESTER."' ";
            $query .= " AND T1.SCHREGNO           = '".$model->schregno."' ";
        }

        return $query;
    }

    //GRADE_CD取得
    public function getGradeCd($grade)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";

        return $query;
    }

    //部クラブ名取得
    public function getClubName($model)
    {
        switch ($model->gradeCd) {
            case "01":
                $setGrdYear = CTRL_YEAR + 3;
                break;
            case "02":
                $setGrdYear = CTRL_YEAR + 2;
                break;
            case "03":
                $setGrdYear = CTRL_YEAR + 1;
                break;
            case "":
                break;
            default:
                break;
        }
        $query  = " SELECT ";
        $query .= "     T2.CLUBNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST T2 ON T1.CLUBCD = T2.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                          AND T1.SCHOOLCD    = T2.SCHOOLCD ";
            $query .= "                          AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND (T1.EDATE IS NULL OR T1.EDATE IN ('9999-12-31', '{$setGrdYear}-03-31')) ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SCHOOL_KIND IN ( ";
            $query .= "                            SELECT ";
            $query .= "                                SCHOOL_KIND ";
            $query .= "                            FROM ";
            $query .= "                                SCHREG_REGD_GDAT ";
            $query .= "                            WHERE ";
            $query .= "                                    YEAR  = '".CTRL_YEAR."' ";
            $query .= "                                AND GRADE = '".$model->grade."' ";
            $query .= "                            ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.CLUBCD ";

        return $query;
    }

    //委員会取得
    public function getCommitName($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     LEFT JOIN COMMITTEE_MST T2 ON ";
        $query .= "         T1.COMMITTEE_FLG = T2.COMMITTEE_FLG AND T1.COMMITTEECD = T2.COMMITTEECD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.SCHOOLCD    = T2.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SCHOOL_KIND IN ( ";
            $query .= "                            SELECT ";
            $query .= "                                SCHOOL_KIND ";
            $query .= "                            FROM ";
            $query .= "                                SCHREG_REGD_GDAT ";
            $query .= "                            WHERE ";
            $query .= "                                    YEAR  = '".CTRL_YEAR."' ";
            $query .= "                                AND GRADE = '".$model->grade."' ";
            $query .= "                            ) ";
        }
        if ($model->getname == 'sundaikoufu') {
            $query .= "     AND T1.YEAR  = '".CTRL_YEAR."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR DESC, ";
        $query .= "     T1.SEQ DESC ";

        return $query;
    }

    //評定平均取得
    public function getValueAverage($model)
    {
        $query  = " WITH SCH_KIND AS ( ";
        if ($model->mode == "grd") {
            $query .= "     SELECT ";
            $query .= "         T2.SCHOOL_KIND ";
            $query .= "     FROM ";
            $query .= "         GRD_BASE_MST T1, ";
            $query .= "         SCHREG_REGD_GDAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.SCHREGNO             = '".$model->schregno."' ";
            $query .= "     AND FISCALYEAR(T1.GRD_DATE) = T2.YEAR ";
            $query .= "     AND T1.GRD_GRADE            = T2.GRADE ";
        } else {
            $query .= "     SELECT ";
            $query .= "         T2.SCHOOL_KIND ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1, ";
            $query .= "         SCHREG_REGD_GDAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = T2.YEAR ";
            $query .= "     AND T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND T1.GRADE    = T2.GRADE ";
            $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     DECIMAL(ROUND(DECIMAL(AVG(1.0 * VALUATION),5,2),1),5,1) AS AVERAGE ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " AND CLASSCD < '90' ";
        $query .= " AND VALUATION IS NOT NULL ";
        $query .= " AND SCHOOL_KIND = (SELECT SCHOOL_KIND FROM SCH_KIND) ";

        return $query;
    }

    //出身学校取得
    public function getFinschoolName($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST T2 ON ";
        $query .= "         T1.FINSCHOOLCD = T2.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //進路データ一覧取得
    public function selectQuery($model, $asc_or_desc)
    {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ENTRYDATE, ";
        $query .= "         T1.SEQ, ";
        $query .= "         '1' AS KINDCD, ";
        $query .= "         '進路調査' AS KINDNAME, ";
        $query .= "         CASE T1.COURSE_KIND WHEN '1' THEN '進学' WHEN '2' THEN '就職' WHEN '3' THEN 'その他（家事手伝い等）' ELSE '未定' END AS ONE, ";
        $query .= "         T2.QUESTIONNAIRENAME AS TWO, ";
        $query .= "         '' AS THREE, ";
        $query .= "         '' AS FOUR, ";
        $query .= "         '' AS FIVE, ";
        $query .= "         '' AS SIX, ";
        $query .= "         '' AS SEVEN, ";
        // 入試カレンダーの使用
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $query .= "         '' AS EIGHT, ";
            $query .= "         '' AS NINE, ";
        }
        if ($model->sort == "date") {
            $query .= "         T1.ENTRYDATE AS SORT ";
        } else {
            $query .= "         '' AS SORT ";
        }
        $query .= "     FROM ";
        $query .= "         COURSE_HOPE_SS_DAT T1 ";
        $query .= "         LEFT JOIN QUESTIONNAIRE_MST T2 ON T1.QUESTIONNAIRECD = T2.QUESTIONNAIRECD ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.TOROKU_DATE AS ENTRYDATE, ";
        $query .= "         T1.SEQ, ";
        $query .= "         '2' AS KINDCD, ";
        $query .= "         '受験報告' AS KINDNAME, ";
        $query .= "         '進学' AS ONE, ";
        $query .= "         T2.SCHOOL_NAME AS TWO, ";
        $query .= "         T3.FACULTYNAME AS THREE, ";
        $query .= "         T4.DEPARTMENTNAME AS FOUR, ";
        // 入試カレンダーの使用
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $query .= "         C1.PROGRAM_NAME AS FIVE, ";
            $query .= "         N1.NAME1 || ' ' || VALUE(N4.NAME1, '') AS SIX, ";
            $query .= "         N2.NAME1 AS SEVEN, ";
            $query .= "         N3.NAME1 AS EIGHT, ";
            $query .= "         C1.FORM_NAME AS NINE, ";
        } else {
            $query .= "         N1.NAME1 || ' ' || VALUE(N4.NAME1, '') AS FIVE, ";
            $query .= "         N2.NAME1 AS SIX, ";
            $query .= "         N3.NAME1 AS SEVEN, ";
        }
        if ($model->sort == "date") {
            $query .= "         T1.TOROKU_DATE AS SORT ";
        } else {
            $query .= "         T1.STAT_CD || '-' || T1.FACULTYCD || '-' || T1.DEPARTMENTCD AS SORT ";
        }
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_SS_DAT T1 ";
        $query .= "         LEFT JOIN COLLEGE_MST T2 ON T1.STAT_CD = T2.SCHOOL_CD ";
        $query .= "         LEFT JOIN COLLEGE_FACULTY_MST T3 ON T1.STAT_CD   = T3.SCHOOL_CD ";
        $query .= "                                         AND T1.FACULTYCD = T3.FACULTYCD ";
        $query .= "         LEFT JOIN COLLEGE_DEPARTMENT_MST T4 ON T1.STAT_CD       = T4.SCHOOL_CD ";
        $query .= "                                            AND T1.FACULTYCD     = T4.FACULTYCD ";
        $query .= "                                            AND T1.DEPARTMENTCD  = T4.DEPARTMENTCD ";
        $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E002' ";
        $query .= "                              AND N1.NAMECD2 = T1.HOWTOEXAM ";
        $query .= "         LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'E005' ";
        $query .= "                              AND N2.NAMECD2 = T1.DECISION ";
        $query .= "         LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'E006' ";
        $query .= "                              AND N3.NAMECD2 = T1.PLANSTAT ";
        $query .= "         LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D1 ON ";
        $query .= "                 T1.YEAR         = D1.YEAR AND ";
        $query .= "                 T1.SEQ          = D1.SEQ AND ";
        $query .= "                 D1.DETAIL_SEQ   = 1 ";
        $query .= "         LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D3 ON ";
        $query .= "                 T1.YEAR         = D3.YEAR AND ";
        $query .= "                 T1.SEQ          = D3.SEQ AND ";
        $query .= "                 D3.DETAIL_SEQ   = 3 ";
        $query .= "         LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'L006' ";
        $query .= "                              AND N4.NAMECD2 = D3.REMARK1 ";
        $query .= "         LEFT JOIN COLLEGE_EXAM_CALENDAR C1 ON ";
        $query .= "                 T1.YEAR             = C1.YEAR AND ";
        $query .= "                 T1.STAT_CD          = C1.SCHOOL_CD AND ";
        $query .= "                 T1.FACULTYCD        = C1.FACULTYCD AND ";
        $query .= "                 T1.DEPARTMENTCD     = C1.DEPARTMENTCD AND ";
        $query .= "                 D1.REMARK1          = C1.ADVERTISE_DIV AND ";
        $query .= "                 D1.REMARK2          = C1.PROGRAM_CD AND ";
        $query .= "                 D1.REMARK3          = C1.FORM_CD AND ";
        $query .= "                 D1.REMARK5          = C1.S_CD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.TOROKU_DATE IS NOT NULL ";
        $query .= "     AND T1.SENKOU_KIND = '0' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.TOROKU_DATE AS ENTRYDATE, ";
        $query .= "         T1.SEQ, ";
        $query .= "         '3' AS KINDCD, ";
        $query .= "         '受験報告' AS KINDNAME, ";
        $query .= "         '就職' AS ONE, ";
        $query .= "         T2.COMPANY_NAME AS TWO, ";
        $query .= "         '' AS THREE, ";
        $query .= "         '' AS FOUR, ";
        $query .= "         '' AS FIVE, ";
        $query .= "         '' AS SIX, ";
        $query .= "         '' AS SEVEN, ";
        // 入試カレンダーの使用
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $query .= "         '' AS EIGHT, ";
            $query .= "         '' AS NINE, ";
        }
        if ($model->sort == "date") {
            $query .= "         T1.TOROKU_DATE AS SORT ";
        } else {
            $query .= "         '' AS SORT ";
        }
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_SS_DAT T1 ";
        $query .= "         LEFT JOIN COMPANY_MST T2 ON T1.STAT_CD = T2.COMPANY_CD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.TOROKU_DATE IS NOT NULL ";
        $query .= "     AND T1.SENKOU_KIND = '1' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         ENTRYDATE, ";
        $query .= "         SEQ, ";
        $query .= "         '4' AS KINDCD, ";
        $query .= "         '進路相談' AS KINDNAME, ";
        $query .= "         TITLE AS ONE, ";
        $query .= "         CASE WHEN LENGTH(CONTENTS) > 90 THEN SUBSTR(CONTENTS,1,90) || '...' ELSE CONTENTS END AS TWO, ";
        $query .= "         '' AS THREE, ";
        $query .= "         '' AS FOUR, ";
        $query .= "         '' AS FIVE, ";
        $query .= "         '' AS SIX, ";
        $query .= "         '' AS SEVEN, ";
        // 入試カレンダーの使用
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $query .= "         '' AS EIGHT, ";
            $query .= "         '' AS NINE, ";
        }
        if ($model->sort == "date") {
            $query .= "         ENTRYDATE AS SORT ";
        } else {
            $query .= "         '' AS SORT ";
        }
        $query .= "     FROM ";
        $query .= "         COURSE_COUNSELING_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.TOROKU_DATE AS ENTRYDATE, ";
        $query .= "         T1.SEQ, ";
        $query .= "         '5' AS KINDCD, ";
        $query .= "         'その他' AS KINDNAME, ";
        $query .= "         '家事手伝い等' AS ONE, ";
        $query .= "         T2.NAME1 AS TWO, ";
        $query .= "         '' AS THREE, ";
        $query .= "         '' AS FOUR, ";
        $query .= "         '' AS FIVE, ";
        $query .= "         '' AS SIX, ";
        $query .= "         '' AS SEVEN, ";
        // 入試カレンダーの使用
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $query .= "         '' AS EIGHT, ";
            $query .= "         '' AS NINE, ";
        }
        if ($model->sort == "date") {
            $query .= "         T1.TOROKU_DATE AS SORT ";
        } else {
            $query .= "         '' AS SORT ";
        }
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_SS_DAT T1 ";
        $query .= "         LEFT JOIN V_NAME_MST T2 ON T1.YEAR = T2.YEAR ";
        $query .= "                                AND T2.NAMECD1 = 'E022' ";
        $query .= "                                AND T1.SENKOU_KIND_SUB = T2.NAMECD2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO     = '".$model->schregno."' ";
        $query .= "     AND T1.TOROKU_DATE IS NOT NULL ";
        $query .= "     AND T1.SENKOU_KIND  = '2' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        if ($model->output == "2" || $model->output == "3") {
            $query .= " WHERE ";
            $query .= "     KINDCD = '".$model->output."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SORT ".$asc_or_desc.", ";
        $query .= "     SEQ  ".$asc_or_desc." ";

        return $query;
    }

    // DELETE
    public function &getDeleteQuery($db, $model)
    {
        $array = explode(",", implode(',', $model->checked));

        for ($i = 0; $i < get_count($array); $i++) {
            list($entrydate, $seq, $kindcd) = explode(":", $array[$i]);

            if (($kindcd == '1') || ($kindcd == '4')) {
                $table = ($kindcd == '1') ? "COURSE_HOPE_SS_DAT" : "COURSE_COUNSELING_DAT";
                $entrydate = str_replace("/", "-", $entrydate);

                $query  = " DELETE FROM ";
                $query .=       $table ;
                $query .= " WHERE ";
                $query .= "     ENTRYDATE = '".$entrydate."' ";
                $query .= " AND SEQ      = ".$seq." ";
                $query .= " AND SCHREGNO = '".$model->schregno."' ";
                $db->query($query);

                if ($kindcd == '1') {
                    $query  = " DELETE FROM ";
                    $query .= "     COURSE_HOPE_DETAIL_SS_DAT ";
                    $query .= " WHERE ";
                    $query .= "     ENTRYDATE = '".$entrydate."' ";
                    $query .= " AND SEQ      = ".$seq." ";
                    $query .= " AND SCHREGNO = '".$model->schregno."' ";
                    $db->query($query);
                }
            } else {
                $query  = " DELETE FROM ";
                $query .= "     AFT_GRAD_COURSE_SS_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '".CTRL_YEAR."' ";
                $query .= " AND SEQ = ".$seq." ";
                $query .= " AND SCHREGNO = '".$model->schregno."' ";
                $db->query($query);

                $query  = " DELETE FROM ";
                $query .= "     AFT_GRAD_COURSE_DETAIL_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     SEQ = ".$seq." ";
                $db->query($query);

                //クリア -- CERTIF_DETAIL_EACHTYPE_DAT
                $data = array();
                $data["REMARK13"][TEXT]         = null;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $where  = " WHERE ";
                $where .= "     YEAR     = '".CTRL_YEAR."' ";
                $where .= " AND REMARK13 = '{$seq}' ";

                $db->query(Query::updateSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT", $where));
            }
        }
        return ;
    }

    /***************共通***************/

    //名称マスタ取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得(NAMECD2)
    public function getNameMstNamecd2($namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAMESPARE1 ";
        } else {
            $query .= "     NAMECD2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."'  ";
        } else {
            $query .= "     AND NAMESPARE1 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

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

    //本都道府県取得
    public function getMainPref()
    {
        $query .= " SELECT ";
        $query .= "     SCHOOL_REMARK1 AS PREF_CD ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_SEQ = '002' ";

        return $query;
    }

    //都道府県一覧取得
    public function getPrefList($prefcd = "")
    {
        $query  = " SELECT ";
        $query .= "     PREF_CD || '-' AS VALUE, ";
        $query .= "     PREF_CD || '：' || PREF_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        if ($prefcd) {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     PREF_CD || '-' || CITY_CD AS VALUE, ";
            $query .= "     PREF_CD || CITY_CD || '：' || CITY_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     CITY_MST ";
            $query .= " WHERE ";
            $query .= "     PREF_CD = '".$prefcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度内期間取得
    public function getSemesterMst()
    {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '9' ";

        return $query;
    }

    //SEQのMAX値取得
    public function getMaxSeq($db, $model, $table)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .=       $table ;
        $query .= " WHERE ";
        $query .= "     ENTRYDATE = '".str_replace("/", "-", $model->field["ENTRYDATE"])."' ";
        $query .= " AND SCHREGNO = '".$model->schregno."' ";
        $query .= " GROUP BY ";
        $query .= "     ENTRYDATE, ";
        $query .= "     SCHREGNO ";

        return $query;
    }

    //SEQのMAX値取得
    public function getMaxSeq2($db)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_SS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //テーブル存在チェック
    public function checkTableExist()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    public function getSchName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND IN ( ";
            $query .= "                     SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                             YEAR  = '".CTRL_YEAR."' ";
            $query .= "                         AND GRADE = '".$model->grade."' ";
            $query .= "                     ) ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         S2.SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT S1, ";
            $query .= "                         SCHREG_REGD_GDAT S2 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR     = S2.YEAR ";
            $query .= "                     AND S1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "                     AND S1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "                     AND S1.GRADE    = S2.GRADE ";
            $query .= "                     AND S1.SCHREGNO = '".$model->schregno."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }

    //大学データ存在チェック
    public function checkCollegeData($school_cd, $facultycd, $departmentcd, $faculty_flg, $department_flg)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST T1 ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST L1 ON  ";
        $query .= "             L1.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST L2 ON  ";
        $query .= "             L2.SCHOOL_CD = T1.SCHOOL_CD AND ";
        $query .= "             L2.FACULTYCD = L1.FACULTYCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_CD    = '".$school_cd."' ";
        if ($faculty_flg) {
            $query .= " AND L1.FACULTYCD    = '".$facultycd."' ";
        }
        if ($faculty_flg && $department_flg) {
            $query .= " AND L2.DEPARTMENTCD = '".$departmentcd."' ";
        }

        return $query;
    }

    /***************進路調査***************/

    //進路調査データ一覧取得
    public function getSubQuery1List($model)
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
        $query .= "     COURSE_HOPE_SS_DAT T1 ";
        $query .= "     LEFT JOIN COLLEGE_MST L1 ON T1.SCHOOL_CD1 = L1.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_MST L2 ON T1.SCHOOL_CD2 = L2.SCHOOL_CD ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST L3 ON T1.JOBTYPE_LCD1 = L3.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST L4 ON T1.JOBTYPE_LCD2 = L4.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST L5 ON T1.QUESTIONNAIRECD = L5.QUESTIONNAIRECD ";
        $query .= "     LEFT JOIN JOBTYPE_SS_MST L6 ON T1.JOBTYPE_LCD1 = L6.JOBTYPE_LCD AND  T1.JOBTYPE_MCD1 = L6.JOBTYPE_MCD AND  T1.JOBTYPE_SCD1 = L6.JOBTYPE_SCD AND T1.JOBTYPE_SSCD1 = L6.JOBTYPE_SSCD";
        $query .= "     LEFT JOIN JOBTYPE_SS_MST L7 ON T1.JOBTYPE_LCD2 = L7.JOBTYPE_LCD AND  T1.JOBTYPE_MCD2 = L7.JOBTYPE_MCD AND  T1.JOBTYPE_SCD2 = L7.JOBTYPE_SCD AND T1.JOBTYPE_SSCD2 = L7.JOBTYPE_SSCD";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.YEAR     = '".CTRL_YEAR."' ";
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
        $query .= "     COURSE_HOPE_DETAIL_SS_DAT T1, ";
        $query .= "     COLLEGE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTRYDATE    = '".$entrydate."' ";
        $query .= " AND T1.SEQ          = ".$seq." ";
        $query .= " AND T1.SCHREGNO     = '".$model->schregno."' ";
        $query .= " AND T1.SCHOOL_CD    = T2.SCHOOL_CD  ";
        $query .= " ORDER BY ";
        $query .= "     T1.HOPE_NUM ";

        return $query;
    }

    //進路調査データ取得
    public function getSubQuery1($model, $entrydate)
    {
        $entrydate = str_replace("/", "-", $entrydate);

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     S1.JOBTYPE_SNAME AS JOBTYPE_SNAME1, ";
        $query .= "     S2.JOBTYPE_SNAME AS JOBTYPE_SNAME2 ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_SS_DAT T1 ";
        $query .= "     LEFT JOIN JOBTYPE_SS_MST S1 ON T1.JOBTYPE_LCD1 = S1.JOBTYPE_LCD AND T1.JOBTYPE_MCD1 = S1.JOBTYPE_MCD AND T1.JOBTYPE_SCD1 = S1.JOBTYPE_SCD AND T1.JOBTYPE_SSCD1 = S1.JOBTYPE_SSCD";
        $query .= "     LEFT JOIN JOBTYPE_SS_MST S2 ON T1.JOBTYPE_LCD2 = S2.JOBTYPE_LCD AND T1.JOBTYPE_MCD2 = S2.JOBTYPE_MCD AND T1.JOBTYPE_SCD2 = S2.JOBTYPE_SCD AND T1.JOBTYPE_SSCD2 = S2.JOBTYPE_SSCD";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.ENTRYDATE = '".$entrydate."' ";
        $query .= " AND T1.SEQ = ".$model->seq." ";
        $query .= " AND T1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //進路調査詳細データ取得
    public function getSubQuery1Detail($model, $hope_num)
    {
        $entrydate = str_replace("/", "-", $model->entrydate);

        $query  = " SELECT ";
        $query .= "     SCHOOL_GROUP AS SCHOOL_GROUP".$hope_num.", ";
        $query .= "     FACULTY_GROUP AS FACULTY_GROUP".$hope_num.", ";
        $query .= "     DEPARTMENT_GROUP AS DEPARTMENT_GROUP".$hope_num.", ";
        $query .= "     SCHOOL_CD AS SCHOOL_CD".$hope_num.", ";
        $query .= "     FACULTYCD AS FACULTYCD".$hope_num.", ";
        $query .= "     DEPARTMENTCD AS DEPARTMENTCD".$hope_num.", ";
        $query .= "     HOWTOEXAM AS HOWTOEXAM".$hope_num." ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DETAIL_SS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $query .= " AND ENTRYDATE   = '".$entrydate."' ";
        $query .= " AND SEQ         = ".$model->seq." ";
        $query .= " AND HOPE_NUM    = ".$hope_num." ";

        return $query;
    }

    //アンケート一覧取得
    public function getQuestionnaireList()
    {
        $query  = " SELECT ";
        $query .= "     T1.QUESTIONNAIRECD AS VALUE, ";
        $query .= "     T1.QUESTIONNAIRECD || '：' || T2.QUESTIONNAIRENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_YDAT T1 ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST T2 ON T1.QUESTIONNAIRECD = T2.QUESTIONNAIRECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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

    //学校系列取得
    public function getCollegeGroup($db, $cd)
    {
        $query .= " SELECT ";
        $query .= "     SCHOOL_GROUP ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '".$cd."' ";

        return $query;
    }

    //職種（大分類）一覧取得
    public function getJobtypeLList($lcd = "")
    {
        $query  = " SELECT ";
        $query .= "     JOBTYPE_LCD AS VALUE, ";
        $query .= "     JOBTYPE_LCD || '：' || JOBTYPE_LNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_L_MST ";
        if ($lcd) {
            $query .= " WHERE ";
            $query .= "     JOBTYPE_LCD = '".$lcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //職種（中分類）一覧取得
    public function getJobtypeMList($jobtype_lcd)
    {
        $query .= " SELECT ";
        $query .= "     JOBTYPE_MCD || ' ' || JOBTYPE_MNAME AS LABEL, ";
        $query .= "     JOBTYPE_MCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_M_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '{$jobtype_lcd}' ";

        return $query;
    }

    //職種（小分類）一覧取得
    public function getJobtypeSList($jobtype_lcd, $jobtype_mcd)
    {
        $query .= " SELECT ";
        $query .= "        JOBTYPE_SCD || '-' || JOBTYPE_SSCD || ' ' || JOBTYPE_SNAME AS LABEL ";
        $query .= "      , JOBTYPE_SCD || '-' || JOBTYPE_SSCD AS VALUE ";
        $query .= "   FROM ";
        $query .= "        JOBTYPE_SS_MST ";
        $query .= "  WHERE ";
        $query .= "        JOBTYPE_LCD = '{$jobtype_lcd}' ";
        $query .= "    AND JOBTYPE_MCD = '{$jobtype_mcd}' ";

        return $query;
    }

    // UPDATE（進路調査）
    public function &getUpdateQuery1($db, $model, $fields, $insert)
    {
        $seq = ($insert == "insert") ? (int)$db->getOne(knje360bQuery::getMaxSeq($db, $model, "COURSE_HOPE_SS_DAT"))+1 : $model->seq;
        $entrydate = str_replace("/", "-", $fields["ENTRYDATE"]);

        $data["ENTRYDATE"][TEXT]            = $entrydate;
        $data["SEQ"][NUMBER]                = $seq;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["COURSE_KIND"][TEXT]          = $fields["COURSE_KIND"];
        $data["QUESTIONNAIRECD"][TEXT]      = $fields["QUESTIONNAIRECD"];

        if ($fields["COURSE_KIND"] == '1') {
            $data["SCHOOL_GROUP1"][TEXT]        = ($fields["SCHOOL_GROUP1"]) ? $fields["SCHOOL_GROUP1"] : $db->getOne(knje360bQuery::getCollegeGroup($db, $fields["SCHOOL_CD1"]));
            $data["FACULTY_GROUP1"][TEXT]       = $fields["FACULTY_GROUP1"];
            $data["DEPARTMENT_GROUP1"][TEXT]    = $fields["DEPARTMENT_GROUP1"];
            $data["SCHOOL_CD1"][TEXT]           = $fields["SCHOOL_CD1"];
            $data["FACULTYCD1"][TEXT]           = $fields["FACULTYCD1"];
            $data["DEPARTMENTCD1"][TEXT]        = $fields["DEPARTMENTCD1"];
            $data["HOWTOEXAM1"][NUMBER]         = $fields["HOWTOEXAM1"];
            $data["SCHOOL_GROUP2"][TEXT]        = ($fields["SCHOOL_GROUP2"]) ? $fields["SCHOOL_GROUP2"] : $db->getOne(knje360bQuery::getCollegeGroup($db, $fields["SCHOOL_CD2"]));
            $data["FACULTY_GROUP2"][TEXT]       = $fields["FACULTY_GROUP2"];
            $data["DEPARTMENT_GROUP2"][TEXT]    = $fields["DEPARTMENT_GROUP2"];
            $data["SCHOOL_CD2"][TEXT]           = $fields["SCHOOL_CD2"];
            $data["FACULTYCD2"][TEXT]           = $fields["FACULTYCD2"];
            $data["DEPARTMENTCD2"][TEXT]        = $fields["DEPARTMENTCD2"];
            $data["HOWTOEXAM2"][NUMBER]         = $fields["HOWTOEXAM2"];
        } elseif ($fields["COURSE_KIND"] == '2') {
            $data["JOBTYPE_LCD1"][TEXT]         = $fields["JOBTYPE_LCD1"];
            $data["JOBTYPE_MCD1"][TEXT]         = $fields["JOBTYPE_MCD1"];
            $data["JOBTYPE_SCD1"][TEXT]         = $fields["JOBTYPE_SCD1"];
            $data["JOBTYPE_SSCD1"][TEXT]        = $fields["JOBTYPE_SSCD1"];
            $data["WORK_AREA1"][TEXT]           = $fields["WORK_AREA1"];
            $data["INTRODUCTION_DIV1"][TEXT]    = $fields["INTRODUCTION_DIV1"];
            $data["JOBTYPE_LCD2"][TEXT]         = $fields["JOBTYPE_LCD2"];
            $data["JOBTYPE_MCD2"][TEXT]         = $fields["JOBTYPE_MCD2"];
            $data["JOBTYPE_SCD2"][TEXT]         = $fields["JOBTYPE_SCD2"];
            $data["JOBTYPE_SSCD2"][TEXT]        = $fields["JOBTYPE_SSCD2"];
            $data["WORK_AREA2"][TEXT]           = $fields["WORK_AREA2"];
            $data["INTRODUCTION_DIV2"][TEXT]    = $fields["INTRODUCTION_DIV2"];
        }

        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($insert == "insert") {
            $db->query(Query::insertSQL($data, "COURSE_HOPE_SS_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     ENTRYDATE = '".$entrydate."' ";
            $where .= " AND SCHREGNO = '".$model->schregno."' ";
            $where .= " AND SEQ = ".$seq." ";

            $db->query(Query::updateSQL($data, "COURSE_HOPE_SS_DAT", $where));
        }

        if ($fields["COURSE_KIND"] == '1') {
            $query  = " DELETE FROM ";
            $query .= "     COURSE_HOPE_DETAIL_SS_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTRYDATE   = '".$entrydate."' ";
            $query .= " AND SEQ         = ".$seq." ";
            $query .= " AND SCHREGNO    = '".$model->schregno."' ";
            $db->query($query);

            for ($i = 3; $i <= $model->kibouCnt; $i++) {
                $school_group = ($fields["SCHOOL_GROUP".$i]) ? $fields["SCHOOL_GROUP".$i] : $db->getOne(knje360bQuery::getCollegeGroup($db, $fields["SCHOOL_CD".$i]));
                if ($school_group) {
                    $data = array();
                    $data["ENTRYDATE"][TEXT]            = $entrydate;
                    $data["SEQ"][NUMBER]                = $seq;
                    $data["SCHREGNO"][TEXT]             = $model->schregno;
                    $data["HOPE_NUM"][NUMBER]           = $i;
                    $data["SCHOOL_GROUP"][TEXT]         = $school_group;
                    $data["FACULTY_GROUP"][TEXT]        = $fields["FACULTY_GROUP".$i];
                    $data["DEPARTMENT_GROUP"][TEXT]     = $fields["DEPARTMENT_GROUP".$i];
                    $data["SCHOOL_CD"][TEXT]            = $fields["SCHOOL_CD".$i];
                    $data["FACULTYCD"][TEXT]            = $fields["FACULTYCD".$i];
                    $data["DEPARTMENTCD"][TEXT]         = $fields["DEPARTMENTCD".$i];
                    $data["HOWTOEXAM"][NUMBER]          = $fields["HOWTOEXAM".$i];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "sysdate()";

                    $db->query(Query::insertSQL($data, "COURSE_HOPE_DETAIL_SS_DAT"));
                }
            }
        }

        return;
    }

    // UPDATE（進路調査・進学）一括更新
    public function &getRepUpdateQuery1($db, $model, $fields, $schregno)
    {
        $model->field["ENTRYDATE"] = $fields["ENTRYDATE"];
        $seq = (int)$db->getOne(knje360bQuery::getMaxSeq($db, $model, "COURSE_HOPE_SS_DAT")) + 1;
        $entrydate = str_replace("/", "-", $model->field["ENTRYDATE"]);

        $data["ENTRYDATE"][TEXT]            = $entrydate;
        $data["SEQ"][NUMBER]                = $seq;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["COURSE_KIND"][TEXT]          = $fields["COURSE_KIND"];
        $data["QUESTIONNAIRECD"][TEXT]      = $fields["QUESTIONNAIRECD"];
        $data["SCHOOL_GROUP1"][TEXT]        = ($fields["SCHOOL_GROUP1"]) ? $fields["SCHOOL_GROUP1"] : $db->getOne(knje360bQuery::getCollegeGroup($db, $fields["SCHOOL_CD1"]));
        $data["FACULTY_GROUP1"][TEXT]       = $fields["FACULTY_GROUP1"];
        $data["DEPARTMENT_GROUP1"][TEXT]    = $fields["DEPARTMENT_GROUP1"];
        $data["SCHOOL_CD1"][TEXT]           = $fields["SCHOOL_CD1"];
        $data["FACULTYCD1"][TEXT]           = $fields["FACULTYCD1"];
        $data["DEPARTMENTCD1"][TEXT]        = $fields["DEPARTMENTCD1"];
        $data["HOWTOEXAM1"][NUMBER]         = $fields["HOWTOEXAM1"];
        $data["SCHOOL_GROUP2"][TEXT]        = ($fields["SCHOOL_GROUP2"]) ? $fields["SCHOOL_GROUP2"] : $db->getOne(knje360bQuery::getCollegeGroup($db, $fields["SCHOOL_CD2"]));
        $data["FACULTY_GROUP2"][TEXT]       = $fields["FACULTY_GROUP2"];
        $data["DEPARTMENT_GROUP2"][TEXT]    = $fields["DEPARTMENT_GROUP2"];
        $data["SCHOOL_CD2"][TEXT]           = $fields["SCHOOL_CD2"];
        $data["FACULTYCD2"][TEXT]           = $fields["FACULTYCD2"];
        $data["DEPARTMENTCD2"][TEXT]        = $fields["DEPARTMENTCD2"];
        $data["HOWTOEXAM2"][NUMBER]         = $fields["HOWTOEXAM2"];
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "COURSE_HOPE_SS_DAT");
        return $query;
    }

    /***************受験報告（進学）***************/

    //受験報告データ（進学）取得
    public function getSubQuery2($model)
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
        $query .= "     D7.REMARK1 AS CHALLENGE_SEMI1, ";
        $query .= "     D7.REMARK2 AS CHALLENGE_SEMI2, ";
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
        if ($model->Properties["KNJE360B_SENKOUKOUMOKU"] == "1") {
            $query .= "     ,D6.REMARK1 AS SELECT_CATEGORY ";
            $query .= "     ,D6.REMARK2 AS SELECT_RESULT ";
            $query .= "     ,D6.REMARK3 AS DESIRED_RANK ";
        }
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_SS_DAT T1 ";
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
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D7 ON ";
        $query .= "             T1.YEAR         = D7.YEAR AND ";
        $query .= "             T1.SEQ          = D7.SEQ AND ";
        $query .= "             D7.DETAIL_SEQ   = 7 ";
        //選考、志望順位
        if ($model->Properties["KNJE360B_SENKOUKOUMOKU"] == "1") {
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
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEQ         = ".$model->seq." ";
        $query .= " AND T1.SCHREGNO    = '".$model->schregno."' ";
        $query .= " AND T1.SENKOU_KIND = '0' ";

        return $query;
    }

    //証明書番号取得
    public function getCertifNo($model, $seq)
    {
        if ($model->Properties["certif_no_8keta"] == "1") {
            $query  = " SELECT ";
            $query .= "     REMARK1 AS CERTIF_NO, ";
            $query .= "     CERTIF_INDEX ";
            $query .= " FROM ";
            $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR."' ";
            $query .= " AND REMARK13    = '".$seq."' ";
            $query .= " ORDER BY ";
            $query .= "     CERTIF_INDEX DESC ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.CERTIF_NO, ";
            $query .= "     T1.CERTIF_INDEX ";
            $query .= " FROM ";
            $query .= "     CERTIF_ISSUE_DAT T1, ";
            $query .= "     CERTIF_DETAIL_EACHTYPE_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = T2.YEAR ";
            $query .= " AND T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= " AND T1.CERTIF_INDEX = T2.CERTIF_INDEX ";
            $query .= " AND T2.REMARK13 = '".$seq."' ";
            $query .= " ORDER BY ";
            $query .= "     T1.CERTIF_INDEX DESC ";
        }

        return $query;
    }

    //証明書番号取得
    public function getCertifSchoolDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $setCertifKindCd = $model->mode == "grd" ? "025" : "008";
        $query .= " AND CERTIF_KINDCD    = '{$setCertifKindCd}' ";

        return $query;
    }

    //日程取得
    public function getPROGRAMCD($Row)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     PROGRAM_CD AS VALUE, ";
        $query .= "     PROGRAM_CD || '：' || PROGRAM_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_CALENDAR ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_CD       = '".$Row["SCHOOL_CD"]."' ";
        $query .= " AND FACULTYCD       = '".$Row["FACULTYCD"]."' ";
        $query .= " AND DEPARTMENTCD    = '".$Row["DEPARTMENTCD"]."' ";
        $query .= " AND ADVERTISE_DIV   = '".$Row["ADVERTISE_DIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //方式取得
    public function getFORMCD($Row)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     FORM_CD AS VALUE, ";
        $query .= "     FORM_CD || '：' || FORM_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_CALENDAR ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_CD       = '".$Row["SCHOOL_CD"]."' ";
        $query .= " AND FACULTYCD       = '".$Row["FACULTYCD"]."' ";
        $query .= " AND DEPARTMENTCD    = '".$Row["DEPARTMENTCD"]."' ";
        $query .= " AND ADVERTISE_DIV   = '".$Row["ADVERTISE_DIV"]."' ";
        $query .= " AND PROGRAM_CD      = '".$Row["PROGRAM_CD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //大分類取得
    public function getLCD($Row)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.L_CD1 AS VALUE, ";
        $query .= "     T1.L_CD1 || '：' || L1.L_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_CALENDAR T1 ";
        $query .= "     LEFT JOIN COLLEGE_EXAM_LDAT L1 ";
        $query .= "             ON T1.YEAR  = L1.YEAR ";
        $query .= "            AND T1.L_CD1 = L1.L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " AND T1.SCHOOL_CD        = '".$Row["SCHOOL_CD"]."' ";
        $query .= " AND T1.FACULTYCD        = '".$Row["FACULTYCD"]."' ";
        $query .= " AND T1.DEPARTMENTCD     = '".$Row["DEPARTMENTCD"]."' ";
        $query .= " AND T1.ADVERTISE_DIV    = '".$Row["ADVERTISE_DIV"]."' ";
        $query .= " AND T1.PROGRAM_CD       = '".$Row["PROGRAM_CD"]."' ";
        $query .= " AND T1.FORM_CD          = '".$Row["FORM_CD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //小分類取得
    public function getSCD($Row)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     S_CD AS VALUE, ";
        $query .= "     S_CD || '：' || S_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_CALENDAR ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_CD       = '".$Row["SCHOOL_CD"]."' ";
        $query .= " AND FACULTYCD       = '".$Row["FACULTYCD"]."' ";
        $query .= " AND DEPARTMENTCD    = '".$Row["DEPARTMENTCD"]."' ";
        $query .= " AND ADVERTISE_DIV   = '".$Row["ADVERTISE_DIV"]."' ";
        $query .= " AND PROGRAM_CD      = '".$Row["PROGRAM_CD"]."' ";
        $query .= " AND FORM_CD         = '".$Row["FORM_CD"]."' ";
        $query .= " AND L_CD1           = '".$Row["L_CD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //日付以下
    public function getLimit($Row)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLEGE_EXAM_CALENDAR ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_CD       = '".$Row["SCHOOL_CD"]."' ";
        $query .= " AND FACULTYCD       = '".$Row["FACULTYCD"]."' ";
        $query .= " AND DEPARTMENTCD    = '".$Row["DEPARTMENTCD"]."' ";
        $query .= " AND ADVERTISE_DIV   = '".$Row["ADVERTISE_DIV"]."' ";
        $query .= " AND PROGRAM_CD      = '".$Row["PROGRAM_CD"]."' ";
        $query .= " AND FORM_CD         = '".$Row["FORM_CD"]."' ";
        $query .= " AND L_CD1           = '".$Row["L_CD"]."' ";
        $query .= " AND S_CD            = '".$Row["S_CD"]."' ";

        return $query;
    }

    //東京都学校区分取得
    public function getSchoolCategoryCd()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHOOL_CATEGORY_CD AS VALUE, ";
        $query .= "     SCHOOL_CATEGORY_CD || '：' || CATEGORY_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_SCHOOL_CATEGORY_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //東京都大分類
    public function getTokyoLcd($schoolCategoryCd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     TOKYO_L_CD AS VALUE, ";
        $query .= "     TOKYO_L_CD || '：' || TOKYO_L_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_FACULTY_TOKYO_L_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CATEGORY_CD = '{$schoolCategoryCd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //東京都中分類
    public function getTokyoMcd($schoolCategoryCd, $tokyoLcd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     TOKYO_M_CD AS VALUE, ";
        $query .= "     TOKYO_M_CD || '：' || TOKYO_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_FACULTY_TOKYO_M_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_CATEGORY_CD = '{$schoolCategoryCd}' ";
        $query .= "     AND TOKYO_L_CD         = '{$tokyoLcd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ存在チェック
    public function checkAftGradCourseDetailDat($seq, $detail_seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= " AND SEQ         = ".$seq." ";
        $query .= " AND DETAIL_SEQ  = ".$detail_seq." ";

        return $query;
    }

    // UPDATE（受験報告・進学）
    public function &getUpdateQuery2($db, $model, $fields, $insert)
    {
        $seq = ($insert == "insert") ? (int)$db->getOne(knje360bQuery::getMaxSeq2($db))+1 : $model->seq;
        $school_group = $db->getOne(knje360bQuery::getSchoolGroup($db, $fields["SCHOOL_CD"]));
        $entrydate    = str_replace("/", "-", $fields["TOROKU_DATE"]);
        $prefcd       = explode('-', $fields["PREF_CD"]);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $seq;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["SENKOU_KIND"][TEXT]      = '0';
        $data["STAT_CD"][TEXT]          = $fields["SCHOOL_CD"];
        $data["SCHOOL_GROUP"][TEXT]     = $school_group;
        $data["FACULTYCD"][TEXT]        = $fields["FACULTYCD"];
        $data["DEPARTMENTCD"][TEXT]     = $fields["DEPARTMENTCD"];
        $data["PREF_CD"][TEXT]          = $prefcd[0];
        $data["CITY_CD"][TEXT]          = $prefcd[1];
        $data["TOROKU_DATE"][TEXT]      = $entrydate;
        $data["STAT_DATE1"][TEXT]       = str_replace("/", "-", $fields["STAT_DATE1"]);
        $data["STAT_DATE3"][TEXT]       = str_replace("/", "-", $fields["STAT_DATE3"]);
        $data["HOWTOEXAM"][TEXT]        = $fields["HOWTOEXAM"];
        $data["CONTENTEXAM"][TEXT]      = $fields["CONTENTEXAM"];
        $data["REASONEXAM"][TEXT]       = $fields["REASONEXAM"];
        $data["THINKEXAM"][TEXT]        = $fields["THINKEXAM"];
        $data["DECISION"][TEXT]         = $fields["DECISION"];
        $data["PLANSTAT"][TEXT]         = $fields["PLANSTAT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($insert == "insert") {
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_SS_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' ";
            $where .= " AND SEQ  = ".$seq." ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_SS_DAT", $where));
        }

        //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
        $cntD = $db->getOne(knje360bQuery::checkAftGradCourseDetailDat($seq, 1));

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $seq;
        $data["DETAIL_SEQ"][NUMBER]     = 1;
        $data["REMARK1"][TEXT]          = $fields["ADVERTISE_DIV"];
        $data["REMARK2"][TEXT]          = $fields["PROGRAM_CD"];
        $data["REMARK3"][TEXT]          = $fields["FORM_CD"];
        $data["REMARK4"][TEXT]          = $fields["L_CD"];
        $data["REMARK5"][TEXT]          = $fields["S_CD"];
        $data["REMARK6"][TEXT]          = str_replace("/", "-", $fields["LIMIT_DATE_WINDOW"]);
        $data["REMARK7"][TEXT]          = str_replace("/", "-", $fields["LIMIT_DATE_MAIL"]);
        $data["REMARK8"][TEXT]          = $fields["LIMIT_MAIL_DIV"];
        $data["REMARK9"][TEXT]          = $fields["EXAMNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cntD > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR       = '".CTRL_YEAR."' ";
            $where .= " AND SEQ        = ".$seq." ";
            $where .= " AND DETAIL_SEQ = 1 ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
        }

        //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
        $cntD3 = $db->getOne(knje360bQuery::checkAftGradCourseDetailDat($seq, 3));

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $seq;
        $data["DETAIL_SEQ"][NUMBER]     = 3;
        $data["REMARK1"][TEXT]          = $fields["SHDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cntD3 > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR       = '".CTRL_YEAR."' ";
            $where .= " AND SEQ        = ".$seq." ";
            $where .= " AND DETAIL_SEQ = 3 ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
        }

        if ($model->Properties["useCollegeSearch10Keta"] == "1") {
            //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
            $cntD = $db->getOne(knje360bQuery::checkAftGradCourseDetailDat($seq, 4));

            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEQ"][NUMBER]            = $seq;
            $data["DETAIL_SEQ"][NUMBER]     = 4;
            $data["REMARK1"][TEXT]          = $fields["SEARCH10"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            if ($cntD > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR       = '".CTRL_YEAR."' ";
                $where .= " AND SEQ        = ".$seq." ";
                $where .= " AND DETAIL_SEQ = 4 ";

                $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
            } else {
                $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
            }
        }

        //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
        $cntD = $db->getOne(knje360bQuery::checkAftGradCourseDetailDat($seq, 5));

        $data = array();
        if ($cntD == 0) {
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEQ"][NUMBER]            = $seq;
            $data["DETAIL_SEQ"][NUMBER]     = 5;
        }
        $data["REMARK1"][TEXT]          = $fields["TOKUMEI"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cntD > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR       = '".CTRL_YEAR."' ";
            $where .= " AND SEQ        = ".$seq." ";
            $where .= " AND DETAIL_SEQ = 5 ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
        }

        //選考、志望順位
        if ($model->Properties["KNJE360B_SENKOUKOUMOKU"] == "1") {
            //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
            $cntD = $db->getOne(knje360bQuery::checkAftGradCourseDetailDat($seq, 6));

            $data = array();
            if ($cntD == 0) {
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SEQ"][NUMBER]            = $seq;
                $data["DETAIL_SEQ"][NUMBER]     = 6;
            }
            $data["REMARK1"][TEXT]          = $fields["SELECT_CATEGORY"];
            $data["REMARK2"][TEXT]          = $fields["SELECT_RESULT"];
            $data["REMARK3"][TEXT]          = $fields["DESIRED_RANK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            if ($cntD > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR       = '".CTRL_YEAR."' ";
                $where .= " AND SEQ        = ".$seq." ";
                $where .= " AND DETAIL_SEQ = 6 ";

                $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
            } else {
                $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
            }
        }

        //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
        $cntD = $db->getOne(knje360bQuery::checkAftGradCourseDetailDat($seq, 7));

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $seq;
        $data["DETAIL_SEQ"][NUMBER]     = 7;
        $data["REMARK1"][TEXT]          = $fields["CHALLENGE_SEMI1"];
        $data["REMARK2"][TEXT]          = $fields["CHALLENGE_SEMI2"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cntD > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR       = '".CTRL_YEAR."' ";
            $where .= " AND SEQ        = ".$seq." ";
            $where .= " AND DETAIL_SEQ = 7 ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
        }

        //存在チェック -- CERTIF_DETAIL_EACHTYPE_DAT
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= " AND REMARK13    = '".$seq."' ";

        $cntC = $db->getOne($query);

        //調査書発行チェックボックスが"on"のとき処理する
        if ($fields["ISSUE"] == "1" && $cntC == 0) {
            //MAX証明書連番取得
            $query  = " SELECT ";
            $query .= "     COALESCE(MAX(INTEGER(CERTIF_INDEX)+1),1) AS INDEX ";
            $query .= " FROM ";
            $query .= "     CERTIF_ISSUE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $max_index = $db->getOne($query);

            if ($model->mode == "grd") {
                $certif_kindcd  = "025";
                $graduate_flg   = "1";
            } else {
                $certif_kindcd  = "008";
                $graduate_flg   = "0";
            }

            //追加 -- CERTIF_ISSUE_DAT
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["CERTIF_INDEX"][TEXT]     = $max_index;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["CERTIF_KINDCD"][TEXT]    = $certif_kindcd;
            $data["GRADUATE_FLG"][TEXT]     = $graduate_flg;
            $data["APPLYDATE"][TEXT]        = $entrydate;
            $data["ISSUECD"][TEXT]          = 0;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "CERTIF_ISSUE_DAT"));

            //追加 -- CERTIF_DETAIL_EACHTYPE_DAT
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["CERTIF_INDEX"][TEXT]     = $max_index;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["TYPE"][TEXT]             = '1';
            $data["REMARK2"][TEXT]          = STAFFCD; // 記載責任者
            $data["REMARK3"][TEXT]          = '1'; // 氏名漢字出力
            $data["REMARK4"][TEXT]          = '2'; // 未履修科目出力しない
            $data["REMARK5"][TEXT]          = '1'; // 履修のみ科目出力する
            $data["REMARK7"][TEXT]          = CTRL_DATE; // 証明日付
            $data["REMARK10"][TEXT]         = '1'; // 概評人数出力する
            $data["REMARK13"][TEXT]         = $seq;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT"));
        }

        //東京都集計用
        if ($model->Properties["useTokyotoShinroTyousasyo"] == "1") {
            //保護フラグ -- COLLEGE_FACULTY_SYSTEM_MST
            $setFacultycd = sprintf("%03d", $fields["FACULTYCD"]);
            $query = knje360bQuery::getCollegeFacultySystemInfo("PROTECTION_FLG", $fields["SCHOOL_CD"], $setFacultycd);
            $protectionFlg = $db->getOne($query);

            if ($protectionFlg != "1") { // フラグ"1"は裏から作成したもの。更新はしない。
                //存在チェック -- COLLEGE_FACULTY_SYSTEM_MST
                $query = knje360bQuery::getCollegeFacultySystemInfo("COUNT(*)", $fields["SCHOOL_CD"], $setFacultycd);
                $cntSystemDat = $db->getOne($query);

                $data = array();
                $data["SCHOOL_CD"][TEXT]            = $fields["SCHOOL_CD"];
                $data["FACULTYCD"][TEXT]            = $setFacultycd;
                $data["SCHOOL_CATEGORY_CD"][TEXT]   = $fields["SCHOOL_CATEGORY_CD"];
                $data["TOKYO_L_CD"][TEXT]           = $fields["TOKYO_L_CD"];
                $data["TOKYO_M_CD"][TEXT]           = $fields["TOKYO_M_CD"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                if ($cntSystemDat > 0) {
                    $where  = " WHERE ";
                    $where .= "     SCHOOL_CD = '".$fields["SCHOOL_CD"]."' ";
                    $where .= " AND FACULTYCD = '".$setFacultycd."' ";

                    $db->query(Query::updateSQL($data, "COLLEGE_FACULTY_SYSTEM_MST", $where));
                } else {
                    $db->query(Query::insertSQL($data, "COLLEGE_FACULTY_SYSTEM_MST"));
                }
            }
        }

        return;
    }

    //COLLEGE_FACULTY_SYSTEM_MST取得
    public function getCollegeFacultySystemInfo($field, $schoolCd, $facultyCd)
    {
        $query  = " SELECT ";
        $query .= "     {$field} ";
        $query .= " FROM ";
        $query .= "     COLLEGE_FACULTY_SYSTEM_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_CD = '{$schoolCd}' ";
        $query .= "     AND FACULTYCD = '{$facultyCd}' ";

        return $query;
    }

    //学校系列取得
    public function getSchoolGroup($db, $schoolcd)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_GROUP ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '".$schoolcd."' ";

        return $query;
    }

    /***************受験報告（進学）一括更新画面***************/

    //年組取得
    public function getGradeHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER    = '".CTRL_SEMESTER."' ";
        $query .= " AND GRADE       = '".$model->grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧取得
    public function getStudent($model, $flg, $sch_array)
    {
        $query  = " SELECT ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '_' || REGD.SCHREGNO AS VALUE, ";
        $query .= "     VALUE(T3.HR_NAMEABBV,'') || '-' || RTRIM(CHAR(INT(REGD.ATTENDNO))) || ' ' || T2.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON REGD.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "              ON REGD.YEAR       = T3.YEAR ";
        $query .= "             AND REGD.SEMESTER   = T3.SEMESTER ";
        $query .= "             AND REGD.GRADE      = T3.GRADE ";
        $query .= "             AND REGD.HR_CLASS   = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR       = '".CTRL_YEAR."' ";
        $query .= " AND REGD.SEMESTER   = '".CTRL_SEMESTER."' ";
        if ($flg == "left") {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '_' || REGD.SCHREGNO IN ('".implode("','", $sch_array)."') ";
        } else {
            $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS = '".$model->replace["ghr"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // UPDATE（受験報告・進学）
    public function &getRepUpdateQuery2($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $sch_array = explode(",", $this->replace["selectdata"]);
        for ($i = 0; $i < get_count($sch_array); $i++) {
            list($gha, $schregno) = preg_split("/_/", $sch_array[$i]);
            $seq = (int)$db->getOne(knje360bQuery::getMaxSeq2($db))+1;
            $school_group = $db->getOne(knje360bQuery::getSchoolGroup($db, $fields["SCHOOL_CD"]));
            $entrydate = str_replace("/", "-", $fields["TOROKU_DATE"]);
            $prefcd = explode('-', $fields["PREF_CD"]);

            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEQ"][NUMBER]            = $seq;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["SENKOU_KIND"][TEXT]      = '0';
            $data["STAT_CD"][TEXT]          = $fields["SCHOOL_CD"];
            $data["SCHOOL_GROUP"][TEXT]     = $school_group;
            $data["FACULTYCD"][TEXT]        = $fields["FACULTYCD"];
            $data["DEPARTMENTCD"][TEXT]     = $fields["DEPARTMENTCD"];
            $data["PREF_CD"][TEXT]          = $prefcd[0];
            $data["CITY_CD"][TEXT]          = $prefcd[1];
            $data["TOROKU_DATE"][TEXT]      = $entrydate;
            $data["HOWTOEXAM"][TEXT]        = $fields["HOWTOEXAM"];
            $data["DECISION"][TEXT]         = $fields["DECISION"];
            $data["PLANSTAT"][TEXT]         = $fields["PLANSTAT"];
            $data["STAT_DATE1"][TEXT]       = str_replace("/", "-", $fields["STAT_DATE1"]);
            $data["STAT_DATE3"][TEXT]       = str_replace("/", "-", $fields["STAT_DATE3"]);
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_SS_DAT"));

            //追加 -- AFT_GRAD_COURSE_DETAIL_DAT
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEQ"][NUMBER]            = $seq;
            $data["DETAIL_SEQ"][NUMBER]     = 5;
            $data["REMARK1"][TEXT]          = $fields["TOKUMEI"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));

            //存在チェック -- CERTIF_DETAIL_EACHTYPE_DAT
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR."' ";
            $query .= " AND REMARK13    = '".$seq."' ";

            $cntC = $db->getOne($query);

            //調査書発行チェックボックスが"on"のとき処理する
            if ($fields["ISSUE"] == "1" && $cntC == 0) {
                //MAX証明書連番取得
                $query  = " SELECT ";
                $query .= "     COALESCE(MAX(INTEGER(CERTIF_INDEX)+1),1) AS INDEX ";
                $query .= " FROM ";
                $query .= "     CERTIF_ISSUE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '".CTRL_YEAR."' ";
                $max_index = $db->getOne($query);

                if ($model->mode == "grd") {
                    $certif_kindcd  = "025";
                    $graduate_flg   = "1";
                } else {
                    $certif_kindcd  = "008";
                    $graduate_flg   = "0";
                }

                //追加 -- CERTIF_ISSUE_DAT
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["CERTIF_INDEX"][TEXT]     = $max_index;
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["CERTIF_KINDCD"][TEXT]    = $certif_kindcd;
                $data["GRADUATE_FLG"][TEXT]     = $graduate_flg;
                $data["APPLYDATE"][TEXT]        = $entrydate;
                $data["ISSUECD"][TEXT]          = 0;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $db->query(Query::insertSQL($data, "CERTIF_ISSUE_DAT"));

                //追加 -- CERTIF_DETAIL_EACHTYPE_DAT
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["CERTIF_INDEX"][TEXT]     = $max_index;
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["TYPE"][TEXT]             = '1';
                $data["REMARK2"][TEXT]          = STAFFCD; // 記載責任者
                $data["REMARK3"][TEXT]          = '1'; // 氏名漢字出力
                $data["REMARK4"][TEXT]          = '2'; // 未履修科目出力しない
                $data["REMARK5"][TEXT]          = '1'; // 履修のみ科目出力する
                $data["REMARK7"][TEXT]          = CTRL_DATE; // 証明日付
                $data["REMARK10"][TEXT]         = '1'; // 概評人数出力する
                $data["REMARK13"][TEXT]         = $seq;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $db->query(Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT"));
            }
        }
        //東京都集計用
        if ($model->Properties["useTokyotoShinroTyousasyo"] == "1") {
            //保護フラグ -- COLLEGE_FACULTY_SYSTEM_MST
            $query = knje360bQuery::getCollegeFacultySystemInfo("PROTECTION_FLG", $fields["SCHOOL_CD"], $fields["FACULTYCD"]);
            $protectionFlg = $db->getOne($query);

            if ($protectionFlg != "1") { // フラグ"1"は裏から作成したもの。更新はしない。
                //存在チェック -- COLLEGE_FACULTY_SYSTEM_MST
                $query = knje360bQuery::getCollegeFacultySystemInfo("COUNT(*)", $fields["SCHOOL_CD"], $fields["FACULTYCD"]);
                $cntSystemDat = $db->getOne($query);

                $data = array();
                $data["SCHOOL_CD"][TEXT]            = $fields["SCHOOL_CD"];
                $data["FACULTYCD"][TEXT]            = $fields["FACULTYCD"];
                $data["SCHOOL_CATEGORY_CD"][TEXT]   = $fields["SCHOOL_CATEGORY_CD"];
                $data["TOKYO_L_CD"][TEXT]           = $fields["TOKYO_L_CD"];
                $data["TOKYO_M_CD"][TEXT]           = $fields["TOKYO_M_CD"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                if ($cntSystemDat > 0) {
                    $where  = " WHERE ";
                    $where .= "     SCHOOL_CD = '".$fields["SCHOOL_CD"]."' ";
                    $where .= " AND FACULTYCD = '".$fields["FACULTYCD"]."' ";

                    $db->query(Query::updateSQL($data, "COLLEGE_FACULTY_SYSTEM_MST", $where));
                } else {
                    $db->query(Query::insertSQL($data, "COLLEGE_FACULTY_SYSTEM_MST"));
                }
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    /***************受験報告（就職）***************/

    //受験報告データ（就職）取得
    public function getSubQuery3($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.TOROKU_DATE, ";
        $query .= "     T1.SENKOU_NO, ";
        $query .= "     T2.COMPANY_CD AS STAT_CD, ";
        $query .= "     T2.COMPANY_NAME AS STAT_NAME, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.ADDR1, ";
        $query .= "     T2.ADDR2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.INDUSTRY_LCD, ";
        $query .= "     T2.INDUSTRY_MCD, ";
        $query .= "     T3.INDUSTRY_MNAME, ";
        $query .= "     T1.JOBTYPE_LCD  , ";
        $query .= "     T1.JOBTYPE_MCD, ";
        $query .= "     T1.JOBTYPE_SCD, ";
        $query .= "     T1.JOBTYPE_SSCD, ";
        $query .= "     T1.PREF_CD, ";
        $query .= "     T1.CITY_CD, ";
        $query .= "     T1.DECISION, ";
        $query .= "     T1.PLANSTAT, ";
        $query .= "     T1.INTRODUCTION_DIV, ";
        $query .= "     T1.JOB_THINK, ";
        $query .= "     T5.REMARK3 AS REASON1, ";
        $query .= "     T5.REMARK4 AS REASON2, ";
        $query .= "     T5.REMARK1 AS CHALLENGE_SEMI1, ";
        $query .= "     T5.REMARK2 AS CHALLENGE_SEMI2, ";
        $query .= "     T4.REMARK1, ";
        $query .= "     T4.REMARK2 ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_SS_DAT T1 ";
        $query .= "     LEFT JOIN COMPANY_MST T2 ON T1.STAT_CD = T2.COMPANY_CD ";
        $query .= "     LEFT JOIN INDUSTRY_M_MST T3 ON T2.INDUSTRY_LCD = T3.INDUSTRY_LCD AND T2.INDUSTRY_MCD = T3.INDUSTRY_MCD ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT T4 ON T1.YEAR = T4.YEAR AND T1.SEQ = T4.SEQ AND T4.DETAIL_SEQ = 2 ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT T5 ON T1.YEAR = T5.YEAR AND T1.SEQ = T5.SEQ AND T5.DETAIL_SEQ = 7 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEQ      = ".$model->seq." ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    // １レコード取得(会社マスタ)
    public function &getCollegeOrCompanyMst($stat_cd)
    {
        $query  = " SELECT ";
        $query .= "     T1.COMPANY_CD AS STAT_CD, ";
        $query .= "     T1.COMPANY_NAME AS STAT_NAME, ";
        $query .= "     T1.ZIPCD, ";
        $query .= "     T1.ADDR1, ";
        $query .= "     T1.ADDR2, ";
        $query .= "     T1.TELNO, ";
        $query .= "     T1.INDUSTRY_LCD, ";
        $query .= "     T1.INDUSTRY_MCD, ";
        $query .= "     T2.INDUSTRY_MNAME ";
        $query .= " FROM ";
        $query .= "     COMPANY_MST T1 ";
        $query .= "     LEFT JOIN INDUSTRY_M_MST T2 ON   ";
        $query .= "         T1.INDUSTRY_LCD = T2.INDUSTRY_LCD AND  ";
        $query .= "         T1.INDUSTRY_MCD = T2.INDUSTRY_MCD  ";
        $query .= " WHERE ";
        $query .= "     COMPANY_CD = '$stat_cd'";

        return $query;
    }

    // UPDATE（受験報告・就職）
    public function &getUpdateQuery3($db, $model, $fields, $insert)
    {
        $seq = ($insert == "insert") ? (int)$db->getOne(knje360bQuery::getMaxSeq2($db))+1 : $model->seq;
        $entrydate = str_replace("/", "-", $fields["TOROKU_DATE"]);
        $prefcd  = explode('-', $fields["PREF_CD"]);
        $jobtype = explode('-', $fields["JOBTYPE_SCD"]);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $seq;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["SENKOU_KIND"][TEXT]      = '1';
        $data["STAT_CD"][TEXT]          = $fields["STAT_CD"];
        $data["TOROKU_DATE"][TEXT]      = $entrydate;
        $data["SENKOU_NO"][NUMBER]      = $fields["SENKOU_NO"];
        $data["JOBTYPE_LCD"][TEXT]      = $fields["JOBTYPE_LCD"];
        $data["JOBTYPE_MCD"][TEXT]      = $fields["JOBTYPE_MCD"];
        $data["JOBTYPE_SCD"][TEXT]      = $jobtype[0];
        $data["JOBTYPE_SSCD"][TEXT]      = $jobtype[1];
        $data["PREF_CD"][TEXT]          = $prefcd[0];
        $data["CITY_CD"][TEXT]          = $prefcd[1];
        $data["DECISION"][TEXT]         = $fields["DECISION"];
        $data["PLANSTAT"][TEXT]         = $fields["PLANSTAT"];
        $data["INTRODUCTION_DIV"][TEXT] = $fields["INTRODUCTION_DIV"];
        $data["JOB_THINK"][TEXT]        = $fields["JOB_THINK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($insert == "insert") {
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_SS_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' ";
            $where .= " AND SEQ  = ".$seq." ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_SS_DAT", $where));
        }

        //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
        $cntD = $db->getOne(knje360bQuery::checkAftGradCourseDetailDat($seq, 2));

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $seq;
        $data["DETAIL_SEQ"][NUMBER]     = 2;
        $data["REMARK1"][TEXT]          = $fields["REMARK1"];
        $data["REMARK2"][TEXT]          = $fields["REMARK2"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cntD > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR       = '".CTRL_YEAR."' ";
            $where .= " AND SEQ        = ".$seq." ";
            $where .= " AND DETAIL_SEQ = 2 ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
        }

        //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
        $cntD = $db->getOne(knje360bQuery::checkAftGradCourseDetailDat($seq, 7));

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $seq;
        $data["DETAIL_SEQ"][NUMBER]     = 7;
        $data["REMARK1"][TEXT]          = $fields["CHALLENGE_SEMI1"];
        $data["REMARK2"][TEXT]          = $fields["CHALLENGE_SEMI2"];
        $data["REMARK3"][TEXT]          = $fields["REASON1"];
        $data["REMARK4"][TEXT]          = $fields["REASON2"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cntD > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR       = '".CTRL_YEAR."' ";
            $where .= " AND SEQ        = ".$seq." ";
            $where .= " AND DETAIL_SEQ = 7 ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
        }

        return;
    }

    /***************進路相談***************/

    //進路相談データ取得
    public function getSubQuery4($model, $list = "")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COURSE_COUNSELING_DAT ";
        $query .= " WHERE ";
        if ($list == "") {
            $query .= "     ENTRYDATE = '".$model->entrydate."' AND ";
            $query .= "     SEQ = ".$model->seq." AND ";
        }
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     ENTRYDATE DESC, ";
        $query .= "     SEQ DESC ";

        return $query;
    }

    //職員名取得
    public function getStaffName($staffcd)
    {
        $query  = " SELECT ";
        $query .= "     STAFFCD || '　' || STAFFNAME AS STAFFNAME ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND STAFFCD = '".$staffcd."' ";

        return $query;
    }

    // UPDATE（進路相談）
    public function &getUpdateQuery4($db, $model, $fields, $insert)
    {
        $seq = ($insert == "insert") ? (int)$db->getOne(knje360bQuery::getMaxSeq($db, $model, "COURSE_COUNSELING_DAT"))+1 : $model->seq;
        $entrydate = str_replace("/", "-", $fields["ENTRYDATE"]);

        $data["ENTRYDATE"][TEXT]    = $entrydate;
        $data["SEQ"][NUMBER]        = $seq;
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["TITLE"][TEXT]        = $fields["TITLE"];
        $data["STAFFCD"][TEXT]      = STAFFCD;
        $data["CONTENTS"][TEXT]     = $fields["CONTENTS"];
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        if ($insert == "insert") {
            $db->query(Query::insertSQL($data, "COURSE_COUNSELING_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     ENTRYDATE = '".$entrydate."' ";
            $where .= " AND SCHREGNO  = '".$model->schregno."' ";
            $where .= " AND SEQ       = ".$seq." ";

            $db->query(Query::updateSQL($data, "COURSE_COUNSELING_DAT", $where));
        }
        return;
    }

    /***************その他***************/

    //その他進路の表示設定
    public function getHyoujiset()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        return $query;
    }

    //その他情報取得
    public function getSubQuery5($model)
    {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     SCHREGNO, ";
        if ($model->getname === 'kyoto') {
            $query .= "     SENKOU_KIND_SUB, ";
        }
        $query .= "     TOROKU_DATE ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_SS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' ";
        $query .= " AND SEQ      = ".$model->seq." ";
        $query .= " AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    // UPDATE（その他）
    public function &getUpdateQuery5($db, $model, $fields, $insert)
    {
        $seq = ($insert == "insert") ? (int)$db->getOne(knje360bQuery::getMaxSeq2($db))+1 : $model->seq;
        $entrydate = str_replace("/", "-", $fields["TOROKU_DATE"]);
        $prefcd = explode('-', $fields["PREF_CD"]);

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $seq;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["SENKOU_KIND"][TEXT]      = '2';
        if ($model->getname === 'kyoto') {
            $data["SENKOU_KIND_SUB"][TEXT]  = $fields["SENKOU_KIND_SUB"];
        }
        $data["TOROKU_DATE"][TEXT]      = $entrydate;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($insert == "insert") {
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_SS_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' ";
            $where .= " AND SEQ  = ".$seq." ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_SS_DAT", $where));
        }
        return;
    }

    //入力した求人番号の会社情報データ取得（PDF参照ボタン押下後）
    public function getJobOfferList($model)
    {
        $query .= " SELECT ";
        $query .= "     VALUE(L1.COMPANY_CD, '　') AS STAT_CD, ";
        $query .= "     VALUE(L1.COMPANY_NAME, '　') AS STAT_NAME, ";
        $query .= "     VALUE(L1.ZIPCD, '　') AS ZIPCD, ";
        $query .= "     VALUE(L1.ADDR1, '　') AS ADDR1, ";
        $query .= "     VALUE(L1.ADDR2, '　') AS ADDR2, ";
        $query .= "     VALUE(L1.TELNO, '　') AS TELNO, ";
        $query .= "     VALUE(L2.INDUSTRY_MNAME, '　') AS INDUSTRY_MNAME ";
        $query .= " FROM ";
        $query .= "     JOB_OFFER_SS_DAT T1 ";
        $query .= "     LEFT JOIN COMPANY_MST L1 ON T1.COMPANY_CD = L1.COMPANY_CD ";
        $query .= "     LEFT JOIN INDUSTRY_M_MST L2 ON L1.INDUSTRY_LCD = L2.INDUSTRY_LCD ";
        $query .= "                                AND L1.INDUSTRY_MCD = L2.INDUSTRY_MCD ";
        $query .= " WHERE ";
        $query .= "         SENKOU_NO = ".$model->field["SENKOU_NO"]." ";

        return $query;
    }

    /***************受験報告（進学・一括入力）***************/

    //受験報告データ（進学）取得
    public function getSubQuery6($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.TOROKU_DATE, ";
        $query .= "     T2.SCHOOL_CD, ";
        $query .= "     T2.SCHOOL_NAME_SHOW1 AS SCHOOL_NAME, ";
        $query .= "     T1.FACULTYCD, ";
        $query .= "     T3.FACULTYNAME_SHOW1 AS FACULTYNAME, ";
        $query .= "     T1.DEPARTMENTCD, ";
        $query .= "     T4.DEPARTMENTNAME_SHOW1 AS DEPARTMENTNAME, ";
        $query .= "     T1.STAT_DATE1, ";
        $query .= "     T1.STAT_DATE3, ";
        $query .= "     T1.HOWTOEXAM, ";
        $query .= "     D3.REMARK1 AS SHDIV, ";
        $query .= "     T1.CONTENTEXAM, ";
        $query .= "     T1.REASONEXAM, ";
        $query .= "     T1.DECISION, ";
        $query .= "     D5.REMARK1 AS TOKUMEI, ";
        $query .= "     CASE WHEN VALUE(E1.REMARK13,'') = '' THEN '' ELSE '1' END AS ISSUE, ";
        $query .= "     T1.PLANSTAT, ";
        //入試カレンダーの使用
        if ($model->Properties["useCollegeExamCalendar"] == "1") {
            $query .= "     D1.REMARK1 AS ADVERTISE_DIV, ";
            $query .= "     N1.NAME1 AS ADVERTISE_NAME, ";
            $query .= "     D1.REMARK2 AS PROGRAM_CD, ";
            $query .= "     C1.PROGRAM_NAME, ";
            $query .= "     D1.REMARK3 AS FORM_CD, ";
            $query .= "     C2.FORM_NAME, ";
            $query .= "     D1.REMARK4 AS L_CD, ";
            $query .= "     L2.L_NAME, ";
            $query .= "     D1.REMARK5 AS S_CD, ";
            $query .= "     C3.S_NAME, ";
        }
        $query .= "     D1.REMARK6 AS LIMIT_DATE_WINDOW, ";
        $query .= "     D1.REMARK7 AS LIMIT_DATE_MAIL, ";
        $query .= "     D1.REMARK8 AS LIMIT_MAIL_DIV, ";
        $query .= "     D1.REMARK9 AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_SS_DAT T1 ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D1 ON ";
        $query .= "             T1.YEAR         = D1.YEAR AND ";
        $query .= "             T1.SEQ          = D1.SEQ AND ";
        $query .= "             D1.DETAIL_SEQ   = 1 ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D3 ON ";
        $query .= "             T1.YEAR         = D3.YEAR AND ";
        $query .= "             T1.SEQ          = D3.SEQ AND ";
        $query .= "             D3.DETAIL_SEQ   = 3 ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D5 ON ";
        $query .= "             T1.YEAR         = D5.YEAR AND ";
        $query .= "             T1.SEQ          = D5.SEQ AND ";
        $query .= "             D5.DETAIL_SEQ   = 5 ";
        $query .= "     LEFT JOIN COLLEGE_MST T2 ON T1.STAT_CD = T2.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST T3 ON ";
        $query .= "             T1.STAT_CD      = T3.SCHOOL_CD AND ";
        $query .= "             T1.FACULTYCD    = T3.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST T4 ON ";
        $query .= "             T1.STAT_CD      = T4.SCHOOL_CD AND ";
        $query .= "             T1.FACULTYCD    = T4.FACULTYCD AND ";
        $query .= "             T1.DEPARTMENTCD = T4.DEPARTMENTCD ";
        $query .= "     LEFT JOIN COLLEGE_EXAM_LDAT L2 ON ";
        $query .= "             L2.L_CD = D1.REMARK4 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON ";
        $query .= "             N1.NAMECD1 = 'E044' AND ";
        $query .= "             N1.NAMECD2 = D1.REMARK1 ";
        //入試カレンダーの使用
        if ($model->Properties["useCollegeExamCalendar"] == "1") {
            $query .= "     LEFT JOIN COLLEGE_EXAM_CALENDAR C1 ON ";
            $query .= "             C1.YEAR             = T1.YEAR AND ";
            $query .= "             C1.SCHOOL_CD        = T1.STAT_CD AND ";
            $query .= "             C1.FACULTYCD        = T1.FACULTYCD AND ";
            $query .= "             C1.DEPARTMENTCD     = T1.DEPARTMENTCD AND ";
            $query .= "             C1.ADVERTISE_DIV    = D1.REMARK1 AND ";
            $query .= "             C1.PROGRAM_CD       = D1.REMARK2 ";
            $query .= "     LEFT JOIN COLLEGE_EXAM_CALENDAR C2 ON ";
            $query .= "             C2.YEAR             = T1.YEAR AND ";
            $query .= "             C2.SCHOOL_CD        = T1.STAT_CD AND ";
            $query .= "             C2.FACULTYCD        = T1.FACULTYCD AND ";
            $query .= "             C2.DEPARTMENTCD     = T1.DEPARTMENTCD AND ";
            $query .= "             C2.ADVERTISE_DIV    = D1.REMARK1 AND ";
            $query .= "             C2.PROGRAM_CD       = D1.REMARK2 AND ";
            $query .= "             C2.FORM_CD          = D1.REMARK3 ";
            $query .= "     LEFT JOIN COLLEGE_EXAM_CALENDAR C3 ON ";
            $query .= "             C3.YEAR             = T1.YEAR AND ";
            $query .= "             C3.SCHOOL_CD        = T1.STAT_CD AND ";
            $query .= "             C3.FACULTYCD        = T1.FACULTYCD AND ";
            $query .= "             C3.DEPARTMENTCD     = T1.DEPARTMENTCD AND ";
            $query .= "             C3.ADVERTISE_DIV    = D1.REMARK1 AND ";
            $query .= "             C3.PROGRAM_CD       = D1.REMARK2 AND ";
            $query .= "             C3.FORM_CD          = D1.REMARK3 AND ";
            $query .= "             C3.L_CD1            = D1.REMARK4 AND ";
            $query .= "             C3.S_CD             = D1.REMARK5 ";
        }
        $query .= "     LEFT JOIN CERTIF_DETAIL_EACHTYPE_DAT E1 ON ";
        $query .= "             T1.YEAR             = E1.YEAR AND ";
        $query .= "             T1.SEQ              = INT(E1.REMARK13) AND ";
        $query .= "             E1.TYPE             = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= " AND T1.SCHREGNO     = '".$model->schregno."' ";
        $query .= " AND T1.SENKOU_KIND  = '0' ";
        $query .= " ORDER BY ";
        $query .= "     T2.SCHOOL_CD, ";
        $query .= "     T1.FACULTYCD, ";
        $query .= "     T1.DEPARTMENTCD, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    // UPDATE（受験報告・進学・一括入力）
    public function &getUpdateQuery6($db, $model, $fields)
    {
        $db->autoCommit(false);

        $seq_array = preg_split("/,/", $this->seq_list);
        foreach ($seq_array as $seq) {
            $data = array();
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["SENKOU_KIND"][TEXT]      = '0';
            $data["TOROKU_DATE"][TEXT]      = str_replace("/", "-", $fields["TOROKU_DATE"][$seq]);
            $data["STAT_DATE1"][TEXT]       = str_replace("/", "-", $fields["STAT_DATE1"][$seq]);
            $data["STAT_DATE3"][TEXT]       = str_replace("/", "-", $fields["STAT_DATE3"][$seq]);
            $data["HOWTOEXAM"][TEXT]        = $fields["HOWTOEXAM"][$seq];
            $data["CONTENTEXAM"][TEXT]      = $fields["CONTENTEXAM"][$seq];
            $data["REASONEXAM"][TEXT]       = $fields["REASONEXAM"][$seq];
            $data["DECISION"][TEXT]         = $fields["DECISION"][$seq];
            $data["PLANSTAT"][TEXT]         = $fields["PLANSTAT"][$seq];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' ";
            $where .= " AND SEQ  = ".$seq." ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_SS_DAT", $where));

            //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
            $cntD = $db->getOne(knje360bQuery::checkAftGradCourseDetailDat($seq, 1));

            $data = array();
            if ($cntD == 0) {
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SEQ"][NUMBER]            = $seq;
                $data["DETAIL_SEQ"][NUMBER]     = 1;
            }
            $data["REMARK1"][TEXT]          = $fields["ADVERTISE_DIV"][$seq];
            $data["REMARK2"][TEXT]          = $fields["PROGRAM_CD"][$seq];
            $data["REMARK3"][TEXT]          = $fields["FORM_CD"][$seq];
            $data["REMARK4"][TEXT]          = $fields["L_CD"][$seq];
            $data["REMARK5"][TEXT]          = $fields["S_CD"][$seq];
            $data["REMARK6"][TEXT]          = str_replace("/", "-", $fields["LIMIT_DATE_WINDOW"][$seq]);
            $data["REMARK7"][TEXT]          = str_replace("/", "-", $fields["LIMIT_DATE_MAIL"][$seq]);
            $data["REMARK8"][TEXT]          = $fields["LIMIT_MAIL_DIV"][$seq];
            $data["REMARK9"][TEXT]          = $fields["EXAMNO"][$seq];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            if ($cntD > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR       = '".CTRL_YEAR."' ";
                $where .= " AND SEQ        = ".$seq." ";
                $where .= " AND DETAIL_SEQ = 1 ";

                $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
            } else {
                $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
            }

            //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
            $query = knje360bQuery::checkAftGradCourseDetailDat($seq, 3);

            $cntD3 = $db->getOne($query);

            $data = array();
            if ($cntD3 == 0) {
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SEQ"][NUMBER]            = $seq;
                $data["DETAIL_SEQ"][NUMBER]     = 3;
            }
            $data["REMARK1"][TEXT]          = $fields["SHDIV"][$seq];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            if ($cntD3 > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR       = '".CTRL_YEAR."' ";
                $where .= " AND SEQ        = ".$seq." ";
                $where .= " AND DETAIL_SEQ = 3 ";

                $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
            } else {
                $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
            }

            //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
            $query = knje360bQuery::checkAftGradCourseDetailDat($seq, 5);

            $cntD5 = $db->getOne($query);

            $data = array();
            if ($cntD5 == 0) {
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SEQ"][NUMBER]            = $seq;
                $data["DETAIL_SEQ"][NUMBER]     = 5;
            }
            $data["REMARK1"][TEXT]          = $fields["TOKUMEI"][$seq];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            if ($cntD5 > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR       = '".CTRL_YEAR."' ";
                $where .= " AND SEQ        = ".$seq." ";
                $where .= " AND DETAIL_SEQ = 5 ";

                $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
            } else {
                $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
            }

            //存在チェック -- CERTIF_DETAIL_EACHTYPE_DAT
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR."' ";
            $query .= " AND REMARK13    = '".$seq."' ";

            $cntC = $db->getOne($query);

            //調査書発行チェックボックスが"on"のとき処理する
            if ($fields["ISSUE"][$seq] == "1" && $cntC == 0) {
                //MAX証明書連番取得
                $query  = " SELECT ";
                $query .= "     COALESCE(MAX(INTEGER(CERTIF_INDEX)+1),1) AS INDEX ";
                $query .= " FROM ";
                $query .= "     CERTIF_ISSUE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '".CTRL_YEAR."' ";
                $max_index = $db->getOne($query);

                if ($model->mode == "grd") {
                    $certif_kindcd  = "025";
                    $graduate_flg   = "1";
                } else {
                    $certif_kindcd  = "008";
                    $graduate_flg   = "0";
                }

                //追加 -- CERTIF_ISSUE_DAT
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["CERTIF_INDEX"][TEXT]     = $max_index;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["CERTIF_KINDCD"][TEXT]    = $certif_kindcd;
                $data["GRADUATE_FLG"][TEXT]     = $graduate_flg;
                $data["APPLYDATE"][TEXT]        = str_replace("/", "-", $fields["TOROKU_DATE"][$seq]);
                $data["ISSUECD"][TEXT]          = 0;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $db->query(Query::insertSQL($data, "CERTIF_ISSUE_DAT"));

                //追加 -- CERTIF_DETAIL_EACHTYPE_DAT
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["CERTIF_INDEX"][TEXT]     = $max_index;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["TYPE"][TEXT]             = '1';
                $data["REMARK2"][TEXT]          = STAFFCD; // 記載責任者
                $data["REMARK3"][TEXT]          = '1'; // 氏名漢字出力
                $data["REMARK4"][TEXT]          = '2'; // 未履修科目出力しない
                $data["REMARK5"][TEXT]          = '1'; // 履修のみ科目出力する
                $data["REMARK7"][TEXT]          = CTRL_DATE; // 証明日付
                $data["REMARK10"][TEXT]         = '1'; // 概評人数出力する
                $data["REMARK13"][TEXT]         = $seq;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $db->query(Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT"));
            }
        }

        $db->commit();

        return;
    }

    //受験報告データ（進学）取得（一括）
    public function getSubQuery62($model)
    {
        if ($model->field["SEARCH_DIV"] == "1") {//学校名検索
            //学校名を全角スペースで分割
            $school_name = explode('　', $model->replace["field"]["SEARCH_TXT"]);

            $query  = "";
            for ($i = 0; $i < get_count($school_name); $i++) {
                if ($school_name[$i] == "") {
                    continue;
                }
                if ($query != "") {
                    $query .= " UNION ";
                }
                $query .= " SELECT DISTINCT ";
                $query .= "     T2.SCHOOL_CD, ";
                $query .= "     T3.FACULTYCD, ";
                $query .= "     T4.DEPARTMENTCD, ";
                $query .= "     T1.TOROKU_DATE, ";
                $query .= "     T1.STAT_DATE1 AS EXAM_DATE, ";
                $query .= "     T1.STAT_DATE3 AS EXAM_PASS_DATE, ";
                $query .= "     T1.PREF_CD, ";
                $query .= "     D1.REMARK1 AS ADVERTISE_DIV, ";
                $query .= "     D1.REMARK2 AS PROGRAM_CD, ";
                $query .= "     D1.REMARK3 AS FORM_CD, ";
                $query .= "     D1.REMARK4 AS L_CD, ";
                $query .= "     D1.REMARK5 AS S_CD, ";
                $query .= "     D1.REMARK6 AS LIMIT_DATE_WINDOW, ";
                $query .= "     D1.REMARK7 AS LIMIT_DATE_MAIL, ";
                $query .= "     D1.REMARK8 AS LIMIT_MAIL_DIV, ";
                $query .= "     T2.SCHOOL_NAME, ";
                $query .= "     T3.FACULTYNAME, ";
                $query .= "     T4.DEPARTMENTNAME ";

                //入試カレンダーの使用
                if ($model->Properties["useCollegeExamCalendar"] == "1") {
                    $query .= "     , N1.NAME1 AS ADVERTISE_NAME ";
                    $query .= "     , C1.PROGRAM_NAME ";
                    $query .= "     , C1.FORM_NAME ";
                    $query .= "     , L2.L_NAME ";
                    $query .= "     , C1.S_NAME ";
                }

                $query .= " FROM ";
                $query .= "     COLLEGE_MST T2 ";
                $query .= "     LEFT JOIN COLLEGE_FACULTY_MST T3 ON T2.SCHOOL_CD = T3.SCHOOL_CD ";
                $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST T4 ON T2.SCHOOL_CD = T4.SCHOOL_CD ";
                $query .= "                                        AND T3.FACULTYCD = T4.FACULTYCD ";
                $query .= "     LEFT JOIN AFT_GRAD_COURSE_SS_DAT T1 ON T1.YEAR    = '".CTRL_YEAR."' ";
                $query .= "                                        AND T1.STAT_CD = T2.SCHOOL_CD ";
                $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D1 ON ";
                $query .= "                                         T1.YEAR       = D1.YEAR ";
                $query .= "                                     AND T1.SEQ        = D1.SEQ ";
                $query .= "                                     AND D1.DETAIL_SEQ = 1 ";

                //入試カレンダーの使用
                if ($model->Properties["useCollegeExamCalendar"] == "1") {
                    $query .= "     LEFT JOIN NAME_MST N1 ON ";
                    $query .= "             N1.NAMECD1 = 'E044' AND ";
                    $query .= "             N1.NAMECD2 = D1.REMARK1 ";
                    $query .= "     LEFT JOIN COLLEGE_EXAM_LDAT L2 ON ";
                    $query .= "             L2.L_CD = D1.REMARK4 ";
                    $query .= "     LEFT JOIN COLLEGE_EXAM_CALENDAR C1 ON ";
                    $query .= "             C1.YEAR         = '".CTRL_YEAR."' AND ";
                    $query .= "             T2.SCHOOL_CD    = C1.SCHOOL_CD AND ";
                    $query .= "             T3.FACULTYCD    = C1.FACULTYCD AND ";
                    $query .= "             T4.DEPARTMENTCD = C1.DEPARTMENTCD AND ";
                    $query .= "             D1.REMARK1      = C1.ADVERTISE_DIV AND ";
                    $query .= "             D1.REMARK2      = C1.PROGRAM_CD AND ";
                    $query .= "             D1.REMARK3      = C1.FORM_CD AND ";
                    $query .= "             D1.REMARK5      = C1.S_CD ";
                }

                $query .= " WHERE ";
                $query .= "     T2.SCHOOL_NAME       LIKE '%".$school_name[$i]."%' OR ";
                $query .= "     T2.SCHOOL_NAME_SHOW1 LIKE '%".$school_name[$i]."%' OR ";
                $query .= "     T2.SCHOOL_NAME_SHOW2 LIKE '%".$school_name[$i]."%' ";
            }
        } elseif ($model->field["SEARCH_DIV"] == "2") {//学校コード検索
            $query  = "";
            for ($i = 0; $i < get_count($model->search_no); $i++) {
                $school_cd = "";
                //学校コードを分解
                $school_cd    = sprintf("%08d", substr($model->search_no[$i], 0, 4));

                if ($model->search_no[$i] == "") {
                    continue;
                }
                if ($query != "") {
                    $query .= " UNION ";
                }
                $query .= " SELECT DISTINCT ";
                $query .= "     T2.SCHOOL_CD, ";
                $query .= "     T3.FACULTYCD, ";
                $query .= "     T4.DEPARTMENTCD, ";
                $query .= "     T1.TOROKU_DATE, ";
                $query .= "     T1.STAT_DATE1 AS EXAM_DATE, ";
                $query .= "     T1.STAT_DATE3 AS EXAM_PASS_DATE, ";
                $query .= "     T1.PREF_CD, ";
                $query .= "     D1.REMARK1 AS ADVERTISE_DIV, ";
                $query .= "     D1.REMARK2 AS PROGRAM_CD, ";
                $query .= "     D1.REMARK3 AS FORM_CD, ";
                $query .= "     D1.REMARK4 AS L_CD, ";
                $query .= "     D1.REMARK5 AS S_CD, ";
                $query .= "     D1.REMARK6 AS LIMIT_DATE_WINDOW, ";
                $query .= "     D1.REMARK7 AS LIMIT_DATE_MAIL, ";
                $query .= "     D1.REMARK8 AS LIMIT_MAIL_DIV, ";
                $query .= "     T2.SCHOOL_NAME, ";
                $query .= "     T3.FACULTYNAME, ";
                $query .= "     T4.DEPARTMENTNAME ";
                //入試カレンダーの使用
                if ($model->Properties["useCollegeExamCalendar"] == "1") {
                    $query .= "     , N1.NAME1 AS ADVERTISE_NAME ";
                    $query .= "     , C1.PROGRAM_NAME ";
                    $query .= "     , C1.FORM_NAME ";
                    $query .= "     , L2.L_NAME ";
                    $query .= "     , C1.S_NAME ";
                }
                $query .= " FROM ";
                $query .= "     COLLEGE_MST T2 ";
                $query .= "     LEFT JOIN COLLEGE_FACULTY_MST T3 ON T2.SCHOOL_CD = T3.SCHOOL_CD ";
                $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST T4 ON T2.SCHOOL_CD = T4.SCHOOL_CD ";
                $query .= "                                     AND T3.FACULTYCD    = T4.FACULTYCD ";
                $query .= "     LEFT JOIN AFT_GRAD_COURSE_SS_DAT T1 ON T1.YEAR    = '".CTRL_YEAR."' ";
                $query .= "                                        AND T1.STAT_CD = T2.SCHOOL_CD ";
                $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D1 ON ";
                $query .= "                                     T1.YEAR            = D1.YEAR ";
                $query .= "                                     AND T1.SEQ         = D1.SEQ ";
                $query .= "                                     AND D1.DETAIL_SEQ  = 1 ";

                //入試カレンダーの使用
                if ($model->Properties["useCollegeExamCalendar"] == "1") {
                    $query .= "     LEFT JOIN NAME_MST N1 ON ";
                    $query .= "             N1.NAMECD1 = 'E044' AND ";
                    $query .= "             N1.NAMECD2 = D1.REMARK1 ";
                    $query .= "     LEFT JOIN COLLEGE_EXAM_LDAT L2 ON ";
                    $query .= "             L2.L_CD = D1.REMARK4 ";
                    $query .= "     LEFT JOIN COLLEGE_EXAM_CALENDAR C1 ON ";
                    $query .= "             C1.YEAR         = '".CTRL_YEAR."' AND ";
                    $query .= "             T2.SCHOOL_CD    = C1.SCHOOL_CD AND ";
                    $query .= "             T3.FACULTYCD    = C1.FACULTYCD AND ";
                    $query .= "             T4.DEPARTMENTCD = C1.DEPARTMENTCD AND ";
                    $query .= "             D1.REMARK1      = C1.ADVERTISE_DIV AND ";
                    $query .= "             D1.REMARK2      = C1.PROGRAM_CD AND ";
                    $query .= "             D1.REMARK3      = C1.FORM_CD AND ";
                    $query .= "             D1.REMARK5      = C1.S_CD ";
                }
                $query .= " WHERE ";
                $query .= "     T2.SCHOOL_CD = '".$school_cd."' ";
            }
        }
        if ($query != "") {
            $query .= " ORDER BY ";
            $query .= "     SCHOOL_CD ";
            $query .= "     , FACULTYCD ";
            $query .= "     , DEPARTMENTCD ";
        }

        return $query;
    }

    //選択済み学校取得
    public function getSelectedSchool($model)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(T1.STAT_CD,'') || ':' || VALUE(T1.FACULTYCD,'') || ':' || VALUE(T1.DEPARTMENTCD,'') || ':' || VALUE(D1.REMARK1,'') || ':' || VALUE(D1.REMARK2,'') || ':' || VALUE(D1.REMARK3,'') || ':' || VALUE(D1.REMARK5,'') ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_SS_DAT T1 ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D1 ON ";
        $query .= "             T1.YEAR         = D1.YEAR AND ";
        $query .= "             T1.SEQ          = D1.SEQ AND ";
        $query .= "             D1.DETAIL_SEQ   = 1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' ";
        $query .= " AND T1.SCHREGNO    = '".$model->schregno."' ";
        $query .= " AND T1.SENKOU_KIND = '0' ";

        return $query;
    }

    // UPDATE（学校選択）
    public function &getRepUpdateQuery6($db, $model)
    {
        $db->autoCommit(false);

        foreach ($model->replace["data_chk"] as $key) {
            list($school_cd, $facultycd, $departmentcd, $advertise_div, $program_cd, $form_cd, $s_cd, $exam_date, $exam_pass_date, $pref_cd, $l_cd1, $limit_date_window, $limit_date_mail, $limit_mail_div) = preg_split("/:/", $key);
            $seq          = (int)$db->getOne(knje360bQuery::getMaxSeq2($db))+1;
            $school_group = $db->getOne(knje360bQuery::getSchoolGroup($db, $school_cd));
            $toroku_date  = str_replace("/", "-", $model->replace["field"]["TOROKU_DATE"]);

            //追加 -- AFT_GRAD_COURSE_SS_DAT
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEQ"][NUMBER]            = $seq;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["SENKOU_KIND"][TEXT]      = '0';
            $data["STAT_CD"][TEXT]          = $school_cd;
            $data["SCHOOL_GROUP"][TEXT]     = $school_group;
            $data["FACULTYCD"][TEXT]        = $facultycd;
            $data["DEPARTMENTCD"][TEXT]     = $departmentcd;
            $data["TOROKU_DATE"][TEXT]      = $toroku_date;
            $data["STAT_DATE1"][TEXT]       = $exam_date;
            $data["STAT_DATE3"][TEXT]       = $exam_pass_date;
            $data["PREF_CD"][TEXT]          = $pref_cd;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_SS_DAT"));

            //追加 -- AFT_GRAD_COURSE_DETAIL_DAT

            $query  = " DELETE FROM ";
            $query .= "     AFT_GRAD_COURSE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEQ = ".$seq." ";
            $db->query($query);

            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEQ"][NUMBER]            = $seq;
            $data["DETAIL_SEQ"][NUMBER]     = 1;
            $data["REMARK1"][TEXT]          = $advertise_div;
            $data["REMARK2"][TEXT]          = $program_cd;
            $data["REMARK3"][TEXT]          = $form_cd;
            $data["REMARK4"][TEXT]          = $l_cd1;
            $data["REMARK5"][TEXT]          = $s_cd;
            $data["REMARK6"][TEXT]          = $limit_date_window;
            $data["REMARK7"][TEXT]          = $limit_date_mail;
            $data["REMARK8"][TEXT]          = $limit_mail_div;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
        }
        $db->commit();

        return;
    }
}
