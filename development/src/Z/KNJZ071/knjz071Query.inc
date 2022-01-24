<?php

require_once('for_php7.php');

class knjz071Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 || '：' || ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科取得（左画面）
    function getClassCd($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '：' || CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ANOTHER_CLASS_MST ";
        if ($model->leftField["SCHKIND"] != "" && $model->leftField["SCHKIND"] != "99") {
            $query .= "     WHERE SCHOOL_KIND = '".$model->leftField["SCHKIND"]."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     WHERE SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CLASSCD ";

        return $query;
    }

    //ANOTHER_SUBCLASS_MST データ取得
    function getSubclassData($model)
    {
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      ANOTHER_SUBCLASS_MST ";
        if ($model->leftField["S_CLASSCD"] != "99" || $model->leftField["S_CURRICULUM_CD"] != "99"
        //  || $model->leftField["SCHKIND"] != "99"
            || $model->Properties["use_prg_schoolkind"] == "1"
            ) {
            $query .= " WHERE ";
        } else if ($model->leftField["S_CLASSCD"] != "99" || $model->leftField["S_CURRICULUM_CD"] != "99"
            || $model->Properties["useSchool_KindField"] == "1"
        ) {
            $query .= " WHERE ";
        }
        if ($model->leftField["S_CLASSCD"] != "99") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND = '".$model->leftField["S_CLASSCD"]."' ";
            $and = "AND";
        }
        if ($model->leftField["S_CURRICULUM_CD"] != "99") {
            $query .= "     {$and} CURRICULUM_CD = '".$model->leftField["S_CURRICULUM_CD"]."' ";
            $and = "AND";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->leftField["SCHKIND"] != "99") {
                $query .= "     {$and} SCHOOL_KIND = '".$model->leftField["SCHKIND"]."' ";
            } else {
                if ($model->selectSchoolKind) {
                    $query .= "    {$and} SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                } else {
                    $query .= "    {$and} SCHOOL_KIND IS NOT NULL ";
                }
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     {$and} SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }
    
    function getClassData($model)
    {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || ':' || CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ANOTHER_CLASS_MST ";
        if ($model->leftField["SCHKIND"] != "" && $model->leftField["SCHKIND"] != "99") {
            $query .= " WHERE SCHOOL_KIND = '".$model->leftField["SCHKIND"]."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND ";

        return $query;
    }

    //教育課程コード取得
    function getNamecd($cd)
    {
        $query  = " SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "      V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "      NAMECD1 = '".$cd."' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $school_kind, $curriculum_cd, $cd)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $cd);

        $db = Query::dbCheckOut();

        $query  = "SELECT ";
        $query .= "   T1.* , ";
        $query .= "   CLASSCD || '-' || SCHOOL_KIND AS CLASSCD_SET ";
        $query .= "FROM ";
        $query .= "     ANOTHER_SUBCLASS_MST T1";
        if ($school_kind == "" && $curriculum_cd == "") {
            $query .= "  WHERE ";
            $query .= "         CLASSCD         = '" .$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND     = '" .$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD   = '" .$model->field["CURRICULUM_CD"]."' ";
            $query .= "     AND SUBCLASSCD      = '" .$subclass_array[2]."' ";
        } else if ($school_kind != "" && $curriculum_cd != "") {
            $query .= "  WHERE ";
            $query .= "         CLASSCD         = '" .substr($cd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '" .$school_kind."' ";
            $query .= "     AND CURRICULUM_CD   = '" .$curriculum_cd."' ";
            $query .= "     AND SUBCLASSCD      = '" .$cd."' ";
        }
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    //教育課程_チェック用１レコード取得
    function getRow_check($model, $cd, $checkcd)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $cd);
        
        $db = Query::dbCheckOut();

        $query  = "SELECT ";
        $query .= "   T1.* , ";
        $query .= "   CLASSCD || '-' || SCHOOL_KIND AS CLASSCD_SET ";
        $query .= "FROM ";
        $query .= "     ANOTHER_SUBCLASS_MST T1";
        $query .= "  WHERE ";
        $query .= "         CLASSCD         = '" .$subclass_array[0]."' ";
        $query .= "     AND SCHOOL_KIND     = '" .$subclass_array[1]."' ";
        $query .= "     AND CURRICULUM_CD   = '" .$model->field["CURRICULUM_CD"]."' ";
        $query .= "     AND SUBCLASSCD      = '" .$checkcd."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    //INSERT
    function &getInsertQuery($fields, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $fields["SUBCLASSCD"]);
        
        $db = Query::dbCheckOut();

        $data["CLASSCD"][TEXT]              = $subclass_array[0];
        $data["SCHOOL_KIND"][TEXT]          = $subclass_array[1];
        $data["CURRICULUM_CD"][TEXT]        = $fields["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]           = $subclass_array[2];
        $data["SUBCLASSNAME"][TEXT]         = $fields["SUBCLASSNAME"];
        $data["SUBCLASSABBV"][TEXT]         = $fields["SUBCLASSABBV"];
        $data["SUBCLASSNAME_ENG"][TEXT]     = $fields["SUBCLASSNAME_ENG"];
        $data["SUBCLASSABBV_ENG"][TEXT]     = $fields["SUBCLASSABBV_ENG"];
        $data["SUBCLASSORDERNAME1"][TEXT]   = $fields["SUBCLASSORDERNAME1"];
        $data["SUBCLASSORDERNAME2"][TEXT]   = $fields["SUBCLASSORDERNAME2"];
        $data["SUBCLASSORDERNAME3"][TEXT]   = $fields["SUBCLASSORDERNAME3"];
        $data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        $data["SHOWORDER2"][NUMBER]         = $fields["SHOWORDER2"];
        $data["SHOWORDER3"][NUMBER]         = $fields["SHOWORDER3"];
        $data["SUBCLASSCD2"][TEXT]          = $fields["SUBCLASSCD2"];
        $data["SUBCLASSCD3"][TEXT]          = $fields["SUBCLASSCD3"];
        $data["ELECTDIV"][TEXT]             = ($fields["ELECTDIV"]==1) ? 1 : 0;
        $data["VALUATION"][NUMBER]          = $fields["VALUATION"];
        $data["GET_CREDIT"][NUMBER]         = $fields["GET_CREDIT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "ANOTHER_SUBCLASS_MST");
        $db->query($query);

        Query::dbCheckIn($db);

        return;
    }
    //UPDATE
    function &getUpdateQuery($fields, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $fields["SUBCLASSCD"]);
        
        $db = Query::dbCheckOut();

        $data["CLASSCD"][TEXT]              = $subclass_array[0];
        $data["SCHOOL_KIND"][TEXT]          = $subclass_array[1];
        $data["CURRICULUM_CD"][TEXT]        = $fields["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]           = $subclass_array[2];
        $data["SUBCLASSNAME"][TEXT]         = $fields["SUBCLASSNAME"];
        $data["SUBCLASSABBV"][TEXT]         = $fields["SUBCLASSABBV"];
        $data["SUBCLASSNAME_ENG"][TEXT]     = $fields["SUBCLASSNAME_ENG"];
        $data["SUBCLASSABBV_ENG"][TEXT]     = $fields["SUBCLASSABBV_ENG"];
        $data["SUBCLASSORDERNAME1"][TEXT]   = $fields["SUBCLASSORDERNAME1"];
        $data["SUBCLASSORDERNAME2"][TEXT]   = $fields["SUBCLASSORDERNAME2"];
        $data["SUBCLASSORDERNAME3"][TEXT]   = $fields["SUBCLASSORDERNAME3"];
        $data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        $data["SHOWORDER2"][NUMBER]         = $fields["SHOWORDER2"];
        $data["SHOWORDER3"][NUMBER]         = $fields["SHOWORDER3"];
        $data["SUBCLASSCD2"][TEXT]          = $fields["SUBCLASSCD2"];
        $data["SUBCLASSCD3"][TEXT]          = $fields["SUBCLASSCD3"];
        $data["ELECTDIV"][TEXT]             = ($fields["ELECTDIV"]==1) ? 1 : 0;
        $data["VALUATION"][NUMBER]          = $fields["VALUATION"];
        $data["GET_CREDIT"][NUMBER]         = $fields["GET_CREDIT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE  CLASSCD         = '" .$subclass_array[0]."'";
        $where .= "   AND  SCHOOL_KIND     = '" .$subclass_array[1]."'";
        $where .= "   AND  CURRICULUM_CD   = '" .$fields["CURRICULUM_CD"]."'";
        $where .= "   AND  SUBCLASSCD      = '" .$subclass_array[2]."'";
        
        $query = Query::updateSQL($data, "ANOTHER_SUBCLASS_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);

        return ;
    }
    //DELETE
    function &getDeleteQuery($fields, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $fields["SUBCLASSCD"]);
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM ANOTHER_SUBCLASS_MST ";
        $query .= " WHERE  CLASSCD         = '" .$subclass_array[0]."'";
        $query .= "   AND  SCHOOL_KIND     = '" .$subclass_array[1]."'";
        $query .= "   AND  CURRICULUM_CD   = '" .$fields["CURRICULUM_CD"]."'";
        $query .= "   AND  SUBCLASSCD      = '" .$subclass_array[2]."'";
        
        $db->query($query);
        Query::dbCheckIn($db);

        $result = true;

        return $result;
    }
}
?>
