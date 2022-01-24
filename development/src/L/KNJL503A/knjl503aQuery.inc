<?php

require_once('for_php7.php');

class knjl503aQuery extends Query {

    //校種コンボ
    function getFinschoolType() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.FINSCHOOL_TYPE || ' ' || NAME1 AS LABEL, ";
        $query .= "     T1.FINSCHOOL_TYPE AS VALUE ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD1 = 'L019' ";
        $query .= "                 AND L1.NAMECD2 = T1.FINSCHOOL_TYPE ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOL_TYPE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.FINSCHOOL_TYPE ";

        return $query;
    }

    //左側リスト取得
    function getList($model, $prm="") {
        $query .= " SELECT ";
        if ($prm == "csv") {
            $query .= "     T1.ENTEXAM_SCHOOLCD, ";
            $query .= "     T2.FINSCHOOL_NAME, ";
            $query .= "     T1.FINSCHOOLCD ";
        } else {
            $query .= "   T1.ENTEXAM_SCHOOLCD, ";
            $query .= "   T2.FINSCHOOL_NAME, ";
            $query .= "   T1.FINSCHOOLCD ";
        }
        $query .= " FROM ";
        $query .= "   ENTEXAM_SCHOOL_MST T1 ";
        $query .= "   LEFT JOIN FINSCHOOL_MST T2 ";
        $query .= "     ON T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$model->examyear}' ";
        if ($model->finschoolType != "ALL") {
            $query .= "   AND T2.FINSCHOOL_TYPE = '{$model->finschoolType}' ";
        }

        $query .= " ORDER BY ";
        $query .= "   T1.ENTEXAM_SCHOOLCD ";
        return $query;
    }

    //１レコード取得
    function getRow($model, $schCd="") {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "   T1.ENTEXAM_SCHOOLCD, ";
        $query .= "   T2.FINSCHOOL_NAME, ";
        $query .= "   T1.FINSCHOOLCD ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_SCHOOL_MST T1 ";
        $query .= "   LEFT JOIN FINSCHOOL_MST T2 ";
        $query .= "     ON T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$model->examyear}' ";
        if ($schCd != "") {
            $query .= "   AND T1.ENTEXAM_SCHOOLCD = '{$schCd}' ";
        } else {
            $query .= "   AND T1.ENTEXAM_SCHOOLCD = '{$model->field["ENTEXAM_SCHOOLCD"]}' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

/***** 前年度コピー処理 *****/
    //年度のデータが存在するかチェック
    function CheckData($model) {
        $year  = $model->examyear;     //今年度チェック用
        $Lyear = $model->examyear - 1; //前年度チェック用

        $db = Query::dbCheckOut();

            $query  = " SELECT DISTINCT ";
            $query .= "     CASE WHEN ENTEXAMYEAR = '".$year."'  THEN 'X' ";
            $query .= "          WHEN ENTEXAMYEAR = '".$Lyear."' THEN 'Y' END  AS CNT";
            $query .= " FROM ";
            $query .= "     ENTEXAM_SCHOOL_MST ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     IN ('".$year."', '".$Lyear."') ";

        $result = $db->query($query);
        $opt_year = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = $row["CNT"];
        }

        Query::dbCheckIn($db);
        return $opt_year;
    }

    //前年度のデータをコピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ENTEXAM_SCHOOL_MST ";
        $query .= "         (  ";
        $query .= "          ENTEXAMYEAR ";
        $query .= "          , ENTEXAM_SCHOOLCD ";
        $query .= "          , FINSCHOOLCD ";
        $query .= "          , REGISTERCD ";
        $query .= "          , UPDATED ";
        $query .= "         ) ";
        $query .= "     ( ";
        $query .= "      SELECT ";
        $query .= "          '".$model->examyear."' ";
        $query .= "          , ENTEXAM_SCHOOLCD ";
        $query .= "          , FINSCHOOLCD ";
        $query .= "          , '".STAFFCD."' ";
        $query .= "          , SYSDATE() ";
        $query .= "      FROM ";
        $query .= "          ENTEXAM_SCHOOL_MST ";
        $query .= "      WHERE ";
        $query .= "          ENTEXAMYEAR = '".($model->examyear - 1)."' ";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $fields = $model->field;
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]          = $model->examyear;
        $data["ENTEXAM_SCHOOLCD"][TEXT]     = $fields["ENTEXAM_SCHOOLCD"];
        $data["FINSCHOOLCD"][TEXT]          = $fields["FINSCHOOLCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

       $query = Query::insertSQL($data, "ENTEXAM_SCHOOL_MST");
       $db->query($query);
       Query::dbCheckIn($db);
        return true;
    }

    //INSERT
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $fields = $model->field;
        $data = array();
        $data["FINSCHOOLCD"][TEXT]          = $fields["FINSCHOOLCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ";
        $where .= "  ENTEXAMYEAR = '{$model->examyear}' ";
        $where .= "  AND ENTEXAM_SCHOOLCD = '{$fields["ENTEXAM_SCHOOLCD"]}' ";

       $query = Query::updateSQL($data, "ENTEXAM_SCHOOL_MST", $where);

       $db->query($query);
       Query::dbCheckIn($db);

        return true;
    }

    //DELETE
    function &getDeleteQuery($model) {
     $db = Query::dbCheckOut();

        $fields = $model->field;
        $query  = " DELETE FROM ENTEXAM_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "   AND ENTEXAM_SCHOOLCD = '{$fields["ENTEXAM_SCHOOLCD"]}' ";
        $db->query($query);
        Query::dbCheckIn($db);
        
        return true;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1["PROGRAMID"][TEXT] = "KNJL503A";
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //エラーデータの削除
    function deleteQueryErr() {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL503A' ";

        return $query;
    }

    //エラー出力
    function getErrorCsvData() {
        $query  = "SELECT MSGROW, MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL503A'";

        return $query;
    }

    //存在チェック
    function checkFinSchCdExist($data, $model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '".$data["FINSCHOOLCD"]."' ";

        return $query;
    }

    //存在チェック
    function checkExist($data, $model, $flg="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR      = '".$model->examyear."' ";
        if ($flg == "CHK_FINSCH") {
            $query .= "     AND FINSCHOOLCD = '".$data["FINSCHOOLCD"]."' ";
        } else {
            $query .= "     AND ENTEXAM_SCHOOLCD = '".$data["ENTEXAM_SCHOOLCD"]."' ";
        }

        return $query;
    }

    //CSVファイルよりDBへインサート
    function insertQueryCsv($db, $model, $data_arr) {
        $data = array();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data["FINSCHOOLCD"][TEXT]        = $data_arr[$i]["FINSCHOOLCD"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "SYSDATE()";
            //登録データチェック
            $query  = knjl503aQuery::checkExist($data_arr[$i], $model);
            if (1 > $db->getOne($query)) {
                $data["ENTEXAMYEAR"][TEXT]        = $model->examyear;
                $data["ENTEXAM_SCHOOLCD"][TEXT]   = $data_arr[$i]["ENTEXAM_SCHOOLCD"];
                $query = Query::insertSQL($data, "ENTEXAM_SCHOOL_MST");
            } else {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR      = '{$model->examyear}' ";
                $where .= "     AND ENTEXAM_SCHOOLCD = '{$data_arr[$i]["ENTEXAM_SCHOOLCD"]}' ";

                $query = Query::updateSQL($data, "ENTEXAM_SCHOOL_MST", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();

        return $cnt;
    }
}
?>
