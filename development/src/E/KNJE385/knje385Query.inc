<?php

require_once('for_php7.php');

class knje385query extends Query {

    //年度取得
    function getYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //NAME_MST取得
    function getNameMst($year) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = 'E056' ";
        $query .= "     AND NAMESPARE1 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック(学力点、加減点) -- SCHREG_REGD_DAT
    function checkSchregRegdDat($model, $data) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".$data["YEAR"]."' ";
        $query .= "     AND REGD.GRADE = '".sprintf("%02d", $data["GRADE"])."' ";
        $query .= "     AND REGD.HR_CLASS = '".sprintf("%03d", $data["HR_CLASS"])."' ";
        $query .= "     AND REGD.ATTENDNO = '".sprintf("%03d", $data["ATTENDNO"])."' ";

        return $query;
    }

    //学籍番号の取得
    function getSchregno($model, $data) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".$data["YEAR"]."' ";
        $query .= "     AND REGD.GRADE = '".sprintf("%02d", $data["GRADE"])."' ";
        $query .= "     AND REGD.HR_CLASS = '".sprintf("%03d", $data["HR_CLASS"])."' ";
        $query .= "     AND REGD.ATTENDNO = '".sprintf("%03d", $data["ATTENDNO"])."' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function updateQueryCsv($model, &$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //学籍番号取得
            $schregno = $db->getOne(knje385Query::getSchregno($model, $data_arr[$i]));

            //DELETE
            $query  = " DELETE FROM ".$model->updateTableName;
            $query .= "     WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SCHREGNO = '".$schregno."' ";
            if ($model->field["INPUT"] == "1") {
                $query .= "     AND TEST_DATE = '".$data_arr[$i]["TEST_DATE"]."' ";
            }
            $db->query($query);

            //INSERT
            $data = array();
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SCHREGNO"][TEXT]         = $schregno;
            if ($model->field["INPUT"] == "1") {
                $data["TEST_DATE"][TEXT]        = $data_arr[$i]["TEST_DATE"];
            }
            $data["GRADE"][TEXT]            = sprintf("%02d", $data_arr[$i]["GRADE"]);
            $data["HR_CLASS"][TEXT]         = sprintf("%03d", $data_arr[$i]["HR_CLASS"]);
            $data["ATTENDNO"][TEXT]         = sprintf("%03d", $data_arr[$i]["ATTENDNO"]);
            $data["SCORE"][NUMBER]          = $data_arr[$i]["SCORE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, $model->updateTableName);
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //存在
    function getExists($db, $model, $schregno, $data_arr) {
        $query  = " SELECT COUNT(*) FROM ".$model->updateTableName;
        $query .= "     WHERE YEAR = '".$data_arr["YEAR"]."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        if ($model->field["INPUT"] == "1") {
            $query .= "     AND TEST_DATE = '".$data_arr["TEST_DATE"]."' ";
        }
        return $db->getOne($query);
    }

    //マスタの削除（ＣＳＶデータより読込）
    function deleteQueryCsv($model, &$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //学籍番号取得
            $schregno = $db->getOne(knje385Query::getSchregno($model, $data_arr[$i]));

            $existsCnt = knje385Query::getExists($db, $model, $schregno, $data_arr[$i]);
            if ($existsCnt > 0) {
                //DELETE
                $query  = " DELETE FROM ".$model->updateTableName;
                $query .= "     WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $query .= "     AND SCHREGNO = '".$schregno."' ";
                if ($model->field["INPUT"] == "1") {
                    $query .= "     AND TEST_DATE = '".$data_arr[$i]["TEST_DATE"]."' ";
                }
                $db->query($query);
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }
}
?>
