<?php
class knjl691hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学籍番号重複チェック
    public function isExistStudentno($model, $examno, $studentno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE  ";
        $query .= "     ENTEXAMYEAR      = '{$model->objYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO       <> '{$examno}' ";
        $query .= "     AND SEQ          = '026' ";
        $query .= "     AND REMARK1      = '{$studentno}' "; //重複番号

        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return (($row == 0) ? false : true);
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA,  ";
        $query .= "     BD026.REMARK1 AS STUDENTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          ON R1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND R1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND R1.EXAMNO       = B1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_SETTING_MST S1 ";
        $query .= "          ON S1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND S1.SETTING_CD   = 'L013' ";
        $query .= "         AND S1.SEQ          = B1.JUDGEMENT ";
        $query .= "         AND S1.NAMESPARE1   = '1' "; //合格者のみ
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD026 ";
        $query .= "            ON BD026.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND BD026.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND BD026.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND BD026.SEQ          = '026' ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR      = '{$model->objYear}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND B1.PROCEDUREDIV = '1' ";      //手続済
        $query .= "     AND VALUE(B1.ENTDIV,'') != '2' "; //辞退者は除く
        $query .= " ORDER BY ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B1.EXAMNO ";
        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->studentnoArray as $receptno => $studentno) {
            $examno = $model->examnoArray[$receptno];

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:026
            $seq = "026";
            $data = array();
            $data["REMARK1"][TEXT]      = $studentno;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->objYear}' ";
            $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO       = '{$examno}' ";
            $where .= "   AND SEQ          = '{$seq}' ";

            //件数取得
            $cnt = $db->getOne(" SELECT COUNT(*) AS CNT FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
            if ($cnt == 0) {
                //追加
                $data["ENTEXAMYEAR"][TEXT]  = $model->objYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = $seq;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
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
