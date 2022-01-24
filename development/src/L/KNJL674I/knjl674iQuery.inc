<?php

require_once('for_php7.php');

class knjl674iQuery extends Query
{
    //特待コード一覧取得
    public function getHonordivQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     GENERAL_CD || ':' || GENERAL_NAME AS LABEL, ";
        $query .= "     GENERAL_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->entexamyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND GENERAL_DIV  = '{$model->generalDivHonorcd}' "; // '04'：特待コード
        $query .= " ORDER BY ";
        $query .= "     VALUE(GENERAL_CD, '') ";

        return $query;
    }

    //特待理由コード一覧取得
    public function getHonorReasondivQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     GENERAL_CD || ':' || GENERAL_NAME AS LABEL, ";
        $query .= "     GENERAL_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->entexamyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND GENERAL_DIV  = '{$model->generalDivHonorReason}' "; // '05'：特待理由コード
        $query .= " ORDER BY ";
        $query .= "     VALUE(GENERAL_CD, '') ";

        return $query;
    }

    //出身学校取得
    public function getFinshoolMst()
    {
        $query  = " SELECT ";
        $query .= "     FINSCHOOLCD AS VALUE, ";
        $query .= "     FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校マスタよりデータを取得
    public function getFinschoolName($cd)
    {
        $query  = " SELECT ";
        $query .= "     T1.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOLCD = '".$cd."' ";

        return $query;
    }

    //一覧取得
    public function getSelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     ADVICE.EXAMNO AS VALUE, ";
        $query .= "     ADVICE.EXAMNO || '：' || ADVICE.NAME || '：' || VALUE(FIN.FINSCHOOL_NAME,'') || '：' || ";
        $query .= "     CASE WHEN (D004.REMARK2 IS NOT NULL AND G04.GENERAL_NAME IS NULL) ";
        $query .= "         THEN ' ' "; //特待コードが特待コードマスタのコードに一致しない場合は空白を表示
        $query .= "         ELSE VALUE(G04.GENERAL_NAME,'-') "; //特待コード名称（特待コードがNULLの場合は「-」）を表示
        $query .= "     END ";
        $query .= "     || '：' || ";
        $query .= "     CASE WHEN (D004.REMARK1 IS NOT NULL AND G05.GENERAL_NAME IS NULL) ";
        $query .= "         THEN ' ' "; //特待理由が特待理由マスタのコードに一致しない場合は空白を表示
        $query .= "         ELSE VALUE(G05.GENERAL_NAME,'-') "; //特待理由名称（特待理由がNULLの場合は「-」）を表示
        $query .= "     END ";
        $query .= "     AS LABEL ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DAT ADVICE ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "            ON FIN.FINSCHOOLCD = ADVICE.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT D004 ";
        $query .= "             ON D004.ENTEXAMYEAR = ADVICE.ENTEXAMYEAR ";
        $query .= "            AND D004.EXAMNO      = ADVICE.EXAMNO ";
        $query .= "            AND D004.SEQ         = '004' ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST G04  ";
        $query .= "             ON G04.ENTEXAMYEAR  = ADVICE.ENTEXAMYEAR  ";
        $query .= "            AND G04.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "            AND G04.TESTDIV      = '{$model->testdiv}' ";
        $query .= "            AND G04.GENERAL_DIV  = '{$model->generalDivHonorcd}' "; // '04'：特待コード
        $query .= "            AND G04.GENERAL_CD   = D004.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST G05  ";
        $query .= "             ON G05.ENTEXAMYEAR  = ADVICE.ENTEXAMYEAR  ";
        $query .= "            AND G05.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "            AND G05.TESTDIV      = '{$model->testdiv}' ";
        $query .= "            AND G05.GENERAL_DIV  = '{$model->generalDivHonorReason}' "; // '05'：特待理由コード
        $query .= "            AND G05.GENERAL_CD   = D004.REMARK1 ";
        $query .= " WHERE ";
        $query .= "     ADVICE.ENTEXAMYEAR = '".$model->entexamyear."' ";
        if ($model->field["FINSCHOOLCD"] != "") {
            $query .= "     AND ADVICE.FS_CD = '".$model->field["FINSCHOOLCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ADVICE.EXAMNO ";

        return $query;
    }

    //特待生取得
    public function getSelectQueryLeft($model)
    {
        $query  = " SELECT ";
        $query .= "     ADVICE.EXAMNO, ";
        $query .= "     ADVICE.NAME, ";
        $query .= "     FIN.FINSCHOOL_NAME ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DAT ADVICE ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "            ON FIN.FINSCHOOLCD = ADVICE.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT D004 ";
        $query .= "             ON D004.ENTEXAMYEAR = ADVICE.ENTEXAMYEAR ";
        $query .= "            AND D004.EXAMNO      = ADVICE.EXAMNO ";
        $query .= "            AND D004.SEQ         = '004' ";
        $query .= " WHERE ";
        $query .= "     ADVICE.ENTEXAMYEAR = '".$model->entexamyear."' ";
        if ($model->field["FINSCHOOLCD"] != "") {
            $query .= "     AND ADVICE.FS_CD = '".$model->field["FINSCHOOLCD"]."' ";
        }
        if ($model->field["HONOR_REASONDIV"] == "") {
            $query .= "     AND D004.REMARK1 IS NULL ";
        } else {
            $query .= "     AND D004.REMARK1 = '".$model->field["HONOR_REASONDIV"]."' ";
        }
        if ($model->field["HONORDIV"] == "") {
            $query .= "     AND D004.REMARK2 IS NULL ";
        } else {
            $query .= "     AND D004.REMARK2 = '".$model->field["HONORDIV"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ADVICE.EXAMNO ";

        return $query;
    }

    //更新
    public function getUpdateQuery(&$model, $flg)
    {
        //リストが空の場合
        if ($flg == "left" && !$model->selectLeft) {
            return false;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $selectSchArr = array();

        if ($flg == "left") {
            //左側リスト
            $remark1 = $model->field["UPD_HONOR_REASONDIV"];
            $remark2 = $model->field["UPD_HONORDIV"];
            $selectSchArr = explode(",", $model->selectLeft);
        }

        foreach ($selectSchArr as $examno) {
            //存在チェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR  = '".$model->entexamyear."' ";
            $query .= "     AND EXAMNO   = '".$examno."' ";
            $query .= "     AND SEQ      = '004' ";
            $dataCnt = $db->getOne($query);

            $data = array();
            $data["REMARK1"][TEXT]      = $remark1;
            $data["REMARK2"][TEXT]      = $remark2;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if ($dataCnt > 0) {
                $where  = " WHERE ";
                $where .= "   ENTEXAMYEAR  = '".$model->entexamyear."' ";
                $where .= "   AND EXAMNO   = '".$examno."' ";
                $where .= "   AND SEQ      = '004' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->entexamyear;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = '004';
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
