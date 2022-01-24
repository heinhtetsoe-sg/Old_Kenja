<?php

require_once('for_php7.php');

class knjz060Query extends Query
{

    //中学校年度一覧取得
    public function selectQuery($model)
    {
        $query  = " SELECT  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || ' ' || CLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE ";
        } else {
            $query .= "     CLASSCD || ' ' || CLASSNAME AS LABEL, ";
            $query .= "     CLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
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

    //中学校年度一覧取得
    public function selectYearQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     CLASS_YDAT ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR desc";
        
        return $query;
    }

    //中学校科目一覧取得
    public function selectClassQuery($model, $classcd_array)
    {
        $query  = " SELECT  ";
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
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND y.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND y.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "                      AND y.CLASSCD = m.CLASSCD) ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
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
    public function checkClsCd($model, $tableName, $classcdArray)
    {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   ( ";
        $query .= "     SELECT ";
        $query .= "       CLASSCD, ";
        $query .= "       SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "       {$tableName} ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     GROUP BY ";
        $query .= "       CLASSCD, ";
        $query .= "       SCHOOL_KIND ";
        $query .= "   ) ";
        $query .= " WHERE ";
        $query .= "   CLASSCD || '-' || SCHOOL_KIND IN ('".implode("','", $classcdArray)."') ";

        return $query;
    }

    //UPDATE
    public function &getUpdateQuery(&$model)
    {
        //中学校コード配列に格納
        $array = explode(",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "      CLASS_YDAT ";
        $query .= " WHERE ";
        $query .= "      YEAR = '" .$model->year ."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        if ($array[0] != "") {
            //成績項目別得点データの登録
            $query  = " INSERT ";
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
            for ($i = 0; $i < get_count($array); $i++) {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $substr_classcd[$i]       = substr($array[$i], 0, 2);
                    $substr_school_kind[$i]   = substr($array[$i], 3, 1);
                }
                $sp = ",";
                if ($i == get_count($array)-1) {
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
