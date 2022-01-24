<?php

require_once('for_php7.php');

class knjz402j_2Query extends Query {

    //１レコード取得
    function getRow($subclasscd, $viewcd, $model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.*, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  AS CLASSCD";
        } else {
            $query .= "     T1.*, ";
            $query .= "     SUBSTR(VIEWCD,1,2) AS CLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_MST T1 ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".substr($subclasscd, 0, 2)."' AND ";
            $query .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
            $query .= "     CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        }
        $query .= "     SUBCLASSCD = '".$subclasscd."' AND ";
        $query .= "     VIEWCD = '".$viewcd."' ";

        return $query;
    }

    //教育課程用１レコード取得(エラーチェック含む)
    function getRow2($subclasscd, $viewcd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd       = substr($subclasscd, 0, 2);
            $substr_subclasscd    = substr($subclasscd, 7, 6);
            $substr_school_kind   = substr($subclasscd, 3, 1);
            $substr_curriculum_cd = substr($subclasscd, 5, 1);
        }
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND  AS CLASSCD";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_MST T1 ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$substr_classcd."' AND ";
            $query .= "     SCHOOL_KIND     = '".$substr_school_kind."' AND ";
            $query .= "     CURRICULUM_CD   = '".$substr_curriculum_cd."' AND ";
            $query .= "     SUBCLASSCD      = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$subclasscd."' AND ";
        }
        $query .= "     VIEWCD = '".$viewcd."' ";

        return $query;
    }

    //観点一覧
    function getJViewNameList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     L1.CLASSNAME, ";
        $query .= "     L2.SUBCLASSNAME, ";
        $query .= "     L3.ABBV1 ";
        $query .= " FROM  ";
        $query .= "     JVIEWNAME_SUB_MST T1 ";
        $query .= "     LEFT JOIN CLASS_MST L1 ON L1.CLASSCD = SUBSTR(T1.VIEWCD,1,2) ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L2.CLASSCD = T1.CLASSCD ";
            $query .= "          AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "          AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'A023' ";
        $query .= "          AND L3.NAME1 = T1.SCHOOL_KIND ";
        if ($model->schkind != "" && $model->schkind != "99") {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SCHOOL_KIND DESC, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }
    //教科取得
    function getClassMst($model)
    {
        $query  = " (SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  || ':' || CLASSNAME AS LABEL ";
        } else {
            $query .= "     CLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '：' || CLASSNAME AS LABEL ";
        }
        $query .= " FROM  ";
        $query .= "     CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD <= '90' ";
        if ($model->schkind != "" && $model->schkind != "99") {
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE) ";
        $query .= " UNION ";
        $query .= " (SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     NAME1 || '-' || NAME2  AS VALUE, ";
            $query .= "     NAME1 || '-' || NAME2  || ':' AS LABEL ";
        } else {
            $query .= "     NAME1  AS VALUE, ";
            $query .= "     NAME1  || ':' AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='D044' ";
        if ($model->schkind != "" && $model->schkind != "99") {
            $query .= " AND NAME2 = '".$model->schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME2 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME2 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE) ";

        return $query;
    }
    
    //NAME_MSTに存在するかどうか
    function IsNameMst044($classcd, $model){
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D044' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND NAME1 = '" . $class_array[0] . "'";
            $query .= "     AND NAME2 = '" . $class_array[1] . "'";
        } else {
            $query .= "     AND NAME1 = '" . $classcd . "'";
        }
        
        return $query;
    }
    
    //科目取得
    function getSubClassMst($classcd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '：' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || '：' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM  ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD          = '".$class_array[0]."' ";
            $query .= "     AND SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) = '{$classcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //科目取得044
    function getSubClassMst044($classcd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     NAME1 || '-' || NAME2 || '-' || NAME3 || '-' || ABBV1 || '：' AS LABEL, ";
            $query .= "     NAME1 || '-' || NAME2 || '-' || NAME3 || '-' || ABBV1 AS VALUE ";
        } else {
            $query .= "     ABBV1 || '：' AS LABEL, ";
            $query .= "     ABBV1 AS VALUE ";
        }
        $query .= " FROM  ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1='D044' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND NAME1          = '".$class_array[0]."' ";
            $query .= "     AND NAME2      = '".$class_array[1]."' ";
        } else {
            $query .= "     AND SUBSTR(ABBV1,1,2) = '{$classcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校種別取得
    function getSchoolKind($model, $leftCmb="")
    {
        $query  = "";
        if ($leftCmb != "") {
            $query .= " WITH T_ALL (VALUE, LABEL, SORT) AS ( ";
            $query .= "      VALUES('99', '--全て--', '00') ";
            $query .= "      ) ";
            $query .= " SELECT ";
            $query .= "      VALUE, ";
            $query .= "      LABEL, ";
            $query .= "      SORT ";
            $query .= " FROM ";
            $query .= "      T_ALL ";
            $query .= " UNION ALL  ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHOOL_KIND AS VALUE, ";
        $query .= "     T1.SCHOOL_KIND || ':' || VALUE(L1.ABBV1, '') AS LABEL, ";
        $query .= "     T1.SCHOOL_KIND AS SORT ";
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A023' ";
        $query .= "          AND L1.NAME1 = T1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND IS NOT NULL ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        if ($leftCmb != "") {
            $query .= "     SORT ";
        } else {
            $query .= "     VALUE DESC ";
        }

        return $query;
    }

    //１レコード取得
    function getSumWeight($model)
    {
        $query  = " SELECT ";
        $query .= "     SUM(WEIGHT) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$model->classcd."' AND ";
            $query .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
            $query .= "     CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
            $query .= "     SUBCLASSCD      = '".$model->subclasscd."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->subclasscd."' AND ";
        }
        $query .= "     VIEWCD <> '".$model->field["VIEWCD"]."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields, $model)
    {
        $db = Query::dbCheckOut();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($fields["SUBCLASSCD"], 0, 2);
            $substr_curriculum_cd    = substr($fields["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($fields["SUBCLASSCD"], 7, 6);
        }
        
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]              = $substr_classcd;
            $data["CURRICULUM_CD"][TEXT]        = $substr_curriculum_cd;
            $data["SUBCLASSCD"][TEXT]           = $substr_subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]           = $fields["SUBCLASSCD"];
        }
        $data["VIEWCD"][TEXT]           = $fields["VIEWCD"];
        $data["VIEWNAME"][TEXT]         = $fields["VIEWNAME"];
        $data["VIEWABBV"][TEXT]         = $fields["VIEWABBV"];
        $data["SHOWORDER"][NUMBER]      = $fields["SHOWORDER"];
        $data["SCHOOL_KIND"][TEXT]      = $fields["SCHOOL_KIND"];
        $data["WEIGHT"][NUMBER]         = $fields["WEIGHT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "jviewname_sub_mst");
        $db->query($query);

        Query::dbCheckIn($db);

        return;
    }
    //UPDATE
    function &getUpdateQuery($fields, $model)
    {
        $db = Query::dbCheckOut();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($fields["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($fields["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($fields["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($fields["SUBCLASSCD"], 7, 6);
        }

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]              = $substr_classcd;
            $data["CURRICULUM_CD"][TEXT]        = $substr_curriculum_cd;
            $data["SUBCLASSCD"][TEXT]           = $substr_subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]           = $fields["SUBCLASSCD"];
        }
        $data["VIEWCD"][TEXT]           = $fields["VIEWCD"];
        $data["VIEWNAME"][TEXT]         = $fields["VIEWNAME"];
        $data["VIEWABBV"][TEXT]         = $fields["VIEWABBV"];
        $data["SHOWORDER"][NUMBER]      = $fields["SHOWORDER"];
        $data["SCHOOL_KIND"][TEXT]      = $fields["SCHOOL_KIND"];
        $data["WEIGHT"][NUMBER]         = $fields["WEIGHT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= "       CLASSCD       = '" .$substr_classcd."' AND ";
            $where .= "       SCHOOL_KIND   = '" .$substr_school_kind."' AND ";
            $where .= "       CURRICULUM_CD = '" .$substr_curriculum_cd."' AND ";
            $where .= "       SUBCLASSCD    = '" .$substr_subclasscd."' AND ";
        } else {
            $where .= "       SUBCLASSCD    = '" .$fields["SUBCLASSCD"]."' AND";
        }
        $where .= " VIEWCD      = '" .$fields["VIEWCD"]."' ";

        $query = Query::updateSQL($data, "jviewname_sub_mst", $where);

        $db->query($query);
        Query::dbCheckIn($db);

        return ;
    }
    //DELETE
    function &getDeleteQuery($fields, $model)
    {
        if (knjz402j_2Query::year_data_chk($fields["SUBCLASSCD"], $fields["VIEWCD"])==0) {
            $db = Query::dbCheckOut();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd       = substr($fields["SUBCLASSCD"], 0, 2);
            $substr_school_kind   = substr($fields["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd = substr($fields["SUBCLASSCD"], 5, 1);
            $substr_subclasscd    = substr($fields["SUBCLASSCD"], 7, 6);
        }
            $query  = "DELETE ";
            $query .= "FROM ";
            $query .= "     JVIEWNAME_SUB_MST ";
            $query .= "WHERE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       CLASSCD       = '".$substr_classcd."' AND ";
                $query .= "       SCHOOL_KIND   = '".$substr_school_kind."' AND ";
                $query .= "       CURRICULUM_CD = '".$substr_curriculum_cd."' AND ";
                $query .= "       SUBCLASSCD    = '".$substr_subclasscd."' AND ";
            } else {
                $query .= "       SUBCLASSCD = '".$fields["SUBCLASSCD"]."' AND ";
            }
            $query .= "       VIEWCD = '".$fields["VIEWCD"]."' ";
            
            $db->query($query);
            Query::dbCheckIn($db);

            $result = true;
        } else {
            $result = false;
        }
        return $result;
    }

    function year_data_chk($subclasscd, $viewcd)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd       = substr($subclasscd, 0, 2);
            $substr_school_kind   = substr($subclasscd, 3, 1);
            $substr_curriculum_cd = substr($subclasscd, 5, 1);
            $substr_subclasscd    = substr($subclasscd, 7, 6);
        }    
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SUBCLASSCD      = '".$substr_subclasscd."' ";
            $query .= "     AND CLASSCD         = '".$substr_classcd."' ";
            $query .= "     AND SCHOOL_KIND     = '".$substr_school_kind."' ";
            $query .= "     AND CURRICULUM_CD   = '".$substr_curriculum_cd."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= "     AND VIEWCD     = '" .$viewcd ."'" ;
        
        $db->query($query);

        $row = $db->getone($query,null,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
}
?>
