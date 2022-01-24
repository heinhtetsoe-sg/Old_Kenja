<?php
class knjl520iQuery extends Query
{
    //設定マスタより取得
    public function getSettingMst($model, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '" . $model->examYear . "' ";
        $query .= "     AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
        $query .= "     AND SETTING_CD   = '" . $settingCd . "' ";
        if ($seq) {
            $query .= "     AND SEQ          = '" . $seq . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //入試区分マスタ取得
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($testdiv) {
            $query .= "     AND TESTDIV      = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    public function selectQuery($model)
    {
        //学科名
        $gakkaName = $model->examTypeList[$model->exam_type];

        $query  = "";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        $query .= "     CONFD001.REMARK1 AS KYOKA1_01, ";
        $query .= "     CONFD001.REMARK2 AS KYOKA1_02, ";
        $query .= "     CONFD001.REMARK3 AS KYOKA1_03, ";
        $query .= "     CONFD001.REMARK4 AS KYOKA1_04, ";
        $query .= "     CONFD001.REMARK5 AS KYOKA1_05, ";
        $query .= "     CONFD001.REMARK6 AS KYOKA1_06, ";
        $query .= "     CONFD001.REMARK7 AS KYOKA1_07, ";
        $query .= "     CONFD001.REMARK8 AS KYOKA1_08, ";
        $query .= "     CONFD001.REMARK9 AS KYOKA1_09, ";
        $query .= "     CONFD001.REMARK10 AS TOTAL_ALL1, ";
        $query .= "     CONFD001.REMARK11 AS AVERAGE_ALL1, ";
        $query .= "     CONFD002.REMARK1 AS KYOKA2_01, ";
        $query .= "     CONFD002.REMARK2 AS KYOKA2_02, ";
        $query .= "     CONFD002.REMARK3 AS KYOKA2_03, ";
        $query .= "     CONFD002.REMARK4 AS KYOKA2_04, ";
        $query .= "     CONFD002.REMARK5 AS KYOKA2_05, ";
        $query .= "     CONFD002.REMARK6 AS KYOKA2_06, ";
        $query .= "     CONFD002.REMARK7 AS KYOKA2_07, ";
        $query .= "     CONFD002.REMARK8 AS KYOKA2_08, ";
        $query .= "     CONFD002.REMARK9 AS KYOKA2_09, ";
        $query .= "     CONFD002.REMARK10 AS TOTAL_ALL2, ";
        $query .= "     CONFD002.REMARK11 AS AVERAGE_ALL2, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT01 AS KYOKA3_01, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT02 AS KYOKA3_02, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT03 AS KYOKA3_03, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT04 AS KYOKA3_04, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT05 AS KYOKA3_05, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT06 AS KYOKA3_06, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT07 AS KYOKA3_07, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT08 AS KYOKA3_08, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT09 AS KYOKA3_09, ";
        $query .= "     CONF1.TOTAL_ALL AS TOTAL_ALL3, ";
        $query .= "     CONF1.AVERAGE_ALL AS AVERAGE_ALL3, ";
        $query .= "     CONF1.ABSENCE_DAYS AS KESSEKI1, ";
        $query .= "     CONF1.ABSENCE_DAYS2 AS KESSEKI2, ";
        $query .= "     CONF1.ABSENCE_DAYS3 AS KESSEKI3 ";
        if ($model->cmd == "csvOutput") {
            $query .= "     , B1.TESTDIV0 ";
            $query .= "     , '".$gakkaName."' AS GAKKA_NAME ";
            $query .= "     , B1.TESTDIV ";
            $query .= "     , T1.TESTDIV_NAME ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF1 ";
        $query .= "         ON CONF1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "        AND CONF1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "        AND CONF1.EXAMNO       = B1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFD001 ";
        $query .= "         ON CONFD001.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "        AND CONFD001.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "        AND CONFD001.EXAMNO       = B1.EXAMNO ";
        $query .= "        AND CONFD001.SEQ          = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFD002 ";
        $query .= "         ON CONFD002.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "        AND CONFD002.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "        AND CONFD002.EXAMNO       = B1.EXAMNO ";
        $query .= "        AND CONFD002.SEQ          = '002' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ";
        $query .= "         ON F1.FINSCHOOLCD  = B1.FS_CD ";
        if ($model->cmd == "csvOutput") {
            $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST T1 ";
            $query .= "             ON T1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
            $query .= "            AND T1.APPLICANTDIV = B1.APPLICANTDIV ";
            $query .= "            AND T1.TESTDIV      = B1.TESTDIV ";
        }
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR      = '" . $model->examYear . "' ";
        $query .= "     AND B1.APPLICANTDIV = '" . $model->applicantdiv . "' ";
        $query .= "     AND B1.TESTDIV      = '" . $model->testdiv . "' ";    //入試区分
        $query .= "     AND B1.TESTDIV0     = '" . $model->exam_type . "' ";  //学科
        if ($model->cmd == "read") {
            if ($model->s_examno != "") {
                $query .= "  AND B1.EXAMNO >= '".$model->s_examno."' ";
            }
            if ($model->e_examno != "") {
                $query .= "  AND B1.EXAMNO <= '".$model->e_examno."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";

        return $query;
    }

    public function getConfRptCount($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        return $query;
    }

    public function getConfRptDetailCount($model, $examno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        $query .= "     AND SEQ = '{$seq}' ";

        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $tmp = explode(",", $model->hid_examno);
        $examno_array = array();
        foreach ($tmp as $key => $examno_count) {
            $examno_array = explode("-", $examno_count);
            $examno  = $examno_array[0];
            $recexam = $model->recordarry[$examno_array[1]];

            //ENTEXAM_APPLICANTCONFRPT_DAT(UPD or INS)
            $reccnt = $db->getOne(knjl520iQuery::getConfRptCount($model, $examno));
            $data = array();

            foreach ($this->kyokaSeqArray as $kyokaseq) {
                $data["CONFIDENTIAL_RPT{$kyokaseq}"][NUMBER] = $recexam["kyoka3_{$kyokaseq}"];
            }
            $data["TOTAL_ALL"][NUMBER]      = !($recexam["total_all3"]) ? 0 : $recexam["total_all3"];
            $data["AVERAGE_ALL"][NUMBER]    = !($recexam["average_all3"]) ? 0.0 : $recexam["average_all3"];
            $data["ABSENCE_DAYS"][NUMBER]   = $recexam["kesseki1"];
            $data["ABSENCE_DAYS2"][NUMBER]  = $recexam["kesseki2"];
            $data["ABSENCE_DAYS3"][NUMBER]  = $recexam["kesseki3"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '" . $model->examYear . "' ";
                $where .= "       AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
                $where .= "       AND EXAMNO       = '" . $examno . "' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->examYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            }

            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT(UPD or INS) 001
            $seq = '001';
            $reccnt = $db->getOne(knjl520iQuery::getConfRptDetailCount($model, $examno, $seq));
            $data = array();
            $tmpCnt = 1;
            foreach ($this->kyokaSeqArray as $kyokaseq) {
                $data["REMARK{$tmpCnt}"][TEXT]          = $recexam["kyoka1_{$kyokaseq}"];
                $tmpCnt++;
            }
            $data["REMARK10"][TEXT]         = !($recexam["total_all1"]) ? "0" : $recexam["total_all1"];
            $data["REMARK11"][TEXT]         = !($recexam["average_all1"]) ? "0.0" : $recexam["average_all1"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '" . $model->examYear . "' ";
                $where .= "       AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
                $where .= "       AND EXAMNO       = '" . $examno . "' ";
                $where .= "       AND SEQ          = '" . $seq . "' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->examYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = $seq;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            }

            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT(UPD or INS) 002
            $seq = '002';
            $reccnt = $db->getOne(knjl520iQuery::getConfRptDetailCount($model, $examno, $seq));
            $data = array();
            $tmpCnt = 1;
            foreach ($this->kyokaSeqArray as $kyokaseq) {
                $data["REMARK{$tmpCnt}"][TEXT]          = $recexam["kyoka2_{$kyokaseq}"];
                $tmpCnt++;
            }
            $data["REMARK10"][TEXT]         = !($recexam["total_all2"]) ? "0" : $recexam["total_all2"];
            $data["REMARK11"][TEXT]         = !($recexam["average_all2"]) ? "0.0" : $recexam["average_all2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '" . $model->examYear . "' ";
                $where .= "       AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
                $where .= "       AND EXAMNO       = '" . $examno . "' ";
                $where .= "       AND SEQ          = '" . $seq . "' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->examYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = $seq;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            }

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            $examno = $data_arr[$i]["EXAMNO"];

            //ENTEXAM_APPLICANTCONFRPT_DAT(UPD or INS)
            $reccnt = $db->getOne(knjl520iQuery::getConfRptCount($model, $examno));
            $data = array();

            $total_all3 = 0;
            $average_all3 = "0.0";
            $kyokaCnt = count($model->kyokaSeqArray);
            foreach ($model->kyokaSeqArray as $kyokaseq) {
                $data["CONFIDENTIAL_RPT{$kyokaseq}"][NUMBER] = $data_arr[$i]["KYOKA3_{$kyokaseq}"];
                //合計
                $total_all3 += (int)$data_arr[$i]["KYOKA3_{$kyokaseq}"];
            }
            if ($total_all3 != 0) {
                //平均
                $average_all3 = sprintf("%.1f", round($total_all3 / $kyokaCnt, 1));
            }
            $data["TOTAL_ALL"][NUMBER]      = $total_all3;
            $data["AVERAGE_ALL"][NUMBER]    = $average_all3;
            $data["ABSENCE_DAYS"][NUMBER]   = $data_arr[$i]["KESSEKI1"];
            $data["ABSENCE_DAYS2"][NUMBER]  = $data_arr[$i]["KESSEKI2"];
            $data["ABSENCE_DAYS3"][NUMBER]  = $data_arr[$i]["KESSEKI3"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '" . $model->examYear . "' ";
                $where .= "       AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
                $where .= "       AND EXAMNO       = '" . $examno . "' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->examYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            }

            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT(UPD or INS) 001
            $seq = '001';
            $reccnt = $db->getOne(knjl520iQuery::getConfRptDetailCount($model, $examno, $seq));
            $data = array();
            $tmpCnt = 1;
            $total_all1 = 0;
            $average_all1 = "0.0";
            foreach ($this->kyokaSeqArray as $kyokaseq) {
                $data["REMARK{$tmpCnt}"][TEXT]          = $data_arr[$i]["KYOKA1_{$kyokaseq}"];
                //合計
                $total_all1 += (int)$data_arr[$i]["KYOKA1_{$kyokaseq}"];
                $tmpCnt++;
            }
            if ($total_all1 != 0) {
                //平均
                $average_all1 = sprintf("%.1f", round($total_all1 / $kyokaCnt, 1));
            }
            $data["REMARK10"][TEXT]         = $total_all1;
            $data["REMARK11"][TEXT]         = $average_all1;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '" . $model->examYear . "' ";
                $where .= "       AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
                $where .= "       AND EXAMNO       = '" . $examno . "' ";
                $where .= "       AND SEQ          = '" . $seq . "' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->examYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = $seq;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            }

            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT(UPD or INS) 002
            $seq = '002';
            $reccnt = $db->getOne(knjl520iQuery::getConfRptDetailCount($model, $examno, $seq));
            $data = array();
            $tmpCnt = 1;
            $total_all2 = 0;
            $average_all2 = "0.0";
            foreach ($this->kyokaSeqArray as $kyokaseq) {
                $data["REMARK{$tmpCnt}"][TEXT]          = $data_arr[$i]["KYOKA2_{$kyokaseq}"];
                //合計
                $total_all2 += (int)$data_arr[$i]["KYOKA2_{$kyokaseq}"];
                $tmpCnt++;
            }
            if ($total_all2 != 0) {
                //平均
                $average_all2 = sprintf("%.1f", round($total_all2 / $kyokaCnt, 1));
            }
            $data["REMARK10"][TEXT]         = $total_all2;
            $data["REMARK11"][TEXT]         = $average_all2;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '" . $model->examYear . "' ";
                $where .= "       AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
                $where .= "       AND EXAMNO       = '" . $examno . "' ";
                $where .= "       AND SEQ          = '" . $seq . "' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->examYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = $seq;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
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

    //受験番号
    public function checkExamNo($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR       = '{$model->examYear}' ";
        $query .= "     AND B1.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV       = '{$model->testdiv}' ";    //入試区分
        $query .= "     AND B1.TESTDIV0      = '{$model->exam_type}' ";  //学科
        $query .= "     AND B1.EXAMNO        = '{$examno}' ";

        return $query;
    }
}
