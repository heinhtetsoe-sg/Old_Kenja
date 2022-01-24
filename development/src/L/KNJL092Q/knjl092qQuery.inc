<?php

require_once('for_php7.php');

class knjl092qQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T3.REMARK1 AS SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     N4.NAME1 AS SEX, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T5.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "       ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "      AND N1.NAMECD1      = 'L013' ";
        $query .= "      AND N1.NAMECD2      = T1.JUDGEMENT ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ";
        $query .= "       ON N2.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "      AND N2.NAMECD1      = 'L006' ";
        $query .= "      AND N2.NAMECD2      = T1.SHDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ";
        $query .= "       ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "      AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T3.EXAMNO       = T1.EXAMNO ";
        $query .= "      AND T3.SEQ          = '026' ";
        $query .= "     LEFT JOIN V_NAME_MST N4 ";
        $query .= "       ON N4.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "      AND N4.NAMECD1      = 'Z002' ";
        $query .= "      AND N4.NAMECD2      = T1.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST T5 ";
        $query .= "       ON T5.FINSCHOOLCD = T1.FS_CD";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR                 = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV            = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV <> '9' ";
        //合格者が対象
        $query .= "     AND N1.NAMESPARE1              = '1' ";
        //入学決定者のみ(辞退者は除く)
        $query .= "     AND VALUE(T1.PROCEDUREDIV,'')  = '1' ";    //手続区分 1:済み 2:無し（辞退）
        $query .= "     AND VALUE(T1.ENTDIV,'')        = '1' ";        //入学区分 1:済み 2:辞退
        $query .= " ORDER BY ";
        $query .= "     TRANSLATE_KANA(T1.NAME_KANA), ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //更新 ENTEXAM_INTERVIEW_DAT
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $seq = '026';

        //削除
        $query  = " UPDATE ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT T1 ";
        $query .= " SET ";
        $query .= "   T1.REMARK1 = '' ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "   AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND T1.SEQ = '".$seq."' ";
        $query .= "   AND T1.EXAMNO IN ( ";
        $query .= "                     SELECT ";
        $query .= "                       T2.EXAMNO ";
        $query .= "                     FROM ";
        $query .= "                       ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "                       LEFT JOIN V_NAME_MST N2 ";
        $query .= "                         ON N2.YEAR         = T2.ENTEXAMYEAR ";
        $query .= "                        AND N2.NAMECD1      = 'L013' ";
        $query .= "                        AND N2.NAMECD2      = T2.JUDGEMENT ";
        $query .= "                     WHERE ";
        $query .= "                       T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                       AND T2.APPLICANTDIV  = T1.APPLICANTDIV ";
        $query .= "                       AND T2.TESTDIV       <> '9' ";   //外生のみ
        $query .= "                       AND N2.NAMESPARE1    = '1' ";    //合格者
        $query .= "                       AND VALUE(T2.PROCEDUREDIV,'')  = '1' ";  //入学者
        $query .= "                       AND VALUE(T2.ENTDIV,'')        = '1' ";  //入学者
        $query .= "                    ) ";

        $db->query($query);

        //追加
        foreach ($model->schregNo as $exNo => $schNo) {
            $data = array();
            $data["REMARK1"][TEXT]    = $schNo;
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "SYSDATE()";
            $query = knjl092qQuery::getCount($model, $exNo);
            $cnt = $db->getOne($query);
            if ($cnt > 0) {
                $where  = " WHERE ";
                $where .= "   ENTEXAMYEAR = '".$model->ObjYear."' ";
                $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
                $where .= "   AND SEQ = '".$seq."' ";
                $where .= "   AND EXAMNO = '".$exNo."' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $exNo;
                $data["SEQ"][TEXT]          = $seq;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //面接評価の存在確認
    public function getCount($model, $exNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*)";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND EXAMNO = '".$exNo."' ";
        $query .= "     AND SEQ = '026' ";

        return $query;
    }
}
?>