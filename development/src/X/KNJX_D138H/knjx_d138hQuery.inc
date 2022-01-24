<?php

require_once('for_php7.php');
class knjx_d138hquery extends Query
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

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            /**********/
            /* 通  年 */
            /**********/
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
            $dataCnt = $db->getOne($query);

            $data = array();
            //データセット
            foreach ($model->fieldSize as $key => $val) {
                if ($key == "REMARK1" || $key == "SPECIALACTREMARK" || $key == "ATTENDREC_REMARK" || $key == "COMMUNICATION") {
                    continue;
                }
                $data[$key][TEXT]    = $data_arr[$i][$key];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $dataCnt) {
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]         = '9';
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "     AND SEMESTER = '9' ";
                $where .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            }
            $db->query($query);

            /**************/
            /* 詳細データ */
            /**************/
            //データセット
            foreach ($model->fieldSize as $key => $val) {
                $data = array();
                if ($key != "REMARK1" && $key != "SPECIALACTREMARK") {
                    continue;
                }
                $setCode = $key == "REMARK1" ? "01" : "02";
                $setKey = $key == "SPECIALACTREMARK" ? "REMARK1" : $key;
                $data[$setKey][TEXT]    = $data_arr[$i][$key];

                $query  = " SELECT ";
                $query .= "     COUNT(*) ";
                $query .= " FROM ";
                $query .= "     HREPORTREMARK_DETAIL_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $query .= "     AND SEMESTER = '9' ";
                $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
                $query .= "     AND DIV      = '03' ";
                $query .= "     AND CODE     = '".$setCode."' ";
                $dataCnt = $db->getOne($query);

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                if (1 > $dataCnt) {
                    $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $data["SEMESTER"][TEXT]         = '9';
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    $data["DIV"][TEXT]              = "03";
                    $data["CODE"][TEXT]             = $setCode;
                    $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
                    $where .= "     AND SEMESTER = '9' ";
                    $where .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $where .= "     AND DIV      = '03' ";
                    $where .= "     AND CODE     = '".$setCode."' ";

                    $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                }
                $db->query($query);
            }

            /**********/
            /* 年  度 */
            /**********/
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
            $dataCnt = $db->getOne($query);

            $data = array();
            //データセット
            foreach ($model->fieldSize as $key => $val) {
                if ($key != "ATTENDREC_REMARK" && $key != "COMMUNICATION") {
                    continue;
                }
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
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND DIV      = '03' ";
            $query .= "     AND CODE     = '01' ";
            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND DIV      = '03' ";
            $query .= "     AND CODE     = '02' ";
            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
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
        $field = "";
        foreach ($model->fieldSize as $key => $val) {
            if ($key == "REMARK1") {
                $tableSoeji = "T4";
            } elseif ($key == "SPECIALACTREMARK") {
                $tableSoeji = "T5";
            } elseif ($key == "ATTENDREC_REMARK" || $key == "COMMUNICATION") {
                $tableSoeji = "T6";
            } else {
                $tableSoeji = "T3";
            }
            $setKey = $key == "SPECIALACTREMARK" ? "REMARK1 AS SPECIALACTREMARK" : $key;
            $field .= $tableSoeji.".".$setKey.",";
        }

        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     ".$field." ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "          AND T3.SEMESTER = '9' ";
        $query .= "          AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT T4 ON T4.YEAR = T1.YEAR ";
        $query .= "          AND T4.SEMESTER = T3.SEMESTER ";
        $query .= "          AND T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND T4.DIV      = '03' ";
        $query .= "          AND T4.CODE     = '01' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT T5 ON T5.YEAR = T1.YEAR ";
        $query .= "          AND T5.SEMESTER = T3.SEMESTER ";
        $query .= "          AND T5.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND T5.DIV      = '03' ";
        $query .= "          AND T5.CODE     = '02' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T6 ON T6.YEAR = T1.YEAR ";
        $query .= "          AND T6.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T6.SCHREGNO = T1.SCHREGNO ";
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
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER ";

        return $query;
    }
}
