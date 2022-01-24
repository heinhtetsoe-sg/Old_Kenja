<?php

require_once('for_php7.php');

class knjz072_subclass_reflectionQuery extends Query {

    //教育委員会・学校種別コンボ
    function selectSchoolKind($model) {
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

    //教育委員会・教育課程コンボ
    function selectCurriculum($model) {
        $query  = "";
        $query .= " WITH T_ALL (LABEL, VALUE, SORT) AS ( ";
        $query .= "     VALUES('-全て-', 'all', 'all') ";
        $query .= "     ) ";
        $query .= "SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE, ";
        $query .= "     SORT ";
        $query .= "FROM ";
        $query .= "     T_ALL ";
        $query .= "UNION ALL  ";
        $query .= "SELECT  ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 AS SORT ";
        $query .= "FROM ";
        $query .= "     NAME_MST ";
        $query .= "WHERE ";
        $query .= "     NAMECD1  = 'Z018' ";
        $query .= "ORDER BY ";
        $query .= "     SORT ";
        
        return $query;
    }

    //教育委員会科目一覧取得
    function selectSubclassQuery($model) {
        $query  = "SELECT  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= "FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= "WHERE ";
        $query .= "     SUBCLASSCD IS NOT NULL ";
        if ($model->field["BOARD_SCHOOL_KIND"] !== 'all' && $model->field["BOARD_SCHOOL_KIND"] !== '') {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND SCHOOL_KIND  = '".$model->field["BOARD_SCHOOL_KIND"]."' ";
            }
        }
        if ($model->field["BOARD_CURRICULUM_CD"] !== 'all' && $model->field["BOARD_CURRICULUM_CD"] !== '') {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CURRICULUM_CD  = '".$model->field["BOARD_CURRICULUM_CD"]."' ";
            }
        }
        $query .= "ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     CLASSCD, ";
            $query .= "     SUBCLASSCD   ";
        } else {
            $query .= "     VALUE ";
        }
        
        return $query;
    }

    //学校科目に存在するか
    function getSubclassCnt($model, $subclasscd) {
        $query  = "SELECT  ";
        $query .= "     * ";
        $query .= "FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= "WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "     SUBCLASSCD  = '".$subclasscd."' ";
        }

        return $query;
    }

    //学校科目に存在するか
    function getClassCnt($model, $classcd) {
        $query  = "SELECT  ";
        $query .= "     * ";
        $query .= "FROM ";
        $query .= "     CLASS_MST ";
        $query .= "WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND = '".$classcd."' ";
        } else {
            $query .= "     CLASSCD  = '".$classcd."' ";
        }

        return $query;
    }

    function getEdboardSubclassDatCount($model, $row) {
       $query  = " SELECT COUNT(*) ";
       $query .= " FROM EDBOARD_SUBCLASS_DAT ";
       $query .= " WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
       if ($model->Properties["useCurriculumcd"] == '1') {
           $query .= " AND CLASSCD = '".$row["CLASSCD"]."' ";
           $query .= " AND SCHOOL_KIND = '".$row["SCHOOL_KIND"]."' ";
           $query .= " AND CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
       }
       $query .= " AND SUBCLASSCD = '".$row["SUBCLASSCD"]."' ";
       return $query; 
    }

    function delEdboardSubclassDat($model, $row) {
       $query  = " DELETE ";
       $query .= " FROM EDBOARD_SUBCLASS_DAT ";
       $query .= " WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
       if ($model->Properties["useCurriculumcd"] == '1') {
           $query .= " AND CLASSCD = '".$row["CLASSCD"]."' ";
           $query .= " AND SCHOOL_KIND = '".$row["SCHOOL_KIND"]."' ";
           $query .= " AND CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
       }
       $query .= " AND SUBCLASSCD = '".$row["SUBCLASSCD"]."' ";
       return $query; 
    }

    function getClassAnotherDatCount($model, $row) {
       $query  = " SELECT COUNT(*) ";
       $query .= " FROM CLASS_ANOTHER_DAT ";
       $query .= " WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
       if ($model->Properties["useCurriculumcd"] == '1') {
           $query .= " AND SCHOOL_KIND = '".$row["SCHOOL_KIND"]."' ";
       }
       $query .= " AND CLASSCD = '".$row["CLASSCD"]."' ";
       return $query; 
    }

    function delClassAnotherDat($model, $row) {
       $query  = " DELETE ";
       $query .= " FROM CLASS_ANOTHER_DAT ";
       $query .= " WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
       if ($model->Properties["useCurriculumcd"] == '1') {
           $query .= " AND SCHOOL_KIND = '".$row["SCHOOL_KIND"]."' ";
       }
       $query .= " AND CLASSCD = '".$row["CLASSCD"]."' ";
       return $query; 
    }

    //INSERTのみ
    function &getUpdateQuery($model) {
        //科目コード配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);

        if ($array[0] !="") {
            for ($i = 0; $i < get_count($array); $i++) {
                //学校DB.SUBCLASS_MSTチェック
                $query = knjz072_subclass_reflectionQuery::getSubclassCnt($model, $array[$i]);
                $cnt = get_count($db->getCol($query));
                //教育委員会DB.SUBCLASS_MSTから学校DB.SUBCLASS_MSTに登録
                if ($cnt == 0) {
                    //取得
                    $query = knjz072_subclass_reflectionQuery::getSubclassCnt($model, $array[$i]);
                    $boardSubClassRow = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                    //セット
                    $data = array();
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $data["CLASSCD"][TEXT]          = $boardSubClassRow["CLASSCD"];
                        $data["SCHOOL_KIND"][TEXT]      = $boardSubClassRow["SCHOOL_KIND"];
                        $data["CURRICULUM_CD"][TEXT]    = $boardSubClassRow["CURRICULUM_CD"];
                    }
                    $data["SUBCLASSCD"][TEXT]           = $boardSubClassRow["SUBCLASSCD"];
                    $data["SUBCLASSNAME"][TEXT]         = $boardSubClassRow["SUBCLASSNAME"];
                    $data["SUBCLASSABBV"][TEXT]         = $boardSubClassRow["SUBCLASSABBV"];
                    $data["SUBCLASSNAME_ENG"][TEXT]     = $boardSubClassRow["SUBCLASSNAME_ENG"];
                    $data["SUBCLASSABBV_ENG"][TEXT]     = $boardSubClassRow["SUBCLASSABBV_ENG"];
                    $data["SUBCLASSORDERNAME1"][TEXT]   = $boardSubClassRow["SUBCLASSORDERNAME1"];
                    $data["SUBCLASSORDERNAME2"][TEXT]   = $boardSubClassRow["SUBCLASSORDERNAME2"];
                    $data["SUBCLASSORDERNAME3"][TEXT]   = $boardSubClassRow["SUBCLASSORDERNAME3"];
                    $data["SHOWORDER"][NUMBER]          = $boardSubClassRow["SHOWORDER"];
                    $data["SHOWORDER2"][NUMBER]         = $boardSubClassRow["SHOWORDER2"];
                    $data["SHOWORDER3"][NUMBER]         = $boardSubClassRow["SHOWORDER3"];
                    $data["SUBCLASSCD2"][TEXT]          = $boardSubClassRow["SUBCLASSCD2"];
                    $data["SUBCLASSCD3"][TEXT]          = $boardSubClassRow["SUBCLASSCD3"];
                    $data["ELECTDIV"][TEXT]             = $boardSubClassRow["ELECTDIV"];

                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";
                    $query = Query::insertSQL($data, "SUBCLASS_MST");
                    $db->query($query);

                    if ($db2->getOne(knjz072_subclass_reflectionQuery::getEdboardSubclassDatCount($model, $boardSubClassRow)) > 0) {
                        $db2->query(knjz072_subclass_reflectionQuery::delEdboardSubclassDat($model, $boardSubClassRow));
                    }
                    $data2 = array();
                    //学校別教育委員会科目外テーブルへのデータ追加
                    $data2["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $data2["CLASSCD"][TEXT]          = $boardSubClassRow["CLASSCD"];
                        $data2["SCHOOL_KIND"][TEXT]      = $boardSubClassRow["SCHOOL_KIND"];
                        $data2["CURRICULUM_CD"][TEXT]    = $boardSubClassRow["CURRICULUM_CD"];
                    }
                    $data2["SUBCLASSCD"][TEXT]           = $boardSubClassRow["SUBCLASSCD"];
                    $data2["EDBOARD_FLG"][TEXT]          = "1"; //1:教育委員会科目
                    $data2["REGISTERCD"][TEXT]           = STAFFCD;
                    $data2["UPDATED"][NUMBER]            = "sysdate()";

                    $query = Query::insertSQL($data2, "EDBOARD_SUBCLASS_DAT");
                    $db2->query($query);
                }
                /**********************************/
                //学校DB.CLASS_MSTチェック
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query = knjz072_subclass_reflectionQuery::getClassCnt($model, substr($array[$i], 0, 4));
                } else {
                    $query = knjz072_subclass_reflectionQuery::getClassCnt($model, substr($array[$i], 0, 2));
                }
                $cnt = get_count($db->getCol($query));
                //教育委員会DB.CLASS_MSTから学校DB.CLASS_MSTに登録
                if ($cnt == 0) {
                    //取得
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query = knjz072_subclass_reflectionQuery::getClassCnt($model, substr($array[$i], 0, 4));
                    } else {
                        $query = knjz072_subclass_reflectionQuery::getClassCnt($model, substr($array[$i], 0, 2));
                    }
                    $boardClassRow = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                    //セット
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

                    if ($db2->getOne(knjz072_subclass_reflectionQuery::getClassAnotherDatCount($model, $boardSubClassRow)) > 0) {
                        $db2->query(knjz072_subclass_reflectionQuery::delClassAnotherDat($model, $boardSubClassRow));
                    }
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

                    $query = Query::insertSQL($data2, "CLASS_ANOTHER_DAT");
                    $db2->query($query);
                }
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
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
}
?>
