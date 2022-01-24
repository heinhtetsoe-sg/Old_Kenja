<?php

require_once('for_php7.php');

class knjxcommi_detailQuery extends Query
{

    //リスト取得
    public function getList($model)
    {
        $query  = " WITH COMMITTEE AS( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         COMMITTEE_MST T1, ";
        $query .= "         COMMITTEE_YDAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.COMMITTEE_FLG = T2.COMMITTEE_FLG AND ";
        $query .= "         T1.COMMITTEECD = T2.COMMITTEECD AND ";
        $query .= "         T2.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "     AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SCHOOL_KIND  = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "     AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ), HIST AS( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_COMMITTEE_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SCHREGNO = '".$model->schregno."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T3.COMMITTEENAME, ";
        $query .= "     T2.CHARGENAME, ";
        $query .= "     T4.NAME1 AS EXECUTIVENAME, ";
        $query .= "     T1.DETAIL_DATE, ";
        $query .= "     T1.DETAIL_SEQ, ";
        $query .= "     T1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT T1, ";
        $query .= "     HIST T2 ";
        $query .= "     LEFT JOIN COMMITTEE T3 ON ";
        $query .= "         T2.COMMITTEE_FLG = T3.COMMITTEE_FLG AND ";
        $query .= "         T2.COMMITTEECD = T3.COMMITTEECD ";
        $query .= "     LEFT JOIN V_NAME_MST T4 ON  ";
        $query .= "         T2.YEAR = T4.YEAR AND  ";
        $query .= "         T4.NAMECD1 = 'J002' AND  ";
        $query .= "         T2.EXECUTIVECD = T4.NAMECD2  ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.SEQ = T2.SEQ ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.DETAIL_DATE, ";
        $query .= "     T1.DETAIL_SEQ ";

        return $query;
    }

    //生徒情報取得
    public function getSchinfo($model)
    {
        $query  = " SELECT ";
        $query .= "     T3.HR_NAME || ' ' || T1.ATTENDNO || '番　　' || T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON ";
        $query .= "         T1.YEAR = T3.YEAR AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.GRADE = T3.GRADE AND ";
        $query .= "         T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //１レコード取得
    public function getRow($model)
    {
        $date = str_replace("/", "-", $model->date);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     SEQ = ".$model->seq." AND ";
        $query .= "     DETAIL_DATE = '".$date."' AND ";
        $query .= "     DETAIL_SEQ = ".$model->detail_seq." ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //委員会取得
    public function getCommitteeName($model)
    {
        $query  = " WITH COMMITTEE AS( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         COMMITTEE_MST T1, ";
        $query .= "         COMMITTEE_YDAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.COMMITTEE_FLG = T2.COMMITTEE_FLG AND ";
        $query .= "         T1.COMMITTEECD = T2.COMMITTEECD AND ";
        $query .= "         T2.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "     AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SCHOOL_KIND  = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "     AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     T1.SEQ AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     LEFT JOIN COMMITTEE T2 ON ";
        $query .= "         T1.COMMITTEE_FLG = T2.COMMITTEE_FLG AND ";
        $query .= "         T1.COMMITTEECD = T2.COMMITTEECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //DETAIL_SEQ取得
    public function getDetailSeq($model)
    {
        $db = Query::dbCheckOut();
        $date = str_replace("/", "-", $model->field["DETAIL_DATE"]);

        $query  = " SELECT ";
        $query .= "     MAX(DETAIL_SEQ) AS DETAIL_SEQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     SEQ = ".$model->field["SEQ"]." AND ";
        $query .= "     DETAIL_DATE = '".$date."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $seq = $db->getOne($query);

        Query::dbCheckIn($db);
        return $seq;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        if ($model->cmd == "update") {
            knjxcommi_detailQuery::getDeleteQuery($model);
        } else {
            $result = knjxcommi_detailQuery::getDetailSeq($model);
            $model->field["DETAIL_SEQ"] = ($result) ? $result+1 : 1;
        }

        $db = Query::dbCheckOut();

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $model->schKind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
        }
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $model->field["SEQ"];
        $data["DETAIL_DATE"][TEXT]      = str_replace("/", "-", $model->field["DETAIL_DATE"]);
        $data["DETAIL_SEQ"][NUMBER]     = $model->field["DETAIL_SEQ"];
        $data["DETAIL_REMARK"][TEXT]    = $model->field["DETAIL_REMARK"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_COMMITTEE_HDETAIL_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return $model->field["DETAIL_SEQ"];
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     SEQ = ".$model->field["SEQ"]." AND ";
        $query .= "     DETAIL_DATE = '".str_replace("/", "-", $model->field["DETAIL_DATE"])."' AND ";
        $query .= "     DETAIL_SEQ = ".$model->field["DETAIL_SEQ"]." ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
