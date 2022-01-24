<?php

require_once('for_php7.php');

class knjl532jQuery extends Query {
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

    //学校種別名称取得
    function getApplicantDivName($model) {
        $query  = " SELECT ";
        $query .= "   NAME1 AS LABEL, ";
        $query .= "   NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "   NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = 'L003' ";
        $query .= "   AND NAMECD2 = '{$model->applicantdiv}' ";
        return $query;
    }

    //入試種別
    function getTestdiv($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試方式取得
    function getExamType($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "   EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL, ";
        $query .= "   EXAM_TYPE AS VALUE ";
        $query .= " FROM ";
        $query .= "  ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "  ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "  AND APPLICANTDIV = '".$model->applicantdiv."' ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model, $force_next = false, $getalldat = false) {
        $getRowVal = 50;
        if (!$force_next && $model->cmd == "back") {
            $query .= " WITH SEARCH_FST_RECORD AS ( ";
            $query .= " SELECT RECEPTNO FROM ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ";
            $query .= "    ENTEXAMYEAR  = '{$model->ObjYear}' ";
            $query .= "    AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "    AND TESTDIV      = '{$model->testdiv}' ";
            $query .= "    AND '{$model->s_receptno}' > RECEPTNO ";
            $query .= " ORDER BY ";
            $query .= "    RECEPTNO DESC ";
            $query .= "    FETCH FIRST {$getRowVal} ROWS ONLY ";
            $query .= " ), GET_MINRECEPT AS ( ";
            $query .= " SELECT ";
            $query .= "   CASE WHEN MIN(SC1.RECEPTNO) IS NULL THEN '{$model->s_receptno}' ELSE MIN(SC1.RECEPTNO) END AS MIN_RCPT ";
            $query .= " FROM ";
            $query .= "   SEARCH_FST_RECORD SC1 ";
            $query .= "     ) ";
        }
        $query .= " select ";
        $query .= "  T1.RECEPTNO, ";
        $query .= "  T2.NAME, ";
        $query .= "  T2.NAME_KANA, ";
        $query .= "  T3.REMARK1 AS ORDERNO ";
        $query .= " FROM ";
        $query .= "  ENTEXAM_RECEPT_DAT T1 ";
        $query .= "  LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "  LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T3 ";
        $query .= "    ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND T3.TESTDIV = T1.TESTDIV ";
        $query .= "   AND T3.RECEPTNO = T1.RECEPTNO ";
        $query .= "   AND T3.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "   AND T3.SEQ = '009' ";
        if (!$force_next && $model->cmd == "back") {
            $query .= " , GET_MINRECEPT GM1 ";
        }
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV = '{$model->testdiv}' ";
        $query .= "   AND T1.EXAM_TYPE = '{$model->examtype}' ";
        if ($force_next || $model->cmd == "next") {
            if ($model->chg_srchnoflg) {
                $query .= "         AND T1.RECEPTNO >= '{$model->s_receptno}' ";
            } else {
                $query .= "         AND T1.RECEPTNO > '{$model->e_receptno}' ";
            }
        }
        if (!$force_next && $model->cmd == "back") {
            //前のデータの場合は、"前の先頭データ"を利用するので、必ず"それ"以上を検索する。
            $query .= "         AND T1.RECEPTNO >= GM1.MIN_RCPT ";
        }
        $query .= " ORDER BY ";
        $query .= "    RECEPTNO ASC ";
        if ($getalldat === false) {
            $query .= "    FETCH FIRST {$getRowVal} ROWS ONLY ";
        }
        return $query;
    }

    function getReceptDetailCount($model, $receptno, $seq="") {
        $tblname = "ENTEXAM_RECEPT_DETAIL_DAT";

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$tblname} ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE = '{$model->examtype}' ";
        $query .= "     AND RECEPTNO = '{$receptno}' ";
        if ($seq != "") {
            $query .= "     AND SEQ = '{$seq}' ";
        }
        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->orderno as $receptno => $val) {
            $data = array();
            $addzerostr = "000";
            $addzlen = strlen($addzerostr);
            $recaddstr = $val == "" ? $val : substr($addzerostr.$val, strlen($addzerostr.$val)-$addzlen);
            $data["REMARK1"][TEXT]    = $recaddstr;
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "SYSDATE()";

            $reccnt = $db->getOne(knjl532jQuery::getReceptDetailCount($model, $receptno, "009"));
            if ($reccnt > 0) {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
                $where .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $where .= "     AND TESTDIV = '{$model->testdiv}' ";
                $where .= "     AND EXAM_TYPE = '{$model->examtype}' ";
                $where .= "     AND RECEPTNO = '{$receptno}' ";
                $where .= "     AND SEQ = '009' ";
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
                $db->query($query);
            } else {
                $data["ENTEXAMYEAR"][TEXT]    = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]    = $model->applicantdiv;
                $data["TESTDIV"][TEXT]    = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]    = $model->examtype;
                $data["RECEPTNO"][TEXT]    = $receptno;
                $data["SEQ"][TEXT]    = "009";
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
                $db->query($query);
            }

        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

}
?>