<?php

require_once('for_php7.php');


class knjm540Query extends Query {

    //科目（講座）リスト
    function ReadQuery($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT DISTINCT CHAIRCD,CHAIRNAME, ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "  CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD ";
            } else {
                $query .= "  SUBCLASSCD ";
            }
            $query .= "FROM CHAIR_DAT ";
            $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
            $query .= "AND CHAIRCD not like '92%' ";
            $query .= " ORDER BY CHAIRCD ";
        //参照・更新可（制限付き）
        }else {
            $query  = "SELECT DISTINCT CHAIRCD,CHAIRNAME, ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "  CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD ";
            } else {
                $query .= "  SUBCLASSCD ";
            }
            $query .= "FROM CHAIR_DAT ";
            $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
            $query .= "AND CHAIRCD not like '92%' ";
            $query .= "AND CHAIRCD in ";
            $query .= " (SELECT CHAIRCD ";
            $query .= "  FROM CHAIR_STF_DAT ";
            $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
            $query .= "AND STAFFCD ='" .STAFFCD ."')";
            $query .= " ORDER BY CHAIRCD ";
        }

        return $query;
    }

    //年度職員取得
    function selectQuery($model)
    {
        $query  = "SELECT STAFFCD,STAFFNAME ";
        $query .= "FROM V_STAFF_MST m ";
        $query .= "WHERE NOT EXISTS (SELECT 'X' ";
        $query .= "                  FROM REP_STF_DAT y ";
        $query .= "                  WHERE y.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                  AND y.CHAIRCD || y.CLASSCD || y.SCHOOL_KIND || y.CURRICULUM_CD || y.SUBCLASSCD = '".$model->sub."'";
        } else {
            $query .= "                  AND y.CHAIRCD || y.SUBCLASSCD = '".$model->sub."'";
        }
        $query .= "                  AND y.STAFFCD = m.STAFFCD) ";
        $query .= "AND m.YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY STAFFCD";
        return $query;
    }


    //科目添削担当職員取得
    function selectCorrectionQuery($model)
    {
        $query  = "SELECT STAFFCD,STAFFNAME ";
        $query .= "FROM V_STAFF_MST m ";
        $query .= "WHERE EXISTS (SELECT 'X' ";
        $query .= "                  FROM REP_STF_DAT y ";
        $query .= "                  WHERE y.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                  AND y.CHAIRCD || y.CLASSCD || y.SCHOOL_KIND || y.CURRICULUM_CD || y.SUBCLASSCD = '".$model->sub."'";
        } else {
            $query .= "                  AND y.CHAIRCD || y.SUBCLASSCD = '".$model->sub."'";
        }
        $query .= "                  AND y.STAFFCD = m.STAFFCD) ";
        $query .= "AND m.YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY STAFFCD";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $all_light = false;
        //科目コード配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
//        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM rep_stf_dat WHERE year = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " AND CHAIRCD || CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '" . $model->sub . "' ";
        } else {
            $query .= " AND CHAIRCD || SUBCLASSCD = '" . $model->sub . "' ";
        }
        $db->query($query);

        //科目年度データの登録
        if ($array[0] !="") {
            $query = "INSERT INTO rep_stf_dat(year, ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= " CLASSCD, ";
                $query .= " SCHOOL_KIND, ";
                $query .= " CURRICULUM_CD, ";
            }
            $query.= " subclasscd,chaircd,staffcd,registercd,updated) ";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('".CTRL_YEAR."' ";                                   //年度
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $query.= ",'" .substr($model->sub, 7, 2) ."'";              //教科コード
                    $query.= ",'" .substr($model->sub, 9, 1) ."'";              //校種
                    $query.= ",'" .substr($model->sub, 10, 1) ."'";             //教育課程
                    $query.= ",'" .substr($model->sub, 11) ."'";                //科目コード
                } else {
                    $query.= ",'" .substr($model->sub,7) ."'";                  //科目コード
                }
                $query.= ",'" .substr($model->sub,0,7) ."'";                    //講座コード
                $query.= ",'" .$array[$i] ."'";                                 //職員コード
                $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
            }
            $db->query($query);
        } else {
            $all_light = true;
        }
//        $db->commit(); //トランザクションをコミットする。
//echo $query;

        Query::dbCheckIn($db);
        return $all_light;
    }

    //年度追加時の講座チェック
    function getChairCheckQuery($model)
    {
        $query  = "SELECT DISTINCT CHAIRCD,CHAIRNAME,SUBCLASSCD ";
        $query .= "FROM CHAIR_DAT ";
        $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " AND CHAIRCD = '". substr($model->sub,0,7) ."'";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " AND CLASSCD = '". substr($model->sub, 7, 2) ."'";
            $query .= " AND SCHOOL_KIND = '". substr($model->sub, 9, 1) ."'";
            $query .= " AND CURRICULUM_CD = '". substr($model->sub, 10, 1) ."'";
            $query .= " AND SUBCLASSCD = '". substr($model->sub, 11) ."'";
        } else {
            $query .= " AND SUBCLASSCD = '". substr($model->sub,7) ."'";
        }

        return $query;

    }

    function getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "DELETE FROM REP_STF_DAT WHERE YEAR = '".CTRL_YEAR."' ";

        $db->query($query);

        $query  = " INSERT INTO REP_STF_DAT ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     REP_STF_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".(CTRL_YEAR - 1)."' ";
        $query .= "     AND EXISTS( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 V_STAFF_MST E1 ";
        $query .= "             WHERE ";
        $query .= "                 E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 AND E1.STAFFCD = T1.STAFFCD ";
        $query .= "     ) ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}

?>
