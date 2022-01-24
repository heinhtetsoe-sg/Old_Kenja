<?php

require_once('for_php7.php');

class knjz201query extends Query {
    function getSchoolMst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //更新
    function executeQuery($risyu_kijun, $syutoku_kijun, $knjSchoolMst, $model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //上限値の算出方法（初期値：3.切り捨て）
        // 1.四捨五入・・・小数点第1位を四捨五入・・・ROUND(XXX, 0)
        // 2.切り上げ・・・小数点第1位を切り上げ・・・CEIL(XXX)
        // 3.切り捨て・・・小数点第1位を切り捨て・・・FLOOR(XXX)
        // 4.実数・・・・・小数点第2位を四捨五入・・・ROUND(XXX * 10, 0) / 10
        $sKeisan = "FLOOR(";
        $eKeisan = ")";
        if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "1") {
            $sKeisan = "ROUND(";
            $eKeisan = ", 0)";
        } else if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "2") {
            $sKeisan = "CEIL(";
            $eKeisan = ")";
        } else if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "3") {
            $sKeisan = "FLOOR(";
            $eKeisan = ")";
        } else if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "4") {
            $sKeisan = "ROUND(";
            $eKeisan = " * 10, 0) / 10";
        }

        $query  = " UPDATE ";
        $query .= "     CREDIT_MST ";
        $query .= " SET ";
        $query .= "     ABSENCE_HIGH     = {$sKeisan}CREDITS * {$risyu_kijun}  {$eKeisan}, ";
        $query .= "     GET_ABSENCE_HIGH = {$sKeisan}CREDITS * {$syutoku_kijun}{$eKeisan} ";
        if ($model->field["ABSENCE_WARN_CHECK"] == "on" && strlen($model->field["ABSENCE_WARN"])) {
            $query .= " ,ABSENCE_WARN     = {$model->field["ABSENCE_WARN"]} ";
        }
        if ($model->field["ABSENCE_WARN_CHECK2"] == "on" && strlen($model->field["ABSENCE_WARN2"])) {
            $query .= " ,ABSENCE_WARN2     = {$model->field["ABSENCE_WARN2"]} ";
        }
        if ($model->field["ABSENCE_WARN_CHECK3"] == "on" && strlen($model->field["ABSENCE_WARN3"])) {
            $query .= " ,ABSENCE_WARN3     = {$model->field["ABSENCE_WARN3"]} ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //更新
    function executeQuerySpecial($risyu_kijun_special, $syutoku_kijun_special, $knjSchoolMst, $model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //上限値の算出方法（初期値：3.切り捨て）
        // 1.四捨五入・・・小数点第1位を四捨五入・・・ROUND(XXX, 0)
        // 2.切り上げ・・・小数点第1位を切り上げ・・・CEIL(XXX)
        // 3.切り捨て・・・小数点第1位を切り捨て・・・FLOOR(XXX)
        // 4.実数・・・・・小数点第2位を四捨五入・・・ROUND(XXX * 10, 0) / 10
        $sKeisan = "FLOOR(";
        $eKeisan = ")";
        if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "1") {
            $sKeisan = "ROUND(";
            $eKeisan = ", 0)";
        } else if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "2") {
            $sKeisan = "CEIL(";
            $eKeisan = ")";
        } else if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "3") {
            $sKeisan = "FLOOR(";
            $eKeisan = ")";
        } else if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "4") {
            $sKeisan = "ROUND(";
            $eKeisan = " * 10, 0) / 10";
        }


        $query  = " UPDATE ";
        $query .= "     CREDIT_SPECIAL_MST ";
        $query .= " SET ";
        $query .= "     ABSENCE_HIGH     = {$sKeisan}CREDITS * {$risyu_kijun_special}  {$eKeisan}, ";
        $query .= "     GET_ABSENCE_HIGH = {$sKeisan}CREDITS * {$syutoku_kijun_special}{$eKeisan} ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND CREDITS IS NOT NULL ";

        $db->query($query);

        if ($model->field["ABSENCE_WARN_CHECK"] == "on" && strlen($model->field["ABSENCE_WARN"])) {
            $query  = " UPDATE ";
            $query .= "     CREDIT_SPECIAL_MST ";
            $query .= " SET ";
            $query .= "     ABSENCE_WARN     = {$model->field["ABSENCE_WARN"]} ";
            $query .= " WHERE ";
            $query .= "     YEAR = '". CTRL_YEAR ."' ";

            $db->query($query);
        }

        if ($model->field["ABSENCE_WARN_CHECK2"] == "on" && strlen($model->field["ABSENCE_WARN2"])) {
            $query  = " UPDATE ";
            $query .= "     CREDIT_SPECIAL_MST ";
            $query .= " SET ";
            $query .= "     ABSENCE_WARN2     = {$model->field["ABSENCE_WARN2"]} ";
            $query .= " WHERE ";
            $query .= "     YEAR = '". CTRL_YEAR ."' ";

            $db->query($query);
        }

        if ($model->field["ABSENCE_WARN_CHECK3"] == "on" && strlen($model->field["ABSENCE_WARN3"])) {
            $query  = " UPDATE ";
            $query .= "     CREDIT_SPECIAL_MST ";
            $query .= " SET ";
            $query .= "     ABSENCE_WARN3     = {$model->field["ABSENCE_WARN3"]} ";
            $query .= " WHERE ";
            $query .= "     YEAR = '". CTRL_YEAR ."' ";

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //存在SQL(SPECIAL_GROUP_CD = '999')
    function getExistsQuerySpecial999($row, $where) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CREDIT_SPECIAL_MST ";
        $query .= $where;
        return $query;
    }

    //追加SQL(SPECIAL_GROUP_CD = '999')
    function getInsertQuerySpecial999($model) {
        $query  = " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECODE, ";
        $query .= "     '999' AS SPECIAL_GROUP_CD, ";
        $query .= "     CASE WHEN 1000 <= SUM(ABSENCE_HIGH) THEN 999.9 ELSE SUM(ABSENCE_HIGH) END AS ABSENCE_HIGH ";
        $query .= " FROM ";
        $query .= "     CREDIT_SPECIAL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND SPECIAL_GROUP_CD NOT IN ('999') ";
        $query .= " GROUP BY ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECODE ";
        return $query;
    }

    //更新・追加(SPECIAL_GROUP_CD = '999')
    function executeQuerySpecial999($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //追加(ABSENCE_HIGH)
        $query  = knjz201Query::getInsertQuerySpecial999($model);
        $result = $db->query($query); 
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["COURSECD"][TEXT]         = $row["COURSECD"];
            $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
            $data["GRADE"][TEXT]            = $row["GRADE"];
            $data["COURSECODE"][TEXT]       = $row["COURSECODE"];
            $data["SPECIAL_GROUP_CD"][TEXT] = $row["SPECIAL_GROUP_CD"];
            $data["ABSENCE_HIGH"][NUMBER]   = $row["ABSENCE_HIGH"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            //存在
            //条件
            $where  = " WHERE ";
            $where .= "     YEAR = '". CTRL_YEAR ."' ";
            $where .= "     AND COURSECD = '". $row["COURSECD"] ."' ";
            $where .= "     AND MAJORCD = '". $row["MAJORCD"] ."' ";
            $where .= "     AND GRADE = '". $row["GRADE"] ."' ";
            $where .= "     AND COURSECODE = '". $row["COURSECODE"] ."' ";
            $where .= "     AND SPECIAL_GROUP_CD = '999' ";
            $query  = knjz201Query::getExistsQuerySpecial999($row, $where);
            if (0 < $db->getOne($query)) {
                //更新
                $query = Query::updateSQL($data, "CREDIT_SPECIAL_MST", $where);
                $db->query($query);
            } else {
                //追加
                $query = Query::insertSQL($data, "CREDIT_SPECIAL_MST");
                $db->query($query);
            }
        }

        //更新(ABSENCE_WARN)
        if ($model->field["ABSENCE_WARN_CHECK"] == "on" && strlen($model->field["ABSENCE_WARN"])) {
            $query  = " UPDATE ";
            $query .= "     CREDIT_SPECIAL_MST ";
            $query .= " SET ";
            $query .= "     ABSENCE_WARN     = {$model->field["ABSENCE_WARN"]} ";
            $query .= " WHERE ";
            $query .= "     YEAR = '". CTRL_YEAR ."' ";
            $query .= "     AND SPECIAL_GROUP_CD = '999' ";
            $db->query($query);
        }
        if ($model->field["ABSENCE_WARN_CHECK2"] == "on" && strlen($model->field["ABSENCE_WARN2"])) {
            $query  = " UPDATE ";
            $query .= "     CREDIT_SPECIAL_MST ";
            $query .= " SET ";
            $query .= "     ABSENCE_WARN2     = {$model->field["ABSENCE_WARN2"]} ";
            $query .= " WHERE ";
            $query .= "     YEAR = '". CTRL_YEAR ."' ";
            $query .= "     AND SPECIAL_GROUP_CD = '999' ";
            $db->query($query);
        }
        if ($model->field["ABSENCE_WARN_CHECK3"] == "on" && strlen($model->field["ABSENCE_WARN3"])) {
            $query  = " UPDATE ";
            $query .= "     CREDIT_SPECIAL_MST ";
            $query .= " SET ";
            $query .= "     ABSENCE_WARN3     = {$model->field["ABSENCE_WARN3"]} ";
            $query .= " WHERE ";
            $query .= "     YEAR = '". CTRL_YEAR ."' ";
            $query .= "     AND SPECIAL_GROUP_CD = '999' ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //既に登録されているデータがあるのか カウントで確かめる
    function countGetAbsenceHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND (ABSENCE_HIGH IS NOT NULL ";
        $query .= "     OR  GET_ABSENCE_HIGH IS NOT NULL) ";

        return $query;
    }

    //既に登録されているデータがあるのか カウントで確かめる
    function countGetAbsenceHighSpecial() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CREDIT_SPECIAL_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND (ABSENCE_HIGH IS NOT NULL ";
        $query .= "     OR  GET_ABSENCE_HIGH IS NOT NULL) ";

        return $query;
    }

    //既に登録されているデータがあるのか カウントで確かめる(欠課数オーバー)
    function countGetAbsenceWarn($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND ( ";
        $query .= "                ABSENCE_WARN  IS NOT NULL ";
        if (in_array("2", $model->control["SEMESTER"])) {
            $query .= "         OR ABSENCE_WARN2 IS NOT NULL ";
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $query .= "         OR ABSENCE_WARN3 IS NOT NULL ";
        }
        $query .= "         ) ";

        return $query;
    }

    //既に登録されているデータがあるのか カウントで確かめる(欠課数オーバー)
    function countGetAbsenceWarnSpecial($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CREDIT_SPECIAL_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND ( ";
        $query .= "                ABSENCE_WARN  IS NOT NULL ";
        if (in_array("2", $model->control["SEMESTER"])) {
            $query .= "         OR ABSENCE_WARN2 IS NOT NULL ";
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $query .= "         OR ABSENCE_WARN3 IS NOT NULL ";
        }
        $query .= "         ) ";

        return $query;
    }

    //SCHOOL_DETAIL_DATにデータが登録されているのかどうかをチェックする
    function checkDetailDat() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND JITU_JIFUN IS NOT NULL ";

        return $query;
    }

    //一単位あたりの欠課次数の上限値(履修)
    function getRisyuKijun() {
        $query  = " SELECT ";
        $query .= "     DECIMAL(INT(JITU_SYUSU) * INT(RISYU_BUNSI)) / INT(RISYU_BUNBO) ";//INTだと整数なのでDECIMALにする
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //一単位あたりの欠課次数の上限値(修得)
    function getSyutokuKijun() {
        $query  = " SELECT ";
        $query .= "     DECIMAL(INT(JITU_SYUSU) * INT(SYUTOKU_BUNSI)) / INT(SYUTOKU_BUNBO) ";//INTだと整数なのでDECIMALにする
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //一単位あたりの欠課次数の上限値(履修)
    function getRisyuKijunSpecial() {
        $query  = " SELECT ";
        $query .= "     DECIMAL(INT(JITU_SYUSU) * INT(RISYU_BUNSI_SPECIAL)) / INT(RISYU_BUNBO_SPECIAL) ";//INTだと整数なのでDECIMALにする
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //一単位あたりの欠課次数の上限値(修得)
    function getSyutokuKijunSpecial() {
        $query  = " SELECT ";
        $query .= "     DECIMAL(INT(JITU_SYUSU) * INT(SYUTOKU_BUNSI_SPECIAL)) / INT(SYUTOKU_BUNBO_SPECIAL) ";//INTだと整数なのでDECIMALにする
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }
}
?>