<?php

require_once('for_php7.php');

class knjl580jQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
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

    //入試種別取得
    function getTestdiv($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストtoリスト取得
    function getReceptDatData($model) {
        $query  = " SELECT ";
        $query .= "     RCPT.EXAMNO AS VALUE, ";
        $query .= "     RCPT.EXAMNO || '：' || BASE.NAME AS LABEL, ";
        $query .= "     RCPT.ENTEXAMYEAR, ";
        $query .= "     RCPT.APPLICANTDIV, ";
        $query .= "     RCPT.TESTDIV, ";
        $query .= "     RCPT.EXAM_TYPE, ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     RCPT.ADJOURNMENTDIV AS ENTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                              AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                              AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND RCPT.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND RCPT.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND RCPT.JUDGEDIV     = '1' "; // 合格者
        $query .= "     AND RCPT.PROCEDUREDIV1 = '1' "; // 手続者
        $query .= " ORDER BY ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //辞退者一覧を配列に格納
        $passExamNo = explode(",", $model->selectdata);

        $queryRecept = knjl580jQuery::getReceptDatData($model);
        $result = $db->query($queryRecept);
        while ($recept = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            if (in_array($recept["EXAMNO"], $passExamNo)) {
                $setEntDiv = "2"; // 辞退
            }else{
                $setEntDiv = "1"; // 入学
            }

            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["ADJOURNMENTDIV"][TEXT]   = $setEntDiv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
            $where .= "   AND EXAMNO        = '{$recept["EXAMNO"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //ADJOURNMENTDIV = 1 が1件でもあれば、ENTDIV = 1 なければ ENTDIV = 2 で更新
            $query  = " SELECT COUNT(*) FROM ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->year}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "   AND EXAMNO        = '{$recept["EXAMNO"]}' ";
            $query .= "   AND JUDGEDIV      = '1' ";
            $query .= "   AND PROCEDUREDIV1 = '1' ";
            $query .= "   AND ADJOURNMENTDIV = '1' ";
            $entCnt = $db->getOne($query);

            if ($entCnt == 0) {
                $setEntDiv = "2"; // 辞退
            } else {
                $setEntDiv = "1"; // 入学
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTDIV"][TEXT]           = $setEntDiv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$recept["EXAMNO"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>