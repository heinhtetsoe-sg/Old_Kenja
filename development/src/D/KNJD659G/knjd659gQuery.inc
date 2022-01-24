<?php

class knjd659gQuery extends Query
{

    //学期取得
    public function getSemester()
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
    public function getSchoolName()
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

    //学年取得（権限チェック）
    public function getGrade($semester, $model)
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
                $query .= " AND T1.SCHOOL_KIND IN ('".implode("','", explode(':', $model->selectSchoolKind))."') ";
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
    public function getHrClass()
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
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

    //生徒取得
    public function getStudent($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO || '-' || T1.ATTENDNO AS VALUE, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //観点(ヘッダ)の取得
    public function getJviewSql($model)
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
        if ($model->schoolName != "chiyoda") {
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
    public function getStudentInfo($model, $semester)
    {
        $seme = $semester == "9" ? CTRL_SEMESTER : $semester;

        $query  = " SELECT ";
        $query .= "     REGD.YEAR, ";
        $query .= "     ".$semester." AS SEMESTER, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     HDAT.GRADE, ";
        $query .= "     HDAT.HR_CLASS_NAME1, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ";
        $query .= "           AND HDAT.YEAR     = REGD.YEAR ";
        $query .= "           AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "           AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".$seme."' ";
        $query .= "     AND REGD.SCHREGNO IN (".implode(", ", $model->selectSchregno).") ";
        $query .= " ORDER BY ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //観点(明細)の取得
    public function getJviewStatSql($model, $schregno, $semester)
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
        $query .= "          AND JRD.SEMESTER = '".$semester."' ";
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
        if ($model->schoolName != "chiyoda") {
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
    public function getHyoukaSql($model, $schregno, $subClassCd, $semester)
    {
        $query  = " SELECT ";
        if ($model->Properties["useRecordDat"] == 'KIN_RECORD_DAT') {
            if ("1" == $semester) {
                $query .= "     SEM1_ASSESS AS STATUS ";
            } elseif ("2" == $semester) {
                $query .= "     SEM2_ASSESS AS STATUS ";
            } elseif ("3" == $semester) {
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
            $query .= "     AND SEMESTER = '".$semester."' ";
            $query .= "     AND TESTKINDCD || TESTITEMCD = '9900' ";
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV" == $model->Properties["useTestCountflg"] || "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV" == $model->Properties["useTestCountflg"]) {
                if ("9" == $semester) {
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
