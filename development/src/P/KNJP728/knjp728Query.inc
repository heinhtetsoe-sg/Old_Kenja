<?php

require_once('for_php7.php');

class knjp728Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //クラス取得
    function getAuthClass($model) {
        if ($model->field["DIV"] == "1") {
            $query  = " SELECT ";
            $query .= "     value(FRES.GRADE, '') || value(FRES.HR_CLASS, '') AS VALUE, ";
            $query .= "     '新入生' || value(FRES.GRADE, '') || '-' || value(FRES.HR_CLASS, '') AS LABEL ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT FRES ";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = FRES.ENTERYEAR ";
            $query .= "                                    AND GDAT.GRADE = FRES.GRADE ";
            $query .= " WHERE ";
            $query .= "         FRES.ENTERYEAR   = '".$model->year."' ";
            $query .= "     AND GDAT.SCHOOL_KIND = '".$model->schoolKind."' ";
            $query .= " GROUP BY";
            $query .= "     FRES.GRADE, ";
            $query .= "     FRES.HR_CLASS ";
            $query .= " ORDER BY";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT ";
            $query .= "     HDAT.GRADE || HDAT.HR_CLASS AS VALUE, ";
            $query .= "     HDAT.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT HDAT ";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = HDAT.YEAR ";
            $query .= "                                    AND GDAT.GRADE = HDAT.GRADE ";
            $query .= " WHERE ";
            $query .= "         HDAT.YEAR        = '".$model->year."' ";
            $query .= "     AND HDAT.SEMESTER    = '".CTRL_SEMESTER ."' ";
            $query .= "     AND GDAT.SCHOOL_KIND = '".$model->schoolKind."' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
                $query .= "    AND (HDAT.TR_CD1 = '" .STAFFCD ."' OR ";
                $query .= "         HDAT.TR_CD2 = '" .STAFFCD ."' OR ";
                $query .= "         HDAT.TR_CD3 = '" .STAFFCD ."') ";
            }
            $query .= " ORDER BY";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //生徒取得
    function getAuthStudent($model) {
        if ($model->field["DIV"] == "1") {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO AS VALUE, ";
            $query .= "     T1.SCHREGNO || '　' || value(T1.ATTENDNO, '***') ||  '番　' || T1.NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T1 ";
            $query .= " WHERE ";
            $query .= "         T1.ENTERYEAR = '".$model->year."' ";
            $query .= "     AND T1.GRADE || value(T1.HR_CLASS, '') = '".$model->field["GRADE_HR_CLASS"] ."' ";
            $query .= " ORDER BY ";
            $query .= "     T1.ATTENDNO ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO AS VALUE, ";
            $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO ||  '番　' || L1.NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR     = '".$model->year."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER ."' ";
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"] ."' ";
            $query .= " ORDER BY ";
            $query .= "     T1.ATTENDNO ";
        }

        return $query;
    }

    //納期限取得
    function getMonth($model) {
        $query  = " SELECT ";
        foreach ($model->month as $month) {
            $query .= "     sum(case when COLLECT_MONTH_{$month} = '1' then 1 else 0 end) as M_{$month}, ";
        }
        $query .= " '' as dummy ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONTH_GRP_DAT MONTH ";
        $query .= "     LEFT JOIN COLLECT_M_MST M_MST ON M_MST.SCHOOLCD     = MONTH.SCHOOLCD ";
        $query .= "                                  AND M_MST.SCHOOL_KIND  = MONTH.SCHOOL_KIND ";
        $query .= "                                  AND M_MST.YEAR         = MONTH.YEAR ";
        $query .= "                                  AND M_MST.COLLECT_L_CD = MONTH.COLLECT_L_CD ";
        $query .= "                                  AND M_MST.COLLECT_M_CD = MONTH.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "         MONTH.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND MONTH.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND MONTH.YEAR        = '{$model->year}' ";
        $query .= "     AND value(GAKUNOKIN_DIV, '0') = '1' ";
        $query .= " GROUP BY ";
        $query .= "     MONTH.SCHOOLCD, ";
        $query .= "     MONTH.SCHOOL_KIND, ";
        $query .= "     MONTH.YEAR ";

        return $query;
    }
}
?>
