<?php

require_once('for_php7.php');
class knjx140aquery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX140A' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "";
        $query .= "SELECT DISTINCT T2.YEAR, T2.SEMESTER, T2.SEMESTERNAME ";
        $query .= "FROM   SCH_PTRN_HDAT T1, ";
        $query .= "       SEMESTER_MST T2 ";
        $query .= "WHERE  T1.YEAR = T2.YEAR AND ";
        $query .= "       T1.SEMESTER = T2.SEMESTER ";
        $query .= "ORDER BY T2.YEAR, T2.SEMESTER ";

        return $query;
    }

    //ＳＥＱとタイトル
    public function getSelectFieldSQL2($model)
    {
        $query  = "";
        $query .= "SELECT BSCSEQ,TITLE ";
        $query .= "FROM   SCH_PTRN_HDAT ";
        $query .= "WHERE  YEAR||SEMESTER = '". $model->field["YEAR"] ."' ";
        $query .= "ORDER BY BSCSEQ ";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = "";
        $query .= "SELECT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T2.BSCSEQ, ";
        $query .= "     T1.TITLE, ";
        $query .= "     T2.DAYCD, ";
        $query .= "     T2.PERIODCD, ";
        $query .= "     T2.CHAIRCD, ";
        $query .= "     L1.CHAIRNAME, ";
        $query .= "     T3.STAFFCD, ";
        $query .= "     L2.STAFFNAME, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "FROM   SCH_PTRN_HDAT T1, ";
        $query .= "       SCH_PTRN_DAT T2 ";
        $query .= "       LEFT JOIN SCH_PTRN_STF_DAT T3 ON T3.YEAR=T2.YEAR ";
        $query .= "                                    AND T3.SEMESTER=T2.SEMESTER ";
        $query .= "                                    AND T3.BSCSEQ=T2.BSCSEQ ";
        $query .= "                                    AND T3.DAYCD=T2.DAYCD ";
        $query .= "                                    AND T3.PERIODCD=T2.PERIODCD ";
        $query .= "                                    AND T3.CHAIRCD=T2.CHAIRCD ";
        $query .= "     LEFT JOIN CHAIR_DAT L1 ON L1.YEAR = T2.YEAR AND L1.SEMESTER = T2.SEMESTER AND L1.CHAIRCD = T2.CHAIRCD ";
        $query .= "     LEFT JOIN STAFF_MST L2 ON L2.STAFFCD = T3.STAFFCD ";
        $query .= "WHERE T1.YEAR||T1.SEMESTER = '". $model->field["YEAR"] ."' ";
        $query .= "  AND T1.BSCSEQ = ". $model->field["SEQ"] ." ";
        $query .= "  AND T2.YEAR = T1.YEAR ";
        $query .= "  AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "  AND T2.BSCSEQ = T1.BSCSEQ ";
        $query .= "ORDER BY T2.DAYCD,T2.PERIODCD,T2.CHAIRCD ";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($data, $chk_no)
    {
        $flg    = true;
        $query  = "";
        //条件選択
        switch ($chk_no) {
            case "1":
                $where = "NAME_MST WHERE NAMECD1 = 'B001' AND NAMECD2 = '".$data["PERIODCD"]."' ";
                break;
            case "2":
                $where  = "chair_dat where YEAR = '".$data["YEAR"]."' ";
                $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
                $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
                break;
            case "3":
                $where = "STAFF_YDAT where YEAR = '".$data["YEAR"]."' AND STAFFCD = '".$data["STAFFCD"]."' ";
                break;
            default:
                $flg = false;
                break;
        }
        if ($flg) {
            $query = "SELECT 'X' FROM " . $where;
        }

        return $query;
    }


    //存在クエリー(パターン反映履歴データ)
    public function getReflectCheckQuery($data, $db)
    {
        $query  = "";
        $query .= "SELECT COUNT(*) FROM PTRN_REFLECTHIST_DAT ";
        $query .= "where YEAR   = '".$data["YEAR"]."' ";
        $query .= "  and BSCSEQ    = ".$data["BSCSEQ"]." ";

        $ret_val = $db->getOne($query);

        return $ret_val;
    }


    //基本時間割の更新(各テーブルのレコードを削除して追加する)
    public function InsertTableQuery($model, $table, $db, $data2)
    {
        //項目
        $data = knjx140aquery::getFieldData($table, $data2);

        //削除
        $query = knjx140aquery::getDeleteQuery($model, $table, $data2);
        $db->query($query);

        //追加
        if ($table == "sch_ptrn_countflg_dat") {
            $result = $db->query(knjx140aquery::getGradeHrClass($data2));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data["GRADE"][TEXT]        = $row["GRADE"];
                $data["HR_CLASS"][TEXT]     = $row["HR_CLASS"];
                $data["COUNTFLG"][TEXT]     = $row["COUNTFLG"];
                $query = Query::insertSQL($data, $table);
                $db->query($query);
            }
            $result->free();
        } else {
            $query = Query::insertSQL($data, $table);
            $db->query($query);
        }

        return;
    }

    //項目
    public function getFieldData($table, $data2)
    {
        $data = array();

        if ($table == "sch_ptrn_hdat") {
            $data["YEAR"][TEXT]         = $data2["YEAR"];
            $data["SEMESTER"][TEXT]     = $data2["SEMESTER"];
            $data["BSCSEQ"][NUMBER]     = $data2["BSCSEQ"];
            $data["TITLE"][TEXT]        = $data2["TITLE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";
        } elseif ($table == "sch_ptrn_dat") {
            $data["YEAR"][TEXT]         = $data2["YEAR"];
            $data["SEMESTER"][TEXT]     = $data2["SEMESTER"];
            $data["BSCSEQ"][NUMBER]     = $data2["BSCSEQ"];
            $data["DAYCD"][TEXT]        = $data2["DAYCD"];
            $data["PERIODCD"][TEXT]     = $data2["PERIODCD"];
            $data["CHAIRCD"][TEXT]      = $data2["CHAIRCD"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";
        } elseif ($table == "sch_ptrn_stf_dat") {
            $data["YEAR"][TEXT]         = $data2["YEAR"];
            $data["SEMESTER"][TEXT]     = $data2["SEMESTER"];
            $data["BSCSEQ"][NUMBER]     = $data2["BSCSEQ"];
            $data["DAYCD"][TEXT]        = $data2["DAYCD"];
            $data["PERIODCD"][TEXT]     = $data2["PERIODCD"];
            $data["CHAIRCD"][TEXT]      = $data2["CHAIRCD"];
            $data["STAFFCD"][TEXT]      = $data2["STAFFCD"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";
        } elseif ($table == "sch_ptrn_countflg_dat") {
            $data["YEAR"][TEXT]         = $data2["YEAR"];
            $data["SEMESTER"][TEXT]     = $data2["SEMESTER"];
            $data["BSCSEQ"][NUMBER]     = $data2["BSCSEQ"];
            $data["DAYCD"][TEXT]        = $data2["DAYCD"];
            $data["PERIODCD"][TEXT]     = $data2["PERIODCD"];
            $data["CHAIRCD"][TEXT]      = $data2["CHAIRCD"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";
        }

        return $data;
    }

    //削除クエリー
    public function getDeleteQuery($model, $table, $data2)
    {
        $query  = "";
        $query .= "DELETE FROM " .$table ." ";
        $query .= "where YEAR       = '".$data2["YEAR"]."' ";
        $query .= "  and SEMESTER   = '".$data2["SEMESTER"]."' ";
        $query .= "  and BSCSEQ     =  ".$data2["BSCSEQ"]." ";

        if ($table == "sch_ptrn_dat" || $table == "sch_ptrn_stf_dat" || $table == "sch_ptrn_countflg_dat") {
            $query .= "  and DAYCD      = '".$data2["DAYCD"]."' ";
            $query .= "  and PERIODCD   = '".$data2["PERIODCD"]."' ";
            $query .= "  and CHAIRCD    = '".$data2["CHAIRCD"]."' ";

            if ($table == "sch_ptrn_stf_dat" && $model->field["SHORI_MEI"] == "1") { //更新のみ使用
                $query .= "  and STAFFCD    = '".$data2["STAFFCD"]."' ";
            }
        }

        return $query;
    }

    //存在クエリー(時間割パターンデータの件数)
    public function getExistsQuery($model, $data2)
    {
        $query  = "";
        $query .= "SELECT COUNT(*) FROM sch_ptrn_dat ";
        $query .= "where YEAR       = '".$data2["YEAR"]."' ";
        $query .= "  and SEMESTER   = '".$data2["SEMESTER"]."' ";
        $query .= "  and BSCSEQ     =  ".$data2["BSCSEQ"]." ";

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

    //受講クラスデータ取得
    public function getGradeHrClass($data2)
    {
        $query  = " SELECT ";
        $query .= "     T1.TRGTGRADE AS GRADE, ";
        $query .= "     T1.TRGTCLASS AS HR_CLASS, ";
        $query .= "     T2.COUNTFLG ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$data2["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$data2["SEMESTER"]."' AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD  = '".$data2["CHAIRCD"]."' AND ";
        $query .= "     T1.GROUPCD  = T2.GROUPCD AND ";
        $query .= "     T1.GROUPCD  = '0000' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.TRGTGRADE AS GRADE, ";
        $query .= "     T1.TRGTCLASS AS HR_CLASS, ";
        $query .= "     T2.COUNTFLG ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$data2["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$data2["SEMESTER"]."' AND ";
        $query .= "     T1.CHAIRCD  = '0000000' AND ";
        $query .= "     T2.CHAIRCD  = '".$data2["CHAIRCD"]."' AND ";
        $query .= "     T1.GROUPCD  = T2.GROUPCD ";

        return $query;
    }
}
