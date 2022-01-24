<?php

require_once('for_php7.php');

class knjh080equery extends Query {
    //学期一覧取得
    function getSemesterList() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($semester, $model) {
        $query .= " SELECT ";
        $query .= "     H1.GRADE || H1.HR_CLASS AS VALUE, ";
        $query .= "     H1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT H1 ";
        $query .= " INNER JOIN ";
        $query .= " 	(SELECT  ";
        $query .= "         NAME2 ";
        $query .= "     FROM  ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'A023' ";
        $query .= "         AND NAME1 = 'H') N1 ";
        $query .= "     ON H1.GRADE = N1.NAME2 ";
        $query .= " WHERE ";
        $query .= "     H1.YEAR = '" . CTRL_YEAR . "' ";
        $query .= "     AND H1.SEMESTER = '" . $semester . "' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    function selectQuery($model) {
    
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     B1.BASE_REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B1 ON T1.SCHREGNO = B1.SCHREGNO ";
        $query .= "                                             AND T1.YEAR     = B1.YEAR ";
        $query .= "                                             AND B1.BASE_SEQ = '006', ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR                 = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER             = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T1.SCHREGNO             = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();

        foreach ($model->checked as $schregno) {

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["BASE_SEQ"][TEXT]         = "006";
            $data["BASE_REMARK1"][TEXT]     = $model->field["BASE_REMARK1{$schregno}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //存在チェック
            $query  = " SELECT COUNT(*) FROM SCHREG_BASE_YEAR_DETAIL_MST ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SCHREGNO = '".$schregno."' ";
            $query .= "     AND BASE_SEQ = '006' ";

            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE ";
                $where .= "         YEAR         = '".CTRL_YEAR."' ";
                $where .= "     AND SCHREGNO     = '".$schregno."' ";
                $where .= "     AND BASE_SEQ     = '006' ";
                $query = Query::updateSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST");
            }
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }

/*===================================一括更新画面===================================*/

    //学期名取得
    function getSemesterName($semester) {
        $query .= " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER ='" . $semester . "' ";

        return $query;
    }

    //年組名取得
    function getHrName($semester, $grade_hr_class) {
        $query .= " SELECT ";
        $query .= "     H1.HR_NAME";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT H1 ";
        $query .= " INNER JOIN ";
        $query .= " 	(SELECT  ";
        $query .= "         NAME2 ";
        $query .= "     FROM  ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'A023' ";
        $query .= "         AND NAME1 = 'H') N1 ";
        $query .= "     ON H1.GRADE = N1.NAME2 ";
        $query .= " WHERE ";
        $query .= "     H1.YEAR = '" . CTRL_YEAR . "' ";
        $query .= "     AND H1.SEMESTER = '" . $semester . "' ";
        $query .= "     AND H1.GRADE || H1.HR_CLASS = '" . $grade_hr_class . "' ";

        return $query;
    }

    //置換画面用の生徒一覧取得
    function getStudent($model, $flg="")
    {
        $selectdata = str_replace(",","','",$model->replace_data["selectdata"]);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->grade_hr_class."' AND ";
        if($flg == "1"){
            $query .= "     T1.SCHREGNO IN ('".$selectdata."') ";
        } else {
            $query .= "     T1.SCHREGNO NOT IN ('".$selectdata."') ";
        }
        $query .= " ORDER BY";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //一括更新
    function ReplaceUpdate($model)
    {
        $db = Query::dbCheckOut();
        $model->replace_data["selectdata"] = explode(",", $model->replace_data["selectdata"]);

        for ($i = 0; $i < get_count($model->replace_data["selectdata"]); $i++) {
            $schregno = $model->replace_data["selectdata"][$i];

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["BASE_SEQ"][TEXT]         = "006";
            $data["BASE_REMARK1"][TEXT]     = $model->replace_data["field"]["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //存在チェック
            $query  = " SELECT COUNT(*) FROM SCHREG_BASE_YEAR_DETAIL_MST ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SCHREGNO = '".$schregno."' ";
            $query .= "     AND BASE_SEQ = '006' ";

            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE ";
                $where .= "         YEAR         = '".CTRL_YEAR."' ";
                $where .= "     AND SCHREGNO     = '".$schregno."' ";
                $where .= "     AND BASE_SEQ     = '006' ";
                $query = Query::updateSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST");
            }
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>

