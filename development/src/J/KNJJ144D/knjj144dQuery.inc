<?php

require_once('for_php7.php');

class knjj144dQuery extends Query {

    //学級コンボ
    function getGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE ";
        $query .= "     , HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
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
        $query .= "     , BASE.NAME ";
        $query .= "     , Z002.NAME2 AS GENDER ";
        $query .= "     , MAR.TIME_H ";
        $query .= "     , MAR.TIME_M ";
        $query .= "     , MAR.TIME_S ";
        $query .= "     , MAR.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "         ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN V_NAME_MST Z002 ";
        $query .= "         ON Z002.YEAR = REGD.YEAR ";
        $query .= "         AND Z002.NAMECD1 = 'Z002' ";
        $query .= "         AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN MARATHON_EVENT_DAT MAR ";
        $query .= "         ON MAR.YEAR = REGD.YEAR ";
        $query .= "         AND MAR.SEQ = '00' ";
        $query .= "         AND MAR.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".$model->year."' ";
        $query .= "     AND REGD.SEMESTER = '".$model->semester."' ";
        $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        if ($model->field["GENDER"]) {
            $query .= "     AND BASE.SEX = '".$model->field["GENDER"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    function &getUpdateQuery($model)
    {

        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        foreach ($model->schregnoList as $schregno) {
            $data = array();

            $data["TIME_H"][TEXT] = sprintf("%02d", $model->listData[$schregno]["TIME_H"]);
            $data["TIME_M"][TEXT] = sprintf("%02d", $model->listData[$schregno]["TIME_M"]);
            $data["TIME_S"][TEXT] = sprintf("%02d", $model->listData[$schregno]["TIME_S"]);
            $data["REMARK"][TEXT] = $model->listData[$schregno]["REMARK"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC] = "sysdate()";

            if (0 < $db->getOne(knjj144dQuery::checkMarathonEventDat($model, $schregno))) {
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
