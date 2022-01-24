<?php

require_once('for_php7.php');

class knjp100kQuery extends Query {

    //１レコード取得
    function getRow(&$db, $model, $flg)
    {
        $query  = "SELECT ";
        $query .= "     T2.schregno, ";
        $query .= "     T1.applicationcd, ";
        $query .= "     T1.applicationname, ";
        $query .= "     T1.applicationmoney, ";
        $query .= "     T2.applied_date, ";
        $query .= "     T2.appli_money_due, ";
        $query .= "     T2.appli_paid_div, ";
        $query .= "     T2.appli_paid_money, ";
        $query .= "     T2.appli_paid_date ";
        $query .= " FROM ";
        $query .= "     application_mst T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     application_dat T2 ";
        $query .= " ON ";
        $query .= "     T1.year = T2.year AND ";
        $query .= "     T1.applicationcd = T2.applicationcd AND ";
        $query .= "     T2.schregno = '".$model->schregno."' ";        
        $query .= " WHERE ";
        $query .= "     T1.year = '".$model->year."' AND ";
        $query .= "     T1.applicationcd = '".$model->appli_cd."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    function getRow2($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * ";
        $query .= "  FROM application_dat ";
        $query .= " WHERE year = '".$model->year."' ";
        $query .= "   AND schregno = '".$model->schregno."'  ";
        $query .= "   AND applicationcd = '".$model->field["APPLICATIONCD"]."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //生徒の名前の取得
    function getStudentName($schregno)
    {
        return "SELECT schregno,name_show,name_kana FROM schreg_base_mst WHERE schregno ='".$schregno."'";
    }

    //申込名称の取得
    function getAppliName($appli_cd)
    {
        $query  = "SELECT applicationname ";
        $query .= "  FROM application_mst ";
        $query .= " WHERE year ='".CTRL_YEAR."' AND ";
        $query .= "       applicationcd = '".$appli_cd."'";
        return $query;
    }

    //申込コードの取得
    function getApplicd($year)
    {
        $query  = " SELECT ";
        $query .= "     APPLICATIONCD, ";
        $query .= "     APPLICATIONNAME, ";
        $query .= "     APPLICATIONMONEY ";
        $query .= " FROM ";
        $query .= "     APPLICATION_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."'";

        return $query;
    }

    //名称
    function nameGet()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'G205' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //リストの取得
    function getList($model)
    {
        $query  = "SELECT ";
        $query .= "     T2.schregno, ";
        $query .= "     T1.applicationcd, ";
        $query .= "     T1.applicationname, ";
        $query .= "     T2.applied_date, ";
        $query .= "     T2.appli_money_due, ";
        $query .= "     MEISYOU_GET(T2.appli_paid_div, 'G205', 1) AS appli_paid_div, ";
        $query .= "     T2.appli_paid_money, ";
        $query .= "     T2.appli_paid_date ";
        $query .= " FROM ";
        $query .= "     application_mst T1, ";
        $query .= "     application_dat T2 ";
        $query .= " WHERE ";
        $query .= "     T1.year = T2.year AND ";
        $query .= "     T1.applicationcd = T2.applicationcd AND ";
        $query .= "     T1.year = '".$model->year."' AND ";
        $query .= "     T2.schregno = '".$model->schregno."'";
        $query .= " ORDER BY ";
        $query .= "     T1.applicationcd ";

        return $query;
    }

    //学年毎のクラスの取得
    function GetHrclass()
    {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //学年取得
    function GetGrade()
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE || '学年' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " GROUP BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //一括更新画面の生徒一覧
    function GetStudent($model)
    {
        $arr = explode("-", $model->hrclass);
        $query  = "SELECT ";
        if ($model->selectDiv == "1") {
            $query .= "       T2.SCHREGNO AS VALUE, ";
            $query .= "       T3.HR_NAME || '  ' || T2.ATTENDNO || '  ' || T2.SCHREGNO || '  ' || T1.NAME_SHOW AS LABEL ";
        } else {
            $query .= "       T2.GRADE || '-' || T2.HR_CLASS AS VALUE, ";
            $query .= "       T3.HR_NAME AS LABEL ";
        }
        $query .= "  FROM SCHREG_BASE_MST T1 RIGHT OUTER JOIN ";
        $query .= "       SCHREG_REGD_DAT T2 ";
        $query .= "    ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "       INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "               ON T2.YEAR     = T3.YEAR ";
        $query .= "              AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "              AND T2.GRADE    = T3.GRADE ";
        $query .= "              AND T2.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE T2.YEAR     = '".CTRL_YEAR."'";
        $query .= "   AND T2.SEMESTER = '".CTRL_SEMESTER."'";
        $query .= "   AND T2.GRADE    = '".$arr[0]."'";
        If ($model->selectDiv == "1") {
            $query .= "   AND T2.HR_CLASS = '".$arr[1]."'";
            $query .= " ORDER BY ";
            $query .= "     ATTENDNO ";
        } else {
            $query .= " GROUP BY ";
            $query .= "     T2.GRADE, ";
            $query .= "     T2.HR_CLASS, ";
            $query .= "     T3.HR_NAME ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //一括更新生徒
    function getExeData($model)
    {
        $data = explode(",", $model->selectdata);
        $instate = "(";
        $sep = "";
        foreach ($data as $gradeHr) {
            $instate .= $sep."'".$gradeHr."'";
            $sep = ",";
        }
        $instate .= ")";
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."'";
        $query .= "     AND GRADE || '-' || HR_CLASS IN ".$instate." ";

        return $query;
    }

    //重複チェック
    function CheckExistData($appli_cd, $schregno)
    {
        $query  = "SELECT * FROM application_dat ";
        $query .= " WHERE year          = '".CTRL_YEAR."'";
        $query .= "   AND applicationcd = '".$appli_cd."'";
        $query .= "   AND schregno      = '".$schregno."'";
        return $query; 
    }

    //追加
    function &getInsertQuery($field, $schregno)
    {
        $data = array();
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["APPLICATIONCD"][TEXT]        = $field["APPLICATIONCD"];
        $data["APPLI_MONEY_DUE"][NUMBER]    = $field["APPLI_MONEY_DUE"];
        $data["APPLIED_DATE"][TEXT]         = str_replace("/", "-", $field["APPLIED_DATE"]);
        $data["APPLI_PAID_MONEY"][NUMBER]   = $field["APPLI_PAID_MONEY"];
        if ($field["APPLI_MONEY_DUE"] == $field["APPLI_PAID_MONEY"]) {
            $data["APPLI_PAID_FLG"][TEXT]       = "1";
        }
        $data["APPLI_PAID_DIV"][TEXT]       = $field["APPLI_PAID_DIV"];
        $data["APPLI_PAID_DATE"][TEXT]      = str_replace("/", "-", $field["APPLI_PAID_DATE"]);
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "application_dat");
        return $query;
    }

    //更新
    function &getUpdateQuery($field, $schregno)
    {
        $data = array();
        $data["APPLI_MONEY_DUE"][NUMBER]    = $field["APPLI_MONEY_DUE"];
        $data["APPLIED_DATE"][TEXT]         = str_replace("/", "-", $field["APPLIED_DATE"]);
        $data["APPLI_PAID_MONEY"][NUMBER]   = $field["APPLI_PAID_MONEY"];
        if ($field["APPLI_MONEY_DUE"] == $field["APPLI_PAID_MONEY"]) {
            $data["APPLI_PAID_FLG"][TEXT] = "1";
        } else {
            $data["APPLI_PAID_FLG"][TEXT] = "";
        }
        $data["APPLI_PAID_DIV"][TEXT]       = $field["APPLI_PAID_DIV"];
        $data["APPLI_PAID_DATE"][TEXT]      = str_replace("/", "-", $field["APPLI_PAID_DATE"]);
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE year           = '".CTRL_YEAR."'";
        $where .= "   AND schregno       = '".$schregno."'";
        $where .= "   AND applicationcd  = '".$field["APPLICATIONCD"]."'";

        $query = Query::updateSQL($data, "application_dat", $where);
        return $query;
    }

    //削除
    function &getDeleteQuery($field, $schregno)
    {
        $query  = " DELETE FROM application_dat ";
        $query .= "  WHERE year = '".CTRL_YEAR."' AND ";
        $query .= "        schregno = '".$schregno."' AND ";
        $query .= "        applicationcd = '".$field["APPLICATIONCD"]."'";
        return $query;
    }
}
?>
