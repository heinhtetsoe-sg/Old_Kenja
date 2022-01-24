<?php
class knjl097iQuery extends Query {

    //名称マスタより取得 
    function getNameMst($namecd1, $year) {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if($namecd1 == "L003"){
            $query .= "     AND NAMECD2 = '1' "; //入試制度 1:中学 固定
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //ENTEXAM_SETTING_MSTより取得 
    function getEntexamSettingMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND SETTING_CD   = '".$namecd1."' ";
        if ($namecd1 == "L013") {
            $query .= "     AND SEQ <> '4' "; //合否 4:欠席 以外
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分の取得
    function getTestDiv($model) {
        $query .= " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "       ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "   T1.RECEPTNO, ";
        $query .= "   T1.EXAMNO, ";
        $query .= "   T2.NAME, ";
        $query .= "   T1.TOTAL1, ";
        $query .= "   T1.JUDGEDIV, ";
        $query .= "   L013.NAME1 AS JUDGEDIV_NAME, ";
        $query .= "   RECOM.EXAMNO AS RECOM_EXAMNO, ";
        $query .= "   RECOM_RECEPT.JUDGEDIV AS RECOM_JUDGEDIV, ";
        $query .= "   L013_2.NAME1 AS RECOM_JUDGEDIV_NAME ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT T1 ";
        $query .= "   INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "           ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "          AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "          AND T2.EXAMNO       = T1.EXAMNO ";
        if ($model->field["SEX"] == "2") {
            $query .= "          AND T2.SEX    = '1' ";
        } else if ($model->field["SEX"] == "3") {
            $query .= "          AND T2.SEX    = '2' ";
        }
        $query .= "   LEFT JOIN ENTEXAM_SETTING_MST L013 ";
        $query .= "          ON L013.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "         AND L013.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND L013.SETTING_CD   = 'L013' ";
        $query .= "         AND L013.SEQ          = T1.JUDGEDIV ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DAT RECOM ";
        $query .= "          ON RECOM.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "         AND RECOM.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND RECOM.EXAMNO       = T2.RECOM_EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_RECEPT_DAT RECOM_RECEPT ";
        $query .= "          ON RECOM_RECEPT.ENTEXAMYEAR  = RECOM.ENTEXAMYEAR ";
        $query .= "         AND RECOM_RECEPT.APPLICANTDIV = RECOM.APPLICANTDIV ";
        $where .= "         AND RECOM_RECEPT.EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        $query .= "         AND RECOM_RECEPT.EXAMNO       = RECOM.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_SETTING_MST L013_2 ";
        $query .= "          ON L013_2.ENTEXAMYEAR  = RECOM_RECEPT.ENTEXAMYEAR ";
        $query .= "         AND L013_2.APPLICANTDIV = RECOM_RECEPT.APPLICANTDIV ";
        $query .= "         AND L013_2.SETTING_CD   = 'L013' ";
        $query .= "         AND L013_2.SEQ          = RECOM_RECEPT.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $where .= "   AND T1.EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        $query .= "   AND (T1.JUDGEDIV IS NULL OR T1.JUDGEDIV <> '4') ";
        $query .= " ORDER BY ";
        if ($model->field["SORT"] == "1") {
            $query .= " VALUE(T1.TOTAL1, 0) DESC, ";
        }
        $query .= " T1.RECEPTNO ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->score as $receptno => $val) {

            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["JUDGEDIV"][TEXT]     = $model->field["JUDGEDIV"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND TESTDIV       = '".$model->field["TESTDIV"]."' ";
            $where .= "   AND EXAM_TYPE     = '".$model->field["EXAM_TYPE"]."' ";
            $where .= "   AND RECEPTNO      = '".$receptno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //ENTEXAM_RECEPT_DAT.EXAMNOを取得
            $examno = $db->getOne(" SELECT EXAMNO FROM ENTEXAM_RECEPT_DAT ".$where);

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["JUDGEMENT"][TEXT]    = $model->field["JUDGEDIV"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND EXAMNO        = '".$examno."' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>