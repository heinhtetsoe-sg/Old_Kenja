<?php

require_once('for_php7.php');
class knja129Query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA129' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //１レコード取得
    public function getTrainRow($schregno, $exp_year, $model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT T1.YEAR ";
        $query .= "      , T1.SCHREGNO ";
        $query .= "      , T1.ANNUAL ";
        $query .= "      , T1.ABSENCE_REASON ";
        $query .= "      , T1.DAYS ";
        $query .= "      , T1.PARTICIPATION_DAYS ";
        $query .= "      , T1.METHOD ";
        $query .= "      , T1.OTHER_LEARNING ";
        $query .= "   FROM HTRAINREMARK_ONLINE_DAT T1 ";
        $query .= "  WHERE T1.year     = '".$exp_year."'";
        $query .= "    AND T1.schregno = '".$schregno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    public function &getInsertQuery($field, $schregno, $exp_year, $exp_semester)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $query  = "SELECT MAX(ANNUAL) FROM SCHREG_REGD_DAT ";
        $query .= " WHERE SCHREGNO = '".$schregno."'";
        $query .= "   AND YEAR = '".$exp_year."'";
        $query .= "   AND SEMESTER = '".$exp_semester."'";
        $annual = $db->getOne($query);

        $data["YEAR"][TEXT]               = $exp_year;
        $data["SCHREGNO"][TEXT]           = $schregno;
        $data["ANNUAL"][TEXT]             = $db->getOne($query);
        $data["ABSENCE_REASON"][TEXT]     = $field["ABSENCE_REASON"];
        $data["DAYS"][TEXT]               = $field["DAYS"];
        $data["PARTICIPATION_DAYS"][TEXT] = $field["PARTICIPATION_DAYS"];
        $data["METHOD"][TEXT]             = $field["METHOD"];
        $data["OTHER_LEARNING"][TEXT]     = $field["OTHER_LEARNING"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $query = Query::insertSQL($data, "HTRAINREMARK_ONLINE_DAT");

        $db->query($query);

        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    public function &getDeleteQuery($schregno, $exp_year)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM HTRAINREMARK_ONLINE_DAT ";
        $query .= " WHERE YEAR = '".$exp_year."'";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return ;
    }

    //校種
    public function getSchoolKind($schregno, $exp_year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT GDAT.SCHOOL_KIND ";
        $query .= " FROM SCHREG_REGD_DAT REGD ";
        $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "    ON GDAT.YEAR = REGD.YEAR ";
        $query .= "   AND GDAT.GRADE = REGD.GRADE ";
        $query .= " WHERE REGD.YEAR = '".$exp_year."'";
        $query .= "   AND REGD.SCHREGNO = '".$schregno."'";

        $schoolKind = $db->getOne($query);

        Query::dbCheckIn($db);
        return $schoolKind;
    }
}
