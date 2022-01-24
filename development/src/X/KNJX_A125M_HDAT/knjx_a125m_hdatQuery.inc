<?php

require_once('for_php7.php');
class knjx_a125m_hdatquery extends Query
{

    //年度・学期取得
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

    //年組取得
    public function getSelectFieldSQL2($model, $flg = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE || T2.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        if ($flg) {
            $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON ";
            $query .= "             T3.YEAR     = T2.YEAR AND ";
            $query .= "             T3.SEMESTER = T2.SEMESTER AND ";
            $query .= "             T3.GRADE    = T2.GRADE AND ";
            $query .= "             T3.HR_CLASS = T2.HR_CLASS AND ";
            $query .= "             T3.SCHREGNO = '".$model->schregno."'";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T2.YEAR || T2.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.SCHOOL_KIND = 'H' ";
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

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data2["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            foreach ($model->fieldSize as $key => $val) {
                $data2[$key][TEXT]    = $data_arr[$i][$key];
            }
            $data2["REGISTERCD"][TEXT]       = STAFFCD;
            $data2["UPDATED"][NUMBER]        = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_HDAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data2, "HTRAINREMARK_HDAT");
            } else {
                $where  = "WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data2, "HTRAINREMARK_HDAT", $where);
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
            //奉仕の記録(HTRAINREMARK_HDAT) ※他のフィールドもあるため、NULLで更新
            if ($db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_HDAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'") > 0) {
                $data2["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                foreach ($model->fieldSize as $key => $val) {
                    $data2[$key][TEXT]    = '';
                }
                $data2["REGISTERCD"][TEXT]       = STAFFCD;
                $data2["UPDATED"][NUMBER]        = "SYSDATE()";
                
                $where  = "WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                
                $query = Query::updateSQL($data2, "HTRAINREMARK_HDAT", $where);
                $db->query($query);
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_DAT ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "w_csvmsg_dat");

        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = "select * from w_csvmsg_dat";

        return $query;
    }

    //年次取得
    public function getAnnual($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
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
        $query .= "     T1.SCHREGNO, ";
        $query .= "     ".$field." ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_HDAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T3.SCHREGNO = T1.SCHREGNO AND ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        }
        $query .= "     T1.GRADE IN (SELECT  ";
        $query .= "                     GRADE ";
        $query .= "                  FROM ";
        $query .= "                     SCHREG_REGD_GDAT G1 ";
        $query .= "                  WHERE ";
        $query .= "                     T1.YEAR = G1.YEAR AND G1.SCHOOL_KIND = 'H') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
