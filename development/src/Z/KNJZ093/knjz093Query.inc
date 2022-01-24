<?php

require_once('for_php7.php');

class knjz093Query extends Query {

    //名称取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧用データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.FINSCHOOL_TYPE || ':' || L2.NAME1 AS FINSCHOOL_TYPE, ";
        $query .= "     T1.FINSCHOOL_DISTCD || ':' || L3.NAME1 AS FINSCHOOL_DISTCD, ";
        $query .= "     T1.FINSCHOOL_DISTCD2 || ':' || L6.NAME1 AS FINSCHOOL_DISTCD2, ";
        $query .= "     T1.FINSCHOOL_DIV || ':' || L4.NAME1 AS FINSCHOOL_DIV, ";
        $query .= "     T1.FINSCHOOL_NAME, ";
        $query .= "     T1.FINSCHOOL_KANA, ";
        $query .= "     T1.FINSCHOOL_NAME_ABBV, ";
        $query .= "     T1.FINSCHOOL_KANA_ABBV, ";
        $query .= "     T1.PRINCNAME, ";
        $query .= "     T1.PRINCNAME_SHOW, ";
        $query .= "     T1.PRINCKANA, ";
        $query .= "     T1.DISTRICTCD || ':' || L5.NAME1 AS DISTRICTCD, ";
        $query .= "     T1.FINSCHOOL_PREF_CD || ':' || P1.PREF_NAME AS FINSCHOOL_PREF_CD, ";
        $query .= "     T1.FINSCHOOL_ZIPCD, ";
        $query .= "     T1.FINSCHOOL_ADDR1, ";
        $query .= "     T1.FINSCHOOL_ADDR2, ";
        $query .= "     T1.FINSCHOOL_TELNO, ";
        $query .= "     T1.FINSCHOOL_FAXNO, ";
        $query .= "     T1.EDBOARDCD || ':' || L1.EDBOARDNAME AS EDBOARDCD, ";
        $query .= "     T1.REGISTERCD, ";
        $query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= " LEFT JOIN EDBOARD_MST L1 ON L1.EDBOARDCD = T1.EDBOARDCD ";
        $query .= " LEFT JOIN NAME_MST L2 ON T1.FINSCHOOL_TYPE     = L2.NAMECD2 AND L2.NAMECD1 = 'L019' ";
        $query .= " LEFT JOIN NAME_MST L3 ON T1.FINSCHOOL_DISTCD   = L3.NAMECD2 AND L3.NAMECD1 = 'L001' ";
        $query .= " LEFT JOIN NAME_MST L4 ON T1.FINSCHOOL_DIV      = L4.NAMECD2 AND L4.NAMECD1 = 'L015' ";
        $query .= " LEFT JOIN NAME_MST L5 ON T1.DISTRICTCD         = L5.NAMECD2 AND L5.NAMECD1 = 'Z003' ";
        $query .= " LEFT JOIN NAME_MST L6 ON T1.FINSCHOOL_DISTCD2  = L6.NAMECD2 AND L6.NAMECD1 = 'Z015' ";
        $query .= " LEFT JOIN PREF_MST P1 ON T1.FINSCHOOL_PREF_CD  = P1.PREF_CD ";
        if ($model->selectFinschoolType != "99") {
            $query .= " WHERE ";
            $query .= "     T1.FINSCHOOL_TYPE = '".$model->selectFinschoolType."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     FINSCHOOLCD ";

        return $query ;
    }

    //１レコード取得
    function getSchoolData($finschoolcd) {
        $query  = " SELECT ";
        $query .= "     F1.*, ";
        $query .= "     F2.REMARK1 AS DISTRICT_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST F1 ";
        $query .= "     LEFT JOIN FINSCHOOL_DETAIL_MST F2 ";
        $query .= "         ON F1.FINSCHOOLCD   = F2.FINSCHOOLCD ";
        $query .= "        AND F2.FINSCHOOL_SEQ = '002' ";
        $query .= " WHERE ";
        $query .= "     F1.FINSCHOOLCD = '".$finschoolcd."' ";

        return $query;
    }

    //教育委員会用学校コード取得
    function getEdboardSchoolcd() {
        $query  = " SELECT ";
        $query .= "     KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //都道府県コード取得
    function getPrefMst() {
        $query  = " SELECT ";
        $query .= "     PREF_CD || ' ' || PREF_NAME AS LABEL, ";
        $query .= "     PREF_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教育委員会コード取得
    function getEdboardMst() {
        $query  = " SELECT ";
        $query .= "     EDBOARDCD || ' ' || EDBOARDNAME AS LABEL, ";
        $query .= "     EDBOARDCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     EDBOARD_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //所在地データ取得
    function getDistrictName() {
        $query  = " SELECT ";
        $query .= "     DISTRICTCD || ' ' || DISTRICT_NAME AS LABEL, ";
        $query .= "     DISTRICTCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_LOCATION_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度データの確認
    function IsExisting($db, $finschoolcd) {
        $query = " SELECT * FROM FINSCHOOL_YDAT WHERE FINSCHOOLCD = '".$finschoolcd."' ";
        $row   = $db->getRow($query);

        if (is_array($row)) {
            return true;
        } else {
            return false;
        }
    }

    //学校別出身学校外テーブル存在チェック
    function chkEdboardFinschoolDat($finschoolcd) {
        $query  = " SELECT "; 
        $query .= "     * "; 
        $query .= " FROM "; 
        $query .= "     EDBOARD_FINSCHOOL_DAT "; 
        $query .= " WHERE "; 
        $query .= "     FINSCHOOLCD = '".$finschoolcd."' "; 

        return $query;
    }

    //INSERT -- FINSCHOOL_MST
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data["FINSCHOOLCD"][TEXT]         = $fields["FINSCHOOLCD"];
        $data["FINSCHOOL_TYPE"][TEXT]      = $fields["FINSCHOOL_TYPE"];
        $data["FINSCHOOL_DISTCD"][TEXT]    = $fields["FINSCHOOL_DISTCD"];
        $data["FINSCHOOL_DISTCD2"][TEXT]   = $fields["FINSCHOOL_DISTCD2"];
        $data["FINSCHOOL_DIV"][TEXT]       = $fields["FINSCHOOL_DIV"];
        $data["FINSCHOOL_NAME"][TEXT]      = $fields["FINSCHOOL_NAME"];
        $data["FINSCHOOL_KANA"][TEXT]      = $fields["FINSCHOOL_KANA"];
        $data["FINSCHOOL_NAME_ABBV"][TEXT] = $fields["FINSCHOOL_NAME_ABBV"];
        $data["FINSCHOOL_KANA_ABBV"][TEXT] = $fields["FINSCHOOL_KANA_ABBV"];
        $data["PRINCNAME"][TEXT]           = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]      = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]           = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]          = $fields["DISTRICTCD"];
        $data["FINSCHOOL_PREF_CD"][TEXT]   = $fields["FINSCHOOL_PREF_CD"];
        $data["FINSCHOOL_ZIPCD"][TEXT]     = $fields["FINSCHOOL_ZIPCD"];
        $data["FINSCHOOL_ADDR1"][TEXT]     = $fields["FINSCHOOL_ADDR1"];
        $data["FINSCHOOL_ADDR2"][TEXT]     = $fields["FINSCHOOL_ADDR2"];
        $data["FINSCHOOL_TELNO"][TEXT]     = $fields["FINSCHOOL_TELNO"];
        $data["FINSCHOOL_FAXNO"][TEXT]     = $fields["FINSCHOOL_FAXNO"];
        $data["EDBOARDCD"][TEXT]           = $fields["EDBOARDCD"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "SYSDATE()";

        $query = Query::insertSQL($data, "FINSCHOOL_MST");
        $db->query($query);

        //FINSCHOOL_DETAIL_MST
        $data2 = array();
        $data2["FINSCHOOLCD"][TEXT]   = $fields["FINSCHOOLCD"];
        $data2["FINSCHOOL_SEQ"][TEXT] = "002";
        $data2["REMARK1"][TEXT]       = $fields["DISTRICT_NAME"];
        $data2["REGISTERCD"][TEXT]    = STAFFCD;
        $data2["UPDATED"][FUNC]       = "SYSDATE()";

        $query2 = Query::insertSQL($data2, "FINSCHOOL_DETAIL_MST");
        $db->query($query2);

        Query::dbCheckIn($db);
        return;
    }

    //INSERT -- EDBOARD_FINSCHOOL_DAT
    function &insertEdboardFinschoolDat($model, $fields) {
        $db2 = Query::dbCheckOut2();

        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->edboard_schoolcd;
        $data["FINSCHOOLCD"][TEXT]          = $fields["FINSCHOOLCD"];
        $data["EDBOARD_FLG"][TEXT]          = ($model->iinkai) ? 1 : 2;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "EDBOARD_FINSCHOOL_DAT");
        $db2->query($query);

        Query::dbCheckIn($db2);
        return;
    }

    //UPDATE -- FINSCHOOL_MST
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data["FINSCHOOL_TYPE"][TEXT]      = $fields["FINSCHOOL_TYPE"];
        $data["FINSCHOOL_DISTCD"][TEXT]    = $fields["FINSCHOOL_DISTCD"];
        $data["FINSCHOOL_DISTCD2"][TEXT]   = $fields["FINSCHOOL_DISTCD2"];
        $data["FINSCHOOL_DIV"][TEXT]       = $fields["FINSCHOOL_DIV"];
        $data["FINSCHOOL_NAME"][TEXT]      = $fields["FINSCHOOL_NAME"];
        $data["FINSCHOOL_KANA"][TEXT]      = $fields["FINSCHOOL_KANA"];
        $data["FINSCHOOL_NAME_ABBV"][TEXT] = $fields["FINSCHOOL_NAME_ABBV"];
        $data["FINSCHOOL_KANA_ABBV"][TEXT] = $fields["FINSCHOOL_KANA_ABBV"];
        $data["PRINCNAME"][TEXT]           = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]      = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]           = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]          = $fields["DISTRICTCD"];
        $data["FINSCHOOL_PREF_CD"][TEXT]   = $fields["FINSCHOOL_PREF_CD"];
        $data["FINSCHOOL_ZIPCD"][TEXT]     = $fields["FINSCHOOL_ZIPCD"];
        $data["FINSCHOOL_ADDR1"][TEXT]     = $fields["FINSCHOOL_ADDR1"];
        $data["FINSCHOOL_ADDR2"][TEXT]     = $fields["FINSCHOOL_ADDR2"];
        $data["FINSCHOOL_TELNO"][TEXT]     = $fields["FINSCHOOL_TELNO"];
        $data["FINSCHOOL_FAXNO"][TEXT]     = $fields["FINSCHOOL_FAXNO"];
        $data["EDBOARDCD"][TEXT]           = $fields["EDBOARDCD"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' ";

        $query  = Query::updateSQL($data, "FINSCHOOL_MST", $where);
        $db->query($query);

        //FINSCHOOL_DETAIL_MST
        $query  = "DELETE FROM FINSCHOOL_DETAIL_MST WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' AND FINSCHOOL_SEQ = '002' ";
        $db->query($query);
        $data2 = array();
        $data2["FINSCHOOLCD"][TEXT]   = $fields["FINSCHOOLCD"];
        $data2["FINSCHOOL_SEQ"][TEXT] = "002";
        $data2["REMARK1"][TEXT]       = $fields["DISTRICT_NAME"];
        $data2["REGISTERCD"][TEXT]    = STAFFCD;
        $data2["UPDATED"][FUNC]       = "SYSDATE()";

        $query2 = Query::insertSQL($data2, "FINSCHOOL_DETAIL_MST");
        $db->query($query2);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $db->autoCommit(false);

        //DELETE -- FINSCHOOL_MST
        $query  = " DELETE FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' ";
        $db->query($query);

        //DELETE -- FINSCHOOL_DETAIL_MST
        $query  = " DELETE FROM ";
        $query .= "     FINSCHOOL_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "         FINSCHOOLCD   = '".$fields["FINSCHOOLCD"]."' ";
        $query .= "     AND FINSCHOOL_SEQ = '002' ";
        $db->query($query);

        //学校側のとき
        if ($model->sendSubmit != "") {
            //DELETE -- EDBOARD_STAFF_DAT
            $query  = " DELETE FROM EDBOARD_FINSCHOOL_DAT ";
            $query .= " WHERE EDBOARD_SCHOOLCD = '".$model->edboard_schoolcd."' ";
            $query .= "   AND FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' ";
            $db2->query($query);
        }

        $db->commit();  // トランザクションをコミットする。

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return true;
    }
}
?>
