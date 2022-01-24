<?php

require_once('for_php7.php');

class knja611Query extends Query
{
    //中学一年
    public function getGrade($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='A023' ";
        $query .= "     AND NAME1='{$model->schoolKindJ}' ";

        return $query;
    }

    //年組リスト
    public function getHrClassAuth($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || ':' || T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
        $query .= " AND T2.SCHOOL_KIND = '{$model->schoolKindJ}' ";
        $query .= "WHERE T1.YEAR = '{$model->year}'";
        $query .= "AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "AND T1.GRADE = '{$model->grade}' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (TR_CD1 = '" . STAFFCD . "' ";
            $query .= " OR TR_CD2 = '" . STAFFCD . "' ";
            $query .= " OR TR_CD3 = '" . STAFFCD . "') ";
        }

        return $query;
    }

    //登録データ
    public function selectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.HR_NAME || '-' || T1.ATTENDNO || '番' AS ATTEND, ";
        $query .= "     T3.NAME_SHOW AS NAME, ";
        $query .= "     T4.IQ AS IQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.GRADE = T2.GRADE ";
        $query .= "     AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST T3 ";
        $query .= "     ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_IQ_DAT T4 ";
        $query .= "     ON T1.YEAR = T4.YEAR ";
        $query .= "     AND T1.SCHREGNO = T4.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        if ($model->hrClass != '') {
            $query .= "     AND T1.HR_CLASS= '{$model->hrClass}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.HR_CLASS,T1.ATTENDNO ";

        return $query;
    }
    //IQデータ削除
    public function deleteSchregIqDat($model, $schregno)
    {
        $query .= " DELETE ";
        $query .= "     SCHREG_IQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHREGNO='{$schregno}' ";

        return $query;
    }
    //IQデータ登録
    public function insertSchregIqDat($db, $model, $row)
    {
        $data = array();
        $data["YEAR"][TEXT]         = $model->year;
        $data["SCHREGNO"][TEXT]     = $row['SCHREGNO'];
        $data["IQ"][TEXT]           = $row['IQ'];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $db->query(Query::insertSQL($data, "SCHREG_IQ_DAT"));
    }
}
