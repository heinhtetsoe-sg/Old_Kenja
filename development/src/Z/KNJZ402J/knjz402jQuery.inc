<?php

require_once('for_php7.php');

class knjz402jQuery extends Query {

    //年度一覧取得
    function selectYearQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SMALLINT(YEAR) AS year ";
        $query .= " FROM ";
        $query .= "     jviewname_sub_ydat ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "  SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "GROUP BY year ";
        $query .= "ORDER BY year DESC ";
        return $query;
    }
    //年度観点一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SUBCLASSCD || '-' || T1.VIEWCD  AS VALUE, ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SUBCLASSCD, ";
        } else {
            $query .= "     T1.SUBCLASSCD || '-' || T1.VIEWCD  AS VALUE, ";
            $query .= "     T1.SUBCLASSCD, ";
        }
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWNAME, ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A023' ";
        $query .= "          AND T1.SCHOOL_KIND = L1.NAME1, ";
        $query .= "     JVIEWNAME_SUB_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".$model->year."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD = T2.CLASSCD ";
            $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "     AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     AND T1.VIEWCD = T2.VIEWCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND DESC, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }        
        $query .= "     VALUE ";

        return $query;
    }
    //観点一覧取得
    function selectSubclassQuery($subclasscd_array,$model)
    {
        $query  = " SELECT ";
        $query .= "     M.SUBCLASSCD || '-' || M.VIEWCD  AS VALUE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     M.CLASSCD, ";
            $query .= "     M.SCHOOL_KIND, ";
            $query .= "     M.CURRICULUM_CD, ";
        }        
        $query .= "     M.SUBCLASSCD, ";
        $query .= "     M.VIEWCD, ";
        $query .= "     M.VIEWNAME, ";
        $query .= "     M.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_MST M ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A023' ";
        $query .= "          AND M.SCHOOL_KIND = L1.NAME1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     JVIEWNAME_SUB_YDAT Y ";
        $query .= "                 WHERE ";
        $query .= "                     Y.YEAR = '".$model->year."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                  AND Y.CLASSCD = M.CLASSCD ";
            $query .= "                  AND Y.SCHOOL_KIND = M.SCHOOL_KIND ";
            $query .= "                  AND Y.CURRICULUM_CD = M.CURRICULUM_CD ";
        }
        $query .= "                     AND Y.SUBCLASSCD = M.SUBCLASSCD ";
        $query .= "                     AND Y.VIEWCD = M.VIEWCD) ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND M.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND M.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     M.SCHOOL_KIND DESC, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     M.CLASSCD, ";
            $query .= "     M.SCHOOL_KIND, ";
            $query .= "     M.CURRICULUM_CD, ";
        }        
        $query .= "     VALUE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $all_light = false;
        //観点コード配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM JVIEWNAME_SUB_YDAT WHERE YEAR = '" .$model->year ."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        //観点年度データの登録
        if ($array[0] !="") {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query = "INSERT INTO JVIEWNAME_SUB_YDAT(YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD, REGISTERCD, UPDATED)";
            } else {
                $query = "INSERT INTO JVIEWNAME_SUB_YDAT(YEAR, SUBCLASSCD, VIEWCD, REGISTERCD, UPDATED)";
            }
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $data = preg_split("/-/", $array[$i]);
                
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query.= "('" .$model->year ."'";                               //年度
                    $query.= ",'" .$data[0] ."'";                                   //教科コード
                    $query.= ",'" .$data[1] ."'";                                   //学校校種
                    $query.= ",'" .$data[2] ."'";                                   //教育課程コード
                    $query.= ",'" .$data[3] ."'";                                   //科目コード
                    $query.= ",'" .$data[4] ."'";                                   //観点コード
                    $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                    $query.= ",'" .$updated ."')" .$sp;                             //更新日付
                } else {
                    $query.= "('" .$model->year ."'";                               //年度
                    $query.= ",'" .$data[0] ."'";                                   //科目コード
                    $query.= ",'" .$data[1] ."'";                                   //観点コード
                    $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                    $query.= ",'" .$updated ."')" .$sp;                             //更新日付
                }

            }
            $db->query($query);
        } else {
            $all_light = true;
        }
        $db->commit(); //トランザクションをコミットする。

        Query::dbCheckIn($db);
        return $all_light;
    }
}
?>
