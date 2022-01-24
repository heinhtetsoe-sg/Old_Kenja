<?php

require_once('for_php7.php');

class knje360jQuery extends Query
{
    /***************メイン画面***************/

    //生徒情報取得
    public function getSchInfo($model)
    {
        //卒業生
        if ($model->mode == "grd") {
            $query  = " SELECT ";
            $query .= "     GRD_BASE.SCHREGNO, ";
            $query .= "     T2.HR_NAME, ";
            $query .= "     INT(GRD_BASE.GRD_ATTENDNO) AS ATTENDNO, ";
            $query .= "     GRD_BASE.NAME_SHOW ";
            $query .= " FROM ";
            $query .= "     GRD_BASE_MST GRD_BASE, ";
            $query .= "     GRD_REGD_HDAT T2 ";
            $query .= " WHERE ";
            $query .= "     GRD_BASE.SCHREGNO = '".$model->schregno."' AND ";
            $query .= "     FISCALYEAR(GRD_BASE.GRD_DATE) = T2.YEAR AND ";
            $query .= "     GRD_BASE.GRD_SEMESTER         = T2.SEMESTER AND ";
            $query .= "     GRD_BASE.GRD_GRADE            = T2.GRADE AND ";
            $query .= "     GRD_BASE.GRD_HR_CLASS         = T2.HR_CLASS ";

        //在学生
        } else {
            $query  = " SELECT ";
            $query .= "     REGD.SCHREGNO, ";
            $query .= "     T3.HR_NAME, ";
            $query .= "     INT(REGD.ATTENDNO) AS ATTENDNO, ";
            $query .= "     T2.NAME_SHOW ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT REGD ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON ";
            $query .= "             REGD.SCHREGNO = T2.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON ";
            $query .= "             REGD.YEAR     = T3.YEAR AND ";
            $query .= "             REGD.SEMESTER = T3.SEMESTER AND ";
            $query .= "             REGD.GRADE    = T3.GRADE AND ";
            $query .= "             REGD.HR_CLASS = T3.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     REGD.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     REGD.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     REGD.SCHREGNO = '".$model->schregno."' ";
        }

        return $query;
    }

    //部クラブ名取得
    public function getClubName($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.CLUBNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT CLUB ";
        $query .= "     LEFT JOIN CLUB_MST T2 ON CLUB.CLUBCD = T2.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     CLUB.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.SDATE DESC, ";
        $query .= "     CLUB.CLUBCD ";

        return $query;
    }

    //委員会取得
    public function getCommitName($model)
    {
        $query  = " SELECT ";
        $query .= "     COMMITTEE.YEAR, ";
        $query .= "     COMMITTEE.SEQ, ";
        $query .= "     T2.COMMITTEENAME, ";
        $query .= "     COMMITTEE.CHARGENAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT COMMITTEE ";
        $query .= "     LEFT JOIN COMMITTEE_MST T2 ON ";
        $query .= "         COMMITTEE.COMMITTEE_FLG = T2.COMMITTEE_FLG AND COMMITTEE.COMMITTEECD = T2.COMMITTEECD ";
        $query .= " WHERE ";
        $query .= "     COMMITTEE.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     COMMITTEE.YEAR DESC, ";
        $query .= "     COMMITTEE.SEQ DESC ";

        return $query;
    }

    //中高一貫取得
    public function getCombJHsch()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //評定平均取得
    public function getValueAverage($model, $flg="")
    {
        $query  = " SELECT ";
        $query .= "     DECIMAL(ROUND(DECIMAL(AVG(1.0 * VALUATION),5,2),1),5,1) AS AVERAGE ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        if ($flg) {
            $query .= "     AND ANNUAL >= '04' ";
        }

        return $query;
    }

    //出身学校取得
    public function getFinschoolName($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE ";
        $query .= "     LEFT JOIN FINSCHOOL_MST T2 ON ";
        $query .= "         BASE.FINSCHOOLCD = T2.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     BASE.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //進路データ一覧取得
    public function selectQuery($model)
    {
        $query = "";
        if ($model->Properties["useKNJE360J_shinro"] == '1') {
            $query .= " SELECT ";
            $query .= "     'shinro' AS JUMP_NAME, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.ENTRYDATE, ";
            $query .= "     T1.SEQ, ";
            $query .= "     '1' AS KINDCD, ";
            $query .= "     '進路調査' AS KINDNAME, ";
            $query .= "     CASE T1.COURSE_KIND WHEN '1' THEN '進学' WHEN '2' THEN '就職' WHEN '3' THEN 'その他（家事手伝い等）' ELSE '未定' END AS ONE, ";
            $query .= "     T2.QUESTIONNAIRENAME AS TWO, ";
            $query .= "     '' AS THREE, ";
            $query .= "     '' AS FOUR, ";
            $query .= "     '' AS FIVE, ";
            $query .= "     '' AS SIX, ";
            $query .= "     '' AS SEVEN ";
            $query .= " FROM ";
            $query .= "     COURSE_HOPE_DAT T1 ";
            $query .= "     LEFT JOIN QUESTIONNAIRE_MST T2 ON T1.QUESTIONNAIRECD = T2.QUESTIONNAIRECD ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= " UNION ";
        }
        $query .= " SELECT ";
        $query .= "     'shingaku' AS JUMP_NAME, ";
        $query .= "     AFT_GRAD.SCHREGNO, ";
        $query .= "     AFT_GRAD.TOROKU_DATE AS ENTRYDATE, ";
        $query .= "     AFT_GRAD.SEQ, ";
        $query .= "     '2' AS KINDCD, ";
        $query .= "     '受験報告' AS KINDNAME, ";
        $query .= "     '進学' AS ONE, ";
        $query .= "     FINSCHOOL.FINSCHOOL_NAME AS TWO, ";
        $query .= "     AFT_GRAD.BUNAME AS THREE, ";
        $query .= "     '' AS FOUR, ";
        $query .= "     N1.NAME1 AS FIVE, ";
        $query .= "     N2.NAME1 AS SIX, ";
        $query .= "     N3.NAME1 AS SEVEN ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT_GRAD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON AFT_GRAD.STAT_CD = FINSCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1   = 'E002' ";
        $query .= "                          AND N1.NAMECD2 = AFT_GRAD.HOWTOEXAM ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1   = 'E005' ";
        $query .= "                          AND N2.NAMECD2 = AFT_GRAD.DECISION ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1   = 'E006' ";
        $query .= "                          AND N3.NAMECD2 = AFT_GRAD.PLANSTAT ";
        $query .= " WHERE ";
        $query .= "     AFT_GRAD.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     AFT_GRAD.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     AFT_GRAD.TOROKU_DATE IS NOT NULL AND ";
        $query .= "     AFT_GRAD.SENKOU_KIND = '0' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     'syuusyoku' AS JUMP_NAME, ";
        $query .= "     AFT_GRAD.SCHREGNO, ";
        $query .= "     AFT_GRAD.TOROKU_DATE AS ENTRYDATE, ";
        $query .= "     AFT_GRAD.SEQ, ";
        $query .= "     '3' AS KINDCD, ";
        $query .= "     '受験報告' AS KINDNAME, ";
        $query .= "     '就職' AS ONE, ";
        $query .= "     T2.COMPANY_NAME AS TWO, ";
        $query .= "     '' AS THREE, ";
        $query .= "     '' AS FOUR, ";
        $query .= "     '' AS FIVE, ";
        $query .= "     '' AS SIX, ";
        $query .= "     '' AS SEVEN ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT_GRAD ";
        $query .= "     LEFT JOIN COMPANY_MST T2 ON AFT_GRAD.STAT_CD = T2.COMPANY_CD ";
        $query .= " WHERE ";
        $query .= "     AFT_GRAD.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     AFT_GRAD.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     AFT_GRAD.TOROKU_DATE IS NOT NULL AND ";
        $query .= "     AFT_GRAD.SENKOU_KIND = '1' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     'shinroSoudan' AS JUMP_NAME, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ENTRYDATE, ";
        $query .= "     SEQ, ";
        $query .= "     '4' AS KINDCD, ";
        $query .= "     '進路相談' AS KINDNAME, ";
        $query .= "     TITLE AS ONE, ";
        $query .= "     CASE WHEN LENGTH(CONTENTS) > 90 THEN SUBSTR(CONTENTS,1,90) || '...' ELSE CONTENTS END AS TWO, ";
        $query .= "     '' AS THREE, ";
        $query .= "     '' AS FOUR, ";
        $query .= "     '' AS FIVE, ";
        $query .= "     '' AS SIX, ";
        $query .= "     '' AS SEVEN ";
        $query .= " FROM ";
        $query .= "     COURSE_COUNSELING_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     'sonota' AS JUMP_NAME, ";
        $query .= "     AFT_GRAD.SCHREGNO, ";
        $query .= "     AFT_GRAD.TOROKU_DATE AS ENTRYDATE, ";
        $query .= "     AFT_GRAD.SEQ, ";
        $query .= "     '5' AS KINDCD, ";
        $query .= "     'その他' AS KINDNAME, ";
        $query .= "     '家事手伝い等' AS ONE, ";
        $query .= "     T2.NAME1 AS TWO, ";
        $query .= "     '' AS THREE, ";
        $query .= "     '' AS FOUR, ";
        $query .= "     '' AS FIVE, ";
        $query .= "     '' AS SIX, ";
        $query .= "     '' AS SEVEN ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT_GRAD ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON AFT_GRAD.YEAR = T2.YEAR ";
        $query .= "                            AND T2.NAMECD1 = 'E022' ";
        $query .= "                            AND AFT_GRAD.SENKOU_KIND_SUB = T2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     AFT_GRAD.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     AFT_GRAD.SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     AFT_GRAD.TOROKU_DATE IS NOT NULL AND ";
        $query .= "     AFT_GRAD.SENKOU_KIND  = '2' ";
        $query .= " ORDER BY ";
        $query .= "     ENTRYDATE DESC, ";
        $query .= "     SEQ DESC ";
        return $query;
    }

    // DELETE
    public function &getDeleteQuery($db, $model)
    {
        $array = explode(",", implode(',', $model->checked));

        for ($i = 0; $i < get_count($array); $i++) {
            list($entrydate, $seq, $kindcd) = explode(":", $array[$i]);

            if (($kindcd == '1') || ($kindcd == '4')) {
                $table = ($kindcd == '1') ? "COURSE_HOPE_DAT" : "COURSE_COUNSELING_DAT";
                $entrydate = str_replace("/", "-", $entrydate);

                $query  = " DELETE FROM ";
                $query .=       $table ;
                $query .= " WHERE ";
                $query .= "     ENTRYDATE = '".$entrydate."' AND ";
                $query .= "     SEQ = ".$seq." AND ";
                $query .= "     SCHREGNO = '".$model->schregno."' ";
                $db->query($query);

                if ($kindcd == '1') {
                    $query  = " DELETE FROM ";
                    $query .= "     COURSE_HOPE_DETAIL_DAT ";
                    $query .= " WHERE ";
                    $query .= "     ENTRYDATE = '".$entrydate."' AND ";
                    $query .= "     SEQ = ".$seq." AND ";
                    $query .= "     SCHREGNO = '".$model->schregno."' ";
                    $db->query($query);
                }
            } else {
                $query  = " DELETE FROM ";
                $query .= "     AFT_GRAD_COURSE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     SEQ = ".$seq." AND ";
                $query .= "     SCHREGNO = '".$model->schregno."' ";
                $db->query($query);

                $query  = " DELETE FROM ";
                $query .= "     AFT_GRAD_COURSE_DETAIL_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     SEQ = ".$seq." ";
                $db->query($query);
            }
        }
        return ;
    }

    /***************共通***************/

    //クラス名称取得
    public function getHrName($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE = '".$model->grade."' AND ";
        $query .= "     HR_CLASS = '".$model->hr_class."' ";

        return $query;
    }

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

    //大学・専門学校情報取得
    public function getFinSchoolInfo($schoolcd)
    {
        $query  = " SELECT ";
        $query .= "     FINSCHOOL.FINSCHOOLCD AS FINSCHOOLCD, ";
        $query .= "     FINSCHOOL.FINSCHOOL_NAME AS SCHOOL_NAME, ";
        $query .= "     FINSCHOOL.FINSCHOOL_ZIPCD AS ZIPCD, ";
        $query .= "     FINSCHOOL.FINSCHOOL_ADDR1 AS ADDR1, ";
        $query .= "     FINSCHOOL.FINSCHOOL_ADDR2 AS ADDR2, ";
        $query .= "     FINSCHOOL.FINSCHOOL_TELNO AS TELNO, ";
        $query .= "     FINSCHOOL.FINSCHOOL_DIV, ";
        $query .= "     L015.NAME1 AS DISTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST FINSCHOOL ";
        $query .= "     LEFT JOIN NAME_MST L015 ON  ";
        $query .= "             L015.NAMECD1 = 'L015' AND ";
        $query .= "             L015.NAMECD2 = FINSCHOOL.FINSCHOOL_DIV ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOL.FINSCHOOLCD = '".$schoolcd."' ";

        return $query;
    }

    //大学・専門学校情報取得
    public function getCollegeInfo($schoolcd, $facultycd, $departmentcd, $addr_cd="")
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
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_SEQ = '002' ";

        return $query;
    }

    //都道府県一覧取得
    public function getPrefList($prefcd="")
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
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '9' ";

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
        $query .= "     ENTRYDATE = '".str_replace("/", "-", $model->field["ENTRYDATE"])."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
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
        $query .= "     AFT_GRAD_COURSE_DAT ";
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
        $query .= "     REMARK1";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    , SCHOOLCD ";
        }
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                    $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else {
                $query .= " AND SCHOOL_KIND IN (SELECT ";
                $query .= "                         S2.SCHOOL_KIND ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_DAT S1, ";
                $query .= "                         SCHREG_REGD_GDAT S2 ";
                $query .= "                     WHERE ";
                $query .= "                         S1.YEAR     = S2.YEAR AND ";
                $query .= "                         S1.YEAR     = '".CTRL_YEAR."' AND ";
                $query .= "                         S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
                $query .= "                         S1.GRADE    = S2.GRADE AND ";
                $query .= "                         S1.SCHREGNO = '".$model->schregno."' ";
                $query .= "                     ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     SCHOOLCD ";
        }
        return $query;
    }

    //出身学校データ存在チェック
    public function checkFinSchoolData($school_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST FINSCHOOL ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOL.FINSCHOOLCD = '".$school_cd."' ";

        return $query;
    }

    /***************進路調査***************/

    //進路調査データ一覧取得
    public function getSubQuery1List($model)
    {
        $query  = " SELECT ";
        $query .= "     HOPE.*, ";
        $query .= "     L1.SCHOOL_NAME AS SCHOOL_NAME1, ";
        $query .= "     L2.SCHOOL_NAME AS SCHOOL_NAME2, ";
        $query .= "     L3.JOBTYPE_LNAME AS JOBTYPE_LNAME1, ";
        $query .= "     L4.JOBTYPE_LNAME AS JOBTYPE_LNAME2, ";
        $query .= "     HOPE.QUESTIONNAIRECD || '：' || L5.QUESTIONNAIRENAME AS QUESTIONNAIRENAME ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DAT HOPE ";
        $query .= "     LEFT JOIN COLLEGE_MST L1 ON HOPE.SCHOOL_CD1 = L1.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_MST L2 ON HOPE.SCHOOL_CD2 = L2.SCHOOL_CD ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST L3 ON HOPE.JOBTYPE_LCD1 = L3.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST L4 ON HOPE.JOBTYPE_LCD2 = L4.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST L5 ON HOPE.QUESTIONNAIRECD = L5.QUESTIONNAIRECD ";
        $query .= " WHERE ";
        $query .= "     HOPE.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     HOPE.YEAR     = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     HOPE.COURSE_KIND, ";
        $query .= "     HOPE.ENTRYDATE DESC, ";
        $query .= "     HOPE.SEQ DESC ";

        return $query;
    }

    //学校名取得
    public function getSchoolName($model, $entrydate, $seq)
    {
        $query  = " SELECT ";
        $query .= "     HOPE_DET.HOPE_NUM, ";
        $query .= "     T2.SCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DETAIL_DAT HOPE_DET, ";
        $query .= "     COLLEGE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     HOPE_DET.ENTRYDATE    = '".$entrydate."' AND ";
        $query .= "     HOPE_DET.SEQ          = ".$seq." AND ";
        $query .= "     HOPE_DET.SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     HOPE_DET.SCHOOL_CD    = T2.SCHOOL_CD  ";
        $query .= " ORDER BY ";
        $query .= "     HOPE_DET.HOPE_NUM ";

        return $query;
    }

    //進路調査データ取得
    public function getSubQuery1($model, $entrydate)
    {
        $entrydate = str_replace("/", "-", $entrydate);

        $query  = " SELECT ";
        $query .= "     HOPE.*, ";
        $query .= "     S1.JOBTYPE_SNAME AS JOBTYPE_SNAME1, ";
        $query .= "     S2.JOBTYPE_SNAME AS JOBTYPE_SNAME2 ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DAT HOPE ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST S1 ON HOPE.JOBTYPE_LCD1 = S1.JOBTYPE_LCD AND HOPE.JOBTYPE_MCD1 = S1.JOBTYPE_MCD AND HOPE.JOBTYPE_SCD1 = S1.JOBTYPE_SCD ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST S2 ON HOPE.JOBTYPE_LCD2 = S2.JOBTYPE_LCD AND HOPE.JOBTYPE_MCD2 = S2.JOBTYPE_MCD AND HOPE.JOBTYPE_SCD2 = S2.JOBTYPE_SCD ";
        $query .= " WHERE ";
        $query .= "     HOPE.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     HOPE.ENTRYDATE = '".$entrydate."' AND ";
        $query .= "     HOPE.SEQ = ".$model->seq." AND ";
        $query .= "     HOPE.YEAR = '".CTRL_YEAR."' ";

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
        $query .= "     COURSE_HOPE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     ENTRYDATE   = '".$entrydate."' AND ";
        $query .= "     SEQ         = ".$model->seq." AND ";
        $query .= "     HOPE_NUM    = ".$hope_num." ";

        return $query;
    }

    //アンケート一覧取得
    public function getQuestionnaireList()
    {
        $query  = " SELECT ";
        $query .= "     QUESTIONNAIRE.QUESTIONNAIRECD AS VALUE, ";
        $query .= "     QUESTIONNAIRE.QUESTIONNAIRECD || '：' || T2.QUESTIONNAIRENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_YDAT QUESTIONNAIRE ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST T2 ON QUESTIONNAIRE.QUESTIONNAIRECD = T2.QUESTIONNAIRECD ";
        $query .= " WHERE ";
        $query .= "     QUESTIONNAIRE.YEAR = '".CTRL_YEAR."' ";
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
    public function getJobtypeLList($lcd="")
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
        $query .= "     JOBTYPE_SCD || ' ' || JOBTYPE_SNAME AS LABEL, ";
        $query .= "     JOBTYPE_SCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_S_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '{$jobtype_lcd}' AND ";
        $query .= "     JOBTYPE_MCD = '{$jobtype_mcd}' ";

        return $query;
    }

    // UPDATE（進路調査）
    public function &getUpdateQuery1($db, $model, $fields, $insert)
    {
        $seq = ($insert == "insert") ? $db->getOne(knje360jQuery::getMaxSeq($db, $model, "COURSE_HOPE_DAT"))+1 : $model->seq;
        $entrydate = str_replace("/", "-", $fields["ENTRYDATE"]);

        $data["ENTRYDATE"][TEXT]            = $entrydate;
        $data["SEQ"][NUMBER]                = $seq;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["COURSE_KIND"][TEXT]          = $fields["COURSE_KIND"];
        $data["QUESTIONNAIRECD"][TEXT]      = $fields["QUESTIONNAIRECD"];

        if ($fields["COURSE_KIND"] == '1') {
            $data["SCHOOL_GROUP1"][TEXT]        = ($fields["SCHOOL_GROUP1"]) ? $fields["SCHOOL_GROUP1"] : $db->getOne(knje360jQuery::getCollegeGroup($db, $fields["SCHOOL_CD1"]));
            $data["FACULTY_GROUP1"][TEXT]       = $fields["FACULTY_GROUP1"];
            $data["DEPARTMENT_GROUP1"][TEXT]    = $fields["DEPARTMENT_GROUP1"];
            $data["SCHOOL_CD1"][TEXT]           = $fields["SCHOOL_CD1"];
            $data["FACULTYCD1"][TEXT]           = $fields["FACULTYCD1"];
            $data["DEPARTMENTCD1"][TEXT]        = $fields["DEPARTMENTCD1"];
            $data["HOWTOEXAM1"][NUMBER]         = $fields["HOWTOEXAM1"];
            $data["SCHOOL_GROUP2"][TEXT]        = ($fields["SCHOOL_GROUP2"]) ? $fields["SCHOOL_GROUP2"] : $db->getOne(knje360jQuery::getCollegeGroup($db, $fields["SCHOOL_CD2"]));
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
            $data["WORK_AREA1"][TEXT]           = $fields["WORK_AREA1"];
            $data["INTRODUCTION_DIV1"][TEXT]    = $fields["INTRODUCTION_DIV1"];
            $data["JOBTYPE_LCD2"][TEXT]         = $fields["JOBTYPE_LCD2"];
            $data["JOBTYPE_MCD2"][TEXT]         = $fields["JOBTYPE_MCD2"];
            $data["JOBTYPE_SCD2"][TEXT]         = $fields["JOBTYPE_SCD2"];
            $data["WORK_AREA2"][TEXT]           = $fields["WORK_AREA2"];
            $data["INTRODUCTION_DIV2"][TEXT]    = $fields["INTRODUCTION_DIV2"];
        }

        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($insert == "insert") {
            $db->query(Query::insertSQL($data, "COURSE_HOPE_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     ENTRYDATE = '".$entrydate."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' AND ";
            $where .= "     SEQ = ".$seq." ";

            $db->query(Query::updateSQL($data, "COURSE_HOPE_DAT", $where));
        }

        if ($fields["COURSE_KIND"] == '1') {
            $query  = " DELETE FROM ";
            $query .= "     COURSE_HOPE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTRYDATE   = '".$entrydate."' AND ";
            $query .= "     SEQ         = ".$seq." AND ";
            $query .= "     SCHREGNO    = '".$model->schregno."' ";
            $db->query($query);

            for ($i=3; $i<7; $i++) {
                $school_group = ($fields["SCHOOL_GROUP".$i]) ? $fields["SCHOOL_GROUP".$i] : $db->getOne(knje360jQuery::getCollegeGroup($db, $fields["SCHOOL_CD".$i]));
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

                    $db->query(Query::insertSQL($data, "COURSE_HOPE_DETAIL_DAT"));
                }
            }
        }

        return;
    }

    // UPDATE（進路調査・進学）一括更新
    public function &getRepUpdateQuery1($db, $model, $fields, $schregno)
    {
        $model->field["ENTRYDATE"] = $fields["ENTRYDATE"];
        $seq = $db->getOne(knje360jQuery::getMaxSeq($db, $model, "COURSE_HOPE_DAT"))+1;
        $entrydate = str_replace("/", "-", $model->field["ENTRYDATE"]);

        $data["ENTRYDATE"][TEXT]            = $entrydate;
        $data["SEQ"][NUMBER]                = $seq;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["COURSE_KIND"][TEXT]          = $fields["COURSE_KIND"];
        $data["QUESTIONNAIRECD"][TEXT]      = $fields["QUESTIONNAIRECD"];
        $data["SCHOOL_GROUP1"][TEXT]        = ($fields["SCHOOL_GROUP1"]) ? $fields["SCHOOL_GROUP1"] : $db->getOne(knje360jQuery::getCollegeGroup($db, $fields["SCHOOL_CD1"]));
        $data["FACULTY_GROUP1"][TEXT]       = $fields["FACULTY_GROUP1"];
        $data["DEPARTMENT_GROUP1"][TEXT]    = $fields["DEPARTMENT_GROUP1"];
        $data["SCHOOL_CD1"][TEXT]           = $fields["SCHOOL_CD1"];
        $data["FACULTYCD1"][TEXT]           = $fields["FACULTYCD1"];
        $data["DEPARTMENTCD1"][TEXT]        = $fields["DEPARTMENTCD1"];
        $data["HOWTOEXAM1"][NUMBER]         = $fields["HOWTOEXAM1"];
        $data["SCHOOL_GROUP2"][TEXT]        = ($fields["SCHOOL_GROUP2"]) ? $fields["SCHOOL_GROUP2"] : $db->getOne(knje360jQuery::getCollegeGroup($db, $fields["SCHOOL_CD2"]));
        $data["FACULTY_GROUP2"][TEXT]       = $fields["FACULTY_GROUP2"];
        $data["DEPARTMENT_GROUP2"][TEXT]    = $fields["DEPARTMENT_GROUP2"];
        $data["SCHOOL_CD2"][TEXT]           = $fields["SCHOOL_CD2"];
        $data["FACULTYCD2"][TEXT]           = $fields["FACULTYCD2"];
        $data["DEPARTMENTCD2"][TEXT]        = $fields["DEPARTMENTCD2"];
        $data["HOWTOEXAM2"][NUMBER]         = $fields["HOWTOEXAM2"];
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "COURSE_HOPE_DAT");
        return $query;
    }

    /***************受験報告（進学）***************/

    //受験報告データ（進学）取得
    public function getSubQuery2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     AFT_GRAD.SEQ, ";
        $query .= "     AFT_GRAD.SCHREGNO, ";
        $query .= "     FINSCHOOL.FINSCHOOLCD AS FINSCHOOLCD, ";
        $query .= "     FINSCHOOL.FINSCHOOL_NAME AS SCHOOL_NAME, ";
        $query .= "     AFT_GRAD.FACULTYCD, ";
        $query .= "     AFT_GRAD.BUNAME, ";
        $query .= "     AFT_GRAD.DEPARTMENTCD, ";
        $query .= "     CASE WHEN AFT_GRAD.PREF_CD IS NULL THEN '' ELSE AFT_GRAD.PREF_CD END || '-' || CASE WHEN AFT_GRAD.CITY_CD IS NULL THEN '' ELSE AFT_GRAD.CITY_CD END AS PREF_CD, ";
        $query .= "     AFT_GRAD.TOROKU_DATE, ";
        $query .= "     AFT_GRAD.STAT_DATE1, ";
        $query .= "     AFT_GRAD.STAT_DATE3, ";
        $query .= "     AFT_GRAD.HOWTOEXAM, ";
        $query .= "     AFT_GRAD.CONTENTEXAM, ";
        $query .= "     AFT_GRAD.REASONEXAM, ";
        $query .= "     AFT_GRAD.THINKEXAM, ";
        $query .= "     AFT_GRAD.DECISION, ";
        $query .= "     CASE WHEN VALUE(E1.REMARK13,'') = '' THEN '' ELSE '1' END AS ISSUE, ";
        $query .= "     AFT_GRAD.PLANSTAT, ";
        $query .= "     D1.REMARK1 AS ADVERTISE_DIV, ";
        $query .= "     D1.REMARK2 AS PROGRAM_CD, ";
        $query .= "     D1.REMARK3 AS FORM_CD, ";
        $query .= "     D1.REMARK4 AS L_CD, ";
        $query .= "     D1.REMARK5 AS S_CD, ";
        $query .= "     D1.REMARK9 AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT_GRAD ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D1 ON ";
        $query .= "             AFT_GRAD.YEAR         = D1.YEAR AND ";
        $query .= "             AFT_GRAD.SEQ          = D1.SEQ AND ";
        $query .= "             D1.DETAIL_SEQ   = 1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON AFT_GRAD.STAT_CD = FINSCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN CERTIF_DETAIL_EACHTYPE_DAT E1 ON ";
        $query .= "             AFT_GRAD.YEAR         = E1.YEAR AND ";
        $query .= "             AFT_GRAD.SEQ          = INT(E1.REMARK13) AND ";
        $query .= "             E1.TYPE         = '1' ";
        $query .= " WHERE ";
        $query .= "     AFT_GRAD.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     AFT_GRAD.SEQ = ".$model->seq." AND ";
        $query .= "     AFT_GRAD.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     AFT_GRAD.SENKOU_KIND = '0' ";

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
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     REMARK13    = '".$seq."' ";
            $query .= " ORDER BY ";
            $query .= "     CERTIF_INDEX DESC ";
        } else {
            $query  = " SELECT ";
            $query .= "     ISSUE.CERTIF_NO, ";
            $query .= "     ISSUE.CERTIF_INDEX ";
            $query .= " FROM ";
            $query .= "     CERTIF_ISSUE_DAT ISSUE, ";
            $query .= "     CERTIF_DETAIL_EACHTYPE_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     ISSUE.YEAR     = T2.YEAR AND ";
            $query .= "     ISSUE.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     ISSUE.CERTIF_INDEX = T2.CERTIF_INDEX AND ";
            $query .= "     T2.REMARK13 = '".$seq."' ";
            $query .= " ORDER BY ";
            $query .= "     ISSUE.CERTIF_INDEX DESC ";
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
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $setCertifKindCd = $model->mode == "grd" ? "025" : "007";
        $query .= "     CERTIF_KINDCD    = '{$setCertifKindCd}' ";

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
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_CD       = '".$Row["SCHOOL_CD"]."' AND ";
        $query .= "     FACULTYCD       = '".$Row["FACULTYCD"]."' AND ";
        $query .= "     DEPARTMENTCD    = '".$Row["DEPARTMENTCD"]."' AND ";
        $query .= "     ADVERTISE_DIV   = '".$Row["ADVERTISE_DIV"]."' AND ";
        $query .= "     PROGRAM_CD      = '".$Row["PROGRAM_CD"]."' AND ";
        $query .= "     FORM_CD         = '".$Row["FORM_CD"]."' AND ";
        $query .= "     L_CD1           = '".$Row["L_CD"]."' AND ";
        $query .= "     S_CD            = '".$Row["S_CD"]."' ";

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
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEQ         = ".$seq." AND ";
        $query .= "     DETAIL_SEQ  = ".$detail_seq." ";

        return $query;
    }

    // UPDATE（受験報告・進学）
    public function &getUpdateQuery2($db, $model, $fields, $insert)
    {
        $seq = ($insert == "insert") ? $db->getOne(knje360jQuery::getMaxSeq2($db))+1 : $model->seq;
        $entrydate = str_replace("/", "-", $fields["TOROKU_DATE"]);
        $prefcd = explode('-', $fields["PREF_CD"]);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $seq;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["SENKOU_KIND"][TEXT]      = '0';
        $data["STAT_CD"][TEXT]          = $fields["FINSCHOOLCD"];
        $data["BUNAME"][TEXT]           = $fields["BUNAME"];
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
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' AND ";
            $where .= "     SEQ  = ".$seq." ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DAT", $where));
        }

        //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
        $cntD = $db->getOne(knje360jQuery::checkAftGradCourseDetailDat($seq, 1));

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $seq;
        $data["DETAIL_SEQ"][NUMBER]     = 1;
        $data["REMARK9"][TEXT]          = $fields["EXAMNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cntD > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' AND ";
            $where .= "     SEQ  = ".$seq." AND ";
            $where .= "     DETAIL_SEQ = 1 ";

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
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     REMARK13    = '".$seq."' ";

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
            $data["REMARK13"][TEXT]         = $seq;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT"));
        }

        return;
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
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";
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
        $query .= "     REGD.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     REGD.SEMESTER   = '".CTRL_SEMESTER."' AND ";
        if ($flg == "left") {
            $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '_' || REGD.SCHREGNO IN ('".implode("','", $sch_array)."') ";
        } else {
            $query .= "     REGD.GRADE || '-' || REGD.HR_CLASS = '".$model->replace["ghr"]."' ";
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
            $seq = $db->getOne(knje360jQuery::getMaxSeq2($db))+1;
            $entrydate = str_replace("/", "-", $fields["TOROKU_DATE"]);
            $prefcd = explode('-', $fields["PREF_CD"]);

            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEQ"][NUMBER]            = $seq;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["SENKOU_KIND"][TEXT]      = '0';
            $data["STAT_CD"][TEXT]          = $fields["FINSCHOOLCD"];
            $data["BUNAME"][TEXT]           = $fields["BUNAME"];
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

            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DAT"));

            //存在チェック -- CERTIF_DETAIL_EACHTYPE_DAT
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     REMARK13    = '".$seq."' ";

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
                $data["REMARK13"][TEXT]         = $seq;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $db->query(Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT"));
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
        $query .= "     AFT_GRAD.SEQ, ";
        $query .= "     AFT_GRAD.SCHREGNO, ";
        $query .= "     AFT_GRAD.TOROKU_DATE, ";
        $query .= "     AFT_GRAD.SENKOU_NO, ";
        $query .= "     T2.COMPANY_CD AS STAT_CD, ";
        $query .= "     T2.COMPANY_NAME AS STAT_NAME, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.ADDR1, ";
        $query .= "     T2.ADDR2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.INDUSTRY_LCD, ";
        $query .= "     T2.INDUSTRY_MCD, ";
        $query .= "     T3.INDUSTRY_MNAME, ";
        $query .= "     AFT_GRAD.JOBTYPE_LCD  , ";
        $query .= "     AFT_GRAD.JOBTYPE_MCD, ";
        $query .= "     AFT_GRAD.JOBTYPE_SCD, ";
        $query .= "     AFT_GRAD.PREF_CD, ";
        $query .= "     AFT_GRAD.CITY_CD, ";
        $query .= "     AFT_GRAD.DECISION, ";
        $query .= "     AFT_GRAD.PLANSTAT, ";
        $query .= "     AFT_GRAD.INTRODUCTION_DIV, ";
        $query .= "     AFT_GRAD.JOB_THINK, ";
        $query .= "     T4.REMARK1, ";
        $query .= "     T4.REMARK2 ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT_GRAD ";
        $query .= "     LEFT JOIN COMPANY_MST T2 ON AFT_GRAD.STAT_CD = T2.COMPANY_CD ";
        $query .= "     LEFT JOIN INDUSTRY_M_MST T3 ON T2.INDUSTRY_LCD = T3.INDUSTRY_LCD AND T2.INDUSTRY_MCD = T3.INDUSTRY_MCD ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT T4 ON AFT_GRAD.YEAR = T4.YEAR AND AFT_GRAD.SEQ = T4.SEQ AND T4.DETAIL_SEQ = 2 ";
        $query .= " WHERE ";
        $query .= "     AFT_GRAD.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     AFT_GRAD.SEQ = ".$model->seq." AND ";
        $query .= "     AFT_GRAD.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    // １レコード取得(会社マスタ)
    public function &getCollegeOrCompanyMst($stat_cd)
    {
        $query  = " SELECT ";
        $query .= "     COMPANY.COMPANY_CD AS STAT_CD, ";
        $query .= "     COMPANY.COMPANY_NAME AS STAT_NAME, ";
        $query .= "     COMPANY.ZIPCD, ";
        $query .= "     COMPANY.ADDR1, ";
        $query .= "     COMPANY.ADDR2, ";
        $query .= "     COMPANY.TELNO, ";
        $query .= "     COMPANY.INDUSTRY_LCD, ";
        $query .= "     COMPANY.INDUSTRY_MCD, ";
        $query .= "     T2.INDUSTRY_MNAME ";
        $query .= " FROM ";
        $query .= "     COMPANY_MST COMPANY ";
        $query .= "     LEFT JOIN INDUSTRY_M_MST T2 ON   ";
        $query .= "         COMPANY.INDUSTRY_LCD = T2.INDUSTRY_LCD AND  ";
        $query .= "         COMPANY.INDUSTRY_MCD = T2.INDUSTRY_MCD  ";
        $query .= " WHERE ";
        $query .= "     COMPANY_CD = '$stat_cd'";

        return $query;
    }

    // UPDATE（受験報告・就職）
    public function &getUpdateQuery3($db, $model, $fields, $insert)
    {
        $seq = ($insert == "insert") ? $db->getOne(knje360jQuery::getMaxSeq2($db))+1 : $model->seq;
        $entrydate = str_replace("/", "-", $fields["TOROKU_DATE"]);
        $prefcd = explode('-', $fields["PREF_CD"]);

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
        $data["JOBTYPE_SCD"][TEXT]      = $fields["JOBTYPE_SCD"];
        $data["PREF_CD"][TEXT]          = $prefcd[0];
        $data["CITY_CD"][TEXT]          = $prefcd[1];
        $data["DECISION"][TEXT]         = $fields["DECISION"];
        $data["PLANSTAT"][TEXT]         = $fields["PLANSTAT"];
        $data["INTRODUCTION_DIV"][TEXT] = $fields["INTRODUCTION_DIV"];
        $data["JOB_THINK"][TEXT]        = $fields["JOB_THINK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($insert == "insert") {
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' AND ";
            $where .= "     SEQ  = ".$seq." ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DAT", $where));
        }

        //存在チェック -- AFT_GRAD_COURSE_DETAIL_DAT
        $cntD = $db->getOne(knje360jQuery::checkAftGradCourseDetailDat($seq, 2));

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
            $where .= "     YEAR = '".CTRL_YEAR."' AND ";
            $where .= "     SEQ  = ".$seq." AND ";
            $where .= "     DETAIL_SEQ = 2 ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
        }

        return;
    }

    /***************進路相談***************/

    //進路相談データ取得
    public function getSubQuery4($model, $list="")
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
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     STAFFCD = '".$staffcd."' ";

        return $query;
    }

    // UPDATE（進路相談）
    public function &getUpdateQuery4($db, $model, $fields, $insert)
    {
        $seq = ($insert == "insert") ? $db->getOne(knje360jQuery::getMaxSeq($db, $model, "COURSE_COUNSELING_DAT"))+1 : $model->seq;
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
            $where .= "     ENTRYDATE = '".$entrydate."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' AND ";
            $where .= "     SEQ = ".$seq." ";

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
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

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
        $query .= "     AFT_GRAD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEQ = ".$model->seq." AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    // UPDATE（その他）
    public function &getUpdateQuery5($db, $model, $fields, $insert)
    {
        $seq = ($insert == "insert") ? $db->getOne(knje360jQuery::getMaxSeq2($db))+1 : $model->seq;
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
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' AND ";
            $where .= "     SEQ  = ".$seq." ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DAT", $where));
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
        $query .= "     JOB_OFFER_DAT T1 ";
        $query .= "     LEFT JOIN COMPANY_MST L1 ON T1.COMPANY_CD = L1.COMPANY_CD ";
        $query .= "     LEFT JOIN INDUSTRY_M_MST L2 ON L1.INDUSTRY_LCD = L2.INDUSTRY_LCD ";
        $query .= "                                AND L1.INDUSTRY_MCD = L2.INDUSTRY_MCD ";
        $query .= " WHERE ";
        $query .= "         SENKOU_NO = ".$model->field["SENKOU_NO"]." ";

        return $query;
    }
}
