<?php

require_once('for_php7.php');

class knjj144cQuery extends Query {

    //学年コンボ
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE ";
        $query .= "     , GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";

        return $query;
    }

    //学級コンボ
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     HR_CLASS AS VALUE ";
        $query .= "     , HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別コンボ
    function getGender($model) {
        $query  = " SELECT ";
        $query .= "     NAME2 AS LABEL ";
        $query .= "     , NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'Z002' ";

        return $query;
    }

    //一覧取得
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO ";
        $query .= "     , REGD.ATTENDNO ";
        $query .= "     , HDAT.HR_NAME || '　' || REGD.ATTENDNO AS HR_NAME ";
        $query .= "     , BASE.NAME ";
        $query .= "     , MAR.ATTEND_CD ";
        $query .= "     , MAR.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "         ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN V_NAME_MST Z002 ";
        $query .= "         ON Z002.YEAR = REGD.YEAR ";
        $query .= "         AND Z002.NAMECD1 = 'Z002' ";
        $query .= "         AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "         ON HDAT.YEAR = REGD.YEAR ";
        $query .= "         AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "         AND HDAT.GRADE = REGD.GRADE ";
        $query .= "         AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN MARATHON_EVENT_DAT MAR ";
        $query .= "         ON MAR.YEAR = REGD.YEAR ";
        $query .= "         AND MAR.SEQ = '00' ";
        $query .= "         AND MAR.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".$model->year."' ";
        $query .= "     AND REGD.SEMESTER = '".$model->semester."' ";
        $query .= "     AND REGD.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND REGD.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        if ($model->field["GENDER"]) {
            $query .= "     AND BASE.SEX = '".$model->field["GENDER"]."' ";
        }
        if ($model->field["IGNORE_TIME_REGISTRATION"]) {
            $query .= "     AND MAR.TIME_S IS NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    function checkAttendCd ($model, $attendCd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'J010' ";
        $query .= "     AND NAMECD2 = '".$attendCd."' ";

        return $query;
    }

    function getNameMstJ010 ($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS NAME, ";
        $query .= "     CAST(NAMECD2 AS INT) AS NAMEVAL  ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'J010' ";
        $query .= " ORDER BY ";
        $query .= "     NAMEVAL ";

        return $query;
    }

    function &getUpdateQuery($model)
    {

        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        foreach ($model->schregnoList as $schregno) {
            $data = array();

            $data["ATTEND_CD"][TEXT] = $model->listData[$schregno]["ATTEND_CD"];
            $data["REMARK"][TEXT] = $model->listData[$schregno]["REMARK"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC] = "sysdate()";

            if (0 < $db->getOne(knjj144cQuery::checkMarathonEventDat($model, $schregno))) {
                $where  = " WHERE ";
                $where .= "     YEAR = '".$model->year."' ";
                $where .= "     AND SEQ = '00' ";
                $where .= "     AND SCHREGNO = '".$schregno."' ";

                $query = Query::updateSQL($data, "MARATHON_EVENT_DAT", $where);
            } else {
                $data["YEAR"][TEXT] = $model->year;
                $data["SEQ"][TEXT] = '00';
                $data["SCHREGNO"][TEXT] = $schregno;

                $query = Query::insertSQL($data, "MARATHON_EVENT_DAT");
            }
            $db->query($query);
        }


        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    function checkMarathonEventDat($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MARATHON_EVENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SEQ = '00' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

}
?>
