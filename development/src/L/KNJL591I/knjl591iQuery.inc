<?php
class knjl591iQuery extends Query
{
    //一覧表示
    public function selectQuery($model)
    {
        $query  = "";
        $query .= " WITH GOKAKU_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR,";
        $query .= "         APPLICANTDIV, ";
        $query .= "         SEQ ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SETTING_MST ";
        $query .= "     WHERE ";
        $query .= "         SETTING_CD = 'L013' ";
        $query .= "         AND NAMESPARE1 = '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA,  ";
        $query .= "     BD026.REMARK1 AS STUDENTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          ON R1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND R1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND R1.EXAMNO       = B1.EXAMNO ";
        $query .= "     INNER JOIN GOKAKU_SEQ G1 ";
        $query .= "          ON G1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND G1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND G1.SEQ          = R1.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD026 ";
        $query .= "          ON BD026.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND BD026.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND BD026.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND BD026.SEQ          = '026' ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR      = '{$model->objYear}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV0     = '{$model->majorcd}' ";
        $query .= "     AND B1.ENTDIV       = '1' "; //入学者(手続終了者)
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

        foreach ($model->studentnoArray as $examno => $studentno) {
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
