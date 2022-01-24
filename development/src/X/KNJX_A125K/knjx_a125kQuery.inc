<?php

require_once('for_php7.php');
class knjx_a125kquery extends Query
{

    //年度一覧
    public function getSelectFieldSQL($model, $flg = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        if ($flg) {
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
            $query .= "     T1.SEMESTER = '".$model->exp_semester."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model, $flg = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($flg) {
            $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T1.YEAR AND ";
            $query .= "                                      T3.SEMESTER = T1.SEMESTER AND ";
            $query .= "                                      T3.GRADE = T1.GRADE AND ";
            $query .= "                                      T3.HR_CLASS = T1.HR_CLASS AND ";
            $query .= "                                      T3.SCHREGNO = '".$model->schregno."'";
        } else {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND ";
            $query .= "                                       T2.GRADE = T1.GRADE AND ";
            $query .= "                                       T2.SCHOOL_KIND = 'K' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
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

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data = array();
            //データセット
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            foreach ($model->fieldSize as $key => $val) {
                $data[$key][TEXT]    = $data_arr[$i][$key];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            $count = $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_K_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'");
            if (1 > $count) {
                $query = Query::insertSQL($data, "HTRAINREMARK_K_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".$data_arr[$i]["YEAR"]."' AND ";
                $where .= "     SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HTRAINREMARK_K_DAT", $where);
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
            $query .= "     HTRAINREMARK_K_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     YEAR     = '".$data_arr[$i]["YEAR"]."' ";
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
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //存在チェック
    public function checkSchregRegdDat($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' AND ";
        $query .= "     GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";

        return $query;
    }

    //署名チェック
    public function getOpinionsWk($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     CHAGE_OPI_SEQ ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
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
        $query .= "     VALUE(T3.YEAR, '".substr($model->field["YEAR"], 0, 4)."') AS YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     ".$field." ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_K_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                                  AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= "     AND T1.GRADE IN (SELECT ";
        $query .= "                     G1.GRADE ";
        $query .= "                  FROM ";
        $query .= "                     SCHREG_REGD_GDAT G1 ";
        $query .= "                  WHERE ";
        $query .= "                     T1.YEAR = G1.YEAR AND ";
        $query .= "                     G1.SCHOOL_KIND = 'K' ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
