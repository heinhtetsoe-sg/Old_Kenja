<?php

require_once('for_php7.php');
class knjx_d138jquery extends Query
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

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->schoolkind) {
            $query .= "     AND T1.GRADE IN(SELECT ";
            $query .= "                         GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = SUBSTR('".$model->field["YEAR"]."',1,4) AND ";
            $query .= "                         SCHOOL_KIND = '".$model->schoolkind."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    //学校種別チェック
    public function checkSchoolKind($model, &$data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".$data["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = '".$data["SEMESTER"]."' AND ";
        $query .= "     T1.SCHREGNO = '".$data["SCHREGNO"]."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T2.SCHOOL_KIND = '".$model->schoolkind."' ";

        return $query;
    }

    //学期チェック
    public function checkSemester($year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            /**********/
            /* 学  期 */
            /**********/
            $data = array();
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
            $dataCnt = $db->getOne($query);

            //データセット
            foreach ($model->fieldSize as $key => $val) {
                $data[$key][TEXT]    = $data_arr[$i][$key];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $dataCnt) {
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            }
            $db->query($query);

            /**********/
            /* 通  年 */
            /**********/
            $data2 = array();
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
            $dataCnt = $db->getOne($query);

            //データセット
            foreach ($model->fieldSize2 as $key => $val) {
                if ($key == "TOTALSTUDYTIME") {
                    $data2[$key][TEXT]  = $data_arr[$i][$key];
                }
            }
            $data2["REGISTERCD"][TEXT]      = STAFFCD;
            $data2["UPDATED"][NUMBER]       = "SYSDATE()";

            if (1 > $dataCnt) {
                $data2["YEAR"][TEXT]            = $data_arr[$i]["YEAR"];
                $data2["SEMESTER"][TEXT]        = '9';
                $data2["SCHREGNO"][TEXT]        = $data_arr[$i]["SCHREGNO"];
                $query = Query::insertSQL($data2, "HREPORTREMARK_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "     AND SEMESTER = '9' ";
                $where .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data2, "HREPORTREMARK_DAT", $where);
            }
            $db->query($query);

            /****************************/
            /* HREPORTREMARK_DETAIL_DAT */
            /****************************/
            //削除
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND (DIV = '01' OR (DIV = '03' AND CODE IN ('03', '04'))) ";

            $db->query($query);

            //観点・評価追加
            foreach ($model->fieldSize2 as $key => $val) {
                if ($key != "TOTALSTUDYTIME") {
                    $code = preg_split('/_/', $key);
                    $data = array();
                    $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
                    $data["SEMESTER"][TEXT]     = '9';
                    $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
                    $data["DIV"][TEXT]          = "03";
                    $data["CODE"][TEXT]         = sprintf("%02d", $code[1]);
                    $data["REMARK1"][TEXT]      = $data_arr[$i][$key];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
                }
            }

            //特別活動等の記録追加
            foreach ($model->fieldSize3 as $key => $val) {
                $data = array();
                $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]     = '9';
                $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
                $data["DIV"][TEXT]          = "01";
                $data["CODE"][TEXT]         = sprintf("%02d", substr($key, 6));
                $data["REMARK1"][TEXT]      = $data_arr[$i][$key];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
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

            /**********/
            /* 学  期 */
            /**********/
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);

            /**********/
            /* 通  年 */
            /**********/
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);

            /****************************/
            /* HREPORTREMARK_DETAIL_DAT */
            /****************************/
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND (DIV = '01' OR (DIV = '03' AND CODE IN ('03', '04'))) ";
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_DAT ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_DAT");
        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query = " SELECT * FROM W_CSVMSG_DAT ";
        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $field = $field2 = $field3 = "";
        foreach ($model->fieldSize as $key => $val) {
            $field .= "T3.".$key.",";
        }
        foreach ($model->fieldSize2 as $key => $val) {
            if ($key == "TOTALSTUDYTIME") {
                $field2 .= "T4.".$key.",";
            } else {
                $code = preg_split('/_/', $key);
                $field2 .= "L{$code[1]}.REMARK1 AS {$key},";
            }
        }
        foreach ($model->fieldSize3 as $key => $val) {
            $code = sprintf("%02d", substr($key, 6));
            $field3 .= "D{$code}.REMARK1 AS {$key},";
        }

        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     ".$field2." ";
        $query .= "     ".$field3." ";
        $query .= "     ".$field." ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "          AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T4 ON T4.YEAR = T1.YEAR ";
        $query .= "          AND T4.SEMESTER = '9' ";
        $query .= "          AND T4.SCHREGNO = T1.SCHREGNO ";
        foreach ($model->fieldSize3 as $key => $val) {
            $code = sprintf("%02d", substr($key, 6));
            $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D{$code} ON D{$code}.YEAR = T1.YEAR ";
            $query .= "          AND D{$code}.SEMESTER = '9' ";
            $query .= "          AND D{$code}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND D{$code}.DIV = '01' ";
            $query .= "          AND D{$code}.CODE = '".$code."' ";
        }
        foreach ($model->fieldSize2 as $key => $val) {
            $code = preg_split('/_/', $key);
            $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT L{$code[1]} ON L{$code[1]}.YEAR = T1.YEAR ";
            $query .= "          AND L{$code[1]}.SEMESTER = '9' ";
            $query .= "          AND L{$code[1]}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND L{$code[1]}.DIV = '03' ";
            $query .= "          AND L{$code[1]}.CODE = '".$code[1]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->schoolkind) {
            $query .= "     AND T1.GRADE IN(SELECT ";
            $query .= "                         GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = SUBSTR('".$model->field["YEAR"]."',1,4) AND ";
            $query .= "                         SCHOOL_KIND = '".$model->schoolkind."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
