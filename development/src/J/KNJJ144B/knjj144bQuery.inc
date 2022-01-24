<?php

require_once('for_php7.php');

class knjj144bquery extends Query {

    //学年コンボ
    function getGrade($type) {
        $query  = " SELECT ";
        if($type == "value"){
            $query .= " GRADE AS VALUE ";
        } else {
            $query .= " GRADE AS VALUE, ";
            $query .= " GRADE_NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if($type == "value"){
            $query .= " FETCH FIRST 1 ROWS ONLY ";
        }

        return $query;
    }

    //年組コンボ
    function getHrClass($grade) {
        $query  = " SELECT DISTINCT ";
        $query .= "     HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別コンボ
    function getGender() {
        $query  = " SELECT ";
        $query .= "     NAME2 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'Z002' ";

        return $query;
    }

    //在籍データ取得
    function getRegd($grade, $hr_class, $attendno, $type) {
        $query  = " SELECT ";
        if($type == "cnt"){
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     T2.SCHREGNO ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "        ON T1.YEAR     = T2.YEAR ";
        $query .= "       AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "       AND T1.GRADE    = T2.GRADE ";
        $query .= "       AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND T2.GRADE    = '".sprintf("%02d",$grade)."' ";
        $query .= " AND T2.HR_CLASS = '".sprintf("%03d",$hr_class)."' ";
        $query .= " AND T2.ATTENDNO = '".sprintf("%03d",$attendno)."' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model) {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($model->data_arr); $i++) {
            $schregno = $db->getOne(knjj144bQuery::getRegd($model->data_arr[$i]["GRADE"], $model->data_arr[$i]["HR_CLASS"], $model->data_arr[$i]["ATTENDNO"], "schregno"));

            //削除
            $query  = " DELETE FROM ";
            $query .= "     MARATHON_EVENT_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR          = '".CTRL_YEAR."' AND ";
            $query .= "     SEQ           = '00' AND ";
            $query .= "     SCHREGNO      = '".$schregno."' ";

            $db->query($query);

            //追加
            $data = array();
            $data["YEAR"][TEXT]       = CTRL_YEAR;
            $data["SEQ"][TEXT]        = "00";
            $data["SCHREGNO"][TEXT]   = $schregno;
            $data["TIME_H"][TEXT]     = sprintf("%02d", $model->data_arr[$i]["TIME_H"]);
            $data["TIME_M"][TEXT]     = sprintf("%02d", $model->data_arr[$i]["TIME_M"]);
            $data["TIME_S"][TEXT]     = sprintf("%02d", $model->data_arr[$i]["TIME_S"]);
            $data["ATTEND_CD"][TEXT]  = "";
            $data["REMARK"][TEXT]     = $model->data_arr[$i]["REMARK"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "SYSDATE()";

            $query = Query::insertSQL($data, "MARATHON_EVENT_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($model->data_arr); $i++) {
            $schregno = $db->getOne(knjj144bQuery::getRegd($model->data_arr[$i]["GRADE"], $model->data_arr[$i]["HR_CLASS"], $model->data_arr[$i]["ATTENDNO"], "schregno"));
            //データセット
            $data = array();
            $query  = " DELETE FROM ";
            $query .= "     MARATHON_EVENT_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR          = '".CTRL_YEAR."' AND ";
            $query .= "     SEQ           = '00' AND ";
            $query .= "     SCHREGNO      = '".$schregno."' ";

            $db->query($query);
            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $query .= " SELECT ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.TIME_H, ";
        $query .= "     T1.TIME_M, ";
        $query .= "     T1.TIME_S, ";
        $query .= "     T1.ATTEND_CD, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     MARATHON_EVENT_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_DAT T2 ON  T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                        AND T1.YEAR     = T2.YEAR ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SEQ  = '00' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.GRADE       = '{$model->field["GRADE"]}' AND ";
        if ($model->field["HR_CLASS"] != "ALL") {
            $query .= "     T2.HR_CLASS = '{$model->field["HR_CLASS"]}' AND ";
        }
        if ($model->field["SEX"] != "ALL") {
            $query .= "     T3.SEX = '{$model->field["SEX"]}' AND ";
        }
        if ($model->field["ATTEND_CD"] != "1") {
            $query .= "     T1.ATTEND_CD IS NULL AND ";
        }
        $query .= "     T1.YEAR           = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
?>
