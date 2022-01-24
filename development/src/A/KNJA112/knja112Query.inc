<?php

require_once('for_php7.php');

class knja112Query extends Query {

    //学校種別取得
    function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     VALUE(NAMECD2, '') || ':' || VALUE(NAME1, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd1 == "A025") {
            $query .= "     AND NAMESPARE2 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //右クラスコンボ
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     T1.GRADE || ':' || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //左生徒リスト
    function getGhrStudents($model, $flg="") {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     B_DETAIL.BASE_REMARK1 AS HANDICAP, ";
        $query .= "     N1.NAME1 AS HANDICAP_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR = HDAT.YEAR ";
        $query .= "           AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "           AND REGD.GRADE = HDAT.GRADE ";
        $query .= "           AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST B_DETAIL ON REGD.SCHREGNO = B_DETAIL.SCHREGNO ";
        $query .= "          AND REGD.YEAR = B_DETAIL.YEAR ";
        $query .= "          AND B_DETAIL.BASE_SEQ = '004' ";
        $query .= "     INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'A025' ";
        $query .= "          AND B_DETAIL.BASE_REMARK1 = N1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND VALUE(B_DETAIL.BASE_REMARK1, '000') = '{$model->handicap}' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //右生徒リスト
    function getHrStudents($model) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     B_DETAIL.BASE_REMARK1 AS HANDICAP, ";
        $query .= "     N1.NAME1 AS HANDICAP_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR = HDAT.YEAR ";
        $query .= "           AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "           AND REGD.GRADE = HDAT.GRADE ";
        $query .= "           AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B_DETAIL ON REGD.SCHREGNO = B_DETAIL.SCHREGNO ";
        $query .= "          AND REGD.YEAR = B_DETAIL.YEAR ";
        $query .= "          AND B_DETAIL.BASE_SEQ = '004' ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A025' ";
        $query .= "          AND B_DETAIL.BASE_REMARK1 = N1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE || ':' || REGD.HR_CLASS = '{$model->hr_class}' ";
        $query .= "     AND VALUE(B_DETAIL.BASE_REMARK1, '000') <> '{$model->handicap}' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $clearSchArray = array();
        $clearSch = "";
        $clearSchSep = "";
        $taisyougaiSchArray = array();
        $taisyouSch = "";
        $taisyouSchSep = "";
        $selectArray = explode(",", $model->selectdata);
        foreach ($model->leftList as $key => $val) {
            if (!is_numeric(array_search($val, $selectArray))) {
                $clearSch .= $clearSchSep.$val;
                $clearSchSep = ",";
                $clearSchArray[$val] = $val;
            } else {
                $taisyougaiSchArray[$val] = $val;
            }
        }

        foreach ($selectArray as $key => $val) {
            if (!array_search($val, $clearSchArray) && !array_search($val, $taisyougaiSchArray)) {
                $taisyouSch .= $taisyouSchSep.$val;
                $taisyouSchSep = ",";
            }
        }

        if ($taisyouSch) {
            $query  = " DELETE FROM SCHREG_BASE_YEAR_DETAIL_MST ";
            $query .= " WHERE SCHREGNO  IN ('" .str_replace(",", "','", $taisyouSch). "') ";
            $query .= "   AND YEAR      ='".CTRL_YEAR."' ";
            $query .= "   AND BASE_SEQ  ='004' ";
            $db->query($query);

            $query  = " INSERT INTO SCHREG_BASE_YEAR_DETAIL_MST ( ";
            $query .= " SCHREGNO, ";
            $query .= " YEAR, ";
            $query .= " BASE_SEQ, ";
            $query .= " BASE_REMARK1, ";
            $query .= " REGISTERCD, ";
            $query .= " UPDATED ";
            $query .= " ) ";
            $query .= "   (SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     '".CTRL_YEAR."', ";
            $query .= "     '004', ";
            $query .= "     '{$model->handicap}', ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     SYSDATE() ";
            $query .= "   FROM ";
            $query .= "     SCHREG_BASE_MST ";
            $query .= "   WHERE ";
            $query .= "     SCHREGNO IN ('" .str_replace(",", "','", $taisyouSch). "') ";
            $query .= "   )  ";
            $db->query($query);
        }

        $data = array();
        $data["HANDICAP"][TEXT]     = $model->handicap;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE SCHREGNO  IN ('" .str_replace(",", "','", $taisyouSch). "') ";
        $query = Query::updateSQL($data, SCHREG_BASE_MST, $where);
        $db->query($query);

        $data = array();
        $data["BASE_REMARK1"][TEXT] = NULL;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE SCHREGNO  IN ('" .str_replace(",", "','", $clearSch). "') ";
        $where .= "   AND YEAR      ='".CTRL_YEAR."' ";
        $where .= "   AND BASE_SEQ  ='004' ";
        $query = Query::updateSQL($data, SCHREG_BASE_YEAR_DETAIL_MST, $where);
        $db->query($query);

        $data = array();
        $data["HANDICAP"][TEXT]     = NULL;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE SCHREGNO  IN ('" .str_replace(",", "','", $clearSch). "') ";
        $query = Query::updateSQL($data, SCHREG_BASE_MST, $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //コピー対象データ取得
    function getCopyData($model) {
        $pre_year   = CTRL_YEAR - 1;
        $this_year  = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.BASE_REMARK1, ";
        $query .= "     L1.SCHREGNO AS SCHNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_YEAR_DETAIL_MST T1 ";
        $query .= "     INNER JOIN V_NAME_MST T2 ";
        $query .= "          ON T2.YEAR         = '".$this_year."' ";
        $query .= "         AND T2.NAMECD1      = 'A025' ";
        $query .= "         AND T2.NAMECD2      = T1.BASE_REMARK1 ";
        $query .= "         AND T2.NAMESPARE2   = '1' ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L1 ";
        $query .= "          ON T1.SCHREGNO     = L1.SCHREGNO ";
        $query .= "         AND T1.BASE_SEQ     = L1.BASE_SEQ ";
        $query .= "         AND L1.YEAR         = '".$this_year."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$pre_year."' AND ";
        $query .= "     T1.BASE_SEQ = '004' AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT E1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         INNER JOIN SCHREG_REGD_GDAT E2 ";
                $query .= "              ON E1.YEAR     = E2.YEAR ";
                $query .= "             AND E1.GRADE    = E2.GRADE ";
                $query .= "             AND E2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             INNER JOIN SCHREG_REGD_GDAT E2 ";
            $query .= "                  ON E1.YEAR         = E2.YEAR ";
            $query .= "                 AND E1.GRADE        = E2.GRADE ";
            $query .= "                 AND E2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "             WHERE ";
        $query .= "                 E1.YEAR     = '".$this_year."' AND ";
        $query .= "                 E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "             ) AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_BASE_YEAR_DETAIL_MST E1 ";
        $query .= "                 WHERE ";
        $query .= "                     E1.YEAR     = '".$this_year."' AND ";
        $query .= "                     E1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                     E1.BASE_SEQ = '004' AND ";
        $query .= "                     VALUE(E1.BASE_REMARK1,'') != '' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //前年度コピー処理
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //前年度データ取得
        $query = knja112Query::getCopyData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["BASE_REMARK1"][TEXT] = $row["BASE_REMARK1"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            //更新
            if (strlen($row["SCHNO"])) {
                $where  = " WHERE ";
                $where .= "     SCHREGNO    = '".$row["SCHREGNO"]."' AND ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     BASE_SEQ    = '004' ";
                $query = Query::updateSQL($data, SCHREG_BASE_YEAR_DETAIL_MST, $where);
                $db->query($query);

            //追加
            } else {
                $data["SCHREGNO"][TEXT]     = $row["SCHREGNO"];
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["BASE_SEQ"][TEXT]     = '004';
                $query = Query::insertSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST");
                $db->query($query);
            }

            $data = array();
            $data["HANDICAP"][TEXT]     = $row["BASE_REMARK1"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $where  = " WHERE SCHREGNO = '".$row["SCHREGNO"]."' ";
            $query = Query::updateSQL($data, SCHREG_BASE_MST, $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
