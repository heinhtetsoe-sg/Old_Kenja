<?php

require_once('for_php7.php');

class knjs580Query extends Query {

    //年度取得
    function selectYearQuery($year)
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '" .SCHOOLKIND ."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年の最小値（データが存在するものの中で）
    function getMinGrade()
    {
        $gradequery  = " SELECT DISTINCT";
        $gradequery .= "     GD.GRADE AS VALUE ";
        $gradequery .= " FROM ";
        $gradequery .= "     SCHREG_REGD_GDAT GD ";
        $gradequery .= "     INNER JOIN UNIT_STUDY_CLASS_DAT L1 ON L1.GRADE = GD.GRADE ";
        $gradequery .= "                                        AND L1.YEAR = GD.YEAR ";
        $gradequery .= " WHERE ";
        $gradequery .= "     L1.YEAR = '".CTRL_YEAR."' ";
        $gradequery .= " ORDER BY ";
        $gradequery .= "     VALUE ";

        return $gradequery;
    }

    /* 科目取得 */
    function getUnitStudySubclass($model)
    {
        $query  = " SELECT ";
        $query .= "     L1.SUBCLASSABBV AS LABEL, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     UNIT_STUDY_CLASS_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND L1.CLASSCD       = T1.CLASSCD ";
            $query .= "     AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "     AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /* 出版社 */
    function getIssueCompany($model, $subclassCd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' && strlen($subclassCd) > 6) {
            $classCd        = substr($subclassCd, 0, 2);
            $school_kind    = substr($subclassCd, 3, 1);
            $curriculum_cd  = substr($subclassCd, 5, 1);
            $subclassCd     = substr($subclassCd, 7, 6);
        }
        $query  = " SELECT ";
        $query .= "     T1.ISSUECOMPANYCD AS VALUE, ";
        $query .= "     L1.ISSUECOMPANYABBV AS LABEL ";
        $query .= " FROM ";
        $query .= "     UNIT_MST T1 ";
        $query .= "     INNER JOIN ISSUECOMPANY_MST L1 ON L1.ISSUECOMPANYCD = T1.ISSUECOMPANYCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD       = '".$classCd."' ";
            $query .= "     AND T1.SCHOOL_KIND   = '".$school_kind."' ";
            $query .= "     AND T1.CURRICULUM_CD = '".$curriculum_cd."' ";
        }
        $query .= "     AND T1.SUBCLASSCD = '".$subclassCd."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.ISSUECOMPANYCD, ";
        $query .= "     L1.ISSUECOMPANYABBV ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科書取得
    function getTextIssue($model, $subclassCd)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' && strlen($subclassCd) > 6) {
            $classCd        = substr($subclassCd, 0, 2);
            $school_kind    = substr($subclassCd, 3, 1);
            $curriculum_cd  = substr($subclassCd, 5, 1);
            $subclassCd     = substr($subclassCd, 7, 6);
        }
        $query  = " SELECT ";
        $query .= "     ISSUECOMPANYCD ";
        $query .= " FROM ";
        $query .= "     UNIT_STUDY_TEXT_BOOK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD       = '".$classCd."' ";
            $query .= "     AND SCHOOL_KIND   = '".$school_kind."' ";
            $query .= "     AND CURRICULUM_CD = '".$curriculum_cd."' ";
        }
        $query .= "     AND SUBCLASSCD = '".$subclassCd."' ";

        return $query;
    }

    /* イベントデータの更新 */
    function delInsSql($db, $model)
    {
        $query  = "DELETE FROM UNIT_STUDY_TEXT_BOOK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        $db->query($query);
        foreach ($model->updMeisai as $subclassCd => $issuecompanyCd) {
            if (strlen($model->updMeisai[$subclassCd]) == 0) {
                continue;
            } else {
                $data["YEAR"][TEXT]             = $model->field["YEAR"];
                $data["GRADE"][TEXT]            = $model->field["GRADE"];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]       = substr($subclassCd, 0, 2);
                    $data["SCHOOL_KIND"][TEXT]   = substr($subclassCd, 3, 1);
                    $data["CURRICULUM_CD"][TEXT] = substr($subclassCd, 5, 1);
                    $data["SUBCLASSCD"][TEXT]    = substr($subclassCd, 7, 6);
                } else {
                    $data["SUBCLASSCD"][TEXT]       = $subclassCd;
                }
                $data["ISSUECOMPANYCD"][TEXT]   = $issuecompanyCd;
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";
                $query = Query::insertSQL($data, "UNIT_STUDY_TEXT_BOOK_DAT");
                $db->query($query);
            }
        }

        return true;
    }
}
?>
