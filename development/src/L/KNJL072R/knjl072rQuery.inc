<?php

require_once('for_php7.php');

class knjl072rQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "          BASE.EXAMNO, ";
        $query .= "          BASE.NAME, ";
        $query .= "          COURSE.EXAMCOURSE_MARK, ";
        $query .= "          CASE WHEN BEF.BEFORE_PAGE IS NOT NULL AND BEF.SCHOLARSHIP IS NOT NULL ";
        $query .= "               THEN '1' ";
        $query .= "               ELSE '0' ";
        $query .= "          END AS SCHOLARSHIP, ";
        $query .= "          RECE.TOTAL4, ";
        $query .= "          RECE.TOTAL_RANK4, ";
        $query .= "          COURSE2.EXAMCOURSE_MARK AS EXAMCOURSE_MARK2, ";
        $query .= "          L031.NAME1 AS SYOUGAKU, ";
        $query .= "          L032.NAME1 AS CLUB, ";
        $query .= "          BASE.JUDGE_KIND ";
        $query .= "      FROM ";
        $query .= "          ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          LEFT JOIN ENTEXAM_RECEPT_DAT RECE ON BASE.ENTEXAMYEAR = RECE.ENTEXAMYEAR ";
        $query .= "               AND BASE.APPLICANTDIV = RECE.APPLICANTDIV ";
        $query .= "               AND BASE.TESTDIV = RECE.TESTDIV ";
        $query .= "               AND RECE.EXAM_TYPE = '".$model->exam_type."' ";
        $query .= "               AND BASE.EXAMNO = RECE.EXAMNO ";
        $query .= "          LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ";
        $query .= "               AND BASE.APPLICANTDIV = COURSE.APPLICANTDIV ";
        $query .= "               AND BASE.TESTDIV = COURSE.TESTDIV ";
        $query .= "               AND BASE.SUC_COURSECD = COURSE.COURSECD ";
        $query .= "               AND BASE.SUC_MAJORCD = COURSE.MAJORCD ";
        $query .= "               AND BASE.SUC_COURSECODE = COURSE.EXAMCOURSECD ";
        $query .= "          LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ON BASE.ENTEXAMYEAR = DETAIL.ENTEXAMYEAR ";
        $query .= "               AND BASE.APPLICANTDIV = DETAIL.APPLICANTDIV ";
        $query .= "               AND BASE.EXAMNO = DETAIL.EXAMNO ";
        $query .= "               AND DETAIL.SEQ = '002' ";
        $query .= "          LEFT JOIN ENTEXAM_APPLICANT_BEFORE_DAT BEF ON BASE.ENTEXAMYEAR = BEF.ENTEXAMYEAR ";
        $query .= "               AND BASE.APPLICANTDIV = BEF.APPLICANTDIV ";
        $query .= "               AND BASE.TESTDIV = BEF.TESTDIV ";
        $query .= "               AND DETAIL.REMARK1 = BEF.BEFORE_PAGE ";
        $query .= "               AND DETAIL.REMARK2 = BEF.BEFORE_SEQ ";
        $query .= "          LEFT JOIN ENTEXAM_COURSE_MST COURSE2 ON BEF.ENTEXAMYEAR = COURSE2.ENTEXAMYEAR ";
        $query .= "               AND BEF.APPLICANTDIV = COURSE2.APPLICANTDIV ";
        $query .= "               AND BEF.TESTDIV = COURSE2.TESTDIV ";
        $query .= "               AND BEF.BEFORE_COURSECD = COURSE2.COURSECD ";
        $query .= "               AND BEF.BEFORE_MAJORCD = COURSE2.MAJORCD ";
        $query .= "               AND BEF.BEFORE_EXAMCOURSECD = COURSE2.EXAMCOURSECD ";
        $query .= "          LEFT JOIN NAME_MST L031 ON L031.NAMECD1 = 'L031' ";
        $query .= "               AND BEF.SCHOLARSHIP = L031.NAMECD2 ";
        $query .= "          LEFT JOIN NAME_MST L032 ON L032.NAMECD1 = 'L032' ";
        $query .= "               AND BEF.RECOM_FLG = L032.NAMECD2 ";
        $query .= "          INNER JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ";
        $query .= "                AND BASE.JUDGEMENT = L013.NAMECD2 ";
        $query .= "                AND  L013.NAMESPARE1 = '1' ";
        $query .= "      WHERE ";
        $query .= "          BASE.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "          AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "          AND BASE.TESTDIV = '".$model->testdiv."' ";
        $query .= "      ORDER BY ";
        $query .= "          CASE WHEN BEF.BEFORE_PAGE IS NOT NULL AND BEF.SCHOLARSHIP IS NOT NULL ";
        $query .= "               THEN 999 ";
        $query .= "               ELSE VALUE(RECE.TOTAL4, 0) ";
        $query .= "          END DESC, ";
        $query .= "          RECE.TOTAL4, ";
        $query .= "          RECE.TOTAL_RANK4, ";
        $query .= "          BASE.EXAMNO ";
        $query .= "     ) T1 ";
        $query .= " ORDER BY ";
        if ($model->sort == "2") {
            $query .= "     T1.SCHOLARSHIP DESC, ";
        } else if ($model->sort == "3") {
            $query .= "     T1.EXAMNO, ";
        }
        $query .= "     VALUE(T1.TOTAL4, 0) DESC ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        foreach ($model->judgeKind as $examno => $judgeKind) {

            $data = array();
            $data["JUDGE_KIND"][TEXT]           = $judgeKind;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = "      WHERE ";
            $where .= "             ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "          AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $where .= "          AND EXAMNO       = '".$examno."' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>