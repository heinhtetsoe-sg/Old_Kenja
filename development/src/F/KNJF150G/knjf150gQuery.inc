<?php

require_once('for_php7.php');

class knjf150gQuery extends Query
{
    //年組取得
    public function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$model->year}' AND ";
        $query .= "     SEMESTER = '{$model->semester}' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= "     AND GRADE ='{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //保健室来室記録データよりデータを取得
    public function selectQuery($model)
    {
        $query  = " WITH TYPE1 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F200' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F200' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F200' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F208' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F208' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F208' ";
        $query .= " WHERE ";
        $query .= "     T1.TYPE = '1' ";
        $query .= " ), TYPE2 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F201' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F201' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F201' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F209' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F209' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F209' ";
        $query .= " WHERE ";
        $query .= "     T1.TYPE = '2' ";
        $query .= " ), TYPE3 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F203' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F203' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F203' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F210' ";
        $query .= " WHERE ";
        $query .= "     T1.TYPE = '3' ";
        $query .= " ), TYPE4 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F202' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F202' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F202' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F210' ";
        $query .= " WHERE ";
        $query .= "     T1.TYPE = '4' ";
        $query .= " ), TYPE5 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F219' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F219' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F219' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F220' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F220' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F220' ";
        $query .= " WHERE ";
        $query .= "     T1.TYPE = '5' ";
        $query .= " ), MAIN AS( ";
        $query .= " SELECT * FROM TYPE1 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE2 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE3 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE4 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE5 ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T4.HR_NAME,'') || VALUE(T2.ATTENDNO, '') || '番' AS HR_ATTEND, ";
        $query .= "     T3.NAME_SHOW AS NAME, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T4.HR_NAME, ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.TYPE, ";
        $query .= "     CASE T1.TYPE WHEN '1' THEN '内科' WHEN '2' THEN '外科' WHEN '3' THEN 'その他' WHEN '4' THEN '".$model->sch_label."以外' ELSE '健康相談' END TYPE_NAME, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.GRADE ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "     ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T4 ";
        $query .= "     ON T2.YEAR = T4.YEAR ";
        $query .= "     AND T2.SEMESTER = T4.SEMESTER ";
        $query .= "     AND T2.GRADE = T4.GRADE ";
        $query .= "     AND T2.HR_CLASS = T4.HR_CLASS ";
        if ($model->grade != 'all' || $model->hrClass != 'all' || $model->startDate != '' || $model->endDate != '') {
            $query .= " WHERE ";
        }
        $flag =false;
        if ($model->grade != 'all') {
            $query .= "     T2.GRADE='{$model->grade}' ";
            $flag = true;
        }
        if ($model->hrClass != 'all') {
            if ($flag) {
                $query .= " AND ";
            }
            $query .= "     T2.HR_CLASS='{$model->hrClass}' ";
            $flag = true;
        }
        if ($model->startDate != '') {
            if ($flag) {
                $query .= " AND ";
            }
            $query .= "     T1.VISIT_DATE >='".str_replace('/', '-', $model->startDate)."' ";
            $flag = true;
        };
        if ($model->endDate != '') {
            if ($flag) {
                $query .= " AND ";
            }
            $query .= "     T1.VISIT_DATE <='".str_replace('/', '-', $model->endDate)."' ";
            $flag = true;
        }
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.VISIT_DATE ";

        return $query;
    }
}
