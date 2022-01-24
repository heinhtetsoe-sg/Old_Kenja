<?php

require_once('for_php7.php');

class knjp374Query extends Query {

    //学年
    function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GDAT.SCHOOL_KIND, ";
        $query .= "     GDAT.GRADE AS VALUE, ";
        $query .= "     GDAT.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT GDAT ";
        $query .= " WHERE ";
        $query .= "     GDAT.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "   AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "   AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組
    function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     HDAT.GRADE || '-' || HDAT.HR_CLASS AS VALUE, ";
        $query .= "     HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= " WHERE ";
        $query .= "     HDAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND HDAT.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND HDAT.GRADE = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //振込区分
    function getTransferDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     TRANS_DIV.TRANSFER_DIV AS VALUE, ";
        $query .= "     TRANS_DIV.TRANSFER_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MONEY_TRANSFER_DIV_MST TRANS_DIV ";
        $query .= " WHERE ";
        $query .= "     TRANS_DIV.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND TRANS_DIV.SCHOOL_KIND = '{$model->selectSchoolKind[$model->grade]}' ";
        $query .= "     AND TRANS_DIV.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //対象生徒
    function getStd($model)
    {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     HDAT.HR_NAME || '-' || REGD.ATTENDNO AS GHRA, ";
        $query .= "     BASE.NAME, ";
        $query .= "     TRANS_STD.TRANSFER_MONEY ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR = HDAT.YEAR ";
        $query .= "          AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "          AND REGD.GRADE = HDAT.GRADE ";
        $query .= "          AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= "     LEFT JOIN MONEY_TRANSFER_STD_DAT TRANS_STD ON REGD.YEAR = TRANS_STD.YEAR ";
        $query .= "          AND REGD.SCHREGNO = TRANS_STD.SCHREGNO ";
        $query .= "          AND TRANS_STD.TRANSFER_DIV = '{$model->transferDiv}' ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS = '{$model->hrClass}' ";
        $query .= " ORDER BY ";
        $query .= "     GHRA ";
        return $query;
    }

    function getInsertQuery($db, $model, $schregNo, $setMoney) {
        $query  = " DELETE FROM MONEY_TRANSFER_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";
        $query .= "     AND TRANSFER_DIV = '{$model->transferDiv}' ";
        $db->query($query);

        if (strlen($setMoney) > 0) {
            $data["YEAR"][TEXT]  = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]          = $schregNo;
            $data["TRANSFER_DIV"][TEXT]      = $model->transferDiv;
            $data["TRANSFER_MONEY"][NUMBER]  = $setMoney;
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][NUMBER]         = "SYSDATE()";

            $query = Query::insertSQL($data, "MONEY_TRANSFER_STD_DAT");
            $db->query($query);
        }

        return $query;
    }
}
?>
