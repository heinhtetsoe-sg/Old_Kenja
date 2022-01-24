<?php

require_once('for_php7.php');


class knjd659Query extends Query {

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

    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //学年取得(智辯)
    function getGradeChiben($semester, $model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD1 = 'A023' ";
        $query .= "                 AND L1.NAME1   = 'J' ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD1 = 'A023' ";
        $query .= "                 AND L2.NAME1   = 'H' ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR ";
        $query .= "     AND T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '{$semester}' ";
        $query .= "     AND T1.GRADE    = T2.GRADE ";
        $query .= "     AND (T1.GRADE BETWEEN L1.NAME2 AND L1.NAME3 ";
        $query .= "     OR   T1.GRADE BETWEEN L2.NAME2 AND L2.NAME3) ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T2.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T2.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T2.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得（権限チェック）
    function getGrade($semester, $model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR ";
        $query .= "     AND T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$semester."' ";
        $query .= "     AND T1.GRADE    = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T2.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T2.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T2.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($semester, $grade)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND GRADE = '".$grade."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //SEMESTER_MSTの取得
    function getSemesterMst($model, $field)
    {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";

        return $query;
    }

    //SCHREG_REGD_GDATの取得
    function getSchregRegdGdat($model, $field)
    {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //観点(ヘッダ)の取得
    function getJviewSql($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T1.SUBCLASSCD, ";
        }
        $query .= "     L2.SUBCLASSNAME, ";
        $query .= "     L2.SUBCLASSABBV, ";
        $query .= "     substr(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ";
        $query .= "     L3.CLASSNAME, ";
        $query .= "     L3.CLASSABBV, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     L1.VIEWNAME, ";
        $query .= "     L1.VIEWABBV, ";
        $query .= "     L3.ELECTDIV ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_MST L1 ON L1.GRADE = T1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "          AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "          AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND L1.VIEWCD = T1.VIEWCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L2.CLASSCD = T1.CLASSCD ";
            $query .= "          AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "          AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN CLASS_MST L3 ON L3.CLASSCD = substr(T1.SUBCLASSCD, 1, 2) ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND substr(T1.SUBCLASSCD, 1, 2) < '90' ";
        $query .= " ORDER BY ";
        if($model->schoolName != "chiyoda"){
            $query .= "     VALUE(L3.ELECTDIV, '0'), ";
        }
        $query .= "     L3.SHOWORDER4, ";
        $query .= "     L3.CLASSCD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1.SHOWORDER, ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }

    //生徒の取得
    function getStudentInfo($model, $schoolKind, $gradeHrClass, $semester)
    {
        $query  = " SELECT ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     EG.ENT_DATE, ";
        $query .= "     EG.ENT_DIV, ";
        $query .= "     EG.ENT_REASON, ";
        $query .= "     EG.GRD_DATE, ";
        $query .= "     EG.GRD_DIV, ";
        $query .= "     EG.GRD_REASON, ";
        $query .= "     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ";
        $query .= "     CM.COURSENAME, ";
        $query .= "     MM.MAJORNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EG ON EG.SCHREGNO = REGD.SCHREGNO ";
        $query .= "         AND EG.SCHOOL_KIND = '".$schoolKind."' ";
        $query .= "     LEFT JOIN COURSE_MST CM ON CM.COURSECD = REGD.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST MM ON MM.COURSECD = REGD.COURSECD AND MM.MAJORCD = REGD.MAJORCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ";
        $query .= "           AND HDAT.YEAR     = REGD.YEAR ";
        $query .= "           AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "           AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".$semester."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$gradeHrClass."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //観点(明細)の取得
    function getJviewStatSql($model, $schregno)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T1.SUBCLASSCD, ";
        }
        $query .= "     T1.VIEWCD, ";
        $query .= "     L3.ELECTDIV, ";
        $query .= "     JRD.STATUS ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_MST L1 ON L1.GRADE = T1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "          AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "          AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND L1.VIEWCD = T1.VIEWCD ";
        $query .= "     LEFT JOIN CLASS_MST L3 ON L3.CLASSCD = substr(T1.SUBCLASSCD, 1, 2) ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN JVIEWSTAT_RECORD_DAT JRD ON JRD.YEAR = T1.YEAR ";
        $query .= "          AND JRD.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "          AND JRD.SCHREGNO = '".$schregno."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND JRD.CLASSCD = T1.CLASSCD ";
            $query .= "          AND JRD.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "          AND JRD.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "          AND JRD.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND JRD.VIEWCD = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND substr(T1.SUBCLASSCD, 1, 2) < '90' ";
        $query .= " ORDER BY ";
        if($model->schoolName != "chiyoda"){
            $query .= "     VALUE(L3.ELECTDIV, '0'), ";
        }
        $query .= "     L3.SHOWORDER4, ";
        $query .= "     L3.CLASSCD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1.SHOWORDER, ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }
    
    //評定の取得
    function getHyoukaSql($model, $schregno, $subClassCd)
    {
        $query  = " SELECT ";
        if ($model->Properties["useRecordDat"] == 'KIN_RECORD_DAT') {
            if ("1" == $model->field["SEMESTER"]) {
                $query .= "     SEM1_ASSESS AS STATUS ";
            } elseif ("2" == $model->field["SEMESTER"]) {
                $query .= "     SEM2_ASSESS AS STATUS ";
            } elseif ("3" == $model->field["SEMESTER"]) {
                $query .= "     SEM3_ASSESS AS STATUS ";
            } else {
                $query .= "     GRADE_ASSESS AS STATUS ";
            }
        } else {
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV" == $model->Properties["useTestCountflg"] || "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV" == $model->Properties["useTestCountflg"]) {
                $query .= "     SCORE AS STATUS ";
            } else {
                $query .= "     VALUE AS STATUS ";
            }
        }
        $query .= " FROM ";
        if ($model->Properties["useRecordDat"] == 'KIN_RECORD_DAT') {
            $query .= "     KIN_RECORD_DAT ";
        } else {
            $query .= "     RECORD_SCORE_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useRecordDat"] == 'KIN_RECORD_DAT') {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subClassCd."' ";
            $query .= "     AND SCHREGNO = '".$schregno."' ";
        } else {
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND TESTKINDCD || TESTITEMCD = '9900' ";
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV" == $model->Properties["useTestCountflg"] || "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV" == $model->Properties["useTestCountflg"]) {
                if ("9" == $model->field["SEMESTER"]) {
                    $query .= "     AND SCORE_DIV = '09' ";
                } else {
                    $query .= "     AND SCORE_DIV = '08' ";
                }
            } else {
                $query .= "     AND SCORE_DIV = '00' ";
            }
            $query .= "     AND SCHREGNO = '".$schregno."' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subClassCd."' ";
            } else {
                $query .= "     AND SUBCLASSCD = '".$subClassCd."' ";
            }
        }
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND ||  '-' ||CURRICULUM_CD || '-' || SUBCLASSCD ";
        } else {
            $query .= "     SUBCLASSCD ";
        }

        return $query;
    }

}
?>
