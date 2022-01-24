<?php
class knjl661hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試日程取得
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($testdiv) {
            $query .= "     AND TESTDIV      = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     C1.CLASSIFY_NAME, ";
        $query .= "     RD008.REMARK1 AS PLUS_POINT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_CLASSIFY_MST C1 ";
        $query .= "          ON C1.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND C1.CLASSIFY_CD  = BASE.TESTDIV1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD008 ";
        $query .= "          ON RD008.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND RD008.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND RD008.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "         AND RD008.EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "         AND RD008.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "         AND RD008.SEQ          = '008' ";
        $query .= " WHERE ";
        $query .= "         RECEPT.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RECEPT.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND RECEPT.EXAM_TYPE    = '{$model->exam_type}' ";
        //受験番号範囲
        if ($model->s_receptno) {
            $query .= "     AND RECEPT.RECEPTNO >= '{$model->s_receptno}' ";
        }
        if ($model->e_receptno) {
            $query .= "     AND RECEPT.RECEPTNO <= '{$model->e_receptno}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO ";

        return $query;
    }

    //ENTEXAM_RECEPT_DETAIL_DAT存在チェック
    public function getReceptDetailCnt($model, $receptno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $query .= "   AND EXAM_TYPE      = '{$model->exam_type}' ";
        $query .= "   AND RECEPTNO       = '{$receptno}' ";
        $query .= "   AND SEQ            = '{$seq}' ";
        return $query;
    }

    //更新
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->updField as $receptno => $value) {
            $seq = "008";

            //存在チェック
            $dataCnt = $db->getOne(knjl661hQuery::getReceptDetailCnt($model, $receptno, $seq));

            //データセット
            $data = array();
            $data["REMARK1"][TEXT]          = $value["PLUS_POINT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
                $where .= "   AND EXAM_TYPE     = '{$model->exam_type}' ";
                $where .= "   AND RECEPTNO      = '{$receptno}' ";
                $where .= "   AND SEQ           = '{$seq}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["SEQ"][TEXT]              = $seq;
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            }

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
