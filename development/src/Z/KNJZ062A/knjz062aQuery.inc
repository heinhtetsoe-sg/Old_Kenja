<?php

require_once('for_php7.php');

class knjz062aQuery extends Query {

    //教科マスタ一覧取得
    function selectQuery($model)
    {
        $query  = "SELECT  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || ' ' || CLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE ";
        } else {
            $query .= "     CLASSCD || ' ' || CLASSNAME AS LABEL, ";
            $query .= "     CLASSCD AS VALUE ";
        }
        $query .= "FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= "WHERE ";
        $query .= "     YEAR = '".$model->year."'";
        $query .= "ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CLASSCD ";
        } else {
            $query .= "     VALUE ";
        }
        return $query;
    }

    //教科マスタ年度データ一覧取得
    function selectYearQuery()
    {
        $query  = "SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= "FROM ";
        $query .= "     CLASS_YDAT ";
        $query .= "ORDER BY ";
        $query .= "     YEAR desc";
        
        return $query;
    }

    //教科一覧取得
    function selectClassQuery($classcd_array,$model)
    {
        $query  = "SELECT  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || ' ' || CLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE ";
        } else {
            $query .= "     CLASSCD || ' ' || CLASSNAME AS LABEL, ";
            $query .= "     CLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     CLASS_MST m";
        $query .= " WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM CLASS_YDAT y ";
        $query .= "                    WHERE y.YEAR = '".$model->year."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                      AND y.SCHOOL_KIND   = m.SCHOOL_KIND ";
        }
        $query .= "                      AND y.CLASSCD = m.CLASSCD) ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CLASSCD ";
        } else {
            $query .= "     VALUE ";
        }
        
        return $query;
    }

    //チェック対象のテーブル名を取得
    function getTargetTable($model, $allTables) {
        $query  = " select distinct ";
        $query .= "   TABLE_NAME ";
        $query .= " from ";
        $query .= "   sysibm.columns ";
        $query .= " where ";
        $query .= "   TABLE_NAME IN ('".implode("','", $allTables)."') ";

        return $query;
    }

    //対象科目が特定のテーブルに参照されているかをチェック
    function checkClsCd($model, $tableName, $classcdArray) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   ( ";
        $query .= "     SELECT ";
        $query .= "       CLASSCD, ";
        $query .= "       SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "       {$tableName} ";
        $query .= "     GROUP BY ";
        $query .= "       CLASSCD, ";
        $query .= "       SCHOOL_KIND ";
        $query .= "   ) ";
        $query .= " WHERE ";
        $query .= "   CLASSCD || '-' || SCHOOL_KIND IN ('".implode("','", $classcdArray)."') ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model)
    {
        //教科コード配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "      CLASS_YDAT ";
        $query .= " WHERE ";
        $query .= "      YEAR = '" .$model->year ."'";
        $db->query($query);

        if ($array[0] != ""){
            $query  = "INSERT ";
            $query .= "     INTO CLASS_YDAT ";
            $query .= "    ( YEAR, ";
            $query .= "      CLASSCD,";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "      SCHOOL_KIND,";
            }
            $query .= "      REGISTERCD, ";
            $query .= "      UPDATED  ) ";
            $query .= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $substr_classcd[$i]       = substr($array[$i], 0, 2);
                    $substr_school_kind[$i]   = substr($array[$i], 3, 1);
                }
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query.= ",'" .$substr_classcd[$i] ."' ";                   //科目コード
                    $query.= ",'" .$substr_school_kind[$i] ."' ";               //学校校種
                } else {
                    $query.= ",'" .$array[$i] ."'";                             //科目コード
                }
                $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
            }
            $db->query($query);
        } else {
            $model->year = "";
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }
}
?>
