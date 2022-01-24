<?php

require_once('for_php7.php');

class knjz072aQuery extends Query
{
    //年度科目一覧取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."'";
        $query .= " ORDER BY ";
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
    
    //年度一覧取得
    public function selectYearQuery()
    {
        $query  = " SELECT ";
        $query .= "     SMALLINT(YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_YDAT ";
        $query .= " GROUP BY  ";
        $query .= "     YEAR ";
        $query .= " ORDER BY  ";
        $query .= "     YEAR DESC";
        
        return $query;
    }
    
    //科目一覧取得
    public function selectSubclassQuery($subclasscd_array, $model)
    {
        $query  = " SELECT  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST m ";
        $query .= " WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                   FROM SUBCLASS_YDAT y ";
        $query .= "                   WHERE y.year = '".$model->year."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                      AND y.CLASSCD       = m.CLASSCD ";
            $query .= "                      AND y.SCHOOL_KIND   = m.SCHOOL_KIND ";
            $query .= "                      AND y.CURRICULUM_CD = m.CURRICULUM_CD ";
        }
        $query .= "                    AND y.SUBCLASSCD = m.SUBCLASSCD) ";
        $query .= " ORDER BY ";
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
    
    public function getFieldQuery($table, $year)
    {
        $query  = " SELECT * FROM $table WHERE YEAR = '".$year."' ";
        return $query;
    }

    public function getInsertQuery($year, $year_add, $table, $field)
    {
        $query  = " INSERT INTO $table ";
        $query .= " SELECT ";
        $query .= "     $field ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        return $query;
    }

    //チェック対象のテーブル名を取得
    public function getTargetTable($model, $allTables)
    {
        $query  = " select distinct ";
        $query .= "   TABLE_NAME ";
        $query .= " from ";
        $query .= "   sysibm.columns ";
        $query .= " where ";
        $query .= "   TABLE_NAME IN ('".implode("','", $allTables)."') ";

        return $query;
    }

    //対象科目が特定のテーブルに参照されているかをチェック
    public function checkSubclass($model, $tableName, $subclasscdArray)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "    ( ";
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "       SUBCLASSCD AS SUBCLASSCD ";
        }
        $query .= "     FROM ";
        $query .= "       {$tableName} ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     GROUP BY ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD ";
        $query .= "   ) ";
        $query .= " WHERE ";
        $query .= "     SUBCLASSCD IN ('".implode("','", $subclasscdArray)."') ";

        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $all_light = false;
        //科目コード配列に格納
        $array = explode(",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $isYear = false;
        $query = knjz072aQuery::selectYearQuery();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row['YEAR'] == $model->year) {
                $isYear = true;
                break;
            }
        }
        $hyoujiClassDetailDat = false;
        if ($model->Properties["useCurriculumcd"] == 1 && $model->Properties["useClassDetailDat"] == 1 && $model->Properties["hyoujiClassDetailDat"] == 1) {
            $hyoujiClassDetailDat = true;
        }

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "      SUBCLASS_YDAT ";
        $query .= " WHERE ";
        $query .= "      YEAR = '" .$model->year ."'";

        $db->query($query);

        //科目年度データの登録
        if ($array[0] != "") {
            $query  = " INSERT ";
            $query .= "     INTO SUBCLASS_YDAT ";
            $query .= "    ( YEAR, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "      CLASSCD,";
                $query .= "      SCHOOL_KIND,";
                $query .= "      CURRICULUM_CD,";
            }
            $query .= "      SUBCLASSCD,";
            $query .= "      REGISTERCD, ";
            $query .= "      UPDATED  ) ";

            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++) {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $substr_classcd[$i]       = substr($array[$i], 0, 2);
                    $substr_school_kind[$i]   = substr($array[$i], 3, 1);
                    $substr_curriculum_cd[$i] = substr($array[$i], 5, 1);
                    $substr_subclasscd[$i]    = substr($array[$i], 7, 6);
                }
                $sp = ",";
                if ($i == get_count($array)-1) {
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query.= ",'" .$substr_classcd[$i] ."' ";                   //教科コード
                    $query.= ",'" .$substr_school_kind[$i] ."' ";               //学校校種
                    $query.= ",'" .$substr_curriculum_cd[$i] ."' ";             //教育課程コード
                    $query.= ",'" .$substr_subclasscd[$i] ."' ";                //科目コード
                } else {
                    $query.= ",'" .$array[$i] ."'";                             //科目コード
                }
                $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
            }
            $db->query($query);
        } else {
            $all_light = true;
        }
        
        if (!$isYear && $hyoujiClassDetailDat) {
            $query  = " DELETE ";
            $query .= " FROM ";
            $query .= "      SUBCLASS_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "      YEAR = '" .$model->year ."'";
            $db->query($query);

            $query  = knjz072aQuery::getFieldQuery('SUBCLASS_DETAIL_DAT', $model->year - 1);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (isset($Row)) {
                $field = "";
                $sep = "";
                foreach ($Row as $key => $val) {
                    if ($key == "YEAR") {
                        $key = "'$model->year'";
                    } elseif ($key == "REGISTERCD") {
                        $key = "'".STAFFCD."'";
                    } elseif ($key == "UPDATED") {
                        $key = "SYSDATE()";
                    }
                    $field .= $sep.$key;
                    $sep = ",";
                }
                $query = knjz072aQuery::getInsertQuery($model->year - 1, $model->year, 'SUBCLASS_DETAIL_DAT', $field);
                $db->query($query);
            }
        }
        
        $db->commit(); //トランザクションをコミットする。

        Query::dbCheckIn($db);
        return $all_light;
    }
}
