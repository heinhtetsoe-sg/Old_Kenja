<?php

require_once('for_php7.php');

class knjl051nQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($namecd1, $year, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAME1 AS CSV_NAME ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //志望区分取得
    public function getEntExamCourse($model, $totalcd = "")
    {
        $query .= " SELECT ";
        $query .= "     COURSECD || MAJORCD || EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL, ";
        $query .= "     EXAMCOURSE_NAME AS CSV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " AND TESTDIV = '{$model->testdiv}' ";
        if (strlen($totalcd)) {
            $query .= "     AND COURSECD || MAJORCD || EXAMCOURSECD = '".$totalcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model, $flg)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        if ($flg == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     L1.EXAMCOURSE_NAME, ";
            $query .= "     RECE.RECEPTNO, ";
            $query .= "     RECE.EXAMNO, ";
            $query .= "     BASE.NAME, ";
            $query .= "     L2.FINSCHOOL_NAME, ";
            $query .= "     DET_009.REMARK2, ";//備考2
            $query .= "     INTER.INTERVIEW_VALUE, ";
            $query .= "     INTER.INTERVIEW_REMARK, ";
            $query .= "     INTER.INTERVIEW_VALUE2, ";
            $query .= "     INTER.INTERVIEW_REMARK2 ";
        }
        $query .= "  FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECE ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "           ON RECE.ENTEXAMYEAR   = BASE.ENTEXAMYEAR ";
        $query .= "          AND RECE.EXAMNO        = BASE.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DET_001 ";
        $query .= "           ON RECE.ENTEXAMYEAR   = DET_001.ENTEXAMYEAR ";
        $query .= "          AND RECE.EXAMNO        = DET_001.EXAMNO ";
        $query .= "          AND DET_001.SEQ        = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DET_009 ";
        $query .= "           ON RECE.ENTEXAMYEAR   = DET_009.ENTEXAMYEAR ";
        $query .= "          AND RECE.EXAMNO        = DET_009.EXAMNO ";
        $query .= "          AND DET_009.SEQ        = '009' ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTER ";
        $query .= "           ON RECE.ENTEXAMYEAR   = INTER.ENTEXAMYEAR ";
        $query .= "          AND RECE.APPLICANTDIV  = INTER.APPLICANTDIV ";
        $query .= "          AND RECE.TESTDIV       = INTER.TESTDIV ";
        $query .= "          AND RECE.EXAMNO        = INTER.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "           ON RECE.ENTEXAMYEAR   = L1.ENTEXAMYEAR ";
        $query .= "          AND RECE.APPLICANTDIV  = L1.APPLICANTDIV ";
        $query .= "          AND RECE.TESTDIV       = L1.TESTDIV ";
        $query .= "          AND DET_001.REMARK8    = L1.COURSECD ";
        $query .= "          AND DET_001.REMARK9    = L1.MAJORCD ";
        $query .= "          AND DET_001.REMARK10   = L1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST L2 ";
        $query .= "           ON BASE.FS_CD   = L2.FINSCHOOLCD ";
        $query .= " WHERE RECE.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND RECE.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND RECE.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND RECE.EXAM_TYPE      = '".$model->exam_type."' ";
        $query .= "   AND DET_001.REMARK8 || DET_001.REMARK9 || DET_001.REMARK10 = '".$model->totalcd."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= "  FROM ";
        $query .= "     MAIN_T ";
        if ($flg != "COUNT") {
            $query .= " ORDER BY RECEPTNO ";
        }
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
            $query  = " DELETE FROM ENTEXAM_INTERVIEW_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR        = '".$model->ObjYear."' ";
            $query .= "     AND APPLICANTDIV   = '".$model->applicantdiv."' ";
            $query .= "     AND TESTDIV        = '".$model->testdiv."' ";
            $query .= "     AND EXAMNO         = '".$receptno_array[0]."' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["TESTDIV"][TEXT]              = $model->testdiv;
            $data["EXAMNO"][TEXT]               = $receptno_array[0];
            $data["INTERVIEW_VALUE"][TEXT]      = $model->interView_Value[$receptno_array[1]];
            $data["INTERVIEW_REMARK"][TEXT]     = $model->interView_Remark[$receptno_array[1]];
            $data["INTERVIEW_VALUE2"][TEXT]     = $model->interView_Value2[$receptno_array[1]];
            $data["INTERVIEW_REMARK2"][TEXT]    = $model->interView_Remark2[$receptno_array[1]];
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
