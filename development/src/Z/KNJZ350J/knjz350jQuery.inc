<?php

require_once('for_php7.php');

class knjz350jQuery extends Query {

    //学校種別の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
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

    //(左)入力可テキストボックス表示データ取得
    function selectLeftQuery($year, $model)
    {
        $setNameCd = "Z005";
        if ($model->setSchoolKind != "") {
            $setNameCd = "Z".$model->setSchoolKind."05";
        }
        $setNameCd2 = "Z009";
        if ($model->setSchoolKind != "") {
            $setNameCd2 = "Z".$model->setSchoolKind."09";
        }
        $query  = "SELECT ";
        $query .= "    dat.control_flg AS control_flg, ";
        $query .= "    dat.control_code AS control_code, ";
        $query .= "    name.name1 AS name1, ";
        $query .= "    name.namecd2 AS namecd2, ";
        $query .= "    name.namespare1 ";
        $query .= "FROM ";
        $query .= "    admin_control_dat dat LEFT OUTER JOIN V_NAME_MST name ON dat.YEAR=name.YEAR AND name.namecd1='Z004' AND dat.control_code=name.namecd2 ";
        $query .= "WHERE ";
        $query .= "    dat.year = '$year'  AND ";
        if ($model->setSchoolKind != "") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        }
        $query .= "    dat.control_flg='1' ";
        $query .= "UNION ";
        $query .= "SELECT ";
        $query .= "    dat.control_flg AS control_flg, ";
        $query .= "    dat.control_code AS control_code, ";
        $query .= "    name.name1 AS name1, ";
        $query .= "    name.namecd2 AS namecd2, ";
        $query .= "    name.namespare1 ";
        $query .= "FROM ";
        $query .= "    admin_control_dat dat LEFT OUTER JOIN V_NAME_MST name ON dat.YEAR=name.YEAR AND name.namecd1='{$setNameCd}' AND dat.control_code=name.namecd2 ";
        $query .= "WHERE ";
        $query .= "    dat.year = '$year'  AND ";
        if ($model->setSchoolKind != "") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        }
        $query .= "    dat.control_flg='2' ";
        $query .= "UNION ";
        $query .= "SELECT ";
        $query .= "    dat.control_flg AS control_flg, ";
        $query .= "    dat.control_code AS control_code, ";
        $query .= "    name.namecd2 || ' ' || name.name1 AS name1, ";
        $query .= "    name.namecd2 AS namecd2, ";
        $query .= "    name.namespare1 ";
        $query .= "FROM ";
        $query .= "    admin_control_dat dat LEFT OUTER JOIN V_NAME_MST name ON dat.YEAR=name.YEAR AND name.namecd1='{$setNameCd2}' AND dat.control_code=name.namecd2 ";
        $query .= "WHERE ";
        $query .= "    dat.year = '$year'  AND ";
        if ($model->setSchoolKind != "") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        }
        $query .= "    dat.control_flg='3' ";
        $query .= "ORDER BY ";
        $query .= "    control_flg, namespare1, control_code ";

        return $query;
    }

    //(右)入力不可テキストボックス表示データ取得
    function selectRightQuery($year, $model)
    {
        $setNameCd = "Z005";
        if ($model->setSchoolKind != "") {
            $setNameCd = "Z".$model->setSchoolKind."05";
        }
        $setNameCd2 = "Z009";
        if ($model->setSchoolKind != "") {
            $setNameCd2 = "Z".$model->setSchoolKind."09";
        }
        $query  = " SELECT DISTINCT name.namecd1, name.namecd2, name.name1 ";
        $query .= " FROM V_NAME_MST name ";
        $query .= " WHERE YEAR = '$year' ";
        $query .= "   AND namecd1 = 'Z004' ";
        $query .= "   AND NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM ";
        $query .= "                         admin_control_dat dat ";
        $query .= "                    WHERE ";
        $query .= "                         dat.year = '$year'  AND ";
        if ($model->setSchoolKind != "") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        }
        $query .= "                         dat.control_flg = '1' AND ";
        $query .= "                         dat.control_code = name.namecd2 ) ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT name.namecd1, name.namecd2, name.name1 ";
        $query .= " FROM V_NAME_MST name ";
        $query .= " WHERE YEAR = '$year' ";
        $query .= "   AND namecd1 = '{$setNameCd}' ";
        $query .= "   AND NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM ";
        $query .= "                         admin_control_dat dat ";
        $query .= "                    WHERE ";
        $query .= "                         dat.year = '$year'  AND ";
        if ($model->setSchoolKind != "") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        }
        $query .= "                         dat.control_flg = '2' AND ";
        $query .= "                         dat.control_code = name.namecd2 ) ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     name.namecd1, ";
        $query .= "     name.namecd2, ";
        $query .= "     name.namecd2 || ' ' || name.name1 AS name1 ";
        $query .= " FROM V_NAME_MST name ";
        $query .= " WHERE YEAR = '$year' ";
        $query .= "   AND namecd1 = '{$setNameCd2}' ";
        $query .= "   AND NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM ";
        $query .= "                         admin_control_dat dat ";
        $query .= "                    WHERE ";
        $query .= "                         dat.year = '$year'  AND ";
        if ($model->setSchoolKind != "") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        }
        $query .= "                         dat.control_flg = '3' AND ";
        $query .= "                         dat.control_code = name.namecd2 ) ";
        $query .= " ORDER BY ";
        $query .= "     namecd1, ";
        $query .= "     namecd2 ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model)
    {

        //所属コード配列に格納
        $grade  = explode (",", $model->selectdata);
        $attend = explode (",", $model->selectdata2);
        $jview  = explode (",", $model->selectdata3);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE FROM admin_control_dat WHERE year = '" .$model->year ."'";
        if ($model->setSchoolKind != "") {
            $query .= "                     AND SCHOOL_KIND = '".$model->setSchoolKind."' ";
        }
        $db->query($query);

        if ($grade[0] != "") {
            for ($i = 0; $i < get_count($grade); $i++){
                if ($model->setSchoolKind != "") {
                    $query  = "INSERT INTO admin_control_dat(year, SCHOOL_KIND, control_flg, control_code, registercd, updated) ";
                    $query .= "VALUES('$model->year', '".$model->setSchoolKind."', '1', '$grade[$i]', '" . STAFFCD . "', '$updated')";
                } else {
                    $query  = "INSERT INTO admin_control_dat(year, control_flg, control_code, registercd, updated) ";
                    $query .= "VALUES('$model->year', '1', '$grade[$i]', '" . STAFFCD . "', '$updated')";
                }
                $db->query($query);
            }
        }
        if ($attend[0] != "") {
            for ($i = 0; $i < get_count($attend); $i++){
                if ($model->setSchoolKind != "") {
                    $query  = "INSERT INTO admin_control_dat(year, SCHOOL_KIND, control_flg, control_code, registercd, updated) ";
                    $query .= "VALUES('$model->year', '".$model->setSchoolKind."', '2', '$attend[$i]', '" . STAFFCD . "', '$updated')";
                } else {
                    $query  = "INSERT INTO admin_control_dat(year, control_flg, control_code, registercd, updated) ";
                    $query .= "VALUES('$model->year', '2', '$attend[$i]', '" . STAFFCD . "', '$updated')";
                }
                $db->query($query);
            }
        }
        if ($jview[0] != "") {
            for ($i = 0; $i < get_count($jview); $i++){
                if ($model->setSchoolKind != "") {
                    $query  = "INSERT INTO admin_control_dat(year, SCHOOL_KIND, control_flg, control_code, registercd, updated) ";
                    $query .= "VALUES('$model->year', '".$model->setSchoolKind."', '3', '$jview[$i]', '" . STAFFCD . "', '$updated')";
                } else {
                    $query  = "INSERT INTO admin_control_dat(year, control_flg, control_code, registercd, updated) ";
                    $query .= "VALUES('$model->year', '3', '$jview[$i]', '" . STAFFCD . "', '$updated')";
                }
                $db->query($query);
            }
        }
        $db->commit(); // トランザクションをコミットする。
        $db->autoCommit(true);
        Query::dbCheckIn($db);
        return;
    }
}
?>
