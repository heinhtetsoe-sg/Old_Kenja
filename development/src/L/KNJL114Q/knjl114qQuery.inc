<?php

require_once('for_php7.php');

class knjl114qQuery extends Query {

    //一覧表示
    function SelectQuery($model, $div) {

        $query  = " SELECT ";
        $query .= "    VBASE.EXAMNO, ";
        $query .= "    S035.REMARK1 AS TMPCLS, ";
        $query .= "    L063.NAME1 AS CLSNAME, ";
        $query .= "    S035.REMARK2 AS NO, ";
        $query .= "    VBASE.NAME, ";
        $query .= "    S035.REMARK3 AS ART_SELECT_FST_VAL, ";
        $query .= "    S035.REMARK4 AS ART_SELECT_SND_VAL ";
        $query .= " FROM ";
        $query .= "    V_ENTEXAM_APPLICANTBASE_DAT VBASE ";
        //入試合格者(BASE.JUDGEMENTと紐づく名称ML013.NAMESPARE1=1のデータ)が対象
        $query .= "    INNER JOIN NAME_MST L013 ";
        $query .= "       ON L013.NAMECD2 = VBASE.JUDGEMENT ";
        $query .= "      AND L013.NAMECD1 = 'L013' ";
        $query .= "      AND L013.NAMESPARE1 = '1' ";
        $query .= "    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S035 ";
        $query .= "      ON S035.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ";
        $query .= "     AND S035.APPLICANTDIV = VBASE.APPLICANTDIV ";
        $query .= "     AND S035.EXAMNO       = VBASE.EXAMNO ";
        $query .= "     AND S035.SEQ          = '035' ";
        $query .= "    LEFT JOIN NAME_MST L063 ";
        $query .= "      ON L063.NAMECD2 = S035.REMARK1 ";
        $query .= "     AND L063.NAMECD1 = 'L063' ";
        $query .= " WHERE ";
        $query .= "    VBASE.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "    AND VBASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "    AND value(VBASE.ENTDIV, '')       = '1' "; // 入学区分
        $query .= "    AND value(VBASE.PROCEDUREDIV, '') = '1' "; // 手続き区分
        $query .= "ORDER BY ";
        $query .= "    VBASE.EXAMNO ";

        return $query;
    }

    function getCntDataQuery($model, $examno) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND EXAMNO = '{$examno}' ";
        $query .= "   AND SEQ = '035' ";

        return $query;
    }
    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //追加
        foreach ($model->examNoLst as $key => $examno) {

            $cnt = $db->getOne(knjl114qQuery::getCntDataQuery($model, $examno));

            $data = array();
            $data["REMARK3"][TEXT]    = !strlen($model->artSelFstLst[$examno]) ? "" : $model->artSelFstLst[$examno];
            $data["REMARK4"][TEXT]    = !strlen($model->artSelSndLst[$examno]) ? "" : $model->artSelSndLst[$examno];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "SYSDATE()";

            if ($cnt > 0) {
                $where  = " WHERE ENTEXAMYEAR = '".$model->ObjYear."' ";
                $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
                $where .= "   AND EXAMNO = '".$examno."' ";
                $where .= "   AND SEQ = '035' ";
                
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = "035";
                $data["REMARK1"][TEXT]      = "";  //存在しない場合、組、番号が入っていないので空で設定。
                $data["REMARK2"][TEXT]      = "";  //存在しない場合、組、番号が入っていないので空で設定。
                
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>