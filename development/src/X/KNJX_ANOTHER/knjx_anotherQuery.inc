<?php

require_once('for_php7.php');

class knjx_anotherQuery extends Query
{

    //前籍校履データリスト取得
    public function getList($cd, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.FINSCHOOL_NAME, ";
        $query .= "     (SELECT  ";
        $query .= "          NAME1 ";
        $query .= "      FROM ";
        $query .= "          V_NAME_MST ";
        $query .= "      WHERE ";
        $query .= "          YEAR = '".$year."' AND ";
        $query .= "          NAMECD1 = 'E026' AND ";
        $query .= "          NAMECD2 = T1.STUDENT_DIV ";
        $query .= "     ) AS STUDENT_DIV_NAME, ";
        $query .= "     (SELECT  ";
        $query .= "          NAME1 ";
        $query .= "      FROM ";
        $query .= "          V_NAME_MST ";
        $query .= "      WHERE ";
        $query .= "          YEAR = '".$year."' AND ";
        $query .= "          NAMECD1 = 'E027' AND ";
        $query .= "          NAMECD2 = T1.ENT_FORM ";
        $query .= "     ) AS ENT_FORM_NAME ";
        $query .= " FROM ";
        $query .= "     ANOTHER_SCHOOL_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     FINSCHOOL_MST T2 ";
        $query .= "     ON T1.FORMER_REG_SCHOOLCD = T2.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$cd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.REGD_S_DATE DESC, ";
        $query .= "     T1.REGD_E_DATE DESC, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //前籍校履データ取得
    public function getAnotherSchoolData($cd1, $cd2)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.FORMER_REG_SCHOOLCD AS FINSCHOOLCD, ";
        $query .= "     T2.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     ANOTHER_SCHOOL_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     FINSCHOOL_MST T2 ";
        $query .= "     ON T1.FORMER_REG_SCHOOLCD = T2.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$cd1."' ";
        $query .= "    AND T1.SEQ = ".intval($cd2)." ";

        return $query;
    }

    //前籍校履データチェック
    public function getAnotherSchoolCheck($cd1, $REGD_S_DATE, $REGD_E_DATE, $SEQ = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(SEQ) ";
        $query .= " FROM ";
        $query .= "     ANOTHER_SCHOOL_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "        SCHREGNO = '".$cd1."' ";
        $query .= $SEQ ? " AND SEQ <> ".intval($SEQ)." " : "";
        $query .= "    AND ((REGD_S_DATE BETWEEN '".str_replace("/", "-", $REGD_S_DATE)."' AND '".str_replace("/", "-", $REGD_E_DATE)."') ";
        $query .= "    OR (REGD_E_DATE BETWEEN '".str_replace("/", "-", $REGD_S_DATE)."' AND '".str_replace("/", "-", $REGD_E_DATE)."')) ";

        return $query;
    }

    //志願者基礎データ取得
    public function getApplicantBaseData($model)
    {
        $query  = " SELECT ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     MAIN.NAME AS NAME, ";
        $query .="      SC_H.CURRICULUM_YEAR AS CURRICULUM_YEAR ";
        $query .= " FROM ";
        $query .= "     V_SCHREG_BASE_MST MAIN ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT SC_R ON SC_R.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "          AND SC_R.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND SC_R.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT SC_G ON SC_R.YEAR = SC_G.YEAR ";
        $query .= "          AND SC_R.GRADE = SC_G.GRADE ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT SC_H ON MAIN.SCHREGNO = SC_H.SCHREGNO ";
        $query .= "          AND SC_H.SCHOOL_KIND = SC_G.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     MAIN.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //名称マスタより取得
    public function getNamecd($year, $cd)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //高校データ取得
    public function getFinHighSchoolData($cd)
    {
        $query  = " SELECT ";
        $query .= "    FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "    FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "    FINSCHOOLCD = '".$cd."' ";

        return $query;
    }

    //新規コード取得
    public function getNewSEQ($cd)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .= "     ANOTHER_SCHOOL_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$cd."' ";

        return $query;
    }

    //--- INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjx_anotherQuery::getNewSEQ($model->field["SCHREGNO"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $seq = intval($row["SEQ"]) + 1;

        $data = array();
        $data["SCHREGNO"][TEXT]            = $model->field["SCHREGNO"];
        $data["SEQ"][NUMBER]               = $seq;
        $data["STUDENT_DIV"][TEXT]         = $model->field["STUDENT_DIV"];
        $data["FORMER_REG_SCHOOLCD"][TEXT] = sprintf("%0".($model->finschoolcdKeta)."s", $model->field["FINSCHOOLCD"]);
        $data["MAJOR_NAME"][TEXT]          = $model->field["MAJOR_NAME"];
        $data["REGD_S_DATE"][TEXT]         = str_replace("/", "-", $model->field["REGD_S_DATE"]);
        $data["REGD_E_DATE"][TEXT]         = str_replace("/", "-", $model->field["REGD_E_DATE"]);
        $data["PERIOD_MONTH_CNT"][TEXT]    = $model->field["PERIOD_MONTH_CNT"];
        $data["ABSENCE_CNT"][TEXT]         = $model->field["ABSENCE_CNT"];
        $data["MONTH_CNT"][TEXT]           = $model->field["MONTH_CNT"];
        $data["ENT_FORM"][TEXT]            = $model->field["ENT_FORM"];
        $data["REASON"][TEXT]              = $model->field["REASON"];
        $data["ANOTHER_SPORT"][TEXT]       = $model->field["ANOTHER_SPORT"];

        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ANOTHER_SCHOOL_HIST_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return $seq;
    }

    //--- UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHREGNO"][TEXT]            = $model->field["SCHREGNO"];
        $data["SEQ"][NUMBER]               = $model->field["SEQ"];
        $data["STUDENT_DIV"][TEXT]         = $model->field["STUDENT_DIV"];
        $data["FORMER_REG_SCHOOLCD"][TEXT] = sprintf("%0".($model->finschoolcdKeta)."s", $model->field["FINSCHOOLCD"]);
        $data["MAJOR_NAME"][TEXT]          = $model->field["MAJOR_NAME"];
        $data["REGD_S_DATE"][TEXT]         = str_replace("/", "-", $model->field["REGD_S_DATE"]);
        $data["REGD_E_DATE"][TEXT]         = str_replace("/", "-", $model->field["REGD_E_DATE"]);
        $data["PERIOD_MONTH_CNT"][TEXT]    = $model->field["PERIOD_MONTH_CNT"];
        $data["ABSENCE_CNT"][TEXT]         = $model->field["ABSENCE_CNT"];
        $data["MONTH_CNT"][TEXT]           = $model->field["MONTH_CNT"];
        $data["ENT_FORM"][TEXT]            = $model->field["ENT_FORM"];
        $data["REASON"][TEXT]              = $model->field["REASON"];
        $data["ANOTHER_SPORT"][TEXT]       = $model->field["ANOTHER_SPORT"];

        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE SCHREGNO = '".$model->schregno."' AND";
        $where .= "       SEQ     = ".$model->seq." ";

        $query = Query::updateSQL($data, "ANOTHER_SCHOOL_HIST_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //前籍校履歴データ
        $query = " DELETE FROM ANOTHER_SCHOOL_HIST_DAT ";
        $query.= "       WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query.= "             SEQ = ".$model->seq." ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- INSERT or UPDATE
    public function &getUpdateSchregBaseDetailMst($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $base_seq = "008";
        $data = array();
        $data["SCHREGNO"][TEXT]      = $model->field["SCHREGNO"];
        $data["BASE_SEQ"][TEXT]      = $base_seq;
        $data["BASE_REMARK1"][TEXT]  = $model->field["ANOTHER_SPORT"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE SCHREGNO = '".$model->schregno."' ";
        $where .= "   AND BASE_SEQ = '".$base_seq."' ";
        $query  = " SELECT COUNT(*) FROM SCHREG_BASE_DETAIL_MST " . $where;
        if (0 < $db->getOne($query)) {
            $query = Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where);
        } else {
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- COUNT
    public function &getBaseRemark1($model)
    {
        $query  = " SELECT BASE_REMARK1 ";
        $query .= "   FROM SCHREG_BASE_DETAIL_MST ";
        $query .= "  WHERE SCHREGNO = '".$model->schregno."' ";
        $query .= "    AND BASE_SEQ = '008' ";

        return $query;
    }

    //学校コードが存在するかどうか
    public function getFinschoolcd($model)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD='{$model->field['FINSCHOOLCD']}' ";
        $query .= "  ";

        return $query;
    }
}
