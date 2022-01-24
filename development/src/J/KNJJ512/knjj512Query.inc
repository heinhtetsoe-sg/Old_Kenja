<?php
class knjj512Query extends Query
{

    //左フレームのリスト
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SPORTS_TOTAL_VALUE_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " AND SEX  = '9' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     AGE, ";
        $query .= "     TOTAL_LEVEL DESC ";
        return $query;
    }

    //SPORTS_TOTAL_VALUE_BASE_MSTから1レコード取得
    public function getSportsTotalValueBaseMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.AGE, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.TOTAL_LEVEL, ";
        $query .= "     LA.TOTAL_SCORE_HIGH AS TOTAL_SCORE_HIGH_A, ";
        $query .= "     LB.TOTAL_SCORE_LOW AS TOTAL_SCORE_LOW_B, ";
        $query .= "     LB.TOTAL_SCORE_HIGH AS TOTAL_SCORE_HIGH_B, ";
        $query .= "     LC.TOTAL_SCORE_LOW AS TOTAL_SCORE_LOW_C, ";
        $query .= "     LC.TOTAL_SCORE_HIGH AS TOTAL_SCORE_HIGH_C, ";
        $query .= "     LD.TOTAL_SCORE_LOW AS TOTAL_SCORE_LOW_D, ";
        $query .= "     LD.TOTAL_SCORE_HIGH AS TOTAL_SCORE_HIGH_D, ";
        $query .= "     LE.TOTAL_SCORE_LOW AS TOTAL_SCORE_LOW_E ";
        $query .= " FROM ";
        $query .= "     SPORTS_TOTAL_VALUE_BASE_MST T1 ";
        $query .= "     LEFT JOIN SPORTS_TOTAL_VALUE_BASE_MST LA ";
        $query .= "            ON LA.YEAR         = T1.YEAR ";
        $query .= "           AND LA.AGE          = T1.AGE ";
        $query .= "           AND LA.SEX          = T1.SEX ";
        $query .= "           AND LA.TOTAL_LEVEL  = '5' ";
        $query .= "     LEFT JOIN SPORTS_TOTAL_VALUE_BASE_MST LB ";
        $query .= "            ON LB.YEAR         = T1.YEAR ";
        $query .= "           AND LB.AGE          = T1.AGE ";
        $query .= "           AND LB.SEX          = T1.SEX ";
        $query .= "           AND LB.TOTAL_LEVEL  = '4' ";
        $query .= "     LEFT JOIN SPORTS_TOTAL_VALUE_BASE_MST LC ";
        $query .= "            ON LC.YEAR         = T1.YEAR ";
        $query .= "           AND LC.AGE          = T1.AGE ";
        $query .= "           AND LC.SEX          = T1.SEX ";
        $query .= "           AND LC.TOTAL_LEVEL  = '3' ";
        $query .= "     LEFT JOIN SPORTS_TOTAL_VALUE_BASE_MST LD ";
        $query .= "            ON LD.YEAR         = T1.YEAR ";
        $query .= "           AND LD.AGE          = T1.AGE ";
        $query .= "           AND LD.SEX          = T1.SEX ";
        $query .= "           AND LD.TOTAL_LEVEL  = '2' ";
        $query .= "     LEFT JOIN SPORTS_TOTAL_VALUE_BASE_MST LE ";
        $query .= "            ON LE.YEAR         = T1.YEAR ";
        $query .= "           AND LE.AGE          = T1.AGE ";
        $query .= "           AND LE.SEX          = T1.SEX ";
        $query .= "           AND LE.TOTAL_LEVEL  = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " AND T1.AGE  = '".$model->field["AGE"]."' ";
        $query .= " AND T1.SEX  = '9' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TOTAL_LEVEL DESC ";
        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($this->totalMark as $lebel => $mark) {
            $low  = "TOTAL_SCORE_LOW_".$mark;
            $high = "TOTAL_SCORE_HIGH_".$mark;
            $data["YEAR"][TEXT]                 = $model->year;
            $data["AGE"][TEXT]                  = $model->field["AGE"];
            $data["SEX"][TEXT]                  = "9";
            $data["TOTAL_LEVEL"][TEXT]          = $lebel;
            $data["TOTAL_MARK"][TEXT]           = $mark;
            $data["TOTAL_SCORE_LOW"][NUMBER]    = $model->field[$low];
            $data["TOTAL_SCORE_HIGH"][NUMBER]   = $model->field[$high];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";
            $query = Query::insertSQL($data, "SPORTS_TOTAL_VALUE_BASE_MST");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($this->totalMark as $lebel => $mark) {
            $low  = "TOTAL_SCORE_LOW_".$mark;
            $high = "TOTAL_SCORE_HIGH_".$mark;
            $data["TOTAL_MARK"][TEXT]           = $mark;
            $data["TOTAL_SCORE_LOW"][NUMBER]    = $model->field[$low];
            $data["TOTAL_SCORE_HIGH"][NUMBER]   = $model->field[$high];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";
            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->year."' ";
            $where .= " AND AGE         = '".$model->field["AGE"]."' ";
            $where .= " AND SEX         = '9' ";
            $where .= " AND TOTAL_LEVEL = '".$lebel."' ";
    
            $query = Query::updateSQL($data, "SPORTS_TOTAL_VALUE_BASE_MST", $where);
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     SPORTS_TOTAL_VALUE_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' ";
        $query .= " AND AGE         = '".$model->field["AGE"]."' ";
        $query .= " AND SEX         = '9' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
    }
}
