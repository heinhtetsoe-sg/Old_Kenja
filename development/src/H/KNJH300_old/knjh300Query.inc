<?php

require_once('for_php7.php');

class knjh300Query extends Query {

    //生徒情報取得
    function getStudentData($schregno)
    {
        $query  = "SELECT ";
        $query .= "    L2.HR_NAME, ";
        $query .= "    T1.ATTENDNO, ";
        $query .= "    T1.ANNUAL, ";
        $query .= "    T1.COURSECD || T1.MAJORCD || '：' || L4.COURSENAME || L4.MAJORABBV AS COURSEMAJOR_NAME, ";
        $query .= "    T1.COURSECODE || '：' || L3.COURSECODENAME AS COURSE_NAME, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    L1.NAME, ";
        $query .= "    L1.SEX || '：' || N1.NAME1 AS SEX, ";
        $query .= "    L1.NAME_KANA, ";
        $query .= "    L1.NAME_ENG, ";
        $query .= "    L5.CLUBNAME, ";
        $query .= "    L6.COMMITTEENAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "         AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND L2.GRADE = T1.GRADE ";
        $query .= "         AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "    LEFT JOIN COURSECODE_MST L3 ON L3.COURSECODE = T1.COURSECODE ";
        $query .= "    LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' ";
        $query .= "         AND N1.NAMECD2 = L1.SEX ";
        $query .= "    LEFT JOIN V_COURSE_MAJOR_MST L4 ON L4.YEAR = T1.YEAR ";
        $query .= "         AND L4.COURSECD = T1.COURSECD ";
        $query .= "         AND L4.MAJORCD = T1.MAJORCD ";
        $query .= "    LEFT JOIN (SELECT ";
        $query .= "                   T1.SCHREGNO, ";
        $query .= "                   T1.CLUBCD, ";
        $query .= "                   L1.CLUBNAME ";
        $query .= "               FROM ";
        $query .= "                   SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "                   LEFT JOIN CLUB_MST L1 ON T1.CLUBCD = L1.CLUBCD, ";
        $query .= "                   (SELECT ";
        $query .= "                        SCHREGNO, ";
        $query .= "                        MAX(SDATE) AS SDATE ";
        $query .= "                    FROM ";
        $query .= "                        SCHREG_CLUB_HIST_DAT ";
        $query .= "                    WHERE ";
        $query .= "                        SCHREGNO = '".$schregno."' ";
        $query .= "                        AND EDATE IS NULL ";
        $query .= "                    GROUP BY ";
        $query .= "                        SCHREGNO ";
        $query .= "                   ) T2 ";
        $query .= "               WHERE ";
        $query .= "                   T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                   AND T1.SDATE = T2.SDATE ";
        $query .= "               GROUP BY ";
        $query .= "                   T1.SCHREGNO, ";
        $query .= "                   T1.CLUBCD, ";
        $query .= "                   L1.CLUBNAME ";
        $query .= "              ) L5 ON L5.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN (SELECT ";
        $query .= "                   T1.SCHREGNO, ";
        $query .= "                   T1.COMMITTEECD, ";
        $query .= "                   L1.COMMITTEENAME ";
        $query .= "               FROM ";
        $query .= "                   SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "                   LEFT JOIN COMMITTEE_MST L1 ON T1.COMMITTEE_FLG = L1.COMMITTEE_FLG AND T1.COMMITTEECD = L1.COMMITTEECD, ";
        $query .= "                   (SELECT ";
        $query .= "                       SCHREGNO, ";
        $query .= "                       MAX(SEQ) AS SEQ ";
        $query .= "                   FROM ";
        $query .= "                       SCHREG_COMMITTEE_HIST_DAT ";
        $query .= "                   WHERE ";
        $query .= "                       YEAR = '".CTRL_YEAR."' ";
        $query .= "                       AND SCHREGNO = '".$schregno."' ";
        $query .= "                   GROUP BY ";
        $query .= "                       SCHREGNO ";
        $query .= "                   ) T2 ";
        $query .= "               WHERE ";
        $query .= "                   YEAR = '".CTRL_YEAR."' ";
        $query .= "                   AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                   AND T1.SEQ = T2.SEQ ";
        $query .= "               GROUP BY ";
        $query .= "                   T1.SCHREGNO, ";
        $query .= "                   T1.COMMITTEECD, ";
        $query .= "                   L1.COMMITTEENAME ";
        $query .= "              ) L6 ON L6.SCHREGNO = T1.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$schregno."' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //行動の記録取得
    function getActionDuc($model, $order)
    {
        $query  = "SELECT ";
        $query .= "    T1.STAFFCD || '　' || L1.STAFFNAME AS STAFFNAME, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.ACTIONTIME, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.TITLE, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.SEQ ";
        $query .= "FROM ";
        $query .= "    ACTION_DOCUMENT_DAT T1 ";
        $query .= "    LEFT JOIN V_STAFF_MST L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND L1.STAFFCD = T1.STAFFCD ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$model->schregno."' ";
        if (AUTHORITY != DEF_UPDATABLE &&
            $model->auth["CHAIRFLG"] == "OFF" &&
            $model->auth["CLUBFLG"] == "OFF" &&
            $model->auth["HRCLASSFLG"] == "OFF")
        {
            $query .= "    AND T1.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    ".$order." ";
        $query .= "    T1.SEQ ";

        return $query;
    }

    //権限(受講講座)
    function getAuthChair($schregno)
    {
        $query  = "SELECT ";
        $query .= "    CASE WHEN COUNT(*) > 0 THEN 'ON' ELSE 'OFF' END AS CHAIRFLG ";
        $query .= "FROM ";
        $query .= "    CHAIR_STD_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T1.CHAIRCD IN (SELECT ";
        $query .= "                           T2.CHAIRCD ";
        $query .= "                       FROM ";
        $query .= "                           CHAIR_STF_DAT T2 ";
        $query .= "                       WHERE ";
        $query .= "                           T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "                           AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                           AND STAFFCD = '".STAFFCD."' ";
        $query .= "                       ) ";
        $query .= "    AND T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //権限(クラブ)
    function getAuthClub($schregno)
    {
        $query  = "SELECT ";
        $query .= "    CASE WHEN COUNT(*) > 0 THEN 'ON' ELSE 'OFF' END AS CLUBFLG ";
        $query .= "FROM ";
        $query .= "    SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.CLUBCD IN (SELECT ";
        $query .= "                      CLUBCD ";
        $query .= "                  FROM ";
        $query .= "                      CLUB_YDAT ";
        $query .= "                  WHERE ";
        $query .= "                      YEAR = '".CTRL_YEAR."' ";
        $query .= "                 ) ";
        $query .= "    AND T1.CLUBCD IN (SELECT ";
        $query .= "                          CLUBCD ";
        $query .= "                      FROM ";
        $query .= "                          CLUB_ADVISER_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          YEAR = '".CTRL_YEAR."' ";
        $query .= "                          AND ADVISER = '".STAFFCD."' ";
        $query .= "                     ) ";
        $query .= "    AND T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //権限(担任)
    function getAuthHrclass($schregno)
    {
        $query  = "SELECT ";
        $query .= "    CASE WHEN COUNT(*) > 0 THEN 'ON' ELSE 'OFF' END AS HRCLASSFLG ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$schregno."' ";
        $query .= "    AND YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND VALUE(T1.GRADE, '00') || VALUE(T1.HR_CLASS, '000') ";
        $query .= "        IN (SELECT ";
        $query .= "                VALUE(T2.GRADE, '00') || VALUE(T2.HR_CLASS, '000') ";
        $query .= "            FROM ";
        $query .= "                SCHREG_REGD_HDAT T2 ";
        $query .= "            WHERE ";
        $query .= "                T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "                AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                AND (T2.TR_CD1 = '".STAFFCD."' ";
        $query .= "                     OR T2.TR_CD2 = '".STAFFCD."' ";
        $query .= "                     OR T2.TR_CD3 = '".STAFFCD."' ";
        $query .= "                     OR T2.SUBTR_CD1 = '".STAFFCD."' ";
        $query .= "                     OR T2.SUBTR_CD2 = '".STAFFCD."' ";
        $query .= "                     OR T2.SUBTR_CD3 = '".STAFFCD."')) ";

        return $query;
    }

    //権限(進路担当)
    function getAuthCourse()
    {
        $query .= "SELECT ";
        $query .= "    CASE WHEN COUNT(*) > 0 THEN 'ON' ELSE 'OFF' END AS COURSEFLG ";
        $query .= "FROM ";
        $query .= "    USERGROUP_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND GROUPCD = '0009' ";
        $query .= "    AND STAFFCD = '".STAFFCD."' ";

        return $query;
    }

    //行動の記録取得
    function getActionDucSub($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.STAFFCD || '　' || L1.STAFFNAME AS STAFFNAME, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.ACTIONTIME, ";
        $query .= "    T1.DIVIDECD, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.TITLE, ";
        $query .= "    T1.TEXT, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.SEQ ";
        $query .= "FROM ";
        $query .= "    ACTION_DOCUMENT_DAT T1 ";
        $query .= "    LEFT JOIN V_STAFF_MST L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND L1.STAFFCD = T1.STAFFCD ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$model->schregno."' ";
        $query .= "    AND ACTIONDATE = '".str_replace("/", "-", $model->actiondate)."' ";
        $query .= "    AND SEQ = ".$model->seq." ";
        $query .= "ORDER BY ";
        $query .= "    T1.ACTIONDATE DESC, ";
        $query .= "    T1.ACTIONTIME DESC, ";
        $query .= "    T1.SEQ ";

        return $query;
    }

    //生徒情報取得
    function getName($schregno)
    {
        $query  = "SELECT ";
        $query .= "    NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1, $namecd2 = "")
    {
        $query  = "SELECT ";
        $query .= "    NAME1 AS LABEL, ";
        $query .= "    NAMECD2 AS VALUE ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "    AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    NAMECD2 ";

        return $query;
    }

    /**
     * 回数取得
     */
    function getMaxSeq($schregno)
    {
        $query  = "SELECT ";
        $query .= "    SCHREGNO, ";
        $query .= "    MAX(SEQ) + 1 AS SEQ ";
        $query .= "FROM ";
        $query .= "    ACTION_DOCUMENT_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";
        $query .= "GROUP BY ";
        $query .= "    SCHREGNO ";

        return $query;
    }

    /**
     * 更新
     */
    function &updateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHREGNO"][TEXT]               = $model->schregno;
        $data["ACTIONDATE"][TEXT]             = str_replace("/", "-", $model->actiondate);
        $data["SEQ"][NUMBER]                  = $model->seq;
        $data["ACTIONTIME"][TEXT]             = $model->actiontime;
        $data["DIVIDECD"][TEXT]               = $model->dividecd;
        $data["TITLE"][TEXT]                  = $model->title;
        $data["TEXT"][TEXT]                   = $model->text;
        $data["REGISTERCD"][TEXT]             = STAFFCD;
        $data["UPDATED"][FUNC]                = "sysdate()";

        $where  = " WHERE SCHREGNO = '".$model->schregno."'";
        $where .= "       AND ACTIONDATE = '".str_replace("/", "-", $model->actiondateUpd)."'";
        $where .= "       AND SEQ = ".$model->seq." ";

        $query = Query::updateSQL($data, "ACTION_DOCUMENT_DAT", $where);
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 新規
     */
    function &insertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHREGNO"][TEXT]               = $model->schregno;
        $data["ACTIONDATE"][TEXT]             = str_replace("/", "-", $model->actiondate);
        $data["SEQ"][NUMBER]                  = $model->seq;
        $data["ACTIONTIME"][TEXT]             = $model->actiontime;
        $data["STAFFCD"][TEXT]                = STAFFCD;
        $data["DIVIDECD"][TEXT]               = $model->dividecd;
        $data["TITLE"][TEXT]                  = $model->title;
        $data["TEXT"][TEXT]                   = $model->text;
        $data["REGISTERCD"][TEXT]             = STAFFCD;
        $data["UPDATED"][FUNC]                = "sysdate()";

        $query = Query::insertSQL($data, "ACTION_DOCUMENT_DAT");
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 削除
     */
    function &getDeleteQuery($delvalue)
    {

        $query  = "DELETE FROM ";
        $query .= "    ACTION_DOCUMENT_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$delvalue[0]."' ";
        $query .= "    AND ACTIONDATE = '".str_replace("/", "-", $delvalue[1])."' ";
        $query .= "    AND SEQ = ".$delvalue[2]." ";

        return $query;
    }
}
?>
