<?php

require_once('for_php7.php');

class knjd655cquery extends Query {
    //学期一覧取得
    function getSemesterList() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     =  '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト一覧取得
    function getTestList($model, $flg="") {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || ' ' || TESTITEMNAME AS LABEL, ";
        $query .= "     SEMESTER_DETAIL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' ";
        if($flg){
            $query .= "    AND TESTKINDCD || TESTITEMCD = '".$model->field["TESTKINDCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($semester) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期詳細データより日付取得
    function getSemesDate($semester, $detail="") {
        $query  = " SELECT ";
        if(!$detail){
            $query .= "     MIN(SDATE) AS SDATE, ";
            $query .= "     MAX(EDATE) AS EDATE ";
        } else {
            $query .= "     SDATE, ";
            $query .= "     EDATE ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if($semester != "9"){
            $query .= "     AND SEMESTER = '".$semester."' ";
        }
        if($detail){
            $query .= "     AND SEMESTER_DETAIL = '".$detail."' ";
        }

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $semester) {
        $query  = " WITH REMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 AS REMARK ";
        $query .= "     FROM ";
        $query .= "         HEXAM_RECORD_REMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "         TESTKINDCD || TESTITEMCD = '{$model->field["TESTKINDCD"]}' AND ";
        $query .= "         REMARK_DIV = '{$model->field["RIYUU"]}' ";
        $query .= " ), SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".$semester."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     REMARK T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            //チェックボックスにチェックされてなかったら更新対象ではない
            if ($model->fields["CHECK"][$i] != '1') {
                continue;
            }

            $query  = " DELETE FROM ";
            $query .= "     HEXAM_RECORD_REMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER   = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     TESTKINDCD || TESTITEMCD = '".$model->field["TESTKINDCD"]."' AND ";
            $query .= "     SCHREGNO   = '".$schregno."' AND ";
            $query .= "     REMARK_DIV = '{$model->field["RIYUU"]}' ";

            $db->query($query);

            //空で更新の場合はレコード削除の意味
            if (!strlen($model->fields["REMARK"][$i])) {
                continue;
            }

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["TESTKINDCD"][TEXT]       = substr($model->field["TESTKINDCD"], 0, 2);
            $data["TESTITEMCD"][TEXT]       = substr($model->field["TESTKINDCD"], 2);
            $data["REMARK_DIV"][TEXT]       = "{$model->field["RIYUU"]}";
            $data["REMARK1"][TEXT]          = $model->fields["REMARK"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "HEXAM_RECORD_REMARK_DAT");

            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
