<?php

require_once('for_php7.php');


class knjp300Query extends Query {

    //年度取得
    function getyear()
    {
        $query  = " SELECT YEAR ";
        $query .= " FROM APPLICATION_DAT ";
        $query .= " GROUP BY YEAR ";

        return $query;
    }
    
    //年度取得
    function getApplication($year)
    {
        $query  = " SELECT APPLICATIONCD,APPLICATIONNAME ";
        $query .= " FROM APPLICATION_MST ";
        $query .= " WHERE YEAR = '".$year."' ";
        $query .= " GROUP BY APPLICATIONCD,APPLICATIONNAME ";

        return $query;
    }

    //クラスデータ取得
    function getclass($model)
    {
        $query  = " WITH ATABLE AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     APPLICATION_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND APPLICATIONCD = '{$model->field["APPLICATION"]}' ";
        if ($model->field["TAISYOSYA"] == '2') { //対象者が『未納者』ならば条件が増える
            $query .= " AND VALUE(APPLI_PAID_FLG,'0') = '0' ";
        }
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     t2.GRADE||t2.HR_CLASS VALUE,t3.HR_NAME LABEL ";
        $query .= " FROM ";
        $query .= "     ATABLE t1 ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                    SCHREGNO,GRADE,HR_CLASS ";
        $query .= "                FROM ";
        $query .= "                    SCHREG_REGD_DAT ";
        $query .= "                WHERE ";
        $query .= "                    YEAR = '".$model->field["YEAR"]."' ";
        $query .= "                GROUP BY ";
        $query .= "                    SCHREGNO,GRADE,HR_CLASS ";
        $query .= "                    )t2 ON t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                    GRADE,HR_CLASS,HR_NAME ";
        $query .= "                FROM ";
        $query .= "                    SCHREG_REGD_HDAT ";
        $query .= "                WHERE ";
        $query .= "                    YEAR = '".$model->field["YEAR"]."' ";
        $query .= "                GROUP BY ";
        $query .= "                    GRADE,HR_CLASS,HR_NAME ";
        $query .= "                    )t3 ON t2.GRADE||t2.HR_CLASS = t3.GRADE||t3.HR_CLASS ";
        $query .= " GROUP BY ";
        $query .= "     t2.GRADE,t2.HR_CLASS,t3.HR_NAME ";
        $query .= " ORDER BY ";
        $query .= "     t2.GRADE,t2.HR_CLASS,t3.HR_NAME ";

        return $query;
    }

    //生徒データ取得
    function getsch($model)
    {
        $query  = " WITH ATABLE AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     APPLICATION_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND APPLICATIONCD = '{$model->field["APPLICATION"]}' ";
        if ($model->field["TAISYOSYA"] == '2') { //対象者が『未納者』ならば条件が増える
            $query .= " AND VALUE(APPLI_PAID_FLG,'0') = '0' ";
        }
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     t2.SCHREGNO,t2.ATTENDNO,t2.MAJORCD,t2.GRADE,t2.HR_CLASS,t3.NAME_SHOW,t4.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t2 LEFT JOIN SCHREG_BASE_MST t3 ON t2.SCHREGNO = t3.SCHREGNO, ";
        $query .= "     SCHREG_REGD_HDAT t4 ";
        $query .= " WHERE ";
        $query .= "     t2.YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     t2.GRADE || t2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 ATABLE t1 ";
        $query .= "             WHERE ";
        $query .= "                 t2.SCHREGNO = t1.SCHREGNO) AND ";
        $query .= "     t4.YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     t4.GRADE || t4.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " GROUP BY ";
        $query .= "     t2.SCHREGNO,t2.ATTENDNO,t2.MAJORCD,t2.GRADE,t2.HR_CLASS,t3.NAME_SHOW,t4.HR_NAME ";
        $query .= " ORDER BY ";
        $query .= "     t2.ATTENDNO ";

        return $query;
    }

}
?>
