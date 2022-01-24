<?php

require_once('for_php7.php');

class knje354Query extends Query
{
    public function getMaxUpdate($db)
    {
        $query = "SELECT DATE(MAX(UPDATED)),TIME(MAX(UPDATED)) FROM COLLEGE_EXAM_CALENDAR";
        $tmp   = $db->getRow($query);

        $date1 = ($tmp[0]=="") ? null : strftime("%Y年%m月%d日 %H時%M分", strtotime($tmp[0]." ".$tmp[1]));
        return $date1;
    }

    public function getCsvTableData()
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     NAME, ";
        $query .= "     TYPENAME, ";
        $query .= "     NULLS, ";
        $query .= "     LENGTH, ";
        $query .= "     COLNO ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBCREATOR = 'DB2INST1' ";
        $query .= "     AND TBNAME = 'COLLEGE_EXAM_CALENDAR' ";
        $query .= "     AND NAME NOT IN ('YEAR', 'REGISTERCD', 'UPDATED') ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     NAME, ";
        $query .= "     TYPENAME, ";
        $query .= "     NULLS, ";
        $query .= "     LENGTH ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    public function insertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //INSERT
        for ($i = 0; $i < get_count($model->data_arr); $i++) {
            //DELETE
            $query  = "DELETE FROM COLLEGE_EXAM_CALENDAR";
            $query .= " WHERE YEAR  = '".$model->field["YEAR"]."'";
            $query .= "   AND SCHOOL_CD   = '".$model->data_arr[$i]["SCHOOL_CD"]."'";
            $query .= "   AND FACULTYCD = '".$model->data_arr[$i]["FACULTYCD"]."'";
            $query .= "   AND DEPARTMENTCD = '".$model->data_arr[$i]["DEPARTMENTCD"]."'";
            $query .= "   AND PROGRAM_CD = '".$model->data_arr[$i]["PROGRAM_CD"]."'";
            $query .= "   AND FORM_CD = '".$model->data_arr[$i]["FORM_CD"]."'";
            $query .= "   AND S_CD = '".$model->data_arr[$i]["S_CD"]."'";
            $query .= "   AND ADVERTISE_DIV = '".$model->data_arr[$i]["ADVERTISE_DIV"]."'";
            $db->query($query);

            $data["YEAR"][TEXT] = $model->field["YEAR"];
            foreach ($model->csvTable as $key => $val) {
                if ($val["TYPE"] == "SMALLINT" || $val["TYPE"] == "INTEGER") {
                    $data[$val["FIELDNAME"]][NUMBER]  = $model->data_arr[$i][$val["FIELDNAME"]];
                } elseif ($val["TYPE"] == "VARCHAR") {
                    $data[$val["FIELDNAME"]][TEXT]    = $model->data_arr[$i][$val["FIELDNAME"]];
                }
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $query = Query::insertSQL($data, "COLLEGE_EXAM_CALENDAR");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
