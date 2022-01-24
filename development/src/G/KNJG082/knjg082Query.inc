<?php

require_once('for_php7.php');

class knjg082Query extends Query {

    //コースグループリスト取得
    function getGroupList() {
        $query  = " SELECT ";
        $query .= "     T1.GROUP_DIV, ";
        $query .= "     T1.PRGID, ";
        $query .= "     T1.GROUP_DIV || ':' || L1.NAME1 AS SET_GROUP_DIV, ";
        $query .= "     T1.PRGID || '　' || L2.PRGNAME AS SET_PRGNAME ";
        $query .= " FROM ";
        $query .= "     LICENSE_GROUP_PRGID_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'D062' ";
        $query .= "                          AND L1.NAMECD2 = T1.GROUP_DIV ";
        $query .= "     LEFT JOIN LICENSE_GROUP_PRGID_MST L2 ON L2.PRGID = T1.PRGID ";
        $query .= " ORDER BY ";
        $query .= "     T1.GROUP_DIV, ";
        $query .= "     T1.PRGID ";

        return $query;
    }

    //グループカウント
    function getGroupCnt($groupDiv) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LICENSE_GROUP_PRGID_DAT ";
        $query .= " WHERE ";
        $query .= "     GROUP_DIV = '{$groupDiv}' ";

        return $query;
    }

    //グループコンボ
    function getNameMst() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D062' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
        
    //データ一覧取得 (右側)
    function ListQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.PRGID AS VALUE, ";
        $query .= "     T1.PRGID || '　' || T1.PRGNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LICENSE_GROUP_PRGID_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     LICENSE_GROUP_PRGID_DAT E1 ";
        $query .= "                 WHERE ";
        $query .= "                     E1.PRGID = T1.PRGID ";//登録済みのプログラムIDは表示しない
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //データ一覧取得 (左側)
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.PRGID AS VALUE, ";
        $query .= "     T1.PRGID || '　' || L1.PRGNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LICENSE_GROUP_PRGID_DAT T1 ";
        $query .= "     LEFT JOIN LICENSE_GROUP_PRGID_MST L1 ON L1.PRGID = T1.PRGID ";
        $query .= " WHERE ";
        $query .= "     T1.GROUP_DIV = '{$model->field["GROUP_DIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //COURSE_GROUP_CD_DAT追加
    function &insertGroupDatQuery(&$db, $selectdata, $model) {
        $data = array();
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }

            $db->query(knjg082Query::insertGroupDat($model, $selectdata[$i]));
        }
        return;
    }

    //LICENSE_GROUP_PRGID_DAT追加
    function insertGroupDat($model, $selectdata) {
        $data = array();
        $data["GROUP_DIV"][TEXT]    = $model->field["GROUP_DIV"];
        $data["PRGID"][TEXT]        = $selectdata;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "LICENSE_GROUP_PRGID_DAT");

        return $query;
    }

    //LICENSE_GROUP_PRGID_DAT削除
    function deleteGroupDat($model) {
        $query  = " DELETE FROM ";
        $query .= "     LICENSE_GROUP_PRGID_DAT ";
        $query .= " WHERE ";
        $query .="      GROUP_DIV = '".$model->field["GROUP_DIV"]."' ";

        return $query;
    }
}
?>
