<?php

require_once('for_php7.php');

class knjz218aQuery extends Query {

    //コースグループリスト取得
    function getCourseGroupList($model) {
        $query  = " SELECT ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T3.GRADE_NAME1, ";
        $query .= "     T1.GROUP_CD, ";
        $query .= "     T1.GROUP_NAME, ";
        $query .= "     T1.GROUP_ABBV, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     T2.COURSECD || ':' || T4.COURSENAME AS COURSENAME, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     T2.MAJORCD || ':' || T4.MAJORNAME AS MAJORNAME, ";
        $query .= "     T2.COURSECODE, ";
        $query .= "     T2.COURSECODE || ':' || T5.COURSECODENAME AS COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT T1 ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT T2 ON ";
        $query .= "         T2.YEAR     = T1.YEAR AND ";
        $query .= "         T2.GRADE    = T1.GRADE AND ";
        $query .= "         T2.GROUP_CD = T1.GROUP_CD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ON ";
        $query .= "         T3.YEAR     = T1.YEAR AND ";
        $query .= "         T3.GRADE    = T1.GRADE ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST T4 ON ";
        $query .= "         T4.YEAR     = T1.YEAR AND ";
        $query .= "         T4.COURSECD = T2.COURSECD AND ";
        $query .= "         T4.MAJORCD  = T2.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T5 ON ";
        $query .= "         T5.YEAR         = T1.YEAR AND ";
        $query .= "         T5.COURSECODE   = T2.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T3.SCHOOL_KIND IN ('J','H') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T3.SCHOOL_KIND DESC, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.GROUP_CD, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     T2.COURSECODE ";

        return $query;
    }

    //学年数取得
    function getGradeCnt($model, $grade) {
        $query  = " WITH MAIN AS ( ";
        $query .= knjz218aQuery::getCourseGroupList($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS G_CNT ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$grade."' ";

        return $query;
    }

    //コースグループ数取得
    function getCourseGroupCnt($model, $grade, $group_cd) {
        $query  = " WITH MAIN AS ( ";
        $query .= knjz218aQuery::getCourseGroupList($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS G_CNT ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$grade."' AND ";
        $query .= "     GROUP_CD = '".$group_cd."' ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND IN ('J','H') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOL_KIND DESC, ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースグループヘッダデータ取得
    function getCourseGroupHdata($grade, $group_cd, $feild="") {
        $query  = " SELECT ";
        if($feild == "") {
            $query .= "     * ";
        } else {
            $query .= "     {$feild} ";
        }
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE = '".$grade."' AND ";
        $query .= "     GROUP_CD = '".$group_cd."' ";

        return $query;
    }

    //データ一覧取得
    function selectQuery($grade, $group_cd) {
        $query  =" SELECT DISTINCT ";
        $query .="     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .="     T2.COURSECD || '-' || T2.MAJORCD || '-' || T2.COURSECODE AS JOINCD, ";
        $query .="     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE || ': ' || T3.MAJORNAME || ' ' || T4.COURSECODENAME AS LABEL ";
        $query .=" FROM ";
        $query .="     SCHREG_REGD_DAT T1 ";
        $query .="     LEFT JOIN COURSE_GROUP_CD_DAT T2 ON ";
        $query .="          T2.YEAR         = T1.YEAR AND ";
        $query .="          T2.GRADE        = T1.GRADE AND  ";
        $query .="          T2.GROUP_CD     = '".$group_cd."' AND ";
        $query .="          T2.COURSECD     = T1.COURSECD AND  ";
        $query .="          T2.MAJORCD      = T1.MAJORCD AND  ";
        $query .="          T2.COURSECODE   = T1.COURSECODE  ";
        $query .="     LEFT JOIN V_COURSE_MAJOR_MST T3 ON ";
        $query .="          T3.YEAR     = T1.YEAR AND ";
        $query .="          T3.COURSECD = T1.COURSECD AND ";
        $query .="          T3.MAJORCD  = T1.MAJORCD ";
        $query .="     LEFT JOIN V_COURSECODE_MST T4 ON ";
        $query .="          T4.YEAR         = T1.YEAR AND ";
        $query .="          T4.COURSECODE   = T1.COURSECODE ";
        $query .=" WHERE ";
        $query .="     T1.YEAR      = '".CTRL_YEAR."' AND ";
        $query .="     T1.SEMESTER  = '".CTRL_SEMESTER."' AND ";
        $query .="     T1.GRADE     = '".$grade."' ";
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //COURSE_GROUP_CD_HDATチェック
    function checkCourseGroupHdata($model, $year, $div = "") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT T1 ";
        $query .= " WHERE ";
        if ($div == "LAST_DATA") {
            $query .= "     YEAR = '".$year."' ";
        } else {
            $query .= "     YEAR = '".CTRL_YEAR."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "             ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "             ) ";
        }

        return $query;
    }

    //COURSE_GROUP_CD_HDATチェック
    function checkCourseGroupData($model, $year, $div) {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    COURSE_GROUP_CD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$year."' ";
        if ($div == "COPY") {
            $query .= "    AND GRADE || COURSECD || MAJORCD || COURSECODE ";
            $query .= "         IN (".knjz218aQuery::getCourse($model).") ";
        } else {
            $query .= "    AND GRADE    = '".$model->field["GRADE"]."' ";
            $query .= "    AND GROUP_CD = '".$model->field["GROUP_CD"]."' ";
        }

        return $query;
    }

    //今年度のコース
    function getCourse($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST T2 ON ";
        $query .= "         T2.YEAR     = T1.YEAR AND ";
        $query .= "         T2.COURSECD = T1.COURSECD AND ";
        $query .= "         T2.MAJORCD  = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T3 ON ";
        $query .= "         T3.YEAR         = T1.YEAR AND ";
        $query .= "         T3.COURSECODE   = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE IN (SELECT ";
        $query .= "                     GRADE ";
        $query .= "                  FROM ";
        $query .= "                     SCHREG_REGD_GDAT ";
        $query .= "                  WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                     SCHOOL_KIND IN ('J','H')) ";

        return $query;
    }

    //コピー処理
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $lastyear = CTRL_YEAR - 1;

        //COURSE_GROUP_CD_HDAT
        $query  = " INSERT INTO COURSE_GROUP_CD_HDAT ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     GRADE, ";
        $query .= "     GROUP_CD, ";
        $query .= "     GROUP_NAME, ";
        $query .= "     GROUP_ABBV, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$lastyear."' AND ";
        $query .= "     GRADE IN (  SELECT ";
        $query .= "                     GRADE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".$lastyear."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                     SCHOOL_KIND IN ('J','H')) AND ";
        $query .= "     GRADE || GROUP_CD IN (  SELECT DISTINCT ";
        $query .= "                                 GRADE || GROUP_CD ";
        $query .= "                             FROM ";
        $query .= "                                 COURSE_GROUP_CD_DAT ";
        $query .= "                             WHERE ";
        $query .= "                                 YEAR = '".$lastyear."' AND ";
        $query .= "                                 GRADE IN (  SELECT ";
        $query .= "                                                 GRADE ";
        $query .= "                                             FROM ";
        $query .= "                                                 SCHREG_REGD_GDAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 YEAR = '".$lastyear."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                                                 SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                                                 SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                                                 SCHOOL_KIND IN ('J','H')) AND ";
        $query .= "                                 GRADE || COURSECD || MAJORCD || COURSECODE IN (".knjz218aQuery::getCourse($model).") ";
        $query .= "                           ) ";
        $query .= " ) ";

        $db->query($query);

        //COURSE_GROUP_CD_DAT
        $query  = " INSERT INTO COURSE_GROUP_CD_DAT ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     GRADE, ";
        $query .= "     GROUP_CD, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$lastyear."' AND ";
        $query .= "     GRADE IN (  SELECT ";
        $query .= "                     GRADE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".$lastyear."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                     SCHOOL_KIND IN ('J','H')) AND ";
        $query .= "     GRADE || COURSECD || MAJORCD || COURSECODE IN (".knjz218aQuery::getCourse($model).") ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();

        Query::dbCheckIn($db);

        return;
    }

    //COURSE_GROUP_CD_HDAT追加
    function insertCourseGroupHdat($model) {
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["GRADE"][TEXT]        = $model->field["GRADE"];
        $data["GROUP_CD"][TEXT]     = $model->field["GROUP_CD"];
        $data["GROUP_NAME"][TEXT]   = $model->field["GROUP_NAME"];
        $data["GROUP_ABBV"][TEXT]   = $model->field["GROUP_ABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "COURSE_GROUP_CD_HDAT");

        return $query;
    }

    //COURSE_GROUP_CD_HDAT削除
    function deleteCourseGroupHdat($model) {
        $query  = " DELETE FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .="      YEAR        = '".CTRL_YEAR."' AND ";
        $query .="      GRADE       = '".$model->field["GRADE"]."' AND ";
        $query .="      GROUP_CD    = '".$model->field["GROUP_CD"]."' ";

        return $query;
    }

    //コースグループデータ追加
    function &insertCourseGroupDatQuery(&$db, $selectdata, $model) {
        $data = array();
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }

            $db->query(knjz218aQuery::insertCourseGroupDat($model, $selectdata[$i]));
        }
        return;
    }

    //COURSE_GROUP_CD_DAT追加
    function insertCourseGroupDat($model, $selectdata) {
        list($coursecd, $majorcd, $coursecode) = preg_split("/-/", $selectdata);
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["GRADE"][TEXT]        = $model->field["GRADE"];
        $data["GROUP_CD"][TEXT]     = $model->field["GROUP_CD"];
        $data["COURSECD"][TEXT]     = $coursecd;
        $data["MAJORCD"][TEXT]      = $majorcd;
        $data["COURSECODE"][TEXT]   = $coursecode;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "COURSE_GROUP_CD_DAT");

        return $query;
    }

    //COURSE_GROUP_CD_DAT削除
    function deleteCourseGroupDat($model) {
        $query  = " DELETE FROM ";
        $query .= "     COURSE_GROUP_CD_DAT ";
        $query .= " WHERE ";
        $query .="      YEAR = '".CTRL_YEAR."' AND ";
        $query .="      GRADE       = '".$model->field["GRADE"]."' AND ";
        $query .="      GROUP_CD    = '".$model->field["GROUP_CD"]."' ";

        return $query;
    }
}
?>
