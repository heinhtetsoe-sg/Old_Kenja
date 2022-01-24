<?php

require_once('for_php7.php');

class knjz181Query extends Query {
    //参照年度
    function getCopyYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_ASSESS_DAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //対象年度
    function getTargetYear() {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //学年
    function getGrade($year) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     RTRIM(CHAR(INT(GRADE))) || '学年' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //科目
    function getMockSubclass() {

        $query  = " SELECT ";
        $query .= "     MOCK_SUBCLASS_CD AS VALUE, ";
        $query .= "     SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     MOCK_SUBCLASS_CD ";

        return $query;
    }

    //段階数
    function getAssessLevelCnt($model) {

        $query .= " SELECT ";
        $query .= "     MAX(SEQ) AS MAX_SEQ ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_ASSESS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '".$model->mockSubclassCd."' ";

        return $query;
    }

    //評定マスタ情報抽出
    function selectQuery($model)
    {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_ASSESS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '".$model->mockSubclassCd."' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //Insert作成
    function getInsertQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM MOCK_SUBCLASS_ASSESS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '".$model->mockSubclassCd."' ";

        $db->query($query);

        $data = array();
        for ($i = 1; $i <= $model->level; $i++) {
            $data["YEAR"][TEXT]             = $model->year;
            $data["GRADE"][TEXT]            = $model->grade;
            $data["MOCK_SUBCLASS_CD"][TEXT] = $model->mockSubclassCd;
            $data["SEQ"][NUMBER]            = $i;
            $data["ASSESSMARK"][TEXT]       = $model->field["ASSESSMARK".$i];
            $data["ASSESSLOW"][NUMBER]      = $model->field["ASSESSLOW".$i];

            if ($i == "1") {
                $data["ASSESSHIGH"][NUMBER]     = $model->field["ASSESSHIGH1"];
            } else {
                $ASSESSHIGH = "ASSESSLOW".($i - 1);
                $data["ASSESSHIGH"][NUMBER]     = ($model->field[$ASSESSHIGH] - 1);
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "MOCK_SUBCLASS_ASSESS_DAT");

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //COPY作成
    function getCopyQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM MOCK_SUBCLASS_ASSESS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";

        $db->query($query);

        $query  = " INSERT INTO MOCK_SUBCLASS_ASSESS_DAT ";
        $query .= " SELECT ";
        $query .= "     '".$model->year."', ";
        $query .= "     GRADE, ";
        $query .= "     MOCK_SUBCLASS_CD, ";
        $query .= "     SEQ, ";
        $query .= "     ASSESSMARK, ";
        $query .= "     ASSESSLOW, ";
        $query .= "     ASSESSHIGH, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_ASSESS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->copyYear."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
