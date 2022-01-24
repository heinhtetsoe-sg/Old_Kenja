<?php

require_once('for_php7.php');

class knjd410Query extends Query {
    //年度取得
    function getYear() {
        $query  = "  SELECT ";
        $query .= "      '".CTRL_YEAR."' AS VALUE, ";
        $query .= "      '".CTRL_YEAR."' AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      YEAR AS VALUE, ";
        $query .= "      YEAR AS LABEL ";
        $query .= "  FROM ";
        $query .= "      GRADE_KIND_SCHREG_GROUP_DAT ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = "  SELECT ";
        $query .= "      SEMESTER AS VALUE, ";
        $query .= "      SEMESTERNAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".$model->year."' AND ";
        $query .= "      SEMESTER <> '9' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //学部取得
    function getSchoolKind($gakubu_school_kind="") {
        $query  = " SELECT ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($gakubu_school_kind) {
            $query .= "     AND NAME1 = '".$gakubu_school_kind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //状態区分取得
    function getCondition($condition="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A033' ";
        if ($condition) {
            $query .= "     AND NAMECD2 = '".$condition."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //グループ名取得
    function getGroupName($model, $condition, $groupcd) {
        $query  = " SELECT ";
        $query .= "     GROUPCD || ':' || GROUPNAME ";
        $query .= " FROM ";
        $query .= "      GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' ";
        $query .= " AND SEMESTER    = '".$model->semester."' ";
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        if (strlen($model->hr_class) == "2") {
            $query .= " AND GHR_CD = '".$model->hr_class."' ";
            $query .= " AND GRADE || '-' || HR_CLASS = '".$model->hr_class2."' ";
        } else {
            $query .= " AND GHR_CD = '00' ";
            $query .= " AND GRADE || '-' || HR_CLASS = '".$model->hr_class."' ";
        }
        $query .= " AND CONDITION   = '".$condition."' ";
        $query .= " AND GROUPCD     = '".$groupcd."' ";
        
        return $query;
    }

    //リスト取得（左フレーム）
    function getList($model, $condition) {
        $query  = " SELECT ";
        if ($condition) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->year."' ";
        $query .= " AND T1.SEMESTER = '".$model->semester."' ";
        $query .= " AND T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        if (strlen($model->hr_class) == "2") {
            $query .= " AND T1.GHR_CD = '".$model->hr_class."' ";
            $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->hr_class2."' ";
        } else {
            $query .= " AND T1.GHR_CD = '00' ";
            $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->hr_class."' ";
        }
        if ($condition) {
            $query .= " AND T1.CONDITION = '".$condition."' ";
        }
        if ($condition == "") {
            $query .= " ORDER BY ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.GROUPCD, ";
            $query .= "     T1.GAKUBU_SCHOOL_KIND ";
        }
        return $query;
    }
    
    //登録人数をカウント
    function getSchregCnt($model, $condition, $groupcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' ";
        $query .= " AND SEMESTER    = '".$model->semester."' ";
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        if (strlen($model->hr_class) == "2") {
            $query .= " AND GHR_CD = '".$model->hr_class."' ";
            $query .= " AND GRADE || '-' || HR_CLASS = '".$model->hr_class2."' ";
        } else {
            $query .= " AND GHR_CD = '00' ";
            $query .= " AND GRADE || '-' || HR_CLASS = '".$model->hr_class."' ";
        }
        $query .= " AND CONDITION   = '".$condition."' ";
        $query .= " AND GROUPCD     = '".$groupcd."' ";
        
        return $query;
    }

    //クラスコンボ
    function getHrClass($model) {

        if ($model->hukusiki_radio == "2") {
            $query  = " SELECT DISTINCT ";
            $query .= "     L0.GHR_CD AS VALUE, ";
            $query .= "     L0.GHR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_DAT T0 ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT L0 ON L0.YEAR       = T0.YEAR  ";
            $query .= "                                       AND L0.SEMESTER   = T0.SEMESTER ";
            $query .= "                                       AND L0.GHR_CD     = T0.GHR_CD, ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR       = T1.YEAR  ";
            $query .= "                                   AND T2.SEMESTER   = T1.SEMESTER  ";
            $query .= "                                   AND T2.GRADE      = T1.GRADE  ";
            $query .= "                                   AND T2.HR_CLASS   = T1.HR_CLASS ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
            $query .= "          ON L2.SCHREGNO     = T1.SCHREGNO ";
            $query .= "         AND L2.YEAR         = T1.YEAR ";
            $query .= "         AND L2.BASE_SEQ     = '004', ";
            $query .= "     SCHREG_REGD_GDAT T3 ";
            $query .= " WHERE ";
            $query .= "     T0.YEAR         = '{$model->year}' ";
            $query .= " AND T0.SEMESTER     = '{$model->regdSemester}' ";
            $query .= " AND T0.YEAR         = T1.YEAR ";
            $query .= " AND T0.SEMESTER     = T1.SEMESTER ";
            $query .= " AND T0.SCHREGNO     = T1.SCHREGNO ";
            $query .= " AND T1.YEAR         = T3.YEAR ";
            $query .= " AND T1.GRADE        = T3.GRADE ";
            $query .= " AND T3.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
            $query .= " AND T2.GRADE || T2.HR_CLASS NOT IN (SELECT ";
            $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
            $query .= "                                     FROM ";
            $query .= "                                         V_NAME_MST ";
            $query .= "                                     WHERE ";
            $query .= "                                         NAMECD1 = 'A032' ";
            $query .= "                                     AND YEAR = '{$model->year}') ";
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (   L0.TR_CD1       = '".STAFFCD."' ";
                $query .= "      OR L0.TR_CD2       = '".STAFFCD."' ";
                $query .= "      OR L0.TR_CD3       = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD1    = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD2    = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD3    = '".STAFFCD."') ";
            }
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT L0 ";
            $query .= "          ON L0.YEAR         = T1.YEAR  ";
            $query .= "         AND L0.SEMESTER     = T1.SEMESTER  ";
            $query .= "         AND L0.GRADE        = T1.GRADE  ";
            $query .= "         AND L0.HR_CLASS     = T1.HR_CLASS ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = L0.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
            $query .= "          ON L2.SCHREGNO     = L0.SCHREGNO ";
            $query .= "         AND L2.YEAR         = L0.YEAR ";
            $query .= "         AND L2.BASE_SEQ     = '004', ";
            $query .= "     SCHREG_REGD_GDAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '{$model->year}' ";
            $query .= " AND T1.SEMESTER     = '{$model->regdSemester}' ";
            $query .= " AND T1.YEAR         = T2.YEAR ";
            $query .= " AND T1.GRADE        = T2.GRADE ";
            $query .= " AND T2.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
            $query .= " AND T1.GRADE || T1.HR_CLASS NOT IN (SELECT ";
            $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
            $query .= "                                     FROM ";
            $query .= "                                         V_NAME_MST ";
            $query .= "                                     WHERE ";
            $query .= "                                         NAMECD1 = 'A032' ";
            $query .= "                                     AND YEAR = '{$model->year}') ";
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (   T1.TR_CD1       = '".STAFFCD."' ";
                $query .= "      OR T1.TR_CD2       = '".STAFFCD."' ";
                $query .= "      OR T1.TR_CD3       = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD1    = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD2    = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD3    = '".STAFFCD."') ";
            }
        }
        //準ずる教育以外
        $query .= " AND CASE WHEN L2.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラスコンボ
    function getHrClass2($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     L1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_DAT T0 ";
        $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT L0 ON L0.YEAR       = T0.YEAR  ";
        $query .= "                                       AND L0.SEMESTER   = T0.SEMESTER ";
        $query .= "                                       AND L0.GHR_CD     = T0.GHR_CD, ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR       = T1.YEAR  ";
        $query .= "                                   AND L1.SEMESTER   = T1.SEMESTER  ";
        $query .= "                                   AND L1.GRADE      = T1.GRADE  ";
        $query .= "                                   AND L1.HR_CLASS   = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR       = T1.YEAR  ";
        $query .= "                                   AND L2.GRADE      = T1.GRADE  ";
        $query .= "                                   AND L2.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B2 ";
        $query .= "          ON B2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND B2.YEAR         = T1.YEAR ";
        $query .= "         AND B2.BASE_SEQ     = '004' ";
        $query .= " WHERE ";
        $query .= "     T0.YEAR         = T1.YEAR ";
        $query .= " AND T0.YEAR         = '{$model->year}' ";
        $query .= " AND T0.SEMESTER     = T1.SEMESTER ";
        $query .= " AND T0.SEMESTER     = '{$model->regdSemester}' ";
        $query .= " AND T0.GHR_CD       = '{$model->hr_class}' ";
        $query .= " AND T0.SCHREGNO     = T1.SCHREGNO ";
        $query .= " AND T1.GRADE || T1.HR_CLASS NOT IN (SELECT ";
        $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
        $query .= "                                     FROM ";
        $query .= "                                         V_NAME_MST ";
        $query .= "                                     WHERE ";
        $query .= "                                         NAMECD1 = 'A032' ";
        $query .= "                                     AND YEAR    = '{$model->year}') ";
        //準ずる教育以外
        $query .= " AND CASE WHEN B2.BASE_REMARK1 IS NULL THEN B1.HANDICAP ELSE B2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //左生徒リスト
    function getGhrStudents($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     CASE WHEN T4.GHR_ATTENDNO IS NOT NULL THEN T4.GHR_ATTENDNO ELSE T1.ATTENDNO END AS ATTENDNO, ";
        $query .= "     CASE WHEN T5.GHR_NAME IS NOT NULL THEN T5.GHR_NAME ELSE T2.HR_NAME END AS HR_NAME, ";
        $query .= "     T3.NAME, ";
        $query .= "     CASE WHEN T4.GHR_ATTENDNO IS NOT NULL THEN '1' ELSE '2' END AS GHR_DIV, ";
        $query .= "     CASE WHEN T4.GHR_ATTENDNO IS NOT NULL THEN T5.GHR_CD || T4.GHR_ATTENDNO ELSE T1.GRADE || T1.HR_CLASS || T1.ATTENDNO END AS GHA ";
        $query .= "     , B2.BASE_REMARK2 ";

        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT T0 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.YEAR = T1.YEAR  AND T1.SEMESTER = '{$model->regdSemester}' ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B2 ON B2.SCHREGNO = T1.SCHREGNO AND B2.YEAR = T1.YEAR AND B2.BASE_SEQ = '004' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GHR_DAT T4 ON T4.SCHREGNO = T0.SCHREGNO AND T4.YEAR = T0.YEAR AND T4.SEMESTER = '{$model->regdSemester}' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GHR_HDAT T5 ON T5.YEAR = T4.YEAR AND T5.SEMESTER = T4.SEMESTER AND T5.GHR_CD = T4.GHR_CD ";
        $query .= " WHERE ";
        $query .= "     T0.YEAR         = '".$model->year."' ";
        $query .= " AND T0.SEMESTER     = '".$model->semester."' ";
        $query .= " AND T0.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        if (strlen($model->hr_class) == "2") {
            $query .= " AND T0.GHR_CD = '".$model->hr_class."' ";
            $query .= " AND T0.GRADE || '-' || T0.HR_CLASS = '".$model->hr_class2."' ";
        } else {
            $query .= " AND T0.GHR_CD = '00' ";
            $query .= " AND T0.GRADE || '-' || T0.HR_CLASS = '".$model->hr_class."' ";
        }
        $query .= " AND T0.CONDITION    = '".$model->condition."' ";
        $query .= " AND T0.GROUPCD      = '".$model->groupcd."' ";
        //準ずる教育以外
        if ($model->condition == "1") {
            $query .= " AND CASE WHEN B2.BASE_REMARK1 IS NULL THEN T3.HANDICAP ELSE B2.BASE_REMARK1 END ";
            $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T0.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        } else {
            $query .= " AND CASE WHEN B2.BASE_REMARK1 IS NULL THEN T3.HANDICAP ELSE B2.BASE_REMARK1 END ";
            $query .= "         NOT IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T0.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T5.GHR_CD, ";
        $query .= "     T4.GHR_ATTENDNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //右生徒リスト
    function getHrStudents($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        //特別クラスの場合
        if (strlen($model->hr_class) == "2") {
            $query .= "     T4.GHR_ATTENDNO AS ATTENDNO, ";
            $query .= "     T5.GHR_NAME AS HR_NAME, ";
            $query .= "     '1' AS GHR_DIV, ";
            $query .= "     T5.GHR_CD || T4.GHR_ATTENDNO AS GHA, ";
        } else {
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T2.HR_NAME, ";
            $query .= "     '2' AS GHR_DIV, ";
            $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO AS GHA, ";
        }
        $query .= "     T3.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B2 ";
        $query .= "          ON B2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND B2.YEAR         = T1.YEAR ";
        $query .= "         AND B2.BASE_SEQ     = '004' ";
        //特別クラスの場合
        if (strlen($model->hr_class) == "2") {
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO AND T4.YEAR = T1.YEAR AND T4.SEMESTER = T1.SEMESTER ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT T5 ON T5.YEAR = T4.YEAR AND T5.SEMESTER = T4.SEMESTER AND T5.GHR_CD = T4.GHR_CD, ";
            $query .= "     SCHREG_REGD_GDAT T6 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->year}' ";
        $query .= " AND T1.SEMESTER = '{$model->regdSemester}' ";
        //準ずる教育以外
        if ($model->condition == "1") {
            $query .= " AND CASE WHEN B2.BASE_REMARK1 IS NULL THEN T3.HANDICAP ELSE B2.BASE_REMARK1 END ";
            $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        } else {
            $query .= " AND CASE WHEN B2.BASE_REMARK1 IS NULL THEN T3.HANDICAP ELSE B2.BASE_REMARK1 END ";
            $query .= "         NOT IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        }
        //特別クラスの場合
        if (strlen($model->hr_class) == "2") {
            $query .= " AND T5.GHR_CD   = '{$model->hr_class}' ";
            $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->hr_class2}' ";
            $query .= " AND T1.YEAR     = T6.YEAR ";
            $query .= " AND T1.GRADE    = T6.GRADE ";
            $query .= " AND T6.SCHOOL_KIND = '{$model->gakubu_school_kind}' ";
        } else {
            $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->hr_class}' ";
        }
        //但し、左側リストにいる人(右側から左側へ移動しただけの生徒も含む)
        //GRADE_KIND_SCHREG_GROUP_DATに登録されている人は除く
        $query .= "     AND T1.SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectschno). "') ";
        $query .= "     AND T1.SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             T4.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             GRADE_KIND_SCHREG_GROUP_DAT T4 ";
        $query .= "         WHERE ";
        $query .= "             T4.YEAR = '{$model->year}' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= "             AND T4.SEMESTER <> '9' ";
        } else {
            $query .= "             AND T4.SEMESTER = '9' ";
        }
        $query .= "         AND T4.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        //特別クラスの場合
        if (strlen($model->hr_class) == "2") {
            $query .= "     T5.GHR_CD, ";
            $query .= "     T4.GHR_ATTENDNO ";
        } else {
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";
        }

        return $query;
    }

    //学籍番号登録チェックカウント
    function getSchregData($model) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$model->year}' ";
        $query .= " AND SEMESTER = '".$model->semester."' ";
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        if (strlen($model->hr_class) == "2") {
            $query .= " AND GHR_CD = '".$model->hr_class."' ";
            $query .= " AND GRADE || '-' || HR_CLASS = '".$model->hr_class2."' ";
        } else {
            $query .= " AND GHR_CD = '00' ";
            $query .= " AND GRADE || '-' || HR_CLASS = '".$model->hr_class."' ";
        }
        $query .= " AND (   CONDITION <> '".$model->condition."' ";
        $query .= "      OR GROUPCD   <> '".$model->groupcd."' ) ";
        $query .= " AND SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectschno). "') ";

        return $query;
    }

    //更新
    function &UpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (strlen($model->hr_class) == "2") {
            $ghr_cd = $model->hr_class;
            list ($grade, $hr_class) = explode('-', $model->hr_class2);
        } else {
            $ghr_cd = '00';
            list ($grade, $hr_class) = explode('-', $model->hr_class);
        }

        //①左のリストの生徒のうち、既に登録済み生徒は何もしない
        //（つまり、最初から左にいる生徒は何もしない）

        //②既に登録済み生徒のうち、左のリストにいない生徒を削除する
        //（つまり、左から右へ移動した生徒を削除する）
        $query  = " DELETE FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".$model->year."' ";
        $query .= "     AND SEMESTER    = '".$model->semester."' ";
        $query .= "     AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "     AND GHR_CD      = '".$ghr_cd."' ";
        $query .= "     AND GRADE       = '".$grade."' ";
        $query .= "     AND HR_CLASS    = '".$hr_class."' ";
        $query .= "     AND CONDITION   = '".$model->condition."' ";
        $query .= "     AND GROUPCD     = '".$model->groupcd."' ";
        $query .= "     AND SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectschno). "') ";
        $db->query($query);

        //③左のリストの生徒のうち、未登録生徒を追加する
        //（つまり、右から左へ移動した生徒を追加する）
        $query  = " INSERT INTO GRADE_KIND_SCHREG_GROUP_DAT ( ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     GAKUBU_SCHOOL_KIND, ";
        $query .= "     GHR_CD, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     CONDITION, ";
        $query .= "     GROUPCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= "   (SELECT ";
        $query .= "     YEAR, ";
        $query .= "     '{$model->semester}', ";
        $query .= "     '{$model->gakubu_school_kind}', ";
        $query .= "     '{$ghr_cd}', ";
        $query .= "     '{$grade}', ";
        $query .= "     '{$hr_class}', ";
        $query .= "     '{$model->condition}', ";
        $query .= "     '{$model->groupcd}', ";
        $query .= "     SCHREGNO, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "     SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectschno). "') ";
        $query .= "     AND YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->regdSemester}' ";
        //登録済み生徒は除く
        $query .= "     AND SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             GRADE_KIND_SCHREG_GROUP_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR        = '{$model->year}' ";
        $query .= "         AND SEMESTER    = '".$model->semester."' ";
        $query .= "         AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "         AND GHR_CD      = '".$ghr_cd."' ";
        $query .= "         AND GRADE       = '".$grade."' ";
        $query .= "         AND HR_CLASS    = '".$hr_class."' ";
        $query .= "         AND CONDITION   = '".$model->condition."' ";
        $query .= "         AND GROUPCD     = '".$model->groupcd."' ";
        $query .= "     ) ";
        $query .= "   )  ";
        $db->query($query);

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //MAX学期取得
    function getMaxSemester() {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     MAX(SEMESTER) AS MAX_SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " GROUP BY ";
        $query .= "      YEAR ";

        return $query;
    }

    //存在チェック（コピー） -- GRADE_KIND_COMP_GROUP_YMST
    function checkCompGroupYMst($year, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' ";
        $query .= " AND SEMESTER = '".$semester."' ";

        return $query;
    }

    //存在チェック（コピー） -- GRADE_KIND_SCHREG_GROUP_DAT
    function checkSchregGroupDat($year, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' ";
        $query .= " AND SEMESTER = '".$semester."' ";

        return $query;
    }

    //コピー用データ取得
    function copyCompGroupDat($model, $pre_year, $pre_semester) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$pre_year."' ";
        $query .= " AND T1.SEMESTER = '".$pre_semester."' ";
        $query .= " AND T2.YEAR     = '".$model->year."' ";
        $query .= " AND T2.SEMESTER = '".$model->semester."' ";
        $query .= " AND T1.SCHREGNO = T2.SCHREGNO ";

        return $query;
    }

    //コピー
    function &copyInsertQuery($model, $Row) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $model->semester;
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $Row["GAKUBU_SCHOOL_KIND"];
        $data["GHR_CD"][TEXT]               = $Row["GHR_CD"];
        $data["GRADE"][TEXT]                = $Row["GRADE"];
        $data["HR_CLASS"][TEXT]             = $Row["HR_CLASS"];
        $data["CONDITION"][TEXT]            = $Row["CONDITION"];
        $data["GROUPCD"][TEXT]              = $Row["GROUPCD"];
        $data["SCHREGNO"][TEXT]             = $Row["SCHREGNO"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "GRADE_KIND_SCHREG_GROUP_DAT");
        return $query;
    }
}
?>
