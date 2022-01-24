<?php

require_once('for_php7.php');

class knjj092Query extends Query {
/************ 左画面 ***************/
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

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
        $query .= "         YEAR    = '". CTRL_YEAR ."' ";
        $query .= "     AND NAMECD1 = 'J004' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //委員会取得
    function getCommitteeNameList($model) {
        $query  = " SELECT ";
        $query .= "     COM_Y.COMMITTEE_FLG || '-' || COM_Y.COMMITTEECD AS COMMITTEECD, ";
        $query .= "     COM_Y.COMMITTEE_FLG || '-' || COM_Y.COMMITTEECD || '　' || COM_M.COMMITTEENAME AS COMMITTEENAME ";
        $query .= " FROM ";
        $query .= "     COMMITTEE_YDAT COM_Y, ";
        $query .= "     COMMITTEE_MST COM_M ";
        //参照・更新可（制限付き
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     INNER JOIN (SELECT ";
            $query .= "                     COMMITTEECD ";
            $query .= "                 FROM ";
            $query .= "                     COMMITTEE_ADVISER_DAT ";
            $query .= "                 WHERE ";
            $query .= "                         YEAR        = '".CTRL_YEAR."' ";
            $query .= "                     AND ADVISER     = '".STAFFCD."' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                 AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "                 AND SCHOOL_KIND = '".$model->schkind."' ";
            }
            $query .= "                 ) COM_A ON COM_A.COMMITTEECD = COM_M.COMMITTEECD ";
        }
        $query .= " WHERE ";
        $query .= "         COM_Y.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND COM_Y.COMMITTEECD  = COM_M.COMMITTEECD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND COM_Y.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND COM_Y.SCHOOL_KIND  = '".$model->schkind."' ";
            $query .= " AND COM_M.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND COM_M.SCHOOL_KIND  = '".$model->schkind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     COM_Y.COMMITTEE_FLG || COM_Y.COMMITTEECD ";

        return $query;
    }

    //委員名簿一覧取得
    function getStudentList($model, $selectcommittee) {
        $query  = " WITH SCHREG_COMMITTEE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEQ, ";
        $query .= "         SCHREGNO, ";
        $query .= "         COMMITTEE_FLG, ";
        $query .= "         COMMITTEECD, ";
        $query .= "         EXECUTIVECD ";
        $query .= "     FROM ";
        $query .= "         SCHREG_COMMITTEE_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         COMMITTEE_FLG || '-' || COMMITTEECD = '".$selectcommittee."' ";
        $query .= "         AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER    = '".$model->semester."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->schkind."' ";
        }
        $query .= " ), SCHREG_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.YEAR, ";
        $query .= "         REGD.SEMESTER, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         BASE.NAME_SHOW, ";
        $query .= "         BASE.SEX, ";
        $query .= "         HDAT.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD, ";
        $query .= "         SCHREG_REGD_HDAT HDAT, ";
        $query .= "         SCHREG_BASE_MST BASE ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "       , SCHREG_REGD_GDAT GDAT ";
        }
        $query .= "     WHERE ";
        $query .= "             HDAT.YEAR     = REGD.YEAR ";
        $query .= "         AND REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "         AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "         AND BASE.SCHREGNO = REGD.SCHREGNO ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND GDAT.YEAR         = HDAT.YEAR ";
            $query .= "     AND GDAT.GRADE        = HDAT.GRADE ";
            $query .= "     AND GDAT.SCHOOL_KIND  = '".$model->schkind."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     COMM.SEQ, ";
        $query .= "     COMM.SCHREGNO, ";
        $query .= "     COMM.COMMITTEE_FLG || '-' || COMM.COMMITTEECD AS COMMITTEECD, ";
        $query .= "     J002.NAME1 AS EXECUTIVENAME, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.NAME_SHOW, ";
        $query .= "     REGD.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE COMM ";
        $query .= "         INNER JOIN SCHREG_REGD REGD ON COMM.SCHREGNO = REGD.SCHREGNO ";
        $query .= " LEFT JOIN V_NAME_MST J002 ";
        $query .= "      ON J002.YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND J002.NAMECD1 = 'J002' ";
        $query .= "     AND J002.NAMECD2 = COMM.EXECUTIVECD ";
        $query .= " ORDER BY ";
        $query .= "     COMM.EXECUTIVECD, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

/************ 右画面 ***************/
    //１レコード取得（学籍委員会履歴データ）edit.php
    function getCommitteeHistory_DatEdit($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '".$model->semester."' ";
        $query .= "     AND SEQ         = ".$model->seq." ";
        $query .= "     AND SCHREGNO    = '".$model->schregNo."' ";
        $query .= "     AND COMMITTEE_FLG || '-' || COMMITTEECD = '".$model->committeecd."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //今年度の生徒の名前を取得（上のリストで選んだデータの生徒のみ）
    function getStudent_data_One($snendo, $sgakki, $sno) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO AS SCHREGNO, ";
        $query .= "     BASE.NAME_SHOW AS STUDENTNAME, ";
        $query .= "     REGD.GRADE AS GRADE, ";
        $query .= "     HDAT.HR_NAME || '-' || REGD.ATTENDNO || '番' AS NENKUMI ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD, ";
        $query .= "     SCHREG_BASE_MST BASE, ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = HDAT.YEAR ";
        $query .= "     AND REGD.YEAR     = '".$snendo."' ";
        $query .= "     AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "     AND REGD.SEMESTER = '".$sgakki."' ";
        $query .= "     AND REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     AND REGD.SCHREGNO = '".$sno."' ";
        $query .= "     AND REGD.GRADE    = HDAT.GRADE ";
        $query .= "     AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     REGD.SCHREGNO ";

        return $query;
    }

    //NAME_MSTから役職名を取得
    function getNameMst($nameCd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '　' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$nameCd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //在校生検索ボタン
    function getSchregregd_data($model, $ych, $gch, $sch) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO AS SCHREGNO, ";
        $query .= "     BASE.NAME AS STUDENTNAME, ";
        $query .= "     HDAT.HR_NAME || '-' || REGD.ATTENDNO || '番' AS NENKUMI ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD, ";
        $query .= "     SCHREG_BASE_MST BASE, ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "      ON HDAT.YEAR         = G1.YEAR ";
            $query .= "     AND HDAT.GRADE        = G1.GRADE ";
            $query .= "     AND G1.SCHOOL_KIND  = '".$model->schkind."' ";
        }
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = HDAT.YEAR ";
        $query .= "     AND REGD.YEAR     = '".$ych."' ";
        $query .= "     AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "     AND REGD.SEMESTER = '".$gch."' ";
        $query .= "     AND REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     AND REGD.SCHREGNO = '".$sch."' ";
        $query .= "     AND REGD.GRADE    = HDAT.GRADE ";
        $query .= "     AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     REGD.SCHREGNO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //記録備考データ存在チェック
    function checkDetailExist($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEQ      = ".$model->seq." ";
        $query .= "     AND SCHREGNO = '".$model->schregNo."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        }

        $cnt = $db->getOne($query);

        Query::dbCheckIn($db);
        return $cnt;
    }

/******** 追加更新処理  ********/
    //--- UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        list($comFlg, $comCd) = explode("-", $fields["HID_COMMITTEECD"]);

        $data = array();
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["SCHREGNO"][TEXT]         = $fields["SCHREGNO"];
        $data["GRADE"][TEXT]            = $fields["GRADE"];
        $data["COMMITTEE_FLG"][TEXT]    = $comFlg;
        $data["COMMITTEECD"][TEXT]      = $comCd;
        $data["EXECUTIVECD"][TEXT]      = $fields["EXECUTIVECD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE YEAR = '".CTRL_YEAR."' ";
        $where .= "   AND SEQ  = ".$model->seq." ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND  = '".$model->schkind."' ";
        }

        $query = Query::updateSQL($data, "SCHREG_COMMITTEE_HIST_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return true;
    }

    //--- INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        list($comFlg, $comCd) = explode("-", $fields["HID_COMMITTEECD"]);

        $queryMax  = " SELECT MAX(SEQ) AS MAXSEQ FROM SCHREG_COMMITTEE_HIST_DAT ";
        $queryMax .= " WHERE YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $queryMax .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $queryMax .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        }
        $maxSeq = $db->getOne($queryMax) + 1;  //インデックス最大値 + 1

        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]  = $model->schkind;
        }
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQ"][NUMBER]            = $maxSeq;
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["SCHREGNO"][TEXT]         = $fields["SCHREGNO"];
        $data["GRADE"][TEXT]            = $fields["GRADE"];
        $data["COMMITTEE_FLG"][TEXT]    = $comFlg;
        $data["COMMITTEECD"][TEXT]      = $comCd;
        $data["EXECUTIVECD"][TEXT]      = $fields["EXECUTIVECD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_COMMITTEE_HIST_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return true;
    }

    //--- DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEQ  = ".$model->seq." ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

/******** 一括更新画面  ********/
    //年組取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     HDAT.GRADE || HDAT.HR_CLASS AS VALUE, ";
        $query .= "     HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = HDAT.YEAR AND GDAT.GRADE = HDAT.GRADE ";
            $query .= "     AND SCHOOL_KIND = '".$model->schkind."' ";
        }
        $query .= " WHERE ";
        $query .= "     HDAT.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     HDAT.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧取得(左リスト)
    function getSchregLeftList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.SCHREGNO AS VALUE, ";
        $query .= "     REGD.ATTENDNO || '番　' || BASE.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT COM_D ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON COM_D.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                   AND COM_D.YEAR     = REGD.YEAR  ";
        $query .= "                                   AND REGD.SEMESTER  = '".CTRL_SEMESTER."'  ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO  ";
        $query .= " WHERE ";
        $query .= "         COM_D.YEAR        = '".CTRL_YEAR."'  ";
        $query .= "     AND COM_D.SEMESTER    = '".$model->semester."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND COM_D.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND COM_D.SCHOOL_KIND = '".$model->schkind."' ";
        }
        $query .= "     AND COM_D.COMMITTEE_FLG || '-' || COM_D.COMMITTEECD = '".$model->field["COMMITTEE_IKKATSU"]."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧取得(右リスト)
    function getSchregRightList($model, $left_schregno) {
        $schregno_target = implode("','", (array)$left_schregno);

        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO AS VALUE, ";
        $query .= "     REGD.ATTENDNO || '番　' || BASE.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND REGD.SCHREGNO NOT IN ('{$schregno_target}') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    function checkExist($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND GRADE    = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
        $query .= "     AND COMMITTEE_FLG || '-' || COMMITTEECD = '".$model->field["COMMITTEE_IKKATSU"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        }

        return $query;
    }

    //--- INSERT
    function &getSubInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        list($comFlg, $comCd) = explode("-", $model->field["COMMITTEE_IKKATSU"]);
        $schregno = array();
        $schregno = explode(',', $model->field["SUB_SCHREGNO"]);

        //削除
        $setSchNo = str_replace(',', "','", $model->field["SUB_SCHREGNO"]);
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND COMMITTEE_FLG || '-' || COMMITTEECD = '".$model->field["COMMITTEE_IKKATSU"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        }
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                          SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                          SCHREG_REGD_DAT ";
        $query .= "                      WHERE ";
        $query .= "                              YEAR     = '".CTRL_YEAR."' ";
        $query .= "                          AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                          AND GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "                          AND SCHREGNO NOT IN ('{$setSchNo}') ";
        $query .= "                      ) ";

        $db->query($query);

        //MAXSEQ取得
        $queryMax  = " SELECT MAX(SEQ) AS MAXSEQ FROM SCHREG_COMMITTEE_HIST_DAT ";
        $queryMax .= " WHERE YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $queryMax .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $queryMax .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        }
        $maxSeq = $db->getOne($queryMax) + 1;  //インデックス最大値

        //インサート
        if ($model->field["SUB_SCHREGNO"]) {
            for ($i = 0; $i < get_count($schregno); $i++) {
                //存在チェック
                $check = $db->getOne(knjj092Query::checkExist($model, $schregno[$i]));
                if (!$check) {
                    if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
                        $data["SCHOOL_KIND"][TEXT]  = $model->schkind;
                    }
                    $data["YEAR"][TEXT]             = CTRL_YEAR;
                    $data["SEQ"][NUMBER]            = $maxSeq;
                    $data["SEMESTER"][TEXT]         = $model->semester;
                    $data["SCHREGNO"][TEXT]         = $schregno[$i];
                    $data["GRADE"][TEXT]            = substr($model->field["GRADE_HR_CLASS"], 0, 2);
                    $data["COMMITTEE_FLG"][TEXT]    = $comFlg;
                    $data["COMMITTEECD"][TEXT]      = $comCd;
                    $data["CHARGENAME"][TEXT]       = "";
                    $data["EXECUTIVECD"][TEXT]      = "";
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $query = Query::insertSQL($data, "SCHREG_COMMITTEE_HIST_DAT");
                    $db->query($query);
                    $maxSeq++;
                } else {
                    continue;
                }
            }
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }
}
?>
