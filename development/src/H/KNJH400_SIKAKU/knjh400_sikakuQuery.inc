<?php

require_once('for_php7.php');

class knjh400_sikakuQuery extends Query
{
    //生徒名取得
    public function getName($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //資格情報データを取得
    public function selectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.REGDDATE, ";
        $query .= "     L1.CONDITION_DIV, ";
        $query .= "     L2.NAME1 AS CONDITION_DIV_NAME, ";
        $query .= "     T1.QUALIFIED_CD, ";
        $query .= "     '' AS SHIKAKU_CD, ";
        $query .= "     L1.QUALIFIED_NAME, ";
        $query .= "     L1.PROMOTER, ";
        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            $query .= "     L1.MANAGEMENT_FLG, ";
        }
        $query .= "     T1.CONTENTS, ";
        $query .= "     '' AS TEST_CD, ";
        $query .= "     T1.RANK, ";
        $query .= "     L3.NAME1 AS RANK_NAME, ";
        $query .= "     T1.REMARK, ";
        $query .= "     T1.SCORE AS HOBBY_SCORE ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = L1.CONDITION_DIV ";
        $query .= "                 AND L2.NAMECD1 = 'H311' ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L3 ON  L3.NAMECD2 = T1.RANK ";
        $query .= "                 AND L3.NAMECD1 = 'H312' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     0 AS SEQ, ";
            $query .= "     T1.TEST_DATE AS REGDDATE, ";
            $query .= "     L1.CONDITION_DIV, ";
            $query .= "     L2.NAME1 AS CONDITION_DIV_NAME, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     T1.QUALIFIED_CD AS SHIKAKU_CD, ";
            $query .= "     L1.QUALIFIED_NAME, ";
            $query .= "     L1.PROMOTER, ";
            $query .= "     L1.MANAGEMENT_FLG, ";
            $query .= "     '' AS CONTENTS, ";
            $query .= "     T1.TEST_CD, ";
            $query .= "     T1.RESULT_CD AS RANK, ";
            $query .= "     L3.RESULT_NAME AS RANK_NAME, ";
            $query .= "     '' AS REMARK, ";
            $query .= "     NULL AS HOBBY_SCORE ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_TEST_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = L1.CONDITION_DIV ";
            $query .= "                 AND L2.NAMECD1 = 'H311' ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_RESULT_MST L3 ON L3.YEAR         = T1.YEAR ";
            $query .= "                            AND L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= "                            AND L3.RESULT_CD    = T1.RESULT_CD ";
            $query .= " WHERE ";
            $query .= "         T1.SCHREGNO = '{$model->schregno}' ";
            $query .= "     AND L3.CERT_FLG = 'T' ";//正式フラグが立っているもの
            $query .= " ORDER BY ";
            $query .= "     REGDDATE, ";
            $query .= "     SEQ ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            $query .= "     T1.SUBCLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .="      T1.CLASSCD, ";
                $query .="      T1.SCHOOL_KIND, ";
                $query .="      T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SEQ ";
        }

        return $query;
    }
}
