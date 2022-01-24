<?php

require_once('for_php7.php');

class knjl514hquery extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試種別取得
    function getTestDiv($year, $applicantDiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試方式取得
    function getExamType($year, $applicantDiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL, ";
        $query .= "     EXAM_TYPE AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($year) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //願書の追加（ＣＳＶデータより読込）
    function updateQueryCsv($model, &$data_arr, &$data_arr_mirai) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            //データ件数取得
            $mirai_cnt      = $db->getOne(knjl514hQuery::cntEntexamMiraicompassDat($model, $data_arr_mirai[$i]));

            //ミライコンパス保持用　ENTEXAM_MIRAI_MEIKEI_DAT
            $data = array();
            foreach ($model->miraiTableInfo as $field) {
                $setValue = $data_arr_mirai[$i][$field["COLNAME"]];
                if (strlen($setValue) > $field["LENGTH"]) {
                    $setValue = mb_strcut($setValue,0,$field["LENGTH"]);
                }
                $data[$field["COLNAME"]][TEXT] = $setValue;
            }
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = $model->field["EXAM_TYPE"];
            if ($model->field["GRADE"] != "") {
                $data["GRADE"][TEXT]            = $model->field["GRADE"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       TEST_DATE     = '{$data_arr_mirai[$i]["TEST_DATE"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr_mirai[$i]["EXAMNO"]}'";

            if (0 == $mirai_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_MIRAI_MEIKEI_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_MIRAI_MEIKEI_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //ミライコンパスCSVテーブル情報取得
    function getMiraiTableInfo($miraiDataCnt) {
        $query  = " SELECT * from (SELECT ";
        $query .= "     COLNAME, ";
        $query .= "     COLNO, ";
        $query .= "     LENGTH ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME='ENTEXAM_MIRAI_MEIKEI_DAT') t1 ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";

        //ミライコンパスCSVテーブル情報取得
        $db = Query::dbCheckOut();
        $rtnArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["COLNO"] >= $miraiDataCnt) break;
            $rtnArray[] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        return $rtnArray;
    }

    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model, &$data_arr, &$data_arr_mirai) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ENTEXAM_MIRAI_MEIKEI_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '{$model->ObjYear}' ";
            $query .= "   AND TEST_DATE   = '{$data_arr_mirai[$i]["TEST_DATE"]}' ";
            $query .= "   AND EXAMNO      = '{$data_arr_mirai[$i]["EXAMNO"]}' ";

            $db->query($query);
            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーデータの削除
    function deleteQueryErr() {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    function selectCsvErrQuery() {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    /********************/
    /**  更新時に使用  **/
    /********************/
    //件数取得 -- ENTEXAM_MIRAI_MEIKEI_DAT
    function cntEntexamMiraicompassDat($model, $data_arr_mirai) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_MEIKEI_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     TEST_DATE   = '{$data_arr_mirai["TEST_DATE"]}' AND ";
        $query .= "     EXAMNO      = '{$data_arr_mirai["EXAMNO"]}' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

    //名称マスタより取得
    function getNameMstAll($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //データ出力
    function selectDataQuery($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_MEIKEI_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        if ($model->field["OUTPUT"] == "4") {
            $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "     AND TESTDIV      = '".$model->field["TESTDIV"]."' ";
            $query .= "     AND EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
            if ($model->field["GRADE"] != "") {
                $query .= "     AND GRADE        = '".$model->field["GRADE"]."' ";
            }
        } else if ($model->field["OUTPUT"] == "6") {
            $query .= "     AND DISTINCT_ID  IS NULL ";
        }
        $query .= " ORDER BY ";
        if ($model->field["OUTPUT"] == "5" || $model->field["OUTPUT"] == "6") {
            $query .= "     APPLICANTDIV, ";
            $query .= "     TESTDIV, ";
            $query .= "     EXAM_TYPE, ";
        }
        $query .= "     EXAMNO ";

        return $query;
    }
}
?>
