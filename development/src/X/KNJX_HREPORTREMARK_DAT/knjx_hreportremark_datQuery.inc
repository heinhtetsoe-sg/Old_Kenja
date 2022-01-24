<?php

require_once('for_php7.php');
class knjx_hreportremark_datquery extends Query
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

    //学年一覧
    public function getGrade($model)
    {
        $year = substr($model->field["YEAR"], 0, 4);

        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if ($model->schoolkind) {
            $query .= "     AND SCHOOL_KIND = '".$model->schoolkind."' ";
        }
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
        if ($model->field["GRADE"]) {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
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
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$data["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = '".$data["SEMESTER"]."' AND ";
        $query .= "     T1.SCHREGNO = '".$data["SCHREGNO"]."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T2.SCHOOL_KIND = '".$model->schoolkind."' ";
        if ($model->field["GRADE"]) {
            $query .= " AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }

        return $query;
    }

    public function getGradeCd($data_arr)
    {
        $query .= " SELECT ";
        $query .= "   T5.GRADE_CD ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T4 ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T5 ";
        $query .= "     ON T5.YEAR = T4.YEAR ";
        $query .= "    AND T5.GRADE = T4.GRADE ";
        $query .= " WHERE ";
        $query .= "     T4.YEAR = '".$data_arr["YEAR"]."' ";
        $query .= "     AND T4.SEMESTER = '".$data_arr["SEMESTER"]."' ";
        $query .= "     AND T4.SCHREGNO = '".$data_arr["SCHREGNO"]."' ";
        return $query;
    }
    public function getCntHReportRemark($data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$data_arr["YEAR"]."' ";
        $query .= "     AND SEMESTER = '".$data_arr["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$data_arr["SCHREGNO"]."' ";
        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query = knjx_hreportremark_datquery::getCntHReportRemark($data_arr[$i]);
            $dataCnt = $db->getOne($query);
            $query = knjx_hreportremark_datquery::getGradeCd($data_arr[$i]);
            $gradeCd = $db->getOne($query);
            $query = knjx_hreportremark_datquery::insertQueryCsvSetSemes($model, $data_arr[$i], $dataCnt, $gradeCd);
            $db->query($query);
            if ($model->getPrgId == "KNJD138P" && (int)$gradeCd >= 3) {
                $lastdata = $data_arr[$i];
                $lastdata["SEMESTER"] = "9";
                $query = knjx_hreportremark_datquery::getCntHReportRemark($lastdata);
                $dataCnt = $db->getOne($query);
                $query = knjx_hreportremark_datquery::insertQueryCsvSetSemes($model, $lastdata, $dataCnt, $gradeCd);
                $db->query($query);
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);
        return $cnt;
    }

    public function insertQueryCsvSetSemes($model, $data_arr, $dataCnt, $gradeCd)
    {
        $data = array();

        //データセット
        foreach ($model->fieldSize as $key => $val) {
            //KNJD138P呼び出しで2年生以下で統合的な学習は無いのでcontinue
            if ($model->getPrgId == "KNJD138P" && (int)$gradeCd < 3 && $key == "TOTALSTUDYTIME") {
                continue;
            }
            //KNJD138P呼び出しで"9"学期以外では委員会/クラブ活動は登録しないのでcontinue
            if ($model->getPrgId == "KNJD138P" && $data_arr["SEMESTER"] != "9" && in_array($key, array("COMMITTEE", "CLUB"))) {
                continue;
            }
            if ($model->getPrgId == "KNJD138P" && $data_arr["SEMESTER"] == "9") {
                //KNJD138P呼び出しで"9"学期なら委員会/クラブ活動以外は登録しない
                if (in_array($key, array("COMMITTEE", "CLUB"))) {
                    $data[$key][TEXT]    = $data_arr[$key];
                }
            } else {
                //上記以外は通常登録
                $data[$key][TEXT]    = $data_arr[$key];
            }
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        if (1 > $dataCnt) {
            $data["YEAR"][TEXT]             = $data_arr["YEAR"];
            $data["SEMESTER"][TEXT]         = $data_arr["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $data_arr["SCHREGNO"];
            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '".$data_arr["YEAR"]."' ";
            $where .= "     AND SEMESTER = '".$data_arr["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO = '".$data_arr["SCHREGNO"]."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
        }
        return $query;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $schChkArry = array();
        $sep = "";
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);
            if (!in_array($data_arr[$i]["SCHREGNO"], $schChkArry)) {
                $schChkInState .= $sep."'".$data_arr[$i]["SCHREGNO"]."'";
                $sep = ", ";
                $schChkArry[] = $data_arr[$i]["SCHREGNO"];
            }
            $cnt++;
        }
        //KNJD138P呼び出しかつ3年生以上のみ実施する処理
        if ($model->getPrgId == "KNJD138P" && (int)$model->gradeCD[$model->field["GRADE"]] >= 3) {
            //上記削除したデータで、9期以外のデータが無ければ、9期のデータを削除
            //※残っているデータに通年のデータは紐づくべきなので、消せない。

            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T2.SCHREGNO IN (".$schChkInState.") ";
            $query .= "     AND NOT EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "           'X' ";
            $query .= "         FROM ";
            $query .= "           HREPORTREMARK_DAT T3 ";
            $query .= "         WHERE ";
            $query .= "           T3.YEAR = T2.YEAR ";
            $query .= "           AND T3.SEMESTER <> '9' ";
            $query .= "           AND T3.SCHREGNO = T2.SCHREGNO ";
            $query .= "     ) ";
            $query .= "     AND NOT EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "           'X' ";
            $query .= "         FROM ";
            $query .= "           SCHREG_REGD_DAT T4 ";
            $query .= "           LEFT JOIN SCHREG_REGD_GDAT T5 ";
            $query .= "             ON T5.YEAR = T4.YEAR ";
            $query .= "            AND T5.GRADE = T4.GRADE ";
            $query .= "         WHERE ";
            $query .= "           T4.YEAR = T2.YEAR ";
            $query .= "           AND T4.SEMESTER = T2.SEMESTER ";
            $query .= "           AND T4.SCHREGNO = T2.SCHREGNO ";
            $query .= "           AND T5.GRADE_CD < '03' ";
            $query .= "     ) ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $field = "";
        foreach ($model->fieldSize as $key => $val) {
            //KNJD138P呼び出しかつ3年生以上は"9"期のCLUB/COMMITTEEを付ける。それ以外では付けない。
            if ($model->getPrgId == "KNJD138P" && (int)$model->gradeCD[$model->field["GRADE"]] >= 3) {
                if ($key == "COMMITTEE") {
                    $field .= "T4.REMARK1 AS ".$key.",";
                } elseif ($key == "CLUB") {
                    $field .= "T4.REMARK3 AS ".$key.",";
                } else {
                    $field .= "T3.".$key.",";
                }
            } else {
                if ($model->getPrgId != "KNJD138P" || ($key != "COMMITTEE" && $key != "CLUB")) {
                    $field .= "T3.".$key.",";
                }
            }
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
        $query .= "          AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->getPrgId == "KNJD138P" && (int)$model->gradeCD[$model->field["GRADE"]] >= 3) {
            $query .= "     LEFT JOIN HREPORTREMARK_DAT T4 ON T4.YEAR = T1.YEAR ";
            $query .= "          AND T4.SEMESTER = '9' ";
            $query .= "          AND T4.SCHREGNO = T1.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE"]) {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
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
