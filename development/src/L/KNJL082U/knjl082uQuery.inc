<?php

require_once('for_php7.php');

class knjl082uQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B2.REMARK1 AS SCHREGNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B2 ";
        $query .= "          ON B2.ENTEXAMYEAR      = B1.ENTEXAMYEAR ";
        $query .= "         AND B2.APPLICANTDIV     = B1.APPLICANTDIV ";
        $query .= "         AND B2.EXAMNO           = B1.EXAMNO ";
        $query .= "         AND B2.SEQ              = '026' ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR         = B1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1      = 'L013' ";
        $query .= "         AND N1.NAMECD2      = B1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND N1.NAMESPARE1   = '1' "; //合格者が対象
        $query .= "     AND B1.PROCEDUREDIV = '1' "; //手続者が対象
        $query .= "     AND B1.ENTDIV       = '1' "; //入学者が対象
        $query .= "     AND VALUE(B1.JUDGEMENT,'0') != '5' "; //受験不可は除く
        $query .= " ORDER BY ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B1.EXAMNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->schregno as $examno => $schregno) {
            $seq = "026";
            //学籍番号
            $data = array();
            $data["REMARK1"][TEXT]              = $schregno;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            //WHERE
            $where  = " WHERE ";
            $where .= "         ENTEXAMYEAR     = '{$model->ObjYear}' ";
            $where .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
            $where .= "     AND EXAMNO          = '{$examno}' ";
            $where .= "     AND SEQ             = '{$seq}' ";
            //COUNT
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $getCount = $db->getOne($query.$where);
            if ($getCount == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = $seq;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>