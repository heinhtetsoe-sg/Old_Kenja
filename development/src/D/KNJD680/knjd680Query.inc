<?php

require_once('for_php7.php');

class knjd680query extends Query {
    //学期取得
    function getSemester($semester="") {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        if ($semester) {
            $query .= " AND SEMESTER = '".$semester."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        //総合的な学習の時間の表
        $query .= " SELECT ";
        $query .= "     W1.GRADE, ";
        $query .= "     W1.HR_CLASS, ";
        $query .= "     W1.HR_NAME, ";
        $query .= "     W2.TITLE ";
        $query .= " FROM SCHREG_REGD_HDAT W1 ";
        $query .= "     LEFT JOIN TOTALSTUDY_HR_TITLE_DAT W2 ON W1.YEAR = W2.YEAR ";
        $query .= "         AND W1.SEMESTER = W2.SEMESTER ";
        $query .= "         AND W1.GRADE = W2.GRADE ";
        $query .= "         AND W1.HR_CLASS = W2.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "    W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND W1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "    AND W1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     W1.GRADE, ";
        $query .= "     W1.HR_CLASS ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["GRADE_HR_CLASS"]); $i++) {

            /*** TOTALSTUDY_HR_TITLE_DAT ***/
            $year = CTRL_YEAR;
            $semcd = $model->field["SEMESTER"];
            list ($grade, $hr_class) = preg_split("/-/", $model->data["GRADE_HR_CLASS"][$i]);
            //項目
            $data = array();
            $data["YEAR"][TEXT]             = $year;
            $data["SEMESTER"][TEXT]         = $semcd;
            $data["GRADE"][TEXT]            = $grade;
            $data["HR_CLASS"][TEXT]         = $hr_class;
            $data["TITLE"][TEXT]            = $model->fields["TITLE"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //存在するか？
            $query  = "SELECT COUNT(*) FROM TOTALSTUDY_HR_TITLE_DAT ";
            $query .= " WHERE YEAR          = '{$year}'";
            $query .= "   AND SEMESTER      = '{$semcd}'";
            $query .= "   AND GRADE         = '{$grade}' ";
            $query .= "   AND HR_CLASS      = '{$hr_class}' ";
            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE YEAR          = '{$year}'";
                $where .= "   AND SEMESTER      = '{$semcd}'";
                $where .= "   AND GRADE         = '{$grade}' ";
                $where .= "   AND HR_CLASS      = '{$hr_class}' ";
                $query = Query::updateSQL($data, "TOTALSTUDY_HR_TITLE_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "TOTALSTUDY_HR_TITLE_DAT");
            }
            $db->query($query);

        }

        Query::dbCheckIn($db);
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR ='".CTRL_YEAR."' ";
        $query .= "         AND T2.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND T1.SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
