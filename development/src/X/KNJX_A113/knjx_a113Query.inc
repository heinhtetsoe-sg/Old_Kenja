<?php

require_once('for_php7.php');
class knjx_a113query extends Query
{
    //学年一覧コンボ
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode("','", explode(':', $model->selectSchoolKind))."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック（学籍番号）
    public function checkSchData($model, $year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "          ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$year."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //存在チェック（校納金コード）
    public function checkReductionDivCd($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD             = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND      = '".$data["SCHOOL_KIND"]."' ";
        $query .= "     AND T1.YEAR             = '".$data["YEAR"]."' ";
        $query .= "     AND T1.REDUCTION_DIV_CD = '".$data["SCHOLARSHIP"]."' ";

        return $query;
    }

    //重複チェック（特待生データ）
    public function checkScholarshipHistDat($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND      = '".$data["SCHOOL_KIND"]."' ";
        $query .= "     AND T1.SCHOLARSHIP   = '".$data["SCHOLARSHIP"]."' ";
        $query .= "     AND T1.SCHREGNO      = '".$data["SCHREGNO"]."' ";
        $query .= "     AND T1.FROM_DATE    != '".$data["FROM_DATE"]."' ";
        $query .= "     AND ( ";
        $query .= "            FROM_DATE BETWEEN '".$data["FROM_DATE"]."' AND '".$data["TO_DATE"]."' ";
        $query .= "         OR TO_DATE BETWEEN '".$data["FROM_DATE"]."' AND '".$data["TO_DATE"]."' ";
        $query .= "         OR '".$data["FROM_DATE"]."' BETWEEN FROM_DATE AND TO_DATE ";
        $query .= "     ) ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($year, $schregno)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR ";
        $query .= "     AND T1.YEAR     = '".$year."' ";
        $query .= "     AND T1.GRADE    = T2.GRADE ";
        $query .= "     AND T1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND T1.SEMESTER = (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                             T1.YEAR     = S1.YEAR ";
        $query .= "                         AND T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                     ) ";

        return $query;
    }

    //データの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT ";
            $query .= " WHERE ";
            $query .= "         SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND  = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
            $query .= "     AND SCHOLARSHIP  = '".$data_arr[$i]["SCHOLARSHIP"]."' ";
            $query .= "     AND SCHREGNO     = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND FROM_DATE    = '".$data_arr[$i]["FROM_DATE"]."' ";

            $dataCnt = $db->getOne($query);

            $data["TO_DATE"][TEXT]    = $data_arr[$i]["TO_DATE"];
            $data["REMARK"][TEXT]     = $data_arr[$i]["REMARK"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ";
                $where .= "         SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
                $where .= "     AND SCHOOL_KIND  = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                $where .= "     AND SCHOLARSHIP  = '".$data_arr[$i]["SCHOLARSHIP"]."' ";
                $where .= "     AND SCHREGNO     = '".$data_arr[$i]["SCHREGNO"]."' ";
                $where .= "     AND FROM_DATE    = '".$data_arr[$i]["FROM_DATE"]."' ";

                $query = Query::updateSQL($data, "SCHREG_SCHOLARSHIP_HIST_DAT", $where);
            } else {
                $data["SCHOOLCD"][TEXT]       = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]    = $data_arr[$i]["SCHOOL_KIND"];
                $data["SCHOLARSHIP"][TEXT]    = $data_arr[$i]["SCHOLARSHIP"];
                $data["SCHREGNO"][TEXT]       = $data_arr[$i]["SCHREGNO"];
                $data["FROM_DATE"][TEXT]      = $data_arr[$i]["FROM_DATE"];

                $query = Query::insertSQL($data, "SCHREG_SCHOLARSHIP_HIST_DAT");
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
            $query  = " DELETE FROM ";
            $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT ";
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
            $query .= "     AND SCHOLARSHIP = '".$data_arr[$i]["SCHOLARSHIP"]."' ";
            $query .= "     AND SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND FROM_DATE   = '".$data_arr[$i]["FROM_DATE"]."' ";

            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " WITH SCH_DATA AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.SCHOLARSHIP, ";
        $query .= "     T2.FROM_DATE, ";
        $query .= "     T2.TO_DATE, ";
        $query .= "     T2.REMARK ";
        $query .= " FROM ";
        $query .= "     SCH_DATA T1 ";
        $query .= "     LEFT JOIN SCHREG_SCHOLARSHIP_HIST_DAT T2 ";
        $query .= "          ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T2.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " WHERE ";
        $query .= "         T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.FROM_DATE ";

        return $query;
    }
}
