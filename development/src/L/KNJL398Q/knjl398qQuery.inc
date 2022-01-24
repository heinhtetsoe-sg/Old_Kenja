<?php

require_once('for_php7.php');

class knjl398qQuery extends Query {

    //一覧用データ取得
    function selectQuery($model, $csv="") {
        $query  = " SELECT ";
        $query .= "     T1.FINSCHOOLCD as LINK_FINSCHOOLCD, ";
        $query .= "     SUBSTR(T1.FINSCHOOLCD, 3,5) as FINSCHOOLCD, ";
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
        $query .= "     T2.FINSCHOOLCD AS TOHAIGO_CHK, ";
        $query .= "     T1.REGISTERCD, ";
        $query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= "     LEFT JOIN FINSCHOOL_DETAIL_MST T2 ON T1.FINSCHOOLCD = T2.FINSCHOOLCD AND T2.FINSCHOOL_SEQ = '001' and T2.REMARK1 = '1'  ";
        $query .= " LEFT JOIN EDBOARD_MST L1 ON L1.EDBOARDCD = T1.EDBOARDCD ";
        $query .= " LEFT JOIN NAME_MST L2 ON T1.FINSCHOOL_TYPE     = L2.NAMECD2 AND L2.NAMECD1 = 'L019' ";
        $query .= " LEFT JOIN NAME_MST L3 ON T1.FINSCHOOL_DISTCD   = L3.NAMECD2 AND L3.NAMECD1 = 'L001' ";
        $query .= " LEFT JOIN NAME_MST L4 ON T1.FINSCHOOL_DIV      = L4.NAMECD2 AND L4.NAMECD1 = 'L015' ";
        $query .= " LEFT JOIN NAME_MST L5 ON T1.DISTRICTCD         = L5.NAMECD2 AND L5.NAMECD1 = 'Z003' ";
        $query .= " LEFT JOIN NAME_MST L6 ON T1.FINSCHOOL_DISTCD2  = L6.NAMECD2 AND L6.NAMECD1 = 'Z015' ";
        $query .= " LEFT JOIN PREF_MST P1 ON T1.FINSCHOOL_PREF_CD  = P1.PREF_CD ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOL_TYPE = '".$model->selectFinschoolType."' ";
        if($csv != ""){
            //CSV出力のときは県を選択してたらその県だけ、選択してなかったら全ての学校を出力
            if($model->selectPrefCd != ""){
                $query .= " AND ";
                $query .= "     T1.FINSCHOOL_PREF_CD = '".$model->selectPrefCd."' ";
            }
        }else{
            //表示のときは中学校選択時は必ず県まで選択しないと表示しない。高校選択時は選んでたら絞る。
            if($model->selectFinschoolType == "3" || ($model->selectFinschoolType == "4" && $model->selectPrefCd != "")){
                $query .= " AND ";
                $query .= "     T1.FINSCHOOL_PREF_CD = '".$model->selectPrefCd."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     FINSCHOOLCD ";

        return $query ;
    }

    //１レコード取得
    function getScoolData($cd) {
        
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.FINSCHOOLCD AS TOHAIGO_CHK ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= "     LEFT JOIN FINSCHOOL_DETAIL_MST T2 on T1.FINSCHOOLCD = T2.FINSCHOOLCD and T2.FINSCHOOL_SEQ = '001' and T2.REMARK1 = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOLCD = '".$cd ."'";
        
        return $query;
    }

    //名称取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namecd1 == "L019"){
            /*中学校と高校のみでいい*/
            $query .= " AND ";
            $query .= "     NAMECD2 in ('3','4') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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

    //CSV書出し
    function getCsvData() {
        $query  = " SELECT ";
        $query .= "     FINSCHOOLCD, ";
        $query .= "     FINSCHOOL_TYPE, ";
        $query .= "     FINSCHOOL_DISTCD, ";
        $query .= "     FINSCHOOL_DISTCD2, ";
        $query .= "     FINSCHOOL_DIV, ";
        $query .= "     FINSCHOOL_NAME, ";
        $query .= "     FINSCHOOL_KANA, ";
        $query .= "     FINSCHOOL_NAME_ABBV, ";
        $query .= "     FINSCHOOL_KANA_ABBV, ";
        $query .= "     PRINCNAME, ";
        $query .= "     PRINCNAME_SHOW, ";
        $query .= "     PRINCKANA, ";
        $query .= "     DISTRICTCD, ";
        $query .= "     FINSCHOOL_PREF_CD, ";
        $query .= "     FINSCHOOL_ZIPCD, ";
        $query .= "     FINSCHOOL_ADDR1, ";
        $query .= "     FINSCHOOL_ADDR2, ";
        $query .= "     FINSCHOOL_TELNO, ";
        $query .= "     FINSCHOOL_FAXNO, ";
        $query .= "     EDBOARDCD, ";
        $query .= "     'DUMMY' AS DUMMY ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";

        return $query;
    }

    //エラーデータの削除
    function deleteQueryErr() {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJZ090_2A' ";

        return $query;
    }

    //年度データの確認
    function IsExisting($db, $finschoolcd) {
        $query = "SELECT * FROM FINSCHOOL_YDAT WHERE FINSCHOOLCD = '".$finschoolcd."'";
        $row   = $db->getRow($query);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }

    //エラー出力
    function getErrorCsvData() {
        $query  = "SELECT MSGROW, MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJZ090_2A'";

        return $query;
    }

    //CSV取り込み時の入力チェック、名称マスターに登録されているコードなのか。
    function checkNameMst($namecd1, $namecd2) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' AND ";
        $query .= "     NAMECD2 = '{$namecd2}' ";

        return $query;
    }

    //CSV取り込み時の入力チェック、都道府県マスタに登録されているコードなのか。
    function checkPrefcd($prefcd) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '{$prefcd}' ";

        return $query;
    }

    //CSV取り込み時の入力チェック、教育委員会マスターに登録されているコードなのか。
    function checkEdboardcd($edboardcd) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EDBOARD_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARDCD = '{$edboardcd}' ";

        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1["PROGRAMID"][TEXT] = "KNJZ090_2A";
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //--- INSERT
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

        //統廃合チェックがON
        if($fields["TOHAIGO_CHK"] == "1"){
            $upinsert["FINSCHOOLCD"]   = $fields["FINSCHOOLCD"];
            $upinsert["FINSCHOOL_SEQ"] = '001';
            $upinsert["REMARK1"]       = "1";
            $upinsert["REGISTERCD"]    = STAFFCD;
            $upinsert["UPDATED"]       = "SYSDATE()";
            $where  = " WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' AND FINSCHOOL_SEQ = '001' ";
            common::_update("FINSCHOOL_DETAIL_MST","",$where,$upinsert,$db,"0");
        } else {
            knjl398qQuery::getDeleteDetailQuery($db, $fields);
        }

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($fields) {
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

        $where = "WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."'";
        $query = Query::updateSQL($data, "FINSCHOOL_MST", $where);

        //統廃合チェックがON
        if($fields["TOHAIGO_CHK"] == "1"){
            $upinsert["FINSCHOOLCD"]   = $fields["FINSCHOOLCD"];
            $upinsert["FINSCHOOL_SEQ"] = '001';
            $upinsert["REMARK1"]       = "1";
            $upinsert["REGISTERCD"]    = STAFFCD;
            $upinsert["UPDATED"]       = "SYSDATE()";
            $where  = " WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' AND FINSCHOOL_SEQ = '001' ";
            common::_update("FINSCHOOL_DETAIL_MST","",$where,$upinsert,$db,"0");
        } else {
            knjl398qQuery::getDeleteDetailQuery($db, $fields);
        }

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($db, $fields) {
        $query  = " DELETE FROM FINSCHOOL_MST ";
        $query .= " WHERE FINSCHOOLCD  = '".$fields["FINSCHOOLCD"]."' ";

        $db->query($query);
        return true;
    }

    //--- DELETE
    function &getDeleteDetailQuery($db, $fields) {
        $query  = " DELETE FROM FINSCHOOL_DETAIL_MST ";
        $query .= " WHERE FINSCHOOLCD  = '".$fields["FINSCHOOLCD"]."' AND FINSCHOOL_SEQ = '001' ";

        $db->query($quer< get_count(

        return true;
    }

    //CSVファイルよりDBへインサート
    function insertQueryCsv($db, $model, $data_arr) {
        $data = array();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["FINSCHOOLCD"][TEXT]         = $data_arr[$i]["FINSCHOOLCD"];
            $data["FINSCHOOL_TYPE"][TEXT]      = $data_arr[$i]["FINSCHOOL_TYPE"];
            $data["FINSCHOOL_DISTCD"][TEXT]    = $data_arr[$i]["FINSCHOOL_DISTCD"];
            $data["FINSCHOOL_DISTCD2"][TEXT]   = $data_arr[$i]["FINSCHOOL_DISTCD2"];
            $data["FINSCHOOL_DIV"][TEXT]       = $data_arr[$i]["FINSCHOOL_DIV"];
            $data["FINSCHOOL_NAME"][TEXT]      = $data_arr[$i]["FINSCHOOL_NAME"];
            $data["FINSCHOOL_KANA"][TEXT]      = $data_arr[$i]["FINSCHOOL_KANA"];
            $data["FINSCHOOL_NAME_ABBV"][TEXT] = $data_arr[$i]["FINSCHOOL_NAME_ABBV"];
            $data["FINSCHOOL_KANA_ABBV"][TEXT] = $data_arr[$i]["FINSCHOOL_KANA_ABBV"];
            $data["PRINCNAME"][TEXT]           = $data_arr[$i]["PRINCNAME"];
            $data["PRINCNAME_SHOW"][TEXT]      = $data_arr[$i]["PRINCNAME_SHOW"];
            $data["PRINCKANA"][TEXT]           = $data_arr[$i]["PRINCKANA"];
            $data["DISTRICTCD"][TEXT]          = $data_arr[$i]["DISTRICTCD"];
            $data["FINSCHOOL_PREF_CD"][TEXT]   = $data_arr[$i]["FINSCHOOL_PREF_CD"];
            $data["FINSCHOOL_ZIPCD"][TEXT]     = $data_arr[$i]["FINSCHOOL_ZIPCD"];
            $data["FINSCHOOL_ADDR1"][TEXT]     = $data_arr[$i]["FINSCHOOL_ADDR1"];
            $data["FINSCHOOL_ADDR2"][TEXT]     = $data_arr[$i]["FINSCHOOL_ADDR2"];
            $data["FINSCHOOL_TELNO"][TEXT]     = $data_arr[$i]["FINSCHOOL_TELNO"];
            $data["FINSCHOOL_FAXNO"][TEXT]     = $data_arr[$i]["FINSCHOOL_FAXNO"];
            $data["EDBOARDCD"][TEXT]           = $data_arr[$i]["EDBOARDCD"];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM FINSCHOOL_MST WHERE FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}'")) {
                $query = Query::insertSQL($data, "FINSCHOOL_MST");
            } else {
                $where  = "WHERE ";
                $where .= "     FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}' ";

                $query = Query::updateSQL($data, "FINSCHOOL_MST", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();

        return $cnt;
    }
}
?>
