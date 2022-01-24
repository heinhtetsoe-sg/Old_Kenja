<?php

require_once('for_php7.php');

class knjb0059Query extends Query {
    //年度一覧取得
    function getYear() {
        $query  = " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS LABEL, ";
        $query .= "     '".CTRL_YEAR."' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '".(CTRL_YEAR + 1)."' AS LABEL, ";
        $query .= "     '".(CTRL_YEAR + 1)."' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学年　課程学科コースを取得
    function getGradeMajorCourse($model) {

        $query  = " SELECT DISTINCT ";
        $query .= "     T5.GRADE_NAME1 || '(' || T4.COURSECD || T4.MAJORCD || ') ' || T2.COURSENAME || T3.MAJORNAME || ' (' || T4.COURSECODE || ') ' || T1.COURSECODENAME AS LABEL, ";
        $query .= "     T4.GRADE || '-' || T4.COURSECD || '-' || T4.MAJORCD || '-' || T4.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSECODE_MST T1, ";
        $query .= "     COURSE_MST T2, ";
        $query .= "     MAJOR_MST T3, ";
        $query .= "     SCHREG_REGD_DAT T4, ";
        $query .= "     SCHREG_REGD_GDAT T5 ";
        $query .= " WHERE ";
        $query .= "     T4.YEAR       ='{$model->year}' AND ";
        $query .= "     T1.COURSECODE = T4.COURSECODE AND ";
        $query .= "     T2.COURSECD   = T4.COURSECD AND ";
        $query .= "     T3.COURSECD   = T4.COURSECD AND ";
        $query .= "     T4.MAJORCD    = T3.MAJORCD AND ";
        $query .= "     T4.YEAR       = T5.YEAR AND ";
        $query .= "     T4.GRADE      = T5.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T5.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T5.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
        
    //履修パターンカウント
    function getCompCreditsPatternCnt($year) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_MST T1, ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.PATTERN_CD   = T2.PATTERN_CD ";
        $query .= "     AND T2.SCHOOL_KIND  = 'H' ";

        return $query;
    }

    //履修パターンリスト
    function getCompCreditsPatternList($model) {

        $query  = " SELECT ";
        $query .= "     T1.PATTERN_CD, ";
        $query .= "     T1.PATTERN_NAME, ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SCHOOL_KIND, ";
        $query .= "     T2.CURRICULUM_CD, ";
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_MST T1, ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_DAT T2 ";
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD      = T2.SUBCLASSCD ";
        $query .= "                              AND T3.CLASSCD         = T2.CLASSCD ";
        $query .= "                              AND T3.SCHOOL_KIND     = T2.SCHOOL_KIND ";
        $query .= "                              AND T3.CURRICULUM_CD   = T2.CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.PATTERN_CD   = T2.PATTERN_CD ";
        $query .= "     AND T1.GRADE        = T2.GRADE ";
        $query .= "     AND T1.COURSECD     = T2.COURSECD ";
        $query .= "     AND T1.MAJORCD      = T2.MAJORCD ";
        $query .= "     AND T1.COURSECODE   = T2.COURSECODE ";
        $query .= "     AND T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE  = '{$model->gradeCourse}' ";
        $query .= "     AND T2.SCHOOL_KIND  = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     T1.PATTERN_CD, ";
        $query .= "     T2.SUBCLASSCD ";

        return $query;
    }

    //名称マスタより取得
    function getNamecd($model, $cd, $flg="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$cd."' ";
        if ($flg) {
            $query .= "     AND '".$model->year."' BETWEEN NAMESPARE1 AND NAMESPARE2 ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //履修パターン情報
    function GetGroup($model, $groupcd) {

        $query  = " SELECT ";
        $query .= "     T1.PATTERN_CD, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     NM.NAME1 AS CURRI_NAME, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     SUB_C.SUBCLASSNAME, ";
        $query .= "     C1.CREDITS, ";
        $query .= "     C1.REQUIRE_FLG ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUB_C ON T1.CLASSCD = SUB_C.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = SUB_C.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = SUB_C.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = SUB_C.SUBCLASSCD ";
        $query .= "     LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'Z018' ";
        $query .= "          AND T1.CURRICULUM_CD = NM.NAMECD2 ";
        $query .= "     LEFT JOIN CREDIT_MST C1 ON C1.YEAR = T1.YEAR";
        $query .= "                            AND C1.COURSECD = T1.COURSECD ";
        $query .= "                            AND C1.MAJORCD = T1.MAJORCD ";
        $query .= "                            AND C1.GRADE = T1.GRADE ";
        $query .= "                            AND C1.COURSECODE = T1.COURSECODE ";
        $query .= "                            AND C1.CLASSCD = T1.CLASSCD ";
        $query .= "                            AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                            AND C1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                            AND C1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.PATTERN_CD = '".$model->field["PATTERN_CD"]."' ";
        $query .= "     AND T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE  = '{$model->gradeCourse}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '999' AS PATTERN_CD, ";
        $query .= "     C1.CLASSCD, ";
        $query .= "     C1.SCHOOL_KIND, ";
        $query .= "     C1.CURRICULUM_CD, ";
        $query .= "     NM.NAME1 AS CURRI_NAME, ";
        $query .= "     SUB.SUBCLASSCD, ";
        $query .= "     SUB.SUBCLASSNAME, ";
        $query .= "     C1.CREDITS, ";
        $query .= "     C1.REQUIRE_FLG ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST C1 ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST SUB ON SUB.YEAR = C1.YEAR ";
        $query .= "                                 AND SUB.CLASSCD = C1.CLASSCD ";
        $query .= "                                 AND SUB.SCHOOL_KIND = C1.SCHOOL_KIND ";
        $query .= "                                 AND SUB.CURRICULUM_CD = C1.CURRICULUM_CD ";
        $query .= "                                 AND SUB.SUBCLASSCD = C1.SUBCLASSCD ";
        $query .= "     LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'Z018' ";
        $query .= "          AND C1.CURRICULUM_CD = NM.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     C1.YEAR = '".$model->year."' ";
        $query .= "     AND C1.SCHOOL_KIND = 'H' ";
        $query .= "     AND C1.GRADE || '-' || C1.COURSECD || '-' || C1.MAJORCD || '-' || C1.COURSECODE  = '{$model->gradeCourse}' ";
        $query .= "     AND NOT EXISTS( ";
        $query .= "                 SELECT ";
        $query .= "                     'x' ";
        $query .= "                 FROM ";
        $query .= "                     COMP_CREDITS_PATTERN_COURSE_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR = '".$model->year."' ";
        $query .= "                     AND T1.PATTERN_CD = '".$model->field["PATTERN_CD"]."' ";
        $query .= "                     AND T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE  = '{$model->gradeCourse}' ";
        $query .= "                     AND T1.CLASSCD = C1.CLASSCD ";
        $query .= "                     AND T1.SCHOOL_KIND = C1.SCHOOL_KIND ";
        $query .= "                     AND T1.CURRICULUM_CD = C1.CURRICULUM_CD ";
        $query .= "                     AND T1.SUBCLASSCD = C1.SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUBCLASSNAME ";
        return $query;
    }

    //SUBCLASS_MSTより取得
    function getSubclassRow($classcd, $school_kind, $curriculum_cd, $subclasscd) {
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD         = '".$classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD      = '".$subclasscd."' ";
        
        return $query;
    }

    //科目マスタより取得
    function getSubClass($curriculum_cd) {

        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND     = 'H' AND ";
        $query .= "     CURRICULUM_CD   = '".$curriculum_cd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //COMP_CREDITS_PATTERN_COURSE_MSTより取得
    function getPatternRow($model, $cd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND PATTERN_CD = '".$cd."' ";
        $query .= "     AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE  = '{$model->gradeCourse}' ";

        return $query;
    }

    //追加
    function &InsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        //学年、課程学科コースを分割
        list($grade, $coursecd, $majorcd, $coursecode) = explode("-", $model->gradeCourse);
        //COMP_CREDITS_PATTERN_COURSE_MSTテーブル更新
        $data = array();
        $data["YEAR"][TEXT]         = $model->year;
        $data["PATTERN_CD"][TEXT]   = $fields["PATTERN_CD"];
        $data["GRADE"][TEXT]        = $grade;
        $data["COURSECD"][TEXT]     = $coursecd;
        $data["MAJORCD"][TEXT]      = $majorcd;
        $data["COURSECODE"][TEXT]   = $coursecode;
        $data["PATTERN_NAME"][TEXT] = $fields["PATTERN_NAME"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "COMP_CREDITS_PATTERN_COURSE_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //追加
    function InsertDatQuery($model, $fields, $setSubData) {
        $db = Query::dbCheckOut();
        //学年、課程学科コースを分割
        list($grade, $coursecd, $majorcd, $coursecode) = explode("-", $model->gradeCourse);
        foreach ($setSubData as $key => $valArray) {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $key);

            $data = array();
            $data["YEAR"][TEXT]             = $model->year;
            $data["PATTERN_CD"][TEXT]       = $fields["PATTERN_CD"];
            $data["GRADE"][TEXT]            = $grade;
            $data["COURSECD"][TEXT]         = $coursecd;
            $data["MAJORCD"][TEXT]          = $majorcd;
            $data["COURSECODE"][TEXT]       = $coursecode;
            $data["CLASSCD"][TEXT]          = $classcd;
            $data["SCHOOL_KIND"][TEXT]      = $school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            $data["COMP_FLG"][TEXT]         = 1;

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "COMP_CREDITS_PATTERN_COURSE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //更新
    function &UpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        //学年、課程学科コースを分割
        list($grade, $coursecd, $majorcd, $coursecode) = explode("-", $model->gradeCourse);
        //COMP_CREDITS_PATTERN_COURSE_MSTテーブル更新
        $data = array();
        $data["YEAR"][TEXT]         = $model->year;
        $data["PATTERN_CD"][TEXT]   = $fields["PATTERN_CD"];
        $data["GRADE"][TEXT]        = $grade;
        $data["COURSECD"][TEXT]     = $coursecd;
        $data["MAJORCD"][TEXT]      = $majorcd;
        $data["COURSECODE"][TEXT]   = $coursecode;
        $data["PATTERN_NAME"][TEXT] = $fields["PATTERN_NAME"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE PATTERN_CD = '".$fields["PATTERN_CD"]."' ";
        $where .= "   AND YEAR = '".$model->year."' ";
        $where .= "   AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE  = '{$model->gradeCourse}' ";

        $query = Query::updateSQL($data, "COMP_CREDITS_PATTERN_COURSE_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //COMP_CREDITS_PATTERN_COURSE_MST削除
    function &DeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND PATTERN_CD = '".$fields["PATTERN_CD"]."' ";
        $query .= "     AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE  = '{$model->gradeCourse}' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    //COMP_CREDITS_PATTERN_COURSE_DAT削除
    function &DeleteDatQuery($model, $cd) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND PATTERN_CD = '".$cd."' ";
        $query .= "     AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE  = '{$model->gradeCourse}' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    //前年度からコピー
    function &CopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_MST ";
        $query .= " SELECT ";
        $query .= "     '".$model->year."', ";
        $query .= "     PATTERN_CD, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     PATTERN_NAME, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->year - 1)."' ";

        $db->query($query);

        $query  = " INSERT INTO ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_DAT ";
        $query .= " SELECT ";
        $query .= "     '".$model->year."', ";
        $query .= "     PATTERN_CD, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     COMP_FLG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->year - 1)."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
