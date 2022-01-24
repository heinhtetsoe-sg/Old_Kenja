<?php

require_once('for_php7.php');

class knjl015gQuery extends Query {
    //学校コードを取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //名称マスタより取得
    function getNameMst($model, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験コース取得
    function getEntExamCourse($model) {
        $koteiCd = "";
        if ($model->isKeiai == "1")     $koteiCd = "1000"; //総合進学コース
        if ($model->isKasiwara == "1")  $koteiCd = "4000"; //スポーツコース

        $query  = " SELECT ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAMCOURSECD    = '".$koteiCd."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMCOURSECD ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst2($model, $namecd1, $label, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .=       $label." AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     L2.REMARK1 AS CLUB, ";
        $query .= "     L2.REMARK2 AS RANK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO       = L1.EXAMNO ";
        $query .= "         AND L1.SEQ          = '001' ";
        $query .= "         AND L1.REMARK8 || '-' || L1.REMARK9 || '-' || L1.REMARK10 = '".$model->examcoursecd."' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L2 ";
        $query .= "          ON T1.ENTEXAMYEAR  = L2.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO       = L2.EXAMNO ";
        $query .= "         AND L2.SEQ          = '019' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->testdiv."' AND ";
        $query .= "     T1.SHDIV        = '".$model->shdiv."' ";
        //志望クラブ（柏原のみ）
        if ($model->isKasiwara == "1") {
            $query .= "     AND L2.REMARK3 = '".$model->hope_club_cd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $examnoList = array();
        $examnoList = explode(",", $model->hid_examno);
        foreach ($examnoList as $examno) {
            $data = array();
            $data["REMARK1"][TEXT]      = $model->field["CLUB"][$examno];
            $data["REMARK2"][TEXT]      = $model->field["RANK"][$examno];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            //条件
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR = '".$model->year."' AND ";
            $where .= "     EXAMNO      = '".$examno."' AND ";
            $where .= "     SEQ         = '019' ";

            //データチェック
            $query  = " SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT " .$where;
            $cnt = $db->getOne($query);

            if (0 < $cnt) {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                $db->query($query);
            } else {
                if (strlen($model->field["CLUB"][$examno]) || strlen($model->field["RANK"][$examno])) {
                    $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                    $data["EXAMNO"][TEXT]       = $examno;
                    $data["SEQ"][TEXT]          = '019';
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>