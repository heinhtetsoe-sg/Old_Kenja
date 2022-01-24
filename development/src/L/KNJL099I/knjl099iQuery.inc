<?php
class knjl099iquery extends Query
{
    public function getNameMst($model, $namecd1, $namecd2 = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->examYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2 != '') {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試制度名の取得
    public function getApplicantDivName($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '{$model->examYear}' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= "   AND NAMECD2 = '{$model->field["APPLICANTDIV"]}' ";

        return $query;
    }

    public function selectCsvQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        // 1:中学 2:高校
        if ($model->field["APPLICANTDIV"] == '2' || $model->field["APPLICANTDIV"] == '1') {
            $query .= "     case ";
            $query .= "         when T1.FS_CD = T2.NAME1 then '1' ";
            $query .= "         else '0' ";
            $query .= "     end AS HONGAKUTAGAKU_KUBUN, ";
        } else {
            $query .= "     '' AS HONGAKUTAGAKU_KUBUN, ";
        }
        $query .= "     T1.NAME AS NAME_1, ";
        $query .= "     TRANSLATE_HK_K(T1.NAME_KANA) AS NAME_KANA_1, ";
        $query .= "     case ";
        $query .= "         when T1.JUDGEMENT = '1' then '合格者' ";
        $query .= "         else '志願者' ";
        $query .= "     end AS OPTIONAL_SETTING_2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.SETTING_CD  = 'L073' ";
        $query .= "         AND T2.SEQ  = '01' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
