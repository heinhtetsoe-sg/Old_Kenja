<?php

require_once('for_php7.php');

class knjl281gQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B2.REMARK1 AS SCHREGNO, ";
        //手続き日付(日付変換)
        $query .= "     CASE WHEN B1.PROCEDUREDATE IS NULL THEN '' ";
        $query .= "          ELSE REPLACE(CAST(B1.PROCEDUREDATE AS VARCHAR(10)), '-', '/') ";
        $query .= "     END AS PROCEDUREDATE, ";
        $query .= "     CASE WHEN B1.PROCEDUREDIV = '1' AND B1.ENTDIV = '1' ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS TEXT_OPEN ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B2 ";
        $query .= "          ON B2.ENTEXAMYEAR      = B1.ENTEXAMYEAR ";
        $query .= "         AND B2.APPLICANTDIV     = B1.APPLICANTDIV ";
        $query .= "         AND B2.EXAMNO           = B1.EXAMNO ";
        $query .= "         AND B2.SEQ              = '026' ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "          ON L013.YEAR         = B1.ENTEXAMYEAR ";
        $query .= "         AND L013.NAMECD1      = 'L013' ";
        $query .= "         AND L013.NAMECD2      = B1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND L013.NAMESPARE1 = '1' ";
        if ($model->testPattern == "1") {
            $query .= "     AND B1.TESTDIV NOT IN ('31', '32', '33') ";
        }
        if ($model->testPattern == "2") {
            $query .= "     AND B1.TESTDIV = '31' ";
        }
        if ($model->testPattern == "3") {
            $query .= "     AND B1.TESTDIV = '32' ";
        }
        if ($model->testPattern == "4") {
            $query .= "     AND B1.TESTDIV = '33' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CASE WHEN B1.PROCEDUREDATE IS NOT NULL ";
        $query .= "          THEN '0' ";
        $query .= "          ELSE '1' ";
        $query .= "     END, ";
        $query .= "     B1.PROCEDUREDATE, ";
        $query .= "     B1.EXAMNO ";

        return $query;
    }

    //別対象者の学籍番号
    function selectOtherSchregQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     B2.REMARK1 AS SCHREGNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B2 ";
        $query .= "          ON B2.ENTEXAMYEAR      = B1.ENTEXAMYEAR ";
        $query .= "         AND B2.APPLICANTDIV     = B1.APPLICANTDIV ";
        $query .= "         AND B2.EXAMNO           = B1.EXAMNO ";
        $query .= "         AND B2.SEQ              = '026' ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND B2.REMARK1 IS NOT NULL ";
        if ($model->testPattern == "1") {
            $query .= "     AND B1.TESTDIV IN ('31', '32', '33') ";
        }
        if ($model->testPattern == "2") {
            $query .= "     AND VALUE(B1.TESTDIV, '') <> '31' ";
        }
        if ($model->testPattern == "3") {
            $query .= "     AND VALUE(B1.TESTDIV, '') <> '32' ";
        }
        if ($model->testPattern == "4") {
            $query .= "     AND VALUE(B1.TESTDIV, '') <> '33' ";
        }
        $query .= " ORDER BY ";
        $query .= "     B2.REMARK1 ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $seq = "026";
        foreach ($model->schregno as $examno => $schregno) {
            $setSchregNo = $schregno ? $model->ObjYear.$schregno : NULL;
            //学籍番号
            $data = array();
            $data["REMARK1"][TEXT]              = $setSchregNo;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            //WHERE
            $where  = " WHERE ";
            $where .= "         ENTEXAMYEAR     = '{$model->ObjYear}' ";
            $where .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
            $where .= "     AND EXAMNO          = '{$examno}' ";
            $where .= "     AND SEQ             = '{$seq}' ";
            //COUNT
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $getCount = $db->getOne($query.$where);
            if ($getCount == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = $seq;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>