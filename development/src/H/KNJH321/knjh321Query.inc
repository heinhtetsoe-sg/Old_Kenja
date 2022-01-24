<?php

require_once('for_php7.php');

class knjh321Query extends Query {

    //生徒情報取得
    function getName($schregno)
    {
        $query  = "SELECT ";
        $query .= "    L2.HR_NAME, ";
        $query .= "    L1.ATTENDNO, ";
        $query .= "    T1.NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND L1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = L1.YEAR ";
        $query .= "         AND L2.SEMESTER = L1.SEMESTER ";
        $query .= "         AND L2.GRADE = L1.GRADE ";
        $query .= "         AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //模試目標値取得
    function getMockTarget($model, $target1Cd1, $target1Cd2)
    {
        $query  = "SELECT ";
        $query .= "    T1.TARGET_DIV || ',' || T1.MOCK_TARGET_CD || ',' || L1.STF_AUTH_CD AS VALUE, ";
        $query .= "    CASE WHEN T1.TARGET_DIV = '1' ";
        $query .= "         THEN '利用者：' || L1.TARGETNAME1 ";
        $query .= "         ELSE 'グループ：' || L1.TARGETNAME1 END AS LABEL ";
        $query .= "FROM ";
        $query .= "    MOCK_GROUP_DAT T1 ";
        $query .= "    LEFT JOIN MOCK_TARGET_MST L1 ON L1.TARGET_DIV = T1.TARGET_DIV ";
        $query .= "         AND L1.STF_AUTH_CD = T1.STF_AUTH_CD ";
        $query .= "         AND L1.TARGETCD = T1.MOCK_TARGET_CD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.GROUP_DIV = '".$model->group_div."' ";
        $query .= "    AND T1.TARGET_DIV = '".$model->group_div."' ";
        $query .= "    AND T1.STF_AUTH_CD = '".$model->stf_auth_cd."' ";
        $query .= "    AND T1.GROUPCD = '".$model->groupcd."' ";
        $query .= "    AND T1.MOCK_TARGET_CD NOT IN ('".$target1Cd1."','".$target1Cd2."') ";
        $query .= "ORDER BY ";
        $query .= "    VALUE, ";
        $query .= "    LABEL ";

        return $query;
    }

    //模試データMAIN
    function getMockMain($model)
    {
        $query  = "FROM ";
        $query .= "    MOCK_DAT T1 ";
        $query .= "    LEFT JOIN MOCK_MST L1 ON L1.MOCKCD = T1.MOCKCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR <= '".CTRL_YEAR."' ";
        $query .= "    AND T1.MOCKCD IN (SELECT ";
        $query .= "                          I1.MOCK_TARGET_CD ";
        $query .= "                      FROM ";
        $query .= "                          MOCK_GROUP_DAT I1 ";
        $query .= "                      WHERE ";
        $query .= "                          I1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                          AND I1.GROUP_DIV = '".$model->group_div."' ";
        $query .= "                          AND I1.TARGET_DIV = '0' ";
        $query .= "                          AND I1.STF_AUTH_CD = '".$model->stf_auth_cd."' ";
        $query .= "                          AND I1.GROUPCD = '".$model->groupcd."' ";
        $query .= "                      ) ";
        $query .= "    AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ";
        return $query;
    }

    //模試データ取得
    function getMockData($model)
    {
        $query  = "SELECT ";
        $query .= "    MAX(T1.YEAR) AS YEAR, ";
        $query .= "    T1.MOCKCD AS VALUE, ";
        $query .= "    L1.MOCKNAME3 AS LABEL ";
        //模試データMAIN
        $query .= knjh321Query::getMockMain($model);

        $query .= "GROUP BY ";
        $query .= "    T1.MOCKCD, ";
        $query .= "    L1.MOCKNAME3 ";
        $query .= "ORDER BY ";
        $query .= "    YEAR, ";
        $query .= "    T1.MOCKCD, ";
        $query .= "    L1.MOCKNAME3 ";

        return $query;
    }

    //目標値取得
    function getTargetScore($target_div, $stf_auth_cd, $targetcd)
    {
        $query  = "SELECT ";
        $query .= "    T1.MOCK_SUBCLASS_CD, ";
        $query .= "    L1.SUBCLASS_ABBV, ";
        $query .= "    T1.DEVIATION ";
        $query .= "FROM ";
        $query .= "    MOCK_TARGET_DAT T1 ";
        $query .= "    LEFT JOIN MOCK_SUBCLASS_MST L1 ON L1.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
        $query .= "WHERE ";
        $query .= "    T1.TARGET_DIV = '".$target_div."' ";
        $query .= "    AND T1.STF_AUTH_CD = '".$stf_auth_cd."' ";
        $query .= "    AND T1.TARGETCD = '".$targetcd."' ";

        return $query;
    }
}
?>
