<?php

require_once('for_php7.php');

class knjl072mQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear) {
        $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
        return $query;
    }

    //合否コンボ
    function getJudgement() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L013' AND ";
        $query .= "     NAMECD2 IN ('1', '2', '4') ";

        return $query;
    }

    //右側のリストに入れない人たちを取ってくる
    function get_dont_use_list() {
        if ($model->judgement == '1' || $model->judgement == '2') {
            $query  = " SELECT ";
            $query .= "     T1.EXAMNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_RECEPT_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                           AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                           AND L1.TESTDIV = T1.TESTDIV ";
            $query .= "                           AND L1.EXAMNO  = T1.EXAMNO ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
            $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
            $query .= "     (T1.JUDGEMENT   = '1' OR T1.JUDGEMENT = '2') AND ";
            $query .= "     L1.JUDGEDIV     = '5' ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.EXAMNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     T1.EXAMNO AS RANK ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_RECEPT_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                           AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                           AND L1.TESTDIV = T1.TESTDIV ";
            $query .= "                           AND L1.EXAMNO  = T1.EXAMNO ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
            $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
            $query .= "     T1.JUDGEMENT    = '{$model->judgement}' AND ";
            $query .= "     L1.JUDGEDIV     = '5' ";
        }

        return $query;
    }

    //左リスト
    function GetLeftList($model) {
        if ($model->judgement == '1') {
            $query  = " SELECT ";
            $query .= "     T1.EXAMNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     L1.TOTAL_RANK4 AS RANK ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_RECEPT_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                           AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                           AND L1.TESTDIV = T1.TESTDIV ";
            $query .= "                           AND L1.EXAMNO  = T1.EXAMNO ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
            $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
            $query .= "     T1.JUDGEMENT    = '{$model->judgement}' ";
            $query .= " ORDER BY ";
            $query .= "     L1.TOTAL_RANK4, ";
            $query .= "     T1.EXAMNO ";
        } elseif ($model->judgement == '2') {
            $query  = " SELECT ";
            $query .= "     T1.EXAMNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     L1.TOTAL_RANK4 AS RANK ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_RECEPT_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                           AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                           AND L1.TESTDIV = T1.TESTDIV ";
            $query .= "                           AND L1.EXAMNO  = T1.EXAMNO ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
            $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
            $query .= "     T1.JUDGEMENT    = '{$model->judgement}' AND ";
            $query .= "     L1.JUDGEDIV     = '5' ";
            $query .= " ORDER BY ";
            $query .= "     T1.SUB_ORDER, ";
            $query .= "     T1.EXAMNO ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.EXAMNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     T1.SUB_ORDER AS RANK ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_RECEPT_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                           AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                           AND L1.TESTDIV = T1.TESTDIV ";
            $query .= "                           AND L1.EXAMNO  = T1.EXAMNO ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
            $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
            $query .= "     T1.JUDGEMENT    = '{$model->judgement}' AND ";
            $query .= "     L1.JUDGEDIV     = '5' ";
            $query .= " ORDER BY ";
            $query .= "     T1.SUB_ORDER, ";
            $query .= "     T1.EXAMNO ";
        }

        return $query;
    }

    //右リスト
    function GetRightList($model) {
        if ($model->judgement == '1' || $model->judgement == '2') {
            $query  = " SELECT ";
            $query .= "     T1.EXAMNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     L1.TOTAL_RANK4 AS RANK ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_RECEPT_DAT L1 ON  L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "                           AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                           AND L1.TESTDIV = T1.TESTDIV ";
            $query .= "                           AND L1.EXAMNO = T1.EXAMNO ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
            $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
            $query .= "     T1.JUDGEMENT = '3' AND ";
            $query .= "     L1.JUDGEDIV = '5' ";
            $query .= " ORDER BY ";
            $query .= "     L1.TOTAL_RANK4, ";
            $query .= "     T1.EXAMNO ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.EXAMNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     T1.SUB_ORDER AS RANK ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_RECEPT_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                           AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                           AND L1.TESTDIV = T1.TESTDIV ";
            $query .= "                           AND L1.EXAMNO  = T1.EXAMNO ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
            $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
            $query .= "     T1.JUDGEMENT    = '2' AND ";
            $query .= "     L1.JUDGEDIV     = '5' ";
            $query .= " ORDER BY ";
            $query .= "     T1.SUB_ORDER, ";
            $query .= "     T1.EXAMNO ";
        }

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";

        $entexam_course_mst = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        //(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());

        $sub_order = 1;
        foreach ($array as $examno) {//(左のリスト）
            if ($model->judgement == '1') {
                $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                $query .= "   SET JUDGEMENT      = '1' ";
                $query .= "      ,SUC_COURSECD   = '{$entexam_course_mst["COURSECD"]}' ";
                $query .= "      ,SUC_MAJORCD    = '{$entexam_course_mst["MAJORCD"]}' ";
                $query .= "      ,SUC_COURSECODE = '{$entexam_course_mst["EXAMCOURSECD"]}' ";
                $query .= "      ,REGISTERCD     = '".STAFFCD."' ";
                $query .= "      ,UPDATED        = SYSDATE() ";
                $query .= " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
                $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $query .= "   AND EXAMNO       = '{$examno}' ";
            } elseif ($model->judgement == '2') {
                $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                $query .= "   SET JUDGEMENT      = '2' ";
                $query .= "      ,SUB_ORDER      = '{$sub_order}' ";
                $query .= "      ,REGISTERCD     = '".STAFFCD."' ";
                $query .= "      ,UPDATED        = SYSDATE() ";
                $query .= " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
                $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $query .= "   AND EXAMNO       = '{$examno}' ";
            } else {
                $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                $query .= "   SET JUDGEMENT      = '4' ";
                $query .= "      ,SUC_COURSECD   = '{$entexam_course_mst["COURSECD"]}' ";
                $query .= "      ,SUC_MAJORCD    = '{$entexam_course_mst["MAJORCD"]}' ";
                $query .= "      ,SUC_COURSECODE = '{$entexam_course_mst["EXAMCOURSECD"]}' ";
                $query .= "      ,REGISTERCD     = '".STAFFCD."' ";
                $query .= "      ,UPDATED        = SYSDATE() ";
                $query .= " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
                $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $query .= "   AND EXAMNO       = '{$examno}' ";
            }
            $sub_order++;

            $db->query($query);
        }

        foreach ($array2 as $examno) {//(右のリスト)
            if ($model->judgement == '1') {
                $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                $query .= "   SET JUDGEMENT      = '3' ";
                $query .= "      ,SUC_COURSECD   = NULL ";
                $query .= "      ,SUC_MAJORCD    = NULL ";
                $query .= "      ,SUC_COURSECODE = NULL ";
                $query .= "      ,REGISTERCD     = '".STAFFCD."'";
                $query .= "      ,UPDATED        = SYSDATE()";
                $query .= " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
                $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $query .= "   AND EXAMNO       = '{$examno}' ";
                $query .= "   AND value(JUDGEMENT,'0') <> '1' "; //「1:合格」の人は更新しない。つまり更新後左リストに表示される
            } elseif ($model->judgement == '2') {
                $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                $query .= "   SET JUDGEMENT      = '3' ";
                $query .= "      ,SUB_ORDER      = NULL ";
                $query .= "      ,REGISTERCD     = '".STAFFCD."'";
                $query .= "      ,UPDATED        = SYSDATE()";
                $query .= " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
                $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $query .= "   AND EXAMNO       = '{$examno}' ";
            } else {
                $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                $query .= "   SET JUDGEMENT      = '2' ";
                $query .= "      ,SUC_COURSECD   = NULL ";
                $query .= "      ,SUC_MAJORCD    = NULL ";
                $query .= "      ,SUC_COURSECODE = NULL ";
                $query .= "      ,REGISTERCD     = '".STAFFCD."'";
                $query .= "      ,UPDATED        = SYSDATE()";
                $query .= " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
                $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $query .= "   AND EXAMNO       = '{$examno}' ";
            }

            $db->query($query);
        }

        //判定履歴データ（4:補員合格）追加
        if ($model->judgement == '4') {
            //削除
            $query  = "DELETE FROM ENTEXAM_JUDGEMENT_HISTORY_DAT";
            $query .= " WHERE ENTEXAMYEAR = '{$model->ObjYear}'";
            $query .= "   AND JUDGEMENT   = '{$model->judgement}'";
            $db->query($query);
            //追加
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
            $data["JUDGEMENT"][TEXT]    = $model->judgement;
            $data["JUDGE_CNT"][NUMBER]  = get_count($array);
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_JUDGEMENT_HISTORY_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
