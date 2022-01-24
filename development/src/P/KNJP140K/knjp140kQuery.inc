<?php

require_once('for_php7.php');

class knjp140kQuery extends Query {

    //１レコード取得
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM installment_dat ";
        $query .= " WHERE schregno = '".$model->schregno."'";
        $query .="    AND year     = '".CTRL_YEAR."'";
        $query .= "   AND inst_cd  = '".$model->inst_cd."'";
        $query .= "   AND inst_seq = ".$model->inst_seq."";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //生徒氏名
    function getStudentName($schregno)
    {
        return "SELECT SCHREGNO,NAME_SHOW,NAME_KANA FROM SCHREG_BASE_MST WHERE SCHREGNO ='".$schregno."'";       
    }

    //名称
    function nameGet()
    {
        $query  = "SELECT namecd2, name1 FROM v_name_mst WHERE namecd1 = 'G205' AND year = '".CTRL_YEAR."'";
        return $query;
    }

    //入金予定データ
    function getList1($schregno)
    {
        $query .= "SELECT T2.inst_cd";
        $query .= "      ,T3.expense_m_cd ";
        $query .= "      ,T3.expense_m_name ";
        $query .= "      ,T3.expense_s_exist_flg ";
        $query .= "      ,T2.money_due ";
        $query .= "      ,SUM(T1.paid_money)      AS paid_money ";
        $query .= "      ,SUM(T1.repay_money)     AS repay_money ";
        $query .= "      ,MAX(T1.paid_money_date) AS paid_money_date ";
        $query .= "  FROM money_due_m_dat T2 ";
        $query .= "       INNER JOIN expense_m_mst T3 ";
        $query .= "               ON T2.year         = T3.year ";
        $query .= "              AND T2.expense_m_cd = T3.expense_m_cd ";
        $query .= "       LEFT OUTER JOIN ";
        $query .= "       installment_dat T1 ";
        $query .= "    ON T1.year         = T2.year ";
        $query .= "   AND T1.schregno     = T2.schregno ";
        $query .= "   AND T1.inst_cd      = T2.inst_cd ";
        $query .= " WHERE T2.schregno     = '".$schregno."'";
        $query .= "   AND T2.year         = '".CTRL_YEAR."'";
        $query .= "   AND COALESCE(T2.inst_cd, '') <> ''";  //INST_CDがNULLまたは空じゃないもの
        $query .= " GROUP BY T2.inst_cd ";
        $query .= "         ,T3.expense_m_cd,T3.expense_m_name ";
        $query .= "         ,T2.money_due,T3.expense_s_exist_flg ";
        //echo $query;
        return $query;
    }

    //入金必要金額の取得
    function getMoneyDue($schregno, $m_cd)
    {
        $query  = " SELECT ";
        $query .= "     sum(money_due) ";
        $query .= "   FROM ";
        $query .= "     money_due_s_dat ";
        $query .= "  WHERE ";
        $query .= "     year = '".CTRL_YEAR."' AND ";
        $query .= "     schregno = '".$schregno."' AND ";
        $query .= "     expense_m_cd = '".$m_cd."'";

        return $query;
    }

    //分納データ
    function getList2($schregno, $inst_cd)
    {
        $query .= "SELECT inst_cd, inst_seq,inst_due_date,inst_money_due,paid_money_date,paid_money,";
        $query .= "       MEISYOU_GET(paid_money_div, 'G205', 1) AS paid_money_div,";
        $query .= "       repay_money,repay_date,remark";
        $query .= "  FROM installment_dat ";
        $query .= " WHERE year         = '".CTRL_YEAR."'";
        $query .= "   AND schregno     = '".$schregno."'";
        $query .= "   AND inst_cd      = '".$inst_cd."'";
        return $query;
    }

    //生徒交付データよりデータを取得 NO001 NO004
    function getlist3($schregno)
    {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRANTCD, ";
        $query .= "     MEISYOU_GET(GRANTCD,'G212',1) GRANTNAME, ";
        $query .= "     GRANTSDATE, ";
        $query .= "     GRANTEDATE, ";
        $query .= "     GRANT_MONEY, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_GRANT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY GRANTSDATE,GRANTCD ";

        return $query;
    }

    //入金予定中分類(INST_CDが他のコードに設定されているものは表示しない）
    function GetMcd($schregno, $inst_cd)
    {
        $query .= "SELECT T1.expense_s_exist_flg ";
        $query .= "      ,T1.expense_m_cd ";
        $query .= "      ,T1.expense_l_cd ";
        $query .= "      ,T1.expense_m_name ";
        $query .= "      ,T2.money_due ";
        $query .= "      ,T2.inst_cd ";
        $query .= "      ,T2.year ";
        $query .= "      ,T2.schregno ";
        $query .= "  FROM expense_m_mst T1 ";
        $query .= "      ,money_due_m_dat T2 ";
        $query .= " WHERE T1.year = T2.year";
        $query .= "   AND T1.expense_m_cd = T2.expense_m_cd ";
        $query .= "   AND T1.year         = '".CTRL_YEAR."'";
        $query .= "   AND T2.schregno     = '".$schregno."'";
        $query .= "   AND (T2.inst_cd = '".$inst_cd."' OR COALESCE(T2.inst_cd, '') = '')";
        return $query;
    }

    //入金済みデータが存在するものは表示しない
    function PaidCehck($row)
    {
        $query  = " SELECT 'X' ";
        $query .= "   FROM money_paid_m_dat ";
        $query .= "  WHERE year             = '".$row["YEAR"]."' ";
        $query .= "    AND schregno         = '".$row["SCHREGNO"]."' ";
        $query .= "    AND expense_l_cd     = '".$row["EXPENSE_L_CD"]."' ";
        $query .= "    AND expense_m_cd     = '".$row["EXPENSE_M_CD"]."' ";
        return $query;
    }

    //重複チェック
    function CheckExistData($schregno, $selectdata, $cd)
    {
        $query  = "SELECT COUNT(*) FROM money_due_m_dat ";
        $query .= " WHERE year         = '".CTRL_YEAR."'";
        $query .= "   AND schregno     = '".$schregno."'";
        $query .= "   AND inst_cd      <> '".$cd."'";
        $query .= "   AND expense_m_cd IN ('".str_replace(",","','",$selectdata)."')"; 
        #echo $query; 
        return $query; 
    }

    //新INST_CD取得
    function GetNewInst_cd($schregno)
    {
        $query  = "SELECT MAX(inst_cd) FROM money_due_m_dat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND schregno = '".$schregno."'";
        return $query;
    }
    
    //入金済データチェック
    function CheckPaidData($schregno, $inst_cd, $flg)
    {
        $query  = "SELECT T1.* ";
        $query .= "  FROM money_paid_m_dat T1 ";
        $query .= " WHERE T1.year         = '".CTRL_YEAR."'";
        $query .= "   AND T1.schregno     = '".$schregno."'";
        $query .= "   AND T1.expense_l_cd || T1.expense_m_cd ";
        $query .= "       IN (SELECT T2.expense_l_cd  || T3.expense_m_cd ";
        $query .= "             FROM expense_m_mst T2, money_due_m_dat T3 ";
        $query .= "            WHERE T1.year         = T2.year ";
        $query .= "              AND T1.expense_m_cd = T2.expense_m_cd ";
        $query .= "              AND T1.schregno     = T3.schregno ";
        $query .= "              AND T1.year         = T3.year ";
        $query .= "              AND T1.expense_m_cd = T3.expense_m_cd ";
        $query .= "              AND T3.inst_cd      = '".$inst_cd."' ";
        $query .= "          )";
        $query .= "   AND T1.paid_input_flg = '2'";

        if ($flg == 2) {
            $query .= "   AND (T1.REPAY_DATE IS NOT NULL ";
            $query .= "    OR  T1.REPAY_MONEY IS NOT NULL ";
            $query .= "    OR  T1.REPAY_DEV IS NOT NULL) ";
        }
        return $query;
    }

    //分納データ取得
    function getInstCheck($schregno, $inst_cd)
    {
        $query  = " SELECT * ";
        $query .= "   FROM installment_dat ";
        $query .= "  WHERE year      = '".CTRL_YEAR."' ";
        $query .= "    AND schregno  = '".$schregno."' ";
        $query .= "    AND inst_cd   = '".$inst_cd."' ";
        return $query;
    }

    //分納対象費目編集
    function &getUpdate1Query(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->cmd == "add1" && !strlen($model->selectdata)) {
            $model->setWarning("MSG304");
            return false;
        }
        
        $m_cd_array = array();
        if (strlen($model->selectdata)) {
            $m_cd_array = explode(",", $model->selectdata);
        }

        $query = knjp140kQuery::CheckPaidData($model->schregno, $model->inst_cd, 1);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //入金済データチェック
        if (is_array($row)) {
            $model->setWarning("MSG203","入金済データがあるので更新できません。");
            Query::dbCheckIn($db);
            return false;
        }

        //分納データチェック
        $row = $db->getRow(knjp140kQuery::getInstCheck($model->schregno, $model->inst_cd));
        if (is_array($row) && !strlen($model->selectdata)) {
            $model->setWarning("MSG203","分納のデータが存在します。\\n分納のデータを削除した後、処理を行なって下さい。");
            Query::dbCheckIn($db);
            return false;
        }

        //更新ボタン時
        if ($model->cmd == "update1") {

            //INST_CDを一度NULLに戻す
            $query  = "UPDATE money_due_m_dat ";
            $query .= "   SET inst_cd = NULL ";
            $query .= " WHERE year     = '".CTRL_YEAR."'";
            $query .= "   AND schregno = '".$model->schregno."'";
            $query .= "   AND inst_cd  = '".$model->inst_cd."'";
            $db->query($query);

        } elseif ($model->cmd == "add1") {

            //新INST_CD
            $inst_cd = $db->getOne(knjp140kQuery::GetNewInst_cd($model->schregno));
            $inst_cd = sprintf("%02d", ((int)$inst_cd + 1));

            //重複チェック
            $query = knjp140kQuery::CheckExistData($model->schregno, $model->selectdata, $inst_cd);
            if ($db->getOne($query) != "0") {
                $this->setWarning("MSG302");
                Query::dbCheckIn($db);
                return false;
            }
            $model->inst_cd = $inst_cd;
        }

        foreach ($m_cd_array as $m_cd)
        {
            $query  = "UPDATE money_due_m_dat ";
			if ($model->inst_cd){
	            $query .= "   SET inst_cd      = '".$model->inst_cd."'";
			}else {
	            $query .= "   SET inst_cd      = NULL ";
			}
            $query .= " WHERE year         = '".CTRL_YEAR."'";
            $query .= "   AND schregno     = '".$model->schregno."'";
            $query .= "   AND expense_m_cd = '".$m_cd."'";
            $db->query($query);
        }
        if (get_count($m_cd_array) == 0) {
            unset($model->inst_cd);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //新INST_SEQ取得
    function GetNewSeq($schregno, $inst_cd)
    {
        $query  = "SELECT MAX(inst_seq) + 1 FROM installment_dat ";
        $query .= " WHERE schregno = '".$schregno."'";
        $query .= "   AND year     = '".CTRL_YEAR."'";
        $query .= "   AND inst_cd  = '".$inst_cd."'";
        return $query;
    }

    //分納期限重複チェック
    function ExistCheck($model)
    {
        $query  = "SELECT * FROM installment_dat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND schregno = '".$model->schregno."'";
        $query .= "   AND inst_cd  = '".$model->inst_cd."'";
        if ($this->cmd == "update") {
            $query .= "AND inst_seq <> ".$model->inst_seq;
        }
        $query .= "   AND inst_due_date = '".str_replace("/", "-", $model->field["INST_DUE_DATE"])."' ";
        //echo $query;
        return $query; 
    }  

    //重複チェック
    function CheckData($schregno, $inst_cd, $inst_seq)
    {
        $query  = "SELECT * FROM installment_dat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND schregno = '".$schregno."'";
        $query .= "   AND inst_cd  = '".$inst_cd."'";
        $query .= "   AND inst_seq = ".$inst_seq;
        return $query; 
    }

    //追加クエリ
    function &getAddQuery($model)
    {
        $query  = "INSERT INTO installment_dat ";
        $query .= "VALUES ('".CTRL_YEAR."'" ;
        $query .= "        ,'".$model->schregno."'";
        $query .= "        ,'".$model->inst_cd ."'";
        $query .= "        , ".$model->inst_seq;
        $query .= "        ," .(strlen($model->field["INST_DUE_DATE"])   ? "DATE('".str_replace('/','-',$model->field["INST_DUE_DATE"])."')" : "CAST(NULL AS DATE)");
        $query .= "        ," .(strlen($model->field["INST_MONEY_DUE"])  ? $model->field["INST_MONEY_DUE"] : "NULL");
        $query .= "        ," .(strlen($model->field["PAID_MONEY_DATE"]) ? "DATE('".str_replace('/','-',$model->field["PAID_MONEY_DATE"])."')" : "CAST(NULL AS DATE)");
        $query .= "        ," .(strlen($model->field["PAID_MONEY"]) ? $model->field["PAID_MONEY"] : "NULL");
        $query .= "        ,'".$model->field["PAID_MONEY_DIV"] ."'";
        $query .= "        ," .(strlen($model->field["REPAY_DATE"]) ? "DATE('".str_replace('/','-',$model->field["REPAY_DATE"])."')" : "CAST(NULL AS DATE)");
        $query .= "        ," .(strlen($model->field["REPAY_MONEY"]) ? $model->field["REPAY_MONEY"] : "NULL");
        $query .= "        ,'".$model->field["REMARK"] ."'";
        $query .= "        ,'".STAFFCD."'";
        $query .= "        ,SYSDATE()) ";

        return $query;

    }

    //更新クエリ
    function getUpdateQuery($model)
    {
        $query  = "UPDATE installment_dat ";
        $query .= "   SET INST_DUE_DATE   = DATE('".str_replace('/','-',$model->field["INST_DUE_DATE"]) ."')";
        $query .= "      ,INST_MONEY_DUE  =  ".(strlen($model->field["INST_MONEY_DUE"]) ? $model->field["INST_MONEY_DUE"] : "NULL");
        $query .= "      ,PAID_MONEY_DATE = " .(strlen($model->field["PAID_MONEY_DATE"]) ? "DATE('".str_replace('/','-',$model->field["PAID_MONEY_DATE"])."')" : "CAST(NULL AS DATE)");
        $query .= "      ,PAID_MONEY      =  ".(strlen($model->field["PAID_MONEY"]) ? $model->field["PAID_MONEY"] : "NULL");
        $query .= "      ,PAID_MONEY_DIV  = '".$model->field["PAID_MONEY_DIV"] ."'";
        $query .= "      ,REPAY_DATE      = " .(strlen($model->field["REPAY_DATE"]) ? "DATE('".str_replace('/','-',$model->field["REPAY_DATE"])."')" : "CAST(NULL AS DATE)");
        $query .= "      ,REPAY_MONEY     =  ".(strlen($model->field["REPAY_MONEY"]) ? $model->field["REPAY_MONEY"] : "NULL");
        $query .= "      ,REMARK          = '".$model->field["REMARK"] ."'";
        $query .= "      ,registercd      = '".STAFFCD."'";
        $query .= "      ,updated         = sysdate() ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND schregno = '".$model->schregno."'";
        $query .= "   AND inst_cd  = '".$model->inst_cd."'";
        $query .= "   AND inst_seq =  ".$model->inst_seq;  

        //echo $query;
        return $query;
    }

    //削除クエリ
    function &getDeleteQuery($model)
    {
        $query  = "DELETE FROM installment_dat ";
        $query .= " WHERE SCHREGNO     = '" .$model->schregno." '";
        $query .= "   AND year         = '".CTRL_YEAR."'";
        $query .= "   AND inst_cd      = '".$model->inst_cd."'";
        $query .= "   AND inst_seq     =  ".$model->inst_seq;
        return $query;
    }
    
    //SEQ順番を更新
    function MakeSeq($schregno, $inst_cd)
    {
        $query  = "SELECT 1 AS new_seq, inst_seq FROM Installment_dat ";
        $query .= " WHERE schregno = '".$schregno."' ";
        $query .= "   AND year     = '".CTRL_YEAR."'";
        $query .= "   AND inst_cd  = '".$inst_cd."'";
        $query .= " order by inst_seq ";
        return $query;
    }

    //分納納入期限の昇順でデータ取得
    function getInstData($schregno, $inst_cd)
    {
        $query  = "SELECT * FROM Installment_dat ";
        $query .= " WHERE schregno = '".$schregno."' ";
        $query .= "   AND year     = '".CTRL_YEAR."'";
        $query .= "   AND inst_cd  = '".$inst_cd."'";
        $query .= " order by inst_due_date ";
        return $query;
    }

    //削除クエリ2
    function &getDeleteQuery2($model)
    {
        $query  = "DELETE FROM installment_dat ";
        $query .= " WHERE SCHREGNO     = '" .$model->schregno." '";
        $query .= "   AND year         = '".CTRL_YEAR."'";
        $query .= "   AND inst_cd      = '".$model->inst_cd."'";
        return $query;
    }

    //追加クエリ2
    function &getAddQuery2($tmp_arr)
    {
        $query  = "INSERT INTO installment_dat ";
        $query .= "VALUES ('".CTRL_YEAR."'" ;
        $query .= "        ,'".$tmp_arr["SCHREGNO"]."'";
        $query .= "        ,'".$tmp_arr["INST_CD"]."'";
        $query .= "        , ".$tmp_arr["INST_SEQ"];
        $query .= "        ," .(strlen($tmp_arr["INST_DUE_DATE"]) ? "DATE('".$tmp_arr["INST_DUE_DATE"]."')" : "CAST(NULL AS DATE)");
        $query .= "        ," .(strlen($tmp_arr["INST_MONEY_DUE"]) ? $tmp_arr["INST_MONEY_DUE"] : "NULL");
        $query .= "        ," .(strlen($tmp_arr["PAID_MONEY_DATE"]) ? "DATE('".$tmp_arr["PAID_MONEY_DATE"]."')" : "CAST(NULL AS DATE)");
        $query .= "        ," .(strlen($tmp_arr["PAID_MONEY"]) ? $tmp_arr["PAID_MONEY"] : "NULL");
        $query .= "        ,'".$tmp_arr["PAID_MONEY_DIV"]."'";
        $query .= "        ," .(strlen($tmp_arr["REPAY_DATE"]) ? "DATE('".str_replace('/','-',$tmp_arr["REPAY_DATE"])."')" : "CAST(NULL AS DATE)");
        $query .= "        ," .(strlen($tmp_arr["REPAY_MONEY"]) ? $tmp_arr["REPAY_MONEY"] : "NULL");
        $query .= "        ,'".$tmp_arr["REMARK"] ."'";
        $query .= "        ,'".STAFFCD."'";
        $query .= "        ,SYSDATE()) ";

        return $query;
    }

    //新INST_SEQ取得2
    function GetNewSeq2($model)
    {
        $query  = "SELECT inst_seq FROM installment_dat ";
        $query .= " WHERE schregno      = '".$model->schregno."'";
        $query .= "   AND year          = '".CTRL_YEAR."'";
        $query .= "   AND inst_cd       = '".$model->inst_cd."'";
        $query .= "   AND inst_due_date = '".str_replace("/", "-", $model->field["INST_DUE_DATE"])."' ";
        return $query;
    }

    //納入済、納入必要金額合計
    function GetSumMoney($schregno, $inst_cd)
    {
        $query  = "SELECT SUM(inst_money_due) as money_due ";
        $query .= "      ,SUM(paid_money)     as paid_money ";
        $query .= "      ,SUM(repay_money)    as repay_money ";
        $query .= "  FROM installment_dat ";
        $query .= " WHERE schregno = '".$schregno."'";
        $query .= "   AND year     = '".CTRL_YEAR."'";
        $query .= "   AND inst_cd  = '".$inst_cd."'";
        return $query;
    }
    
    //入金済データ作成
    function InsertPaidMoney($db, $schregno, $inst_cd)
    {
        $query  = "SELECT T1.year,T1.schregno,T3.expense_l_cd,T3.expense_s_exist_flg, ";
        $query .= "       T1.expense_m_cd,T2.paid_money_date, ";
        $query .= "       T1.money_due AS paid_money,T2.paid_money_div ";
        $query .= "  FROM money_due_m_dat T1, ";
        $query .= "    (SELECT DISTINCT inst_cd, paid_money_div, paid_money_date ";
        $query .= "      FROM installment_dat T1 ";
        $query .= "     WHERE year            = '".CTRL_YEAR."' ";
        $query .= "       AND schregno        = '".$schregno."' ";
        $query .= "       AND inst_cd         = '".$inst_cd."' ";
        $query .= "       AND paid_money_date = (SELECT MAX(T2.paid_money_date) AS paid_money_date  ";
        $query .= "                                FROM installment_dat T2 ";
        $query .= "                               WHERE T1.year     = T2.year  ";
        $query .= "                                 AND T1.schregno = T2.schregno ";
        $query .= "                                 AND T1.inst_cd  = T2.inst_cd)) T2, ";
        $query .= "       expense_m_mst T3 ";
        $query .= " WHERE T1.inst_cd = T2.inst_cd ";
        $query .= "   AND T1.year = T3.year ";
        $query .= "   AND T1.expense_m_cd = T3.expense_m_cd ";
        $query .= "   AND T1.year     = '".CTRL_YEAR."' ";
        $query .= "   AND T1.schregno = '".$schregno."' ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //小分類を持っている中分類の場合、小分類の合計金額を取得
            if ($row["EXPENSE_S_EXIST_FLG"] == "1") {
                $row["PAID_MONEY"] = $db->getOne(knjp140kQuery::getMoneyDue($row["SCHREGNO"], $row["EXPENSE_M_CD"]));
            }

            //入金済み中分類データが存在するかチェック
            $query  = " SELECT 'X' ";
            $query .= "   FROM money_paid_m_dat ";
            $query .= "  WHERE year             = '".$row["YEAR"]."' ";
            $query .= "    AND schregno         = '".$row["SCHREGNO"]."' ";
            $query .= "    AND expense_l_cd     = '".$row["EXPENSE_L_CD"]."' ";
            $query .= "    AND expense_m_cd     = '".$row["EXPENSE_M_CD"]."' ";
            $query .= "    AND paid_input_flg   = '2' ";

            $exist = $db->getOne($query);

            if (!strlen($exist)) {
                $data = array();
                $data["YEAR"][TEXT]             = $row["YEAR"];
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["EXPENSE_L_CD"][TEXT]     = $row["EXPENSE_L_CD"];
                $data["EXPENSE_M_CD"][TEXT]     = $row["EXPENSE_M_CD"];
                $data["PAID_INPUT_FLG"][TEXT]   = '2';
                $data["PAID_MONEY_DATE"][TEXT]  = $row["PAID_MONEY_DATE"];
                $data["PAID_MONEY"][NUMBER]     = $row["PAID_MONEY"];
                $data["PAID_MONEY_DIV"][TEXT]   = $row["PAID_MONEY_DIV"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, "money_paid_m_dat");
            } else {
                $data = array();
                $data["PAID_MONEY_DATE"][TEXT]   = $row["PAID_MONEY_DATE"];
                $data["PAID_MONEY"][NUMBER]      = $row["PAID_MONEY"];
                $data["PAID_MONEY_DIV"][TEXT]    = $row["PAID_MONEY_DIV"];
                $data["REGISTERCD"][TEXT]        = STAFFCD;
                $data["UPDATED"][FUNC]           = "SYSDATE()";

                $where  = " WHERE year           = '".$row["YEAR"]."' ";
                $where .= "   AND schregno       = '".$row["SCHREGNO"]."' ";
                $where .= "   AND expense_l_cd   = '".$row["EXPENSE_L_CD"]."' ";
                $where .= "   AND expense_m_cd   = '".$row["EXPENSE_M_CD"]."' ";
                $where .= "   AND paid_input_flg = '2' ";

                $query = Query::updateSQL($data, "money_paid_m_dat", $where);
            }
            $db->query($query);


            //中分類に属する小分類の取得
            $query  = " SELECT expense_s_cd, money_due AS s_paid_money ";
            $query .= "   FROM money_due_s_dat ";
            $query .= "  WHERE year = '".$row["YEAR"]."' ";
            $query .= "    AND schregno = '".$row["SCHREGNO"]."' ";
            $query .= "    AND expense_m_cd = '".$row["EXPENSE_M_CD"]."' ";

            $result2 = $db->query($query);
            while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC))
            {
                //入金済み小分類データにデータが存在するかチェック
                $query  = " SELECT 'X' ";
                $query .= "   FROM money_paid_s_dat ";
                $query .= "  WHERE year             = '".$row["YEAR"]."' ";
                $query .= "    AND schregno         = '".$row["SCHREGNO"]."' ";
                $query .= "    AND expense_m_cd     = '".$row["EXPENSE_M_CD"]."' ";
                $query .= "    AND expense_s_cd     = '".$row2["EXPENSE_S_CD"]."' ";
                $query .= "    AND paid_input_flg   = '2' ";

                $exist2 = $db->getOne($query);
                
                if (!strlen($exist2)) {
                    $data = array();
                    $data["YEAR"][TEXT]             = $row["YEAR"];
                    $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                    $data["EXPENSE_M_CD"][TEXT]     = $row["EXPENSE_M_CD"];
                    $data["EXPENSE_S_CD"][TEXT]     = $row2["EXPENSE_S_CD"];
                    $data["PAID_INPUT_FLG"][TEXT]   = '2';
                    $data["PAID_MONEY_DATE"][TEXT]  = $row["PAID_MONEY_DATE"];
                    $data["PAID_MONEY"][NUMBER]     = $row2["S_PAID_MONEY"];
                    $data["PAID_MONEY_DIV"][TEXT]   = $row["PAID_MONEY_DIV"];
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "SYSDATE()";

                    $query = Query::insertSQL($data, "money_paid_s_dat");
                } else {
                    $data = array();
                    $data["PAID_MONEY_DATE"][TEXT]   = $row["PAID_MONEY_DATE"];
                    $data["PAID_MONEY"][NUMBER]      = $row2["S_PAID_MONEY"];
                    $data["PAID_MONEY_DIV"][TEXT]    = $row["PAID_MONEY_DIV"];
                    $data["REGISTERCD"][TEXT]        = STAFFCD;
                    $data["UPDATED"][FUNC]           = "SYSDATE()";
        
                    $where  = " WHERE year           = '".$row["YEAR"]."' ";
                    $where .= "   AND schregno       = '".$row["SCHREGNO"]."' ";
                    $where .= "   AND expense_m_cd   = '".$row["EXPENSE_M_CD"]."' ";
                    $where .= "   AND expense_s_cd   = '".$row2["EXPENSE_S_CD"]."' ";
                    $where .= "   AND paid_input_flg = '2' ";

                    $query = Query::updateSQL($data, "money_paid_s_dat", $where);
                }
                $db->query($query);
            }
        }
        return;
    }

    //入金済データ取消
    function PaidMoneyClear($db, $schregno, $inst_cd)
    {
        $flg = false;

        $query  = "SELECT T1.year,T1.schregno,T2.expense_l_cd,T1.expense_m_cd, ";
        $query .= "       T2.expense_s_exist_flg,T1.money_due,T1.inst_cd ";
        $query .= "  FROM money_due_m_dat T1, ";
        $query .= "       expense_m_mst T2 ";
        $query .= " WHERE T1.year = T2.year ";
        $query .= "   AND T1.expense_m_cd = T2.expense_m_cd ";
        $query .= "   AND T1.year     = '".CTRL_YEAR."' ";
        $query .= "   AND T1.schregno = '".$schregno."' ";
        $query .= "   AND T1.inst_cd = '".$inst_cd."' ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query  = " SELECT * ";
            $query .= "   FROM money_paid_m_dat ";
            $query .= "  WHERE year             = '".$row["YEAR"]."' ";
            $query .= "    AND schregno         = '".$row["SCHREGNO"]."' ";
            $query .= "    AND expense_l_cd     = '".$row["EXPENSE_L_CD"]."' ";
            $query .= "    AND expense_m_cd     = '".$row["EXPENSE_M_CD"]."' ";
            $query .= "    AND paid_input_flg   = '2' ";

            $row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if (is_array($row2)) {
                //入金取消処理が発生
                $flg = true;

                //返金情報があれば入金情報をNULLで更新
                if (strlen($row2["REPAY_DATE"]) || strlen($row2["REPAY_MONEY"]) || strlen($row2["REPAY_DEV"])) {
                    $data = array();
                    $data["PAID_MONEY_DATE"][FUNC]   = "CAST(NULL as date)";
                    $data["PAID_MONEY"][NUMBER]      = "CAST(NULL as integer)";
                    $data["PAID_MONEY_DIV"][TEXT]    = NULL;
                    $data["REGISTERCD"][TEXT]        = STAFFCD;
                    $data["UPDATED"][FUNC]           = "SYSDATE()";

                    $where  = " WHERE year           = '".$row2["YEAR"]."' ";
                    $where .= "   AND schregno       = '".$row2["SCHREGNO"]."' ";
                    $where .= "   AND expense_l_cd   = '".$row2["EXPENSE_L_CD"]."' ";
                    $where .= "   AND expense_m_cd   = '".$row2["EXPENSE_M_CD"]."' ";
                    $where .= "   AND paid_input_flg = '2' ";

                    $query  = Query::updateSQL($data, "money_paid_m_dat", $where);
                } else {
                    //返金情報がなければ削除
                    $query  = "DELETE FROM money_paid_m_dat ";
                    $query .= " WHERE year         = '".$row2["YEAR"]."' ";
                    $query .= "   AND schregno     = '".$row2["SCHREGNO"]."' ";
                    $query .= "   AND expense_l_cd = '".$row2["EXPENSE_L_CD"]."' ";
                    $query .= "   AND expense_m_cd = '".$row2["EXPENSE_M_CD"]."' ";
                    $query .= "   AND paid_input_flg = '2' ";
                }
                $db->query($query);

                //入金済み小分類の取得
                $query  = " SELECT * ";
                $query .= "   FROM money_paid_s_dat ";
                $query .= "  WHERE year           = '".$row2["YEAR"]."' ";
                $query .= "    AND schregno       = '".$row2["SCHREGNO"]."' ";
                $query .= "    AND expense_m_cd   = '".$row2["EXPENSE_M_CD"]."' ";
                $query .= "    AND paid_input_flg = '2' ";

                $result2 = $db->query($query);
                while ($row3 = $result2->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    if (is_array($row3)) {
                        //返金情報があれば入金情報をNULLで更新
                        if (strlen($row3["REPAY_MONEY_DATE"]) || strlen($row3["REPAY_MONEY"]) || 
                            strlen($row3["REPAY_MONEY_DIV"]) || strlen($row3["REPAY_FLG"])) {

                            $data = array();
                            $data["PAID_MONEY_DATE"][FUNC]   = "CAST(NULL as date)";
                            $data["PAID_MONEY"][NUMBER]      = "CAST(NULL as integer)";
                            $data["PAID_MONEY_DIV"][TEXT]    = NULL;
                            $data["REGISTERCD"][TEXT]        = STAFFCD;
                            $data["UPDATED"][FUNC]           = "SYSDATE()";
        
                            $where  = " WHERE year           = '".$row3["YEAR"]."' ";
                            $where .= "   AND schregno       = '".$row3["SCHREGNO"]."' ";
                            $where .= "   AND expense_m_cd   = '".$row3["EXPENSE_M_CD"]."' ";
                            $where .= "   AND expense_s_cd   = '".$row3["EXPENSE_S_CD"]."' ";
                            $where .= "   AND paid_input_flg = '2' ";

                            $query = Query::updateSQL($data, "money_paid_s_dat", $where);
                        } else {
                            //返金情報がなければ削除
                            $query  = "DELETE FROM money_paid_s_dat ";
                            $query .= " WHERE year         = '".$row3["YEAR"]."' ";
                            $query .= "   AND schregno     = '".$row3["SCHREGNO"]."' ";
                            $query .= "   AND expense_m_cd = '".$row3["EXPENSE_M_CD"]."' ";
                            $query .= "   AND expense_s_cd = '".$row3["EXPENSE_S_CD"]."' ";
                            $query .= "   AND paid_input_flg = '2' ";
                        }
                        $db->query($query);
                    }
                }
            }
        }
        return $flg;
    }
}
?>
