<?php

require_once('for_php7.php');
class knjh650Query extends Query
{
    public function getStdInfo($model)
    {
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGH.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.NAME2 AS SEX_NAME, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     CENTER_D.BUNRIDIV, ";
        $query .= "     CENTER_D.CENTER_ABSENT, ";
        $query .= "     CENTER_D.MARK_ABSENT ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ";
        $query .= "          AND REGD.SEMESTER = REGH.SEMESTER ";
        $query .= "          AND REGD.GRADE = REGH.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGH.HR_CLASS ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "          AND BASE.SEX = Z002.NAMECD2 ";
        $query .= "     LEFT JOIN MOCK_CENTER_DAT CENTER_D ON REGD.YEAR = CENTER_D.YEAR ";
        $query .= "          AND CENTER_D.CENTERCD = '000000000' ";
        $query .= "          AND REGD.SCHREGNO = CENTER_D.SCHREGNO ";
        $query .= "          AND CENTER_D.RANK_RANGE = '1' ";
        $query .= "          AND CENTER_D.RANK_DIV = '02' ";
        $query .= "          AND CENTER_D.CENTERDIV = '1' ";
        $query .= " WHERE ";
        $query .= "     BASE.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getScore($model)
    {
        $query .= " SELECT ";
        $query .= "     RANK.*, ";
        $query .= "     SUBCLASS.PERFECT ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_RANK_RANGE_DAT RANK ";
        $query .= "     LEFT JOIN MOCK_CENTER_SUBCLASS_MST SUBCLASS ON RANK.YEAR = SUBCLASS.YEAR ";
        $query .= "          AND RANK.CENTER_SUBCLASS_CD = SUBCLASS.CENTER_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "     RANK.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND RANK.CENTERCD = '000000000' ";
        $query .= "     AND RANK.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND RANK.RANK_RANGE = '1' ";
        $query .= "     AND RANK.RANK_DIV = '02' ";
        $query .= "     AND RANK.CENTERDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     RANK.CENTER_SUBCLASS_CD ";

        return $query;
    }

    public function getTotalScore($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_TOTAL_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CENTERCD = '000000000' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND RANK_RANGE = '1' ";
        $query .= "     AND RANK_DIV = '02' ";
        $query .= "     AND CENTERDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     CENTER_CLASS_CD ";

        return $query;
    }

    public function getSubclass($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     CENTER_SUBCLASS_CD ";

        return $query;
    }

    public function getProgrampath()
    {
        $query  = " WITH MENU AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_HIGH_SECURITY_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID ='KNJH650' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID ='KNJH650' ";

        return $query;
    }

    //DELETE
    public function &getDeleteDatQuery($db, $model)
    {
        $query  = " DELETE FROM MOCK_CENTER_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CENTERCD = '000000000' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND RANK_RANGE = '1' ";
        $query .= "     AND RANK_DIV = '02' ";
        $query .= "     AND CENTERDIV = '1' ";

        $db->query($query);
    }

    //INSERT
    public function &getInsertDatQuery($db, $model)
    {
        $data = array();
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["CENTERCD"][TEXT]             = "000000000";
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["RANK_RANGE"][TEXT]           = "1";
        $data["RANK_DIV"][TEXT]             = "02";
        $data["CENTERDIV"][TEXT]            = "1";
        $data["BUNRIDIV"][TEXT]             = $model->field["BUNRIDIV"];
        $data["CENTER_ABSENT"][TEXT]        = $model->field["CENTER_ABSENT"];
        $data["MARK_ABSENT"][TEXT]          = $model->field["MARK_ABSENT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_CENTER_DAT");
        $db->query($query);
    }

    //DELETE
    public function &getDeleteRankQuery($db, $model)
    {
        $query  = " DELETE FROM MOCK_CENTER_RANK_RANGE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CENTERCD = '000000000' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND RANK_RANGE = '1' ";
        $query .= "     AND RANK_DIV = '02' ";
        $query .= "     AND CENTERDIV = '1' ";

        $db->query($query);
    }

    //INSERT
    public function &getInsertRankQuery($db, $model)
    {
        foreach ($model->updData as $key => $val) {
            if (strlen($val["SCORE"]) > 0) {
                $data = array();
                $data["YEAR"][TEXT]                 = CTRL_YEAR;
                $data["CENTERCD"][TEXT]             = "000000000";
                $data["SCHREGNO"][TEXT]             = $model->schregno;
                $data["CENTER_CLASS_CD"][TEXT]      = $val["CLASSCD"];
                $data["CENTER_SUBCLASS_CD"][TEXT]   = $key;
                $data["RANK_RANGE"][TEXT]           = "1";
                $data["RANK_DIV"][TEXT]             = "02";
                $data["CENTERDIV"][TEXT]            = "1";
                $data["SCORE"][NUMBER]              = $val["SCORE"];
                $data["DAIITI_SENTAKU"][TEXT]       = $val["DAIITI_SENTAKU"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                $query = Query::insertSQL($data, "MOCK_CENTER_RANK_RANGE_DAT");
                $db->query($query);
            }
        }
    }

    //DELETE
    public function &getDeleteTotalScoreQuery($db, $model)
    {
        $query  = " DELETE FROM MOCK_CENTER_TOTAL_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CENTERCD = '000000000' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND RANK_RANGE = '1' ";
        $query .= "     AND RANK_DIV = '02' ";
        $query .= "     AND CENTERDIV = '1' ";

        $db->query($query);
    }

    //INSERT
    public function &getInsertTotalScoreQuery($db, $model)
    {
        foreach ($model->totalScore as $key => $val) {
            if (strLen($val["SCORE"]) > 0) {
                $data = array();
                $data["YEAR"][TEXT]                 = CTRL_YEAR;
                $data["CENTERCD"][TEXT]             = "000000000";
                $data["SCHREGNO"][TEXT]             = $model->schregno;
                $data["CENTER_CLASS_CD"][TEXT]      = $key;
                $data["CENTER_SUBCLASS_CD"][TEXT]   = "999999";
                $data["RANK_RANGE"][TEXT]           = "1";
                $data["RANK_DIV"][TEXT]             = "02";
                $data["CENTERDIV"][TEXT]            = "1";
                $data["SCORE"][NUMBER]              = $val["SCORE"];
                $data["CNT"][NUMBER]                = $val["CNT"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";
    
                $query = Query::insertSQL($data, "MOCK_CENTER_TOTAL_SCORE_DAT");
                $db->query($query);
            }
        }
    }

    //DELETE
    public function &getDeleteKansanQuery($db, $model)
    {
        $query  = " DELETE FROM MOCK_CENTER_KANSAN_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CENTERCD = '000000000' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND RANK_RANGE = '1' ";
        $query .= "     AND RANK_DIV = '02' ";
        $query .= "     AND CENTERDIV = '1' ";

        $db->query($query);
    }

    //INSERT
    public function &getInsertKansanQuery($db, $model, $kansanDiv, $bunriDiv, $score)
    {
        $data = array();
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["CENTERCD"][TEXT]             = "000000000";
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["KANSAN_DIV"][TEXT]           = $kansanDiv;
        $data["BUNRIDIV"][TEXT]             = $bunriDiv;
        $data["RANK_RANGE"][TEXT]           = "1";
        $data["RANK_DIV"][TEXT]             = "02";
        $data["CENTERDIV"][TEXT]            = "1";
        $data["SCORE"][NUMBER]              = $score;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_CENTER_KANSAN_RANK_DAT");
        $db->query($query);
    }
}
