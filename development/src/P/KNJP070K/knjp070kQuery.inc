<?php

require_once('for_php7.php');


/******************
  $model->div = "M" (中分類)
  $model->div = "S" (小分類)
 ******************/
class knjp070kQuery extends Query {

    //１レコード取得
    function getRow1($expense_m_cd, $schregno)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM money_due_m_dat ";
        $query .= " WHERE schregno     = '".$schregno."'";
        $query .="    AND year         = '".CTRL_YEAR."'";
        $query .= "   AND expense_m_cd = '".$expense_m_cd."'"; 

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //名称マスタより取得 NO001
    function getNamecd($year,$cd)
    {
        return " SELECT namecd2,name1 FROM v_name_mst WHERE year = '".$year."' AND namecd1 = '".$cd."' ";
    }

    //１レコード取得
    function getRow2($expense_s_cd, $schregno)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM money_due_s_dat ";
        $query .= " WHERE schregno     = '".$schregno."'";
        $query .="    AND year         = '".CTRL_YEAR."'";
        $query .= "   AND expense_m_cd = '".substr($expense_s_cd,2,2)."'";
        $query .= "   AND expense_s_cd = '".substr($expense_s_cd,0,2)."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    function getStudentName($schregno)
    {
        return "SELECT SCHREGNO,NAME_SHOW,NAME_KANA FROM SCHREG_BASE_MST WHERE SCHREGNO ='".$schregno."'";       
    }

    function nameGet()
    {
        $query  = "SELECT namecd2, name1 FROM v_name_mst WHERE namecd1 = 'G204' AND year = '".CTRL_YEAR."'";
        return $query;
    }
    
    function GetHrclass()
    {
        $query  = "SELECT grade, hr_class, hr_name ";
        $query .= "  FROM schreg_regd_hdat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        return $query;
    }

    //置換画面用の生徒一覧
    function GetStudent($hrClass,$sex)
    {
        $arr = explode("-",$hrClass);
        $query  = "SELECT a.name_show";
        $query .= "      ,b.schregno";
        $query .= "      ,b.attendno, c.hr_name";
        $query .= "  FROM schreg_base_mst a right outer join ";
        $query .= "       schreg_regd_dat b";
        $query .= "    ON a.schregno = b.schregno";
        $query .= "       INNER JOIN schreg_regd_hdat c ";
        $query .= "               ON b.year = c.year ";
        $query .= "              AND b.semester = c.semester ";
        $query .= "              AND b.grade    = c.grade ";
        $query .= "              AND b.hr_class = c.hr_class ";
		//NO001
		if ($sex != 99){
	        $query .= " WHERE a.sex      = '".$sex."'";
	        $query .= "   AND b.year     = '".CTRL_YEAR."'";
		}else {
	        $query .= " WHERE b.year     = '".CTRL_YEAR."'";
		}
        $query .= "   AND b.semester = '".CTRL_SEMESTER."'";
        $query .= "   AND b.grade    = '".$arr[0]."'";
        $query .= "   AND b.hr_class = '".$arr[1]."'";
        $query .= " ORDER BY attendno";

        return $query;
    }

    //中分類リストの取得(小分類がある中分類の金額は小分類の合計金額)
    function getList1($schregno)
    {
        $query .= "SELECT T1.bank_trans_sdate ";
        $query .= "      ,T1.expense_m_cd ";
        $query .= "      ,T1.expense_l_cd ";
        $query .= "      ,T1.expense_m_name ";
        $query .= "      ,CASE WHEN T1.expense_s_exist_flg = '1' THEN T3.money_due ELSE T2.money_due END AS money_due ";
        $query .= "      ,MEISYOU_GET(T2.reduction_reason, 'G204', 1) AS reduction_reason ";
        $query .= "  FROM expense_m_mst T1 ";
        $query .= "      ,money_due_m_dat T2 ";
        $query .= "       LEFT OUTER JOIN ";
        $query .= "       (SELECT expense_m_cd, SUM(money_due) AS money_due ";
        $query .= "          FROM money_due_s_dat ";
        $query .= "         WHERE year         = '".CTRL_YEAR."' ";
        $query .= "           AND schregno     = '".$schregno."' ";
        $query .= "         GROUP BY expense_m_cd) T3 ";
        $query .= "      ON T2.expense_m_cd = T3.expense_m_cd ";
        $query .= " WHERE T1.year         = T2.year ";
        $query .= "   AND T1.expense_m_cd = T2.expense_m_cd ";
        $query .= "   AND T1.year         = '".CTRL_YEAR."' ";
        $query .= "   AND T2.schregno     = '".$schregno."' ";
        return $query;
    }

    //小分類リストの取得
    function getList2($schregno)
    {
        $query .= "SELECT T3.expense_l_cd ";
        $query .= "      ,T3.expense_m_cd ";
        $query .= "      ,T2.expense_s_cd ";
        $query .= "      ,T2.expense_s_name ";
        $query .= "      ,T1.money_due ";
        $query .= "      ,T3.expense_m_name ";
        $query .= "  FROM money_due_s_dat T1 ";
        $query .= "      ,v_expense_s_mst T2 ";
        $query .= "      ,expense_m_mst T3 ";
        $query .= " WHERE T1.year         = T2.year ";
        $query .= "   AND T1.year         = T3.year ";
        $query .= "   AND T1.expense_m_cd = T2.expense_m_cd ";
        $query .= "   AND T1.expense_s_cd = T2.expense_s_cd ";
        $query .= "   AND T1.expense_m_cd = T3.expense_m_cd ";
        $query .= "   AND T2.expense_m_cd = T3.expense_m_cd ";
        $query .= "   AND T1.year         = '".CTRL_YEAR."'";
        $query .= "   AND T1.schregno     = '".$schregno."'";
        return $query;
    }

    function GetMcd()
    {
        $query .= "SELECT expense_m_cd ";
        $query .= "      ,expense_l_cd ";
        $query .= "      ,expense_m_name ";
        $query .= "      ,expense_s_exist_flg ";
        $query .= "      ,expense_m_money ";
        $query .= "  FROM expense_m_mst ";
        $query .= " WHERE year = '".CTRL_YEAR."' ";
        return $query;
    }

    //生徒の中分類コードごとに入金予定データを取得
    function ExistMcd($expense_m_cd, $schregno)
    {
        $query  = "SELECT * FROM money_due_m_dat ";
        $query .= " WHERE schregno     = '".$schregno."'";
        $query .="    AND year         = '".CTRL_YEAR."'";
        $query .= "   AND expense_m_cd = '".$expense_m_cd."'"; 
        return $query;
    }

    //生徒が持っている中分類コードの下位の小分類のみ
    function GetScd($schregno)
    {
        $query .= "SELECT T3.expense_l_cd ";
        $query .= "      ,T1.expense_m_cd ";
        $query .= "      ,T1.expense_s_cd ";
        $query .= "      ,T1.expense_s_name ";
        $query .= "      ,T1.expense_s_money ";
        $query .= "  FROM v_expense_s_mst T1 ";
        $query .= "      ,money_due_m_dat T2 ";
        $query .= "      ,expense_m_mst T3    ";
        $query .= " WHERE T1.year         = T2.year ";
        $query .= "   AND T1.year         = T3.year ";
        $query .= "   AND T1.expense_m_cd = T2.expense_m_cd ";
        $query .= "   AND T1.expense_m_cd = T3.expense_m_cd ";
        $query .= "   AND T1.year         = '".CTRL_YEAR."' ";
        $query .= "   AND T2.schregno     = '".$schregno."' ";
        $query .= "   AND T3.expense_s_exist_flg = '1' ";

        return $query;
    }

    //生徒の小分類コードごとに入金予定データを取得
    function ExistScd($expense_s_cd, $expense_m_cd, $schregno)
    {
        $query  = "SELECT * FROM money_due_s_dat ";
        $query .= " WHERE schregno     = '".$schregno."'";
        $query .="    AND year         = '".CTRL_YEAR."'";
        $query .= "   AND expense_m_cd = '".$expense_m_cd."'";
        $query .= "   AND expense_s_cd = '".$expense_s_cd."'";
        return $query;
    }

    //一括更新用（すべての小分類コードを取得)
    function GetScd2()
    {
        $query .= "SELECT T3.expense_l_cd ";
        $query .= "      ,T1.expense_m_cd ";
        $query .= "      ,T1.expense_s_cd ";
        $query .= "      ,T1.expense_s_name ";
        $query .= "      ,T1.expense_s_money ";
        $query .= "  FROM v_expense_s_mst T1 ";
        $query .= "      ,expense_m_mst T3    ";
        $query .= " WHERE T1.year         = T3.year ";
        $query .= "   AND T1.expense_m_cd = T3.expense_m_cd ";
        $query .= "   AND T1.year         = '".CTRL_YEAR."' ";
        return $query;
    }

    //入金済データチェック
    function CheckPaidData($div, $schregno, $cd)
    {
        $query  = "SELECT * FROM money_paid_".$div."_dat ";
        $query .= " WHERE SCHREGNO            = '" .$schregno."'";
        $query .= "   AND year                = '".CTRL_YEAR."'";
        $query .= "   AND expense_".$div."_cd = '".$cd."'";
        return $query;
    }

    //入金済データチェック
    function CheckPaidSdata($schregno, $cd)
    {
        $query  = "SELECT * FROM money_paid_s_dat ";
        $query .= " WHERE SCHREGNO            = '" .$schregno."'";
        $query .= "   AND year                = '".CTRL_YEAR."'";
        $query .= "   AND expense_m_cd = '".substr($cd,2,2)."'";
        $query .= "   AND expense_s_cd = '".substr($cd,0,2)."'";
        return $query;
    }

    //分納データ存在チェック
    function CheckInstData($schregno, $cd)
    {
        $query  = "SELECT * FROM money_due_m_dat ";
        $query .= " WHERE SCHREGNO            = '" .$schregno."'";
        $query .= "   AND year                = '".CTRL_YEAR."'";
        $query .= "   AND expense_m_cd        = '".$cd."'";
        $query .= "   AND inst_cd IS NOT NULL ";
        return $query;
    }

    //小分類の親の中分類コードを取得
    function getCheckMcd($cd)
    {
        $query  = "SELECT expense_m_cd ";
        $query .= "  FROM v_expense_s_mst ";
        $query .= " WHERE year          = '".CTRL_YEAR."' ";
        $query .= "   AND expense_m_cd  = '".substr($cd,2,2)."' ";
        $query .= "   AND expense_s_cd  = '".substr($cd,0,2)."' ";
        return $query;
    }

    //小分類の親のSEXを取得
    function getCheckMcdSex($cd)
    {
        $query  = "SELECT sex ";
        $query .= "  FROM v_expense_s_mst ";
        $query .= " WHERE year          = '".CTRL_YEAR."' ";
        $query .= "   AND expense_m_cd  = '".substr($cd,2,2)."' ";
        $query .= "   AND expense_s_cd  = '".substr($cd,0,2)."' ";
        return $query;
    }

    //基礎データのSEXを取得
    function getCheckBaseSex($schregno)
    {
        $query  = "SELECT sex ";
        $query .= "  FROM SCHREG_BASE_MST ";
        $query .= " WHERE SCHREGNO = '".$schregno."' ";
        return $query;
    }

    //重複チェック
    function CheckExistData($div, $schregno, $cd)
    {
        $query  = "SELECT * FROM money_due_".$div."_dat ";
        $query .= " WHERE year               = '".CTRL_YEAR."'";
        $query .= "   AND schregno           = '".$schregno."'";
		if ($div == "M") {
	        $query .= "   AND expense_".$div."_cd = '".$cd."'";
		} else {
	        $query .= "   AND expense_m_cd = '".substr($cd,2,2)."'";
	        $query .= "   AND expense_s_cd = '".substr($cd,0,2)."'";
		}
        return $query; 
    }

    //中分類の親の大分類コードを取得
    function getCheckLcd($cd)
    {
        $query  = "SELECT EXPENSE_L_CD ";
        $query .= "FROM EXPENSE_M_MST ";
        $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND EXPENSE_M_CD  = '".$cd."' ";
        return $query;
    }

    //生活行事費(大分類CD=03)重複チェック
    function Check03Mdata($div, $schregno)
    {
        $query  = "SELECT ";
        $query .= "    * ";
        $query .= "FROM ";
        $query .= "    MONEY_DUE_M_DAT T1, ";
        $query .= "    EXPENSE_M_MST T2 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SCHREGNO = '".$schregno."' ";
        $query .= "    AND T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.EXPENSE_M_CD = T1.EXPENSE_M_CD ";
        $query .= "    AND T2.EXPENSE_L_CD = '03' ";
        return $query; 
    }

    //中分類の登録確認
    function CheckSdata($div, $schregno, $cd)
    {
        $query  = "SELECT ";
        $query .= "    * ";
        $query .= "FROM ";
        $query .= "    MONEY_DUE_M_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SCHREGNO = '".$schregno."' ";
        $query .= "    AND EXPENSE_M_CD = '".$cd."' ";
        return $query; 
    }

    //更新クエリ
    function &getUpdateQuery($div, $field, $schregno)
    {
        if ($div == "M") {

            $query  = "UPDATE money_due_m_dat ";
            $query .= "   SET money_due        =  ".(strlen($field["MONEY_DUE"]) ? $field["MONEY_DUE"] : "NULL");
            $query .= "      ,reduction_reason = '".$field["REDUCTION_REASON"]."'";
            $query .= "      ,UN_AUTO_PAYFLG = ". ($field["UN_AUTO_PAYFLG"] ? "'".$field["UN_AUTO_PAYFLG"]."'" : "NULL");
            $query .= "      ,registercd       = '".STAFFCD."'";
            $query .= "      ,updated          = SYSDATE() ";
            $query .= " WHERE year         = '".CTRL_YEAR."'";
            $query .= "   AND schregno     = '".$schregno."'";
            $query .= "   AND expense_m_cd = '".$field["EXPENSE_M_CD"]."'";
        
        //親の中分類が既に登録されている場合のみ子の小分類を更新
        } elseif ($div == "S") {
        
            $query  = "UPDATE money_due_s_dat ";
            $query .= "   SET money_due        =  ".(strlen($field["MONEY_DUE"]) ? $field["MONEY_DUE"] : "NULL");
            $query .= "      ,registercd       = '".STAFFCD."'";
            $query .= "      ,updated          = SYSDATE() ";
            $query .= " WHERE year         = '".CTRL_YEAR."'";
            $query .= "   AND schregno     = '".$schregno."'";
            $query .= "   AND expense_m_cd = '".substr($field["EXPENSE_S_CD"],2,2)."'";
            $query .= "   AND expense_s_cd = '".substr($field["EXPENSE_S_CD"],0,2)."'";
            $query .= "   AND EXISTS (SELECT 'X' ";
            $query .= "                 FROM money_due_m_dat T1 ";
            $query .= "                     ,v_expense_s_mst   T2 ";
            $query .= "                WHERE T1.year         = T2.year ";
            $query .= "                  AND T1.expense_m_cd = T2.expense_m_cd ";
            $query .= "                  AND T1.year         = '".CTRL_YEAR."' ";
            $query .= "                  AND T1.schregno     = '".$schregno."' ";
            $query .= "                  AND T2.expense_s_cd = '".substr($field["EXPENSE_S_CD"],0,2)."')";
        }
        return $query;
    }


    //削除クエリ
    function &getDeleteQuery($div, $field, $schregno)
    {
        if ($div == "M") {
        
            $query  = "DELETE FROM money_due_m_dat ";
            $query .= " WHERE SCHREGNO     = '" .$schregno." '";
            $query .= "   AND year         = '".CTRL_YEAR."'";
            $query .= "   AND expense_m_cd = '".$field["EXPENSE_M_CD"]."'";

        } elseif ($div == "S") {

            $query  = "DELETE FROM money_due_s_dat ";
            $query .= " WHERE SCHREGNO     = '" .$schregno."'";
            $query .= "   AND year         = '".CTRL_YEAR."'";
            $query .= "   AND expense_m_cd = '".substr($field["EXPENSE_S_CD"],2,2)."'";
            $query .= "   AND expense_s_cd = '".substr($field["EXPENSE_S_CD"],0,2)."'";
        }
        return $query;
    }
    
    //中分類の下位の小分類も削除クエリ
    function &getDeleteQuery2($field, $schregno)
    {
        $query  = "DELETE FROM money_due_s_dat ";
        $query .= " WHERE SCHREGNO     = '" .$schregno." '";
        $query .= "   AND year         = '".CTRL_YEAR."'";
        $query .= "   AND expense_m_cd = '".$field["EXPENSE_M_CD"]."'";
        return $query;
    }

    //追加クエリ
    function &getAddQuery($div, $field, $schregno)
    {
        if ($div == "M") {

            $query  = "INSERT INTO money_due_m_dat ";
            $query .= "       (year, schregno, expense_m_cd, money_due, reduction_reason, UN_AUTO_PAYFLG, registercd, updated) ";
            $query .= "VALUES ( '".CTRL_YEAR."'" ;
            $query .= "        ,'".$schregno."'";
            $query .= "        ,'".$field["EXPENSE_M_CD"]."'";
            $query .= "        ,".(strlen($field["MONEY_DUE"]) ? $field["MONEY_DUE"] : "NULL");
            $query .= "        ,'".$field["REDUCTION_REASON"]."'";
            $query .= "        ,". ($field["UN_AUTO_PAYFLG"] ? "'".$field["UN_AUTO_PAYFLG"]."'" : "NULL");
            $query .= "        ,'".STAFFCD."'";
            $query .= "        ,SYSDATE() ";
            $query .= "       ) ";

        //親の中分類が既に登録されている場合のみ子の小分類を追加
        } elseif ($div == "S") {

            $query  = "INSERT INTO money_due_s_dat ";
            $query .= "       (year, schregno, expense_m_cd, expense_s_cd, money_due, registercd, updated) ";
            $query .= "VALUES ( '".CTRL_YEAR."'" ;
            $query .= "        ,'".$schregno."'";
            $query .= "        ,'".substr($field["EXPENSE_S_CD"],2,2)."'";
            $query .= "        ,'".substr($field["EXPENSE_S_CD"],0,2)."'";
            $query .= "        ,".(strlen($field["MONEY_DUE"]) ? $field["MONEY_DUE"] : "NULL");
            $query .= "        ,'".STAFFCD."'";
            $query .= "        ,SYSDATE() ";
            $query .= "       ) ";
        }
        return $query;
    }
}
?>
