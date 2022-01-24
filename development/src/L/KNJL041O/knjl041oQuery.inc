<?php

require_once('for_php7.php');

class knjl041oQuery extends Query {

    //名称
    function GetName($namecd1, $ObjYear, $namecd2 = "") {
        $query  = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst ";
        $query .= " WHERE year    = '".$ObjYear."' ";
        $query .= "   AND namecd1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "   AND namecd2 = '".$namecd2."' ";
        }
        $query .= "ORDER BY namecd2 ";
        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     NAME AS NAME1, ";
        $query .= "     SHOWDIV AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV ";
        return $query;
    }

    //会場コンボ
    function getHall($model) {
        $query  = " SELECT ";
        $query .= "     H1.EXAMHALLCD, ";
        $query .= "     H1.EXAMHALL_NAME, ";
        $query .= "     H1.S_RECEPTNO, ";
        $query .= "     H1.E_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT H1 ";
        $query .= " WHERE ";
        $query .= "         H1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND H1.EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= " ORDER BY ";
        $query .= "     H1.EXAMHALLCD ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     R1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B1.SEX, ";
        $query .= "     N1.NAME2 AS SEX_NAME, ";
        $query .= "     RD.REMARK1 AS TARGET_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_HALL_DAT H1 ";
        $query .= "          ON H1.TESTDIV = R1.TESTDIV ";
        $query .= "         AND H1.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "         AND H1.EXAMHALLCD = '".$model->hallcd."' ";
        $query .= "         AND R1.RECEPTNO BETWEEN H1.S_RECEPTNO AND H1.E_RECEPTNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR AND B1.EXAMNO = R1.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = B1.SEX ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD ";
        $query .= "          ON RD.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND RD.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND RD.TESTDIV = R1.TESTDIV ";
        $query .= "         AND RD.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "         AND RD.RECEPTNO = R1.RECEPTNO ";
        $query .= "         AND RD.SEQ = '010' ";
        $query .= " WHERE ";
        $query .= "     R1.ENTEXAMYEAR      = '".$model->ObjYear."' ";
        $query .= "     AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND R1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND R1.EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= " ORDER BY ";
        if ($model->sort == "2") {
            $query .= "     R1.EXAMNO ";
        } else {
            $query .= "     R1.RECEPTNO ";
        }

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->setReceptNo as $key => $receptno) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["TESTDIV"][TEXT]      = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]    = $model->exam_type;
            $data["RECEPTNO"][TEXT]     = $receptno;
            $data["SEQ"][TEXT]          = '010';
            $data["REMARK1"][TEXT]      = ($model->arrTargetFlg[$receptno]) ? $model->arrTargetFlg[$receptno] : "";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."'";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND TESTDIV       = '".$model->testdiv."'";
            $where .= "   AND EXAM_TYPE     = '".$model->exam_type."'";
            $where .= "   AND RECEPTNO      = '".$receptno."'";
            $where .= "   AND SEQ           = '010'";

            $dataCnt = $db->getOne("SELECT COUNT(*) FROM ENTEXAM_RECEPT_DETAIL_DAT".$where);
            if (0 < $dataCnt) {
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>