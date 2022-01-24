<?php

require_once('for_php7.php');

class knjl070eQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //スライドグループデータ取得
    function getSlide($model) {
        $query  = " SELECT ";
        $query .= "     DESIREDIV, ";
        $query .= "     TESTDIV1, ";
        $query .= "     SUC_DESIREDIV,";
        $query .= "     SUC_TESTDIV1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SLIDE_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     DESIREDIV, ";
        $query .= "     TESTDIV1, ";
        $query .= "     SUC_DESIREDIV,";
        $query .= "     SUC_TESTDIV1 ";

        return $query;
    }

    //一覧表示
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.TESTDIV1, ";
        $query .= "     L045.NAME1 as TESTDIV1_NAME, ";
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     L058.NAME1 as DESIREDIV_NAME, ";
        $query .= "     RCPT.JUDGEDIV, ";
        $query .= "     DETAIL.REMARK4, ";
        $query .= "     DETAIL.REMARK5, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     DETAIL.REMARK6 as HOPE, ";
        $query .= "     S033.REMARK1 AS HOPE1, ";
        $query .= "     S033.REMARK2 AS HOPE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT RCPT ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                      AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                      AND RCPT.TESTDIV      = BASE.TESTDIV ";
        $query .= "                                      AND RCPT.EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "                                      AND RCPT.RECEPTNO     = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ";
        $query .= "         ON DETAIL.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL.EXAMNO = BASE.EXAMNO ";
        $query .= "         AND DETAIL.SEQ = '030' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S033 ";
        $query .= "          ON S033.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND S033.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND S033.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND S033.SEQ          = '033' ";
        $query .= "     LEFT JOIN V_NAME_MST L058 ON L058.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L058.NAMECD1 = 'L058' ";
        $query .= "                              AND L058.NAMECD2 = BASE.DESIREDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L045 ON L045.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L045.NAMECD1 = 'L045' ";
        $query .= "                              AND L045.NAMECD2 = BASE.TESTDIV1 ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR            = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV           = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.TESTDIV                = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //生徒個人毎の出願コース、取得
        $schInfoArr = array();
        $query = knjl070eQuery::getBaseData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schInfoArr[$row['EXAMNO']]['TESTDIV1']  = $row['TESTDIV1'];
            $schInfoArr[$row['EXAMNO']]['DESIREDIV'] = $row['DESIREDIV'];
        }

        //追加
        foreach ($model->interview as $examno => $value) {
            $data = array();
            $data["JUDGEMENT"][TEXT]        = isset($value['JUDGEMENT2']) ? $value['JUDGEMENT2'] : '';
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
            $where .= "   AND EXAMNO         = '{$examno}' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
            $query .= "   AND EXAMNO         = '{$examno}' ";
            $query .= "   AND SEQ            = '030' ";
            $detailCnt = $db->getOne($query);

            $data = array();
            $setRemark5 = ($value['JUDGEDIV'] == "1") ? $schInfoArr[$examno]['DESIREDIV']: '';
            $setRemark4 = $setRemark5 || $value['REMARK5'] ? $schInfoArr[$examno]['TESTDIV1']: '';

            $data["REMARK4"][TEXT]      = ($value['REMARK4'] != '') ? $value['REMARK4']: $setRemark4;
            $data["REMARK5"][TEXT]      = ($value['REMARK5'] != '') ? $value['REMARK5']: $setRemark5;
            $data["REMARK6"][TEXT]      = isset($value['HOPE'])?$value['HOPE']:'';
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            if ($detailCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
                $where .= "   AND EXAMNO         = '{$examno}' ";
                $where .= "   AND SEQ            = '030' ";

                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = "030";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            }
            $db->query($query);

            //ENTEXAM_RECEPT_DAT
            $receptCnt = $db->getOne(knjl070eQuery::getCntRecept($model, $examno));

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
            $data["RECEPTNO"][TEXT]         = $examno;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["JUDGEDIV"][TEXT]         = isset($value['JUDGEDIV']) ? $value['JUDGEDIV']: '';

            if ($receptCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
                $where .= "   AND EXAM_TYPE     = '{$model->exam_type}'";
                $where .= "   AND RECEPTNO      = '{$examno}'";
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //BASEカウント取得
    function getBaseData($model) {
        $query  = " SELECT ";
        $query .= "     EXAMNO, ";
        $query .= "     TESTDIV1, ";
        $query .= "     DESIREDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";

        return $query;
    }

    //receptカウント取得
    function getCntRecept($model, $examno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$model->exam_type}'";
        $query .= "     AND RECEPTNO     = '{$examno}'";

        return $query;
    }
}
?>
