<?php

require_once('for_php7.php');
class knjx_g081query extends Query
{

    //年度学期一覧
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
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    //生徒一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.ATTENDNO || '番 ' || T2.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0; //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["CERTIF_KINDCD"][TEXT]    = '008';
            $data["PRINT_CNT"][NUMBER]      = $data_arr[$i]["PRINT_CNT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $db->getOne("SELECT COUNT(*) FROM CERTIF_SCH_PRINT_CNT_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND CERTIF_KINDCD = '008'")) {
                $query = Query::insertSQL($data, "CERTIF_SCH_PRINT_CNT_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     SCHREGNO        = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "     CERTIF_KINDCD   = '008' ";

                $query = Query::updateSQL($data, "CERTIF_SCH_PRINT_CNT_DAT", $where);
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

        $cnt = 0; //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ";
            $query .= "     CERTIF_SCH_PRINT_CNT_DAT ";
            $query .= "	WHERE ";
            $query .= "	    SCHREGNO        = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     CERTIF_KINDCD   = '008' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //年組一覧
    public function checkRegdDat($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     S2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     S1.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S3.PRINT_CNT, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ON T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT S2 ON T1.YEAR = S2.YEAR ";
        $query .= "                                  AND T1.SEMESTER = S2.SEMESTER ";
        $query .= "                                  AND T1.GRADE = S2.GRADE ";
        $query .= "                                  AND T1.HR_CLASS = S2.HR_CLASS ";
        $query .= "     LEFT JOIN CERTIF_SCH_PRINT_CNT_DAT S3 ON T1.SCHREGNO = S3.SCHREGNO ";
        $query .= "                                          AND S3.CERTIF_KINDCD = '008' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= " AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->field["SCHREGNO"] != "") {
            $query .= " AND T1.SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
