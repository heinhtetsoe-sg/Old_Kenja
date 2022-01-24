<?php

require_once('for_php7.php');

class knjz067bQuery extends Query
{

    //年度一覧取得
    public function getIBYear()
    {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS (VALUES('".CTRL_YEAR."', '".CTRL_YEAR."')) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     IBYEAR AS LABEL, ";
        $query .= "     IBYEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_REPLACE_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //IBコース一覧取得
    public function getIBPrgCourse($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ibyear."' ";
        $query .= " AND NAMECD1 = 'A034' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }
    
    //出力対象のIBコース一覧取得
    public function getIBClassReplace($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     L0.NAMECD2, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     L1.IBCLASSNAME_ENG  ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_REPLACE_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L0 ON L0.NAMECD1 = 'A034' ";
        $query .= "                          AND L0.NAME1 = T1.IBPRG_COURSE ";
        $query .= "     LEFT JOIN IBCLASS_MST L1 ON L1.IBCLASSCD = T1.IBCLASSCD   ";
        $query .= "                             AND L1.IBPRG_COURSE = T1.IBPRG_COURSE  ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '".$model->ibyear."' ";
        $query .= " ORDER BY ";
        $query .= "     L0.NAMECD2, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE ";
        return $query;
    }
    
    //対象学年のIB教科数チェック
    public function getIBClassDataCount($model, $ibClassRow, $setGrade)
    {
        $query  = " SELECT  ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_REPLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR = '".$model->ibyear."' ";
        if ($setGrade != "05") {
            $query .= " AND IBGRADE = '".$setGrade."' ";
        } else {
            $query .= " AND IBGRADE IN ('05', '06') ";
        }
        $query .= " AND IBCLASSCD = '".$ibClassRow["IBCLASSCD"]."' ";
        $query .= " AND IBPRG_COURSE = '".$ibClassRow["IBPRG_COURSE"]."' ";
        return $query;
    }
    
    //CSVデータ出力
    public function getCsvData($model, $ibClassRow, $setGrade)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD,  ";
        $query .= "     T1.SCHOOL_KIND,  ";
        $query .= "     L1.CLASSNAME ";
        $query .= " FROM  ";
        $query .= "     IBSUBCLASS_REPLACE_DAT T1   ";
        $query .= " LEFT JOIN CLASS_MST L1 ON L1.CLASSCD = T1.CLASSCD   ";
        $query .= "                       AND L1.SCHOOL_KIND = T1.SCHOOL_KIND  ";
        $query .= " WHERE  ";
        $query .= "     T1.IBYEAR = '".$model->ibyear."' ";
        if ($setGrade != "05") {
            $query .= " AND T1.IBGRADE = '".$setGrade."' ";
        } else {
            $query .= " AND T1.IBGRADE IN ('05', '06') ";
        }
        $query .= " AND T1.IBCLASSCD = '".$ibClassRow["IBCLASSCD"]."' ";
        $query .= " AND T1.IBPRG_COURSE = '".$ibClassRow["IBPRG_COURSE"]."' ";
        $query .= " ORDER BY  ";
        $query .= "     T1.SCHOOL_KIND,  ";
        $query .= "     T1.CLASSCD ";
        return $query;
    }
}
