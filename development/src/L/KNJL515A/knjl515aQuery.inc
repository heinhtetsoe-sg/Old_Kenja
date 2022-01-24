<?php

require_once('for_php7.php');

class knjl515aQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        //推薦入試は表示しない
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 != '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    
    function getHopeCourseCd($model) {
        $query  = " SELECT ";
        $query .= "   T1.HOPE_COURSECODE || ':' || T2.HOPE_NAME AS LABEL, ";  // 志望区分コード
        $query .= "   T1.HOPE_COURSECODE AS VALUE ";                          // 志望区分名称
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_YDAT T1 ";
        $query .= "   LEFT JOIN ENTEXAM_HOPE_COURSE_MST T2 ";
        $query .= "     ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= " ORDER BY ";
        $query .= "  T1.HOPE_COURSECODE ";

        return $query;
    }

    //最初のEXAMNOを取得
    function SelectFstExamno($model) {
        $query  = " WITH MAXCHKTBL AS ( ";
        $query .= "   SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T1.EXAMNO) AS RENBAN, ";
        $query .= "     T1.EXAMNO ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.EXAMNO = T1.EXAMNO ";
        if ($model->hope_coursecode != $model->CONST_SELALL) {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ";
            $query .= "             ON DETAIL.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "            AND DETAIL.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "            AND DETAIL.EXAMNO       = T1.EXAMNO ";
            $query .= "            AND DETAIL.SEQ          = '001' ";
            $query .= "            AND DETAIL.REMARK10     = '{$model->hope_coursecode}' ";
        }
        $query .= "   WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV = '{$model->testdiv}' ";
        $query .= "   ORDER BY ";
        $query .= "     RENBAN ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   TX.EXAMNO ";
        $query .= " FROM ";
        $query .= "   MAXCHKTBL TX ";
        $query .= " ORDER BY ";
        $query .= "   TX.EXAMNO ";
        $query .= " FETCH FIRST ROWS ONLY ";
        return $query;
    }

    //最後のEXAMNOを取得
    function SelectLstExamno($model) {
        $query  = " WITH MAXCHKTBL AS ( ";
        $query .= "   SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T1.EXAMNO) AS RENBAN, ";
        $query .= "     T1.EXAMNO ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.EXAMNO = T1.EXAMNO ";
        if ($model->hope_coursecode != $model->CONST_SELALL) {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ";
            $query .= "             ON DETAIL.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "            AND DETAIL.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "            AND DETAIL.EXAMNO       = T1.EXAMNO ";
            $query .= "            AND DETAIL.SEQ          = '001' ";
            $query .= "            AND DETAIL.REMARK10     = '{$model->hope_coursecode}' ";
        }
        $query .= "   WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV = '{$model->testdiv}' ";
        $query .= "   ORDER BY ";
        $query .= "     RENBAN ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "  TW.EXAMNO ";
        $query .= " FROM ";
        $query .= "  MAXCHKTBL TW ";
        $query .= " WHERE ";
        $query .= "  TW.RENBAN = (SELECT COUNT(*) FROM MAXCHKTBL) ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model, $flg) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        if ($flg == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "   T1.EXAMNO, ";
            $query .= "   T2.CONFIDENTIAL_RPT01, ";
            $query .= "   T2.CONFIDENTIAL_RPT02, ";
            $query .= "   T2.CONFIDENTIAL_RPT03, ";
            $query .= "   T2.CONFIDENTIAL_RPT04, ";
            $query .= "   T2.CONFIDENTIAL_RPT05, ";
            $query .= "   T2.CONFIDENTIAL_RPT06, ";
            $query .= "   T2.CONFIDENTIAL_RPT07, ";
            $query .= "   T2.CONFIDENTIAL_RPT08, ";
            $query .= "   T2.CONFIDENTIAL_RPT09, ";
            $query .= "   T2.TOTAL5, ";
            $query .= "   T2.TOTAL_ALL ";
        }
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T2 ";
        $query .= "     ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND T2.EXAMNO = T1.EXAMNO ";
        if ($model->hope_coursecode != $model->CONST_SELALL) {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ";
            $query .= "             ON DETAIL.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "            AND DETAIL.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "            AND DETAIL.EXAMNO       = T1.EXAMNO ";
            $query .= "            AND DETAIL.SEQ          = '001' ";
            $query .= "            AND DETAIL.REMARK10     = '{$model->hope_coursecode}' ";
        }
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV = '{$model->testdiv}' ";
        if ($model->cmd == "read" || $model->hoji_cmd == "read") {
            if ($model->field["S_EXAMNO"] != "") {
                $query .= "   AND T1.EXAMNO >= '".$model->field["S_EXAMNO"]."' ";
                if ($model->field["E_EXAMNO"] != "") {
                    $query .= "   AND T1.EXAMNO <= '".$model->field["E_EXAMNO"]."' ";
                }
            }
            if ($flg != "COUNT") {
                $query .= " ORDER BY T1.EXAMNO ";
            }
        } else if ($model->cmd == "back" || $model->hoji_cmd == "back") {
            if ($model->field["S_EXAMNO"] != "") {
                $query .= "   AND T1.EXAMNO < '".$model->field["S_EXAMNO"]."' ";
            }
            if ($flg != "COUNT") {
                $query .= " ORDER BY T1.EXAMNO ";
            }
        } else if ($model->cmd == "next" || $model->hoji_cmd == "next") {
            $query .= "   AND T1.EXAMNO > '".$model->field["E_EXAMNO"]."' ";
            if ($flg != "COUNT") {
                $query .= " ORDER BY T1.EXAMNO ";
            }
        }
        $query .= " FETCH FIRST 50 ROWS ONLY ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= "  FROM ";
        $query .= "     MAIN_T ";
        if ($flg != "COUNT") {
            $query .= " ORDER BY EXAMNO ";
        }

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($model->data); $i++) {
            $examno = $model->data[$i];
            
            //データチェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
            $query .= " WHERE ENTEXAMYEAR    = '{$model->examyear}'";
            $query .= "   AND APPLICANTDIV   = '{$model->applicantdiv}'";
            $query .= "   AND EXAMNO         = '{$examno}'";
            $getCount = $db->getOne($query);

            //データ更新
            $data = array();
            for ($j = 1;$j <= 9;$j++) {
                $idx = sprintf("%02d", $j);
                $fidx = $examno."_".$j;
                if($model->fields[$fidx] != ""){
                    $data["CONFIDENTIAL_RPT".$idx][NUMBER] = $model->fields[$fidx];
                }
            }
            $fidx = $examno."_TOTAL5";
            $data["TOTAL5"][NUMBER] = $model->fields[$fidx];
            $fidx = $examno."_TOTAL_ALL";
            $data["TOTAL_ALL"][NUMBER] = $model->fields[$fidx];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($getCount == 0) {
                $data["ENTEXAMYEAR"][TEXT]       = $model->examyear;
                $data["APPLICANTDIV"][TEXT]      = $model->applicantdiv;
                $data["EXAMNO"][TEXT]            = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR    = '{$model->examyear}'";
                $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}'";
                $where .= "   AND EXAMNO         = '{$examno}'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>