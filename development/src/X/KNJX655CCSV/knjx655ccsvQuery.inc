<?php

require_once('for_php7.php');

class knjx655ccsvquery extends Query {
    //学期コンボ
    function getSemesterList() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($semester) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト一覧取得
    function getTestList($model, $flg="") {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || ' ' || TESTITEMNAME AS LABEL, ";
        $query .= "     SEMESTER_DETAIL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' ";
        if($flg){
            $query .= "    AND TESTKINDCD || TESTITEMCD = '".$model->field["TESTKINDCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model,&$data_arr) {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $testkindcd = substr($data_arr[$i]["TEST_CD"], 0, 2);
            $testitemcd = substr($data_arr[$i]["TEST_CD"], -2);

            //データセット
            $data = array();
            $data["YEAR"][TEXT]        = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]    = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]    = $data_arr[$i]["SCHREGNO"];
            $data["TESTKINDCD"][TEXT]  = $testkindcd;
            $data["TESTITEMCD"][TEXT]  = $testitemcd;
            $data["REMARK_DIV"][TEXT]  = '5';
            $data["REMARK1"][TEXT]      = $data_arr[$i]["REMARK1"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "SYSDATE()";

            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HEXAM_RECORD_REMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '{$data_arr[$i]["YEAR"]}' AND ";
            $query .= "     SEMESTER   = '{$data_arr[$i]["SEMESTER"]}' AND ";
            $query .= "     SCHREGNO   = '{$data_arr[$i]["SCHREGNO"]}' AND ";
            $query .= "     TESTKINDCD = '{$testkindcd}' AND ";
            $query .= "     TESTITEMCD = '{$testitemcd}' AND ";
            $query .= "     REMARK_DIV = '5' ";

            if (1 > $db->getOne($query)) {
                $query = Query::insertSQL($data, "HEXAM_RECORD_REMARK_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR       = '{$data_arr[$i]["YEAR"]}' AND ";
                $where .= "     SEMESTER   = '{$data_arr[$i]["SEMESTER"]}' AND ";
                $where .= "     SCHREGNO   = '{$data_arr[$i]["SCHREGNO"]}' AND ";
                $where .= "     TESTKINDCD = '{$testkindcd}' AND ";
                $where .= "     TESTITEMCD = '{$testitemcd}' AND ";
                $where .= "     REMARK_DIV = '5' ";

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
    function DeleteQueryCsv($model,&$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data = array();
            $data['REMARK1'][TEXT] = "";

            $testkindcd = substr($data_arr[$i]["TEST_CD"], 0, 2);
            $testitemcd = substr($data_arr[$i]["TEST_CD"], -2);

            $where  = " WHERE ";
            $where .= "     YEAR       = '{$data_arr[$i]["YEAR"]}' AND ";
            $where .= "     SEMESTER   = '{$data_arr[$i]["SEMESTER"]}' AND ";
            $where .= "     SCHREGNO   = '{$data_arr[$i]["SCHREGNO"]}' AND ";
            $where .= "     TESTKINDCD = '{$testkindcd}' AND ";
            $where .= "     TESTITEMCD = '{$testitemcd}' AND ";
            $where .= "     REMARK_DIV = '5' ";

            $query = Query::updateSQL($data, "HEXAM_RECORD_REMARK_DAT", $where);
            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = "KNJX_D655C";
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.ATTENDNO, ";
        $query .= "     L3.NAME_SHOW, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD AS TEST_CD, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     'DUMMY' AS DUMMY ";
        $query .= " FROM ";
        $query .= "     HEXAM_RECORD_REMARK_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON  L1.YEAR     = T1.YEAR ";
        $query .= "                        AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT L2 ON  L2.YEAR     = T1.YEAR ";
        $query .= "                         AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                         AND L2.GRADE    = L1.GRADE ";
        $query .= "                         AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L3 ON L3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        if ($model->field["GRADE_HR_CLASS"] != "999999") {
            $query .= "     L1.GRADE || L1.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' AND ";
        }
        if ($model->field["TEST_CD"] != "999999") {
            $query .= "     T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TEST_CD"]}' AND ";
        }
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "    (L2.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "     L2.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "     L2.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "     L2.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "     L2.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "     L2.SUBTR_CD1  = '".STAFFCD."') AND ";
        }
        $query .= "     T1.YEAR       = '{$year}' AND ";
        $query .= "     T1.SEMESTER   = '{$model->field['SEMESTER']}' AND ";
        $query .= "     T1.REMARK_DIV = '5' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.ATTENDNO, ";
        $query .= "     TEST_CD ";

        return $query;
    }
}
?>
