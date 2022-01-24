<?php
class knjl108iQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "", $nameSpare3 = "")
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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入辞区分取得
    public function getEntdiv($year, $applicantDiv, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "     SEQ AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR         = '{$year}' ";
        $query .= "     AND APPLICANTDIV    = '{$applicantDiv}' ";
        $query .= "     AND SETTING_CD      = '{$settingCd}' ";
        if ($seq) {
            $query .= " AND SEQ = '{$seq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BD022.REMARK1 AS BD022_REMARK1, "; // 一次手続きフラグ
        $query .= "     BD022.REMARK2 AS BD022_REMARK2, "; // 一次手続き日
        $query .= "     BD022.REMARK3 AS BD022_REMARK3, "; // 招集日
        $query .= "     BASE.PROCEDUREDIV, ";   // 二次手続きフラグ
        $query .= "     BASE.PROCEDUREDATE, ";  // 二次手続き日
        $query .= "     BASE.ENTDIV ";          // 入辞区分
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD022 ";
        $query .= "          ON BD022.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND BD022.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND BD022.EXAMNO = BASE.EXAMNO ";
        $query .= "         AND BD022.SEQ = '022' ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND BASE.JUDGEMENT    = '1' "; // 「1:合格」のみ対象
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->updField as $examno => $value) {
            //一次手続き　ENTEXAM_APPLICANTBASE_DETAIL_DAT(SEQ=022)
            //存在チェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "   AND EXAMNO        = '{$examno}' ";
            $query .= "   AND SEQ           = '022' ";
            $dataCnt = $db->getOne($query);
            //データセット
            $data = array();
            $data["REMARK1"][TEXT]          = $value["BD022_REMARK1"];
            $data["REMARK2"][TEXT]          = str_replace("/", "-", $value["BD022_REMARK2"]);
            $data["REMARK3"][TEXT]          = $value["BD022_REMARK3"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($dataCnt > 0) {
                //更新
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND EXAMNO        = '{$examno}' ";
                $where .= "   AND SEQ           = '022' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            } else {
                //追加
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = "022";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            }
            $db->query($query);

            //二次手続き　ENTEXAM_APPLICANTBASE_DAT
            //データセット
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = $value["PROCEDUREDIV"];
            $data["PROCEDUREDATE"][TEXT]    = str_replace("/", "-", $value["PROCEDUREDATE"]);
            $data["ENTDIV"][TEXT]           = $value["ENTDIV"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            //更新
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
