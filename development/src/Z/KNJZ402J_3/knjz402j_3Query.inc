<?php

require_once('for_php7.php');

class knjz402j_3Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = "";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WITH T_ALL (LABEL, VALUE, SORT) AS ( ";
            $query .= "      VALUES('--全て--', '99', '00') ";
            $query .= "      ) ";
            $query .= " SELECT ";
            $query .= "      LABEL, ";
            $query .= "      VALUE, ";
            $query .= "      SORT ";
            $query .= " FROM ";
            $query .= "      T_ALL ";
            $query .= " UNION ALL  ";
        }
        $query .= " SELECT ";
        $query .= "     NAME1 || '：' || ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME1 AS SORT ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }

    //科目名取得
    function getSubclassName($model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD || ':' || VALUE(T1.SUBCLASSNAME,'') ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD         = '".$model->classcd."' AND ";
            $query .= "     T1.SCHOOL_KIND     = '".$model->school_kind."' AND ";
            $query .= "     T1.CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        }
        $query .= "     T1.SUBCLASSCD = '".$model->subclasscd."' ";
        return $query;
    }
    
    //重みチェック
    function getCountWeightData($model, $row)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_MST T1 ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD         = '".$row["CLASSCD"]."' AND ";
            $query .= "     T1.SCHOOL_KIND     = '".$row["SCHOOL_KIND"]."' AND ";
            $query .= "     T1.CURRICULUM_CD   = '".$row["CURRICULUM_CD"]."' AND ";
        }
        $query .= "     T1.SUBCLASSCD = '".$row["SUBCLASSCD"]."' AND ";
        $query .= "     T1.WEIGHT IS NOT NULL ";

        return $query;
    }

    //１レコード取得
    function getRow($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.WEIGHT, ";
        $query .= "     T1.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_MST T1 ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD         = '".$model->classcd."' AND ";
            $query .= "     T1.SCHOOL_KIND     = '".$model->school_kind."' AND ";
            $query .= "     T1.CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        }
        $query .= "     T1.SUBCLASSCD = '".$model->subclasscd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.VIEWCD ";
        return $query;
    }

    //観点一覧
    function getJViewNameList($model)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L2.SUBCLASSNAME, ";
        $query .= "     L3.ABBV1 ";
        $query .= " FROM  ";
        $query .= "     JVIEWNAME_SUB_MST T1 ";
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
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L2.SUBCLASSNAME, ";
        $query .= "     L3.ABBV1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND DESC, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["VIEWCD"]); $i++) {
            $data = array();
            $data["WEIGHT"][NUMBER]         = $model->fields["WEIGHT"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where  = " WHERE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "       CLASSCD       = '" .$model->classcd."' AND ";
                $where .= "       SCHOOL_KIND   = '" .$model->school_kind."' AND ";
                $where .= "       CURRICULUM_CD = '" .$model->curriculum_cd."' AND ";
                $where .= "       SUBCLASSCD    = '" .$model->subclasscd."' AND ";
            } else {
                $where .= "       SUBCLASSCD    = '" .$model->subclasscd."' AND";
            }
            $where .= " VIEWCD      = '" .$model->data["VIEWCD"][$i]."' ";
            $query = Query::updateSQL($data, "jviewname_sub_mst", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
