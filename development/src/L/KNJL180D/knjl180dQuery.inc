<?php

require_once('for_php7.php');

class knjl180dQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
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
    
    //名称マスタより取得(フィールド指定)
    function getNameMstOne($year, $namecd1, $namecd2, $field) {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "       YEAR    = '".$year."' ";
        $query .= "   AND NAMECD1 = '".$namecd1."' ";
        $query .= "   AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //入試区分リスト取得
    function getTestDivList($year) {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1   AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = 'L004' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //MAX_TESTDIV取得
    function getMaxTestDiv($year) {
        $query  = " SELECT ";
        $query .= "     max(TESTDIV) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= " GROUP BY ";
        $query .= "     ENTEXAMYEAR ";

        return $query;
    }

    //内部判定マスタより取得
    function getEntexamInternalDecisionMst($model) {
        $query  = " SELECT ";
        $query .= "     DECISION_CD AS VALUE, ";
        $query .= "     DECISION_CD || ':' || DECISION_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_INTERNAL_DECISION_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model, $div) {
        $query  = " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.ENTDIV, ";
        $query .= "     B1.PROCEDUREDATE, ";
        $query .= "     N4.NAME1 AS ENTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN NAME_MST N4 ";
        $query .= "          ON N4.NAMECD1      = 'L012' ";
        $query .= "         AND N4.NAMECD2      = B1.ENTDIV ";
        $query .= "     INNER JOIN NAME_MST L013 ON L013.NAMECD1    = 'L013' ";
        $query .= "                             AND L013.NAMECD2    = B1.JUDGEMENT ";
        $query .= "                             AND L013.NAMESPARE1 = '1' ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND B1.TESTDIV      = '".$model->testdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";

        return $query;
    }

    //存在チェック
    function checkEntexamApplicantbaseDetailDat($model, $seq, $examno="") {
        $query  = " SELECT ";
        if ($examno) {
            $query .= "     COUNT(*) AS COUNT ";
        } else {
            $query .= "     EXAMNO ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     SEQ             = '".$seq."' ";
        if ($examno) {
            $query .= " AND EXAMNO = '".$examno."' ";
        }

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新
        $array = explode(",", $model->hid_examno);
        foreach ($array as $key => $examno) {
            // ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTDIV"][TEXT]           = !strlen($model->data["ENTDIV"][$examno]) ? "" : $model->data["ENTDIV"][$examno];
            $data["PROCEDUREDATE"][TEXT]    = !strlen($model->data["PROCEDUREDATE"][$examno]) ? "" : str_replace("/", "-", $model->data["PROCEDUREDATE"][$examno]);
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $where .= "     EXAMNO          = '".$examno."' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //更新(ENTEXAM_APPLICANTBASE_DAT)
            knjl180dQuery::getUpdateDetail($db, $model, $examno, "007");
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }


    function selectMainQuery1 ($model) {

        $query  = " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.ENTDIV, ";
        $query .= "     B1.PROCEDUREDATE, ";
        $query .= "     N4.NAME1 AS ENTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN NAME_MST N4 ";
        $query .= "          ON N4.NAMECD1      = 'L012' ";
        $query .= "         AND N4.NAMECD2      = B1.ENTDIV ";
        $query .= "     INNER JOIN NAME_MST L013 ON L013.NAMECD1    = 'L013' ";
        $query .= "                             AND L013.NAMECD2    = B1.JUDGEMENT ";
        $query .= "                             AND L013.NAMESPARE1 = '1' ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND B1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND B1.ENTDIV IS NOT NULL ";
        $query .= "     AND B1.PROCEDUREDATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";

        return $query;

    }

    //更新 ENTEXAM_APPLICANTBASE_DAT
    function getUpdateDetail($db, &$model, $examno, $seq) {
        //データチェック
        $getCount = $db->getOne(knjl180dQuery::checkEntexamApplicantbaseDetailDat($model, $seq, $examno));

        //更新 
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["EXAMNO"][TEXT]           = $examno;
        $data["REMARK1"][TEXT]          = $model->remark1;
        $data["REMARK2"][TEXT]          = $model->remark2;
        $data["REMARK3"][TEXT]          = $model->remark3;
        $data["SEQ"][TEXT]              = $seq;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        if ($getCount == 0) {
            //追加
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            //更新
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $where .= "     EXAMNO          = '".$examno."' AND ";
            $where .= "     SEQ             = '".$seq."' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);
        return;
    }
}
?>