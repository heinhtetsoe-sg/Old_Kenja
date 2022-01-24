<?php
class knjl580iQuery extends Query
{
    //入試区分マスタ取得
    public function getTestdivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストtoリスト取得
    public function getBaseDatData($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO AS VALUE, ";
        $query .= "     B1.EXAMNO || '：' || B1.NAME AS LABEL, ";
        $query .= "     R1.TESTDIV, ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     R1.EXAM_TYPE, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.TESTDIV0, ";
        $query .= "     B1.PROCEDUREDIV, ";
        $query .= "     B1.PROCEDUREDATE, ";
        $query .= "     RD015.REMARK4 AS SP_CD, ";
        $query .= "     RD015.REMARK5 AS SP_REASON_CD, ";
        $query .= "     GEN04.REMARK1 AS ENT_FEE, ";
        $query .= "     GEN04.REMARK2 AS FACILITY_FEE, ";
        $query .= "     GEN05.REMARK1 AS ENT_PROMISE_FLG, ";
        $query .= "     RD020.REMARK1 AS PROC_TYPE1_FLG, ";
        $query .= "     RD020.REMARK2 AS PROC_TYPE1_DATE, ";
        $query .= "     RD020.REMARK3 AS PROC_TYPE2_FLG, ";
        $query .= "     RD020.REMARK4 AS PROC_TYPE2_DATE, ";
        $query .= "     RD020.REMARK5 AS PROC_TYPE3_FLG, ";
        $query .= "     RD020.REMARK6 AS PROC_TYPE3_DATE, ";
        $query .= "     GEN01.REMARK2 AS SUC_TOTALCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO       = R1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_SETTING_MST S1 ";
        $query .= "          ON S1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND S1.SETTING_CD   = 'L013' ";
        $query .= "         AND S1.NAMESPARE1   = '1' ";
        $query .= "         AND S1.SEQ          = R1.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD015 ";
        $query .= "          ON RD015.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND RD015.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND RD015.TESTDIV      = R1.TESTDIV ";
        $query .= "         AND RD015.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "         AND RD015.RECEPTNO     = R1.RECEPTNO ";
        $query .= "         AND RD015.SEQ          = '015' ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GEN03 ";
        $query .= "          ON GEN03.ENTEXAMYEAR  = RD015.ENTEXAMYEAR ";
        $query .= "         AND GEN03.APPLICANTDIV = RD015.APPLICANTDIV ";
        $query .= "         AND GEN03.TESTDIV      = '0' ";
        $query .= "         AND GEN03.GENERAL_DIV  = '03' ";
        $query .= "         AND GEN03.GENERAL_CD   = RD015.REMARK3 ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GEN02 ";
        $query .= "          ON GEN02.ENTEXAMYEAR  = GEN03.ENTEXAMYEAR ";
        $query .= "         AND GEN02.APPLICANTDIV = GEN03.APPLICANTDIV ";
        $query .= "         AND GEN02.TESTDIV      = '0' ";
        $query .= "         AND GEN02.GENERAL_DIV  = '02' ";
        $query .= "         AND GEN02.GENERAL_CD   = GEN03.REMARK1 ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GEN01 ";
        $query .= "          ON GEN01.ENTEXAMYEAR  = GEN02.ENTEXAMYEAR ";
        $query .= "         AND GEN01.APPLICANTDIV = GEN02.APPLICANTDIV ";
        $query .= "         AND GEN01.TESTDIV      = '0' ";
        $query .= "         AND GEN01.GENERAL_DIV  = '01' ";
        $query .= "         AND GEN01.GENERAL_CD   = GEN02.REMARK1 ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GEN04 ";
        $query .= "          ON GEN04.ENTEXAMYEAR  = RD015.ENTEXAMYEAR ";
        $query .= "         AND GEN04.APPLICANTDIV = RD015.APPLICANTDIV ";
        $query .= "         AND GEN04.TESTDIV      = '0' ";
        $query .= "         AND GEN04.GENERAL_DIV  = '04' ";
        $query .= "         AND GEN04.GENERAL_CD   = RD015.REMARK4 ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GEN05 ";
        $query .= "          ON GEN05.ENTEXAMYEAR  = RD015.ENTEXAMYEAR ";
        $query .= "         AND GEN05.APPLICANTDIV = RD015.APPLICANTDIV ";
        $query .= "         AND GEN05.TESTDIV      = '0' ";
        $query .= "         AND GEN05.GENERAL_DIV  = '05' ";
        $query .= "         AND GEN05.GENERAL_CD   = RD015.REMARK5 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD020 ";
        $query .= "          ON RD020.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND RD020.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND RD020.TESTDIV      = R1.TESTDIV ";
        $query .= "         AND RD020.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "         AND RD020.RECEPTNO     = R1.RECEPTNO ";
        $query .= "         AND RD020.SEQ          = '020' ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR          = '{$model->examYear}' ";
        $query .= "     AND B1.APPLICANTDIV     = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV          = '{$model->testdiv}' ";   //入試区分
        $query .= "     AND B1.TESTDIV0         = '{$model->exam_type}' "; //学科
        $query .= "     AND VALUE(B1.ENTDIV,'') != '2' "; // 辞退者は除く
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";

        return $query;
    }

    public function getReceptDetailCount($model, $receptno, $exam_type, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$exam_type}' ";
        $query .= "     AND RECEPTNO     = '{$receptno}' ";
        $query .= "     AND SEQ          = '{$seq}' ";
        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //更新
    public function getUpdateQuery($db, $model)
    {
        //手続終了者一覧を配列に格納
        $passExamNo = explode(",", $model->selectdata);

        $queryBase = knjl580iQuery::getBaseDatData($model);
        $result = $db->query($queryBase);
        while ($base = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ENTEXAM_RECEPT_DETAIL_DAT(UPD or INS)
            //手続種別
            $procType = $model->field["PROC_TYPE"];

            //指定の手続種別が不要な受験者は除く
            $necessaryProcTypes = $model->getNecessaryProcTypes($base);
            if (!in_array($procType, $necessaryProcTypes)) {
                continue;
            }

            if (in_array($base["RECEPTNO"], $passExamNo)) {
                //手続した人の手続日は上書きしない
                $setProDiv = "1"; // 手続済
                $setProDate = ($base["PROC_TYPE{$procType}_DATE"]) ? $base["PROC_TYPE{$procType}_DATE"] : str_replace('/', '-', $model->field["PROCEDUREDATE"]); // 手続日
            } else {
                $setProDiv = ""; // 未手続
                $setProDate = "";
            }

            $query = knjl580iQuery::updateReceptDetailDatSql($db, $model, $base, $setProDiv, $setProDate);

            $db->query($query);
        }

        return;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    public function updateQueryCsv($db, $model, &$data_arr)
    {
        $cnt = 0;   //処理件数
        $queryBase = knjl580iQuery::getBaseDatData($model);
        $result = $db->query($queryBase);
        while ($base = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = array_search($base["RECEPTNO"], array_column($data_arr, "RECEPTNO"));

            //ENTEXAM_RECEPT_DETAIL_DAT(UPD or INS)
            //手続種別
            $procType = $model->field["PROC_TYPE"];

            if ($key !== false) {
                if ($data_arr[$key]["ERROR"] == "1") {
                    // エラーとなったレコードは処理しない
                    continue;
                }

                //手続した人の手続日は上書きしない
                $setProDiv = "1"; // 手続済
                $setProDate = ($base["PROC_TYPE{$procType}_DATE"]) ? $base["PROC_TYPE{$procType}_DATE"] : str_replace('/', '-', $model->field["PROCEDUREDATE"]); // 手続日
            } else {
                $setProDiv = ""; // 未手続
                $setProDate = "";
            }

            $query = knjl580iQuery::updateReceptDetailDatSql($db, $model, $base, $setProDiv, $setProDate);

            $db->query($query);

            $cnt++;
        }

        return $cnt;
    }

    //更新  ※必要な手続を全て終えた受験者を判定し、手続き終了フラグをセット
    public function getUpdateProcedureDivQuery($db, $model)
    {
        $queryBase = knjl580iQuery::getBaseDatData($model);
        $result = $db->query($queryBase);
        while ($base = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $base["EXAMNO"] = $base["RECEPTNO"]; //※城東入試ではEXAMNO = RECEPTNO

            //必要な手続きを全て満たした志願者はPROCEDUREDIVを1に、それ以外はnull
            $completeFlg = true;
            $necessaryProcTypes = $model->getNecessaryProcTypes($base);
            foreach ($necessaryProcTypes as $chkProcType) {
                $completeFlg &= ($base["PROC_TYPE{$chkProcType}_FLG"] == 1) ? true : false;
            }
            $setProDiv = ($completeFlg) ? "1" : "";

            //課程+学科の4桁コードを分割
            if (strlen($base["SUC_TOTALCD"]) == 4) {
                $base["SUC_COURSECD"] = substr($base["SUC_TOTALCD"], 0, 1);
                $base["SUC_MAJORCD"]  = substr($base["SUC_TOTALCD"], 1, 3);
            }
            $query = knjl580iQuery::updateApplicantbaseDat($db, $model, $base, $setProDiv);

            $db->query($query);
        }

        return;
    }

    //ENTEXAM_APPLICANTBASE_DAT更新用SQL ※既にBASEはあるという前提でUPDATE文のみ
    public function updateApplicantbaseDat($db, $model, $base, $setProDiv)
    {
        $data = array();
        $data["PROCEDUREDIV"][TEXT]     = $setProDiv;
        $data["ENTDIV"][TEXT]           = ($setProDiv == "1") ? "1" : ""; //全手続完了と同時に入学フラグを立てる
        $data["SUC_COURSECD"][TEXT]     = ($setProDiv == "1") ? $base["SUC_COURSECD"] : "";
        $data["SUC_MAJORCD"][TEXT]      = ($setProDiv == "1") ? $base["SUC_MAJORCD"] : "";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = "     WHERE ENTEXAMYEAR  = '{$model->examYear}' ";
        $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $where .= "       AND EXAMNO       = '{$base["EXAMNO"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

        return $query;
    }

    //ENTEXAM_RECEPT_DETAIL_DAT更新用SQL
    public function updateReceptDetailDatSql($db, $model, $base, $setProDiv, $setProDate)
    {
        //手続種別
        $procType = $model->field["PROC_TYPE"];

        $data = array();
        if ($procType == "1") {
            $data["REMARK1"][TEXT]         = $setProDiv;
            $data["REMARK2"][TEXT]         = $setProDate;
        } elseif ($procType == "2") {
            $data["REMARK3"][TEXT]         = $setProDiv;
            $data["REMARK4"][TEXT]         = $setProDate;
        } elseif ($procType == "3") {
            $data["REMARK5"][TEXT]         = $setProDiv;
            $data["REMARK6"][TEXT]         = $setProDate;
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $seq = '020';
        $reccnt = $db->getOne(knjl580iQuery::getReceptDetailCount($model, $base["RECEPTNO"], $base["EXAM_TYPE"], $seq));
        if ($reccnt > 0) {
            $where  = "     WHERE ENTEXAMYEAR  = '{$model->examYear}' ";
            $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "       AND TESTDIV      = '{$model->testdiv}' ";
            $where .= "       AND EXAM_TYPE    = '{$base["EXAM_TYPE"]}' ";
            $where .= "       AND RECEPTNO     = '{$base["RECEPTNO"]}' ";
            $where .= "       AND SEQ          = '{$seq}' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
        } else {
            $data["ENTEXAMYEAR"][TEXT]  = $model->examYear;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["TESTDIV"][TEXT]      = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]    = $base["EXAM_TYPE"];
            $data["RECEPTNO"][TEXT]     = $base["RECEPTNO"];
            $data["SEQ"][TEXT]          = $seq;
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
        }
        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //受験番号　getBaseDatDataと同じ条件
    public function checkReceptNo($model, $receptNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO       = R1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_SETTING_MST S1 ";
        $query .= "          ON S1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND S1.SETTING_CD   = 'L013' ";
        $query .= "         AND S1.NAMESPARE1   = '1' ";
        $query .= "         AND S1.SEQ          = R1.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR          = '{$model->examYear}' ";
        $query .= "     AND B1.APPLICANTDIV     = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV          = '{$model->testdiv}' ";   //入試区分
        $query .= "     AND B1.TESTDIV0         = '{$model->exam_type}' "; //学科
        $query .= "     AND VALUE(B1.ENTDIV,'') != '2' "; // 辞退者は除く
        $query .= "     AND R1.RECEPTNO         = '{$receptNo}' ";

        return $query;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }
}
