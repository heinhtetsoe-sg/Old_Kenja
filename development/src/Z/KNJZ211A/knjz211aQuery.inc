<?php

require_once('for_php7.php');

class knjz211aquery extends Query {
    //名称マスタから観点評価区分を取得
    function getDiv($model) {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z054' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     S1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT S1 ";
        $query .= "              ON T1.YEAR     = S1.YEAR ";
        $query .= "             AND T1.GRADE    = S1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         AND S1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND S1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科取得
    function getClassMst($model) {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND  AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || ' ' || S1.CLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) AS VALUE, ";
            $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) || ' ' || S1.CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     INNER JOIN V_CLASS_MST S1 ";
        $query .= "              ON T1.YEAR         = S1.YEAR ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND T1.CLASSCD      = S1.CLASSCD ";
        } else {
            $query .= "             AND SUBSTR(T1.SUBCLASSCD, 1, 2) = S1.CLASSCD ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND T1.SCHOOL_KIND  = S1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE    = '".$model->field["GRADE"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclassMst($model) {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || S1.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.SUBCLASSCD || ' ' || S1.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     INNER JOIN V_SUBCLASS_MST S1 ";
        $query .= "              ON S1.YEAR             = T1.YEAR ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND S1.CLASSCD          = T1.CLASSCD ";
            $query .= "             AND S1.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "             AND S1.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "             AND S1.SUBCLASSCD       = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE = '".$model->field["GRADE"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND = '".$model->field["CLASSCD"]."' ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //観点取得
    function getViewcd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "    T1.VIEWCD AS VALUE, ";
        $query .= "    S1.VIEWNAME AS LABEL, ";
        $query .= "    S1.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_MST S1 ";
        $query .= "              ON S1.GRADE            = T1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND S1.CLASSCD          = T1.CLASSCD ";
            $query .= "             AND S1.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "             AND S1.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "             AND S1.SUBCLASSCD       = T1.SUBCLASSCD ";
        $query .= "             AND S1.VIEWCD           = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE        = '".$model->field["GRADE"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD  || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    S1.SHOWORDER, ";
        $query .= "    VALUE ";

        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model){
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='".CTRL_YEAR."' ";
        $query .= "     AND GRADE='".$model->field["GRADE"]."' ";

        return $query;
    }

    //対象データの段階値数チェック
    function selectCountQuery($model) {

        $query  = " SELECT  ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_SEMES_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        if ($model->field['CLASSCD'] == 'all') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-{$model->schoolKind}-00-000000' AND ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        if($model->field["DIV"] == '2' || $model->field['CLASSCD'] == 'all'){
            $query .= "     T1.VIEWCD   = '0000' AND ";
        } else {
            $query .= "     T1.VIEWCD   = '".$model->field["VIEWCD"]."' AND ";
        }
        $query .= "     T1.DIV      = '".$model->field["DIV"]."' AND ";
        $query .= "     T1.GRADE    = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {

        $query  = " WITH NAMEDATA AS ( ";
        for ($i = 1; $i <= $model->field["MAX_ASSESSLEVEL"]; $i++) {
            if ($i > 1) {
                $query .= "  UNION ";
            }
            $query .= "  SELECT ";
            $query .= "      '".$i."' AS ROW_NUM ";
            $query .= "  FROM ";
            $query .= "      SYSIBM.SYSDUMMY1 ";
        }
        $query .= " ), ASSESSDATA AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         ASSESSLOW, ";
        $query .= "         ASSESSHIGH, ";
        $query .= "         ASSESSLEVEL, ";
        $query .= "         ASSESSMARK ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_LEVEL_SEMES_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        if ($model->field['CLASSCD'] == 'all') {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '00-{$model->schoolKind}-00-000000' AND ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "         SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        if($model->field["DIV"] == '2' || $model->field['CLASSCD'] == 'all'){
            $query .= "         VIEWCD      = '0000' AND ";
        } else {
            $query .= "         VIEWCD      = '".$model->field["VIEWCD"]."' AND ";
        }
        $query .= "         DIV         = '".$model->field["DIV"]."' AND ";
        $query .= "         GRADE       = '".$model->field["GRADE"]."' ";
        $query .= " ), ASSESSDATA_LIST AS ( ";
        $query .= "     SELECT DISTINCT";
        $query .= "         ROW_NUMBER() OVER(ORDER BY ASSESSLEVEL) AS ROW_NUM, ";
        $query .= "         ASSESSLEVEL, ";
        $query .= "         ASSESSLOW, ";
        $query .= "         ASSESSHIGH, ";
        $query .= "         ASSESSMARK ";
        $query .= "     FROM ";
        $query .= "         ASSESSDATA ";
        $query .= "     ORDER BY ";
        $query .= "         ASSESSLEVEL DESC";
        $query .= " ) ";

        $query .= " SELECT DISTINCT";
        $query .= "     T1.ROW_NUM, ";
        $query .= "     T2.ASSESSLEVEL, ";
        $query .= "     T2.ASSESSLOW, ";
        $query .= "     T2.ASSESSHIGH, ";
        $query .= "     T2.ASSESSMARK ";
        $query .= " FROM ";
        $query .= "     NAMEDATA T1 ";
        $query .= "     LEFT JOIN ASSESSDATA_LIST T2 ON int(T1.ROW_NUM) = T2.ROW_NUM ";
        $query .= " ORDER BY ";
        $query .= "     T1.ROW_NUM DESC";

        return $query;
    }

    //データ削除
    function delete($model, $db) {
        $query  = " DELETE FROM ";
        $query .= "     JVIEWSTAT_LEVEL_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        if ($model->field['CLASSCD'] == 'all') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '00-{$model->schoolKind}-00-000000' AND ";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        if($model->field["DIV"] == '2' || $model->field['CLASSCD'] == 'all'){
            $query .= "     VIEWCD      = '0000' AND ";
        } else {
            $query .= "     VIEWCD      = '".$model->field["VIEWCD"]."' AND ";
        }
        $query .= "     DIV         = '".$model->field["DIV"]."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";

        $db->query($query);

        return;
    }

    // データ更新処理
    function update($model, $db) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        for ($i = 0; $i < get_count($model->data["ROW_NUM"]); $i++) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            if ($model->field['CLASSCD'] == 'all') {
                $data["CLASSCD"][TEXT]          = '00';
                $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
                $data["CURRICULUM_CD"][TEXT]    = '00';
                $data["SUBCLASSCD"][TEXT]       = '000000';
            } else if ($model->Properties["useCurriculumcd"] == "1") {
                $data["CLASSCD"][TEXT]          = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
            }
            if($model->field["DIV"] == '2' || $model->field["CLASSCD"] == "all"){
                $data["VIEWCD"][TEXT]           = '0000';
            } else {
                $data["VIEWCD"][TEXT]           = $model->field["VIEWCD"];
            }
            $data["DIV"][TEXT]              = $model->field["DIV"];
            $data["GRADE"][TEXT]            = $model->field["GRADE"];
            $data["ASSESSLEVEL"][NUMBER]    = $model->fields["ASSESSLEVEL"][$i];
            if ($model->data["ROW_NUM"][$i] === '1') {
                $data["ASSESSLOW"][NUMBER]   = 1;
            } else {
                $data["ASSESSLOW"][NUMBER]   = $model->fields["ASSESSLOW"][$i];
            }
            
            if ($i == 0) {
                $data["ASSESSHIGH"][NUMBER]  = '100';
            } else {
                $data["ASSESSHIGH"][NUMBER]  = $model->fields["ASSESSHIGH"][$i];
            }
            $data["ASSESSMARK"][TEXT]    = $model->fields["ASSESSMARK"][$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "JVIEWSTAT_LEVEL_SEMES_MST");
            $db->query($query);
        }

        return;
    }

    function copy($model, $db) {

        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        if ($model->field["DIV"] == '2') {
            $query .= "     VIEWCD      = '0000' AND ";
        } else {
            $query .= "     VIEWCD      = '".$model->field["VIEWCD2"]."' AND ";
        }
        $query .= "     DIV         = '".$model->field["DIV"]."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";

        $result = $db->query($query);
        $cnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = $row["YEAR"];
            $data["SEMESTER"][TEXT]         = $row["SEMESTER"];
            $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
            if($model->field["DIV"] == '2') {
                $data["VIEWCD"][TEXT]           = '0000';
            } else {
                $data["VIEWCD"][TEXT]           = $model->field["VIEWCD"];
            }
            $data["DIV"][TEXT]              = $row["DIV"];
            $data["GRADE"][TEXT]            = $row["GRADE"];
            $data["ASSESSLEVEL"][NUMBER]    = $row["ASSESSLEVEL"];;
            $data["ASSESSLOW"][NUMBER]      = $row["ASSESSLOW"];
            $data["ASSESSHIGH"][NUMBER]     = $row["ASSESSHIGH"];
            $data["ASSESSMARK"][TEXT]       = $row["ASSESSMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "JVIEWSTAT_LEVEL_SEMES_MST");
            $db->query($query);
            $cnt++;
        }

        return $cnt;
    }
}
?>
