<?php

require_once('for_php7.php');

class knjh537Query extends Query {

    //リスト
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD || ':' || T1.SUBCLASS_NAME AS SUBCLASS_NAME, ";
        $query .= "     T1.SUBCLASS_ABBV, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || ':' || L2.CLASSNAME AS CLASSNAME, ";
            $query .= "     T1.CURRICULUM_CD || ':' || N1.NAME1 AS CURRICULUM_NAME, ";
        } else {
            $query .= "     T1.CLASSCD || ':' || L2.CLASSNAME AS CLASSNAME, ";
        }
        $query .= "     T1.PREF_SUBCLASSCD || ':' || L1.SUBCLASS_NAME AS PREF_SUBCLASS_NAME";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST T1";
        $query .= " LEFT JOIN  ";
        $query .= "     PROFICIENCY_PREF_SUBCLASS_MST L1 ON T1.PREF_SUBCLASSCD = L1.PREF_SUBCLASSCD ";
        $query .= " LEFT JOIN  ";
        $query .= "     CLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.SCHOOL_KIND = L2.SCHOOL_KIND ";
            $query .= " LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z018' ";
            $query .= "                      AND N1.NAMECD2 = T1.CURRICULUM_CD ";
        }
        $query .= " ORDER BY ";
        $query .= "     PROFICIENCY_SUBCLASS_CD ";

        return $query;
    }

    //１レコード取得
    function getRow($cd, $db)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= " PROFICIENCY_SUBCLASS_CD = '" .$cd ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //教育課程コンボ
    function getCurriculumCD($model) {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'Z018' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科コンボ
    function getClasscd($model) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  || ':' || CLASSNAME AS LABEL ";
        } else {
            $query .= "     CLASSCD AS VALUE, ";
            $query .= "     CLASSCD || ':' || CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     , SCHOOL_KIND ";
        }

        return $query;
    }

    //県下統一実力科目コンボ
    function getPrefSubclasscd() {
        $query  = " SELECT ";
        $query .= "     PREF_SUBCLASSCD AS VALUE, ";
        $query .= "     PREF_SUBCLASSCD || ':' || SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_PREF_SUBCLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     PREF_SUBCLASSCD ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data["PROFICIENCY_SUBCLASS_CD"][TEXT]  = $fields["PROFICIENCY_SUBCLASS_CD"];
        $data["SUBCLASS_NAME"][TEXT]            = $fields["SUBCLASS_NAME"];
        $data["SUBCLASS_ABBV"][TEXT]            = $fields["SUBCLASS_ABBV"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classCd, $schoolKind) = preg_split("/-/", $fields["CLASSCD"]);
            $data["CLASSCD"][TEXT]                  = $classCd;
            $data["SCHOOL_KIND"][TEXT]              = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]            = $fields["CURRICULUM_CD"];
        } else {
            $data["CLASSCD"][TEXT]                  = $fields["CLASSCD"];
        }
        $data["PREF_SUBCLASSCD"][TEXT]          = $fields["PREF_SUBCLASSCD"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]	            = "sysdate()";

        $query = Query::insertSQL($data, "PROFICIENCY_SUBCLASS_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data["PROFICIENCY_SUBCLASS_CD"][TEXT]  = $fields["PROFICIENCY_SUBCLASS_CD"];
        $data["SUBCLASS_NAME"][TEXT]            = $fields["SUBCLASS_NAME"];
        $data["SUBCLASS_ABBV"][TEXT]            = $fields["SUBCLASS_ABBV"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classCd, $schoolKind) = preg_split("/-/", $fields["CLASSCD"]);
            $data["CLASSCD"][TEXT]                  = $classCd;
            $data["SCHOOL_KIND"][TEXT]              = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]            = $fields["CURRICULUM_CD"];
        } else {
            $data["CLASSCD"][TEXT]                  = $fields["CLASSCD"];
        }
        $data["PREF_SUBCLASSCD"][TEXT]          = $fields["PREF_SUBCLASSCD"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";

        $where = " WHERE PROFICIENCY_SUBCLASS_CD = '" .$fields["PROFICIENCY_SUBCLASS_CD"]."'";

        $query = Query::updateSQL($data, "PROFICIENCY_SUBCLASS_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCY_SUBCLASS_CD  = '" .$fields["PROFICIENCY_SUBCLASS_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
