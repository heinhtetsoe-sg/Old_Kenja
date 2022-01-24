<?php

require_once('for_php7.php');
class knjx153query extends Query
{

    //学期
    public function getSemesterMst()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //テスト取得
    public function getTest($year_seme, $model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $model->testTable ;
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'";
        $query .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (TR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "          TR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "          TR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    public function getExistsQuery($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".$data["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$data["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS AND ";
        $query .= "     T1.SCHREGNO = '".$data["SCHREGNO"]."' ";
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T2.TR_CD1 = '" .STAFFCD ."' OR";
            $query .= "          T2.TR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "          T2.TR_CD3 = '" .STAFFCD ."') ";
        }

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
            $data["TESTKINDCD"][TEXT]       = $data_arr[$i]["TESTKINDCD"];
            $data["TESTITEMCD"][TEXT]       = $data_arr[$i]["TESTITEMCD"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["REMARK_DIV"][TEXT]       = '2';
            $data["REMARK1"][TEXT]          = $data_arr[$i]["REMARK1"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $db->getOne("SELECT COUNT(*) FROM HEXAM_RECORD_REMARK_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND REMARK_DIV='2'")) {
                $query = Query::insertSQL($data, "HEXAM_RECORD_REMARK_DAT");
            } else {
                $where  = "WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND ";
                $where .= "      SEMESTER = '".$data_arr[$i]["SEMESTER"]."' AND ";
                $where .= "      TESTKINDCD = '".$data_arr[$i]["TESTKINDCD"]."' AND ";
                $where .= "      TESTITEMCD = '".$data_arr[$i]["TESTITEMCD"]."' AND ";
                $where .= "      SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "      REMARK_DIV = '2' ";

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

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data_check  = " SELECT COUNT(*) FROM HEXAM_RECORD_REMARK_DAT ";
            $data_check .= " WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND ";
            $data_check .= "       SEMESTER = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $data_check .= "       TESTKINDCD = '".$data_arr[$i]["TESTKINDCD"]."' AND ";
            $data_check .= "       TESTITEMCD = '".$data_arr[$i]["TESTITEMCD"]."' AND ";
            $data_check .= "       SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $data_check .= "       REMARK_DIV = '2' ";

            if (1 > $db->getOne($data_check)) {
                $query  = " DELETE FROM HEXAM_RECORD_REMARK_DAT ";
                $query .= " WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND ";
                $query .= "       SEMESTER = '".$data_arr[$i]["SEMESTER"]."' AND ";
                $query .= "       TESTKINDCD = '".$data_arr[$i]["TESTKINDCD"]."' AND ";
                $query .= "       TESTITEMCD = '".$data_arr[$i]["TESTITEMCD"]."' AND ";
                $query .= "       SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $query .= "       REMARK_DIV = '2' ";
            } else {
                $data["REMARK1"][TEXT]   = "";
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $where  = " WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND ";
                $where .= "       SEMESTER = '".$data_arr[$i]["SEMESTER"]."' AND ";
                $where .= "       TESTKINDCD = '".$data_arr[$i]["TESTKINDCD"]."' AND ";
                $where .= "       TESTITEMCD = '".$data_arr[$i]["TESTITEMCD"]."' AND ";
                $where .= "       SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "       REMARK_DIV = '2' ";

                $query = Query::updateSQL($data, "HEXAM_RECORD_REMARK_DAT", $where);
            }
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
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.YEAR, ";
        $query .= "     T3.SEMESTER, ";
        $query .= "     T3.TESTKINDCD, ";
        $query .= "     T3.TESTITEMCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.REMARK1, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HEXAM_RECORD_REMARK_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                 AND T3.SEMESTER = '".$model->field["SEMESTER"]."' AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                 AND T3.TESTKINDCD || T3.TESTITEMCD = '".$model->field["TESTKINDCD"]."' ";
        $query .= "                 AND T3.REMARK_DIV = '2' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
