<?php

require_once('for_php7.php');

class knjh530_2Query extends Query {

    //実力種別
    function getProficiencyDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //リスト
    function getList()
    {
        $query  = " SELECT ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     VALUE(L1.NAME1, '') AS PROFICIENCYDIV_NAME, ";
        $query .= "     T1.PROFICIENCYCD, ";
        $query .= "     T1.PROFICIENCYNAME1 ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND L1.NAMECD1 = 'H508' ";
        $query .= "          AND L1.NAMECD2 = T1.PROFICIENCYDIV ";
        $query .= " ORDER BY ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD ";
        return $query;

    }

    function getProficiency($model)
    {
        $div = $model->cmd == "reset" ? $model->proficiencyDiv : $model->field["PROFICIENCYDIV"];
        $cd  = $model->cmd == "reset" ? $model->proficiencyCd : $model->field["PROFICIENCYCD"];

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCYDIV = '".$div."' ";
        $query .= "     AND PROFICIENCYCD = '".$cd."' ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["PROFICIENCYDIV"][TEXT]       = $fields["PROFICIENCYDIV"];
        $data["PROFICIENCYCD"][TEXT]        = $fields["PROFICIENCYCD"];
        $data["PROFICIENCYNAME1"][TEXT]     = $fields["PROFICIENCYNAME1"];
        $data["PROFICIENCYNAME2"][TEXT]     = $fields["PROFICIENCYNAME2"];
        $data["PROFICIENCYNAME3"][TEXT]     = $fields["PROFICIENCYNAME3"];
        $data["COUNTFLG"][TEXT]             = $fields["COUNTFLG"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "PROFICIENCY_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["PROFICIENCYNAME1"][TEXT]     = $fields["PROFICIENCYNAME1"];
        $data["PROFICIENCYNAME2"][TEXT]     = $fields["PROFICIENCYNAME2"];
        $data["PROFICIENCYNAME3"][TEXT]     = $fields["PROFICIENCYNAME3"];
        $data["COUNTFLG"][TEXT]             = $fields["COUNTFLG"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     PROFICIENCYDIV = '".$fields["PROFICIENCYDIV"]."' ";
        $where .= "     AND PROFICIENCYCD = '".$fields["PROFICIENCYCD"]."' ";

        $query = Query::updateSQL($data, "PROFICIENCY_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //年度マスタの確認
    function IsExisting($fields) {
        $db    = Query::dbCheckOut();

        $query  = " SELECT "; 
        $query .= "     * "; 
        $query .= " FROM "; 
        $query .= "     PROFICIENCY_YMST "; 
        $query .= " WHERE "; 
        $query .= "     PROFICIENCYDIV  = '".$fields["PROFICIENCYDIV"]."' AND ";
        $query .= "     PROFICIENCYCD   = '".$fields["PROFICIENCYCD"]."' ";

        $row = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM PROFICIENCY_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCYDIV = '".$fields["PROFICIENCYDIV"]."' ";
        $query .= "     AND PROFICIENCYCD = '".$fields["PROFICIENCYCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
