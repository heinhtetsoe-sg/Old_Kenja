<?php

require_once('for_php7.php');

class knjh543Query extends Query {

    //学期取得
    function getSemester() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST T2 ON T1.YEAR     = T2.YEAR ";
        $query .= "                              AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実力区分取得
    function getProficiencyDiv($semester) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCYDIV AS VALUE, ";
        $query .= "     T1.PROFICIENCYDIV || ':' || T2.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T1.YEAR    = T2.YEAR ";
        $query .= "                            AND T2.NAMECD1 = 'H508' ";
        $query .= "                            AND T1.PROFICIENCYDIV = T2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' ";

        return $query;
    }

    //実力コード取得
    function getProficiencyCd($semester, $p_div) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T2.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_MST T2 ON T1.PROFICIENCYDIV   = T2.PROFICIENCYDIV ";
        $query .= "                                 AND T1.PROFICIENCYCD    = T2.PROFICIENCYCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        $query .= "     T1.PROFICIENCYDIV = '".$p_div."'  ";

        return $query;
    }

    //実力科目合併リスト取得
    function getProficiencySubclassRepCombDatList($model) {

        $div = ($model->field["DIV"] == "1") ? '03' : '04';

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD || ':' || T1.SUBCLASS_NAME AS PROFICIENCY_SUBCLASS_NAME, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T3.GRADE_NAME1, ";
        $query .= "     T2.COURSECD || '-' || T2.MAJORCD || '-' || T2.COURSECODE AS COURSE, ";
        if($model->field["DIV"] == "1") {
            $query .= "     T2.COURSECD || T2.MAJORCD || T2.COURSECODE || ':' || T4.MAJORNAME || T5.COURSECODENAME AS COURSE_NAME, ";
        } else {
            $query .= "     T2.MAJORCD || ':' || T6.GROUP_NAME AS COURSE_NAME, ";
        }
        $query .= "     T2.ATTEND_SUBCLASSCD, ";
        $query .= "     T2.ATTEND_SUBCLASSCD || ':' || T8.SUBCLASS_NAME AS SUBCLASSNAME, ";
        if ($model->Properties["weightingHyouki"] == "1") {
            $query .= "     DECIMAL(T2.WEIGHTING,4,1) AS WEIGHTING ";
        } else {
            $query .= "     T2.WEIGHTING ";
        }
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T2 ON ";
        $query .= "                 T2.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                 T2.SEMESTER         = '".$model->field["SEMESTER"]."' AND ";
        $query .= "                 T2.PROFICIENCYDIV   = '".$model->field["PROFICIENCYDIV"]."' AND ";
        $query .= "                 T2.PROFICIENCYCD    = '".$model->field["PROFICIENCYCD"]."' AND ";
        $query .= "                 T2.DIV              = '".$div."' AND ";
        $query .= "                 T1.PROFICIENCY_SUBCLASS_CD = T2.COMBINED_SUBCLASSCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ON ";
        $query .= "                 T2.YEAR         = T3.YEAR AND ";
        $query .= "                 T2.GRADE        = T3.GRADE ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST T4 ON ";
        $query .= "                 T2.YEAR         = T4.YEAR AND ";
        $query .= "                 T2.COURSECD     = T4.COURSECD AND ";
        $query .= "                 T2.MAJORCD      = T4.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T5 ON ";
        $query .= "                 T2.YEAR         = T5.YEAR AND ";
        $query .= "                 T2.COURSECODE   = T5.COURSECODE ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_HDAT T6 ON ";
        $query .= "                 T2.YEAR         = T6.YEAR AND ";
        $query .= "                 T2.GRADE        = T6.GRADE AND ";
        $query .= "                 T2.MAJORCD      = T6.GROUP_CD ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_MST T7 ON ";
        $query .= "                 T2.COMBINED_SUBCLASSCD = T7.PROFICIENCY_SUBCLASS_CD ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_MST T8 ON ";
        $query .= "                 T2.ATTEND_SUBCLASSCD = T8.PROFICIENCY_SUBCLASS_CD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     T2.GRADE, ";
        $query .= "     COURSE, ";
        $query .= "     T2.ATTEND_SUBCLASSCD ";

        return $query;
    }

    //合併先科目数取得
    function getP_SubclassCnt($model, $row) {
        $query  = " WITH MAIN AS ( ";
        $query .= knjh543Query::getProficiencySubclassRepCombDatList($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCY_SUBCLASS_CD   = '".$row["PROFICIENCY_SUBCLASS_CD"]."' ";

        return $query;
    }

    //合併元科目数取得
    function getA_SubclassCnt($model, $row) {
        $query  = " WITH MAIN AS ( ";
        $query .= knjh543Query::getProficiencySubclassRepCombDatList($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCY_SUBCLASS_CD   = '".$row["PROFICIENCY_SUBCLASS_CD"]."' AND ";
        $query .= "     GRADE       = '".$row["GRADE"]."' AND ";
        $query .= "     COURSE      = '".$row["COURSE"]."' ";

        return $query;
    }

    //実力科目合併設定データ取得
    function getProficiencySubclassRepCombDat($model) {

        $div = ($model->div == "1") ? "03" : "04";

        $query  = " SELECT ";
        $query .= "     COMBINED_SUBCLASSCD AS PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || COURSECODE AS COURSE ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT ";
        $query .= " WHERE ";
        $query .= "     REPLACECD       = '1' AND ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER        = '".$model->semester."' AND ";
        $query .= "     PROFICIENCYDIV  = '".$model->proficiencydiv."' AND ";
        $query .= "     PROFICIENCYCD   = '".$model->proficiencycd."' AND ";
        $query .= "     DIV             = '".$div."' AND ";
        $query .= "     GRADE           = '".$model->field["GRADE"]."' AND ";
        $query .= "     COMBINED_SUBCLASSCD = '".$model->proficiency_subclass_cd."' AND ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || COURSECODE = '".$model->field["COURSE"]."' ";

        return $query;
    }

    //合併先科目名称取得
    function getProficiencySubclassName($subclasscd) {
        $query  = " SELECT DISTINCT ";
        $query .= "     PROFICIENCY_SUBCLASS_CD || ':' || SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCY_SUBCLASS_CD = '".$subclasscd."' ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科コース取得
    function getCourseMajor($model, $grade) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || ':' || T2.MAJORNAME || T3.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST T2 ON T1.YEAR      = T2.YEAR ";
        $query .= "                                    AND T1.COURSECD  = T2.COURSECD ";
        $query .= "                                    AND T1.MAJORCD   = T2.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T3 ON T1.YEAR        = T3.YEAR ";
        $query .= "                                  AND T1.COURSECODE  = T3.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.GRADE    = '".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースグループ取得
    function getCourseGroup($grade) {
        $query  = " SELECT DISTINCT ";
        $query .= "     '0-' || GROUP_CD || '-0000' AS VALUE, ";
        $query .= "     GROUP_CD || ':' || GROUP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ一覧取得
    function selectQuery($model, $grade, $course, $p_subclass) {

        list($coursecd, $majorcd, $coursecode) = preg_split("/-/", $course);
        $div = ($model->div == "1") ? "03" : "04";

        $query  = " WITH COURSE_GROUP AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         YEAR, ";
        $query .= "         GRADE, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         GROUP_CD ";
        $query .= "     FROM ";
        $query .= "         COURSE_GROUP_CD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         GRADE       = '".$grade."' AND ";
        $query .= "         GROUP_CD    = '".$majorcd."' ";
        $query .= "     UNION ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         YEAR, ";
        $query .= "         GRADE, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         GROUP_CD ";
        $query .= "     FROM ";
        $query .= "         COURSE_GROUP_CD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         GRADE       = '".$grade."' AND ";
        $query .= "         COURSECD || MAJORCD || COURSECODE = '".$coursecd.$majorcd.$coursecode."' ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.PROFICIENCYDIV, ";
        $query .= "         T1.PROFICIENCYCD, ";
        $query .= "         T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= "     FROM ";
        $query .= "         PROFICIENCY_PERFECT_COURSE_DAT T1 ";
        $query .= "         LEFT JOIN COURSE_GROUP L1 ON L1.YEAR = T1.YEAR ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER         = '{$model->semester}' AND ";
        $query .= "         T1.PROFICIENCYDIV   = '{$model->proficiencydiv}' AND ";
        $query .= "         T1.PROFICIENCYCD    = '{$model->proficiencycd}' AND ";
        $query .= "         T1.GRADE = CASE WHEN DIV = '01' ";
        $query .= "                              THEN '00' ";
        $query .= "                              ELSE '".$grade."' ";
        $query .= "                         END AND ";
        $query .= "         (T1.COURSECD || T1.MAJORCD || T1.COURSECODE = CASE WHEN T1.DIV IN ('01','02') ";
        $query .= "                                                           THEN '00000000' ";
        $query .= "                                                           ELSE CASE WHEN T1.DIV = '04' ";
        $query .= "                                                                     THEN '0' || L1.GROUP_CD || '0000' ";
        $query .= "                                                                     ELSE '".$coursecd.$majorcd.$coursecode."' ";
        $query .= "                                                                     END ";
        $query .= "                                                      END OR ";
        $query .= "         T1.COURSECD || T1.MAJORCD || T1.COURSECODE = CASE WHEN T1.DIV IN ('01','02') ";
        $query .= "                                                           THEN '00000000' ";
        $query .= "                                                           ELSE CASE WHEN T1.DIV = '04' ";
        $query .= "                                                                     THEN '0' || L1.GROUP_CD || '0000' ";
        $query .= "                                                                     ELSE L1.COURSECD || L1.MAJORCD || L1.COURSECODE ";
        $query .= "                                                                     END ";
        $query .= "                                                      END) AND ";
        $query .= "         SUBSTR(T1.PROFICIENCY_SUBCLASS_CD,1,2) = '".substr($p_subclass,0,2)."' AND ";
        $query .= "         T1.PROFICIENCY_SUBCLASS_CD <> '".$p_subclass."' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD AS VALUE, ";
        $query .= "     T2.ATTEND_SUBCLASSCD AS JOINCD, ";
        if ($model->Properties["weightingHyouki"] == "1") {
            $query .= "     DECIMAL(T2.WEIGHTING,4,1) AS WEIGHTING, ";
        } else {
            $query .= "     T2.WEIGHTING, ";
        }
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD || ': ' || T3.SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T2 ON ";
        $query .= "             T2.YEAR             = T1.YEAR AND ";
        $query .= "             T2.SEMESTER         = T1.SEMESTER AND ";
        $query .= "             T2.PROFICIENCYDIV   = T1.PROFICIENCYDIV AND ";
        $query .= "             T2.PROFICIENCYCD    = T1.PROFICIENCYCD AND ";
        $query .= "             T2.DIV              = '".$div."' AND ";
        $query .= "             T2.GRADE            = '".$grade."' AND ";
        $query .= "             T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '".$coursecd.$majorcd.$coursecode."' AND ";
        $query .= "             T2.COMBINED_SUBCLASSCD = '".$p_subclass."' AND ";
        $query .= "             T2.ATTEND_SUBCLASSCD = T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_MST T3 ON ";
        $query .= "             T3.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT追加処理
    function &insertProficiencySubclassRepCombDatQuery(&$db, $selectdata, $model) {
        $data = array();
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $db->query(knjh543Query::insertProficiencySubclassRepCombDat($model, $selectdata[$i]));
        }
        return;
    }

    //PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT追加
    function insertProficiencySubclassRepCombDat($model, $selectdata) {

        $div = ($model->div == "1") ? "03" : "04";
        list($coursecd, $majorcd, $coursecode) = preg_split("/-/", $model->field["COURSE"]);

        $data["REPLACECD"][TEXT]        = "1";
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["PROFICIENCYDIV"][TEXT]   = $model->proficiencydiv;
        $data["PROFICIENCYCD"][TEXT]    = $model->proficiencycd;
        $data["DIV"][TEXT]              = $div;
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["COURSECD"][TEXT]         = $coursecd;
        $data["MAJORCD"][TEXT]          = $majorcd;
        $data["COURSECODE"][TEXT]       = $coursecode;
        $data["COMBINED_SUBCLASSCD"][TEXT] = $model->proficiency_subclass_cd;
        $data["ATTEND_SUBCLASSCD"][TEXT]   = $selectdata;
        $data["WEIGHTING"][NUMBER]      = $model->field["WEIGHTING".$selectdata] ? $model->field["WEIGHTING".$selectdata] : NULL;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT");

        return $query;
    }

    //PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT削除
    function deleteProficiencySubclassRepCombDat($model) {

        $div = ($model->div == "1") ? "03" : "04";

        $query  = " DELETE FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT ";
        $query .= " WHERE ";
        $query .="      REPLACECD           = '1' AND ";
        $query .="      YEAR                = '".CTRL_YEAR."' AND ";
        $query .="      SEMESTER            = '".$model->semester."' AND ";
        $query .="      PROFICIENCYDIV      = '".$model->proficiencydiv."' AND ";
        $query .="      PROFICIENCYCD       = '".$model->proficiencycd."' AND ";
        $query .="      DIV                 = '".$div."' AND ";
        $query .="      GRADE               = '".$model->field["GRADE"]."' AND ";
        $query .="      COMBINED_SUBCLASSCD = '".$model->proficiency_subclass_cd."' AND ";
        $query .="      COURSECD || '-' || MAJORCD || '-' || COURSECODE = '".$model->field["COURSE"]."' ";

        return $query;
    }

    //--- UPDATE 
    function &getUpdWeight($model)
    {

        $div = ($model->div == "1") ? "03" : "04";
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        foreach ((array)$model->field["SUBCLASS_CD"] as $key => $val) {
            $data["WEIGHTING"][NUMBER] = $model->field["WEIGHTING".$val] ? $model->field["WEIGHTING".$val] : NULL;
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "sysdate()";

            $where  = " WHERE ";
            $where .="      REPLACECD           = '1' AND ";
            $where .="      YEAR                = '".CTRL_YEAR."' AND ";
            $where .="      SEMESTER            = '".$model->semester."' AND ";
            $where .="      PROFICIENCYDIV      = '".$model->proficiencydiv."' AND ";
            $where .="      PROFICIENCYCD       = '".$model->proficiencycd."' AND ";
            $where .="      DIV                 = '".$div."' AND ";
            $where .="      GRADE               = '".$model->field["GRADE"]."' AND ";
            $where .="      COMBINED_SUBCLASSCD = '".$model->proficiency_subclass_cd."' AND ";
            $where .="      ATTEND_SUBCLASSCD   = '".$model->field["SUBCLASS_CD"][$key]."' AND ";
            $where .="      COURSECD || '-' || MAJORCD || '-' || COURSECODE = '".$model->field["COURSE"]."' ";

            $query = Query::updateSQL($data, "PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT", $where);
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
