<?php

require_once('for_php7.php');

class knjb1602Query extends Query {

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJB1602' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年度一覧
    function getSelectYear()
    {
        $query  = " SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_DAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR,T1.SEMESTER";

        return $query;    
    }

    //講座一覧
    function getSelectChair($model)
    {
        $query  = "SELECT DISTINCT T1.CHAIRCD, T2.CHAIRNAME ";
        $query .= "FROM SCHREG_TEXTBOOK_DAT T1 ";
        $query .= "     LEFT JOIN CHAIR_DAT T2 ON T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "          AND T1.YEAR = T2.YEAR ";
        $query .= "          AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "WHERE T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "ORDER BY T1.CHAIRCD";

        return $query;    
    }

    //年組一覧
    function getSelectHrclass($model)
    {
        $query  = "SELECT * FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR || SEMESTER = '".$model->field["YEAR2"]."' ";
        $query .= "ORDER BY GRADE,HR_CLASS";

        return $query;    
    }

    //生徒一覧
    function getSelectStudent($model)
    {
        $query  = "SELECT DISTINCT T1.SCHREGNO, T1.ATTENDNO, T2.NAME_SHOW ";
        $query .= "FROM SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "WHERE T1.YEAR || T1.SEMESTER = '".$model->field["YEAR2"]."' ";
        $query .= "		AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY T1.ATTENDNO";

        return $query;    
    }

    //データ出力（講座別）
    function getselectDataSQL($model)
    {
        $query  = " SELECT SCHREGNO, YEAR, SEMESTER, CHAIRCD, TEXTBOOKCD, 'DUMMY' ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if( $model->field["CHAIRCD"] != "" ) {
            $query .= " AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        $query .= " ORDER BY CHAIRCD, SCHREGNO, TEXTBOOKCD";

        return $query;    
    }

    //データ出力（個人別）
    function getselectDataSQL2($model)
    {
        $query  = " SELECT T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.CHAIRCD, T1.TEXTBOOKCD, 'DUMMY' ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_DAT T1, SCHREG_REGD_DAT T2";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR2"]."' ";
        $query .= "     AND T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T1.SCHREGNO=T2.SCHREGNO ";
        if( $model->field["STUDENT"] != "" ) {
            $query .= " AND T1.SCHREGNO = '".$model->field["STUDENT"]."' ";
        }
        $query .= " ORDER BY  T1.SCHREGNO, T1.CHAIRCD, T1.TEXTBOOKCD";

        return $query;    
    }

    //講座データ(存在チェック用)
    function getChair($chr_cd, $year, $seme)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     CHAIRCD = '".$chr_cd."' ";
        $query .= "     AND YEAR = '".$year."' ";
        $query .= "     AND SEMESTER = '".$seme."' ";

        return $query;    
    }

    //教科書データ(存在チェック用)
    function getText($text_cd, $year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     V_TEXTBOOK_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND TEXTBOOKCD = '".$text_cd."' ";

        return $query;    
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //学籍教科書購入データの存在チェック（追加・更新かの判断用）
    function getSchregTextbookExists($where)
    {
        return "SELECT COUNT(*) FROM SCHREG_TEXTBOOK_DAT ".$where;
    }

    //学籍教科書購入データへ追加・更新
    function InsertQuery(&$db, $data_arr)
    {
        $db->autoCommit(false);
        $data = array();
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
            $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]     = $data_arr[$i]["SEMESTER"];
            $data["CHAIRCD"][TEXT]      = $data_arr[$i]["CHAIRCD"];
            $data["TEXTBOOKCD"][TEXT]   = $data_arr[$i]["TEXTBOOKCD"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $where  = "where SCHREGNO   = '".$data_arr[$i]["SCHREGNO"]."' ";
            $where .= "  and YEAR       = '".$data_arr[$i]["YEAR"]."' ";
            $where .= "  and SEMESTER   = '".$data_arr[$i]["SEMESTER"]."' ";
            $where .= "  and CHAIRCD    = '".$data_arr[$i]["CHAIRCD"]."' ";
            $where .= "  and TEXTBOOKCD = '".$data_arr[$i]["TEXTBOOKCD"]."' ";

            $ret_val = $db->getOne(knjb1602Query::getSchregTextbookExists($where));

            if ($ret_val == "0") {  //INSERT
                $query = Query::insertSQL($data, "SCHREG_TEXTBOOK_DAT");
            } else {                //UPDATE
                $query = Query::updateSQL($data, "SCHREG_TEXTBOOK_DAT", $where);
            }

            $db->query($query);
        }
        $db->commit();
        return;
    }

    //学籍教科書購入データ削除
    function deleteQuery(&$db, $data_arr)
    {
        $db->autoCommit(false);
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM SCHREG_TEXTBOOK_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND CHAIRCD = '".$data_arr[$i]["CHAIRCD"]."' ";
            $query .= "     AND TEXTBOOKCD = '".$data_arr[$i]["TEXTBOOKCD"]."' ";

            $db->query($query);
        }

        $db->commit();
        return;
    }

} 
?>
