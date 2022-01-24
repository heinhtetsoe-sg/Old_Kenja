<?php

require_once('for_php7.php');

class knjx_reason_collection_selectQuery extends Query
{

    //出欠備考
    public function getReasonCollection($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T1.COLLECTION_CD, ";
        $query .= "     T1.COLLECTION_NAME AS COLLECTION_NAME_SHOW, ";
        $query .= "     T2.ATTEND_REMARK AS ATTEND_REMARK_SHOW ";
        $query .= " FROM ";
        $query .= "     ATTEND_REASON_COLLECTION_MST T1 ";
        $query .= "     INNER JOIN ATTEND_REASON_COLLECTION_DAT T2 ";
        $query .= "             ON T2.YEAR          = T1.YEAR ";
        $query .= "            AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "            AND T2.COLLECTION_CD = T1.COLLECTION_CD ";
        $query .= "            AND T2.SCHREGNO      = '".$model->schregno."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "    AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     COLLECTION_CD ";

        return $query;
    }
}
