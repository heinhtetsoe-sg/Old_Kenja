<?php

require_once('for_php7.php');

class knjl432hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
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
        $query .= "     int(VALUE) ";

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
    public function getExamType($model)
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
        $query .= "     int(VALUE) ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = "SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.JUDGEDIV, ";
        $query .= "     VALUE(NML015.ABBV1, '') || '　' || FINSCH.FINSCHOOL_NAME AS FINSCHOOL_NAME, "; //学校立区分△学校名
        $query .= "     INTER.INTERVIEW_A, ";
        $query .= "     INTER.OTHER_REMARK7 AS INTERVIEW_REMARK, ";
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
        if ($model->totalcd) {
            $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT T4 ";
            $query .= "              ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "             AND T4.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "             AND T4.TESTDIV      = T1.TESTDIV ";
            $query .= "             AND T4.EXAM_TYPE    = T1.EXAM_TYPE ";
            $query .= "             AND T4.RECEPTNO     = T1.RECEPTNO ";
            $query .= "             AND T4.SEQ          = '016' ";
            $query .= "             AND T4.REMARK1      = '".$model->totalcd."' ";
        }
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTER ";
        $query .= "              ON INTER.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "             AND INTER.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "             AND INTER.TESTDIV        = T1.TESTDIV ";
        $query .= "             AND INTER.EXAMNO         = T1.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINSCH ";
        $query .= "              ON T2.FS_CD          = FINSCH.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST NML015 ";
        $query .= "              ON NML015.NAMECD1 = 'L015' ";
        $query .= "             AND NML015.NAMECD2 = FINSCH.FINSCHOOL_DIV ";
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
        $tmp = explode(",", $model->hid_receptno);
        $receptno_array = array();
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            $receptno   = $receptno_array[0];
            $examno     = $receptno_array[2];

            //欠席者は、入力不可（つまり、更新しない）
            $query  = "SELECT ";
            $query .= "     JUDGEDIV ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     = '".$model->year."' AND ";
            $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
            $query .= "     EXAM_TYPE       = '".$model->exam_type."' AND ";
            $query .= "     RECEPTNO        = '".$receptno."' ";
            $kessekiFlg = $db->getOne($query);
            if ($kessekiFlg == "4") {
                continue;
            }

            $query  = " DELETE FROM ENTEXAM_INTERVIEW_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     = '".$model->year."' AND ";
            $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
            $query .= "     EXAMNO          = '".$examno."' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->year;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["TESTDIV"][TEXT]              = $model->testdiv;
            $data["EXAMNO"][TEXT]               = $examno;
            $data["INTERVIEW_A"][TEXT]          = $model->interView_Value[$receptno_array[1]];
            $data["OTHER_REMARK7"][TEXT]        = $model->interView_Remark[$receptno_array[1]];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
