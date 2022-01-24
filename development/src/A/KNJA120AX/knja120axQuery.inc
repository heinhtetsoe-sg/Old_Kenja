<?php

require_once('for_php7.php');

class knja120axQuery extends Query {

    //年度学期取得
    function getYearSeme()
    {
        $query  = " SELECT ";
        $query .= "     YEAR || '年度' || SEMESTERNAME AS LABEL, ";
        $query .= "     YEAR || '-' || SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //年組取得
    function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || '-' || SEMESTER = '".$model->field["YEAR_SEME"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getInputVal($model)
    {
        $query  = " WITH MAIN_T(LABEL, VALUE) AS ( ";

        $setUnion = "";
        foreach ($model->inputValArray as $key => $val) {
            $query .= $setUnion;
            $query .= " VALUES('".$val["LABEL"]."', '".$key."') ";
            $setUnion = " UNION ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getStudentsVal($model, $tableNm, $fieldNm)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME, ";
        $query .= "     ".$fieldNm." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN ".$tableNm." L2 ON T1.YEAR = L2.YEAR ";
        $query .= "                               AND T1.SCHREGNO = L2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEME"]."' ";
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //DATレコード有無
    function getDatCount($schregNo, $year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."'";
        $query .= "     AND SCHREGNO = '".$schregNo."'";

        return $query;
    }

    //INSERT
    function &getInsertQuery($db, $model, $schregNo, $setVal, $tableNm, $fieldNm)
    {
        $yearSemArray = preg_split("/-/", $model->field["YEAR_SEME"]);
        $setYear = $yearSemArray[0];
        $setSem = $yearSemArray[1];

        $data[$fieldNm][TEXT]     = $setVal;
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";
        //年次の取得
        $query  = "SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregNo."'";
        $query .= "     AND YEAR || '-' || SEMESTER = '".$model->field["YEAR_SEME"]."' ";
        $annual = $db->getOne($query);

        $dataCnt = $db->getOne(knja120axQuery::getDatCount($schregNo, $setYear));

        if ($dataCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '".$setYear."'";
            $where .= "     AND SCHREGNO = '".$schregNo."'";
            $query = Query::updateSQL($data, $tableNm, $where);
        } else {
            $data["YEAR"][TEXT]                 = $setYear;
            $data["SCHREGNO"][TEXT]             = $schregNo;
            $data["ANNUAL"][TEXT]               = $annual;
            $query = Query::insertSQL($data, $tableNm);
        }

        return $query;
    }
}
?>
