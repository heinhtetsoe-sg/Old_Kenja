<?php

require_once('for_php7.php');

class knjd064kQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$model->control["年度"] ."'";
            if ($model->field["SEMESTER"]=="9") {
                $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            } else {
                $query .= "AND SEMESTER='".$model->field["SEMESTER"] ."' ";
            }
            $query .= "AND GRADE='".$model->field["GRADE"] ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$model->control["年度"] ."'";
            if ($model->field["SEMESTER"]=="9") {
                $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            } else {
                $query .= "AND SEMESTER='".$model->field["SEMESTER"] ."' ";
            }
            $query .= "AND GRADE='".$model->field["GRADE"] ."'";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."' ";
            //---2005.06.01
            $query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '".CTRL_YEAR."' ";
        $query .= "AND GRADE <> '01' ";  //06/04/03 yamauchi
        if ($model->field["SEMESTER"]=="9") {
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            if (CTRL_SEMESTER < 3) $query .= "AND GRADE = '03' ";//2005.11.28
        } else {
            $query .= "AND SEMESTER='".$model->field["SEMESTER"] ."' ";
        }
        $query .= "order by grade";

        return $query;
    }

    //前年度開始・終了日付取得 NO001
    function getDate($year)
    {
        $query = "SELECT SDATE,EDATE FROM SEMESTER_MST ".
                 " WHERE YEAR = '" .$year ."' AND SEMESTER = '9' ";

        return $query;
    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM subclass_mst ".
                 " WHERE SUBCLASSCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //年度取得
    function getYear()
    {
        $db = Query::dbCheckOut();
        $query = "SELECT CTRL_CHAR1 AS YEAR ".
                    "FROM control_mst WHERE CTRL_CD1= 'B201' AND CTRL_CD2='1001'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //年組取得
    function getHrclass($YEAR)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT GRADE AS VALUE,HR_NAME AS LABEL ".
                    "FROM SCHREG_REGD_HDAT ".
                    "GROUP BY YEAR,GRADE,HR_CLASS ".
                    "HAVING YEAR='" .$YEAR ."'";
        $result = $db->query($query);
        for($i=0;$row[$i] = $result->fetchRow(DB_FETCHMODE_ASSOC);$i++)
            print($row[$i]["VALUE"]);
        $result->free();
        Query::dbCheckIn($db);
        return $row;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "INSERT INTO subclass_mst(SUBCLASSCD,SUBCLASSNAME,SUBCLASSABBV,";
        $query.= "ELECTSUBCLASSNAME,UPDATED ) ";
        $query.= "VALUES('" .$fields["SUBCLASSCD"] ."'";
        $query.= ",'" .$fields["SUBCLASSNAME"] ."'";
        $query.= ",'" .$fields["SUBCLASSABBV"] ."'";
        $query.= ",'" .$fields["ELECTSUBCLASSNAME"] ."'";
        $query.= ",sysdate())";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "UPDATE subclass_mst SET ";
        $query.= "SUBCLASSCD          = '" .$fields["SUBCLASSCD"] ."'";
        $query.= ",SUBCLASSNAME       = '" .$fields["SUBCLASSNAME"] ."'";
        $query.= ",SUBCLASSABBV       = '" .$fields["SUBCLASSABBV"] ."'";
        $query.= ",ELECTSUBCLASSNAME  = '" .$fields["ELECTSUBCLASSNAME"] ."'";
        $query.= ",UPDATED       = sysdate()";
        $query .= " WHERE SUBCLASSCD       = '" .$fields["SUBCLASSCD"] ."'";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM subclass_mst "
                ." WHERE SUBCLASSCD  = '" .$fields["SUBCLASSCD"] ."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
