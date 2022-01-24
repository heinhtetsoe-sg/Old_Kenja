<?php

require_once('for_php7.php');

class knje372cQuery extends Query {

    //学級コンボ
    function getGradeHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE ";
        $query .= "     , T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN  SCHREG_REGD_GDAT T2";
        $query .= "         ON T2.YEAR  = T1.YEAR";
        $query .= "        AND T2.GRADE = T1.GRADE";
        $query .= "        AND T2.GRADE_CD = '03' ";
        $query .= "        AND T2.SCHOOL_KIND = 'H' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    function getNameMstE071() {
        $query  = "     SELECT ";
        $query .= "       NAMECD2 AS VALUE, ";
        $query .= "       NAMECD2 || ':' || value(NAME1, '') AS LABEL ";
        $query .= "     FROM ";
        $query .= "       V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND NAMECD1 = 'E071' ";
        $query .= "     ORDER BY ";
        $query .= "       VALUE ";

        return $query;
    }

    //一覧取得
    function getList($model)
    {
        $query  = "   SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.PROFICIENCY1_AVG, ";
        $query .= "     T2.PROFICIENCY2_AVG, ";
        $query .= "     T3.NAME, ";
        $query .= "     T4.ACTIVITY_CD, ";
        $query .= "     T4.ACTIVITY_CONTENT, ";
        $query .= "     T4.DECLINE_FLG, ";
        $query .= "     T5.HOPE_ORDER, ";
        $query .= "     T5.DEPARTMENT_CD ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN AFT_SCHREG_CONVERT_SCORE_DAT T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "       ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN AFT_SCHREG_RECOMMENDATION_INFO_DAT T4 ";
        $query .= "       ON T4.YEAR = T1.YEAR ";
        $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN AFT_SCHREG_HOPE_DEPARTMENT T5 ";
        $query .= "       ON T5.YEAR = T1.YEAR ";
        $query .= "       AND T5.SCHREGNO = T1.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "   ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T5.HOPE_ORDER ";

        return $query;
    }

    //推薦枠マスタカウント
    function getRecLimitCnt()
    {

        $query  = "   SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= "   FROM ";
        $query .= "     AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //推薦枠マスタ登録済み学科CD(専願/併願全て)取得
    function getRecLimitDepartmentCd($db)
    {
        $query = "   WITH REC_LIMIT AS ( ";
        $query .= "   SELECT ";
        $query .= "     * ";
        $query .= "   FROM ";
        $query .= "     AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "   ) ";
        $query .= "   SELECT DEPARTMENT_S AS DEPARTMENTCD FROM REC_LIMIT ";
        $query .= "   UNION ";
        $query .= "   SELECT DEPARTMENT_H AS DEPARTMENTCD FROM REC_LIMIT ";

        $departmentCdArray = array();
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $departmentCdArray[] = $row["DEPARTMENTCD"];
        }

        return $departmentCdArray;
    }

    //存在チェック --AFT_SCHREG_HOPE_DEPARTMENT
    function getExistsHopeOrderArray($db, $schregno) {
        $query  = "     SELECT ";
        $query .= "       HOPE_ORDER ";
        $query .= "     FROM ";
        $query .= "       AFT_SCHREG_HOPE_DEPARTMENT ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND SCHREGNO = '{$schregno}' ";

        $existsHoperOrderArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $existsHoperOrderArray[] = $row["HOPE_ORDER"];
        }

        return $existsHoperOrderArray;
    }

    //存在チェック --AFT_SCHREG_RECOMMENDATION_INFO_DAT
    function getExistsCntSchRecInfo($schregno) {
        $query  = "     SELECT ";
        $query .= "       COUNT(*) ";
        $query .= "     FROM ";
        $query .= "       AFT_SCHREG_RECOMMENDATION_INFO_DAT ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->schregnoList as $schregno) {

            //DELETE INSERT
            $query  = " DELETE FROM AFT_SCHREG_HOPE_DEPARTMENT ";
            $query .= " WHERE ";
            $query .= "         YEAR       = '".CTRL_YEAR."' ";
            $query .= "     AND SCHREGNO   = '{$schregno}' ";
            $db->query($query);

            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]      = $schregno;
            foreach ($model->schField[$schregno]["HOPE_ORDER"] as $hopeOrder => $departmentcd) {
                if ($departmentcd == "") continue; //学科番号が未登録の順位は追加しない

                $data["HOPE_ORDER"][TEXT]       = $hopeOrder;
                $data["DEPARTMENT_CD"][TEXT]    = $departmentcd;

                $query = Query::insertSQL($data, "AFT_SCHREG_HOPE_DEPARTMENT");
                $db->query($query);
            }

            //更新 
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["ACTIVITY_CD"][TEXT]      = $model->schField[$schregno]["ACTIVITY_CD"];
            $data["ACTIVITY_CONTENT"][TEXT] = $model->schField[$schregno]["ACTIVITY_CONTENT"];
            $data["DECLINE_FLG"][TEXT]      = $model->schField[$schregno]["DECLINE_FLG"];
            if ($db->getOne(knje372cQuery::getExistsCntSchRecInfo($schregno)) < 1) {
                //追加
                $query = Query::insertSQL($data, "AFT_SCHREG_RECOMMENDATION_INFO_DAT");
                $db->query($query);
            } else {
                //条件
                $where  = "WHERE   YEAR          = '".CTRL_YEAR."' ";
                $where .= "  AND   SCHREGNO      = '{$schregno}' ";

                //更新
                $query = Query::updateSQL($data, "AFT_SCHREG_RECOMMENDATION_INFO_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }


}
?>
