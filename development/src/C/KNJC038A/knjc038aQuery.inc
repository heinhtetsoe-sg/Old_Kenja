<?php
class knjc038aquery extends Query {

    //学期
    function getSemesterInfo($model) {
        $query  = " SELECT ";
        $query .= "     SEME.* ";
        $query .= " FROM SEMESTER_MST SEME ";
        $query .= " WHERE SEME.YEAR = '{$model->year}' ";
        $query .= "   AND SEME.SEMESTER = '{$model->semester}' ";

        return $query;
    }

    // 学籍番号を取得してくる(取れなければ引数が不正)
    function getSchregNo($model, $data) {

        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO ";
        $query .= " FROM SCHREG_REGD_DAT REGD ";
        $query .= " INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "   ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= " INNER JOIN SCHREG_REGD_HDAT REGDH ";
        $query .= "   ON REGD.YEAR = REGDH.YEAR ";
        $query .= "   AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "   AND REGD.GRADE = REGDH.GRADE ";
        $query .= "   AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= " WHERE REGD.YEAR = '{$model->year}' ";
        $query .= "   AND REGD.SEMESTER = '{$model->semester}' ";

        $query .= "   AND REGD.ATTENDNO = '{$data['ATTENDNO']}' ";
        $query .= "   AND REGDH.HR_NAMEABBV = '{$data['HR_CLASS']}' ";
        $query .= "   AND BASE.NAME = '{$data['NAME']}' ";

        return $query;
    }

    // その日その校時のその生徒が受講した講座コードを取得してくる(取れなければ引数が不正)
    function getChaircd($model, $data, $period) {
        $day = str_replace('/', '-', $data["ATTENDDATE"]);

        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM SCH_CHR_DAT T1 ";
        $query .= " INNER JOIN CHAIR_STD_DAT L1 ";
        $query .= "   ON  T1.YEAR = L1.YEAR ";
        $query .= "   AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "   AND T1.CHAIRCD = L1.CHAIRCD ";
        $query .= "   AND T1.EXECUTEDATE BETWEEN L1.APPDATE AND L1.APPENDDATE ";

        $query .= " WHERE T1.EXECUTEDATE = '{$day}' ";
        $query .= "   AND T1.PERIODCD = '{$period}' ";
        $query .= "   AND L1.SCHREGNO = '{$data["SCHREGNO"]}' ";

        return $query;
    }

    //データの削除(出欠日別データ)
    // 指定範囲のデータは、最初に全て削除する。
    // 学校側が、空のCSVを取り込むことでクリアされる様な仕様が望ましいとの事。
    function allDeleteAttendDayDat($model) {
        $startDate = str_replace('/', '-', $model->field["START_DATE"]);
        $endDate   = str_replace('/', '-', $model->field["END_DATE"]);

        $query  = " DELETE FROM ATTEND_DAY_DAT ";
        $query .= " WHERE ";
        $query .= "   ATTENDDATE BETWEEN '{$startDate}' AND '{$endDate}' ";

        return $query;
    }

    //データの削除(出欠データ)
    // 指定範囲のデータは、最初に全て削除する。
    // 学校側が、空のCSVを取り込むことでクリアされる様な仕様が望ましいとの事。
    function allDeleteAttendDat($model) {
        $startDate = str_replace('/', '-', $model->field["START_DATE"]);
        $endDate   = str_replace('/', '-', $model->field["END_DATE"]);

        $query  = " DELETE FROM ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "   ATTENDDATE BETWEEN '{$startDate}' AND '{$endDate}' ";

        return $query;
    }

    //データの削除(出欠日別データ)
    function deleteAttendDayDat($model, $data) {
        $attenddate = str_replace('/', '-', $data["ATTENDDATE"]);

        $query  = " DELETE FROM ATTEND_DAY_DAT ";
        $query .= " WHERE SCHREGNO    = '{$data["SCHREGNO"]}' ";
        $query .= "   AND ATTENDDATE  = '{$attenddate}' ";

        return $query;
    }

    //データの削除(出欠データ)
    function deleteAttendDat($model, $data) {
        $attenddate = str_replace('/', '-', $data["ATTENDDATE"]);

        $query  = " DELETE FROM ATTEND_DAT ";
        $query .= " WHERE SCHREGNO    = '{$data["SCHREGNO"]}' ";
        $query .= "   AND ATTENDDATE  = '{$attenddate}' ";

        return $query;
    }

    //エラーデータの削除
    function deleteQueryErr() {
        $query   = " DELETE FROM W_CSVMSG_PRG_DAT ";
        $query  .= " WHERE PROGRAMID = 'KNJC038A' ";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = "KNJC038A";
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CVS作成用のQUERY
    function selectCsvQuery() {
        $query   = " SELECT MSGROW, MSGREMARK ";
        $query  .= " FROM W_CSVMSG_PRG_DAT ";
        $query  .= " WHERE PROGRAMID = 'KNJC038A'";

        return $query;
    }
}
?>
