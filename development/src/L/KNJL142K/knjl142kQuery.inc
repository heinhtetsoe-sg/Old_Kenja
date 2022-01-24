<?php
class knjl142kQuery extends Query {

    //名称
    function GetName($namecd1,$year)
    {
        $query = "SELECT namecd2, name1, name2 FROM v_name_mst
                   WHERE year    = '".$year."'
                     AND namecd1 = '".$namecd1."'";
        return $query;
    }
    
    //重複データチェック
    function CheckData($model)
    {
        return $query;
    }

    //一覧表示
    function getExam($model, $div)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        if ($div == "BACK") {
            $query .= "     AND EXAMNO < '".$model->StrExamno."' ";
        } else {
            $query .= "     AND EXAMNO > '".$model->EndExamno."' ";
        }
        $query .= "     AND SCALASHIPDIV = '02' ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {

        $query  = " SELECT ";
        $query .= "     T1.A_TOTAL_RANK, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     L1.ABBV1 AS SEX, ";
        $query .= "     L2.NAME1 AS SHDIV, ";
        $query .= "     L3.EXAMCOURSE_ABBV, ";
        $query .= "     T1.A_TOTAL, ";
        $query .= "     L4.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z002' ";
        $query .= "          AND L1.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L006' ";
        $query .= "          AND L2.NAMECD2 = T1.SHDIV ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L3 ON L3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "          AND L3.COURSECD || L3.MAJORCD || L3.EXAMCOURSECD = T1.SUC_COURSECD || T1.SUC_MAJORCD || T1.SUC_COURSECODE ";
        $query .= "     LEFT JOIN FINSCHOOL_MST L4 ON L4.FINSCHOOLCD = T1.FS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        if ($model->cmd == "back" || $model->cmd == "back2") {
            $query .= "     AND T1.EXAMNO <= '".$model->StrExamno."' ";
        } else {
            $query .= "     AND T1.EXAMNO >= '".$model->StrExamno."' ";
        }
        $query .= "     AND T1.SCALASHIPDIV = '02' ";
        if ($model->cmd == "back" || $model->cmd == "back2") {
            $query .= " ORDER BY T1.EXAMNO DESC ";
        } else {
            $query .= " ORDER BY T1.EXAMNO ";
        }
        $query .= " FETCH FIRST 100 ROWS ONLY ";
        #echo $query;
        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        foreach ($model->delchk as $key => $examno)
        {
            $data = array();

            $data["SCALASHIPDIV"][TEXT] = NULL;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR = '".$model->year."'";
            $where .= "       AND EXAMNO = '".sprintf("%04d",$examno)."'";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>