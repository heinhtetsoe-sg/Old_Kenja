<?php

require_once('for_php7.php');

class knjl433hQuery extends Query
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
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目取得
    public function getExamclass($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     SETTING_CD   = '".$namecd1."' AND ";
        $query .= "     SEQ IN ( ";
        $query .= "         SELECT DISTINCT ";
        $query .= "             TESTSUBCLASSCD ";
        $query .= "         FROM  ";
        $query .= "             ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= "         WHERE  ";
        $query .= "             ENTEXAMYEAR    = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "         AND TESTDIV        = '{$model->testdiv}' ";
        $query .= "         AND EXAM_TYPE      = '{$model->examtype}' ";
        if ($model->totalcd) {
            $query .= "         AND EXAMCOURSECD   = '{$model->totalcd}' ";
        }
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //満点マスタを取得
    public function getPerfectAtClass($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(PERFECT) AS PERFECT ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " WHERE  ";
        $query .= "     ENTEXAMYEAR    = '{$model->year}' ";
        $query .= " AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= " AND TESTDIV        = '{$model->testdiv}' ";
        $query .= " AND EXAM_TYPE      = '{$model->examtype}' ";
        if ($model->totalcd) {
            $query .= " AND EXAMCOURSECD   = '{$model->totalcd}' ";
        }
        $query .= " AND TESTSUBCLASSCD = '{$model->examclass}' ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = "";
        $query .= " WITH T_COUSE_MST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         EXAMCOURSECD, ";
        $query .= "         EXAMCOURSE_ABBV ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_COURSE_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "         APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= " ) ";
        $query .= " , T_SHOUGAKU_MST(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1', '全額') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2', '半額') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     R1.EXAMNO, ";
        $query .= "     R1.JUDGEDIV, ";
        $query .= "     B1.NAME, ";
        $query .= "     S1.SCORE, ";
        $query .= "     BD.REMARK8 AS ROUNIN_FLG, ";
        $query .= "     SM.NAME1 AS DESIREDIV, "; //出願区分
        $query .= "     CM1.EXAMCOURSE_ABBV AS PRIVATE_REP_INTRNL, "; //内申内諾
        $query .= "     CM2.EXAMCOURSE_ABBV AS MOCKTEST_INTRNL, "; //模試内諾
        $query .= "     CM3.EXAMCOURSE_ABBV AS OPT_INTRNL, "; //OPT内諾
        $query .= "     CM4.EXAMCOURSE_ABBV AS REINFORCEMENT_RCMMND, "; //強化クラブ推薦
        $query .= "     GM1.LABEL AS INTRNL_SCHOLARSHIP_STDNT, "; //内申奨学生
        $query .= "     GM2.LABEL AS MOCK_TST_SCHOLARSHIP_STDNT, "; //模試奨学生
        $query .= "     GM3.LABEL AS OPT_SCHOLARSHIP_STDNT "; //OPT奨学生
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO = R1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "          ON S1.ENTEXAMYEAR    = R1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV   = R1.APPLICANTDIV ";
        $query .= "         AND S1.TESTDIV        = R1.TESTDIV ";
        $query .= "         AND S1.EXAM_TYPE      = R1.EXAM_TYPE ";
        $query .= "         AND S1.RECEPTNO       = R1.RECEPTNO ";
        $query .= "         AND S1.TESTSUBCLASSCD = '{$model->examclass}' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD ";
        $query .= "          ON BD.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND BD.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND BD.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND BD.SEQ          = '009' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD ";
        $query .= "          ON RD.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND RD.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND RD.TESTDIV      = R1.TESTDIV ";
        $query .= "         AND RD.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "         AND RD.RECEPTNO     = R1.RECEPTNO ";
        $query .= "         AND RD.SEQ          = '016' "; //第一志望
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST SM ";
        $query .= "          ON SM.ENTEXAMYEAR  = RD.ENTEXAMYEAR ";
        $query .= "         AND SM.APPLICANTDIV = RD.APPLICANTDIV ";
        $query .= "         AND SM.SETTING_CD   = 'L006' ";
        $query .= "         AND SM.SEQ          = RD.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD003 ";
        $query .= "          ON CD003.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND CD003.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND CD003.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND CD003.SEQ          = '003' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD004 ";
        $query .= "          ON CD004.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND CD004.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND CD004.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND CD004.SEQ          = '004' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD005 ";
        $query .= "          ON CD005.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND CD005.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND CD005.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND CD005.SEQ          = '005' ";
        $query .= "     LEFT JOIN T_COUSE_MST CM1 ON CM1.EXAMCOURSECD = CD005.REMARK1 ";    //内申内諾
        $query .= "     LEFT JOIN T_COUSE_MST CM2 ON CM2.EXAMCOURSECD = CD003.REMARK10 ";   //模試内諾
        $query .= "     LEFT JOIN T_COUSE_MST CM3 ON CM3.EXAMCOURSECD = CD004.REMARK10 ";   //OPT内諾
        $query .= "     LEFT JOIN T_COUSE_MST CM4 ON CM4.EXAMCOURSECD = CD005.REMARK5 ";    //強化クラブ推薦
        $query .= "     LEFT JOIN T_SHOUGAKU_MST GM1 ON GM1.VALUE = CD005.REMARK3 ";        //内申奨学生
        $query .= "     LEFT JOIN T_SHOUGAKU_MST GM2 ON GM2.VALUE = CD003.REMARK12 ";       //模試奨学生
        $query .= "     LEFT JOIN T_SHOUGAKU_MST GM3 ON GM3.VALUE = CD004.REMARK12 ";       //OPT奨学生
        $query .= " WHERE ";
        $query .= "         R1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND R1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND R1.EXAM_TYPE    = '{$model->examtype}' ";
        if ($model->totalcd) {
            $query .= "     AND RD.REMARK1      = '{$model->totalcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     R1.RECEPTNO ";

        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $keyList = array();
        $keyList = explode(",", $model->hid_receptno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //欠席者は、入力不可
            if ($model->field["JUDGEDIV"][$receptno] == "4") {
                continue;
            }

            $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
            $query .= " WHERE ";
            $query .= "        ENTEXAMYEAR    = '{$model->year}' ";
            $query .= "    AND APPLICANTDIV   = '{$model->applicantdiv}' ";
            $query .= "    AND TESTDIV        = '{$model->testdiv}' ";
            $query .= "    AND EXAM_TYPE      = '{$model->examtype}' ";
            $query .= "    AND RECEPTNO       = '{$receptno}' ";
            $query .= "    AND TESTSUBCLASSCD = '{$model->examclass}' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->year;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["TESTDIV"][TEXT]              = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]            = $model->examtype;
            $data["RECEPTNO"][TEXT]             = $receptno;
            $data["TESTSUBCLASSCD"][TEXT]       = $model->examclass;
            $data["SCORE"][NUMBER]              = $model->field["SCORE"][$receptno];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
