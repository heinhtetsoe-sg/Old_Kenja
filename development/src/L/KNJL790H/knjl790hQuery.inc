<?php

require_once('for_php7.php');

class knjl790hQuery extends Query
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

    //入試区分の取得
    public function getTestDiv($model, $applicantdiv)
    {
        $query .= " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "       ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "   AND APPLICANTDIV = '" . $applicantdiv . "' ";
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
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     KIND.KINDDIV, ";
        $query .= "     Z002.NAME1 AS SEX, ";
        $query .= "     T1.JUDGEMENT, ";
        $query .= "     T1.SUB_ORDER, ";
        $query .= "     BD033.REMARK2 AS UNPASS, ";
        $query .= "     T1.PROCEDUREDIV, ";
        $query .= "     T1.PROCEDUREDATE, ";
        $query .= "     T1.ENTDIV, ";
        $query .= "     BD026.REMARK1 AS STUDENTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ";
        $query .= "            ON BD005.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND BD005.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND BD005.EXAMNO       = T1.EXAMNO ";
        $query .= "           AND BD005.SEQ          = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD033 ";
        $query .= "            ON BD033.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND BD033.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND BD033.EXAMNO       = T1.EXAMNO ";
        $query .= "           AND BD033.SEQ          = '033' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD026 ";
        $query .= "            ON BD026.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND BD026.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND BD026.EXAMNO       = T1.EXAMNO ";
        $query .= "           AND BD026.SEQ          = '026' ";
        $query .= "     LEFT JOIN ENTEXAM_KINDDIV_MST KIND ";
        $query .= "            ON KIND.ENTEXAMYEAR  = BD005.ENTEXAMYEAR ";
        $query .= "           AND KIND.APPLICANTDIV = BD005.APPLICANTDIV ";
        $query .= "           AND KIND.KINDDIV      = BD005.REMARK1 ";
        $query .= "     LEFT JOIN NAME_MST Z002 ";
        $query .= "            ON Z002.NAMECD2 = T1.SEX ";
        $query .= "           AND Z002.NAMECD1 = 'Z002' ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "   AND T1.APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "   AND T1.TESTDIV      = '" . $model->field["TESTDIV"] . "' ";
        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query .= "   AND T1.EXAMNO < '" . $model->field["EXAMNO"] . "' ";
            $query.= " ORDER BY ";
            $query .= "    T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query .= "   AND T1.EXAMNO > '" . $model->field["EXAMNO"] . "' ";
            $query.= " ORDER BY ";
            $query .= "   T1.EXAMNO ASC ";
        } else {
            $query .= "   AND T1.EXAMNO = '" . $model->field["EXAMNO"] . "' ";
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

    //教科一覧
    public function getSUbclassList($model)
    {
        $query .= " SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     SCORE.TESTSUBCLASSCD, ";
        $query .= "     TESTSUB.TESTSUBCLASS_NAME, ";
        $query .= "     TESTSUB.REMARK2 AS SUB_TYPE, ";
        $query .= "     SCORE.ATTEND_FLG, ";
        $query .= "     SCORE.SCORE, ";
        $query .= "     RD009.REMARK4 AS SAKUBUN_AVG, "; //作文平均(推薦)
        $query .= "     CONF1.TOTAL_ALL AS NAISHIN_TOTAL, "; //内申合計(推薦)
        $query .= "     RD009.REMARK1 AS SHIKEN_TOTAL, "; //試験合計点(一般)
        $query .= "     RD009.REMARK5 AS TYOFUKU_KATEN, "; //重複加点(一般)
        $query .= "     BD031.REMARK10 AS KATEN, "; //加点
        $query .= "     RD009.REMARK6 AS TOTAL, "; //合計
        $query .= "     RECEPT.TOTAL_RANK4, "; //順位
        $query .= "     BASE.RECOM_EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "             ON RECEPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "            AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "            AND RECEPT.TESTDIV      = BASE.TESTDIV ";
        $query .= "            AND RECEPT.EXAM_TYPE    = '1' ";
        $query .= "            AND RECEPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_SCORE_DAT SCORE ";
        $query .= "             ON SCORE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "            AND SCORE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "            AND SCORE.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "            AND SCORE.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "            AND SCORE.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "     INNER JOIN ENTEXAM_TESTSUBCLASSCD_DAT TESTSUB ";
        $query .= "             ON TESTSUB.ENTEXAMYEAR    = SCORE.ENTEXAMYEAR ";
        $query .= "            AND TESTSUB.APPLICANTDIV   = SCORE.APPLICANTDIV ";
        $query .= "            AND TESTSUB.TESTDIV        = SCORE.TESTDIV ";
        $query .= "            AND TESTSUB.EXAM_TYPE      = SCORE.EXAM_TYPE ";
        $query .= "            AND TESTSUB.TESTSUBCLASSCD = SCORE.TESTSUBCLASSCD ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD009 ";
        $query .= "             ON RD009.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "            AND RD009.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "            AND RD009.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "            AND RD009.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "            AND RD009.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "            AND RD009.SEQ          = '009' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ";
        $query .= "             ON BD031.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "            AND BD031.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "            AND BD031.EXAMNO       = BASE.EXAMNO ";
        $query .= "            AND BD031.SEQ          = '031' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF1 ";
        $query .= "             ON CONF1.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "            AND CONF1.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "            AND CONF1.EXAMNO       = BASE.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "     AND BASE.APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "     AND BASE.TESTDIV      = '" . $model->field["TESTDIV"] . "' ";
        $query .= "     AND BASE.EXAMNO       = '" . $model->field["EXAMNO"] . "' ";
        $query .= " ORDER BY  ";
        $query .= "     SCORE.TESTSUBCLASSCD ";
        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_RECEPT_DAT
        $data = array();
        $data["JUDGEDIV"][TEXT]         = $model->field["JUDGEMENT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND TESTDIV       = '".$model->field["TESTDIV"]."' ";
        $where .= "   AND EXAMNO        = '".$model->field["EXAMNO"]."' ";

        //更新
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["JUDGEMENT"][TEXT]        = $model->field["JUDGEMENT"];
        $data["SUB_ORDER"][TEXT]        = $model->field["SUB_ORDER"];
        $data["PROCEDUREDIV"][TEXT]     = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]    = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        $data["ENTDIV"][TEXT]           = $model->field["ENTDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND TESTDIV       = '".$model->field["TESTDIV"]."' ";
        $where .= "   AND EXAMNO        = '".$model->field["EXAMNO"]."' ";

        //更新
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:033
        $seq = "033";
        $data = array();
        $data["REMARK2"][TEXT]      = $model->field["UNPASS"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND EXAMNO        = '".$model->field["EXAMNO"]."' ";
        $where .= "   AND SEQ           = '".$seq."' ";

        //件数取得
        $cnt = $db->getOne(" SELECT COUNT(*) AS CNT FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
        if ($cnt == 0) {
            //追加
            $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]       = $model->field["EXAMNO"];
            $data["SEQ"][TEXT]          = $seq;
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:026
        $seq = "026";
        $data = array();
        $data["REMARK1"][TEXT]      = $model->field["STUDENTNO"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND EXAMNO        = '".$model->field["EXAMNO"]."' ";
        $where .= "   AND SEQ           = '".$seq."' ";

        //件数取得
        $cnt = $db->getOne(" SELECT COUNT(*) AS CNT FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
        if ($cnt == 0) {
            //追加
            $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]       = $model->field["EXAMNO"];
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
