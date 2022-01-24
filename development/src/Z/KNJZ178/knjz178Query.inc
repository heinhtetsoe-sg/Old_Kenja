<?php

require_once('for_php7.php');

class knjz178Query extends Query {
    //学年コンボ
    function getGrade() {
        $query .= " SELECT ";
        $query .= "     CASE WHEN INT(T1.GRADE) > 3 ";
        $query .= "          THEN ";
        $query .= "               CASE WHEN L1.NAMECD1 IS NOT NULL ";
        $query .= "                    THEN '高校' || RTRIM(CHAR(INT(T1.GRADE) - 3)) || '年' ";
        $query .= "                    ELSE '高校' || RTRIM(CHAR(INT(T1.GRADE))) || '年' ";
        $query .= "               END ";
        $query .= "          ELSE '中学' || RTRIM(CHAR(INT(T1.GRADE))) || '年' ";
        $query .= "     END AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON L1.NAMECD1 = 'Z010' ";
        $query .= "                 AND NAMECD2 = '00' ";
        $query .= "                 AND NAMESPARE2 IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '2006' ";
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     L1.NAMECD1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE ";

        return $query;
    }

    //学期コンボ
    function getSemester() {
        $query .= " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //リスト取得
    function getList($grade) {
        $year = CTRL_YEAR;

        $query  = " WITH CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         GRADE, ";
        $query .= "         SEMESTER, ";
        $query .= "         COUNT(*) AS COUNT ";
        $query .= "     FROM ";
        $query .= "         ASSESS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' AND ";
        $query .= "         GRADE = '{$grade}' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         GRADE, ";
        $query .= "         SEMESTER ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEMESTERNAME, ";
        $query .= "     L2.ASSESSLEVEL, ";
        $query .= "     L2.ASSESSMARK, ";
        $query .= "     L2.ASSESSLOW, ";
        $query .= "     L2.ASSESSHIGH, ";
        $query .= "     L1.COUNT ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CNT L1 ON  L1.YEAR     = T1.YEAR ";
        $query .= "            AND L1.SEMESTER = T1.SEMESTER ";
        $query .= " LEFT JOIN ";
        $query .= "     ASSESS_DAT L2 ON  L2.YEAR     = T1.YEAR ";
        $query .= "                   AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                   AND L2.GRADE    = L1.GRADE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         L2.ASSESSLEVEL ";

        return $query;
    }

    //初期としての直前の学期の値を取得
    function getList_before($semester, $model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L2.ASSESSLEVEL, ";
        $query .= "     L2.ASSESSMARK, ";
        $query .= "     L2.ASSESSLOW, ";
        $query .= "     L2.ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ASSESS_DAT L2 ON  L2.YEAR     = T1.YEAR ";
        $query .= "                   AND L2.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$year}' AND ";
        $query .= "         T1.SEMESTER = '{$semester}' AND ";
        $query .= "         L2.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         L2.ASSESSLEVEL ";

        return $query;
    }

    //9学期だったときの対策(直前の8学期が存在しないため)
    function getDummy_semester($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) ";
        $query .= " FROM ";
        $query .= "     ASSESS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     GRADE = '{$model->field["GRADE"]}' AND ";
        $query .= "     SEMESTER < '{$model->field["SEMESTER"]}' ";

        return $query;
    }

    //Insert作成
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     ASSESS_DAT ";
        $query .= " WHERE  ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";

        $db->query($query);

        for ($i = 1; $i <= $this->field["ASSESSLEVELCNT"]; $i++) {
            $assessMark = "ASSESSMARK_" . $model->field["SEMESTER"] . "_" . $i;
            $assessLow  = "ASSESSLOW_"  . $model->field["SEMESTER"] . "_" . $i;
            $assessHigh = "ASSESSHIGH_" . $model->field["SEMESTER"] . "_" . $i;

            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["GRADE"][TEXT]         = $model->field["GRADE"];
            $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
            $data["ASSESSLEVEL"][NUMBER] = $i;
            $data["ASSESSMARK"][TEXT]    = $model->field[$assessMark];
            $data["ASSESSLOW"][NUMBER]   = $model->field[$assessLow];
            $data["ASSESSHIGH"][NUMBER]  = $model->field[$assessHigh];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "sysdate()";

            $query = Query::insertSQL($data, "ASSESS_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
