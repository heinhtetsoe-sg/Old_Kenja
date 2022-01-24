<?php

require_once('for_php7.php');

class knjd617vQuery extends Query {

    //名称取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($semester, $model, $div = "")
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "  INNER JOIN SCHREG_REGD_DAT T3 ON ";
            $query .= "    T3.YEAR = T1.YEAR ";
            $query .= "    AND T3.SEMESTER = T1.SEMESTER ";
            $query .= "    AND T3.GRADE = T1.GRADE ";
            $query .= "    AND T3.HR_CLASS = T1.HR_CLASS ";
            $query .= "    AND T3.COURSECD || '-' || T3.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($semester !== '9') {
            $query .= "     AND T1.SEMESTER = '".$semester."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= "     AND T2.SCHOOL_KIND <> 'P' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($div == "GRADE") {
        } else {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND ((TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
            $query .= "         OR (T1.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト取得
    function getTest($model, $semester, $grade)
    {

        $query  = " SELECT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
            $query .= "     INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT L1 ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
            $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT L1 ";
        }
        $query .= "              ON L1.YEAR = T1.YEAR ";
        $query .= "             AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "             AND L1.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "             AND L1.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "             AND L1.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "             AND L1.CLASSCD = '00' ";
        $query .= "             AND L1.CURRICULUM_CD = '00' ";
        $query .= "             AND L1.SUBCLASSCD = '000000' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "             AND L1.GRADE = T1.GRADE ";
            $query .= "             AND L1.COURSECD = T1.COURSECD ";
            $query .= "             AND L1.MAJORCD= T1.MAJORCD ";
            $query .= "             AND L1.SCHOOLCD = T1.SCHOOLCD ";
            $query .= "             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }

        $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ";
        $query .= "              ON L2.YEAR = T1.YEAR ";
        $query .= "             AND L2.GRADE = '".$grade."' ";
        $query .= "             AND L2.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "             AND T1.GRADE = '00' ";
            $query .= "             AND T1.COURSECD || '-' || L1.MAJORCD = '".$model->field["MAJOR"]."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
                $query .= " AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= " AND L1.SCHOOLCD = '".SCHOOLCD."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getGdat($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     GRADE = '$grade' ";

        return $query;
    }

    function getCourseMajor($model) {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE, ";
        $query .= "     A043.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST VCM ";
        $query .= "     LEFT JOIN NAME_MST A043 ON NAMECD1 = 'A043' ";
        $query .= "          AND VCM.COURSECD || VCM.MAJORCD = A043.NAME2 || A043.NAME3 ";
        $query .= " WHERE ";
        $query .= "     VCM.YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     VALUE(NAME1, 'H') = '".SCHOOLKIND."' ";
        return $query;
    }

    /****************/
    /*  ＣＳＶ出力  */
    /****************/
    //元号取得
    function getNameMstL007() {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'L007' ";
        $query .= "     AND '".CTRL_YEAR."' BETWEEN NAMESPARE1 AND ABBV3 ";

        return $query;
    }

    //学年名称取得
    function getGradeName($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //学期名称取得
    function getSemesterName($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";

        return $query;
    }

    //グループ取得
    function getGroup($model, $gradeHr='') {
        $query  = " SELECT DISTINCT ";
        if ("1" == $model->field["GROUP_DIV"]) {
            $query .= "     T1.GRADE AS GROUP ";
        } else if ("2" == $model->field["GROUP_DIV"]) {
            $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS GROUP ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT T2 ON T2.YEAR       = T1.YEAR ";
        $query .= "                                     AND T2.GRADE      = T1.GRADE ";
        $query .= "                                     AND T2.COURSECD   = T1.COURSECD ";
        $query .= "                                     AND T2.MAJORCD    = T1.MAJORCD ";
        $query .= "                                     AND T2.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.YEAR  = '" . CTRL_YEAR . "' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "     AND T1.SEMESTER = '" .CTRL_SEMESTER. "' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        if ("1" == $model->field["PRINT_DIV"]) {
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '".$gradeHr."' ";
        }
        if ("1" == $model->Properties["use_school_detail_gcm_dat"]) {
            $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GROUP ";

        return $query;
    }

    //科目取得
    function GetSubclass($model, $group, $gradeHr = "") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     VALUE(SUB_M.SUBCLASSABBV, SUB_M.SUBCLASSNAME) AS SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND T2.YEAR     = T1.YEAR ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "                              AND T2.SEMESTER = '" .CTRL_SEMESTER. "' ";
        } else {
            $query .= "                              AND T2.SEMESTER = T1.SEMESTER ";
        }
        if ("1" == $model->field["PRINT_DIV"]) {
            $query .= "                              AND T2.GRADE || '-' || T2.HR_CLASS = '".$gradeHr."' ";
        }
        if ("1" == $model->Properties["use_school_detail_gcm_dat"]) {
            $query .= "                              AND T2.COURSECD || '-' || T2.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST SUB_M ON SUB_M.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "                                 AND SUB_M.CLASSCD       = T1.CLASSCD ";
        $query .= "                                 AND SUB_M.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "                                 AND SUB_M.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        $query .= "     AND T2.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND substr(T1.SUBCLASSCD, 1, 2) < '90' ";

        if ("2" == $model->field["GROUP_DIV"]) {
            $query .= "     AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '".$group."' ";
        }
        $query .= "     AND T1.CLASSCD || '-' || ";
        $query .= "         T1.SCHOOL_KIND || '-' || ";
        if (substr($model->field["TESTCD"], 0, 2) != '99') {
            $query .= "         T1.CURRICULUM_CD || '-' || ";
            $query .= "         T1.SUBCLASSCD ";
        } else {
            $query .= "         T1.CURRICULUM_CD ";
        }
        $query .= "                       not in (SELECT ";
        $query .= "                                   T1.COMBINED_CLASSCD || '-' || ";
        $query .= "                                   T1.COMBINED_SCHOOL_KIND || '-' || ";
        if (substr($model->field["TESTCD"], 0, 2) != '99') {
            $query .= "                                   T1.COMBINED_CURRICULUM_CD || '-' || ";
            $query .= "                                   T1.COMBINED_SUBCLASSCD ";
        } else {
            $query .= "                                   T1.COMBINED_CURRICULUM_CD ";
        }
        $query .= "                               FROM ";
        $query .= "                                   SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "                               WHERE ";
        $query .= "                                   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                               ) ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD as SUBCLASSCD, ";
        $query .= "     VALUE(SUB_M.SUBCLASSABBV, SUB_M.SUBCLASSNAME) AS SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT T2 ON T2.YEAR     = T1.YEAR ";
        $query .= "                                      AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                                      AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SUBCLASS_MST SUB_M ON SUB_M.SUBCLASSCD    = T2.SUBCLASSCD ";
        $query .= "                                  AND SUB_M.CLASSCD       = T2.CLASSCD ";
        $query .= "                                  AND SUB_M.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        $query .= "                                  AND SUB_M.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "         T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.YEAR  = '" . CTRL_YEAR . "' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "     AND T1.SEMESTER = '" .CTRL_SEMESTER. "' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        if ("1" == $model->field["PRINT_DIV"]) {
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '".$gradeHr."' ";
        }
        if ("1" == $model->Properties["use_school_detail_gcm_dat"]) {
            $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= "     AND (T2.SUBCLASSCD = '999999' OR substr(T2.SUBCLASSCD, 1, 2) < '90') ";
        $query .= " ORDER BY ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    // クラス情報取得
    function getHrclass($model, $gruopCd, $gradeHr = "") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR     = T1.YEAR ";
        $query .= "                                  AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                                  AND T2.GRADE    = T1.GRADE ";
        $query .= "                                  AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" . CTRL_YEAR . "' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "     AND T1.SEMESTER = '" .CTRL_SEMESTER. "' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        if ("2" == $model->field["GROUP_DIV"]) {
            $query .= "     AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '".$gruopCd."' ";
        }
        if ("1" == $model->field["PRINT_DIV"]) {
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '".$gradeHr."' ";
        }
        if ("1" == $model->Properties["use_school_detail_gcm_dat"]) {
            $query .= "     AND T2.COURSECD || '-' || T2.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, T1.HR_CLASS ";

        return $query;
    }

    //各クラスの平均等情報を取得
    function getAverageInfo($model) {
        $query  = " SELECT ";
        $query .= "     T1.AVG_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.HIGHSCORE, ";
        $query .= "     T1.LOWSCORE, ";
        $query .= "     T1.AVG, ";
        $query .= "     T1.STDDEV ";
        $query .= " FROM ";
        $query .= "     RECORD_AVERAGE_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '" . CTRL_YEAR . "' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.AVG_DIV IN ('1', '2', '4', '5') ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        $query .= "     AND (T1.SUBCLASSCD = '999999' OR substr(T1.SUBCLASSCD, 1, 2) < '90') ";
        $query .= " ORDER BY ";
        $query .= "     T1.AVG_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.HR_CLASS ";

        return $query;
    }

    //生徒情報取得
    function getStudentInfo($model, $groupCd, $gradeHr = "") {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.INOUTCD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN  SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR     = T1.YEAR ";
        $query .= "                                      AND REGDH.SEMESTER = T1.SEMESTER ";
        $query .= "                                      AND REGDH.GRADE    = T1.GRADE ";
        $query .= "                                      AND REGDH.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT SDIV ";
        $query .= "         ON SDIV.YEAR     = T1.YEAR ";
        $query .= "        AND SDIV.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "        AND SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        $query .= "        AND SDIV.CLASSCD || SDIV.SCHOOL_KIND || SDIV.CURRICULUM_CD || SDIV.SUBCLASSCD = '99H99999999' ";
        $query .= "        AND SDIV.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '" . CTRL_YEAR . "' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "     AND T1.SEMESTER = '" .CTRL_SEMESTER. "' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        if ("2" == $model->field["GROUP_DIV"]) {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$groupCd."' ";
        }
        if ("1" == $model->field["PRINT_DIV"]) {
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '".$gradeHr."' ";
        }
        if ("1" == $model->Properties["use_school_detail_gcm_dat"]) {
            $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " ORDER BY ";
        if ("2" == $model->field["SORT_DIV"]) {
            if ("2" == $model->field["OUTPUT_KIJUN"]) {
                $query .= "     GRADE_AVG_RANK, ";
            } else {
                $query .= "     GRADE_RANK, ";
            }
        }
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //順位等取得
    function getRankInfo($model, $gradeHr = "") {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SCHOOL_KIND, ";
        $query .= "     T2.CURRICULUM_CD, ";
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T2.SCORE, ";
        $query .= "     T2.AVG, ";
        if ("2" == $model->field["OUTPUT_KIJUN"]) {
            $query .= "     T2.GRADE_AVG_RANK AS GRADE_RANK, ";
            $query .= "     T2.CLASS_AVG_RANK AS CLASS_RANK, ";
            $query .= "     T2.COURSE_AVG_RANK AS COURSE_RANK, ";
            $query .= "     T2.MAJOR_AVG_RANK AS COURSEGROUP_RANK, ";
            $query .= "     T2.MAJOR_AVG_RANK AS MAJOR_RANK ";
        } else {
            $query .= "     T2.GRADE_RANK, ";
            $query .= "     T2.CLASS_RANK, ";
            $query .= "     T2.COURSE_RANK, ";
            $query .= "     T2.MAJOR_RANK AS COURSEGROUP_RANK, ";
            $query .= "     T2.MAJOR_RANK AS MAJOR_RANK ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT T2 ON T2.YEAR     = T1.YEAR ";
        $query .= "                                      AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                                      AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.YEAR  = '" . CTRL_YEAR . "' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "     AND T1.SEMESTER = '" .CTRL_SEMESTER. "' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        if ("1" == $model->field["PRINT_DIV"]) {
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '".$gradeHr."' ";
        }
        if ("1" == $model->Properties["use_school_detail_gcm_dat"]) {
            $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= "     AND (T2.SUBCLASSCD = '999999' OR substr(T2.SUBCLASSCD, 1, 2) < '90') ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SCHOOL_KIND, ";
        $query .= "     T2.CURRICULUM_CD, ";
        $query .= "     T2.SUBCLASSCD ";

        return $query;
    }

    function getSubclassGroup($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.GROUP_DIV ";
        $query .= "   , T3.GROUP_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR     = T1.YEAR ";
        $query .= "                                  AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                                  AND T2.GRADE    = T1.GRADE ";
        $query .= "                                  AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN REC_SUBCLASS_GROUP_MST T3 ON T3.YEAR     = T1.YEAR ";
        $query .= "                                  AND T3.GRADE    = T2.GRADE ";
        $query .= "                                  AND T3.COURSECD = T2.COURSECD ";
        $query .= "                                  AND T3.MAJORCD = T2.MAJORCD ";
        $query .= "                                  AND T3.COURSECODE = T2.COURSECODE ";
        $query .= "     INNER JOIN REC_SUBCLASS_GROUP_DAT T4 ON T4.YEAR = T3.YEAR ";
        $query .= "                                  AND T4.GROUP_DIV = T3.GROUP_DIV ";
        $query .= "                                  AND T4.GRADE    = T3.GRADE ";
        $query .= "                                  AND T4.COURSECD = T3.COURSECD ";
        $query .= "                                  AND T4.MAJORCD = T3.MAJORCD ";
        $query .= "                                  AND T4.COURSECODE = T3.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" . CTRL_YEAR . "' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "     AND T1.SEMESTER = '" .CTRL_SEMESTER. "' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        if ("1" == $model->Properties["use_school_detail_gcm_dat"]) {
            $query .= "     AND T2.COURSECD || '-' || T2.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= "     AND T3.GROUP_DIV IN ('3', '5') ";

        return $query;
    }

}
?>
