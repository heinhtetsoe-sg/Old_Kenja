<?php

require_once('for_php7.php');
class knjx153cquery extends Query
{

    //学期取得
    public function getSemesterMst($year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度一覧取得
    public function getYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR || '年度 ' AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getHrClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        if ($model->field["OUT_SEMESTER"] == "9") {
            $query .= "     SEMESTER IN(SELECT  ";
            $query .= "                     MAX(SEMESTER) ";
            $query .= "                 FROM ";
            $query .= "                      SCHREG_REGD_HDAT ";
            $query .= "                 WHERE ";
            $query .= "                      YEAR = '".$model->field["YEAR"]."' AND ";
            $query .= "                      GRADE < '11') AND ";
        } else {
            $query .= "     SEMESTER = '".$model->field["OUT_SEMESTER"]."' AND ";
        }
        $query .= "     GRADE < '11' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]        = PROGRAMID;
        $data1["MSGROW"][NUMBER]         = $record_no;
        $data1["MSGREMARK"][TEXT]         = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $db->query($query);
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
        $query .= "     T3.REMARK1, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HEXAM_RECORD_REMARK_DAT T3 ON ";
        $query .= "          T3.YEAR = T1.YEAR AND ";
        if ($model->field["OUT_SEMESTER"] == "9") {
            $query .= "          T3.SEMESTER = '".$model->field["OUT_SEMESTER"]."' AND ";
        } else {
            $query .= "          T3.SEMESTER = T1.SEMESTER AND ";
        }
        $query .= "          T3.TESTKINDCD || T3.TESTITEMCD = '9900' AND ";
        $query .= "          T3.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "          T3.REMARK_DIV = '3' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' AND ";
        if ($model->field["OUT_SEMESTER"] == "9") {
            $query .= "     T1.SEMESTER IN(SELECT  ";
            $query .= "                     MAX(SEMESTER) ";
            $query .= "                 FROM ";
            $query .= "                      SCHREG_REGD_HDAT ";
            $query .= "                 WHERE ";
            $query .= "                      YEAR = '".$model->field["YEAR"]."' AND ";
            $query .= "                      GRADE < '11') ";
        } else {
            $query .= "     T1.SEMESTER = '".$model->field["OUT_SEMESTER"]."' ";
        }
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        } else {
            $query .= "     AND T1.GRADE < '11' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //データ有無
    public function getHexamRecRemarkCnt($param)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HEXAM_RECORD_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$param["YEAR"]."' AND ";
        $query .= "     SEMESTER = '".$param["SEMESTER"]."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '9900' AND ";
        $query .= "     SCHREGNO = '".$param["SCHREGNO"]."' AND ";
        $query .= "     REMARK_DIV = '3' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data = array();
            $data["REMARK1"][TEXT]          = $data_arr[$i]["REMARK1"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $db->getOne(knjx153cQuery::getHexamRecRemarkCnt($data_arr[$i]))) {
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
                $data["TESTKINDCD"][TEXT]       = '99';
                $data["TESTITEMCD"][TEXT]       = '00';
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["REMARK_DIV"][TEXT]       = '3';

                $query = Query::insertSQL($data, "HEXAM_RECORD_REMARK_DAT");
            } else {
                $where  = "WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND ";
                $where .= "      SEMESTER = '".$data_arr[$i]["SEMESTER"]."' AND ";
                $where .= "      TESTKINDCD || TESTITEMCD = '9900' AND ";
                $where .= "      SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "      REMARK_DIV = '3' ";

                $query = Query::updateSQL($data, "HEXAM_RECORD_REMARK_DAT", $where);
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

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ";
            $query .= "     HEXAM_RECORD_REMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     SEMESTER = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $query .= "     TESTKINDCD || TESTITEMCD = '9900' AND ";
            $query .= "     SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     REMARK_DIV = '3' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
}
