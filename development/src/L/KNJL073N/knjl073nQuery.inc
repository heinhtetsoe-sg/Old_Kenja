<?php

require_once('for_php7.php');

class knjl073nQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($namecd1, $year, $namecd2 = "")
    {
        $query  = " WITH T_SHDIV(VALUE, LABEL, NAMESPARE2) AS ( ";
        $query .= "     VALUES('9', '9' || ':' || '全員', '') ";
        $query .= " ) ";

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

    //名称マスタより取得
    public function getNameMstL013($namecd1, $year)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //志望区分
    public function getExamcourse($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD IN ( ";
        $query .= "         SELECT ";
        $query .= "             NORMAL_PASSCOURSECD || '-' || NORMAL_PASSMAJORCD || '-' || NORMAL_PASSEXAMCOURSECD ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= "         WHERE ";
        $query .= "                 ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //まわし合格コース
    public function getSucCourse($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD IN ( ";
        $query .= "         SELECT ";
        $query .= "             EARLY_PASSCOURSECD || '-' || EARLY_PASSMAJORCD || '-' || EARLY_PASSEXAMCOURSECD ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= "         WHERE ";
        $query .= "                 ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "             AND NORMAL_PASSCOURSECD || '-' || NORMAL_PASSMAJORCD || '-' || NORMAL_PASSEXAMCOURSECD = '{$model->examcourse}' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = "";
        $query .= "SELECT T1.RECEPTNO ";
        $query .= "      ,T1.EXAMNO ";
        $query .= "      ,T2.NAME ";
        $query .= "      ,T1.TOTAL4 ";
        $query .= "      ,T1.DIV_RANK4 ";
//        $query .= "      ,T1.ATTEND_ALL_FLG ";
        $query .= "      ,T2.JUDGEMENT ";
        $query .= "      ,N1.NAMECD2 || ':' || N1.NAME1 AS JUDGEMENT_NAME ";
        $query .= "      ,N1.ABBV1 AS JUDGEMENT_ABBV ";
        $query .= "      ,T2.SUC_COURSECD || '-' || T2.SUC_MAJORCD || '-' || T2.SUC_COURSECODE AS SUC_COURSE ";
        $query .= "      ,L1.EXAMCOURSECD || ':' || L1.EXAMCOURSE_NAME AS SUC_COURSE_NAME ";
        $query .= "      ,C2.EXAMCOURSECD || ':' || C2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME2 ";
        $query .= "      ,N2.NAME1 AS SOUDAN ";
        $query .= "      ,T2.PROCEDUREDIV ";
        $query .= "      ,T2.ENTDIV ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "             AND T2.SHDIV        = '{$model->shdiv}' ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ";
        $query .= "              ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T3.EXAMNO       = T1.EXAMNO ";
        $query .= "             AND T3.SEQ          = '001' ";
        $query .= "             AND T3.REMARK8 || '-' || T3.REMARK9 || '-' || T3.REMARK10 = '{$model->examcourse}' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L2 ";
        $query .= "              ON L2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND L2.EXAMNO       = T1.EXAMNO ";
        $query .= "             AND L2.SEQ          = '009' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD017 ";
        $query .= "              ON T1.ENTEXAMYEAR  = BD017.ENTEXAMYEAR ";
        $query .= "             AND T1.EXAMNO       = BD017.EXAMNO ";
        $query .= "             AND BD017.SEQ       = '017' ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ";
        $query .= "              ON N2.YEAR         = L2.ENTEXAMYEAR ";
        $query .= "             AND N2.NAMECD1      = 'L026' ";
        $query .= "             AND N2.NAMECD2      = L2.REMARK9 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1      = 'L013' ";
        $query .= "             AND N1.NAMECD2      = T2.JUDGEMENT ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "              ON L1.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV      = T2.TESTDIV ";
        $query .= "             AND L1.COURSECD     = T2.SUC_COURSECD ";
        $query .= "             AND L1.MAJORCD      = T2.SUC_MAJORCD ";
        $query .= "             AND L1.EXAMCOURSECD = T2.SUC_COURSECODE ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C2 ";
        $query .= "              ON C2.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND C2.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND C2.TESTDIV      = T2.TESTDIV ";
        $query .= "             AND C2.COURSECD     = BD017.REMARK1 ";
        $query .= "             AND C2.MAJORCD      = BD017.REMARK2 ";
        $query .= "             AND C2.EXAMCOURSECD = BD017.REMARK3 ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND T1.EXAM_TYPE      = '".$model->exam_type."' ";
        //「2:不合格」「3:まわし合格」のみ更新する。
        $query .= "   AND T2.JUDGEMENT IN ('2', '3') ";
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:成績順 2:受験番号順
        if ($model->sort == "1") {
            $query .= "     value(T1.TOTAL4, -1) DESC, ";
        }
        $query .= "     T1.EXAMNO ";
        return $query;
    }

    //更新(一度すべて削除してから追加）
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //まわし合格コース
        $course_array = array();
        $course_array = explode("-", $model->suc_course);

        //チェックありの人を処理
        //「2:不合格」「3:まわし合格」のみ更新する。
        foreach ($model->field["CHK_DATA"] as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //受付データ
            $data = array();
            $data["JUDGEDIV"][TEXT]         = !strlen($model->suc_course) ? "2" : "3";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' AND";
            $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
            $where .= "       TESTDIV       = '".$model->testdiv."' AND";
            $where .= "       EXAMNO        = '".$examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //基礎データ
            //合格コースが空白の時、合否は「2:不合格」で更新する。
            //合格コースがありの時、合否は「3:まわし合格」で更新する。
            $data = array();
            $data["JUDGEMENT"][TEXT]        = !strlen($model->suc_course) ? "2" : "3";
            $data["SUC_COURSECD"][TEXT]     = !strlen($model->suc_course) ? "" : $course_array[0];
            $data["SUC_MAJORCD"][TEXT]      = !strlen($model->suc_course) ? "" : $course_array[1];
            $data["SUC_COURSECODE"][TEXT]   = !strlen($model->suc_course) ? "" : $course_array[2];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' AND";
            $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
            $where .= "       EXAMNO        = '".$examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
