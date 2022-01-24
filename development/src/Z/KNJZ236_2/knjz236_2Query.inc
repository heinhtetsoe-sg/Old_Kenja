<?php

require_once('for_php7.php');

class knjz236_2Query extends Query
{
    //代替先科目（普通）一覧取得
    function selectQuery1_1($model)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SUBSTITUTION_CLASSCD || '-' || T1.SUBSTITUTION_SCHOOL_KIND || '-' || T1.SUBSTITUTION_CURRICULUM_CD || '-' || T1.SUBSTITUTION_SUBCLASSCD AS VALUE_SUB, ";
            $query .= "     S1.SUBCLASSNAME AS SUB_NAME, ";
            $query .= "     T1.SUBSTITUTION_CLASSCD, ";
            $query .= "     T1.SUBSTITUTION_SCHOOL_KIND, ";
            $query .= "     T1.SUBSTITUTION_CURRICULUM_CD, ";
            $query .= "     T1.SUBSTITUTION_SUBCLASSCD, ";
        } else {
            $query .= "     T1.SUBSTITUTION_SUBCLASSCD AS VALUE_SUB, ";
            $query .= "     S1.SUBCLASSNAME AS SUB_NAME, ";
        }
        $query .= "     COUNT(T1.SUBSTITUTION_SUBCLASSCD) AS CNT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_SUBSTITUTION_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT T2 ON ";
        $query .= "                  T1.YEAR = T2.YEAR ";
        $query .= "             AND  T1.SUBSTITUTION_SUBCLASSCD = T2.SUBSTITUTION_SUBCLASSCD ";
        $query .= "             AND  T1.ATTEND_SUBCLASSCD = T2.ATTEND_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND  T1.SUBSTITUTION_CLASSCD        = T2.SUBSTITUTION_CLASSCD ";
            $query .= "         AND  T1.SUBSTITUTION_SCHOOL_KIND    = T2.SUBSTITUTION_SCHOOL_KIND ";
            $query .= "         AND  T1.SUBSTITUTION_CURRICULUM_CD  = T2.SUBSTITUTION_CURRICULUM_CD ";
            $query .= "         AND  T1.ATTEND_CLASSCD              = T2.ATTEND_CLASSCD ";
            $query .= "         AND  T1.ATTEND_SCHOOL_KIND          = T2.ATTEND_SCHOOL_KIND ";
            $query .= "         AND  T1.ATTEND_CURRICULUM_CD        = T2.ATTEND_CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST S1 ON T1.SUBSTITUTION_SUBCLASSCD = S1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND  T1.SUBSTITUTION_CLASSCD         = S1.CLASSCD ";
            $query .= "         AND  T1.SUBSTITUTION_SCHOOL_KIND     = S1.SCHOOL_KIND ";
            $query .= "         AND  T1.SUBSTITUTION_CURRICULUM_CD   = S1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        //親画面からのパラメータ（代替先科目）
        $query .= "     AND T1.SUBSTITUTION_SUBCLASSCD = '".$model->param_subclass."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.SUBSTITUTION_CLASSCD        = '".$model->param_class."' ";
            $query .= "     AND T1.SUBSTITUTION_SCHOOL_KIND    = '".$model->param_schoolkind."' ";
            $query .= "     AND T1.SUBSTITUTION_CURRICULUM_CD  = '".$model->param_curriculumcd."' ";
        }
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SUBSTITUTION_CLASSCD, ";
            $query .= "     T1.SUBSTITUTION_SCHOOL_KIND, ";
            $query .= "     T1.SUBSTITUTION_CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBSTITUTION_SUBCLASSCD, ";
        $query .= "     S1.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SUBSTITUTION_CLASSCD, ";
            $query .= "     T1.SUBSTITUTION_SCHOOL_KIND, ";
            $query .= "     T1.SUBSTITUTION_CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBSTITUTION_SUBCLASSCD, ";
        $query .= "     S1.SUBCLASSNAME ";
        
        return $query;
    }

    //代替元科目一覧取得
    function selectQuery1_2($model, $sub_subclass)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SUBSTITUTION_CLASSCD || '-' || T1.SUBSTITUTION_SCHOOL_KIND || '-' || T1.SUBSTITUTION_CURRICULUM_CD || '-' || T1.SUBSTITUTION_SUBCLASSCD AS VALUE_SUB, ";
            $query .= "     S1.SUBCLASSNAME AS SUB_NAME, ";
            $query .= "     T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS VALUE_ATT, ";
            $query .= "     S2.SUBCLASSNAME AS ATTEND_NAME, ";
            $query .= "     T1.SUBSTITUTION_CLASSCD, ";
            $query .= "     T1.SUBSTITUTION_SCHOOL_KIND, ";
            $query .= "     T1.SUBSTITUTION_CURRICULUM_CD, ";
            $query .= "     T1.SUBSTITUTION_SUBCLASSCD, ";
            $query .= "     T1.ATTEND_CLASSCD, ";
            $query .= "     T1.ATTEND_SCHOOL_KIND, ";
            $query .= "     T1.ATTEND_CURRICULUM_CD, ";
            $query .= "     T1.ATTEND_SUBCLASSCD, ";
        } else {
            $query .= "     T1.SUBSTITUTION_SUBCLASSCD AS VALUE_SUB, ";
            $query .= "     S1.SUBCLASSNAME AS SUB_NAME, ";
            $query .= "     T1.ATTEND_SUBCLASSCD AS VALUE_ATT, ";
            $query .= "     S2.SUBCLASSNAME AS ATTEND_NAME, ";
        }
        $query .= "     T2.ATTEND_SUBCLASSCD AS MAJOR_ATTEND_SUBCLASSCD, ";
        $query .= "     COUNT(T1.ATTEND_SUBCLASSCD) AS CNT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_SUBSTITUTION_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT T2 ON ";
        $query .= "                  T1.YEAR = T2.YEAR ";
        $query .= "             AND  T1.SUBSTITUTION_SUBCLASSCD = T2.SUBSTITUTION_SUBCLASSCD ";
        $query .= "             AND  T1.ATTEND_SUBCLASSCD = T2.ATTEND_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND  T1.SUBSTITUTION_CLASSCD        = T2.SUBSTITUTION_CLASSCD ";
            $query .= "         AND  T1.SUBSTITUTION_SCHOOL_KIND    = T2.SUBSTITUTION_SCHOOL_KIND ";
            $query .= "         AND  T1.SUBSTITUTION_CURRICULUM_CD  = T2.SUBSTITUTION_CURRICULUM_CD ";
            $query .= "         AND  T1.ATTEND_CLASSCD              = T2.ATTEND_CLASSCD ";
            $query .= "         AND  T1.ATTEND_SCHOOL_KIND          = T2.ATTEND_SCHOOL_KIND ";
            $query .= "         AND  T1.ATTEND_CURRICULUM_CD        = T2.ATTEND_CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST S1 ON T1.SUBSTITUTION_SUBCLASSCD = S1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND  T1.SUBSTITUTION_CLASSCD        = S1.CLASSCD ";
            $query .= "         AND  T1.SUBSTITUTION_SCHOOL_KIND    = S1.SCHOOL_KIND ";
            $query .= "         AND  T1.SUBSTITUTION_CURRICULUM_CD  = S1.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST S2 ON T1.ATTEND_SUBCLASSCD = S2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND  T1.ATTEND_CLASSCD         = S2.CLASSCD ";
            $query .= "         AND  T1.ATTEND_SCHOOL_KIND     = S2.SCHOOL_KIND ";
            $query .= "         AND  T1.ATTEND_CURRICULUM_CD   = S2.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.SUBSTITUTION_CLASSCD || '-' || T1.SUBSTITUTION_SCHOOL_KIND || '-' || T1.SUBSTITUTION_CURRICULUM_CD || '-' || T1.SUBSTITUTION_SUBCLASSCD = '".$sub_subclass."' ";
        } else {
            $query .= "     AND T1.SUBSTITUTION_SUBCLASSCD = '".$sub_subclass."' ";
        }
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SUBSTITUTION_CLASSCD, ";
            $query .= "     T1.SUBSTITUTION_SCHOOL_KIND, ";
            $query .= "     T1.SUBSTITUTION_CURRICULUM_CD, ";
            $query .= "     T1.ATTEND_CLASSCD, ";
            $query .= "     T1.ATTEND_SCHOOL_KIND, ";
            $query .= "     T1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBSTITUTION_SUBCLASSCD, ";
        $query .= "     S1.SUBCLASSNAME, ";
        $query .= "     T1.ATTEND_SUBCLASSCD, ";
        $query .= "     S2.SUBCLASSNAME, ";
        $query .= "     T2.ATTEND_SUBCLASSCD ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SUBSTITUTION_CLASSCD, ";
            $query .= "     T1.SUBSTITUTION_SCHOOL_KIND, ";
            $query .= "     T1.SUBSTITUTION_CURRICULUM_CD, ";
            $query .= "     T1.ATTEND_CLASSCD, ";
            $query .= "     T1.ATTEND_SCHOOL_KIND, ";
            $query .= "     T1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBSTITUTION_SUBCLASSCD, ";
        $query .= "     T1.ATTEND_SUBCLASSCD ";

        return $query;
    }

    //学科一覧取得
    function selectQuery1_3($model, $sub_subclass, $att_subclass, $sub_class, $att_class, $sub_schoolkind, $att_schoolkind, $sub_curriculumcd, $att_curriculumcd)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  T1.SUBSTITUTION_CLASSCD, ";
            $query .= "  T1.SUBSTITUTION_SCHOOL_KIND, ";
            $query .= "  T1.SUBSTITUTION_CURRICULUM_CD, ";
            $query .= "  T1.ATTEND_CLASSCD, ";
            $query .= "  T1.ATTEND_SCHOOL_KIND, ";
            $query .= "  T1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBSTITUTION_SUBCLASSCD, ";
        $query .= "     S1.SUBCLASSNAME AS SUB_NAME, ";
        $query .= "     T1.ATTEND_SUBCLASSCD, ";
        $query .= "     S2.SUBCLASSNAME AS ATTEND_NAME, ";
        $query .= "     T2.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     V1.COURSENAME, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     V1.MAJORNAME, ";
        $query .= "     T2.COURSECODE, ";
        $query .= "     S3.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_SUBSTITUTION_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT T2 ON ";
        $query .= "             T1.YEAR = T2.YEAR ";
        $query .= "             AND T1.SUBSTITUTION_SUBCLASSCD = T2.SUBSTITUTION_SUBCLASSCD ";
        $query .= "             AND T1.ATTEND_SUBCLASSCD = T2.ATTEND_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND  T1.SUBSTITUTION_CLASSCD        = T2.SUBSTITUTION_CLASSCD ";
            $query .= "         AND  T1.SUBSTITUTION_SCHOOL_KIND    = T2.SUBSTITUTION_SCHOOL_KIND ";
            $query .= "         AND  T1.SUBSTITUTION_CURRICULUM_CD  = T2.SUBSTITUTION_CURRICULUM_CD ";
            $query .= "         AND  T1.ATTEND_CLASSCD              = T2.ATTEND_CLASSCD ";
            $query .= "         AND  T1.ATTEND_SCHOOL_KIND          = T2.ATTEND_SCHOOL_KIND ";
            $query .= "         AND  T1.ATTEND_CURRICULUM_CD        = T2.ATTEND_CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST S1 ON T1.SUBSTITUTION_SUBCLASSCD = S1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND  T1.SUBSTITUTION_CLASSCD        = S1.CLASSCD ";
            $query .= "         AND  T1.SUBSTITUTION_SCHOOL_KIND    = S1.SCHOOL_KIND ";
            $query .= "         AND  T1.SUBSTITUTION_CURRICULUM_CD  = S1.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST S2 ON T1.ATTEND_SUBCLASSCD = S2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND  T1.ATTEND_CLASSCD         = S2.CLASSCD ";
            $query .= "         AND  T1.ATTEND_SCHOOL_KIND     = S2.SCHOOL_KIND ";
            $query .= "         AND  T1.ATTEND_CURRICULUM_CD   = S2.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN COURSECODE_MST S3 ON T2.COURSECODE = S3.COURSECODE ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST V1 ON ";
        $query .= "                 T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND V1.COURSECD || V1.MAJORCD = T2.COURSECD || T2.MAJORCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT G1 ON ";
        $query .= "                 T1.YEAR     = G1.YEAR ";
        $query .= "             AND T2.GRADE    = G1.GRADE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SUBSTITUTION_SUBCLASSCD = '".$sub_subclass."' ";
        $query .= "     AND T1.ATTEND_SUBCLASSCD = '".$att_subclass."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND T1.SUBSTITUTION_CLASSCD        = '".$sub_class."' ";
            $query .= "  AND T1.SUBSTITUTION_SCHOOL_KIND    = '".$sub_schoolkind."' ";
            $query .= "  AND T1.SUBSTITUTION_CURRICULUM_CD  = '".$sub_curriculumcd."' ";
            $query .= "  AND T1.ATTEND_CLASSCD              = '".$att_class."' ";
            $query .= "  AND T1.ATTEND_SCHOOL_KIND          = '".$att_schoolkind."' ";
            $query .= "  AND T1.ATTEND_CURRICULUM_CD        = '".$att_curriculumcd."' ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  T1.SUBSTITUTION_CLASSCD, ";
            $query .= "  T1.SUBSTITUTION_SCHOOL_KIND, ";
            $query .= "  T1.SUBSTITUTION_CURRICULUM_CD, ";
            $query .= "  T1.ATTEND_CLASSCD, ";
            $query .= "  T1.ATTEND_SCHOOL_KIND, ";
            $query .= "  T1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBSTITUTION_SUBCLASSCD, ";
        $query .= "     T1.ATTEND_SUBCLASSCD, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     T2.COURSECODE ";

        return $query;
    }

    //選択された科目取得
    function getChosenData($model, $classcd, $schoolkind, $curriculumcd, $subclasscd)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     SUBCLASSNAME ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
            $query .= "     SUBCLASSNAME ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     SUBCLASSCD      = '".$subclasscd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD         = '".$classcd."' ";
            $query .= " AND SCHOOL_KIND     = '".$schoolkind."' ";
            $query .= " AND CURRICULUM_CD   = '".$curriculumcd."' ";
        }
        return $query;
    }

    //学科一覧取得
    function selectQuery2($model, $sub_subclass, $att_subclass, $not="")
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     MAX(L1.COURSENAME) AS COURSENAME, ";
        $query .= "     MAX(L1.MAJORNAME) AS MAJORNAME, ";
        $query .= "     MAX(L2.COURSECODENAME) AS COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     V_COURSE_MAJOR_MST L1 ON  L1.YEAR     = T1.YEAR ";
        $query .= "                           AND L1.COURSECD = T1.COURSECD ";
        $query .= "                           AND L1.MAJORCD  = T1.MAJORCD ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST L2 ON L2.COURSECODE = T1.COURSECODE ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT G1 ON G1.YEAR  = T1.YEAR ";
        $query .= "                        AND G1.GRADE = T1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                    AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SUBCLASSCD = '{$att_subclass}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD         = '".$model->att_class."' ";
            $query .= " AND T1.SCHOOL_KIND     = '".$model->att_schoolkind."' ";
            $query .= " AND T1.CURRICULUM_CD   = '".$model->att_curriculumcd."' ";
        }
        $query .= "     AND T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE {$not} IN ( ";
        $query .= "             SELECT ";
        $query .= "                 GRADE || COURSECD || MAJORCD || COURSECODE ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT ";
        $query .= "             WHERE ";
        $query .= "                 YEAR                    = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND SUBSTITUTION_CLASSCD        = '".$model->sub_class."' ";
            $query .= "         AND SUBSTITUTION_SCHOOL_KIND    = '".$model->sub_schoolkind."' ";
            $query .= "         AND SUBSTITUTION_CURRICULUM_CD  = '".$model->sub_curriculumcd."' ";
            $query .= "         AND ATTEND_CLASSCD              = '".$model->att_class."' ";
            $query .= "         AND ATTEND_SCHOOL_KIND          = '".$model->att_schoolkind."' ";
            $query .= "         AND ATTEND_CURRICULUM_CD        = '".$model->att_curriculumcd."' ";
        }
        $query .= "             AND SUBSTITUTION_SUBCLASSCD = '{$sub_subclass}' ";
        $query .= "             AND ATTEND_SUBCLASSCD       = '{$att_subclass}' ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     G1.GRADE_NAME1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SUBSTITUTION_SUBCLASSCD     = '".$model->sub_subclass."' ";
        $query .= "     AND ATTEND_SUBCLASSCD           = '".$model->att_subclass."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SUBSTITUTION_CLASSCD        = '".$model->sub_class."' ";
            $query .= " AND SUBSTITUTION_SCHOOL_KIND    = '".$model->sub_schoolkind."' ";
            $query .= " AND SUBSTITUTION_CURRICULUM_CD  = '".$model->sub_curriculumcd."' ";
            $query .= " AND ATTEND_CLASSCD              = '".$model->att_class."' ";
            $query .= " AND ATTEND_SCHOOL_KIND          = '".$model->att_schoolkind."' ";
            $query .= " AND ATTEND_CURRICULUM_CD        = '".$model->att_curriculumcd."' ";
        }

        $db->query($query);

        $data = array();
        $selectdata = explode(",", $model->selectdata);
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            list($grade, $coursecd, $majorcd, $coursecode) = preg_split("/_/", $selectdata[$i]);
            $data["YEAR"][TEXT]                     = CTRL_YEAR;
            $data["SUBSTITUTION_SUBCLASSCD"][TEXT]  = $model->sub_subclass;
            $data["ATTEND_SUBCLASSCD"][TEXT]        = $model->att_subclass;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
                $data["SUBSTITUTION_CLASSCD"][TEXT]         = $model->sub_class;
                $data["SUBSTITUTION_SCHOOL_KIND"][TEXT]     = $model->sub_schoolkind;
                $data["SUBSTITUTION_CURRICULUM_CD"][TEXT]   = $model->sub_curriculumcd;
                $data["ATTEND_CLASSCD"][TEXT]               = $model->att_class;
                $data["ATTEND_SCHOOL_KIND"][TEXT]           = $model->att_schoolkind;
                $data["ATTEND_CURRICULUM_CD"][TEXT]         = $model->att_curriculumcd;
        }
            $data["GRADE"][TEXT]                    = $grade;
            $data["COURSECD"][TEXT]                 = $coursecd;
            $data["MAJORCD"][TEXT]                  = $majorcd;
            $data["COURSECODE"][TEXT]               = $coursecode;
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][FUNC]                  = "SYSDATE()";

            $query = Query::insertSQL($data, "SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT");
            $db->query($query);
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
}
?>
