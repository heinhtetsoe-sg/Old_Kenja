<?php

require_once('for_php7.php');

class knjh400Query extends Query
{
    //校種取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR AND T1.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //項目取得
    public function getPortfolioHeadMst($model, $schoolkind)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PORTFOLIO_HEAD_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$schoolkind."' AND ";
        $query .= "     COURSECD    = ( SELECT ";
        $query .= "                         COURSECD ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR     = '".$model->year."' AND ";
        $query .= "                         SEMESTER = '".$model->semester."' AND ";
        $query .= "                         SCHREGNO = '".$model->schregno."' ";
        $query .= "                     ) AND ";
        $query .= "     SHOWORDER IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER ";

        return $query;
    }

    //生徒情報取得
    public function getStudentData($model, $schoolkind)
    {
        $query  = " WITH CLUB_HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLUBCD, ";
        $query .= "         L1.CLUBNAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "         INNER JOIN CLUB_MST L1 ";
        $query .= "              ON T1.CLUBCD       = L1.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             AND T1.SCHOOLCD     = L1.SCHOOLCD ";
            $query .= "             AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        }
        $query .= "        ,(SELECT ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             SCHOOLCD, ";
            $query .= "             SCHOOL_KIND, ";
        }
        $query .= "             SCHREGNO, ";
        $query .= "             MAX(VALUE(EDATE,'9999-12-31')) AS EDATE ";
        $query .= "         FROM ";
        $query .= "             SCHREG_CLUB_HIST_DAT ";
        $query .= "         WHERE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "             SCHOOL_KIND = '".$schoolkind."' AND ";
        }
        $query .= "             SCHREGNO    = '".$model->schregno."' ";
        $query .= "         GROUP BY ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             SCHOOLCD, ";
            $query .= "             SCHOOL_KIND, ";
        }
        $query .= "             SCHREGNO ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         T1.SCHOOLCD     = T2.SCHOOLCD AND ";
            $query .= "         T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
        }
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "         VALUE(T1.EDATE,'9999-12-31') = T2.EDATE ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLUBCD, ";
        $query .= "         L1.CLUBNAME ";
        $query .= "     ORDER BY ";
        $query .= "         T1.CLUBCD ";
        $query .= "         FETCH FIRST 1 ROWS ONLY ";
        $query .= " ), COMMITTEE_HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.COMMITTEECD, ";
        $query .= "         L1.COMMITTEENAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "         INNER JOIN COMMITTEE_MST L1 ";
        $query .= "              ON T1.COMMITTEE_FLG    = L1.COMMITTEE_FLG ";
        $query .= "             AND T1.COMMITTEECD      = L1.COMMITTEECD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             AND T1.SCHOOLCD         = L1.SCHOOLCD ";
            $query .= "             AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        }
        $query .= "        ,(SELECT ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             SCHOOLCD, ";
            $query .= "             SCHOOL_KIND, ";
        }
        $query .= "             YEAR, ";
        $query .= "             SCHREGNO, ";
        $query .= "             MAX(SEQ) AS SEQ ";
        $query .= "         FROM ";
        $query .= "             SCHREG_COMMITTEE_HIST_DAT ";
        $query .= "         WHERE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "             SCHOOL_KIND = '".$schoolkind."' AND ";
        }
        $query .= "             YEAR        = '".$model->year."' AND ";
        $query .= "             SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "             COMMITTEECD IS NOT NULL ";
        $query .= "         GROUP BY ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             SCHOOLCD, ";
            $query .= "             SCHOOL_KIND, ";
        }
        $query .= "             YEAR, ";
        $query .= "             SCHREGNO ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         T1.SCHOOLCD     = T2.SCHOOLCD AND ";
            $query .= "         T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
        }
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "         T1.SEQ          = T2.SEQ ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.COMMITTEECD, ";
        $query .= "         L1.COMMITTEENAME ";
        $query .= " ), DOMI_HIST AS ( ";
        $query .= "   SELECT ";
        $query .= "       T1.SCHREGNO, ";
        $query .= "       T2.DOMI_NAME ";
        $query .= "   FROM ";
        $query .= "       (SELECT ";
        $query .= "            T1.SCHREGNO, ";
        $query .= "            MAX(DOMI_CD) AS DOMI_CD ";
        $query .= "        FROM ";
        $query .= "            SCHREG_DOMITORY_HIST_DAT T1 ";
        $query .= "        WHERE ";
        $query .= "            T1.SCHREGNO    = '".$model->schregno."' ";
        $query .= "            AND '".CTRL_DATE."' BETWEEN T1.DOMI_ENTDAY AND VALUE(T1.DOMI_OUTDAY, '9999-12-31') ";
        $query .= "        GROUP BY ";
        $query .= "            T1.SCHREGNO ";
        $query .= "        ) T1 ";
        $query .= "       INNER JOIN DOMITORY_MST T2 ON T2.DOMI_CD = T1.DOMI_CD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     L2.HR_NAME || '　' || T1.ATTENDNO || ' 番' AS ATTENDNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || '：' || L4.COURSENAME || L4.MAJORABBV AS MAJORCD, ";
        $query .= "     T1.COURSECODE || '：' || L3.COURSECODENAME AS COURSECODE, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     L1.SEX || '：' || N1.NAME1 AS SEX, ";
        $query .= "     L1.NAME_KANA, ";
        $query .= "     L1.NAME_ENG, ";
        $query .= "     L5.CLUBNAME AS CLUBCD, ";
        $query .= "     L6.COMMITTEENAME AS COMMITTEECD, ";
        $query .= "     L7.STAFFNAME AS TR_CD1, ";
        $query .= "     L8.FINSCHOOL_NAME AS FINSCHOOLCD, ";
        $query .= "     L9.GUARD_TELNO, ";
        $query .= "     L10.DOMI_NAME ";
        $query .= " FROM ";
        if ($model->mode != "grd") {
            $query .= "     SCHREG_REGD_DAT T1 ";
        } else {
            $query .= "     GRD_REGD_DAT T1 ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ";
        $query .= "          ON L2.YEAR     = T1.YEAR ";
        $query .= "         AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND L2.GRADE    = T1.GRADE ";
        $query .= "         AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN COURSECODE_MST L3 ON L3.COURSECODE = T1.COURSECODE ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = L1.SEX ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L4 ";
        $query .= "          ON L4.YEAR     = T1.YEAR ";
        $query .= "         AND L4.COURSECD = T1.COURSECD ";
        $query .= "         AND L4.MAJORCD  = T1.MAJORCD ";
        $query .= "     LEFT JOIN CLUB_HIST L5 ON L5.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN COMMITTEE_HIST L6 ON L6.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN V_STAFF_MST L7 ON L7.YEAR = T1.YEAR AND L7.STAFFCD = L2.TR_CD1 ";
        $query .= "     LEFT JOIN V_FINSCHOOL_MST L8 ON L8.YEAR = T1.YEAR AND L8.FINSCHOOLCD = L1.FINSCHOOLCD ";
        $query .= "     LEFT JOIN GUARDIAN_DAT L9 ON L9.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN DOMI_HIST L10 ON L10.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.SEMESTER = '".$model->semester."' ";
        $query .= " AND T1.YEAR     = '".$model->year."' ";

        return $query;
    }

    //行動の記録取得
    public function getActionDuc($model, $order)
    {
        $query  = "SELECT ";
        $query .= "    T1.STAFFCD || '　' || L1.STAFFNAME AS STAFFNAME, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.ACTIONTIME, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    L2.NAME1 AS DIVIDENAME, ";
        $query .= "    T1.TITLE, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.SEQ ";
        $query .= "FROM ";
        $query .= "    ACTION_DOCUMENT_DAT T1 ";
        $query .= "    LEFT JOIN V_STAFF_MST L1 ON L1.YEAR = '".$model->year."' ";
        $query .= "         AND L1.STAFFCD = T1.STAFFCD ";
        $query .= "    LEFT JOIN V_NAME_MST L2 ON L2.YEAR = '".$model->year."' ";
        $query .= "         AND L2.NAMECD1 = 'H307' ";
        $query .= "         AND L2.NAMECD2 = T1.DIVIDECD ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$model->schregno."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "    AND T1.STAFFCD = '".STAFFCD."' ";
        }
        if ($model->narrowing) {
            $query .= " AND T1.DIVIDECD = '".$model->narrowing."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    ".$order." ";
        $query .= "    T1.SEQ ";

        return $query;
    }

    //権限(受講講座)
    public function getAuthChair($model)
    {
        $query  = "SELECT ";
        $query .= "    CASE WHEN COUNT(*) > 0 THEN 'ON' ELSE 'OFF' END AS CHAIRFLG ";
        $query .= "FROM ";
        $query .= "    CHAIR_STD_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".$model->year."' ";
        $query .= "    AND T1.SEMESTER = '".$model->semester."' ";
        $query .= "    AND T1.CHAIRCD IN (SELECT ";
        $query .= "                           T2.CHAIRCD ";
        $query .= "                       FROM ";
        $query .= "                           CHAIR_STF_DAT T2 ";
        $query .= "                       WHERE ";
        $query .= "                           T2.YEAR = '".$model->year."' ";
        $query .= "                           AND T2.SEMESTER = '".$model->semester."' ";
        $query .= "                           AND STAFFCD = '".STAFFCD."' ";
        $query .= "                       ) ";
        $query .= "    AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //権限(クラブ)
    public function getAuthClub($model)
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
        $query .= "                      YEAR = '".$model->year."' ";
        $query .= "                 ) ";
        $query .= "    AND T1.CLUBCD IN (SELECT ";
        $query .= "                          CLUBCD ";
        $query .= "                      FROM ";
        $query .= "                          CLUB_ADVISER_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          YEAR = '".$model->year."' ";
        $query .= "                          AND ADVISER = '".STAFFCD."' ";
        $query .= "                     ) ";
        $query .= "    AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //権限(担任)
    public function getAuthHrclass($model)
    {
        $query  = "SELECT ";
        $query .= "    CASE WHEN COUNT(*) > 0 THEN 'ON' ELSE 'OFF' END AS HRCLASSFLG ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "    AND YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER = '".$model->semester."' ";
        $query .= "    AND VALUE(T1.GRADE, '00') || VALUE(T1.HR_CLASS, '000') ";
        $query .= "        IN (SELECT ";
        $query .= "                VALUE(T2.GRADE, '00') || VALUE(T2.HR_CLASS, '000') ";
        $query .= "            FROM ";
        $query .= "                SCHREG_REGD_HDAT T2 ";
        $query .= "            WHERE ";
        $query .= "                T2.YEAR = '".$model->year."' ";
        $query .= "                AND T2.SEMESTER = '".$model->semester."' ";
        $query .= "                AND (T2.TR_CD1 = '".STAFFCD."' ";
        $query .= "                     OR T2.TR_CD2 = '".STAFFCD."' ";
        $query .= "                     OR T2.TR_CD3 = '".STAFFCD."' ";
        $query .= "                     OR T2.SUBTR_CD1 = '".STAFFCD."' ";
        $query .= "                     OR T2.SUBTR_CD2 = '".STAFFCD."' ";
        $query .= "                     OR T2.SUBTR_CD3 = '".STAFFCD."')) ";

        return $query;
    }

    //権限(進路担当)
    public function getAuthCourse($model)
    {
        $query .= "SELECT ";
        $query .= "    CASE WHEN COUNT(*) > 0 THEN 'ON' ELSE 'OFF' END AS COURSEFLG ";
        $query .= "FROM ";
        $query .= "    USERGROUP_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND GROUPCD = '0009' ";
        $query .= "    AND STAFFCD = '".STAFFCD."' ";

        return $query;
    }

    //行動の記録取得
    public function getActionDucSub($model)
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
        $query .= "    LEFT JOIN V_STAFF_MST L1 ON L1.YEAR = '".$model->year."' ";
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
    public function getName($schregno)
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
    public function getNameMst($model, $namecd1, $namecd2 = "")
    {
        $query  = "SELECT ";
        $query .= "    NAME1 AS LABEL, ";
        $query .= "    NAMECD2 AS VALUE ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
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
    public function getMaxSeq($schregno)
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
     * 在籍情報取得
     */
    public function getZaisekiInfo($schregno)
    {
        $query .= " WITH MAX_SEM AS ( ";
        $query .= " SELECT ";
        $query .= "     REG_D.SCHREGNO, ";
        $query .= "     REG_D.YEAR, ";
        $query .= "     MAX(REG_D.SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REG_D ";
        $query .= " WHERE ";
        $query .= "     REG_D.SCHREGNO = '{$schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     REG_D.SCHREGNO, ";
        $query .= "     REG_D.YEAR ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     REG_D.YEAR, ";
        $query .= "     REG_D.SEMESTER, ";
        $query .= "     REG_D.GRADE, ";
        $query .= "     REG_D.HR_CLASS, ";
        $query .= "     REG_D.ATTENDNO, ";
        $query .= "     REG_H.HR_NAME, ";
        $query .= "     REG_D.COURSECD, ";
        $query .= "     REG_D.MAJORCD, ";
        $query .= "     REG_D.COURSECODE, ";
        $query .= "     MAJOR.MAJORNAME, ";
        $query .= "     COURSE.COURSECODENAME, ";
        $query .= "     STAFF.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     MAX_SEM ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REG_D ";
        $query .= "             ON MAX_SEM.YEAR = REG_D.YEAR ";
        $query .= "            AND MAX_SEM.SEMESTER = REG_D.SEMESTER ";
        $query .= "            AND MAX_SEM.SCHREGNO = REG_D.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REG_H ";
        $query .= "            ON REG_D.YEAR = REG_H.YEAR ";
        $query .= "           AND REG_D.SEMESTER = REG_H.SEMESTER ";
        $query .= "           AND REG_D.GRADE = REG_H.GRADE ";
        $query .= "           AND REG_D.HR_CLASS = REG_H.HR_CLASS ";
        $query .= "     LEFT JOIN STAFF_MST STAFF ";
        $query .= "            ON REG_H.TR_CD1 = STAFF.STAFFCD ";
        $query .= "     LEFT JOIN MAJOR_MST MAJOR ";
        $query .= "            ON REG_D.COURSECD = MAJOR.COURSECD ";
        $query .= "           AND REG_D.MAJORCD = MAJOR.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST COURSE ";
        $query .= "            ON REG_D.COURSECODE = COURSE.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     REG_D.SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     REG_D.YEAR, ";
        $query .= "     REG_D.SEMESTER, ";
        $query .= "     REG_D.GRADE, ";
        $query .= "     REG_D.HR_CLASS, ";
        $query .= "     REG_D.ATTENDNO ";

        return $query;
    }

    /**
     * アレルギー疾患取得
     */
    public function getAllergies($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_CARE_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR= '{$model->year}' ";
        $query .= "     AND SCHREGNO='{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     CARE_DIV ";

        return $query;
    }

    /**
     * 更新
     */
    public function &updateQuery($model)
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
    public function &insertQuery($model)
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
    public function &getDeleteQuery($delvalue)
    {
        $query  = "DELETE FROM ";
        $query .= "    ACTION_DOCUMENT_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$delvalue[0]."' ";
        $query .= "    AND ACTIONDATE = '".str_replace("/", "-", $delvalue[1])."' ";
        $query .= "    AND SEQ = ".$delvalue[2]." ";

        return $query;
    }

    public function selectQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     GDAT.GRADE_NAME2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "     ON T1.YEAR = GDAT.YEAR ";
        $query .= "     AND GDAT.GRADE = T1.ANNUAL ";
        $query .= "     AND GDAT.SCHOOL_KIND = '{$model->schKind}' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR <= '{$model->year}' AND ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ANNUAL ";


        return $query;
    }
    public function selectQuery2($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD || ':' || T2.CLASSNAME AS CLASSNAME, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) AS CREDIT, ";
        $query .= "     T1.VALUATION ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1";
        $query .= " LEFT JOIN CLASS_MST T2 ";
        $query .= "     ON T1.CLASSCD = T2.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= " LEFT JOIN SUBCLASS_MST T3 ";
        $query .= "     ON T1.CLASSCD = T3.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD <> '1' ";
        $query .= "     AND T1.YEAR <= '{$model->year}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD,VALUE,T1.ANNUAL ";

        return $query;
    }

    public function selectQuery4($model)
    {
        $query  = " WITH MAX_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE, ";
        $query .= "     MAX(YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT  ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE ";
        $query .= " ), MAX_SEME AS ( ";
        $query .= " SELECT ";
        $query .= "     MAX_YEAR.SCHREGNO, ";
        $query .= "     MAX_YEAR.YEAR, ";
        $query .= "     MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     MAX_YEAR ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON MAX_YEAR.SCHREGNO = REGD.SCHREGNO ";
        $query .= "           AND MAX_YEAR.YEAR = REGD.YEAR ";
        $query .= " GROUP BY ";
        $query .= "     MAX_YEAR.SCHREGNO, ";
        $query .= "     MAX_YEAR.YEAR ";
        $query .= " ), GRADETABLE AS ( ";
        $query .= " SELECT ";
        $query .= "     REGD.*, ";
        $query .= "     GDAT.GRADE_CD ";
        $query .= " FROM ";
        $query .= "     MAX_SEME ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON MAX_SEME.SCHREGNO = REGD.SCHREGNO ";
        $query .= "           AND MAX_SEME.YEAR = REGD.YEAR ";
        $query .= "           AND MAX_SEME.SEMESTER = REGD.SEMESTER ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "         ON REGD.YEAR = GDAT.YEAR ";
        $query .= "         AND REGD.GRADE = GDAT.GRADE ";
        $query .= "         AND GDAT.SCHOOL_KIND = '{$model->schKind}' ";
        $query .= " ), TEMPDATA AS( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.GRADE_CD, ";
        $query .= "     VALUE(SUM(T1.LESSON), 0) - VALUE(SUM(T1.OFFDAYS), 0) - VALUE(SUM(T1.ABROAD), 0) AS ZYUGYOU, ";
        $query .= "     VALUE(SUM(T1.SUSPEND), 0) + VALUE(SUM(T1.MOURNING), 0) AS SYUTTEIKIBIKI, ";
        $query .= "     VALUE(SUM(T1.ABROAD), 0) AS RYUUGAKU, ";
        $query .= "     VALUE(SUM(T1.LESSON), 0) - VALUE(SUM(T1.OFFDAYS), 0) - VALUE(SUM(T1.ABROAD), 0) - VALUE(SUM(T1.SUSPEND), 0) - VALUE(SUM(T1.MOURNING), 0) AS YOUSYUSSKEI, ";
        $query .= "     VALUE(SUM(T1.NONOTICE), 0) AS KESSEKI, ";
        $query .= "     VALUE(SUM(T1.LESSON), 0) - VALUE(SUM(T1.OFFDAYS), 0) - VALUE(SUM(T1.ABROAD), 0) - VALUE(SUM(T1.SUSPEND), 0) - VALUE(SUM(T1.MOURNING), 0) - VALUE(SUM(T1.NONOTICE), 0) AS SYUSSEKI, ";
        $query .= "     VALUE(SUM(T2.LATE), 0) AS TIKOKU, ";
        $query .= "     VALUE(SUM(T2.EARLY), 0) AS SOUTAI ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ATTEND_SUBCLASS_DAT T2 ";
        $query .= "     ON T1.COPYCD = T2.COPYCD ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.MONTH = T2.MONTH ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " INNER JOIN ";
        $query .= "     GRADETABLE T3 ";
        $query .= "     ON T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.COPYCD = '0' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO,T3.GRADE_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE_CD, ";
        $query .= "     T1.ZYUGYOU, ";
        $query .= "     T1.SYUTTEIKIBIKI, ";
        $query .= "     T1.RYUUGAKU, ";
        $query .= "     T1.YOUSYUSSKEI, ";
        $query .= "     T1.KESSEKI, ";
        $query .= "     T1.SYUSSEKI, ";
        $query .= "     T1.TIKOKU, ";
        $query .= "     T1.SOUTAI, ";
        $query .= "     CAST(T1.SYUSSEKI AS FLOAT)/ T1.YOUSYUSSKEI AS SYUSSEKIRITU, ";
        $query .= "     CASE WHEN T1.KESSEKI + (CAST((T1.TIKOKU + T1.SOUTAI) AS FLOAT) / 3) < 1 THEN '皆勤' WHEN T1.KESSEKI + (CAST((T1.TIKOKU + T1.SOUTAI) AS FLOAT) / 3) <= 3 THEN '精勤' END AS KAIKIN, ";
        $query .= "     T1.KESSEKI + (CAST((T1.TIKOKU + T1.SOUTAI) AS FLOAT) / 3) AS KANSANKESSEKISUU ";
        $query .= " FROM ";
        $query .= "     TEMPDATA T1 ";

        return $query;
    }
}
