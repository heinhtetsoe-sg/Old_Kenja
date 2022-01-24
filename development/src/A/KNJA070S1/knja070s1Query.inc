<?php

require_once('for_php7.php');

class knja070s1Query extends Query {
    //クラス情報
    function getHrInfo($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$model->getParam["YEAR"]."'  ";
        $query .= "     AND SEMESTER = '".$model->getParam["SEMESTER"]."'  ";
        $query .= "     AND GRADE    = '".$model->getParam["GRADE"]."'  ";
        $query .= "     AND HR_CLASS = '".$model->getParam["HR_CLASS"]."'  ";

        return $query;
    }

    //クラス情報
    function getTrcd()
    {
        $query  = " VALUES ";
        $query .= "     ('1', '担任１'), ";
        $query .= "     ('2', '担任２'), ";
        $query .= "     ('3', '担任３'), ";
        $query .= "     ('4', '副担任１'), ";
        $query .= "     ('5', '副担任２'), ";
        $query .= "     ('6', '副担任３') ";

        return $query;
    }

    //リスト一覧
    function getList($model, $div = "", $semeDiv = "")
    {
        $query  = " WITH TRCD_T (TR_DIV, TR_NAME) AS (";
        $query .= knja070s1Query::getTrcd();
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.STAFFNAME, ";
        $query .= "     L2.TR_NAME ";
        if ($semeDiv == "LIST") {
            $query .= "    ,L3.SEMESTERNAME ";
        }
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_HIST_DAT T1 ";
        $query .= " LEFT JOIN STAFF_MST L1 ON T1.STAFFCD = L1.STAFFCD ";
        $query .= " LEFT JOIN TRCD_T L2 ON T1.TR_DIV = L2.TR_DIV ";
        if ($semeDiv == "LIST") {
            $query .= " LEFT JOIN SEMESTER_MST L3 ON T1.YEAR = L3.YEAR AND T1.SEMESTER = L3.SEMESTER ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$model->getParam["YEAR"]."' ";
        if ($semeDiv != "LIST") {
            $query .= "     AND T1.SEMESTER = '".$model->getParam["SEMESTER"]."' ";
        }
        $query .= "     AND T1.GRADE = '".$model->getParam["GRADE"]."' ";
        $query .= "     AND T1.HR_CLASS = '".$model->getParam["HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        if ($semeDiv == "LIST") {
            $query .= "     T1.SEMESTER, ";
        }
        $query .= "     T1.TR_DIV, ";
        if ($div == "UPD") {
            $query .= "     T1.FROM_DATE DESC ";
        } else {
            $query .= "     T1.FROM_DATE ";
        }

        return $query;
    }

    //担任区分
    function getTrDiv()
    {
        $query  = " WITH TRCD_T (TR_DIV, TR_NAME) AS (";
        $query .= knja070s1Query::getTrcd();
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     TR_DIV || ':' || TR_NAME AS LABEL, ";
        $query .= "     TR_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     TRCD_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //レコード取得
    function getData($model, $div = "")
    {
        $fromDate = $div == "CHECK" ? str_replace("/", "-", $model->fields["FROM_DATE"]) : $model->getParam["FROM_DATE"];
        $trDiv = $div == "CHECK" ? $model->fields["TR_DIV"] : $model->getParam["TR_DIV"];

        $query  = " WITH TRCD_T (TR_DIV, TR_NAME) AS (";
        $query .= knja070s1Query::getTrcd();
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.STAFFNAME, ";
        $query .= "     L2.TR_NAME ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_HIST_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON T1.STAFFCD = L1.STAFFCD ";
        $query .= "     LEFT JOIN TRCD_T L2 ON T1.TR_DIV = L2.TR_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->getParam["YEAR"]."' ";
        $query .= "     AND T1.SEMESTER = '".$model->getParam["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE = '".$model->getParam["GRADE"]."' ";
        $query .= "     AND T1.HR_CLASS = '".$model->getParam["HR_CLASS"]."' ";
        $query .= "     AND T1.TR_DIV = '".$trDiv."' ";
        if ($fromDate) {
            $query .= "     AND T1.FROM_DATE = '".$fromDate."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.TR_DIV, ";
        $query .= "     T1.FROM_DATE ";

        return $query;
    }

    //職員リストコンボ用取得
    function getStaff($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     STAFFCD || ' ' || STAFFNAME_SHOW AS LABEL, ";
        $query .= "     STAFFCD AS VALUE";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->getParam["YEAR"]."'";
        $query .= " ORDER BY ";
        $query .= " STAFFCD";

        return $query;
    }

    //学期マスタ取得
    function getSemesterMst($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     V_SEMESTER_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$model->getParam["YEAR"]."' ";
        $query .= "     AND SEMESTER = '".$model->getParam["SEMESTER"]."' ";
        $query .= "     AND GRADE    = '".$model->getParam["GRADE"]."' ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = $model->fields["YEAR"];
        $data["SEMESTER"][TEXT]     = $model->fields["SEMESTER"];
        $data["GRADE"][TEXT]        = $model->fields["GRADE"];
        $data["HR_CLASS"][TEXT]     = $model->fields["HR_CLASS"];
        $data["TR_DIV"][TEXT]       = $model->fields["TR_DIV"];
        $data["FROM_DATE"][DATE]    = $model->fields["FROM_DATE"];
        $data["TO_DATE"][DATE]      = $model->fields["TO_DATE"];
        $data["STAFFCD"][TEXT]      = $model->fields["STAFFCD"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "STAFF_CLASS_HIST_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = $model->fields["YEAR"];
        $data["SEMESTER"][TEXT]     = $model->fields["SEMESTER"];
        $data["GRADE"][TEXT]        = $model->fields["GRADE"];
        $data["HR_CLASS"][TEXT]     = $model->fields["HR_CLASS"];
        $data["TR_DIV"][TEXT]       = $model->fields["TR_DIV"];
        $data["FROM_DATE"][DATE]    = $model->fields["FROM_DATE"];
        $data["TO_DATE"][DATE]      = $model->fields["TO_DATE"];
        $data["STAFFCD"][TEXT]      = $model->fields["STAFFCD"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR      = '".$model->fields["YEAR"]."'";
        $where .= "   AND SEMESTER  = '".$model->fields["SEMESTER"]."'";
        $where .= "   AND GRADE     = '".$model->fields["GRADE"]."'";
        $where .= "   AND HR_CLASS  = '".$model->fields["HR_CLASS"]."'";
        $where .= "   AND TR_DIV    = '".$model->fields["TR_DIV"]."'";
        $where .= "   AND FROM_DATE = '".str_replace("/", "-", $model->fields["FROM_DATE"])."'";

        $query = Query::updateSQL($data, "STAFF_CLASS_HIST_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateToDate($row, $toDte)
    {
        $data["TO_DATE"][DATE]      = $toDte;

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR      = '".$row["YEAR"]."'";
        $where .= "   AND SEMESTER  = '".$row["SEMESTER"]."'";
        $where .= "   AND GRADE     = '".$row["GRADE"]."'";
        $where .= "   AND HR_CLASS  = '".$row["HR_CLASS"]."'";
        $where .= "   AND TR_DIV    = '".$row["TR_DIV"]."'";
        $where .= "   AND FROM_DATE = '".str_replace("/", "-", $row["FROM_DATE"])."'";

        $query = Query::updateSQL($data, "STAFF_CLASS_HIST_DAT", $where);

        return $query;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM STAFF_CLASS_HIST_DAT ";
        $query .= " WHERE YEAR     = '".$model->fields["YEAR"]."'";
        $query .= "   AND SEMESTER = '".$model->fields["SEMESTER"]."'";
        $query .= "   AND GRADE    = '".$model->fields["GRADE"]."'";
        $query .= "   AND HR_CLASS = '".$model->fields["HR_CLASS"]."'";
        $query .= "   AND TR_DIV = '".$model->fields["TR_DIV"]."'";
        $query .= "   AND FROM_DATE = '".str_replace("/", "-", $model->fields["FROM_DATE"])."'";

        $db->query($query);
        Query::dbCheckIn($db);
        return "succeeded";
    }

    //--- UPDATE SCHREG_REGD_HDAT全て
    function &getRegdHdatUpdAll($model)
    {
        $data["TR_CD1"][TEXT]       = NULL;
        $data["TR_CD2"][TEXT]       = NULL;
        $data["TR_CD3"][TEXT]       = NULL;
        $data["SUBTR_CD1"][TEXT]    = NULL;
        $data["SUBTR_CD2"][TEXT]    = NULL;
        $data["SUBTR_CD3"][TEXT]    = NULL;

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "       YEAR      = '".$model->fields["YEAR"]."'";
        $where .= "   AND SEMESTER  = '".$model->fields["SEMESTER"]."'";
        $where .= "   AND GRADE     = '".$model->fields["GRADE"]."'";
        $where .= "   AND HR_CLASS  = '".$model->fields["HR_CLASS"]."'";

        $query = Query::updateSQL($data, "SCHREG_REGD_HDAT", $where);

        return $query;
    }

    //--- UPDATE SCHREG_REGD_HDAT
    function &getRegdHdatUpd($row)
    {
        $field = $row["TR_DIV"] < 4 ? "TR_CD".$row["TR_DIV"] : "SUBTR_CD".($row["TR_DIV"] - 3);

        $data[$field][TEXT]         = $row["STAFFCD"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR      = '".$row["YEAR"]."'";
        $where .= "   AND SEMESTER  = '".$row["SEMESTER"]."'";
        $where .= "   AND GRADE     = '".$row["GRADE"]."'";
        $where .= "   AND HR_CLASS  = '".$row["HR_CLASS"]."'";

        $query = Query::updateSQL($data, "SCHREG_REGD_HDAT", $where);

        return $query;
    }
}
?>
