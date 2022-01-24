<?php
class knjh442bQuery extends Query
{

    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJH442B' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //卒業可能学年か判定
    public function getGraduationGrade($model)
    {
        //DB接続
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     L1.SCHOOL_KIND, ";
        $query .= "     CASE WHEN T1.GRADE BETWEEN L2.NAMESPARE2 AND L2.NAMESPARE3 THEN 1 ELSE 0 END AS FLG, ";
        $query .= "     CASE WHEN T1.GRADE BETWEEN L2.NAME2 AND L2.NAME3 THEN 1 ELSE 0 END AS FLG2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ";
        $query .= "          ON L1.YEAR     = T1.YEAR ";
        $query .= "         AND L1.GRADE    = T1.GRADE ";
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "          ON L2.NAMECD1  = 'A023' ";
        $query .= "         AND L2.NAME1    = L1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->exp_year}' ";
        $query .= " AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND T1.SEMESTER = '{$model->exp_semester}' ";

        $ret = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);

        return $ret;
    }

    public function getTargetGrade($model, $num)
    {
        //DB接続
        $db = Query::dbCheckOut();

        $query  = " WITH GRADE_CTRLYEAR AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->exp_year}'  ";
        $query .= "         AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         GRADE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     GRADE_CTRLYEAR T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}'  ";
        $query .= "     AND T1.GRADE = T2.GRADE - {$num} ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR ";

        $ret = $db->getOne($query);

        Query::dbCheckIn($db);

        return $ret;
    }

    //１レコード取得
    public function getTrainRow($schregno, $exp_year, $model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT T1.SCHREGNO, ";
        $query .= "        T1.GRADE1_REMARK, ";
        $query .= "        T1.GRADE2_REMARK, ";
        $query .= "        T1.GRADE3_REMARK, ";
        $query .= "        T1.CONDITION, ";
        $query .= "        T1.HOPE_COLLEGE_NAME1, ";
        $query .= "        T1.HOPE_COURSE_NAME1, ";
        $query .= "        T1.HOPE_COLLEGE_NAME2, ";
        $query .= "        T1.HOPE_COURSE_NAME2 ";
        $query .= "   FROM MOCK_TRAINREMARK_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "    T1.SCHREGNO = '".$schregno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //更新
    public function &getUpdateQuery($fields, $schregno, $exp_year)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = " DELETE FROM MOCK_TRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHREGNO = '".$schregno."'";

        $db->query($query);

        //INSERT
        $data["SCHREGNO"][TEXT]          = $schregno;
        $data["GRADE1_REMARK"][TEXT]     = $fields["GRADE1_REMARK"];
        $data["GRADE2_REMARK"][TEXT]     = $fields["GRADE2_REMARK"];
        $data["GRADE3_REMARK"][TEXT]     = $fields["GRADE3_REMARK"];
        $data["CONDITION"][TEXT]         = $fields["CONDITION"];
        $data["HOPE_COLLEGE_NAME1"][TEXT] = $fields["HOPE_COLLEGE_NAME1"];
        $data["HOPE_COURSE_NAME1"][TEXT]  = $fields["HOPE_COURSE_NAME1"];
        $data["HOPE_COLLEGE_NAME2"][TEXT] = $fields["HOPE_COLLEGE_NAME2"];
        $data["HOPE_COURSE_NAME2"][TEXT]  = $fields["HOPE_COURSE_NAME2"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $db->query(Query::insertSQL($data, "MOCK_TRAINREMARK_DAT"));

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
