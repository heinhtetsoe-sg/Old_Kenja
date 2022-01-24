<?php

require_once('for_php7.php');
class knjx155csvquery extends Query
{

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //テストコンボ
    public function getTest($model)
    {
        $year     = substr($model->field["YEAR"], 0, 4);
        $semester = substr($model->field["YEAR"], -1);
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$year}' AND ";
        $query .= "     SEMESTER = '{$semester}' ";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT DISTINCT GRADE,HR_CLASS,HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "ORDER BY GRADE,HR_CLASS";

        return $query;
    }

    //生徒一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = "SELECT DISTINCT T1.SCHREGNO, T1.ATTENDNO, T2.NAME_SHOW ";
        $query .= "FROM SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "WHERE T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY T1.ATTENDNO";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            foreach ($model->fieldSize as $key => $val) {
                $testkindcd = substr($data_arr[$i]["TEST_CD"], 0, 2);
                $testitemcd = substr($data_arr[$i]["TEST_CD"], -2);

                //データセット
                $data = array();
                $data["YEAR"][TEXT]        = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]    = $data_arr[$i]["SEMESTER"];
                $data["SCHREGNO"][TEXT]    = $data_arr[$i]["SCHREGNO"];
                $data["TESTKINDCD"][TEXT]  = $testkindcd;
                $data["TESTITEMCD"][TEXT]  = $testitemcd;
                $data["REMARK_DIV"][TEXT]  = '4';
                $data[$key][TEXT]          = $data_arr[$i][$key];
                $data["REGISTERCD"][TEXT]  = STAFFCD;
                $data["UPDATED"][NUMBER]   = "SYSDATE()";

                $query  = " SELECT ";
                $query .= "     COUNT(*) ";
                $query .= " FROM ";
                $query .= "     HEXAM_RECORD_REMARK_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' AND ";
                $query .= "     SEMESTER = '{$data_arr[$i]["SEMESTER"]}' AND ";
                $query .= "     TESTKINDCD = '{$testkindcd}' AND ";
                $query .= "     TESTITEMCD = '{$testitemcd}' AND ";
                $query .= "     SCHREGNO   = '{$data_arr[$i]["SCHREGNO"]}' AND ";
                $query .= "     REMARK_DIV = '4' ";
                if (1 > $db->getOne($query)) {
                    $query = Query::insertSQL($data, "HEXAM_RECORD_REMARK_DAT");
                } else {
                    $where  = "WHERE YEAR       = '".$data_arr[$i]["YEAR"]."' ";
                    $where .= "  AND SEMESTER   = '".$data_arr[$i]["SEMESTER"]."' ";
                    $where .= "  AND SCHREGNO   = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $where .= "  AND TESTKINDCD = '{$testkindcd}' ";
                    $where .= "  AND TESTITEMCD = '{$testitemcd}' ";
                    $where .= "  AND REMARK_DIV = '4' ";
                    $query = Query::updateSQL($data, "HEXAM_RECORD_REMARK_DAT", $where);
                }
                $db->query($query);
            }
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
            foreach ($model->fieldSize as $key => $val) {
                //データセット
                $data = array();
                $data[$key][TEXT] = "";

                $testkindcd = substr($data_arr[$i]["TEST_CD"], 0, 2);
                $testitemcd = substr($data_arr[$i]["TEST_CD"], -2);

                $where  = "WHERE YEAR       = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "  AND SEMESTER   = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= "  AND SCHREGNO   = '".$data_arr[$i]["SCHREGNO"]."' ";
                $where .= "  AND TESTKINDCD = '{$testkindcd}' ";
                $where .= "  AND TESTITEMCD = '{$testitemcd}' ";
                $where .= "  AND REMARK_DIV = '4' ";

                $query = Query::updateSQL($data, "HEXAM_RECORD_REMARK_DAT", $where);
                $db->query($query);
            }
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
        $field = "";
        foreach ($model->fieldSize as $key => $val) {
            $field .= "L2.{$key},";
        }

        $year     = substr($model->field["YEAR"], 0, 4);
        $semester = substr($model->field["YEAR"], -1);

        $testkindcd = substr($model->field["TEST_CD"], 0, 2);
        $testitemcd = substr($model->field["TEST_CD"], -2);

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L2.TESTKINDCD || L2.TESTITEMCD AS TEST_CD, ";
        $query .= "     ".$field." ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     HEXAM_RECORD_REMARK_DAT L2 ON  L2.YEAR = T1.YEAR ";
        $query .= "                                AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                                AND L2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$semester}' AND ";
        if ($model->field["STUDENT"] != "") {
            $query .= "     T1.SCHREGNO = '{$model->field["STUDENT"]}' AND ";
        }
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     T1.GRADE || T1.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' AND ";
        }
        if ($model->field["TEST_CD"] != "") {
            $query .= "     L2.TESTKINDCD || L2.TESTITEMCD = '{$model->field["TEST_CD"]}' AND ";
        }
        $query .= "     L2.REMARK_DIV = '4' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     TEST_CD ";

        return $query;
    }
}
