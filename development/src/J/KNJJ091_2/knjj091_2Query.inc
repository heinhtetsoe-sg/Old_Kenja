<?php

require_once('for_php7.php');

class knjj091_2Query extends Query
{
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
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     NAMECD1 = 'J004' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1      = '".STAFFCD."' ";
            $query .= "          OR TR_CD2      = '".STAFFCD."' ";
            $query .= "          OR TR_CD3      = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1   = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2   = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3   = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".$model->field["SCHKIND"]."' ";
            $query .= "                 ) ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getStudent($model, $flg)
    {
        $schregno = implode("','",explode(',', $model->field["SCHREGNO"]));

        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.ATTENDNO || '番 ' || T2.SCHREGNO || ' ' || T1.NAME_SHOW AS LABEL, ";
        $query .= "     T2.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        if($flg == "2"){
            $query .= "     AND T2.SCHREGNO IN ('".$schregno."') ";
        } else {
            $query .= "     AND T2.SCHREGNO NOT IN ('".$schregno."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

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


    //委員会取得
    function getCommitteeCd($model) {
        $query  = " SELECT ";
        $query .= "     T1.COMMITTEECD  AS VALUE, ";
        $query .= "     T1.COMMITTEECD || '　' || T2.COMMITTEENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COMMITTEE_YDAT T1, ";
        $query .= "     COMMITTEE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.COMMITTEE_FLG = T2.COMMITTEE_FLG AND ";
        $query .= "     T1.COMMITTEECD   = T2.COMMITTEECD AND ";
        $query .= "     T1.COMMITTEE_FLG = '{$model->field["COMMITTEE_FLG"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1)
    {
        $query  = " SELECT DISTINCT ";
        if ($namecd1 == "J002") {
            $query .= "     CAST(T1.NAMECD2 AS INT) AS VALUE, ";
        } else {
            $query .= "     T1.NAMECD2 AS VALUE, ";
        }
        $query .= "     T1.NAMECD2 || '　' || T2.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_YDAT T1, ";
        $query .= "     NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.NAMECD1 = '".$namecd1."' AND ";
        $query .= "     T1.NAMECD1 = T2.NAMECD1 AND ";
        $query .= "     T1.NAMECD2 = T2.NAMECD2 ";
        if ($namecd1 == "J002") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //追加処理
    function &InsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;
        $schregno = explode(',', $model->field["SCHREGNO"]);

        if($model->field["SCHREGNO"]){
            for ($i = 0; $i < get_count($schregno); $i++){

                //MAXSEQ取得
                $queryseq = knjj091_2Query::getMaxSeq($model);
                $maxseq = ($db->getOne($queryseq)) ? $db->getOne($queryseq)+1 : 1 ;

                //委員会在籍チェック
                $check = $db->getOne(knjj091_2Query::checkHistData($model, $schregno[$i]));

                if($check == 0){
                    //追加
                    if ($model->Properties["use_prg_schoolkind"] == "1") {
                        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                        $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHKIND"];
                    } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                        $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
                    }
                    $data["YEAR"][TEXT]             = CTRL_YEAR;
                    $data["SEQ"][NUMBER]            = $maxseq;
                    $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                    $data["SCHREGNO"][TEXT]         = $schregno[$i];
                    $data["COMMITTEE_FLG"][TEXT]    = $model->field["COMMITTEE_FLG"];
                    $data["COMMITTEECD"][TEXT]      = $model->field["COMMITTEECD"];
                    $data["EXECUTIVECD"][TEXT]      = $model->field["EXECUTIVECD"];
                    $data["GRADE"][TEXT]            = substr($model->field["GRADE_HR_CLASS"],0,2);
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";

                    $db->query(Query::insertSQL($data, "SCHREG_COMMITTEE_HIST_DAT"));
                    $cnt++;
                }
            }
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $cnt;
    }

    //MAXSEQ取得
    function getMaxSeq($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //委員会在籍チェック
    function checkHistData($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        if($model->field["SEMESTER"] == ""){
            $query .= "     SEMESTER IS NULL AND ";
        } else {
            $query .= "     SEMESTER  = '".$model->field["SEMESTER"]."' AND ";
        }
        if($model->field["COMMITTEE_FLG"] == ""){
            $query .= "     COMMITTEE_FLG IS NULL AND ";
        } else {
            $query .= "     COMMITTEE_FLG  = '".$model->field["COMMITTEE_FLG"]."' AND ";
        }
        if($model->field["COMMITTEECD"] == ""){
            $query .= "     COMMITTEECD IS NULL AND ";
        } else {
            $query .= "     COMMITTEECD = '".$model->field["COMMITTEECD"]."' AND ";
        }
        if($model->field["EXECUTIVECD"] == ""){
            $query .= "     EXECUTIVECD IS NULL ";
        } else {
            $query .= "     EXECUTIVECD = '".$model->field["EXECUTIVECD"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }
}
?>
