<?php

require_once('for_php7.php');
class knjx152query extends Query
{
    public function getSecurityHigh($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '{$model->getPrgId}' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

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
            //データセット
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            foreach ($model->fieldSize as $key => $val) {
                $data[$key][TEXT]    = $data_arr[$i][$key];
            }
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
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $field = "";
        foreach ($model->fieldSize as $key => $val) {
            $field .= "T3.".$key.",";
        }

        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.YEAR, ";
        $query .= "     T3.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     ".$field." ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->field["STUDENT"] != "") {
            $query .= "     AND T1.SCHREGNO = '".$model->field["STUDENT"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}