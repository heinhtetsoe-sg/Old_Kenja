<?php

require_once('for_php7.php');

class knjl582aQuery extends Query {
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
        if (strlen($model->field["DESIREDIV"])) {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ";
            $query .= "             ON DETAIL.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "            AND DETAIL.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "            AND DETAIL.EXAMNO       = T1.EXAMNO ";
            $query .= "            AND DETAIL.SEQ          = '001' ";
            $query .= "            AND DETAIL.REMARK10     = '".$model->field["DESIREDIV"]."' ";
        }
        $query .= "   WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
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
        if (strlen($model->field["DESIREDIV"])) {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ";
            $query .= "             ON DETAIL.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "            AND DETAIL.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "            AND DETAIL.EXAMNO       = T1.EXAMNO ";
            $query .= "            AND DETAIL.SEQ          = '001' ";
            $query .= "            AND DETAIL.REMARK10     = '".$model->field["DESIREDIV"]."' ";
        }
        $query .= "   WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
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
    function SelectQuery($model) {
        $query  = " WITH ENTEXAM_SCHOOL AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.ENTEXAM_SCHOOLCD, ";
        $query .= "         T2.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCHOOL_MST T1 ";
        $query .= "         INNER JOIN  FINSCHOOL_MST T2 ";
        $query .= "                ON T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR      = '".$model->ObjYear."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B1.FS_CD, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        $query .= "     D1.REMARK1, ";
        $query .= "     B1.PROCEDUREDIV, ";
        $query .= "     B1.ENTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        if (strlen($model->field["DESIREDIV"])) {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ";
            $query .= "             ON DETAIL.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
            $query .= "            AND DETAIL.APPLICANTDIV = B1.APPLICANTDIV ";
            $query .= "            AND DETAIL.EXAMNO       = B1.EXAMNO ";
            $query .= "            AND DETAIL.SEQ          = '001' ";
            $query .= "            AND DETAIL.REMARK10     = '".$model->field["DESIREDIV"]."' ";
        }
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D1 ";
        $query .= "          ON D1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND D1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND D1.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND D1.SEQ          = '022' ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL F1 ";
        $query .= "          ON F1.ENTEXAM_SCHOOLCD  = B1.FS_CD ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND B1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        if ($model->cmd == "search" || $model->hoji_cmd == "search" || $model->cmd == "reload") {
            if ($model->field["S_EXAMNO"] != "") {
                $query .= "   AND B1.EXAMNO >= '".$model->field["S_EXAMNO"]."' ";
                if ($model->field["E_EXAMNO"] != "") {
                    $query .= "   AND B1.EXAMNO <= '".$model->field["E_EXAMNO"]."' ";
                }
            }
        } else if ($model->cmd == "back" || $model->hoji_cmd == "back") {
            if ($model->field["S_EXAMNO"] != "") {
                $query .= "   AND B1.EXAMNO < '".$model->field["S_EXAMNO"]."' ";
            }
        } else if ($model->cmd == "next" || $model->hoji_cmd == "next") {
            $query .= "   AND B1.EXAMNO > '".$model->field["E_EXAMNO"]."' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["SORT"] == "2") {
            $query .= "     B1.NAME, ";
        }
        $query .= "     B1.EXAMNO ";
        $query .= " FETCH FIRST 50 ROWS ONLY ";

        return $query;
    }

    //存在チェック ENTEXAM_APPLICANTBASE_DETAIL_DAT
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

        //存在チェック ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $D022      = $db->getCol(knjl582aQuery::checkEntexamApplicantbaseDetailDat($model, "022"));

        //更新
        $array = explode(",", $model->hid_examno);
        foreach ($array as $key => $examno) {
            // ENTEXAM_APPLICANTBASE_DAT
            $procedurediv = !strlen($model->data["PROCEDUREDIV"][$examno]) ? "" : $model->data["PROCEDUREDIV"][$examno];
            $entdiv = !strlen($model->data["ENTDIV"][$examno]) ? "" : $model->data["ENTDIV"][$examno];
            if($entdiv != "2"){
                if($procedurediv != ""){
                    $entdiv =  "1";
                } else{
                    $entdiv =  "";
                }
            }

            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = $procedurediv;
            $data["ENTDIV"][TEXT]           = $entdiv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $where .= "     EXAMNO          = '".$examno."' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            // ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:022
            $data = array();
            if (!in_array($examno, $D022)) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = '022';
            }
            $data["REMARK1"][TEXT]          = !strlen($model->data["REMARK1"][$examno]) ? "" : $model->data["REMARK1"][$examno];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if (!in_array($examno, $D022)) {
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
                $where .= "     EXAMNO          = '".$examno."' AND ";
                $where .= "     SEQ             = '022' ";

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