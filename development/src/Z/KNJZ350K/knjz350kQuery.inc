<?php

require_once('for_php7.php');

class knjz350kQuery extends Query {

    //(左)入力可テキストボックス表示データ取得
    function selectLeftQuery($year, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
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
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
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
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "    dat.control_flg='2' ";
        $query .= "ORDER BY ";
        $query .= "    control_flg, namespare1, control_code ";

        //echo $query;
        return $query;
    }

    //(右)入力不可テキストボックス表示データ取得
    function selectRightQuery($year, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = "SELECT DISTINCT name.namecd1, name.namecd2, name.name1 ";
#        $query .= "  FROM name_mst name ";     2004/09/03 arakaki 近大-作業依頼書20040903-03.doc
        $query .= "  FROM V_NAME_MST name ";
        $query .= " WHERE YEAR = '$year' ";
        $query .= "   AND namecd1='Z004' ";
        $query .= "   AND NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM ";
        $query .= "                         admin_control_dat dat ";
        $query .= "                    WHERE ";
        $query .= "                         dat.year = '$year'  AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                         dat.control_flg='1' AND ";
        $query .= "                         dat.control_code=name.namecd2 ) ";
        $query .= "UNION ";
        $query .= "SELECT DISTINCT name.namecd1, name.namecd2, name.name1 ";
#        $query .= "  FROM name_mst name ";     2004/09/03 arakaki 近大-作業依頼書20040903-03.doc
        $query .= "  FROM V_NAME_MST name ";
        $query .= " WHERE YEAR = '$year' ";
        $query .= "   AND namecd1='{$setNameCd}' ";
        $query .= "   AND NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM ";
        $query .= "                         admin_control_dat dat ";
        $query .= "                    WHERE ";
        $query .= "                         dat.year = '$year'  AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                         dat.control_flg='2' AND ";
        $query .= "                         dat.control_code=name.namecd2 ) ";
        return $query;
    }

    function selectProficiencyQuery($year) {
        $query  = " SELECT ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD, ";
        $query .= "     T1.PROFICIENCYNAME1, ";
        $query .= "     CASE WHEN L1.PROFICIENCYDIV IS NOT NULL ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS ADMIN_FLG ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST T1 ";
        $query .= "     LEFT JOIN ADMIN_CONTROL_PROFICIENCY_DAT L1 ON L1.YEAR = '{$year}' ";
        $query .= "          AND T1.PROFICIENCYDIV = L1.PROFICIENCYDIV ";
        $query .= "          AND T1.PROFICIENCYCD = L1.PROFICIENCYCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD ";
        return $query;
    }

    //名称マスタより取得
    function getNameMst($namecd1, $namecd2) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model)
    {

        //所属コード配列に格納
        $grade  = explode (",", $model->selectdata);
        $attend = explode (",", $model->selectdata2);
        $proficiency = explode (",", $model->selectdata3);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE FROM admin_control_dat WHERE year = '" .$model->year ."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        if ($grade[0] != "") {
            for ($i = 0; $i < get_count($grade); $i++){
                if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query  = "INSERT INTO admin_control_dat(year, SCHOOL_KIND, control_flg, control_code, registercd, updated) ";
                    $query .= "VALUES('$model->year', '".SCHOOLKIND."', '1', '$grade[$i]', '" . STAFFCD . "', '$updated')";
                } else {
                    $query  = "INSERT INTO admin_control_dat(year, control_flg, control_code, registercd, updated) ";
                    $query .= "VALUES('$model->year', '1', '$grade[$i]', '" . STAFFCD . "', '$updated')";
                }
                $db->query($query);
            }
        }
        if ($attend[0] != "") {
            for ($i = 0; $i < get_count($attend); $i++){
                if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query  = "INSERT INTO admin_control_dat(year, SCHOOL_KIND, control_flg, control_code, registercd, updated) ";
                    $query .= "VALUES('$model->year', '".SCHOOLKIND."', '2', '$attend[$i]', '" . STAFFCD . "', '$updated')";
                } else {
                    $query  = "INSERT INTO admin_control_dat(year, control_flg, control_code, registercd, updated) ";
                    $query .= "VALUES('$model->year', '2', '$attend[$i]', '" . STAFFCD . "', '$updated')";
                }
                $db->query($query);
            }
        }
        $db->commit(); // トランザクションをコミットする。

        //実力テスト
        if ($model->Properties["useProficiency"] == "1") {
            $proficiency = explode (",", $model->selectdata3);
            $updated = date("Y-m-d H:i:s", time()); //更新日付
            $query = "DELETE FROM ADMIN_CONTROL_PROFICIENCY_DAT WHERE YEAR = '" .$model->year ."'";
            $db->query($query);

            if ($proficiency[0] != "") {
                for ($i = 0; $i < get_count($proficiency); $i++){
                    list($div, $cd) = preg_split("/:/", $proficiency[$i]);
                    $query  = "INSERT INTO ADMIN_CONTROL_PROFICIENCY_DAT (YEAR, PROFICIENCYDIV, PROFICIENCYCD, REGISTERCD, UPDATED) ";
                    $query .= "VALUES('$model->year', '{$div}', '{$cd}', '" . STAFFCD . "', '$updated')";
                    $db->query($query);
                }
            }
            $db->commit(); // トランザクションをコミットする。
        }

        //学年成績からの評定換算先
        if ($model->Properties["useProvFlg"] == "1" && $model->isKumamoto == true) {
            //存在チェック
            $d073 = $db->getRow(knjz350kQuery::getNameMst("D073", "01"), DB_FETCHMODE_ASSOC);

            //更新
            $data = array();
            $data["NAMESPARE1"][TEXT]   = ($model->kansansaki == "2") ? "1" : NULL;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            if (strlen($d073["NAMECD1"])) {
                $where  = " WHERE ";
                $where .= "     NAMECD1 = 'D073' AND ";
                $where .= "     NAMECD2 = '01' ";

                $db->query(Query::updateSQL($data, "NAME_MST", $where));
            } else {
                $data["NAMECD1"][TEXT]      = 'D073';
                $data["NAMECD2"][TEXT]      = '01';

                $db->query(Query::insertSQL($data, "NAME_MST"));
            }

            //クリア
            unset($model->kansansaki);
        }

        $db->autoCommit(true);
        Query::dbCheckIn($db);
        return;
    }
}
?>
