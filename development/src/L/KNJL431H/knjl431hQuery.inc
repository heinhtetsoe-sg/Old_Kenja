<?php

require_once('for_php7.php');

class knjl431hQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //設定マスタより取得
    public function getSettingMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     SETTING_CD   = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験コース取得
    public function getEntExamCourse($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験型取得
    public function getExamTypeMst($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_TYPE AS VALUE, ";
        $query .= "     EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND REMARK1 = '".$model->testdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.JUDGEDIV, ";
        $query .= "     T3.REMARK8 AS ROUNIN_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ";
        $query .= "              ON T3.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND T3.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND T3.EXAMNO       = T2.EXAMNO ";
        $query .= "             AND T3.SEQ          = '009' ";
        if ($model->examcoursecd) {
            $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT T4 ";
            $query .= "              ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "             AND T4.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "             AND T4.TESTDIV      = T1.TESTDIV ";
            $query .= "             AND T4.EXAM_TYPE    = T1.EXAM_TYPE ";
            $query .= "             AND T4.RECEPTNO     = T1.RECEPTNO ";
            $query .= "             AND T4.SEQ          = '016' ";
            $query .= "             AND T4.REMARK1      = '".$model->examcoursecd."' ";
        }
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->year."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND T1.EXAM_TYPE      = '".$model->exam_type."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECEPTNO ";
        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        //チェックあり：欠席　チェックなし：NULL
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //更新フラグ・・・欠席チェックON/OFFしたものだけを更新する
            if ($model->field["UPD_FLG"][$receptno] !== "1") {
                continue;
            }

            //受付データ
            $data = array();
            $data["JUDGEDIV"][TEXT]         = $model->field["CHK_DATA"][$receptno];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
            $where .= "   AND RECEPTNO      = '".$receptno."' ";
            $where .= "   AND VALUE(JUDGEDIV, '4') = '4' "; //欠席またはNULL
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
