<?php

require_once('for_php7.php');

class knjl521jQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
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

    //入試種別
    function getTestdiv($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試方式取得
    function getExamType($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "   EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL, ";
        $query .= "   EXAM_TYPE AS VALUE ";
        $query .= " FROM ";
        $query .= "  ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "  ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "  AND APPLICANTDIV = '".$model->applicantdiv."' ";
        return $query;
    }

    function SelectQuery($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T4_003.REMARK1 AS NAITEI, ";
        $query .= "     T3.TOTAL_ALL AS NAISINTEN, ";
        $query .= "     T3.ABSENCE_DAYS3 AS KESSEKI, ";
        $query .= "     T4_004.REMARK1 AS CHK_A, ";
        $query .= "     T4_004.REMARK2 AS CHK_F, ";
        $query .= "     T4_004.REMARK3 AS CHK_T, ";
        $query .= "     T4_004.REMARK4 AS CHK_B, ";
        $query .= "     T4_004.REMARK5 AS CHK_J, ";
        $query .= "     T3.REMARK1 AS REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ";
        $query .= "       ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T3.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4_003 ";
        $query .= "       ON T4_003.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T4_003.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T4_003.EXAMNO = T1.EXAMNO ";
        $query .= "      AND T4_003.SEQ = '003' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4_004 ";
        $query .= "       ON T4_004.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T4_004.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T4_004.EXAMNO = T1.EXAMNO ";
        $query .= "      AND T4_004.SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "  T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "  AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "  AND T1.TESTDIV = '".$model->testdiv."' ";
        $query .= "  AND T1.EXAM_TYPE = '".$model->examtype."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECEPTNO ";
        return $query;
    }

    function getConfRptCount($model, $examno, $seq="") {
        $tblname = "ENTEXAM_APPLICANTCONFRPT_DAT";
        if ($seq != "") {
            $tblname = "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT";
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$tblname} ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        if ($seq != "") {
            $query .= "     AND SEQ = '{$seq}' ";
        }
        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $tmp = explode(",", $model->hid_receptno);
        $receptno_array = array();
        $recno = 0;
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            $receptno   = $receptno_array[0];
            $examno     = $receptno_array[2];
            $recptr = $model->recordarry[$receptno_array[1]];

            //ENTEXAM_APPLICANTCONFRPT_DAT(UPD or INS)
            $reccnt = $db->getOne(knjl521jQuery::getConfRptCount($model, $examno));
            $data = array();
            $data["TOTAL_ALL"][NUMBER] = $recptr["naisinten_value"];
            $data["ABSENCE_DAYS3"][NUMBER] = $recptr["kesseki_value"];
            $data["REMARK1"][TEXT] = $recptr["remark"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
                $where .= "       AND APPLICANTDIV = '".$model->applicantdiv."' ";
                $where .= "       AND EXAMNO       = '".$examno."' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
                $db->query($query);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
                $db->query($query);
            }

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT_SEQ003(UPD or INS)
            $reccnt = $db->getOne(knjl521jQuery::getConfRptCount($model, $examno, "003"));
            $data = array();
            $data["REMARK1"][TEXT] = $recptr["naitei_chk"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
                $where .= "       AND APPLICANTDIV = '".$model->applicantdiv."' ";
                $where .= "       AND EXAMNO       = '".$examno."' ";
                $where .= "       AND SEQ          = '003' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
                $db->query($query);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = "003";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
                $db->query($query);
            }

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT_SEQ004(UPD or INS)
            $reccnt = $db->getOne(knjl521jQuery::getConfRptCount($model, $examno, "004"));
            $data = array();
            $data["REMARK1"][TEXT] = $recptr["chk_a"];
            $data["REMARK2"][TEXT] = $recptr["chk_f"];
            $data["REMARK3"][TEXT] = $recptr["chk_t"];
            $data["REMARK4"][TEXT] = $recptr["chk_b"];
            $data["REMARK5"][TEXT] = $recptr["chk_j"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
                $where .= "       AND APPLICANTDIV = '".$model->applicantdiv."' ";
                $where .= "       AND EXAMNO       = '".$examno."' ";
                $where .= "       AND SEQ          = '004' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
                $db->query($query);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = "004";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>