<?php

require_once('for_php7.php');

class knjz062_class_reflectionQuery extends Query {

    //教育委員会・学校種別コンボ
    function selectSchoolKind() {
        $query  = "";
        $query .= " WITH T_ALL (LABEL, VALUE, SORT) AS ( ";
        $query .= "     VALUES('-全て-', 'all', 'all') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE, ";
        $query .= "     SORT ";
        $query .= " FROM ";
        $query .= "     T_ALL ";
        $query .= " UNION ALL  ";
        $query .= " SELECT  ";
        $query .= "     NAME1 || ' ' || ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAMECD2 AS SORT ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1  = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";
        
        return $query;
    }

    //教科マスタ一覧取得
    function selectQuery($model)
    {
        $query  = "SELECT  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || ' ' || CLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
            $query .= "     SCHOOL_KIND || '-' || CLASSCD AS SORT ";
        } else {
            $query .= "     CLASSCD || ' ' || CLASSNAME AS LABEL, ";
            $query .= "     CLASSCD AS VALUE ";
            $query .= "     CLASSCD AS SORT ";
        }
        $query .= "FROM ";
        $query .= "     CLASS_MST ";
        if ($model->field["BOARD_SCHOOL_KIND"] !== 'all') {
            $query .= "WHERE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     SCHOOL_KIND = '".$model->field["BOARD_SCHOOL_KIND"]."' ";
            }
        }
        $query .= "ORDER BY ";
        $query .= "     SORT ";
        
        return $query;
    }

    //学校教科に存在するか
    function getClassCnt($model, $classcd) {
        $query  = "SELECT  ";
        $query .= "     * ";
        $query .= "FROM ";
        $query .= "     CLASS_MST ";
        $query .= "WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  = '".$classcd."' ";
        } else {
            $query .= "     CLASSCD  = '".$classcd."' ";
        }

        return $query;
    }

    //教科マスタ一覧取得
    function getDataQuery($model, $flg, $classcd, $school_kind)
    {
        $query  = " SELECT ";
        if (!$flg) {
            $query .= "      * ";
        } else {
            $query .= "      COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "      CLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      CLASSCD     = '" .$classcd ."'";
            $query .= " AND  SCHOOL_KIND = '" .$school_kind ."'";
        } else {
            $query .= "      CLASSCD = '" .$classcd ."'";
        }
        
        return $query;
    }

    //教育委員会用の学校コード取得
    function getSchoolMst() {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".CTRL_YEAR."' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //UPDATE
    function &getUpdateQuery(&$model)
    {
        //教科コード配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);

        if ($array[0] != ""){
            for ($i = 0; $i < get_count($array); $i++) {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $substr_classcd[$i]       = substr($array[$i], 0, 2);
                    $substr_school_kind[$i]   = substr($array[$i], 3, 1);
                }
                //学校教科マスタチェック
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query = knjz062_class_reflectionQuery::getDataQuery($model, "count", $substr_classcd[$i], $substr_school_kind[$i]);
                } else {
                    $query = knjz062_class_reflectionQuery::getDataQuery($model, "count", $array[$i], "");
                }
                $school_ClasscdCount = $db->getOne($query);
                
                if ($school_ClasscdCount == 0) {
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query = knjz062_class_reflectionQuery::getDataQuery($model, "", $substr_classcd[$i], $substr_school_kind[$i]);
                    } else {
                        $query = knjz062_class_reflectionQuery::getDataQuery($model, "", $array[$i], "");
                    }
                    $boardClassRow = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                    
                    $data = array();
                    $data["CLASSCD"][TEXT]          = $boardClassRow["CLASSCD"];
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $data["SCHOOL_KIND"][TEXT]      = $boardClassRow["SCHOOL_KIND"];
                    }
                    $data["CLASSNAME"][TEXT]        = $boardClassRow["CLASSNAME"];
                    $data["CLASSABBV"][TEXT]        = $boardClassRow["CLASSABBV"];
                    $data["CLASSNAME_ENG"][TEXT]    = $boardClassRow["CLASSNAME_ENG"];
                    $data["CLASSABBV_ENG"][TEXT]    = $boardClassRow["CLASSABBV_ENG"];
                    $data["CLASSORDERNAME1"][TEXT]  = $boardClassRow["CLASSORDERNAME1"];
                    $data["CLASSORDERNAME2"][TEXT]  = $boardClassRow["CLASSORDERNAME2"];
                    $data["CLASSORDERNAME3"][TEXT]  = $boardClassRow["CLASSORDERNAME3"];
                    $data["SUBCLASSES"][NUMBER]     = $boardClassRow["SUBCLASSES"];
                    $data["SHOWORDER"][NUMBER]      = $boardClassRow["SHOWORDER"];
                    $data["SHOWORDER2"][NUMBER]     = $boardClassRow["SHOWORDER2"];
                    $data["SHOWORDER3"][NUMBER]     = $boardClassRow["SHOWORDER3"];
                    $data["SHOWORDER4"][NUMBER]     = $boardClassRow["SHOWORDER4"];
                    $data["ELECTDIV"][TEXT]         = $boardClassRow["ELECTDIV"];
                    $data["SPECIALDIV"][TEXT]       = $boardClassRow["SPECIALDIV"];
                    
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";
                    $query = Query::insertSQL($data, "CLASS_MST");
                    $db->query($query);
                    
                    $data2 = array();
                    //学校別教育委員会科目外テーブルへのデータ追加
                    $data2["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                    $data2["CLASSCD"][TEXT]              = $boardClassRow["CLASSCD"];
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $data2["SCHOOL_KIND"][TEXT]      = $boardClassRow["SCHOOL_KIND"];
                    }
                    $data2["EDBOARD_FLG"][TEXT]          = "1"; //1:教育委員会科目
                    $data2["REGISTERCD"][TEXT]           = STAFFCD;
                    $data2["UPDATED"][NUMBER]            = "sysdate()";

                    $query = Query::insertSQL($data2, "EDBOARD_CLASS_DAT");
                    $db2->query($query);                    
                }
            }
        } 
        
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }
}
?>
