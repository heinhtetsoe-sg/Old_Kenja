<?php
class knjl690hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($model, $namecd1)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR     = '" . $model->ObjYear . "' ";
        $query .= "   AND NAMECD1  = '" . $namecd1 . "' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //生徒情報
    public function getSelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV || ':' || TESTDIV.TESTDIV_NAME AS TESTDIV, ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     CLASSIFY.CLASSIFY_NAME, ";
        $query .= "     Z002.NAME1 AS SEX, ";
        $query .= "     T1.JUDGEMENT, ";
        $query .= "     T1.PROCEDUREDIV, ";
        $query .= "     T1.PROCEDUREDATE, ";
        $query .= "     T1.ENTDIV, ";
        $query .= "     BD026.REMARK1 AS STUDENTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "             ON T1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "            AND T1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "            AND T1.EXAMNO       = R1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD026 ";
        $query .= "            ON BD026.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND BD026.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND BD026.EXAMNO       = T1.EXAMNO ";
        $query .= "           AND BD026.SEQ          = '026' ";
        $query .= "     LEFT JOIN ENTEXAM_CLASSIFY_MST CLASSIFY ";
        $query .= "            ON CLASSIFY.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND CLASSIFY.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND CLASSIFY.CLASSIFY_CD  = T1.TESTDIV1 ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST TESTDIV ";
        $query .= "            ON TESTDIV.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND TESTDIV.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND TESTDIV.TESTDIV      = T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST Z002 ";
        $query .= "            ON Z002.NAMECD2 = T1.SEX ";
        $query .= "           AND Z002.NAMECD1 = 'Z002' ";
        $query .= " WHERE ";
        $query .= "       R1.ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "   AND R1.APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "   AND R1.EXAM_TYPE    = '" . $model->field["EXAM_TYPE"] . "' ";
        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query .= "   AND R1.RECEPTNO < '" . $model->field["RECEPTNO"] . "' ";
            $query.= " ORDER BY ";
            $query .= "    R1.RECEPTNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query .= "   AND R1.RECEPTNO > '" . $model->field["RECEPTNO"] . "' ";
            $query.= " ORDER BY ";
            $query .= "   R1.RECEPTNO ASC ";
        } else {
            $query .= "   AND R1.RECEPTNO = '" . $model->field["RECEPTNO"] . "' ";
        }
        return $query;
    }


    //設定マスタより取得
    public function getEntexamSettingMst($model, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE3, ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "     AND APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "     AND SETTING_CD   = '" . $settingCd . "' ";
        if ($seq) {
            $query .= "     AND SEQ          = '" . $seq . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //EXAMNOの取得
        $query  = " SELECT EXAMNO ";
        $query .= " FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "       ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "   AND APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "   AND EXAM_TYPE    = '" . $model->field["EXAM_TYPE"] . "' ";
        $query .= "   AND RECEPTNO     = '" . $model->field["RECEPTNO"] . "' ";
        //件数取得
        $examno = $db->getOne($query);

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["JUDGEMENT"][TEXT]        = $model->field["JUDGEMENT"];
        $data["PROCEDUREDIV"][TEXT]     = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]    = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        $data["ENTDIV"][TEXT]           = $model->field["ENTDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND EXAMNO        = '".$examno."' ";

        //更新
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:026
        $seq = "026";
        $data = array();
        $data["REMARK1"][TEXT]      = $model->field["STUDENTNO"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND EXAMNO        = '".$examno."' ";
        $where .= "   AND SEQ           = '".$seq."' ";

        //件数取得
        $cnt = $db->getOne(" SELECT COUNT(*) AS CNT FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
        if ($cnt == 0) {
            //追加
            $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]       = $examno;
            $data["SEQ"][TEXT]          = $seq;
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
