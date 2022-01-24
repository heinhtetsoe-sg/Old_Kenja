<?php

require_once('for_php7.php');

class knjz203Query extends Query {
    //リストの最初表らわした時(デフォルト)のcouse_pk
    function getFirst_CouseKey($model) {
        $query  = knjz203Query::getCouseName($model);
        $query .= " fetch first 1 rows only";

        return $query;
    }

    //授業時数のフラグ  欠課数上限値の入力可、付加の判定に使う
    function getJugyouJisuFlg() {
        $query  = " SELECT ";
        $query .= "     JUGYOU_JISU_FLG ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //コース名コンボ取得
   function getCouseName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T4.COURSECD, ";
        $query .= "     T4.MAJORCD, ";
        $query .= "     T4.COURSECODE, ";
        $query .= "     T4.GRADE, ";
        $query .= "     T4.YEAR, ";
        $query .= "     T2.COURSENAME, ";
        $query .= "     T3.MAJORNAME, ";
        $query .= "     T1.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     COURSECODE_MST T1, ";
        $query .= "     COURSE_MST T2, ";
        $query .= "     MAJOR_MST T3, ";
        $query .= "     SCHREG_REGD_DAT T4 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T4.YEAR AND GDAT.GRADE = T4.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T4.YEAR AND GDAT.GRADE = T4.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T4.YEAR       ='".CTRL_YEAR."' AND ";
        $query .= "     T1.COURSECODE = T4.COURSECODE AND ";
        $query .= "     T2.COURSECD   = T4.COURSECD AND ";
        $query .= "     T3.COURSECD   = T4.COURSECD AND ";
        $query .= "     T4.MAJORCD    = T3.MAJORCD ";
        $query .= " ORDER BY ";
        $query .= "     T4.GRADE, ";
        $query .= "     T4.COURSECD, ";
        $query .= "     T4.MAJORCD, ";
        $query .= "     T4.COURSECODE ";

        return $query;
    }

    //コース一覧取得
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CREDIT_SPECIAL_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_MST L1 ON L1.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.COURSECODE = '".$model->coursecode."' AND ";
        $query .= "     T1.COURSECD   = '".$model->coursecd ."' AND ";
        $query .= "     T1.MAJORCD    = '".$model->majorcd ."' AND ";
        $query .= "     T1.GRADE      = '".$model->grade ."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SPECIAL_GROUP_CD ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $db) {
        $query  = " SELECT * FROM CREDIT_SPECIAL_MST ";
        $query .= "  WHERE coursecode = '".$model->coursecode."' AND ";
        $query .= "        coursecd   = '".$model->coursecd ."' AND ";
        $query .= "        majorcd    = '".$model->majorcd ."' AND ";
        $query .= "        grade      = '".$model->grade ."' AND ";
        $query .= "        special_group_cd = '".$model->special_group_cd."' AND ";
        $query .= "        year       = '".CTRL_YEAR."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //重複チェック
    function chkCODE($model, $db) {
        $query  = " SELECT * FROM CREDIT_SPECIAL_MST ";
        $query .= "  WHERE coursecode = '".$model->coursecode."' AND ";
        $query .= "        coursecd   = '".$model->coursecd ."' AND ";
        $query .= "        majorcd    = '".$model->majorcd ."' AND ";
        $query .= "        grade      = '".$model->grade ."' AND ";
        $query .= "        special_group_cd = '".$model->field["SPECIAL_GROUP_CD"]."' AND ";
        $query .= "        year       = '".CTRL_YEAR."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //科目取得
    function getAttendSubclassSpecialMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_MST ";
        $query .= " ORDER BY ";
        $query .= "     SPECIAL_GROUP_CD ";

        return $query;
    }

    //前年度からのデータを取得
    function &getOldYear($model) {
        $query  = " SELECT * ";
        $query .= " FROM ";
        $query .= "     CREDIT_SPECIAL_MST m ";
        $query .= " WHERE ";
        $query .= "     m.year = '".(CTRL_YEAR-1)."' AND ";
        $query .= "     m.coursecode = '".$model->coursecode."' AND ";
        $query .= "     not exists (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     CREDIT_SPECIAL_MST m3 ";
        $query .= "                 WHERE ";
        $query .= "                     m3.year = '".CTRL_YEAR."' AND ";
        $query .= "                     m3.coursecode = m.coursecode AND ";
        $query .= "                     m3.coursecd = m.coursecd AND ";
        $query .= "                     m3.majorcd = m.majorcd AND ";
        $query .= "                     m3.grade = m.grade AND ";
        $query .= "                     m3.special_group_cd = m.special_group_cd  ";
        $query .= "                 ) ";
        $query .= " AND  exists (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 coursecode_mst m2 ";
        $query .= "             WHERE ";
        $query .= "                 m.coursecode = m2.coursecode ";
        $query .= "             ) ";

        return $query;
    }

    //前年度からのデータを今年度にINSERT
    function &copyInsert($Row) {
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["GRADE"][TEXT]                = $Row["GRADE"];
        $data["COURSECODE"][TEXT]           = $Row["COURSECODE"];
        $data["COURSECD"][TEXT]             = $Row["COURSECD"];
        $data["MAJORCD"][TEXT]              = $Row["MAJORCD"];
        $data["SPECIAL_GROUP_CD"][TEXT]     = $Row["SPECIAL_GROUP_CD"];
        $data["CREDITS"][NUMBER]            = $Row["CREDITS"];
        $data["ABSENCE_HIGH"][NUMBER]       = $Row["ABSENCE_HIGH"];
        $data["ABSENCE_WARN"][NUMBER]       = $Row["ABSENCE_WARN"];
        $data["ABSENCE_WARN2"][NUMBER]      = $Row["ABSENCE_WARN2"];
        $data["ABSENCE_WARN3"][NUMBER]      = $Row["ABSENCE_WARN3"];
        $data["REQUIRE_FLG"][TEXT]          = $Row["REQUIRE_FLG"];
        $data["AUTHORIZE_FLG"][TEXT]        = $Row["AUTHORIZE_FLG"];
        $data["COMP_UNCONDITION_FLG"][TEXT] = $Row["COMP_UNCONDITION_FLG"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "CREDIT_SPECIAL_MST");
        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        if($model->cmd == "update"){
            knjz203Query::getDeleteQuery($model); //update時
        }

        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["COURSECODE"][TEXT]           = $model->coursecode;
        $data["COURSECD"][TEXT]             = $model->coursecd;
        $data["MAJORCD"][TEXT]              = $model->majorcd;
        $data["GRADE"][TEXT]                = $model->grade;
        $data["SPECIAL_GROUP_CD"][TEXT]     = $model->field["SPECIAL_GROUP_CD"];
        $data["CREDITS"][NUMBER]            = NULL;
        $data["ABSENCE_HIGH"][NUMBER]       = $model->field["ABSENCE_HIGH"];
        $data["ABSENCE_WARN"][NUMBER]       = $model->field["ABSENCE_WARN"];
        $data["ABSENCE_WARN2"][NUMBER]      = $model->field["ABSENCE_WARN2"];
        $data["ABSENCE_WARN3"][NUMBER]      = $model->field["ABSENCE_WARN3"];
        $data["REQUIRE_FLG"][TEXT]          = $model->field["REQUIRE_FLG"];
        $data["AUTHORIZE_FLG"][TEXT]        = $model->field["AUTHORIZE_FLG"];
        $data["COMP_UNCONDITION_FLG"][TEXT] = $model->field["COMP_UNCONDITION_FLG"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "CREDIT_SPECIAL_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM CREDIT_SPECIAL_MST ";
        $query .= "      WHERE year             = '".CTRL_YEAR."' AND ";
        $query .= "            coursecd         = '" .$model->coursecd ."' AND ";
        $query .= "            majorcd          = '" .$model->majorcd ."' AND ";
        $query .= "            grade            = '" .$model->grade ."' AND ";
        $query .= "            coursecode       = '" .$model->coursecode ."' AND ";
        $query .= "            special_group_cd = '" .$model->field["SPECIAL_GROUP_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //必履修区分取得
    function getRequireName($model) {
        $query  = " SELECT * FROM V_NAME_MST ";
        $query .= "     WHERE YEAR ='".CTRL_YEAR."' AND ";
        $query .= "           NAMECD1 ='Z011'";
        return $query;
    }
}
?>
