<?php

require_once('for_php7.php');

class knjc201Query extends Query
{
    //年組およびＨＲ担任
    public function getHrName($model, $sort)
    {
        $query  = "   SELECT T1.GRADE ";
        $query .= "        , T1.HR_CLASS ";
        $query .= "        , HR_NAME ";
        $query .= "        , T2.GRADE_NAME1 ";
        $query .= "        , TR_CD1 ";
        $query .= "        , STAFFNAME ";
        $query .= "        , STAFFNAME_KANA ";
        $query .= "        , CASE WHEN STAFFNAME_KANA IS NOT NULL THEN ";
        $query .= "               CASE WHEN 'あ' <= SUBSTR(STAFFNAME_KANA,1,3) AND SUBSTR(STAFFNAME_KANA,1,3) < 'か' THEN 'あ' ";
        $query .= "                    WHEN 'か' <= SUBSTR(STAFFNAME_KANA,1,3) AND SUBSTR(STAFFNAME_KANA,1,3) < 'さ' THEN 'か' ";
        $query .= "                    WHEN 'さ' <= SUBSTR(STAFFNAME_KANA,1,3) AND SUBSTR(STAFFNAME_KANA,1,3) < 'た' THEN 'さ' ";
        $query .= "                    WHEN 'た' <= SUBSTR(STAFFNAME_KANA,1,3) AND SUBSTR(STAFFNAME_KANA,1,3) < 'な' THEN 'た' ";
        $query .= "                    WHEN 'な' <= SUBSTR(STAFFNAME_KANA,1,3) AND SUBSTR(STAFFNAME_KANA,1,3) < 'は' THEN 'な' ";
        $query .= "                    WHEN 'は' <= SUBSTR(STAFFNAME_KANA,1,3) AND SUBSTR(STAFFNAME_KANA,1,3) < 'ま' THEN 'は' ";
        $query .= "                    WHEN 'ま' <= SUBSTR(STAFFNAME_KANA,1,3) AND SUBSTR(STAFFNAME_KANA,1,3) < 'や' THEN 'ま' ";
        $query .= "                    WHEN 'や' <= SUBSTR(STAFFNAME_KANA,1,3) AND SUBSTR(STAFFNAME_KANA,1,3) < 'ら' THEN 'や' ";
        $query .= "                    WHEN 'ら' <= SUBSTR(STAFFNAME_KANA,1,3) AND SUBSTR(STAFFNAME_KANA,1,3) < 'わ' THEN 'ら' ";
        $query .= "                    WHEN 'わ' <= SUBSTR(STAFFNAME_KANA,1,3) AND SUBSTR(STAFFNAME_KANA,1,3) < 'ん' THEN 'わ' ";
        $query .= "                    ELSE 'その他' END ";
        $query .= "               ELSE 'その他' END AS KANA1 ";
        $query .= "     FROM SCHREG_REGD_HDAT T1 ";
        $query .= "LEFT JOIN STAFF_MST S1 ";
        $query .= "       ON S1.STAFFCD = T1.TR_CD1 ";
        $query .= "LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "       ON T2.YEAR    = T1.YEAR ";
        $query .= "      AND T2.GRADE   = T1.GRADE ";
        $query .= "WHERE  T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        if ($sort == 1) {
            $query .= "ORDER BY T1.GRADE, T1.HR_CLASS ";//クラス順
        } elseif ($sort == 2) {
            $query .= "ORDER BY STAFFNAME_KANA ";//ＨＲ担任あいうえお順
        }
        return $query;
    }
}
