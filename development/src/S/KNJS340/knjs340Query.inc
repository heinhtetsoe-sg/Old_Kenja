<?php

require_once('for_php7.php');

class knjs340Query extends Query {

    //年組取得
    function getGradeHrClass($model)
    {
        $query = "";
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= " SELECT ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM V_STAFF_HR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
            }
            $query .= "     AND T1.HR_CLASS_NAME1 IN (  SELECT ";
            $query .= "                                    MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
            $query .= "                                 FROM ";
            $query .= "                                     V_STAFF_HR_DAT M1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR     = M1.YEAR AND ";
            $query .= "                                     T1.SEMESTER = M1.SEMESTER AND ";
            $query .= "                                     T1.HR_CLASS = M1.HR_CLASS AND ";
            $query .= "                                     T1.SCHOOL_KIND = M1.SCHOOL_KIND AND ";
            $query .= "                                     T1.STAFFCD  = M1.STAFFCD ";
            $query .= "                                 ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.HR_CLASS_NAME1, ";
            $query .= "     T1.SCHOOL_KIND ";
            $query .= " ORDER BY ";
            $query .= "     T1.SCHOOL_KIND DESC, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     LABEL ";
        } else if($model->field["HR_CLASS_TYPE"] == "2") {
            $query  = " SELECT DISTINCT ";
            if ($model->Properties["useFi_Hrclass"] != '1' && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     GHR_CD AS VALUE, ";
                $query .= "     GHR_NAME AS LABEL ";
            } else {
                $query .= "     GRADE || HR_CLASS AS VALUE, ";
                $query .= "     HR_NAME AS LABEL ";
            }
            $query .= " FROM ";
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_FI_HDAT ";
            } else if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_GHR_HDAT ";
            }
            $query .= " WHERE ";
            $query .= "     YEAR         = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->Properties["useFi_Hrclass"] == '1' && $model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND GRADE IN (SELECT ";
                    $query .= "                       REGD_G.GRADE ";
                    $query .= "                   FROM ";
                    $query .= "                       SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                   WHERE ";
                    $query .= "                       REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                       AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useFi_Hrclass"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                       REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                       SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                       REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                       AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "     REGD_H.YEAR           = '".CTRL_YEAR."' ";
            $query .= "     AND REGD_H.SEMESTER   = '".CTRL_SEMESTER."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND REGD_H.GRADE IN (SELECT ";
                    $query .= "                              REGD_G.GRADE ";
                    $query .= "                          FROM ";
                    $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                          WHERE ";
                    $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                              AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                              REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                              AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //学期情報取得
    function getSemesAll() {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR  = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";

        return $query;
    }
    //対象月データ取得
    function getMonthName($month, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                if ($model->field["GAKUNEN_KONGOU"] == "1" || $model->field["HR_CLASS_TYPE"] != "1") {
                    $setNameCd = "Z".SCHOOLKIND."05";
                } else {
                    $setNameCd  = "Z".$model->schKind."05";
                }
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '{$setNameCd}' AND ";
        $query .= "     NAMECD2 = '".sprintf('%02d',$month)."' ";

        return $query;
    }
    
    //チェック
    function checker($year, $semester) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     MONTH(SDATE) AS SMONTH, ";
        $query .= "     DAY(SDATE) AS SDAY, ";
        $query .= "     MONTH(EDATE) AS EMONTH, ";
        $query .= "     DAY(EDATE) AS EDAY     ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER = '{$semester}' ";

        return $query;
    }

}
?>
