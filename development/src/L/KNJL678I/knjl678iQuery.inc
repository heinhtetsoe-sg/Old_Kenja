<?php

require_once('for_php7.php');

class knjl678iQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAME2, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //出身学校取得
    public function getFinshoolMst()
    {
        $query  = " SELECT ";
        $query .= "     FINSCHOOLCD AS VALUE, ";
        $query .= "     FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // TODO 欠席の処理が未実装
    public function getApplicantList($model)
    {
        $query  = " WITH ADV_HIST_MAX AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         EXAMNO, ";
        $query .= "         MAX(CHANGE_DATE) AS CHANGE_DATE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_HIST_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         EXAMNO ";
        $query .= " ), ";
        $query .= " ADV_HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         HIST.ENTEXAMYEAR, ";
        $query .= "         HIST.EXAMNO, ";
        $query .= "         HIST.CHANGE_TEXT ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_HIST_DAT HIST ";
        $query .= "         INNER JOIN ADV_HIST_MAX ";
        $query .= "                 ON ADV_HIST_MAX.ENTEXAMYEAR = HIST.ENTEXAMYEAR ";
        $query .= "                AND ADV_HIST_MAX.EXAMNO      = HIST.EXAMNO ";
        $query .= "                AND ADV_HIST_MAX.CHANGE_DATE = HIST.CHANGE_DATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        if (($model->field["OUTPUT"] == "1") || ($model->field["OUTPUT"] == "3")) {
            // 1:中学校別 か 3:特待学業が選択された場合は、中学校を改頁制御用の項目として扱う
            $query .= "    ADV.FS_CD AS SEPARATE_CD, ";
        } elseif ($model->field["OUTPUT"] == "2") {
            // 2:地区別が選択された場合は、地区を改頁制御用の項目として扱う
            $query .= "    ADV.FS_AREA_CD AS SEPARATE_CD, ";
        } elseif ($model->field["OUTPUT"] == "4") {
            // 4:特待部活動が選択された場合は、特待理由を改頁制御用の項目として扱う
            $query .= "    VALUE(ADV_D4.REMARK1, '') AS SEPARATE_CD, ";
        } elseif ($model->field["OUTPUT"] == "5") {
            // 5:欠席が選択された場合は、改頁制御用の項目は無しとして扱う
            $query .= "    '' AS SEPARATE_CD, ";
        }
        $query .= "     ADV.EXAMNO, ";
        if ($model->field["OUTPUT"] == "3") {
            $query .= "     VALUE(L001.NAME1, 'その他') AS FS_AREA_NAME, ";
        } else {
            $query .= "     VALUE(L001.NAME1, '') AS FS_AREA_NAME, ";
        }
        $query .= "     FIN_MST.FINSCHOOL_NAME, ";
        $query .= "     ADV.NAME, ";
        $query .= "     TESTDIV_T.TESTDIV_NAME, ";
        $query .= "     GENE02.GENERAL_NAME AS CONSUL_COURSE, ";
        $query .= "     ADV_D3.REMARK1 AS COMMON_TEST, ";
        $query .= "     GENE04.GENERAL_NAME AS SP_CD, ";
        $query .= "     GENE05.GENERAL_NAME AS SP_REASON, ";
        $query .= "     ADV_D6.REMARK1 AS REMARK, ";
        $query .= "     ADV_HIST.CHANGE_TEXT, ";
        $query .= "     CASE WHEN ADV.EXCLUSION = '1' THEN '除外' ELSE '' END AS EXCLUSION ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DAT ADV ";
        $query .= "     LEFT JOIN V_NAME_MST L001 ";
        $query .= "            ON L001.YEAR    = ADV.ENTEXAMYEAR ";
        $query .= "           AND L001.NAMECD1 = 'L001' ";
        $query .= "           AND L001.NAMECD2 = ADV.FS_AREA_CD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN_MST ";
        $query .= "            ON FIN_MST.FINSCHOOLCD = ADV.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D1 ";
        $query .= "            ON ADV_D1.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D1.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D1.SEQ         = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST TESTDIV_T ";
        $query .= "           ON TESTDIV_T.ENTEXAMYEAR  = ADV_D1.ENTEXAMYEAR ";
        $query .= "          AND TESTDIV_T.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "          AND TESTDIV_T.TESTDIV      = ADV_D1.REMARK1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D2 ";
        $query .= "            ON ADV_D2.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D2.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D2.SEQ         = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GENE02 ";
        $query .= "            ON GENE02.ENTEXAMYEAR  = ADV_D2.ENTEXAMYEAR ";
        $query .= "           AND GENE02.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "           AND GENE02.TESTDIV      = '0' ";
        $query .= "           AND GENE02.GENERAL_DIV  = '02' ";
        $query .= "           AND GENE02.GENERAL_CD   = ADV_D2.REMARK1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D3 ";
        $query .= "            ON ADV_D3.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D3.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D3.SEQ         = '3' ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D4 ";
        $query .= "            ON ADV_D4.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D4.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D4.SEQ         = '004' ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GENE04 ";
        $query .= "            ON GENE04.ENTEXAMYEAR  = ADV_D4.ENTEXAMYEAR ";
        $query .= "           AND GENE04.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "           AND GENE04.TESTDIV      = '0' ";
        $query .= "           AND GENE04.GENERAL_DIV  = '04' ";
        $query .= "           AND GENE04.GENERAL_CD   = ADV_D4.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GENE05 ";
        $query .= "            ON GENE05.ENTEXAMYEAR  = ADV_D4.ENTEXAMYEAR ";
        $query .= "           AND GENE05.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "           AND GENE05.TESTDIV      = '0' ";
        $query .= "           AND GENE05.GENERAL_DIV  = '05' ";
        $query .= "           AND GENE05.GENERAL_CD   = ADV_D4.REMARK1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D6 ";
        $query .= "            ON ADV_D6.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D6.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D6.SEQ         = '006' ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D8 ";
        $query .= "            ON ADV_D8.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D8.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D8.SEQ         = '008' ";
        $query .= "     LEFT JOIN ADV_HIST ";
        $query .= "            ON ADV_HIST.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_HIST.EXAMNO      = ADV.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     ADV.ENTEXAMYEAR = '".$model->examyear."' ";
        if ($model->field["OUTPUT"] == "1") {
            if ($model->field["FINSCHOOLCD"] != "ALL") {
                $query .= " AND ADV.FS_CD = '".$model->field["FINSCHOOLCD"]."' ";
            }
        } elseif ($model->field["OUTPUT"] == "2") {
            if ($model->field["FS_AREA_CD"] == "OTHER") {
                $query .= " AND ADV.FS_AREA_CD IS NULL ";
            } elseif ($model->field["FS_AREA_CD"] != "ALL") {
                $query .= " AND ADV.FS_AREA_CD = '".$model->field["FS_AREA_CD"]."' ";
            }
        } elseif ($model->field["OUTPUT"] == "3") {
            $query .= " AND ADV_D4.REMARK1 = '01' "; // 01:学業
        } elseif ($model->field["OUTPUT"] == "4") {
            $query .= " AND ADV_D4.REMARK1 <> '01' "; // 01:学業 以外
        } elseif ($model->field["OUTPUT"] == "5") {
            $query .= " AND ADV_D8.REMARK1 IS NOT NULL "; // 欠席日数がある人
        }
        $query .= " ORDER BY ";
        if ($model->field["OUTPUT"] == "4") {
            $query .= "     VALUE(ADV_D4.REMARK1, 'ZZZ'), ";
        }
        $query .= "     VALUE(ADV.FS_AREA_CD, 'ZZZZZZZZZZZZZ'), ";
        $query .= "     VALUE(ADV.FS_CD, 'ZZZZZZZZZZZZZ'), ";
        if ($model->field["OUTPUT"] == "3") {
            $query .= "     VALUE(ADV_D4.REMARK2, 'ZZ'), ";
        }
        $query .= "     ADV.NAME ";

        return $query;
    }
}
