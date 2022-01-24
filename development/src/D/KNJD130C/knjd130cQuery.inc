<?php

require_once('for_php7.php');

class knjd130cQuery extends Query {
    //校種取得
    function getSchoolKind($schregno) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_DAT T1, ";
        $query .= "    SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE ";

        $db = Query::dbCheckOut();
        $retVal = $db->getOne($query);
        Query::dbCheckIn($db);

        return $retVal;
    }

    //学期取得
    function getSemesterQuery() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY  ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //データ取得
    function getHreportremarkDat($schregno, $semester) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model, $schregno) {
        $db = Query::dbCheckOut();

        //更新対象項目
        $array = array();
        foreach ($model->textList as $field => $val) {
            if ($val["flg"]) {
                $array[9][$field] = $model->field9[$field];
            } else {
                $array[$model->gakki][$field] = $model->field[$field];
            }
        }

        //学期の配列
        $semeArray = array();
        $semeArray[] = $model->gakki;
        if (get_count($array[9])) $semeArray[] = 9;

        foreach ($semeArray as $semester) {
            //削除
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER    = '".$semester."' AND ";
            $query .= "     SCHREGNO    = '".$schregno."' ";
            $db->query($query);

            //更新
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $semester;
            $data["SCHREGNO"][TEXT]         = $schregno;
            foreach ($array[$semester] as $field => $val) {
                $data[$field][TEXT]             = $val;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        }

        Query::dbCheckIn($db);
        return;
    }
}
?>