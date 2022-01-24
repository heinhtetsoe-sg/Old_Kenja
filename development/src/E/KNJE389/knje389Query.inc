<?php

require_once('for_php7.php');

class knje389query extends Query {
    //年度&学期取得
    function getYearSmester() {
                $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T1.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND T1.SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGrade($model) {
        $year_semester = explode('-', $model->field["YEAR_SEMESTER"]);

        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year_semester[0]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //件数取得
    function getDataCount($table, $year, $schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr) {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //AFT_TOTAL_STUDY_BATCH_DAT
            $data = array();
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["CLASS_SCORE"][TEXT]      = $data_arr[$i]["CLASS_SCORE"];
            $data["ABILITY_SCORE"][TEXT]    = $data_arr[$i]["ABILITY_SCORE"];
            $data["TOEFL_SCORE"][TEXT]      = $data_arr[$i]["TOEFL_SCORE"];
            $data["QUALIFIED_SCORE"][TEXT]  = $data_arr[$i]["QUALIFIED_SCORE"];
            $data["ADJUSTMENT_SCORE"][TEXT] = $data_arr[$i]["ADJUSTMENT_SCORE"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "SYSDATE()";

            $dataCnt = $db->getOne(knje389Query::getDataCount("AFT_TOTAL_STUDY_BATCH_DAT", $data_arr[$i]["YEAR"], $data_arr[$i]["SCHREGNO"]));
            if ($dataCnt > 0) {
                //更新
                $where  = " WHERE ";
                $where .= "     YEAR     = '{$data_arr[$i]["YEAR"]}' ";
                $where .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                $query = Query::updateSQL($data, "AFT_TOTAL_STUDY_BATCH_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "AFT_TOTAL_STUDY_BATCH_DAT");
            }
            $db->query($query);

            //AFT_TOTAL_STUDY_ADJUSTMENT_DAT
            $data = array();
            $data["YEAR"][TEXT]        = $data_arr[$i]["YEAR"];
            $data["SCHREGNO"][TEXT]    = $data_arr[$i]["SCHREGNO"];
            $data["SCORE"][TEXT]       = $data_arr[$i]["ADJUSTMENT_SCORE"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "SYSDATE()";

            $dataCnt = $db->getOne(knje389Query::getDataCount("AFT_TOTAL_STUDY_ADJUSTMENT_DAT", $data_arr[$i]["YEAR"], $data_arr[$i]["SCHREGNO"]));
            if ($dataCnt > 0) {
                //更新
                $where  = " WHERE ";
                $where .= "     YEAR     = '{$data_arr[$i]["YEAR"]}' ";
                $where .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                $query = Query::updateSQL($data, "AFT_TOTAL_STUDY_ADJUSTMENT_DAT", $where);
            } else {
                //追加
                $result = $db->query(knje389Query::getSchregInfo($data_arr[$i]["SCHREGNO"], $data_arr[$i]["YEAR"], $data_arr[$i]["SEMESTER"]));
                $schInfo = $result->fetchRow(DB_FETCHMODE_ASSOC);

                if (!$schInfo) continue;

                $data["GRADE"][TEXT]       = $schInfo["GRADE"];
                $data["HR_CLASS"][TEXT]    = $schInfo["HR_CLASS"];
                $data["ATTENDNO"][TEXT]    = $schInfo["ATTENDNO"];

                $query = Query::insertSQL($data, "AFT_TOTAL_STUDY_ADJUSTMENT_DAT");
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //CSV取込時に必要な情報をSCHREG_REGD_DATから取得
    function getSchregInfo($schregno, $year, $semester) {
        $query  = "    SELECT ";
        $query .= "      SCHREGNO, ";
        $query .= "      YEAR, ";
        $query .= "      SEMESTER, ";
        $query .= "      GRADE, ";
        $query .= "      HR_CLASS, ";
        $query .= "      ATTENDNO ";
        $query .= "    FROM ";
        $query .= "      SCHREG_REGD_DAT ";
        $query .= "    WHERE ";
        $query .= "      SCHREGNO       = '{$schregno}' ";
        $query .= "      AND YEAR       = '{$year}' ";
        $query .= "      AND SEMESTER   = '{$semester}' ";

        return $query;

    }

    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model, &$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data = array();
            $query  = " DELETE FROM ";
            $query .= "     AFT_TOTAL_STUDY_BATCH_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data_arr[$i]["YEAR"]}' AND ";
            $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";

            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $year = CTRL_YEAR;

        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(CLASS_SCORE,0) + VALUE(ABILITY_SCORE,0) ";
        $query .= "   + VALUE(TOEFL_SCORE,0) + VALUE(QUALIFIED_SCORE,0) ";
        $query .= "   + VALUE(ADJUSTMENT_SCORE,0) AS TOTAL_SCORE, ";
        $query .= "     ATSBD.CLASS_SCORE, ";
        $query .= "     ATSBD.ABILITY_SCORE, ";
        $query .= "     ATSBD.TOEFL_SCORE, ";
        $query .= "     ATSBD.QUALIFIED_SCORE, ";
        $query .= "     ATSBD.ADJUSTMENT_SCORE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN AFT_TOTAL_STUDY_BATCH_DAT ATSBD ";
        $query .= "             ON ATSBD.SCHREGNO = T1.SCHREGNO ";
        $query .= "            AND ATSBD.YEAR     = T1.YEAR ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "             ON HDAT.YEAR     = T1.YEAR ";
        $query .= "            AND HDAT.SEMESTER = T1.SEMESTER ";
        $query .= "            AND HDAT.GRADE    = T1.GRADE ";
        $query .= "            AND HDAT.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "             ON BASE.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '{$model->field["YEAR_SEMESTER"]}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        if($model->field["SORT"] == '1'){
            $query .= "     TOTAL_SCORE DESC, ";
        }
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
?>
