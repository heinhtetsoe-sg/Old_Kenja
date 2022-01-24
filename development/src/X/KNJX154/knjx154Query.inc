<?php

require_once('for_php7.php');
class knjx154query extends Query
{

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "SELECT DISTINCT T1.YEAR, T1.SEMESTER, T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT DISTINCT GRADE, HR_CLASS, HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "ORDER BY GRADE, HR_CLASS";

        return $query;
    }

    //学期取得
    public function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR     = '".CTRL_YEAR."' ";
        $query .= "AND SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

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
            $data["SEMESTER"][TEXT]         = '9';
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["SPECIALACTREMARK"][TEXT] = $data_arr[$i]["SPECIALACTREMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $db->getOne("SELECT COUNT(*) FROM HREPORTREMARK_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SEMESTER = '9' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            } else {
                $where  = "WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "      AND SEMESTER = '9' ";
                $where .= "      AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            }
            $db->query($query);
            
            
            //通信欄
            $data2["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data2["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data2["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data2["COMMUNICATION"][TEXT]    = $data_arr[$i]["COMMUNICATION"];
            $data2["REGISTERCD"][TEXT]       = STAFFCD;
            $data2["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $db->getOne("SELECT COUNT(*) FROM HREPORTREMARK_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data2, "HREPORTREMARK_DAT");
            } else {
                $where  = "WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "      AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= "      AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data2, "HREPORTREMARK_DAT", $where);
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
            $query .= "       AND SEMESTER IN ('9', '".$data_arr[$i]["SEMESTER"]."') ";
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
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     VALUE(T3.YEAR, T1.YEAR) AS YEAR, ";
        $query .= "     VALUE(T4.SEMESTER, '".$model->field["SEMESTER"]."') AS SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.SPECIALACTREMARK, ";
        $query .= "     T4.COMMUNICATION, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                 AND T3.SEMESTER = '9' AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T4 ON T4.YEAR = T1.YEAR ";
        $query .= "                 AND T4.SEMESTER = '".$model->field["SEMESTER"]."' AND T4.SCHREGNO = T1.SCHREGNO ";
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