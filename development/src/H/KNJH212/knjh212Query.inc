<?php

require_once('for_php7.php');

class knjh212Query extends Query {
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

    //寮データ取得
    function getDomitoryNameList($model) {
        $query  = " SELECT ";
        $query .= "     T1.DOMI_CD AS DOMI_CD, ";
        $query .= "     T1.DOMI_CD || '　' || T2.DOMI_NAME AS DOMI_NAME ";
        $query .= " FROM ";
        $query .= "     DOMITORY_YDAT T1, ";
        $query .= "     DOMITORY_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.DOMI_CD   = T2.DOMI_CD ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOMI_CD ";

        return $query;
    }

    //寮員名簿一覧取得
    function getStudentList($model, $selectdomi) {
        $query  = " WITH SCHREG_DOMI AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         DOMI_CD, ";
        $query .= "         DOMI_ENTDAY, ";
        $query .= "         DOMI_OUTDAY, ";
        $query .= "         REMARK ";
        $query .= "     FROM ";
        $query .= "         SCHREG_DOMITORY_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         DOMI_CD = '".$selectdomi."' ";
        $query .= " ), SCHREG_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.YEAR, ";
        $query .= "         T2.SEMESTER, ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.HR_CLASS, ";
        $query .= "         T2.ATTENDNO, ";
        $query .= "         T3.NAME_SHOW, ";
        $query .= "         T3.SEX, ";
        $query .= "         T4.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T2, ";
        $query .= "         SCHREG_REGD_HDAT T4, ";
        $query .= "         SCHREG_BASE_MST T3 ";
        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "       , SCHREG_REGD_GDAT T5 ";
        }
        $query .= "     WHERE ";
        $query .= "         T4.YEAR     = T2.YEAR AND ";
        $query .= "         T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T4.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T4.GRADE    = T2.GRADE AND ";
        $query .= "         T4.HR_CLASS = T2.HR_CLASS AND ";
        $query .= "         T3.SCHREGNO = T2.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND T5.YEAR         = T4.YEAR ";
            $query .= "     AND T5.GRADE        = T4.GRADE ";
            $query .= "     AND T5.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T5.YEAR         = T4.YEAR ";
            $query .= "     AND T5.GRADE        = T4.GRADE ";
            $query .= "     AND T5.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.DOMI_CD, ";
        $query .= "     T1.DOMI_ENTDAY, ";
        $query .= "     T1.DOMI_OUTDAY, ";
        $query .= "     T1.REMARK, ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_DOMI T1 ";
        $query .= "     INNER JOIN SCHREG_REGD T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.DOMI_ENTDAY ";

        return $query;
    }

    //１レコード取得（学籍寮履歴データ）edit.php
    function getDomitoryHistory_DatEdit($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_DOMITORY_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->gakusekino."' AND ";
        $query .= "     DOMI_CD      = '".$model->domicd."' AND ";
        $query .= "     DOMI_ENTDAY       = '".str_replace("/","-",$model->enterdate)."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //今年度の生徒の名前を取得（上のリストで選んだデータの生徒のみ）
    function getStudent_data_One($snendo, $sgakki, $sno) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS SCHREGNO, ";
        $query .= "     T2.NAME_SHOW AS STUDENTNAME, ";
        $query .= "     T3.HR_NAME || '-' || T1.ATTENDNO || '番' AS NENKUMI ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T3.YEAR AND ";
        $query .= "     T1.YEAR         = '".$snendo."' AND ";
        $query .= "     T1.SEMESTER     = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = '".$sgakki."' AND ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     T1.SCHREGNO     = '".$sno."' AND ";
        $query .= "     T1.GRADE        = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS     = T3.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //NAME_MSTから役職名を取得
    function getName_Data($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.NAMECD2, ";
        $query .= "     T1.NAMECD2 || '　' || T1.NAME1 AS NAME ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1, ";
        $query .= "     NAME_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1  = '".$model->namecd1."' AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.NAMECD1  = T1.NAMECD1 AND ";
        $query .= "     T2.NAMECD2  = T1.NAMECD2 ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2 ";

        return $query;
    }

    //在校生検索ボタン
    function getSchregregd_data($model, $ych, $gch, $sch) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS SCHREGNO, ";
        $query .= "     T2.NAME AS STUDENTNAME, ";
        $query .= "     T3.HR_NAME || '-' || T1.ATTENDNO || '番' AS NENKUMI ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "      ON T3.YEAR         = G1.YEAR ";
            $query .= "     AND T3.GRADE        = G1.GRADE ";
            $query .= "     AND G1.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "      ON T3.YEAR         = G1.YEAR ";
            $query .= "     AND T3.GRADE        = G1.GRADE ";
            $query .= "     AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T3.YEAR AND ";
        $query .= "     T1.YEAR         = '".$ych."' AND ";
        $query .= "     T1.SEMESTER     = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = '".$gch."' AND ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     T1.SCHREGNO     = '".$sch."' AND ";
        $query .= "     T1.GRADE        = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS     = T3.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //DB登録キー重複チェック
    function getDomitoryHistory_key_chk($schregno, $entday) {
        $db = Query::dbCheckOut();

        $query  = knjh212Query::getDomitoryHistory_key_chk_query($schregno, $entday);

        $getrec = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $getrec;
    }

    function getDomitoryHistory_key_chk_query($schregno, $entday) {
        $query  = " SELECT * ";
        $query .= " FROM SCHREG_DOMITORY_HIST_DAT ";
        $query .= "    WHERE SCHREGNO    = '".$schregno."' ";
        $query .= "      AND DOMI_ENTDAY = '".str_replace("/","-",$entday)."' ";
        return $query;
    }

    //１レコード取得（学籍寮履歴データ）追加・更新・削除時チェック
    function getDomitoryHistory_Dat($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_DOMITORY_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->field["SCHREGNO"]."' AND ";
        $query .= "     DOMI_CD      = '".$model->field["DOMI_CODEALL"]."' AND ";
        $query .= "     DOMI_ENTDAY       = '".str_replace("/","-",$model->field["DOMI_ENTDAY"])."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    function getDomitoryHistory_Dat_chk($schregno, $entday, $outday="") {
        $db = Query::dbCheckOut();

        $query  = knjh212Query::getDomitoryHistory_Dat_chk_query($schregno, $entday, $outday);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    function getDomitoryHistory_Dat_chk_query($schregno, $entday, $outday="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_DOMITORY_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' ";
        $query .= "      AND DOMI_ENTDAY != '".str_replace("/","-",$entday)."' ";
        if ($outday != "") {
            //内側から始まる
            $query .= "      AND ((DOMI_ENTDAY    <= '".str_replace("/","-",$entday)."' AND ( DOMI_OUTDAY IS NULL OR DOMI_OUTDAY >= '".str_replace("/","-",$entday)."')) ";
            //内側で止まる
            $query .= "           OR (DOMI_ENTDAY <= '".str_replace("/","-",$outday)."' AND ( DOMI_OUTDAY IS NULL OR DOMI_OUTDAY >= '".str_replace("/","-",$outday)."')) ";
            //範囲を包含
            $query .= "           OR (DOMI_ENTDAY >= '".str_replace("/","-",$entday)."' AND ( DOMI_OUTDAY <= '".str_replace("/","-",$outday)."'))) ";
        } else {
            $query .= "      AND (DOMI_OUTDAY IS NULL OR DOMI_OUTDAY > '".str_replace("/","-",$entday)."' ) ";
        }
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $code = $fields["DOMICODE1"];

        $data = array();
        $data["SCHREGNO"][TEXT]     = $fields["SCHREGNO"];
        $data["DOMI_CD"][TEXT]      = $fields["DOMI_CODEALL"];
        $data["DOMI_ENTDAY"][TEXT]  = str_replace("/","-",$fields["DOMI_ENTDAY"]);
        $data["DOMI_OUTDAY"][TEXT]  = str_replace("/","-",$fields["DOMI_OUTDAY"]);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE SCHREGNO    = '" .$fields["SCHREGNO"] ."' ";
        $where .= "   AND DOMI_CD     = '" .$fields["DOMI_CODEALL"] ."' ";
        $where .= "   AND DOMI_ENTDAY = '" .str_replace("/","-",$fields["DOMI_ENTDAY"]) ."' ";

        $query = Query::updateSQL($data, "SCHREG_DOMITORY_HIST_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return true;
    }

    //--- INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["SCHREGNO"][TEXT]     = $fields["SCHREGNO"];
        $data["DOMI_CD"][TEXT]      = $fields["DOMI_CODEALL"];
        $data["DOMI_ENTDAY"][TEXT]  = str_replace("/","-",$fields["DOMI_ENTDAY"]);
        $data["DOMI_OUTDAY"][TEXT]  = str_replace("/","-",$fields["DOMI_OUTDAY"]);
        $data["REMARK"][TEXT]       = "";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_DOMITORY_HIST_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return true;
    }

    //--- DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
//        $code = $fields["DOMI_CODE1"];

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_DOMITORY_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '" .$fields["SCHREGNO"] ."' ";
        $query .= " AND DOMI_CD      = '" .$fields["DOMI_CODEALL"] ."' ";
        $query .= " AND DOMI_ENTDAY  = '" .str_replace("/","-",$fields["DOMI_ENTDAY"]) ."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    //存在チェック
    function checkExist($model, $schregno) {
        $sdate = str_replace("/","-",$model->field["SUB_SDATE"]);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_DOMITORY_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     DOMI_CD      = '".$model->field["DOMI_CODEALL"]."' AND ";
        $query .= "     ( DOMI_OUTDAY IS NULL OR DOMI_OUTDAY > '".$sdate."' OR DOMI_ENTDAY = '".$sdate."' ) ";

        return $query;
    }

    //--- INSERT（入寮日）
    function &getSubInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $schregno = explode(',', $model->field["SUB_SCHREGNO"]);
        $cnt = 0;

        if ($model->field["SUB_SCHREGNO"]) {
            for ($i = 0; $i < get_count($schregno); $i++) {

                //存在チェック
                $check1 = $db->getOne(knjh212Query::checkExist($model, $schregno[$i]));
                $check2 = $db->getRow(knjh212Query::getDomitoryHistory_key_chk_query($schregno[$i], $model->field["SUB_SDATE"]));
                $check3 = $db->getRow(knjh212Query::getDomitoryHistory_Dat_chk_query($schregno[$i], $model->field["SUB_SDATE"]));
                if (!$check1 && !is_array($check2) && !is_array($check3)) {
                    $data["SCHREGNO"][TEXT]    = $schregno[$i];
                    $data["DOMI_CD"][TEXT]     = $model->field["DOMI_CODEALL"];
                    $data["DOMI_ENTDAY"][TEXT] = str_replace("/","-",$model->field["SUB_SDATE"]);
                    $data["DOMI_OUTDAY"][TEXT] = "";
                    $data["REMARK"][TEXT]      = "";
                    $data["REGISTERCD"][TEXT]  = STAFFCD;
                    $data["UPDATED"][NUMBER]   = "sysdate()";

                    $query = Query::insertSQL($data, "SCHREG_DOMITORY_HIST_DAT");
                    $db->query($query);
                } else {
                    $cnt++;
                    continue;
                }
            }
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $cnt;
    }

    //入寮日取得
    function getSdate($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     DOMI_ENTDAY ";
        $query .= " FROM ";
        $query .= "     SCHREG_DOMITORY_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     DOMI_CD      = '".$model->field["DOMI_CODEALL"]."' AND ";
        $query .= "     DOMI_OUTDAY IS NULL ";

        return $query;
    }

    //--- UPDATE（退寮日）
    function &getSubUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $schregno = explode(',', $model->field["SUB_SCHREGNO"]);
        $edate = str_replace("/","-",$model->field["SUB_EDATE"]);
        $t_cnt = $f_cnt = 0;

        if ($model->field["SUB_SCHREGNO"]) {
            for ($i = 0; $i < get_count($schregno); $i++) {

                $sdate = $db->getCol(knjh212Query::getSdate($model, $schregno[$i]));

                for ($j = 0; $j < get_count($sdate); $j++) {
                    if($sdate[$j] <= $edate){

                        $data["DOMI_OUTDAY"][TEXT] = $edate;
                        $data["REGISTERCD"][TEXT]  = STAFFCD;
                        $data["UPDATED"][NUMBER]   = "sysdate()";

                        $where  = " WHERE SCHREGNO    = '" .$schregno[$i]."' ";
                        $where .= "   AND DOMI_CD     = '" .$model->field["DOMI_CODEALL"]."' ";
                        $where .= "   AND DOMI_ENTDAY = '" .$sdate[$j]."' ";
                        $query = Query::updateSQL($data, "SCHREG_DOMITORY_HIST_DAT", $where);
                        $db->query($query);
                    } else {
                        $f_cnt++;
                    }
                    $t_cnt++;
                }
            }
        }
        $db->commit();      // トランザクションをコミットする。

        if($f_cnt == 0){
            $judgement = "0";   //全て更新
        } else if($t_cnt == $f_cnt){
            $judgement = "1";   //全て失敗
        } else {
            $judgement = "2";   //一部更新
        }

        Query::dbCheckIn($db);
        return $judgement;
    }

    //年組取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "     AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //部活動取得
    function getClub($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CLUBM.CLUBCD AS VALUE, ";
        $query .= "     CLUBM.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_YDAT CLUBY ";
        $query .= "     INNER JOIN CLUB_MST CLUBM ON CLUBM.CLUBCD      = CLUBY.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                          AND CLUBM.SCHOOLCD    = CLUBY.SCHOOLCD ";
            $query .= "                          AND CLUBM.SCHOOL_KIND = CLUBY.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "         CLUBY.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND CLUBY.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND CLUBY.SCHOOL_KIND = '{$model->schkind}' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND CLUBY.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND CLUBY.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒
    function getStudent($model, $flg) {
        $schregno = implode("','",explode(',', $model->field["SUB_SCHREGNO"]));

        $query  = " SELECT ";
        $query .= "     T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || ':' || T2.SCHREGNO AS VALUE, ";
        $query .= "     T4.HR_NAME || ' ' || T2.ATTENDNO || '番 ' || T1.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T4 ";
        $query .= "          ON T2.YEAR     = T4.YEAR ";
        $query .= "         AND T2.SEMESTER = T4.SEMESTER ";
        $query .= "         AND T2.GRADE    = T4.GRADE ";
        $query .= "         AND T2.HR_CLASS = T4.HR_CLASS ";
        if ($model->Properties["useClubCombo"] == "1" && $model->field["CLUB"] != '') {
            $query .= "     ,SCHREG_CLUB_HIST_DAT CLUB ";
        }
        $query .= " WHERE ";
        $query .= "         T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["GRADE_HR_CLASS"] != '') {
            $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->field["GRADE_HR_CLASS"] == '' && $model->field["CLUB"] == '') {
            $query .= "     AND T2.GRADE || T2.HR_CLASS = '00000' ";
        }
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useClubCombo"] == "1" && $model->field["CLUB"] != '') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "     AND CLUB.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                $query .= "     AND CLUB.SCHOOL_KIND = '{$model->schkind}' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND CLUB.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                $query .= "     AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "     AND CLUB.SCHREGNO    = T2.SCHREGNO ";
            $query .= "     AND CLUB.CLUBCD      = '{$model->field["CLUB"]}' ";
            $query .= "     AND CLUB.SDATE      <= '".CTRL_DATE."' ";
        }
        if ($flg == "2") {
            $query .= "     AND T2.SCHREGNO IN ('".$schregno."') ";
        } else {
            $query .= "     AND T2.SCHREGNO NOT IN ('".$schregno."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒
    function getDomiStudent($model, $flg) {
        $schregno = implode("','",explode(',', $model->field["SUB_SCHREGNO"]));

        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || ':' || T1.SCHREGNO AS VALUE, ";
        $query .= "     T4.HR_NAME || ' ' || T2.ATTENDNO || '番 ' || T3.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_DOMITORY_HIST_DAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = T2.YEAR ";
        $query .= "                                     AND GDAT.GRADE = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                                 AND GDAT.SCHOOL_KIND = '{$model->schkind}' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                                 AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->Properties["useClubCombo"] == "1" && $model->field["CLUB"] != '') {
            $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON CLUB.SCHREGNO    = T2.SCHREGNO ";
            $query .= "                                         AND CLUB.CLUBCD      = '{$model->field["CLUB"]}' ";
            $query .= "                                         AND CLUB.SDATE      <= '".CTRL_DATE."' ";
            $query .= "                                         AND CLUB.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "                                         AND CLUB.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T4 ";
        $query .= "          ON T2.YEAR     = T4.YEAR ";
        $query .= "         AND T2.SEMESTER = T4.SEMESTER ";
        $query .= "         AND T2.GRADE    = T4.GRADE ";
        $query .= "         AND T2.HR_CLASS = T4.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "     T1.DOMI_CD   = '".$model->field["DOMI_CODEALL"]."' AND ";
        $query .= "     T1.DOMI_OUTDAY IS NULL ";
        if ($flg == "2") {
            $query .= "     AND T1.SCHREGNO IN ('".$schregno."') ";
        } else {
            $query .= "     AND T1.SCHREGNO NOT IN ('".$schregno."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
