<?php

require_once('for_php7.php');


class knji091Query extends Query {

    //卒業年度取得
    function selectYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.year ";
        $query .= " FROM ";
        $query .= "     schreg_regd_hdat T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT G1  ";
                $query .= "              ON G1.YEAR         = T1.YEAR ";
                $query .= "             AND G1.GRADE        = T1.GRADE ";
                $query .= "             AND G1.SCHOOL_KIND  IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1  ";
            $query .= "              ON G1.YEAR         = T1.YEAR ";
            $query .= "             AND G1.GRADE        = T1.GRADE ";
            $query .= "             AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.year desc";

        return $query;
    }

    //校種
    function getA023($model) {
        $query .= " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学期制・学年数上限取得
    function selectGradeSemesterDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     grade_hval, ";
        $query .= "     semesterdiv ";
        $query .= " FROM ";
        $query .= "     school_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".$model->field["YEAR"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }

        return $query;
    }

    //中高一貫取得
    function getNameMst()
    {
        $query = " SELECT NAMESPARE2 AS VAL FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";

        return $query;
    }

    //年組取得
    function getAuth($model, $opt_Grade)
    {
        $query  = "SELECT ";
        $query .= "    T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "    T1.HR_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1  ";
            $query .= "              ON G1.YEAR         = T1.YEAR ";
            $query .= "             AND G1.GRADE        = T1.GRADE ";
            $query .= "             AND G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "    T1.SEMESTER = '".$model->field["GAKKI"]."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.GRADE || T1.HR_CLASS ";

        return $query;
    }


    //生徒一覧を取得
    function selectSchregno($model)
    {
        $query  = "SELECT ";
        $query .= "    w3.schregno schregno2, ";
        $query .= "    w1.schregno, ";
        $query .= "    w1.attendno, ";
        $query .= "    w2.name_show ";
        $query .= "FROM ";
        $query .= "    schreg_base_mst w2, ";
        $query .= "    schreg_regd_dat w1 ";
        $query .= "    left join kin_grd_ledger_setup_dat w3 on w3.schregno=w1.schregno ";
        $query .= "WHERE ";
        $query .= "    w1.year='".$model->field["YEAR"]."' AND ";
        $query .= "    w1.semester='".$model->field["GAKKI"]."' AND ";
        $query .= "    w1.grade || w1.hr_class ='".$model->field["GR_CL"]."' AND ";
        $query .= "    w1.schregno=w2.schregno ";
        $query .= "ORDER BY ";
        $query .= "    w1.attendno ";

        return $query;
    }


    //KIN_GRD_LEDGER_SETUP_DAT(卒業生台帳設定データ)をDELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //左リストの学籍番号でKIN_GRD_LEDGER_SETUP_DATを削除(DELETE)
        if ($model->leftlistschno != "") {
            $array = explode(",", $model->leftlistschno);
            for ($i = 0; $i < get_count($array); $i++) {
                $query  = " DELETE FROM KIN_GRD_LEDGER_SETUP_DAT WHERE SCHREGNO = '".$array[$i]."' ";
                $db->query($query);
            }
        }
        //右リストの学籍番号でKIN_GRD_LEDGER_SETUP_DATを削除(DELETE)
        if ($model->rightlistschno != "") {
            $array = explode(",", $model->rightlistschno);
            for ($i = 0; $i < get_count($array); $i++) {
                $query  = " DELETE FROM KIN_GRD_LEDGER_SETUP_DAT WHERE SCHREGNO = '".$array[$i]."' ";
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return $result;
    }
    //KIN_GRD_LEDGER_SETUP_DAT(卒業生台帳設定データ)をINSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //左リストの学籍番号でKIN_GRD_LEDGER_SETUP_DATを追加(INSERT)
        if ($model->leftlistschno != "") {
            $array = explode(",", $model->leftlistschno);
            for ($i = 0; $i < get_count($array); $i++) {
                $data["SCHREGNO"][TEXT]     = $array[$i];
                $data["BIRTHDAY_FLG"][TEXT] = "1";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $db->query(Query::insertSQL($data, "KIN_GRD_LEDGER_SETUP_DAT"));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1 ";
        // $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        // } else {
        //     $query .= " AND SCHOOL_KIND IN (SELECT ";
        //     $query .= "                         SCHOOL_KIND ";
        //     $query .= "                     FROM ";
        //     $query .= "                         SCHREG_REGD_GDAT ";
        //     $query .= "                     WHERE ";
        //     $query .= "                         YEAR    = '".$model->field["YEAR"]."' AND ";
        //     $query .= "                         GRADE   = '". substr($model->field["GR_CL"],0,2)."' ";
        //     $query .= "                     ) ";
        }
        // $query .= " ORDER BY ";
        // $query .= "     SCHOOLCD ";

        return $query;
    }
}
?>
