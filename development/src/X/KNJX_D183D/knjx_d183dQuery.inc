<?php

require_once('for_php7.php');
class knjx_d183dquery extends Query
{

    //年度一覧
    public function getSelectFieldSQL($model)
    {
        $query  = "SELECT DISTINCT T1.YEAR, T1.SEMESTER, T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->setSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR   = T3.YEAR ";
                $query .= "                                   AND T1.GRADE  = T3.GRADE ";
                $query .= "                                   AND T3.SCHOOL_KIND = '".$model->setSchoolKind."' ";
            } elseif ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR   = T3.YEAR ";
                $query .= "                                   AND T1.GRADE  = T3.GRADE ";
                $query .= "                                   AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR   = T3.YEAR ";
            $query .= "                                   AND T1.GRADE  = T3.GRADE ";
            $query .= "                                   AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT DISTINCT GRADE, HR_CLASS, HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->setSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".substr($model->field["YEAR"], 0, 4)."' AND ";
                $query .= "                     SCHOOL_KIND = '".$model->setSchoolKind."' ";
                $query .= "             ) ";
            } elseif ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".substr($model->field["YEAR"], 0, 4)."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "             ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".substr($model->field["YEAR"], 0, 4)."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "             ) ";
        }
        $query .= "ORDER BY GRADE, HR_CLASS";

        return $query;
    }

    //在籍チェック
    public function checkRegdDat($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$data["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$data["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$data["SCHREGNO"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->setSchoolKind) {
                $query .= " AND GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT S1 ";
                $query .= "                 WHERE ";
                $query .= "                     T1.YEAR     = S1.YEAR AND ";
                $query .= "                     SCHOOL_KIND = '".$model->setSchoolKind."' ";
                $query .= "                 ) ";
            } elseif ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT S1 ";
                $query .= "                 WHERE ";
                $query .= "                     T1.YEAR     = S1.YEAR AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.YEAR     = S1.YEAR AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data = array();
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["REMARK1"][TEXT]          = $data_arr[$i]["REMARK1"];
            $data["ATTENDREC_REMARK"][TEXT] = $data_arr[$i]["ATTENDREC_REMARK"];
            $data["COMMUNICATION"][TEXT]    = $data_arr[$i]["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $db->getOne("SELECT COUNT(*) FROM HREPORTREMARK_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            } else {
                $where  = "WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "      AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= "      AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            }
            $db->query($query);

            $data = array();
            $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]             = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
            $data["DIV"][TEXT]                  = '01';
            $data["CODE"][TEXT]                 = '01';
            $data["REMARK1"][TEXT]              = $data_arr[$i]["ZIRITU"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            if (1 > $db->getOne("SELECT COUNT(*) FROM HREPORTREMARK_DETAIL_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'"." AND DIV = '01'"." AND CODE = '01'")) {
                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            } else {
                $where  = "WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "      AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= "      AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                $where .= "      AND DIV      = '01' ";
                $where .= "      AND CODE     = '01' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM HREPORTREMARK_DAT ";
            $query .= " WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "       AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "       AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

            $db->query($query);

            $query  = " DELETE FROM HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "       AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "       AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "       AND DIV      = '01' ";
            $query .= "       AND CODE     = '01' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_DAT ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "w_csvmsg_dat");

        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = "select * from w_csvmsg_dat";

        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.YEAR, ";
        $query .= "     T3.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.ATTENDREC_REMARK,";
        $query .= "     T3.REMARK1,";
        $query .= "     T4.REMARK1 AS ZIRITU,";
        $query .= "     T3.COMMUNICATION, ";
        $query .= "     'DUMMY' AS DUMMY ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                 AND T3.SEMESTER = T1.SEMESTER AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT T4 ON T4.YEAR = T1.YEAR ";
        $query .= "                 AND T4.SEMESTER = T1.SEMESTER AND T4.SCHREGNO = T1.SCHREGNO  AND T4.DIV  = '01' AND T4.CODE = '01'";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->setSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".substr($model->field["YEAR"], 0, 4)."' AND ";
                $query .= "                     SCHOOL_KIND = '".$model->setSchoolKind."' ";
                $query .= "              ) ";
            } elseif ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".substr($model->field["YEAR"], 0, 4)."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "              ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".substr($model->field["YEAR"], 0, 4)."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "              ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //入力パターン取得
    public function selectNyuryokuPatternQuery($data)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1";
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$data["YEAR"]."'";
        $query .= "     AND SEQ = '012' ";

        return $query;
    }

    //入力パターン取得
    public function selectHandiCapQuery($schRegNo)
    {
        $query  = " SELECT ";
        $query .= "     HANDICAP";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schRegNo."'";

        return $query;
    }
}
