<?php

require_once('for_php7.php');

class knjh060Query extends Query {

    //学期マスタの取得
    function getSemester()
    {
        return "SELECT semester, semestername FROM semester_mst WHERE year = '".CTRL_YEAR."'";
    }
    
    
    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($schregno)
    {
        $query  ="SELECT schregno, name_show as name FROM schreg_base_mst";
        $query .=" WHERE schregno = '".$schregno."'";
        return $query;
    }

    //１レコード取得
    function getTrainRow($schregno)
    {
        $db = Query::dbCheckOut();
        $query  = "SELECT * FROM schreg_train_dat ";
        $query .= " WHERE year    = '".CTRL_YEAR."'";
        $query .= " AND  schregno = '".$schregno. "'";
        
        $record = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $record[$row["SEMESTER"]] = array("caution"    => $row["CAUTION_GUIDANCE"],
                                              "admonition" => $row["ADMONITION_GUIDANCE"]);    
        }
        Query::dbCheckIn($db);
        return $record;
    }

    //INSERT
    function &getInsertQuery($db,$model,$semester)
    {

        $data["YEAR"][TEXT]                = CTRL_YEAR;
        $data["SEMESTER"][TEXT]            = $semester;
        $data["SCHREGNO"][TEXT]            = $model->schregno;
        $data["CAUTION_GUIDANCE"][TEXT]    = $model->field[$semester]["caution"];
        $data["ADMONITION_GUIDANCE"][TEXT] = $model->field[$semester]["admonition"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $db->query(Query::insertSQL($data, "schreg_train_dat"));
        return ;
    }

    //DELETE
    function &getDeleteQuery($db,$schregno,$semester)
    {

        $query  = " DELETE FROM schreg_train_dat ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "   AND SCHREGNO = '".$schregno."'";
        $query .= "   AND SEMESTER = '".$semester."'"; 

        $db->query($query);

        return $result;
    }
}
?>
