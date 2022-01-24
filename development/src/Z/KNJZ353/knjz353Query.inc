<?php

require_once('for_php7.php');

class knjz353Query extends Query {

    //対象プログラム一覧取得
    function getProgramIdList($model) {
        $query  = "  SELECT ";
        $query .= "    T1.PROGRAMID AS VALUE ";
        $query .= "    , T1.PROGRAMID || '：' || T2.MENUNAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "    ( ";
        $query .= "      SELECT ";
        $query .= "        SCHOOLCD ";
        $query .= "        , SCHOOL_KIND ";
        $query .= "        , PROGRAMID ";
        $query .= "        , MIN(MENUID) AS MENUID ";
        $query .= "      FROM ";
        $query .= "        MENU_MST ";
        $query .= "      WHERE ";
        $query .= "        PROGRAMID IN ('KNJF030', 'KNJF030H') ";
        $query .= "        AND MENUNAME IS NOT NULL ";
        $query .= "      GROUP BY ";
        $query .= "        SCHOOLCD ";
        $query .= "        , SCHOOL_KIND ";
        $query .= "        , PROGRAMID ";
        $query .= "    ) T1 ";
        $query .= "    , MENU_MST T2 ";
        $query .= "  WHERE ";
        $query .= "    T1.MENUID = T2.MENUID ";
        $query .= "    AND T1.PROGRAMID IN ( ";
        $query .= "      SELECT ";
        $query .= "        NAME1 ";
        $query .= "      FROM ";
        $query .= "        NAME_MST ";
        $query .= "      WHERE ";
        $query .= "        NAMECD1 = 'F013' ";
        $query .= "    ) ";
        $query .= "    AND T1.SCHOOLCD = T2.SCHOOLCD ";
        $query .= "    AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "    AND T1.SCHOOLCD = '000000000000' ";
        $query .= "    AND T1.SCHOOL_KIND = 'H' ";
        $query .= "  ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }


    //出欠表示項目取得
    function getAdminFieldList($model, $medexam_div) {
        $query  = " SELECT ";
        $query .= "     MEDEXAM_ITEM ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_MEDEXAM_PRINT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MEDEXAM_DIV  = '".$medexam_div."' AND ";
        $query .= "     PROGRAMID   = '".$model->field["PRG_ID"]."' ";
        $query .= " ORDER BY ";
        $query .= "     MEDEXAM_ITEM ";

        return $query;
    }

    function getExists($year) {
        $db = Query::dbCheckOut();
        $query .= "  SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "  FROM ";
        $query .= "    ADMIN_CONTROL_MEDEXAM_PRINT_DAT ";
        $query .= "  WHERE ";
        $query .= "    YEAR = '".$year."' ";
        $ret_val = $db->getOne($query);
        Query::dbCheckOut($db);

        return $ret_val;
    }

    //UPDATE
    function &getUpdateQuery(&$model, $selectdata) {
        //配列に格納
        $array = explode(",", $selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $medexam_div = $model->prgid[$model->field["PRG_ID"]];

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ADMIN_CONTROL_MEDEXAM_PRINT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MEDEXAM_DIV  = '".$medexam_div."' AND ";
        $query .= "     PROGRAMID   = '".$model->field["PRG_ID"]."' ";

        $db->query($query);

        //追加
        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {
                list ($no, $medexam_item) = explode('-', $array[$i]);

                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["MEDEXAM_DIV"][TEXT]   = $medexam_div;
                $data["PROGRAMID"][TEXT]    = $model->field["PRG_ID"];
                $data["MEDEXAM_ITEM"][TEXT]  = $medexam_item;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $db->query(Query::insertSQL($data, "ADMIN_CONTROL_MEDEXAM_PRINT_DAT"));
            }
        }

        $db->autoCommit(true);
        Query::dbCheckIn($db);
        return;
    }

    //前年度からのコピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $this_year  = CTRL_YEAR;
        $pre_year   = CTRL_YEAR - 1;

        $query  = " INSERT INTO ADMIN_CONTROL_MEDEXAM_PRINT_DAT ";
        $query .= " SELECT ";
        $query .= "     '".$this_year."', ";
        $query .= "     T1.MEDEXAM_DIV, ";
        $query .= "     T1.PROGRAMID, ";
        $query .= "     T1.MEDEXAM_ITEM, ";
        $query .= "     CAST(NULL AS VARCHAR(2)), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_MEDEXAM_PRINT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".$pre_year."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.MEDEXAM_DIV, ";
        $query .= "     T1.PROGRAMID ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
   }
}
?>
