<?php

require_once('for_php7.php');

class knjd645Query extends Query {

    //学部学科取得
    function getBuKa($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.BU_CD || T1.KA_CD AS VALUE, ";
        $query .= "     T1.BU_CD || T1.KA_CD || '　' || T3.BU_NAME || '　' || T3.KA_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_RECOMMENDATION_WISH_DAT T1 ";
        $query .= " LEFT OUTER JOIN NAME_MST T2 ";
        $query .= "     ON T1.SCHOOL_CD = T2.ABBV3 AND T2.NAMECD1 = 'Z010' AND T2.NAMECD2 = '00' ";
        $query .= " LEFT OUTER JOIN COLLEGE_RECOMMENDATION_DAT T3 ";
        $query .= "     ON T1.YEAR = T3.YEAR AND T1.SCHOOL_CD = T3.SCHOOL_CD AND ";
        $query .= "        T1.BU_CD = T3.BU_CD AND T1.KA_CD = T3.KA_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.RECOMMENDATION_FLG = '1' ";

        return $query;
    }

    //学部学科名取得（ＣＳＶ）
    function getBuKa_Name($bukacd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.BU_CD, ";
        $query .= "     T1.KA_CD, ";
        $query .= "     T1.BU_NAME || T1.KA_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_RECOMMENDATION_DAT T1 ";
        $query .= " LEFT OUTER JOIN NAME_MST T2 ";
        $query .= "     ON T1.SCHOOL_CD = T2.ABBV3 AND T2.NAMECD1 = 'Z010' AND T2.NAMECD2 = '00' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.BU_CD || T1.KA_CD = '".$bukacd."' ";

        return $query;
    }

    //推薦名簿取得（ＣＳＶ）
    function getList($model, $bukacd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.RANK1, ";
        $query .= "     T3.SCORE1, ";
        $query .= "     T3.SCORE3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_RECOMMENDATION_WISH_DAT T1 ";
        $query .= " LEFT OUTER JOIN SCHREG_BASE_MST T2 ";
        $query .= "     ON T1.SCHREGNO=T2.SCHREGNO ";
        $query .= " LEFT OUTER JOIN SCHREG_REGD_DAT T5 ";
        $query .= "     ON T1.YEAR=T5.YEAR AND T1.SCHREGNO=T5.SCHREGNO ";
        $query .= " LEFT OUTER JOIN COLLEGE_RECOMMENDATION_DAT T6 ";
        $query .= "     ON T1.YEAR=T6.YEAR AND T1.SCHOOL_CD=T6.SCHOOL_CD AND ";
        $query .= "        T1.BU_CD=T6.BU_CD AND T1.KA_CD=T6.KA_CD ";
        $query .= " LEFT OUTER JOIN RECORD_MOCK_RANK_DAT T3 ";
        $query .= "                               ON T1.YEAR = T3.YEAR ";
        $query .= "                               AND T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "                               AND T3.DATA_DIV = '1' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND T3.CLASSCD = '33' ";
            $query .= "                             AND T3.SCHOOL_KIND = 'H' ";
            $query .= "                             AND T3.CURRICULUM_CD = '2' ";
        }
        $query .= "                               AND T3.SUBCLASSCD = '333333' ";
        $query .= "                               AND T3.GRADE  = T5.GRADE  ";
        $query .= "                               AND T3.COURSE_DIV = T6.DIV ";
        $query .= " INNER JOIN NAME_MST T4 ";
        $query .= "     ON T1.SCHOOL_CD=T4.ABBV3 AND T4.NAMECD1='Z010' AND T4.NAMECD2='00' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.BU_CD || T1.KA_CD = '".$bukacd."' AND ";
        $query .= "     T1.RECOMMENDATION_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T3.RANK1,T1.SCHREGNO ";

        return $query;
    }

}
?>
