<?php

require_once('for_php7.php');

class knjl530aQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namespare="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namespare) {
            $query .= " AND NAMESPARE".$namespare." = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分の取得
    function getHopeCourse($year) {
        $query  = " SELECT ";
        $query .= "   T1.HOPE_COURSECODE AS VALUE, ";
        $query .= "   T1.HOPE_COURSECODE || ':' || T1.HOPE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_MST T1 ";
        $query .= "   INNER JOIN ENTEXAM_HOPE_COURSE_YDAT T2 ";
        $query .= "           ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= "          AND T2.ENTEXAMYEAR     = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "  SELECT ";
        $query .= "    B1.EXAMNO ";
        $query .= "    , B1.NAME ";
        $query .= "    , B1.FS_CD ";
        $query .= "    , F1.FINSCHOOL_NAME AS FINSCHOOL_NAME ";
        $query .= "    , D1.REMARK8 AS GOKAKU ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "    LEFT JOIN ENTEXAM_SCHOOL_MST S1 ";
        $query .= "      ON S1.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "      AND S1.ENTEXAM_SCHOOLCD = B1.FS_CD ";
        $query .= "    LEFT JOIN FINSCHOOL_MST F1 ";
        $query .= "      ON F1.FINSCHOOLCD = S1.FINSCHOOLCD ";
        $query .= "    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D1 ";
        $query .= "      ON D1.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "      AND D1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "      AND D1.EXAMNO = B1.EXAMNO ";
        $query .= "      AND D1.SEQ = '004' ";
        $query .= "    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D2 ";
        $query .= "      ON D2.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "      AND D2.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "      AND D2.EXAMNO = B1.EXAMNO ";
        $query .= "      AND D2.SEQ = '001' ";
        $query .= "  WHERE ";
        $query .= "    B1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "    AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "    AND B1.TESTDIV = '".$model->testdiv."' ";
        if (strlen($model->desirediv)) {
            $query .= "     AND D2.REMARK10 = '".$model->desirediv."' ";
        }
        if (strlen($model->examno)) {
            $query .= "     AND B1.EXAMNO    = '".$model->examno."' ";
        }
        $query .= " ORDER BY ";
        if ($model->sort == "2") {
            $query .= "     B1.NAME, ";
        }
        $query .= "     B1.EXAMNO ";

        return $query;
    }

    //存在チェック
    function checkEntexamApplicantbaseDetailDat($model, $seq) {
        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     SEQ             = '".$seq."' ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック
        $D004 = $db->getCol(knjl530aQuery::checkEntexamApplicantbaseDetailDat($model, "004"));

        //更新
        $array = explode(",", $model->hid_examno);
        foreach ($array as $key => $examno) {
            // ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $where .= "     EXAMNO          = '".$examno."' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            // ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:004
            $data = array();
            if (!in_array($examno, $D004)) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = '004';
            }
            $data["REMARK8"][TEXT]          = !strlen($model->data["GOKAKU"][$examno]) ? "" : $model->data["GOKAKU"][$examno];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if (!in_array($examno, $D004)) {
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
                $where .= "     EXAMNO          = '".$examno."' AND ";
                $where .= "     SEQ             = '004' ";

                //更新
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>