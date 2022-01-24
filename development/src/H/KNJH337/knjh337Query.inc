<?php

require_once('for_php7.php');

class knjh337Query extends Query {

    //リスト
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     T1.SUBCLASS_NAME, ";
        $query .= "     T1.SUBCLASS_ABBV, ";
        $query .= "     T1.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SUBCLASS_DIV, ";
        $query .= "     L1.SUBCLASS_NAME AS PREF_SUBCLASS_NAME";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST T1";
        $query .= " LEFT JOIN  ";
        $query .= "     MOCK_PREF_SUBCLASS_MST L1 ON T1.PREF_SUBCLASSCD = L1.PREF_SUBCLASSCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "    AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     MOCK_SUBCLASS_CD ";

        return $query;
    }

    //１レコード取得
    function getRow($cd, $db, $model)
    {
        $query  = " SELECT ";
        $query .= "     MOCK_SUBCLASS_CD, ";
        $query .= "     SUBCLASS_NAME, ";
        $query .= "     SUBCLASS_ABBV, ";
        $query .= "     SUBCLASS_DIV, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     CLASSCD, ";
            $query .= "     SUBCLASSCD, ";
        }
        $query .= "     PREF_SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     MOCK_SUBCLASS_CD = '" .$cd ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //教科コンボ
    function getClasscd($model) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '　' || CLASSNAME AS LABEL ";
        } else {
            $query .= "     CLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '　' || CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "    SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    function getSubclasscd($classCd, $model) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
            $query .= "     SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  = '{$classCd}' ";
        } else {
            $query .= "     SUBCLASSCD LIKE '{$classCd}%' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CURRICULUM_CD DESC, VALUE ";

        return $query;
    }

    //県下統一模試科目コンボ
    function getPrefSubclasscd() {
        $query  = " SELECT ";
        $query .= "     PREF_SUBCLASSCD AS VALUE, ";
        $query .= "     PREF_SUBCLASSCD || '　' || SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_PREF_SUBCLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     PREF_SUBCLASSCD ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data["MOCK_SUBCLASS_CD"][TEXT] = $fields["MOCK_SUBCLASS_CD"];
        $data["SUBCLASS_NAME"][TEXT]    = $fields["SUBCLASS_NAME"];
        $data["SUBCLASS_ABBV"][TEXT]    = $fields["SUBCLASS_ABBV"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind) = explode('-', $fields["CLASSCD"]);
            $subclasscd = explode('-', $fields["SUBCLASSCD"]);
            $data["CLASSCD"][TEXT]          = $classcd;
            $data["SCHOOL_KIND"][TEXT]      = $school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $subclasscd[2];
            $data["SUBCLASSCD"][TEXT]       = $subclasscd[3];
        } else {
            $data["CLASSCD"][TEXT]          = $fields["CLASSCD"];
            $data["SUBCLASSCD"][TEXT]       = $fields["SUBCLASSCD"];
        }
        $data["PREF_SUBCLASSCD"][TEXT]  = $fields["PREF_SUBCLASSCD"];
        $data["SUBCLASS_DIV"][TEXT]     = $fields["SUBCLASS_DIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_SUBCLASS_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data["MOCK_SUBCLASS_CD"][TEXT] = $fields["MOCK_SUBCLASS_CD"];
        $data["SUBCLASS_NAME"][TEXT]    = $fields["SUBCLASS_NAME"];
        $data["SUBCLASS_ABBV"][TEXT]    = $fields["SUBCLASS_ABBV"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind) = explode('-', $fields["CLASSCD"]);
            $subclasscd = explode('-', $fields["SUBCLASSCD"]);
            $data["CLASSCD"][TEXT]          = $classcd;
            $data["SCHOOL_KIND"][TEXT]      = $school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $subclasscd[2];
            $data["SUBCLASSCD"][TEXT]       = $subclasscd[3];
        } else {
            $data["CLASSCD"][TEXT]          = $fields["CLASSCD"];
            $data["SUBCLASSCD"][TEXT]       = $fields["SUBCLASSCD"];
        }
        $data["PREF_SUBCLASSCD"][TEXT]  = $fields["PREF_SUBCLASSCD"];
        $data["SUBCLASS_DIV"][TEXT]     = $fields["SUBCLASS_DIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where = " WHERE MOCK_SUBCLASS_CD = '" .$fields["MOCK_SUBCLASS_CD"]."'";

        $query = Query::updateSQL($data, "MOCK_SUBCLASS_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MOCK_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     MOCK_SUBCLASS_CD  = '" .$fields["MOCK_SUBCLASS_CD"]."' ";

      $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
