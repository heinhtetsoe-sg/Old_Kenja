<?php

require_once('for_php7.php');

class knjl517jQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試種別取得
    function getTestdiv($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストtoリスト取得
    function getReceptDatData($model, $examNo = "") {
        $query  = " SELECT ";
        $query .= "     RCPT.EXAMNO AS VALUE, ";
        $query .= "     RCPT.EXAMNO || '：' || BASE.NAME AS LABEL, ";
        $query .= "     RCPT.ENTEXAMYEAR, ";
        $query .= "     RCPT.APPLICANTDIV, ";
        $query .= "     RCPT.TESTDIV, ";
        $query .= "     RCPT.EXAM_TYPE, ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     RCPT.PROCEDUREDIV1  AS PROCEDUREDIV, ";
        $query .= "     RCPT.PROCEDUREDATE1 AS PROCEDUREDATE, ";
        $query .= "     RCPT.ADJOURNMENTDIV AS ENTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                              AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                              AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND RCPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RCPT.TESTDIV      = '{$model->testdiv}' ";
        if (strlen($examNo)) {
            $query .= "     AND RCPT.EXAMNO       = '{$examNo}' ";
        }
        $query .= "     AND RCPT.JUDGEDIV     = '1' "; // 合格者
        $query .= "     AND VALUE(RCPT.ADJOURNMENTDIV,'') != '2' "; // 辞退者は除く
        $query .= " ORDER BY ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //手続終了者一覧を配列に格納
        $passExamNo = explode(",", $model->selectdata);

        $queryRecept = knjl517jQuery::getReceptDatData($model);
        $result = $db->query($queryRecept);
        while ($recept = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            if (in_array($recept["EXAMNO"], $passExamNo)) {
                //手続した人の手続日は上書きしない
                $setProDiv = "1"; // 手続済
                $setProDate = ($recept["PROCEDUREDATE"]) ? $recept["PROCEDUREDATE"] : str_replace('/', '-', $model->field["PROCEDUREDATE"]); // 手続日
                $setEntDiv = "1"; // 入学
            } else {
                $setProDiv = "2"; // 未手続
                $setProDate = "";
                $setEntDiv = "";
            }

            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["PROCEDUREDIV1"][TEXT]    = $setProDiv;
            $data["PROCEDUREDATE1"][TEXT]   = $setProDate;
            $data["ADJOURNMENTDIV"][TEXT]   = $setEntDiv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
            $where .= "   AND EXAMNO        = '{$recept["EXAMNO"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //入学が1件でもあれば入学、辞退が1件でもあれば辞退、それ以外は未手続で更新
            $query  = " SELECT ";
            $query .= "     SUM(CASE WHEN PROCEDUREDIV1 = '1' AND ADJOURNMENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT, ";
            $query .= "     SUM(CASE WHEN PROCEDUREDIV1 = '1' AND ADJOURNMENTDIV = '2' THEN 1 ELSE 0 END) AS JITAI_CNT, ";
            $query .= "     MAX(PROCEDUREDATE1) AS PROCEDUREDATE ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ";
            $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
            $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "     AND EXAMNO       = '{$recept["EXAMNO"]}' ";
            $query .= "     AND JUDGEDIV     = '1' ";
            $recRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if ($recRow["ENT_CNT"] > 0) {
                $setProDiv = "1"; // 手続済
                $setProDate = $recRow["PROCEDUREDATE"]; // 手続日
                $setEntDiv = "1"; // 入学
            } else if ($recRow["JITAI_CNT"] > 0) {
                $setProDiv = "1"; // 手続済
                $setProDate = $recRow["PROCEDUREDATE"]; // 手続日
                $setEntDiv = "2"; // 辞退
            } else {
                $setProDiv = "2"; // 未手続
                $setProDate = "";
                $setEntDiv = "";
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = $setProDiv;
            $data["PROCEDUREDATE"][TEXT]    = $setProDate;
            $data["ENTDIV"][TEXT]           = $setEntDiv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$recept["EXAMNO"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    function updateQueryCsv($model, &$data_arr) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            if (trim($data_arr[$i]["PAY_INFO_DIV"]) == "完了") {
                //手続した人の手続日は上書きしない
                $query = knjl517jQuery::getReceptDatData($model, $data_arr[$i]["EXAMNO"]);
                $recept = $db->getRow($query, DB_FETCHMODE_ASSOC);

                $setProDiv = "1"; // 手続済
                $setProDate = ($recept["PROCEDUREDATE"]) ? $recept["PROCEDUREDATE"] : str_replace('/', '-', $model->field["PROCEDUREDATE"]); // 手続日
                $setEntDiv = "1"; // 入学
            } else {
                $setProDiv = "2"; // 未手続
                $setProDate = "";
                $setEntDiv = "";
            }

            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["PROCEDUREDIV1"][TEXT]    = $setProDiv;
            $data["PROCEDUREDATE1"][TEXT]   = $setProDate;
            $data["ADJOURNMENTDIV"][TEXT]   = $setEntDiv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
            $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //入学が1件でもあれば入学、辞退が1件でもあれば辞退、それ以外は未手続で更新
            $query  = " SELECT ";
            $query .= "     SUM(CASE WHEN PROCEDUREDIV1 = '1' AND ADJOURNMENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT, ";
            $query .= "     SUM(CASE WHEN PROCEDUREDIV1 = '1' AND ADJOURNMENTDIV = '2' THEN 1 ELSE 0 END) AS JITAI_CNT, ";
            $query .= "     MAX(PROCEDUREDATE1) AS PROCEDUREDATE ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ";
            $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
            $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "     AND EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' ";
            $query .= "     AND JUDGEDIV     = '1' ";
            $recRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if ($recRow["ENT_CNT"] > 0) {
                $setProDiv = "1"; // 手続済
                $setProDate = $recRow["PROCEDUREDATE"]; // 手続日
                $setEntDiv = "1"; // 入学
            } else if ($recRow["JITAI_CNT"] > 0) {
                $setProDiv = "1"; // 手続済
                $setProDate = $recRow["PROCEDUREDATE"]; // 手続日
                $setEntDiv = "2"; // 辞退
            } else {
                $setProDiv = "2"; // 未手続
                $setProDate = "";
                $setEntDiv = "";
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = $setProDiv;
            $data["PROCEDUREDATE"][TEXT]    = $setProDate;
            $data["ENTDIV"][TEXT]           = $setEntDiv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //ミライコンパス保持用　ENTEXAM_MIRAI_PAY_DAT
            $data = array();
            $data["MIRAI_SCHOOLCD"][TEXT]           = $data_arr[$i]["MIRAI_SCHOOLCD"];
            $data["ENTEXAMYEAR"][TEXT]              = $data_arr[$i]["ENTEXAMYEAR"];
            $data["APPNO"][TEXT]                    = $data_arr[$i]["APPNO"];
            $data["SHIGANSYA_ID"][TEXT]             = $data_arr[$i]["SHIGANSYA_ID"];
            $data["EXAMNO"][TEXT]                   = $data_arr[$i]["EXAMNO"];
            $data["EXAM_NAME_SEI"][TEXT]            = $data_arr[$i]["EXAM_NAME_SEI"];
            $data["EXAM_NAME_MEI"][TEXT]            = $data_arr[$i]["EXAM_NAME_MEI"];
            $data["SEX"][TEXT]                      = $data_arr[$i]["SEX"];
            $data["MIRAI_FS_NAME"][TEXT]            = $data_arr[$i]["MIRAI_FS_NAME"];
            $data["MIRAI_PS_NAME"][TEXT]            = $data_arr[$i]["MIRAI_PS_NAME"];
            $data["ITEM_GROUP"][TEXT]               = $data_arr[$i]["ITEM_GROUP"];
            $data["ITEM_ID"][TEXT]                  = $data_arr[$i]["ITEM_ID"];
            $data["ITEM_NAME"][TEXT]                = $data_arr[$i]["ITEM_NAME"];
            $data["BULK_MONEY"][TEXT]               = $data_arr[$i]["BULK_MONEY"];
            $data["DELAY_PAY_LUMP"][TEXT]           = $data_arr[$i]["DELAY_PAY_LUMP"];
            $data["ACCEPT_START_DATE"][TEXT]        = $data_arr[$i]["ACCEPT_START_DATE"];
            $data["PAY_DUE_DATE_MST"][TEXT]         = $data_arr[$i]["PAY_DUE_DATE_MST"];
            $data["DELAY_DUE_DATE"][TEXT]           = $data_arr[$i]["DELAY_DUE_DATE"];
            $data["ITEM_EXPLAIN"][TEXT]             = $data_arr[$i]["ITEM_EXPLAIN"];
            $data["PAY_DEC"][TEXT]                  = $data_arr[$i]["PAY_DEC"];
            $data["PAY_DEC_DATE"][TEXT]             = $data_arr[$i]["PAY_DEC_DATE"];
            $data["PAY_DEC_REASON"][TEXT]           = $data_arr[$i]["PAY_DEC_REASON"];
            $data["PAY_DEC_REASON_DETAIL"][TEXT]    = $data_arr[$i]["PAY_DEC_REASON_DETAIL"];
            $data["MEMO_MST"][TEXT]                 = $data_arr[$i]["MEMO_MST"];
            $data["ITEM_PAY_DIV"][TEXT]             = $data_arr[$i]["ITEM_PAY_DIV"];
            $data["PAY_TYPE_CD"][TEXT]              = $data_arr[$i]["PAY_TYPE_CD"];
            $data["APP_EXAMNO"][TEXT]               = $data_arr[$i]["APP_EXAMNO"];
            $data["NAME_SEI"][TEXT]                 = $data_arr[$i]["NAME_SEI"];
            $data["NAME_MEI"][TEXT]                 = $data_arr[$i]["NAME_MEI"];
            $data["NAME_KANA_SEI"][TEXT]            = $data_arr[$i]["NAME_KANA_SEI"];
            $data["NAME_KANA_MEI"][TEXT]            = $data_arr[$i]["NAME_KANA_MEI"];
            $data["BIRTHDAY"][TEXT]                 = $data_arr[$i]["BIRTHDAY"];
            $data["TELNO"][TEXT]                    = $data_arr[$i]["TELNO"];
            $data["MAIL"][TEXT]                     = $data_arr[$i]["MAIL"];
            $data["SETTLE_MONEY"][TEXT]             = $data_arr[$i]["SETTLE_MONEY"];
            $data["DEMAND_TOTAL_MONEY"][TEXT]       = $data_arr[$i]["DEMAND_TOTAL_MONEY"];
            $data["FEES"][TEXT]                     = $data_arr[$i]["FEES"];
            $data["PAY_INFO_DIV"][TEXT]             = $data_arr[$i]["PAY_INFO_DIV"];
            $data["APP_DATED"][TEXT]                = $data_arr[$i]["APP_DATED"];
            $data["PAY_DUE_DATE"][TEXT]             = $data_arr[$i]["PAY_DUE_DATE"];
            $data["PAY_DATED"][TEXT]                = $data_arr[$i]["PAY_DATED"];
            $data["ENT_DEC"][TEXT]                  = $data_arr[$i]["ENT_DEC"];
            $data["ENT_DEC_DATE"][TEXT]             = $data_arr[$i]["ENT_DEC_DATE"];
            $data["ENT_DEC_REASON"][TEXT]           = $data_arr[$i]["ENT_DEC_REASON"];
            $data["ENT_DEC_REASON_DETAIL"][TEXT]    = $data_arr[$i]["ENT_DEC_REASON_DETAIL"];
            $data["MEMO_PAY"][TEXT]                 = $data_arr[$i]["MEMO_PAY"];
            $data["APPLICANTDIV"][TEXT]             = $model->applicantdiv;
            $data["TESTDIV"][TEXT]                  = $model->testdiv;
            $data["PROCEDUREDATE"][TEXT]            = $model->field["PROCEDUREDATE"];
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][FUNC]                  = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            $mirai_cnt = $db->getOne("SELECT COUNT(*) FROM ENTEXAM_MIRAI_PAY_DAT".$where);

            if (0 == $mirai_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_MIRAI_PAY_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_MIRAI_PAY_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //名称マスタ登録コードチェック
    function nameCntSql($model, $namecd, $setcd, $where="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
        $query .= "     AND NAMECD1 = '{$namecd}' ";
        $query .= "     AND NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //受験番号　getReceptDatDataと同じ条件
    function checkExamNo($model, $examNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                              AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                              AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND RCPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RCPT.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND RCPT.EXAMNO       = '{$examNo}' ";
        $query .= "     AND RCPT.JUDGEDIV     = '1' "; // 合格者
        $query .= "     AND VALUE(RCPT.ADJOURNMENTDIV,'') != '2' "; // 辞退者は除く

        return $query;
    }
}
?>