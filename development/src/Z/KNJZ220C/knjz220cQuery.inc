<?php

require_once('for_php7.php');

class knjz220cQuery extends Query {

    // 学期
    function semesterQuery() {
        $query  = " SELECT ";
        $query .= "   SEMESTER AS VALUE ";
        $query .= " , SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "   SEMESTER ";

        return $query;
    }

    // 学期名
    function getSemestername($model) {
        $query  = " SELECT ";
        $query .= "   SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER = '".$model->semester."' ";

        return $query;
    }

    // 科目名
    function getSubclassname($subclasscd) {
        $query  = " SELECT ";
        $query .= "   SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "   SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
        return $query;
    }

    //デフォルト値を取得
    function getDefaultData() {
        //評定区分は固定値３
        $sep = "3";

        $query  = " SELECT                     ";
        $query .= "     ASSESSCD,              ";
        $query .= "     ASSESSLEVEL,           ";
        $query .= "     ASSESSMARK,            ";
        $query .= "     ASSESSLOW,             ";
        $query .= "     ASSESSHIGH             ";
        $query .= " FROM                       ";
        $query .= "     ASSESS_MST             ";
        $query .= " WHERE                      ";
        $query .= "     ASSESSCD = '".$sep."'  ";
        $query .= " ORDER BY                   ";
        $query .= "     ASSESSLEVEL,           ";
        $query .= "     ASSESSLEVEL            ";

        return $query;
    }

    //学年コンボボックスの値を取得
    function combo_grdQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //校種を取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$model->grade}' ";

        return $query;
    }

    //教科コンボボックスの値を取得
    function combo_clsQuery($model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   CLASSCD, ";
            $query .= "   SCHOOL_KIND, ";
            $query .= "   CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
            $query .= "   CLASSCD || '-' || SCHOOL_KIND || '：' || CLASSNAME AS LABEL ";
        } else {
            $query .= "   CLASSCD, ";
            $query .= "   CLASSCD AS VALUE, ";
            $query .= "   CLASSCD '：' || CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "   V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND = (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                             YEAR  = '".CTRL_YEAR."' ";
            $query .= "                         AND GRADE = '".$model->grade."' ";
            $query .= "                     ) ";
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   CLASSCD, ";
            $query .= "   SCHOOL_KIND ";
        } else {
            $query .= "   CLASSCD ";
        }

        return $query;
    }

    //評定コピーコンボボックスの値を取得
    function Copy_comboQuery($grade, $model) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field1["SUBCLASSCD"]);

        $query  = " SELECT DISTINCT ";
        $query .= "   S.CLASSCD || '-' || S.SCHOOL_KIND || '-' || S.CURRICULUM_CD || '-' || S.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "   S.SUBCLASSNAME, ";
        $query .= "   '　' || L1.GRADE_NAME1 || '： ' || S.CLASSCD || '-' || S.SCHOOL_KIND || '-' || S.CURRICULUM_CD || '-' || S.SUBCLASSCD || ' ' || S.SUBCLASSNAME || '　' AS LABEL, ";
        $query .= "   R.GRADE || ',' || S.CLASSCD || '-' || S.SCHOOL_KIND || '-' || S.CURRICULUM_CD || '-' || S.SUBCLASSCD AS VALUE ";
        $query .= " FROM                       ";
        $query .= "   ASSESS_LEVEL_SDIV_MST R  ";
        $query .= " LEFT JOIN SUBCLASS_MST S ON R.SUBCLASSCD = S.SUBCLASSCD ";
        $query .= "                         AND R.CLASSCD       = S.CLASSCD ";
        $query .= "                         AND R.SCHOOL_KIND   = S.SCHOOL_KIND ";
        $query .= "                         AND R.CURRICULUM_CD = S.CURRICULUM_CD ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = R.YEAR ";
        $query .= "                         AND L1.GRADE = R.GRADE ";
        $query .= " WHERE ";
        $query .= "     R.YEAR = '".CTRL_YEAR."'  ";
        $query .= " AND R.SEMESTER = '".$model->semester."'  ";
        $query .= " AND R.TESTKINDCD = '99'  ";
        $query .= " AND R.TESTITEMCD = '00'  ";
        $query .= " AND R.SCORE_DIV  = '09'  ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND R.SCHOOL_KIND = (SELECT ";
            $query .= "                          SCHOOL_KIND ";
            $query .= "                      FROM ";
            $query .= "                          SCHREG_REGD_GDAT ";
            $query .= "                      WHERE ";
            $query .= "                              YEAR  = '".CTRL_YEAR."' ";
            $query .= "                          AND GRADE = '".$grade."' ";
            $query .= "                      ) ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND R.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " AND R.RUISEKI_DIV = 'A' "; // 絶対評価
        $query .= " AND R.DIV = '1' ";
        $query .= " AND R.GRADE    = '".$grade."' ";
        $query .= " AND R.HR_CLASS    = '000'";
        $query .= " AND R.COURSECD    = '0' ";
        $query .= " AND R.MAJORCD     = '000' ";
        $query .= " AND R.COURSECODE  = '0000' ";
        $query .= " AND NOT ( ";
        $query .= "         R.SEMESTER       = '".$model->semester."' ";
        $query .= "     AND R.CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "     AND R.SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "     AND R.CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "     AND R.SUBCLASSCD     = '".$subclass_array[3]."' ";
        $query .= " ) ";
        $query .= " ORDER BY                   ";
        $query .= "   SUBCLASSCD             ";

        return $query;
    }

    //リスト内情報取得
    function ReadQuery($cd, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $cd);
        }
        $query  = " SELECT ";
        $query .= "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        $query .= "   SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "   V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND CLASSCD          = '".$class_array[0]."' ";
        $query .= "   AND SCHOOL_KIND      = '".$class_array[1]."' ";
        $query .= " ORDER  BY ";
        $query .= "   VALUE ";

        return $query;
    }

    //評定マスタ情報抽出
    function selectQuery($model) {

        //コピー情報取得
        if ($model->copy["FLG"] == true) {
            list($grade, $sbclss) = explode(",", $model->copy["SELECT"]);
        } else if ($model->copy["FLG"] == false) {
            $grade  = $model->grade;
            $sbclss = $model->field1["SUBCLASSCD"];
        }

        $subclass_array = array();
        $subclass_array = explode("-", $sbclss);

        $query  = " SELECT ";
        $query .= "     ASSESSLEVEL, ";
        $query .= "     ASSESSMARK, ";
        $query .= "     ASSESSLOW, ";
        $query .= "     ASSESSHIGH, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     ASSESS_LEVEL_SDIV_MST R ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV  = '09' ";
        $query .= " AND CLASSCD         = '".$subclass_array[0]."' ";
        $query .= " AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
        $query .= " AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
        $query .= " AND SUBCLASSCD      = '".$subclass_array[3]."' ";
        $query .= " AND RUISEKI_DIV = 'A' "; // 絶対評価
        $query .= " AND DIV = '1' ";
        $query .= " AND GRADE  = '".$grade."' ";
        $query .= " AND HR_CLASS    = '000'";
        $query .= " AND COURSECD    = '0' ";
        $query .= " AND MAJORCD     = '000' ";
        $query .= " AND COURSECODE  = '0000' ";
        $query .= " ORDER BY ";
        $query .= "      ASSESSLOW ";
        return $query;
    }

    //delete処理
    function &getDeleteQuery($model) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field1["SUBCLASSCD"]);

        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ASSESS_LEVEL_SDIV_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV  = '09' ";
        $query .= "     AND CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "     AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "     AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "     AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        $query .= "     AND RUISEKI_DIV = 'A' "; // 絶対評価
        $query .= "     AND DIV = '1' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        $query .= "     AND HR_CLASS    = '000'";
        $query .= "     AND COURSECD    = '0' ";
        $query .= "     AND MAJORCD     = '000' ";
        $query .= "     AND COURSECODE  = '0000' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    //insert処理
    function &getInsertQuery($model) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field1["SUBCLASSCD"]);

        //db open
        $db = Query::dbCheckOut();

        for($i = 1; $i <= $model->assesslevelcnt; $i++) {
            $ASSESSLOW  = "ASSESSLOW".($i);

            $data["YEAR"][TEXT] =  CTRL_YEAR;
            $data["SEMESTER"][TEXT] = $model->semester;
            $data["TESTKINDCD"][TEXT] = '99';
            $data["TESTITEMCD"][TEXT] = '00';
            $data["SCORE_DIV"][TEXT] = '09';
            $data["CLASSCD"][TEXT]       = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]   = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT] = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]    = $subclass_array[3];
            $data["RUISEKI_DIV"][TEXT] =  'A'; // 絶対評価
            $data["DIV"][TEXT] =  '1';
            $data["GRADE"][TEXT] = $model->grade;
            $data["HR_CLASS"][TEXT] = "000";
            $data["COURSECD"][TEXT] = "0";
            $data["MAJORCD"][TEXT] =  "000";
            $data["COURSECODE"][TEXT] =  "0000";
            $data["ASSESSLEVEL"][NUMBER] = ($i);
            $data["ASSESSMARK"][TEXT]    = $model->field2["ASSESSMARK".$i];
            $data["ASSESSLOW"][NUMBER]   = ($i == 1)    ? $model->default_val["LOW"]  : $model->field2[$ASSESSLOW];
            $data["ASSESSHIGH"][NUMBER]  = ($i == $model->assesslevelcnt) ? $model->field2["ASSESSHIGH".$model->assesslevelcnt] : (($model->field2["ASSESSLOW".($i + 1)])-1);
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";
            $query = Query::insertSQL($data, "ASSESS_LEVEL_SDIV_MST");

            $db->query($query);
        }
        Query::dbCheckIn($db);
        return true;
    }
}
?>
