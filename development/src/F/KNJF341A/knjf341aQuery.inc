<?php

require_once('for_php7.php');

class knjf341aQuery extends Query
{
    //教育委員会チェック
    public function getNameMst()
    {
        $query  = " SELECT ABBV1 ";
        $query .= "   FROM NAME_MST ";
        $query .= "  WHERE NAMECD1 = 'Z010' ";
        $query .= "    AND NAMECD2 = '00' ";

        return $query;
    }

    //年度
    public function getYear()
    {
        $query  = "   SELECT '".CTRL_YEAR."' AS VALUE ";
        $query .= "        , '".CTRL_YEAR."' AS LABEL ";
        $query .= "     FROM SYSIBM.SYSDUMMY1 ";
        $query .= "    UNION ";
        $query .= "   SELECT YEAR AS VALUE ";
        $query .= "        , YEAR AS LABEL ";
        $query .= "     FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT ";
        $query .= " GROUP BY YEAR ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //対象学校
    public function getSchoolData($model)
    {
        $query = '';
        $query .= "    SELECT DISTINCT ";
        $query .= "           CASE WHEN FD.EDBOARD_SCHOOLCD IS NOT NULL THEN '【済】' ELSE '【未】' END AS FLG_NAME ";
        $query .= "         , CASE WHEN FD.EDBOARD_SCHOOLCD IS NOT NULL THEN '0'      ELSE '1'      END AS FLG ";
        $query .= "         , ESM.EDBOARD_SCHOOLCD ";
        $query .= "         , ESM.EDBOARD_SCHOOLNAME ";
        $query .= "      FROM EDBOARD_SCHOOL_MST ESM ";
        $query .= " LEFT JOIN EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT AS FD ";
        $query .= "        ON (FD.EDBOARD_SCHOOLCD = ESM.EDBOARD_SCHOOLCD) ";
        $query .= "       AND (FD.YEAR = '{$model->field["YEAR"]}') ";
        $query .= "  ORDER BY ESM.EDBOARD_SCHOOLCD ";

        return $query;
    }

    //対象学校のCSVデータ
    public function getCsvData($model)
    {
        $year = $model->field['YEAR'];

        //選択された学校コードを取得
        $schoolCodes = array();
        $tempSeleted = explode(',', $model->selectleft);
        foreach ($tempSeleted as $v) {
            $schoolCodes[] = "'" . substr($v, -4) . "'";//右側4文字が学校コードに相当
        }
        $schoolCodesStr = implode(',', $schoolCodes);

        $query = '';
        $query .= " WITH MAX_FIXED_DATE AS ( ";
        $query .= "       SELECT EMSU.EDBOARD_SCHOOLCD ";
        $query .= "            , MAX(FIXED_DATE) AS FIXED_DATE ";
        $query .= "         FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT EMSU ";
        $query .= "        WHERE EMSU.YEAR = '{$year}' ";
        $query .= "     GROUP BY EMSU.EDBOARD_SCHOOLCD ";
        $query .= " ) ";
        $query .= "      SELECT MAIN.SCHOOLNAME ";
        $query .= "           , MAIN.TARGETS1 ";
        $query .= "           , MAIN.EXAMINEE1 ";
        $query .= "           , MAIN.PERCENT1 ";
        $query .= "           , MAIN.REMARK1 ";
        $query .= "           , MAIN.URICSUGAR1 ";
        $query .= "           , MAIN.ALBUMINURIA1 ";
        $query .= "           , MAIN.URICBLEED1 ";
        $query .= "           , MAIN.TARGETS2 ";
        $query .= "           , MAIN.EXAMINEE2 ";
        $query .= "           , MAIN.PERCENT2 ";
        $query .= "           , MAIN.TARGETS3 ";
        $query .= "           , MAIN.EXAMINEE3 ";
        $query .= "           , MAIN.NORMAL ";
        $query .= "           , MAIN.CAREFUL ";
        $query .= "           , MAIN.TREATMENT ";
        $query .= "           , MAIN.PERCENT3 ";
        $query .= "        FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT MAIN ";
        $query .= "       WHERE MAIN.YEAR = '{$year}' ";
        if ($schoolCodesStr != '') {
            $query .= "         AND MAIN.EDBOARD_SCHOOLCD IN ($schoolCodesStr) ";
        }
        $query .= "    ORDER BY MAIN.EDBOARD_SCHOOLCD ASC ";

        return $query;
    }
}
