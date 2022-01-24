<?php

require_once('for_php7.php');

class knjl080dQuery extends Query {
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

    //内部判定マスタより取得
    function getEntexamInternalDecisionMst($model) {
        $query  = " SELECT ";
        $query .= "     DECISION_CD AS VALUE, ";
        $query .= "     DECISION_CD || ':' || DECISION_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_INTERNAL_DECISION_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        $query .= "     B1.SUB_ORDER, ";
        $query .= "     B1.SHDIV, ";
        $query .= "     CASE WHEN B1.SHDIV = '1' THEN N1.NAME1 END AS SHDIV_NAME, ";
        $query .= "     B1.DESIREDIV, ";
        $query .= "     N2.ABBV1 AS DESIREDIV_NAME, ";
        $query .= "     B1.JUDGEMENT, ";
        $query .= "     N3.NAME1 AS JUDGEMENT_NAME, ";
        $query .= "     B1.ENTDIV, ";
        $query .= "     N4.NAME1 AS ENTDIV_NAME, ";
        $query .= "     B1.PROCEDUREDIV, ";
        $query .= "     N5.NAME1 AS PROCEDUREDIV_NAME, ";
        $query .= "     D1.REMARK2 AS JITAI, ";
        $query .= "     B1.JUDGE_KIND, ";
        $query .= "     N6.NAME1 AS JUDGE_KIND_NAME";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ";
        $query .= "          ON F1.FINSCHOOLCD  = B1.FS_CD ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "          ON N1.NAMECD1      = 'L006' ";
        $query .= "         AND N1.NAMECD2      = B1.SHDIV ";
        $query .= "     LEFT JOIN NAME_MST N2 ";
        $query .= "          ON N2.NAMECD1      = 'L058' ";
        $query .= "         AND N2.NAMECD2      = B1.DESIREDIV ";
        $query .= "     INNER JOIN NAME_MST N3 ";
        $query .= "          ON N3.NAMECD1      = 'L013' ";
        $query .= "         AND N3.NAMECD2      = B1.JUDGEMENT ";
        $query .= "         AND N3.NAMESPARE1   = '1' ";
        $query .= "     LEFT JOIN NAME_MST N4 ";
        $query .= "          ON N4.NAMECD1      = 'L012' ";
        $query .= "         AND N4.NAMECD2      = B1.ENTDIV ";
        $query .= "     LEFT JOIN NAME_MST N5 ";
        $query .= "          ON N5.NAMECD1      = 'L011' ";
        $query .= "         AND N5.NAMECD2      = B1.PROCEDUREDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D1 ";
        $query .= "          ON D1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND D1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND D1.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND D1.SEQ          = '022' ";
        $query .= "     LEFT JOIN NAME_MST N6 ";
        $query .= "          ON N6.NAMECD1      = 'L025' ";
        $query .= "         AND N6.NAMECD2      = B1.JUDGE_KIND ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND B1.TESTDIV      = '".$model->testdiv."' ";
        if (strlen($model->sub_order)) {
            $query .= "     AND B1.SUB_ORDER = '".$model->sub_order."' ";
        }
        if (strlen($model->desirediv)) {
            $query .= "     AND B1.DESIREDIV = '".$model->desirediv."' ";
        }
        if (strlen($model->judgement)) {
            $query .= "     AND B1.JUDGEMENT = '".$model->judgement."' ";
        }
        $query .= " ORDER BY ";
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
        $D022 = $db->getCol(knjl080dQuery::checkEntexamApplicantbaseDetailDat($model, "022"));
        $D007 = $db->getCol(knjl080dQuery::checkEntexamApplicantbaseDetailDat($model, "007"));

        //入学コース一覧取得
        $courseArray = array();
        $query = knjl080dQuery::getNameMst($model->ObjYear, "L012");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $courseArray[$row["VALUE"]] = array($row["ABBV3"], $row["NAMESPARE1"], $row["NAMESPARE2"]);
        }

        //更新
        $array = explode(",", $model->hid_examno);
        foreach ($array as $key => $examno) {
            // ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTDIV"][TEXT]           = !strlen($model->data["ENTDIV"][$examno]) ? "" : $model->data["ENTDIV"][$examno];
            $data["PROCEDUREDIV"][TEXT]     = !strlen($model->data["PROCEDUREDIV"][$examno]) ? "" : $model->data["PROCEDUREDIV"][$examno];
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
            $data["REMARK2"][TEXT]          = !strlen($model->data["JITAI"][$examno]) ? "" : $model->data["JITAI"][$examno];
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

            // ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:007
            $coursecd = $majorcd = $coursecode = "";
            if (strlen($model->data["ENTDIV"][$examno])) list ($coursecd, $majorcd, $coursecode) = $courseArray[$model->data["ENTDIV"][$examno]];
            $data = array();
            if (!in_array($examno, $D007)) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = '007';
            }
            $data["REMARK1"][TEXT]          = $coursecd;
            $data["REMARK2"][TEXT]          = $majorcd;
            $data["REMARK3"][TEXT]          = $coursecode;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if (!in_array($examno, $D007)) {
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
                $where .= "     EXAMNO          = '".$examno."' AND ";
                $where .= "     SEQ             = '007' ";

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