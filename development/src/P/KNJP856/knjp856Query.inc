<?php

require_once('for_php7.php');

class knjp856query extends Query
{
    //名称マスタ
    public function getNameMst($model, $nameCd1)
    {
        $query .= "   SELECT ";
        $query .= "       NAMECD2, ";
        $query .= "       NAME1 ";
        $query .= "   FROM ";
        $query .= "       V_NAME_MST ";
        $query .= "   WHERE ";
        $query .= "       YEAR = '{$model->year}' ";
        $query .= "       AND NAMECD1 = '{$nameCd1}' ";

        return $query;
    }

    //年組一覧
    public function getGradeHrClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     HDAT.GRADE || HDAT.HR_CLASS AS VALUE, ";
        $query .= "     HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = HDAT.YEAR ";
        $query .= "                                    AND GDAT.GRADE = HDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR     = '{$model->year}' ";
        $query .= "     AND HDAT.SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /****************/
    /** ＣＳＶ出力 **/
    /****************/
    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     CUST.CUSTOMER_NUMBER ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN COLLECT_SCHREG_CUSTOMER_DAT CUST ON CUST.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "                                    AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "                                    AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "                                    AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '{$model->year}' ";
        $query .= "     AND REGD.SEMESTER = '{$model->semester}' ";
        if ($model->field["GRADE_HR_CLASS"] != '') {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO ";

        return $query;
    }

    /****************/
    /** ＣＳＶ取込 **/
    /****************/

    //学籍チェック
    public function getSchExistsCnt($year, $schoolKind, $schregno)
    {
        $query  = " WITH MAX_SEMS AS ( ";
        $query .= "   SELECT ";
        $query .= "       YEAR, ";
        $query .= "       MAX(SEMESTER) AS SEMESTER, ";
        $query .= "       SCHREGNO ";
        $query .= "   FROM ";
        $query .= "       SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "           YEAR      = '{$year}' ";
        $query .= "       AND SCHREGNO  = '{$schregno}' ";
        $query .= "   GROUP BY ";
        $query .= "       YEAR, ";
        $query .= "       SCHREGNO ";
        $query .= " ) ";
        $query .= "  SELECT ";
        $query .= "      COUNT(*) ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT REGD ";
        $query .= "      INNER JOIN MAX_SEMS SEMS ";
        $query .= "           ON SEMS.YEAR      = REGD.YEAR ";
        $query .= "          AND SEMS.SEMESTER  = REGD.SEMESTER ";
        $query .= "          AND SEMS.SCHREGNO  = REGD.SCHREGNO ";
        $query .= "      LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "           ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "          AND GDAT.GRADE = REGD.GRADE ";
        $query .= "  WHERE ";
        $query .= "          REGD.YEAR         = '{$year}' ";
        $query .= "      AND REGD.SCHREGNO     = '{$schregno}' ";
        $query .= "      AND GDAT.SCHOOL_KIND  = '{$schoolKind}' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($db, $model, &$data_arr)
    {
        $data = array();

        $cnt = 0;   //処理件数
        foreach ($data_arr as $line => $data_arr_line) {

            //DELETE INSERT --REDUCTION_INCOME_DAT
            if ($model->field["INPUT_DIV"] == "1" || $model->field["INPUT_DIV"] == "2") {
                $incomeDiv = "INCOME_DIV_{$model->field["INPUT_DIV"]}";
            } else {
                $incomeDiv = "INCOME_DIV_ALL";
            }
            knjp856Query::updateReductionIncomeDat($db, $model, $data_arr_line, $incomeDiv);
            $cnt++;
        }

        return $cnt;
    }

    //存在チェックSQL雛形
    public function getReducExistsCnt($db, $where)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     REDUCTION_INCOME_DAT REDUC ";
        $query .= $where;

        return $query;
    }

    //更新 -- REDUCTION_INCOME_DAT
    public function updateReductionIncomeDat($db, $model, $data_arr_line, $inputDiv)
    {
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '{$model->schoolCd}' ";
        $where .= "     AND SCHOOL_KIND = '{$data_arr_line["SCHOOL_KIND"]}' ";
        $where .= "     AND YEAR        = '{$data_arr_line["YEAR"]}' ";
        $where .= "     AND SCHREGNO    = '{$data_arr_line["SCHREGNO"]}' ";

        $data = array();
        if ($inputDiv == "INCOME_DIV_1" || $inputDiv == "INCOME_DIV_ALL") {
            //前年度確定分
            $data["INCOME_1"][TEXT]    = $data_arr_line["INCOME_1"];
            $data["INCOME_1_1"][TEXT]  = $data_arr_line["INCOME_1_1"];
            $data["INCOME_1_2"][TEXT]  = $data_arr_line["INCOME_1_2"];
            $data["INCOME_1_3"][TEXT]  = $data_arr_line["INCOME_1_3"];
            $data["INCOME_1_4"][TEXT]  = $data_arr_line["INCOME_1_4"];
        }
        if ($inputDiv == "INCOME_DIV_2" || $inputDiv == "INCOME_DIV_ALL") {
            //今年度確定分
            $data["INCOME_2"][TEXT]    = $data_arr_line["INCOME_2"];
            $data["INCOME_2_1"][TEXT]  = $data_arr_line["INCOME_2_1"];
            $data["INCOME_2_2"][TEXT]  = $data_arr_line["INCOME_2_2"];
            $data["INCOME_2_3"][TEXT]  = $data_arr_line["INCOME_2_3"];
            $data["INCOME_2_4"][TEXT]  = $data_arr_line["INCOME_2_4"];
        }
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";

        if ($db->getOne(knjp856Query::getReducExistsCnt($model, $where)) > 0) {
            //UPDATE
            $query = Query::updateSQL($data, "REDUCTION_INCOME_DAT", $where);
        } else {
            $data["SCHOOLCD"][TEXT]    = $model->schoolCd;
            $data["SCHOOL_KIND"][TEXT] = $data_arr_line["SCHOOL_KIND"];
            $data["YEAR"][TEXT]        = $data_arr_line["YEAR"];
            $data["SCHREGNO"][TEXT]    = $data_arr_line["SCHREGNO"];

            //INSERT
            $query = Query::insertSQL($data, "REDUCTION_INCOME_DAT");
        }
        $db->query($query);
    }

    //前年度データがあるかチェック
    public function getLastYearReducDat($db, $model)
    {
        $where  = " WHERE ";
        $where .= "         REDUC.SCHOOLCD    = '{$model->schoolCd}' ";
        $where .= "     AND REDUC.YEAR        = '{$model->lastYear}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT REGD ";
        $query .= "             WHERE ";
        $query .= "                 REGD.YEAR         = '{$model->year}' ";
        $query .= "                 AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= "                 AND REGD.SCHREGNO = REDUC.SCHREGNO ";
        $query .= "     ) ";

        $query = knjp856Query::getReducExistsCnt($db, $where);
        return $query;
    }

    //前年度コピー用SQL取得
    public function getCopyQuery($model)
    {
        $query .= "   SELECT ";
        $query .= "       REDUC.SCHOOLCD, ";
        $query .= "       GDAT.SCHOOL_KIND, ";
        $query .= "       '{$model->year}' AS YEAR, ";
        $query .= "       REDUC.SCHREGNO, ";
        $query .= "       REDUC.INCOME_2 AS INCOME_1, ";
        $query .= "       REDUC.INCOME_2_1 AS INCOME_1_1,";
        $query .= "       REDUC.INCOME_2_2 AS INCOME_1_2, ";
        $query .= "       REDUC.INCOME_2_3 AS INCOME_1_3, ";
        $query .= "       REDUC.INCOME_2_4 AS INCOME_1_4";
        $query .= "   FROM ";
        $query .= "       REDUCTION_INCOME_DAT REDUC ";
        $query .= "       INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "              ON REGD.YEAR     = '{$model->year}' ";
        $query .= "             AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= "             AND REGD.SCHREGNO = REDUC.SCHREGNO ";
        $query .= "       INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "              ON GDAT.YEAR     = REGD.YEAR ";
        $query .= "             AND GDAT.GRADE    = REGD.GRADE ";
        $query .= "   WHERE ";
        $query .= "       REDUC.YEAR = '{$model->lastYear}' ";

        return $query;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/
    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }
}
