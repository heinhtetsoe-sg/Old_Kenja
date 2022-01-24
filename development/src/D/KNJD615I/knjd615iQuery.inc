<?php

require_once('for_php7.php');

class knjd615iQuery extends Query {

    //学期取得
    function getSemester($model="", $meisyou="")
    {
        $query  = " SELECT ";
        if ($meisyou) {
            $query .= "     SEMESTERNAME ";
        } else {
            $query .= "     SEMESTER AS VALUE, ";
            $query .= "     SEMESTERNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($meisyou) {
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

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
    function getTest($model, $semester, $grade, $meisyou="")
    {

        $query  = " SELECT ";
        if ($meisyou) {
            $query .= "     TESTITEMNAME ";
        } else {
            $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
            $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || TESTITEMNAME AS LABEL ";
        }
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
                    $query .= " AND L1.SCHOOLCD = '".SCHOOLCD."' ";
                }
            } else if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
                $query .= " AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= " AND L1.SCHOOLCD = '".SCHOOLCD."' ";
            }
        }
        if ($meisyou) {
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

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

/*************   CSV   ****************/
    function getGradeName($grade) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    function getSubclassList($model, $gradeClass="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.COURSECD || T2.MAJORCD || T2.COURSECODE AS COURSE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     V1.CLASSNAME, ";
        $query .= "     V2.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND T2.YEAR     = T1.YEAR ";
        $semester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER: $model->field["SEMESTER"];
        $query .= "                                  AND T2.SEMESTER = '{$semester}' ";
        if ($model->field["PRINT_DIV"] == "1") {
            $query .= "         AND T2.GRADE || '-' || T2.HR_CLASS = '{$gradeClass}' ";
        }
        $query .= "     LEFT JOIN V_CLASS_MST V1 ON T1.YEAR        = V1.YEAR ";
        $query .= "                             AND T1.CLASSCD     = V1.CLASSCD ";
        $query .= "                             AND V1.SCHOOL_KIND = 'H' ";//高校
        $query .= "     LEFT JOIN V_SUBCLASS_MST V2 ON T1.YEAR          = V2.YEAR ";
        $query .= "                                AND T1.CLASSCD       = V2.CLASSCD ";
        $query .= "                                AND V2.SCHOOL_KIND   = 'H' ";//高校
        $query .= "                                AND T1.CURRICULUM_CD = V2.CURRICULUM_CD ";
        $query .= "                                AND T1.SUBCLASSCD    = V2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        $query .= "     AND T2.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";

        return $query;
    }

    function getSutudentList($model, $gradeClass="", $cnt="") {
        $semester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER: $model->field["SEMESTER"];

        $query  = " SELECT ";
        if ($cnt) {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     REGD.SCHREGNO, ";
            $query .= "     J008.ABBV2 AS DOMITORY, ";
            $query .= "     CASE ";
            $query .= "         WHEN ENTGRD.ENT_DIV = '1' THEN '' ";
            $query .= "         WHEN ENTGRD.ENT_DIV = '2' THEN '後' ";
            $query .= "         WHEN ENTGRD.ENT_DIV = '3' THEN '転' ";
            $query .= "     END AS DIV,";
            $query .= "     REGDH.HR_CLASS_NAME1 AS HR_NAME, ";
            $query .= "     REGD.ATTENDNO AS NO, ";
            $query .= "     BASE.NAME, ";
            $query .= "     CCM.COURSECODENAME AS COURSE_NAME, ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ";
            $query .= "     REGD.COURSECODE, ";
            $query .= "     BASE.SEX, ";
            $query .= "     BASE.INOUTCD, ";
            $query .= "     BRANCH.RESIDENTCD, ";
            $query .= "     J008.NAMESPARE1 AS RESIDENTCD_NAMESPARE1 ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN  SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR     = REGD.YEAR ";
        $query .= "                                     AND REGDH.SEMESTER = REGD.SEMESTER ";
        $query .= "                                     AND REGDH.GRADE    = REGD.GRADE ";
        $query .= "                                     AND REGDH.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = REGD.COURSECODE ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     LEFT JOIN SCHREG_BRANCH_DAT BRANCH ON BRANCH.SCHOOLCD    = '000000000000' ";
        $query .= "                                       AND BRANCH.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
        $query .= "                                       AND BRANCH.YEAR        = REGD.YEAR ";
        $query .= "                                       AND BRANCH.SCHREGNO    = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST J008 ON J008.NAMECD1 = 'J008' ";
        $query .= "                            AND J008.NAMECD2 = BRANCH.RESIDENTCD ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO    = REGD.SCHREGNO ";
        $query .= "                                             AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT SDIV ON SDIV.YEAR     = REGD.YEAR ";
        $query .= "                                        AND SDIV.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                                        AND SDIV.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     AND SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        $query .= "     AND SDIV.CLASSCD || SDIV.SCHOOL_KIND || SDIV.CURRICULUM_CD || SDIV.SUBCLASSCD = '99' || 'H' || '99' || '999999' ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '{$semester}' ";
        $query .= "     AND REGD.GRADE    = '".$model->field["GRADE"]."' ";
        if ($model->field["PRINT_DIV"] == "1") {
            $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS = '{$gradeClass}' ";
        }
        if ($model->field["RYO_ONLY"] == "1") {
            $query .= "     AND J008.NAMESPARE1 = '4' ";
        }
        if (!$cnt) {
            $query .= " ORDER BY ";
            if ($model->field["SORT_DIV"] == "1") {
                if ($model->field["OUTPUT_KIJUN"] == "1") {
                    $query .= "     SDIV.GRADE_RANK, ";
                } else {
                    $query .= "     SDIV.GRADE_AVG_RANK, ";
                }
            }
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO ";
        }

        return $query;
    }

    function selectCsvScoreData($model, $gradeClass="") {
        $semester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER: $model->field["SEMESTER"];

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     TSCORE.CLASSCD, ";
        $query .= "     TSCORE.SCHOOL_KIND, ";
        $query .= "     TSCORE.CURRICULUM_CD, ";
        $query .= "     TSCORE.SUBCLASSCD, ";
        $query .= "     TSCORE.CLASSCD || '-' || TSCORE.SCHOOL_KIND || '-' || TSCORE.CURRICULUM_CD || '-' || TSCORE.SUBCLASSCD AS SUBCLASSCD2, ";
        $query .= "     TSCORE.SCORE, ";
        $query .= "     TSCORE.VALUE_DI, ";
        $query .= "     CAST(NULL AS DOUBLE) AS AVG, ";
        $query .= "     CAST(NULL AS SMALLINT) AS GRADE_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS CLASS_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS COURSE_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS COURSEGROUP_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS GRADE_AVG_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS CLASS_AVG_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS COURSE_AVG_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS COURSEGROUP_AVG_RANK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT TSCORE ON TSCORE.YEAR     = T1.YEAR ";
        $query .= "                                      AND TSCORE.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                                      AND TSCORE.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        $query .= "     AND TSCORE.TESTKINDCD || TSCORE.TESTITEMCD || TSCORE.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        $query .= "     AND TSCORE.VALUE_DI IS NOT NULL ";
        if ($model->field["PRINT_DIV"] == "1") {
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '{$gradeClass}' ";
        }
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SCHOOL_KIND, ";
        $query .= "     T2.CURRICULUM_CD, ";
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD2, ";
        $query .= "     T2.SCORE, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS VALUE_DI, ";
        $query .= "     T2.AVG AS AVG, ";
        $query .= "     T2.GRADE_RANK, ";
        $query .= "     T2.CLASS_RANK, ";
        $query .= "     T2.COURSE_RANK, ";
        $query .= "     T2.MAJOR_RANK AS COURSEGROUP_RANK, ";
        $query .= "     T2.GRADE_AVG_RANK, ";
        $query .= "     T2.CLASS_AVG_RANK, ";
        $query .= "     T2.COURSE_AVG_RANK, ";
        $query .= "     T2.MAJOR_AVG_RANK AS COURSEGROUP_AVG_RANK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT T2 ON T2.YEAR     = T1.YEAR ";
        $query .= "                                      AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                                      AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        $query .= "     AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        if ($model->field["PRINT_DIV"] == "1") {
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '{$gradeClass}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    function getCourseData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ";
        $query .= "     COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN COURSECODE_MST COCD ON REGD.COURSECODE = COCD.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR  = '".CTRL_YEAR."'  ";
        $query .= "     AND REGD.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.COURSECD || REGD.MAJORCD ||  REGD.COURSECODE ";

        return $query;
    }

    function selectCsvScoreAvgData($model) {
        $semester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER: $model->field["SEMESTER"];

        $query  = " WITH RANK9 AS ( ";
        $query .= " SELECT ";
        $query .= "     REGD.GRADE, REGD.HR_CLASS, REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE ";
        $query .= "     , CASE WHEN N1.NAMESPARE1 = '4' THEN '1' END AS RYOUSEI_FLG ";
        $query .= "     , T1.AVG ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_SDIV_DAT T1 ";
        $query .= "     INNER JOIN (SELECT SCHREGNO, YEAR, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE ";
        $query .= "                 FROM SCHREG_REGD_DAT ";
        $query .= "                 WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "                 AND   SEMESTER = '{$semester}' ";
        $query .= "                ) REGD ON REGD.YEAR = T1.YEAR AND REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "        LEFT JOIN SCHREG_REGD_GDAT S3 ";
        $query .= "             ON S3.YEAR         = REGD.YEAR ";
        $query .= "            AND S3.GRADE        = REGD.GRADE ";
        $query .= "        LEFT JOIN SCHREG_BRANCH_DAT S4 ";
        $query .= "             ON S4.SCHOOLCD     = '000000000000' ";
        $query .= "            AND S4.SCHOOL_KIND  = S3.SCHOOL_KIND ";
        $query .= "            AND S4.YEAR         = REGD.YEAR ";
        $query .= "            AND S4.SCHREGNO     = REGD.SCHREGNO ";
        $query .= "        LEFT JOIN V_NAME_MST N1 ";
        $query .= "             ON N1.YEAR         = REGD.YEAR ";
        $query .= "            AND N1.NAMECD1      = 'J008' ";
        $query .= "            AND N1.NAMECD2      = S4.RESIDENTCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        $query .= "     AND T1.SUBCLASSCD = '999999' ";
        $query .= "     AND REGD.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.AVG_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T2.SUBCLASSCD AS SUBCLASS_MST_SUBCLSSCD, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.HIGHSCORE, ";
        $query .= "     T1.LOWSCORE, ";
        $query .= "     T1.COUNT, ";
        $query .= "     T1.AVG, ";
        $query .= "     T1.STDDEV ";
        $query .= " FROM ";
        $query .= "  RECORD_AVERAGE_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ";
        $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.AVG_DIV IN ('1', '2', '3', '7') "; // 学科、クラス、コース
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        $query .= "  ";
        // 学年
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '1' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, '00000000' AS COURSE, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ";
        $query .= "     '999999AVG' AS SUBCLASSCD, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ";
        $query .= "     SUM(DOUBLE(AVG)) AS SCORE, ";
        $query .= "     MAX(DOUBLE(AVG)) AS HIGHSCORE, ";
        $query .= "     MIN(DOUBLE(AVG)) AS LOWSCORE, ";
        $query .= "     COUNT(DOUBLE(T1.AVG)) AS COUNT, ";
        $query .= "     AVG(DOUBLE(T1.AVG)) AS AVG, ";
        $query .= "     STDDEV(DOUBLE(T1.AVG)) AS STDDEV ";
        $query .= " FROM ";
        $query .= "     RANK9 T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE ";
        // 年組
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '2' AS AVG_DIV, T1.GRADE, T1.HR_CLASS, '00000000' AS COURSE, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ";
        $query .= "     '999999AVG' AS SUBCLASSCD, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ";
        $query .= "     SUM(DOUBLE(AVG)) AS SCORE, ";
        $query .= "     MAX(DOUBLE(AVG)) AS HIGHSCORE, ";
        $query .= "     MIN(DOUBLE(AVG)) AS LOWSCORE, ";
        $query .= "     COUNT(DOUBLE(T1.AVG)) AS COUNT, ";
        $query .= "     AVG(DOUBLE(T1.AVG)) AS AVG, ";
        $query .= "     STDDEV(DOUBLE(T1.AVG)) AS STDDEV ";
        $query .= " FROM ";
        $query .= "     RANK9 T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, T1.HR_CLASS ";
        // コース
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '3' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, T1.COURSE AS COURSE, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ";
        $query .= "     '999999AVG' AS SUBCLASSCD, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ";
        $query .= "     SUM(DOUBLE(AVG)) AS SCORE, ";
        $query .= "     MAX(DOUBLE(AVG)) AS HIGHSCORE, ";
        $query .= "     MIN(DOUBLE(AVG)) AS LOWSCORE, ";
        $query .= "     COUNT(DOUBLE(T1.AVG)) AS COUNT, ";
        $query .= "     AVG(DOUBLE(T1.AVG)) AS AVG, ";
        $query .= "     STDDEV(DOUBLE(T1.AVG)) AS STDDEV ";
        $query .= " FROM ";
        $query .= "     RANK9 T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, T1.COURSE ";
        // 寮
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '7' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, '00000000' AS COURSE, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ";
        $query .= "     '999999AVG' AS SUBCLASSCD, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ";
        $query .= "     SUM(DOUBLE(AVG)) AS SCORE, ";
        $query .= "     MAX(DOUBLE(AVG)) AS HIGHSCORE, ";
        $query .= "     MIN(DOUBLE(AVG)) AS LOWSCORE, ";
        $query .= "     COUNT(DOUBLE(T1.AVG)) AS COUNT, ";
        $query .= "     AVG(DOUBLE(T1.AVG)) AS AVG, ";
        $query .= "     STDDEV(DOUBLE(T1.AVG)) AS STDDEV ";
        $query .= " FROM ";
        $query .= "     RANK9 T1 ";
        $query .= " WHERE ";
        $query .= "     RYOUSEI_FLG = '1' ";
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE ";

        return $query;
    }

    function getGradeHrclassData($model) {
        $semester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER: $model->field["SEMESTER"];

        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.HR_CLASS_NAME1, ";
        $query .= "     COUNT(REGD.SCHREGNO) AS REGD_COUNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1  ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR     = T1.YEAR ";
        $query .= "                                    AND REGD.SEMESTER = T1.SEMESTER ";
        $query .= "                                    AND REGD.GRADE    = T1.GRADE ";
        $query .= "                                    AND REGD.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.HR_CLASS_NAME1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

        return $query;
    }
}
?>
