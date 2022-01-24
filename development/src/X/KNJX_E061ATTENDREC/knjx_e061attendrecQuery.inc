<?php

require_once('for_php7.php');
class knjx_e061attendrecQuery extends Query
{

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年一覧
    public function getSelectFieldSQL1($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".substr($model->field["YEAR"], 0, 4)."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    //生徒一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.ATTENDNO || '番 ' || T2.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER  = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            knjx_e061attendrecQuery::getDeleteQueryAttendrec($db, $data_arr, $i);
            knjx_e061attendrecQuery::getInsertQueryAttendrec($db, $data_arr, $i);

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

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            knjx_e061attendrecQuery::getDeleteQueryAttendrec($db, $data_arr, $i);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //追加SQL
    public function getInsertQueryAttendrec($db, $data_arr, $i)
    {
        $data = array();

        $data["SCHOOLCD"][TEXT]             = $data_arr[$i]["SCHOOLCD"];
        $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
        $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
        $data["ANNUAL"][TEXT]               = $data_arr[$i]["ANNUAL"];
        $data["SUMDATE"][TEXT]              = str_replace('/', '-', $data_arr[$i]["SUMDATE"]);
        $data["CLASSDAYS"][NUMBER]          = $data_arr[$i]["CLASSDAYS"];
        $data["OFFDAYS"][NUMBER]            = $data_arr[$i]["OFFDAYS"];
        $data["ABSENT"][NUMBER]             = $data_arr[$i]["ABSENT"];
        $data["SUSPEND"][NUMBER]            = $data_arr[$i]["SUSPEND"];
        $data["MOURNING"][NUMBER]           = $data_arr[$i]["MOURNING"];
        $data["ABROAD"][NUMBER]             = $data_arr[$i]["ABROAD"];
        $data["REQUIREPRESENT"][NUMBER]     = $data_arr[$i]["REQUIREPRESENT"];
        $data["SICK"][NUMBER]               = $data_arr[$i]["SICK"];
        $data["ACCIDENTNOTICE"][NUMBER]     = $data_arr[$i]["ACCIDENTNOTICE"];
        $data["NOACCIDENTNOTICE"][NUMBER]   = $data_arr[$i]["NOACCIDENTNOTICE"];
        $data["PRESENT"][NUMBER]            = $data_arr[$i]["PRESENT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHREG_ATTENDREC_DAT");
        $db->query($query);
    }

    //削除SQL
    public function getDeleteQueryAttendrec($db, $data_arr, $i)
    {
        $query  = " DELETE FROM SCHREG_ATTENDREC_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".$data_arr[$i]["SCHOOLCD"]."' ";
        $query .= "     AND YEAR        = '".$data_arr[$i]["YEAR"]."' ";
        $query .= "     AND SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";
        $db->query($query);
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = "KNJX_E061M";
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T3.SCHOOLCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.YEAR, ";
        $query .= "     T3.ANNUAL, ";
        $query .= "     T3.SUMDATE, ";
        $query .= "     T3.CLASSDAYS, ";
        $query .= "     T3.OFFDAYS, ";
        $query .= "     T3.ABSENT, ";
        $query .= "     T3.SUSPEND, ";
        $query .= "     T3.MOURNING, ";
        $query .= "     T3.ABROAD, ";
        $query .= "     T3.REQUIREPRESENT, ";
        $query .= "     T3.SICK, ";
        $query .= "     T3.ACCIDENTNOTICE, ";
        $query .= "     T3.NOACCIDENTNOTICE, ";
        $query .= "     T3.PRESENT, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_ATTENDREC_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                                       AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->field["STUDENT"] != "") {
            $query .= "     AND T1.SCHREGNO = '".$model->field["STUDENT"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
