<?php
class knjl086iQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2="", $nameSpare3="")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        if ($nameSpare3) {
            $query .= " AND NAMESPARE3 = '".$nameSpare3."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    public function getTestdivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV IN ('1', '2') "; //試験区分は「1:A方式」「2:B方式」のみ対象
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        for ($remarkNo = 1; $remarkNo <= 7; $remarkNo++) {
            $query .= "     INTERVIEW{$remarkNo}.REMARK1 AS OTHER_REMARK{$remarkNo}, ";
        }
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        for ($remarkNo = 1; $remarkNo <= 7; $remarkNo++) {
            $seq = sprintf("%03d", $remarkNo);
            $query .= "     LEFT JOIN ENTEXAM_DOCUMENT_VIEW_DAT INTERVIEW{$remarkNo} ";
            $query .= "          ON INTERVIEW{$remarkNo}.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
            $query .= "         AND INTERVIEW{$remarkNo}.APPLICANTDIV = BASE.APPLICANTDIV ";
            $query .= "         AND INTERVIEW{$remarkNo}.TESTDIV = BASE.TESTDIV ";
            $query .= "         AND INTERVIEW{$remarkNo}.EXAM_TYPE = '1' ";
            $query .= "         AND INTERVIEW{$remarkNo}.SEQ = '{$seq}' ";
            $query .= "         AND INTERVIEW{$remarkNo}.EXAMNO = BASE.EXAMNO ";
        }
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.TESTDIV        = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->interview as $examno => $value) {
            for ($remarkNo = 1; $remarkNo <= 7; $remarkNo++) {
                $seq = sprintf("%03d", $remarkNo);
                //存在チェック
                $query  = " SELECT ";
                $query .= "     COUNT(*) AS CNT ";
                $query .= " FROM ";
                $query .= "     ENTEXAM_DOCUMENT_VIEW_DAT ";
                $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $query .= "   AND TESTDIV       = '{$model->testdiv}' ";
                $query .= "   AND EXAM_TYPE     = '1' ";
                $query .= "   AND SEQ           = '{$seq}' ";
                $query .= "   AND EXAMNO        = '{$examno}' ";
                $dataCnt = $db->getOne($query);
                //データセット
                $data = array();
                $data["REMARK1"][TEXT]          = !strlen($value[$remarkNo]) ? "" : $value[$remarkNo];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";
                if ($dataCnt > 0) {
                    $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                    $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                    $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
                    $where .= "   AND EXAM_TYPE     = '1' ";
                    $where .= "   AND SEQ           = '{$seq}' ";
                    $where .= "   AND EXAMNO        = '{$examno}' ";
                    //更新
                    $query = Query::updateSQL($data, "ENTEXAM_DOCUMENT_VIEW_DAT", $where);
                } else {
                    $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                    $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                    $data["TESTDIV"][TEXT]          = $model->testdiv;
                    $data["EXAM_TYPE"][TEXT]        = "1";
                    $data["SEQ"][TEXT]              = $seq;
                    $data["EXAMNO"][TEXT]           = $examno;
                    //追加
                    $query = Query::insertSQL($data, "ENTEXAM_DOCUMENT_VIEW_DAT");
                }
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
