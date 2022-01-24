<?php

require_once('for_php7.php');


class knjb3052Query extends Query {

    //年度　学期取得
    function getYearSemester($model)
    {
        $startYear = CTRL_YEAR - 2;
        $endYear   = CTRL_YEAR + 2;
    
        $query  = " SELECT ";
        if (!$model) {
            $query .= "     YEAR || '-' || SEMESTER AS VALUE, ";
            $query .= "     YEAR || '年度　' || SEMESTERNAME AS LABEL ";
        } else {
            $query .= "     SDATE, ";
            $query .= "     EDATE ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR >  '".$startYear."' ";
        $query .= " AND YEAR < '".$endYear."' ";
        $query .= " AND SEMESTER <> '9' ";
        if ($model) {
            $query .= " AND YEAR || '-' || SEMESTER = '".$model->field["YEAR_SEMESTER"]."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //講座データ一覧取得
    function getChairCdQuery($model, $flg)
    {
        if ($flg) {
            $setChairCd = "('";
            $comma = "";
            $select_array = explode(",", $model->selectData);
            foreach ($select_array as $chaircd) {
                $setChairCd .= $comma.$chaircd;
                $comma = "', '";
            }
            $setChairCd .= "')";
        }
        
        $query  =" SELECT DISTINCT ";
        $query .="     T1.CHAIRCD AS VALUE, ";
        $query .="     T1.CHAIRNAME AS LABEL ";
        $query .=" FROM ";
        $query .="     CHAIR_DAT T1 ";
        $query .="     INNER JOIN CHAIR_STD_DAT L1 ON L1.YEAR     = T1.YEAR ";
        $query .="                                AND L1.SEMESTER = T1.SEMESTER ";
        $query .="                                AND L1.CHAIRCD  = T1.CHAIRCD ";
        if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_REFERABLE) {
            $query .="     INNER JOIN CHAIR_STF_DAT CHAIR_STF ON CHAIR_STF.YEAR     = T1.YEAR ";
            $query .="                                AND CHAIR_STF.SEMESTER = T1.SEMESTER ";
            $query .="                                AND CHAIR_STF.CHAIRCD  = T1.CHAIRCD ";
            $query .="                                AND CHAIR_STF.STAFFCD  = '".STAFFCD."' ";
        }
        $query .=" WHERE ";
        $query .= "      T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' ";
        $query .="   AND '".str_replace("/", "-", $model->field["EXECUTEDATE"])."' BETWEEN L1.APPDATE AND VALUE(L1.APPENDDATE, '".str_replace("/", "-", $model->field["EDATE"])."') ";
        if ($flg) {
            $query .= "  AND T1.CHAIRCD IN ".$setChairCd." ";
        }
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //講座受講データ
    function getChairStd($model, $chairCd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIR_STD.SCHREGNO, ";
        $query .= "     CHAIR_STD.APPDATE, ";
        if ($model->field["ORDER_DIV"] == "1") {
            $query .= "     ROW_NUMBER() OVER(ORDER BY REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO) AS ROWNUMBER ";
        } else if ($model->field["ORDER_DIV"] == "2") {
            $query .= "     ROW_NUMBER() OVER(ORDER BY BASE.SEX, REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO) AS ROWNUMBER ";
        } else {
            $query .= "     ROW_NUMBER() OVER(ORDER BY CHAIR_STD.SCHREGNO) AS ROWNUMBER ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT CHAIR_STD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON CHAIR_STD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON CHAIR_STD.YEAR = REGD.YEAR ";
        $query .= "           AND CHAIR_STD.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND CHAIR_STD.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     CHAIR_STD.YEAR || '-' || CHAIR_STD.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' ";
        $query .= "     AND CHAIR_STD.CHAIRCD = '{$chairCd}' ";
        $query .="      AND '".str_replace("/", "-", $model->field["EXECUTEDATE"])."' BETWEEN CHAIR_STD.APPDATE AND VALUE(CHAIR_STD.APPENDDATE, '".str_replace("/", "-", $model->field["EDATE"])."') ";
        $query .=" ORDER BY ";
        $query .="     ROWNUMBER ";

        return $query;
    }

    //UPDATE
    function getUpdSql($model, $chairCd, $row) {
        $data["ROW"][TEXT]          = sprintf("%03d", $row["ROWNUMBER"]);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR || '-' || SEMESTER = '".$model->field["YEAR_SEMESTER"]."' ";
        $where .= "     AND CHAIRCD = '{$chairCd}' ";
        $where .= "     AND SCHREGNO = '{$row["SCHREGNO"]}' ";
        $where .= "     AND APPDATE = '{$row["APPDATE"]}' ";

        $query = Query::updateSQL($data, "CHAIR_STD_DAT", $where);
        return $query;
    }
}
?>
