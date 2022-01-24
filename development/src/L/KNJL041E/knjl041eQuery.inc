<?php

require_once('for_php7.php');

class knjl041eQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場コンボ
    function getExamHallCd($model) {
        $query  = " SELECT ";
        $query .= "     HALL.EXAMHALLCD AS VALUE, ";
        $query .= "     HALL.EXAMHALL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT HALL ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$model->examType."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     H1.EXAMHALLGROUPCD AS GROUPNAME, ";
        $query .= "     YDAT.EXAMHALL_NAME, ";
        $query .= "     L058.NAME1 AS DESIREDIV, ";
        $query .= "     L045.NAME1 AS TESTDIV1, ";
        $query .= "     H1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     FM1.FINSCHOOL_NAME_ABBV, ";
        $query .= "     T1.JUDGEMENT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_GROUP_DAT H1 ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_YDAT YDAT ";
        $query .= "          ON YDAT.ENTEXAMYEAR  = H1.ENTEXAMYEAR ";
        $query .= "         AND YDAT.APPLICANTDIV = H1.APPLICANTDIV ";
        $query .= "         AND YDAT.TESTDIV      = H1.TESTDIV ";
        $query .= "         AND YDAT.EXAM_TYPE    = H1.EXAMHALL_TYPE ";
        $query .= "         AND YDAT.EXAMHALLCD   = H1.EXAMHALLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = H1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = H1.APPLICANTDIV ";
        $query .= "         AND T1.TESTDIV      = H1.TESTDIV ";
        $query .= "         AND T1.EXAMNO       = H1.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FM1 ";
        $query .= "         ON T1.FS_CD = FM1.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST L058 ON L058.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                              AND L058.NAMECD1 = 'L058' ";
        $query .= "                              AND L058.NAMECD2 = T1.DESIREDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L045 ON L045.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                              AND L045.NAMECD1 = 'L045' ";
        $query .= "                              AND L045.NAMECD2 = T1.TESTDIV1 ";
        $query .= " WHERE ";
        $query .= "         H1.ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $query .= "     AND H1.APPLICANTDIV  = '".$model->applicantdiv."' ";
        $query .= "     AND H1.TESTDIV       = '".$model->testdiv."' ";
        $query .= "     AND H1.EXAMHALL_TYPE = '".$model->examType."' ";
        if ($model->examhallCd != "" && $model->examhallCd != "9999") {
            $query .= "  AND H1.EXAMHALLCD = '".$model->examhallCd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     H1.EXAMHALLCD, ";
        $query .= "     H1.EXAMHALLGROUPCD, ";
        $query .= "     INT(H1.EXAMHALLGROUP_ORDER), ";
        $query .= "     H1.EXAMNO ";

        return $query;
    }

/***** 更新処理 *****/
    //データ取得(ENTEXAM_INTERVIEW_DAT)
    function getCountEntExamApplicantBaseDat($model, $examNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND EXAMNO       = '".$examNo."' ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $updflg = false;
        foreach ($model->setExamNo as $key => $examno) {
            //更新(ENTEXAM_APPLICANTBASE_DAT)
            //チェックボックスの入力制限となっている(NULL、0、3以外)場合は除外
            if ($model->arrBkAb[$examno] != "3" && $model->arrBkAb[$examno] != "" && $model->arrBkAb[$examno] != "0") {
                continue;
            }
            $data = array();
            //変更箇所(チェックついてて元が3以外、チェックついてなくて元が3)を更新する。
            if ($model->arrTesAb[$examno]) {
                if ($model->arrBkAb[$examno] != "3") {
                    $data["JUDGEMENT"][TEXT] = "3";
                } else {
                    continue;
                }
            } else {
                if ($model->arrBkAb[$examno] == "3") {
                    $data["JUDGEMENT"][TEXT] = "";
                } else {
                    continue;
                }
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO       = '".$examno."' ";
            $where .= "   AND (VALUE(JUDGEMENT, '3') = '3' OR JUDGEMENT = '0') "; //欠席またはNULLまたは0

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
            $updflg = true;
        }

        $db->commit();
        Query::dbCheckIn($db);
        return $updflg;
    }
}
?>