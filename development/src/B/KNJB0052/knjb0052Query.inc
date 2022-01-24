<?php

require_once('for_php7.php');


class knjb0052Query extends Query
{

    //年度　学期取得
    public function getYearSemester($model)
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
    public function getChairCdQuery($model, $flg)
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
        $query .=" WHERE ";
        $query .= "      T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' ";
        $query .="   AND '".str_replace("/", "-", $model->field["EXECUTEDATE"])."' BETWEEN L1.APPDATE AND VALUE(L1.APPENDDATE, '9999-12-31') ";
        if ($flg) {
            $query .= "  AND T1.CHAIRCD IN ".$setChairCd." ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //対象データ一覧取得
    public function selectCsvQuery($model)
    {
        $setChairCd = "('";
        $comma = "";
        $select_array = explode(",", $model->selectData);
        foreach ($select_array as $chaircd) {
            $setChairCd .= $comma.$chaircd;
            $comma = "', '";
        }
        $setChairCd .= "')";

        $query  = " WITH REGD AS (  ";
        $query .= "    SELECT  ";
        $query .= "      T1.SCHREGNO,  ";
        $query .= "      T3.NAME,  ";
        $query .= "      T1.GRADE,  ";
        $query .= "      T1.HR_CLASS,  ";
        $query .= "      T1.ATTENDNO,  ";
        $query .= "      T5.HR_NAME  ";
        $query .= "    FROM SCHREG_REGD_DAT T1  ";
        $query .= "    INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO  ";
        $query .= "    INNER JOIN SCHREG_REGD_HDAT T5 ON T5.YEAR = T1.YEAR  ";
        $query .= "      AND T5.SEMESTER = T1.SEMESTER  ";
        $query .= "      AND T5.GRADE = T1.GRADE  ";
        $query .= "      AND T5.HR_CLASS = T1.HR_CLASS  ";
        $query .= "    WHERE  ";
        $query .= "      T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."'  ";
        $query .= "  )  ";
        $query .= "  SELECT  ";
        $query .= "      T1.CHAIRCD,  ";
        $query .= "      T1.CHAIRNAME,  ";
        $query .= "      T3.SCHREGNO,  ";
        $query .= "      T3.NAME,  ";
        $query .= "      T3.GRADE,  ";
        $query .= "      T3.HR_CLASS,  ";
        $query .= "      T3.ATTENDNO,  ";
        $query .= "      T3.HR_NAME  ";
        $query .= "  FROM CHAIR_DAT T1  ";
        $query .= "  LEFT JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR  ";
        $query .= "      AND T2.SEMESTER = T1.SEMESTER  ";
        $query .= "      AND T2.CHAIRCD = T1.CHAIRCD  ";
        $query .= "      AND '".str_replace("/", "-", $model->field["EXECUTEDATE"])."' BETWEEN T2.APPDATE AND T2.APPENDDATE  ";
        $query .= "  LEFT JOIN REGD T3 ON T3.SCHREGNO = T2.SCHREGNO  ";
        $query .= "  WHERE  ";
        $query .= "      T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."'  ";
        $query .= "  AND T1.CHAIRCD IN ".$setChairCd." ";
        //$query .= "  AND T3.SCHREGNO IS NOT NULL ";
        $query .= "  ORDER BY  ";
        if ($model->field["ORDER_DIV"] === '1') {
            $query .= "      T3.SCHREGNO,  ";
            $query .= "      T3.GRADE,  ";
            $query .= "      T3.HR_CLASS,  ";
            $query .= "      T3.ATTENDNO,  ";
        } else {
            $query .= "      T3.GRADE,  ";
            $query .= "      T3.HR_CLASS,  ";
            $query .= "      T3.ATTENDNO,  ";
            $query .= "      T3.SCHREGNO,  ";
        }
        $query .= "      T1.CHAIRCD  ";

        return $query;
    }
}
