<?php

require_once('for_php7.php');

class knjj091Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";

        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     NAMECD1 = 'J004' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //委員会区分
    function getCommitteeFlg() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'J003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称取得
    function getNameMst($cd1,$cd2)
    {

        $query  = " SELECT DISTINCT CAST(T1.NAMECD2 AS INT) AS NAMECD2,T1.NAME1";
        $query .= " FROM NAME_MST T1,NAME_YDAT T2";
        $query .= " WHERE   T1.NAMECD1  = '" .$cd1 ."'";
        $query .= "     AND T2.YEAR = '" .$cd2 ."'";
        $query .= "     AND T2.NAMECD1  = T1.NAMECD1";
        $query .= "     AND T2.NAMECD2  = T1.NAMECD2";
        if ($cd1 == "J002") {
            $query .= " ORDER BY ";
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //委員会情報を取得
    function getCommiMst($model, $year, $committee_flg) {
        $query  = "     SELECT T3.COMMITTEE_FLG,T3.COMMITTEECD,T3.COMMITTEENAME ";
        $query .= "     FROM COMMITTEE_YDAT T2,COMMITTEE_MST T3 ";
        $query .= "     WHERE   T2.YEAR     = '".$year."'";
        $query .= "         AND T2.COMMITTEE_FLG = T3.COMMITTEE_FLG";
        $query .= "         AND T2.COMMITTEECD   = T3.COMMITTEECD ";
        $query .= "         AND T2.COMMITTEE_FLG   = '{$committee_flg}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "         AND T2.SCHOOLCD     = T3.SCHOOLCD ";
            $query .= "         AND T2.SCHOOL_KIND  = T3.SCHOOL_KIND ";
            $query .= "         AND T2.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "         AND T2.SCHOOL_KIND  = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "         AND T2.SCHOOLCD     = T3.SCHOOLCD ";
            $query .= "         AND T2.SCHOOL_KIND  = T3.SCHOOL_KIND ";
            $query .= "         AND T2.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "         AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CAST(T3.COMMITTEECD AS INT) ";

        return $query;
    }


    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($schregno){
        $query  ="SELECT schregno,name_show as name FROM schreg_base_mst";
        $query .=" WHERE schregno = '".$schregno."'";

        return $query;
    }

    //HR学籍委員会履歴データよりよりデータを取得
    function getCouseling($model, $year, $schregno)
    {
        $query  = " SELECT ";
        $query .= " T1.YEAR, ";
        $query .= " T1.SEQ, ";
        $query .= " L1.NAME1 AS SEMESTERNAME, ";
        $query .= " T1.SEMESTER, ";
        $query .= " T1.SCHREGNO, ";
        $query .= " T1.COMMITTEE_FLG, ";
        $query .= " T1.COMMITTEECD, ";
        $query .= " T1.CHARGENAME, ";
        $query .= " T5.COMMITTEENAME, ";
        $query .= " T1.EXECUTIVECD, ";
        $query .= " T4.NAME1, ";
        $query .= " T6.NAME1 AS COMMITTEE_FLG_NAME, ";
        $query .= " T1.GRADE ";
        $query .= " FROM ";
        $query .= " (";
        $query .= "     SELECT * ";
        $query .= "     FROM SCHREG_COMMITTEE_HIST_DAT ";
        $query .= "     WHERE   SCHREGNO    = '".$schregno."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ) T1";
        $query .= " LEFT JOIN (";
        $query .= "     SELECT T3.COMMITTEE_FLG,T3.COMMITTEECD,T3.COMMITTEENAME ";
        $query .= "     FROM COMMITTEE_YDAT T2,COMMITTEE_MST T3 ";
        $query .= "     WHERE   T2.YEAR      = '".$year."'";
        $query .= "     AND T2.COMMITTEE_FLG = T3.COMMITTEE_FLG";
        $query .= "     AND T2.COMMITTEECD   = T3.COMMITTEECD";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND T2.SCHOOLCD     = T3.SCHOOLCD ";
            $query .= "     AND T2.SCHOOL_KIND  = T3.SCHOOL_KIND ";
            $query .= "     AND T2.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T2.SCHOOL_KIND  = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T2.SCHOOLCD     = T3.SCHOOLCD ";
            $query .= "     AND T2.SCHOOL_KIND  = T3.SCHOOL_KIND ";
            $query .= "     AND T2.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ) T5 ON (T5.COMMITTEE_FLG = T1.COMMITTEE_FLG AND T5.COMMITTEECD  = T1.COMMITTEECD)";
        $query .= " LEFT JOIN (";
        $query .= "     SELECT NAMECD2,NAME1 ";
        $query .= "     FROM NAME_MST ";
        $query .= "     WHERE NAMECD1 = 'J002'";
        $query .= " ) T4 ON (T4.NAMECD2 = T1.EXECUTIVECD)";
        $query .= " LEFT JOIN (";
        $query .= "     SELECT NAMECD2,NAME1 ";
        $query .= "     FROM NAME_MST ";
        $query .= "     WHERE NAMECD1 = 'J003'";
        $query .= " ) T6 ON (T6.NAMECD2 = T1.COMMITTEE_FLG)";
        $query .= " LEFT JOIN (";
        $query .= "     SELECT YEAR,NAMECD2,NAME1 ";
        $query .= "     FROM V_NAME_MST ";
        $query .= "     WHERE NAMECD1 = 'J004'";
        $query .= " ) L1 ON (L1.YEAR = T1.YEAR AND L1.NAMECD2 = T1.SEMESTER)";
        $query .= " ORDER BY T1.YEAR,T1.COMMITTEE_FLG,T1.COMMITTEECD,T1.EXECUTIVECD ";

        return $query;
    }

    //１レコード取得（右上リストで選択された１行を取得）
    function getRow($model,$index, $db)
    {

        $query  = "SELECT * FROM SCHREG_COMMITTEE_HIST_DAT ";
        $query .= " WHERE    YEAR   = '".$model->control_data["年度"]."'";
        $query .= "     AND SEQ     = ".$index;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        return $row;
    }

    //記録備考データ存在チェック
    function checkDetailExist($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEQ = ".$model->field["SEQPOST"]." AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $cnt = $db->getOne($query);

        Query::dbCheckIn($db);
        return $cnt;
    }

//  /**
//   * INSERT
//   */
    function &getInsertQuery($model)
    {
//echo "query=".$model->grade;
        $db = Query::dbCheckOut();
        $querymax = "SELECT MAX(SEQ) AS MAXSEQ FROM SCHREG_COMMITTEE_HIST_DAT";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $querymax .= " WHERE ";
            $querymax .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $querymax .= " AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $querymax .= " WHERE ";
            $querymax .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $querymax .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $resultmax = $db->query($querymax);
        $rowmax = $resultmax->fetchRow(DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        $maxindex = $rowmax["MAXSEQ"] + 1;          //インデックス最大値

        $db = Query::dbCheckOut();

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $model->schKind;
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
        }
        $data["YEAR"][TEXT]                 = $model->control_data["年度"];
        $data["SEQ"][NUMBER]                = $maxindex;
        $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["COMMITTEE_FLG"][TEXT]        = $model->field["COMMITTEE_FLG"];
        $data["COMMITTEECD"][TEXT]          = $model->field["COMMITTEECD"];
        $data["CHARGENAME"][TEXT]           = $model->field["CHARGENAME"];
        $data["EXECUTIVECD"][TEXT]          = $model->field["EXECUTIVECD"];
        $data["GRADE"][TEXT]                = $model->grade;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_COMMITTEE_HIST_DAT");

        $db->query($query);

        Query::dbCheckIn($db);

        return $maxindex;
    }
//  /**
//   * UPDATE
//   */
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $model->schKind;
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
        }
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
        $data["COMMITTEE_FLG"][TEXT]        = $model->field["COMMITTEE_FLG"];
        $data["COMMITTEECD"][TEXT]          = $model->field["COMMITTEECD"];
        $data["CHARGENAME"][TEXT]           = $model->field["CHARGENAME"];
        $data["EXECUTIVECD"][TEXT]          = $model->field["EXECUTIVECD"];
        $data["GRADE"][TEXT]                = $model->grade;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE YEAR      = '".$model->control_data["年度"]."'";
        $where .= "   AND SEQ       = ".$model->field["SEQPOST"];
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $where .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $where .= " AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $where .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $query = Query::updateSQL($data, "SCHREG_COMMITTEE_HIST_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);

        return ;
    }
//  /**
//   * DELETE
//   */
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM SCHREG_COMMITTEE_HIST_DAT ";
        $query .= " WHERE YEAR  = '".$model->control_data["年度"]."'";
        $query .= "     AND SEQ = ".$model->field["SEQPOST"];
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }


}
?>