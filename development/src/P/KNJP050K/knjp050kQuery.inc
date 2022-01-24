<?php

require_once('for_php7.php');

class knjp050kQuery extends Query {

    //学年取得
    function get_Grade()
    {
        $query  = " SELECT GRADE FROM SCHREG_REGD_HDAT GROUP BY GRADE ";
        return $query;
    }

    //費目グループリスト
    function SelectQuery($model)
    {

        $query  = "  SELECT ";
        $query .= "     T1.grade,T1.expense_grp_cd,T1.expense_grp_name, ";
        $query .= "     T2.hr_nameabbv, ";
        $query .= "     T3.expense_m_cd,T3.expense_m_name, ";
        $query .= "     T4.expense_s_cd,T4.expense_s_name, ";
        $query .= "     T3.sort_mcd,T4.sort_scd ";
        $query .= "  FROM ";
        $query .= "     expense_grp_mst T1 ";
        $query .= "  LEFT OUTER JOIN ";
        $query .= "     (SELECT  ";
        $query .= "         wt1.year,wt1.grade,wt1.hr_class, ";
        $query .= "         wt1.expense_grp_cd,wt2.hr_nameabbv ";
        $query .= "      FROM ";
        $query .= "         expense_grp_hr_dat wt1, ";
        $query .= "         schreg_regd_hdat wt2 ";
        $query .= "      WHERE ";
        $query .= "         wt1.year = wt2.year AND ";
        $query .= "         wt2.semester = '".CTRL_SEMESTER."' AND ";
        $query .= "         wt1.grade = wt2.grade AND ";
        $query .= "         wt1.hr_class = wt2.hr_class AND ";
        $query .= "         wt1.year = '".$model->year."' ";
        $query .= "     ) T2 ";
        $query .= "  ON  ";
        $query .= "     T1.year = T2.year AND ";
        $query .= "     T1.expense_grp_cd = T2.expense_grp_cd ";
        //全学年表示の時は学年指定なし
        if ($model->grade != "99") {
            $query .= " AND T1.grade = T2.grade ";
        }
        $query .= "  LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         wt3.year,wt3.expense_grp_cd, wt4.expense_m_cd as sort_mcd, ";
        $query .= "         wt4.expense_l_cd || wt4.expense_m_cd AS expense_m_cd, ";
        $query .= "         wt4.expense_m_name ";
        $query .= "      FROM ";
        $query .= "         expense_grp_m_dat wt3, ";
        $query .= "         expense_m_mst wt4 ";
        $query .= "      WHERE ";
        $query .= "         wt3.year = wt4.year AND ";
        $query .= "         wt3.expense_m_cd = wt4.expense_m_cd AND ";
        $query .= "         wt3.year = '".$model->year."' ";
        $query .= "     ) T3 ";
        $query .= "  ON ";
        $query .= "     T1.year = T3.year AND ";
        $query .= "     T1.expense_grp_cd = T3.expense_grp_cd ";
        $query .= "  LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         wt5.year,wt5.expense_grp_cd, wt5.expense_s_cd as sort_scd, ";
        $query .= "         wt6.expense_l_cd || wt6.expense_m_cd || wt5.expense_s_cd  AS expense_s_cd, ";
        $query .= "         wt7.expense_s_name ";
        $query .= "      FROM ";
        $query .= "         expense_grp_s_dat wt5, ";
        $query .= "         expense_m_mst wt6, ";
        $query .= "         v_expense_s_mst wt7 ";
        $query .= "      WHERE ";
        $query .= "         wt5.year = wt6.year AND ";
        $query .= "         wt5.year = wt7.year AND ";
        $query .= "         wt5.expense_m_cd = wt6.expense_m_cd AND ";
        $query .= "         wt5.expense_s_cd = wt7.expense_s_cd AND ";
        $query .= "         wt6.expense_m_cd = wt7.expense_m_cd AND ";
        $query .= "         wt6.expense_s_exist_flg = '1' AND ";
        $query .= "         wt5.year = '".$model->year."' ";
        $query .= "      ) T4 ";
        $query .= "  ON ";
        $query .= "     T1.year = T4.year AND ";
        $query .= "     T1.expense_grp_cd = T4.expense_grp_cd ";
        $query .= "  WHERE ";
        $query .= "     T1.year = '".$model->year."'"; 
        
        //全学年表示の時は学年指定なし
        if ($model->grade != "99") {
            $query .= " AND T1.grade = '".$model->grade."'";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.grade, ";
        $query .= "     T1.expense_grp_cd, ";
        $query .= "     T2.hr_nameabbv, ";
        $query .= "     T3.sort_mcd, ";
        $query .= "     T4.sort_scd ";

        return $query;    
    }
        
    //割当クラス一覧(左リスト)
    function GetSelectClass($model)
    {
        $query = "SELECT T2.grade, T2.hr_class, T2.hr_nameabbv
                    FROM expense_grp_hr_dat T1,
                         schreg_regd_hdat T2
                   WHERE T1.year        = T2.year
                     AND T1.grade       = T2.grade
                     AND T1.hr_class    = T2.hr_class
                     AND T2.year        = '".$model->year."'
                     AND T2.semester    = '".CTRL_SEMESTER."'
                     AND T2.grade       = '".$model->grade2."'
                     AND T1.expense_grp_cd = '".$model->exp_grpcd."'";
        return $query;
    }

    //割当クラス一覧(左リスト:リロード時)
    function ReloadSelectClass($model)
    {
        $query = "SELECT grade, hr_class, hr_nameabbv
                    FROM schreg_regd_hdat
                   WHERE year        = '".$model->year."'
                     AND semester    = '".CTRL_SEMESTER."'
                     AND grade       = '".$model->grade2."'
                     AND hr_class IN ('".str_replace(",","','",$model->field["CLASS"])."') ";
        return $query;
    }

        
    //クラス一覧(右リスト)
    function GetClass($model,$classcd)
    {
        $query = "SELECT grade, hr_class, hr_nameabbv
                    FROM schreg_regd_hdat
                   WHERE year     = '".$model->year."'
                     AND semester = '".CTRL_SEMESTER."'
                     AND grade = '".$model->grade2."'
                     AND hr_class NOT IN ('".implode("','",$classcd)."')
                     AND hr_class NOT IN (SELECT hr_class
                                            FROM expense_grp_hr_dat
                                           WHERE year = '".$model->year."'
                                             AND grade = '".$model->grade2."'
                                             AND expense_grp_cd <> '".$model->exp_grpcd."'
                                         ) ";
        return $query;
    }

    //費目中分類割当一覧(左リスト)
    function GetSelectMcd($model)
    {
        $query = "SELECT
                      T2.expense_l_cd,
                      T2.expense_m_cd,
                      T2.expense_m_name
                  FROM
                      expense_grp_m_dat T1,
                      expense_m_mst T2
                  WHERE
                      T1.year = T2.year AND
                      T1.expense_m_cd = T2.expense_m_cd AND
                      T1.year = '".$model->year."' AND
                      T1.expense_grp_cd = '".$model->exp_grpcd."'
                  ORDER BY
                      T2.expense_m_cd ";
        return $query;
    }

    //費目中分類割当一覧(左リスト:リロード時)
    function ReloadSelectMcd($model)
    {
        $query = "SELECT
                      expense_l_cd,
                      expense_m_cd,
                      expense_m_name
                  FROM
                      expense_m_mst
                  WHERE
                      year = '".$model->year."' AND
                      expense_m_cd IN ('".str_replace(",","','",$model->field["EXPENSE_M_CD"])."')
                  ORDER BY
                      expense_m_cd ";
        return $query;
    }   

    //費目中分類一覧(右リスト)
    function GetMcd($model,$exp_mcd)
    {
        $query = "SELECT
                      expense_l_cd,
                      expense_m_cd,
                      expense_m_name
                  FROM
                      expense_m_mst
                  WHERE
                      year = '".$model->year."' AND
                      expense_m_cd NOT IN ('".implode("','",$exp_mcd)."')
                  ORDER BY
                      expense_m_cd ";
        return $query;
    }

    //費目中分類金額合計
    function GetMcdMoney($model,$exp_mcd)
    {
        $query = "SELECT
                      T1.expense_m_cd,
                      T1.expense_s_exist_flg,
                      coalesce(T1.expense_m_money,0) as expense_m_money,
                      T2.sex,
                      coalesce(T2.expense_s_money,0) as expense_s_money
                  FROM
                      expense_m_mst T1
                  LEFT OUTER JOIN
                      v_expense_s_mst T2
                  ON
                      T1.year = T2.year AND
                      T1.expense_m_cd = T2.expense_m_cd
                  WHERE
                      T1.year = '".$model->year."' AND
                      T1.expense_m_cd IN ('".implode("','",$exp_mcd)."')
                  ORDER BY
                      T1.expense_m_cd ";
        return $query;
    }


    //費目小分類割当一覧(左リスト)
    function GetSelectScd($model,$exp_mcd)
    {
        $query = "SELECT
                      T3.expense_l_cd,T3.expense_m_cd,
                      T2.expense_s_cd,T2.expense_s_name
                  FROM
                      expense_grp_s_dat T1
                  LEFT OUTER JOIN 
                      v_expense_s_mst T2
                  ON
                      T1.year = T2.year AND
                      T1.expense_m_cd = T2.expense_m_cd AND
                      T1.expense_s_cd = T2.expense_s_cd AND
                      T2.expense_m_cd IN ('".implode("','",$exp_mcd)."')
                  LEFT OUTER JOIN
                      expense_m_mst T3
                  ON
                      T2.year = T3.year AND
                      T2.expense_m_cd = T3.expense_m_cd
                  WHERE
                      T1.year = '".$model->year."' AND
                      T1.expense_grp_cd = '".$model->exp_grpcd."' AND
                      T3.expense_s_exist_flg = '1' ";
        return $query;
    }

    //費目小分類割当一覧(左リスト:リロード時)
    function ReloadSelectScd($model,$exp_mcd)
    {
        $scd_array = array();
        $scd_array = preg_split("/,/",$model->field["EXPENSE_S_CD"]);
        $scd_data = "";
        $sep = "";
        for ($i = 0; $i < get_count($scd_array); $i++) {
            $scd_data .= $sep.substr($scd_array[$i],0,2);
            $sep = ",";
        }

        $query = "SELECT
                      T2.expense_l_cd,T2.expense_m_cd,
                      T1.expense_s_cd,T1.expense_s_name
                  FROM
                      v_expense_s_mst T1,
                      expense_m_mst T2
                  WHERE
                      T1.year = T2.year AND
                      T1.expense_m_cd = T2.expense_m_cd AND
                      T1.year = '".$model->year."' AND
                      T2.expense_m_cd IN ('".implode("','",$exp_mcd)."') AND
                      T2.expense_s_exist_flg = '1' AND
                      T1.expense_s_cd IN ('".str_replace(",","','",$scd_data)."')
                 ";
        return $query;
    }

    //費目小分類一覧(右リスト)
    function GetScd($model,$exp_mcd,$exp_scd)
    {
        $query = "SELECT
                      T2.expense_l_cd,T2.expense_m_cd,
                      T1.expense_s_cd,T1.expense_s_name
                  FROM
                      v_expense_s_mst T1,
                      expense_m_mst T2
                  WHERE
                      T1.year = T2.year AND
                      T1.expense_m_cd = T2.expense_m_cd AND
                      T1.year = '".$model->year."' AND
                      T2.expense_m_cd IN ('".implode("','",$exp_mcd)."') AND
                      T2.expense_s_exist_flg = '1' AND
                      T1.expense_s_cd NOT IN ('".implode("','",$exp_scd)."')
                  ORDER BY
                      T1.expense_s_cd ";
        return $query;
    }

    //費目小分類金額合計
    function GetScdMoney($model,$exp_mcd,$exp_scd)
    {
        $query = "SELECT
                      T1.expense_s_cd,T1.sex,coalesce(T1.expense_s_money,0) as expense_s_money
                  FROM
                      v_expense_s_mst T1,
                      expense_m_mst T2
                  WHERE
                      T1.year = T2.year AND
                      T1.expense_m_cd = T2.expense_m_cd AND
                      T1.year = '".$model->year."' AND
                      T2.expense_m_cd IN ('".implode("','",$exp_mcd)."') AND
                      T2.expense_s_exist_flg = '1' AND
                      T1.expense_s_cd IN ('".implode("','",$exp_scd)."')
                  ORDER BY
                      T1.expense_s_cd ";
        return $query;
    }


    //１レコード取得
    function getRow($model,$flg)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM expense_grp_mst ";
        $query .= " WHERE year = '".$model->year."'";
        if ($flg == 1) {
            $query .= "   AND expense_grp_cd = '".$model->exp_grpcd."'";
        } else {
            $query .= "   AND expense_grp_cd = '".sprintf("%04d",$model->field["EXPENSE_GRP_CD"])."'";
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //HRクラスの重複チェック
    function RepetCheck($model)
    {
        $db = Query::dbCheckOut();
        
        $query = " SELECT * 
                     FROM expense_grp_hr_dat
                    WHERE year = '".$model->year."'
                      AND grade = '".$model->grade2."'
                      AND hr_class IN ('".str_replace(",","','",$model->field["CLASS"])."')
                      AND expense_grp_cd <> '".$model->field["EXPENSE_GRP_CD"]."'
                 ";
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if (is_array($Row)) {
            return false;
        } else {
            return true;
        }
    }

/*    //入金済みデータの存在チェック
    function CheckMoney($model)
    {
        $db = Query::dbCheckOut();

        //入金済み中分類データ
        $query = "SELECT * 
                    FROM money_paid_m_dat
                   WHERE year = '".$model->year."'
                     AND expense_m_cd IN ('".str_replace(",","','",$model->field["EXPENSE_M_CD"])."')
                     AND schregno IN (SELECT schregno
                                        FROM money_due_m_dat
                                       WHERE year = '".$model->year."'
                                         AND expense_m_cd IN ('".str_replace(",","','",$model->field["EXPENSE_M_CD"])."')
                                     )
                  ";
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($Row)) return false;
        
        //入金済み小分類データ
        $query = "SELECT * 
                    FROM money_paid_s_dat
                   WHERE year = '".$model->year."'
                     AND expense_s_cd IN ('".str_replace(",","','",$model->field["EXPENSE_S_CD"])."')
                     AND schregno IN (SELECT schregno
                                        FROM money_due_s_dat
                                       WHERE year = '".$model->year."'
                                         AND expense_s_cd IN ('".str_replace(",","','",$model->field["EXPENSE_S_CD"])."')
                                     )

                  ";
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
    
        if (is_array($Row)) {
            return false;
        } else {
            return true;
        }
    }
*/

    //更新権限があるかチェック
    function getAuthority()
    {
        $query  = " SELECT var1 ";
        $query .= "   FROM school_expenses_sys_ini ";
        $query .= "  WHERE programid = 'KNJP050K'  AND ";
        $query .= "        div = '0001' ";
        
        return $query;
    }
    
    //入金済みデータの存在チェック
    function getPaidMoney($year, $table)
    {
        $query = "SELECT COUNT(*) FROM $table WHERE year='$year'";
        return $query;
    }        

    //減免等により、生徒毎に内容が変更された入金予定中分類データ
    function M_MoneyChange($year)
    {
        $query  = " SELECT COUNT(T2.schregno) ";
        $query .= "   FROM expense_m_mst T1, ";
        $query .= "        money_due_m_dat T2 ";
        $query .= "  WHERE ";
        $query .= "        T1.year = T2.year AND ";
        $query .= "        T1.expense_m_cd = T2.expense_m_cd AND ";
        $query .= "        T1.expense_m_money <> T2.money_due AND ";
        $query .= "        T1.year = '".$year."'";
        
        return $query;
    }

    //減免等により、生徒毎に内容が変更された入金予定小分類データ
    function S_MoneyChange($year)
    {
        $query  = " SELECT COUNT(T2.schregno) ";
        $query .= "   FROM v_expense_s_mst T1, ";
        $query .= "        money_due_s_dat T2 ";
        $query .= "  WHERE ";
        $query .= "        T1.year = T2.year AND ";
        $query .= "        T1.expense_m_cd = T2.expense_m_cd AND ";
        $query .= "        T1.expense_s_cd = T2.expense_s_cd AND ";
        $query .= "        T1.expense_s_money <> T2.money_due AND ";
        $query .= "        T1.year = '".$year."'";
        
        return $query;
    }

    //対象年度に設定されている費目グループコードの取得
    function get_Grpcd($year) 
    {
        $query = " SELECT expense_grp_cd FROM expense_grp_mst WHERE year = '".$year."' ";
        return $query;
    }

    //費目グループ中分類データの内容と入金予定中分類データの内容が一致しているかチェック
    function Check_Grp_MData($year,$grp_cd,$join)
    {
        $query .= " SELECT ";
        $query .= "     wt1.schregno, ";
        $query .= "     wt1.expense_m_cd as grp_m_cd, ";
        $query .= "     wt2.expense_m_cd as due_m_cd ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         T2.schregno,T3.expense_m_cd ";
        $query .= "      FROM ";
        $query .= "         expense_grp_hr_dat T1, ";
        $query .= "         schreg_regd_dat T2, ";
        $query .= "         expense_grp_m_dat T3 ";
        $query .= "      WHERE ";
        $query .= "         T1.year = T2.year AND ";
        $query .= "         T1.grade = T2.grade AND ";
        $query .= "         T1.hr_class = T2.hr_class AND ";
        $query .= "         T2.semester = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.year = T3.year AND ";
        $query .= "         T1.expense_grp_cd = T3.expense_grp_cd AND ";
        $query .= "         T1.year = '".$year."' AND ";
        $query .= "         T1.expense_grp_cd = '".$grp_cd."' ";
        $query .= "     ) wt1 ";
        $query .=   $join;
        $query .= "     (SELECT ";
        $query .= "         T3.schregno, T3.expense_m_cd ";
        $query .= "      FROM ";
        $query .= "         expense_grp_hr_dat T1, ";
        $query .= "         schreg_regd_dat T2, ";
        $query .= "         money_due_m_dat T3 ";
        $query .= "      WHERE ";
        $query .= "         T1.year = T2.year AND ";
        $query .= "         T1.grade = T2.grade AND ";
        $query .= "         T1.hr_class = T2.hr_class AND ";
        $query .= "         T2.semester = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.year = T3.year AND ";
        $query .= "         T2.schregno = T3.schregno AND ";
        $query .= "         T1.year = '".$year."' AND ";
        $query .= "         T1.expense_grp_cd = '".$grp_cd."' ";
        $query .= "     ) wt2 ";
        $query .= " ON ";
        $query .= " wt1.schregno = wt2.schregno AND ";
        $query .= " wt1.expense_m_cd = wt2.expense_m_cd ";

        return $query;
    }

    //費目グループ小分類データにあって入金予定小分類データにないデータをチェック
    function Check_Grp_SData($year,$grp_cd)
    {
        $query .= " SELECT ";
        $query .= "     wt1.schregno, ";
        $query .= "     wt1.expense_s_cd as grp_s_cd, ";
        $query .= "     wt2.expense_s_cd as due_s_cd, ";
        $query .= "     wt1.scd_sex, ";
        $query .= "     wt1.base_sex ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         T2.schregno,T3.expense_m_cd,T3.expense_s_cd,T4.sex as scd_sex,T5.sex as base_sex ";
        $query .= "      FROM ";
        $query .= "         expense_grp_hr_dat T1, ";
        $query .= "         schreg_regd_dat T2, ";
        $query .= "         expense_grp_s_dat T3, ";
        $query .= "         v_expense_s_mst T4, ";
        $query .= "         schreg_base_mst T5 ";
        $query .= "      WHERE ";
        $query .= "         T1.year = T2.year AND ";
        $query .= "         T1.grade = T2.grade AND ";
        $query .= "         T1.hr_class = T2.hr_class AND ";
        $query .= "         T2.semester = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.year = T3.year AND ";
        $query .= "         T1.expense_grp_cd = T3.expense_grp_cd AND ";
        $query .= "         T1.year = T4.year AND ";
        $query .= "         T3.expense_m_cd = T4.expense_m_cd AND ";
        $query .= "         T3.expense_s_cd = T4.expense_s_cd AND ";
        $query .= "         T2.schregno = T5.schregno AND ";
        $query .= "         T1.year = '".$year."' AND ";
        $query .= "         T1.expense_grp_cd = '".$grp_cd."' ";
        $query .= "     ) wt1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         T3.schregno, T3.expense_m_cd, T3.expense_s_cd ";
        $query .= "      FROM ";
        $query .= "         expense_grp_hr_dat T1, ";
        $query .= "         schreg_regd_dat T2, ";
        $query .= "         money_due_s_dat T3 ";
        $query .= "      WHERE ";
        $query .= "         T1.year = T2.year AND ";
        $query .= "         T1.grade = T2.grade AND ";
        $query .= "         T1.hr_class = T2.hr_class AND ";
        $query .= "         T2.semester = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.year = T3.year AND ";
        $query .= "         T2.schregno = T3.schregno AND ";
        $query .= "         T1.year = '".$year."' AND ";
        $query .= "         T1.expense_grp_cd = '".$grp_cd."' ";
        $query .= "     ) wt2 ";
        $query .= " ON ";
        $query .= " wt1.schregno = wt2.schregno AND ";
        $query .= " wt1.expense_m_cd = wt2.expense_m_cd AND ";
        $query .= " wt1.expense_s_cd = wt2.expense_s_cd ";

        return $query;
    }

    //入金予定小分類データにあって費目グループ小分類データにないデータをチェック
    function Check_Grp_SData2($year,$grp_cd)
    {
        $query .= " SELECT ";
        $query .= "     wt1.schregno, ";
        $query .= "     wt1.expense_s_cd as due_s_cd, ";
        $query .= "     wt2.expense_s_cd as grp_s_cd, ";
        $query .= "     wt1.scd_sex, ";
        $query .= "     wt1.base_sex ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         T3.schregno, T3.expense_m_cd, T3.expense_s_cd,T4.sex as scd_sex,T5.sex as base_sex ";
        $query .= "      FROM ";
        $query .= "         expense_grp_hr_dat T1, ";
        $query .= "         schreg_regd_dat T2, ";
        $query .= "         money_due_s_dat T3, ";
        $query .= "         v_expense_s_mst T4, ";
        $query .= "         schreg_base_mst T5 ";
        $query .= "      WHERE ";
        $query .= "         T1.year = T2.year AND ";
        $query .= "         T1.grade = T2.grade AND ";
        $query .= "         T1.hr_class = T2.hr_class AND ";
        $query .= "         T2.semester = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.year = T3.year AND ";
        $query .= "         T2.schregno = T3.schregno AND ";
        $query .= "         T1.year = T4.year AND ";
        $query .= "         T3.expense_m_cd = T4.expense_m_cd AND ";
        $query .= "         T3.expense_s_cd = T4.expense_s_cd AND ";
        $query .= "         T2.schregno = T5.schregno AND ";
        $query .= "         T1.year = '".$year."' AND ";
        $query .= "         T1.expense_grp_cd = '".$grp_cd."' ";
        $query .= "     ) wt1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         T2.schregno,T3.expense_m_cd,T3.expense_s_cd ";
        $query .= "      FROM ";
        $query .= "         expense_grp_hr_dat T1, ";
        $query .= "         schreg_regd_dat T2, ";
        $query .= "         expense_grp_s_dat T3 ";
        $query .= "      WHERE ";
        $query .= "         T1.year = T2.year AND ";
        $query .= "         T1.grade = T2.grade AND ";
        $query .= "         T1.hr_class = T2.hr_class AND ";
        $query .= "         T2.semester = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.year = T3.year AND ";
        $query .= "         T1.expense_grp_cd = T3.expense_grp_cd AND ";
        $query .= "         T1.year = '".$year."' AND ";
        $query .= "         T1.expense_grp_cd = '".$grp_cd."' ";
        $query .= "     ) wt2 ";
        $query .= " ON ";
        $query .= " wt1.schregno = wt2.schregno AND ";
        $query .= " wt1.expense_m_cd = wt2.expense_m_cd AND ";
        $query .= " wt1.expense_s_cd = wt2.expense_s_cd ";
# echo $query."<br>";
        return $query;
    }


    //分納が設定されている入金予定データ
    function getInstChage($year)
    {
        $query  = " SELECT COUNT(*) ";
        $query .= "   FROM money_due_m_dat ";
        $query .= "  WHERE year = '".$year."' AND ";
        $query .= "        inst_cd IS NOT NULL ";
        
        return $query;
    }

    //前年度からコピー時のデータ存在チェック
    function selectYearQuery($year, $table)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT COUNT(*) FROM $table WHERE year='$year'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    //コピー時のマスタチェック(前年度に登録されているクラスが今年度に存在するか)
    function mstClassCheck($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.grade || T1.hr_class as hr_class, ";
        $query .= "     T2.grade || T2.hr_class as mst_hr_class ";
        $query .= " FROM ";
        $query .= "     (SELECT grade,hr_class ";
        $query .= "        FROM expense_grp_hr_dat ";
        $query .= "       WHERE year = '".($model->year - 1)."') T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT grade,hr_class ";
        $query .= "        FROM schreg_regd_hdat ";
        $query .= "       WHERE year = '".$model->year."' AND ";
        $query .= "             semester = '".CTRL_SEMESTER."' ) T2 ";
        $query .= " ON ";
        $query .= "     T1.grade = T2.grade AND ";
        $query .= "     T1.hr_class = T2.hr_class ";

        return $query;
    }

    //コピー時のマスタチェック(前年度に登録されている中分類が今年度に存在するか)
    function mstMcdCheck($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.expense_m_cd, ";
        $query .= "     T2.expense_m_cd as mst_exp_mcd ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT expense_m_cd ";
        $query .= "        FROM expense_grp_m_dat ";
        $query .= "       WHERE year = '".($model->year - 1)."') T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT expense_m_cd ";
        $query .= "        FROM expense_m_mst ";
        $query .= "       WHERE year = '".$model->year."') T2 ";
        $query .= " ON ";
        $query .= "         T1.expense_m_cd = T2.expense_m_cd ";

        return $query;
    }

    //コピー時のマスタチェック(前年度に登録されている小分類が今年度に存在するか)
    function mstScdCheck($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.expense_s_cd, ";
        $query .= "     T2.expense_s_cd as mst_exp_scd ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT expense_m_cd,expense_s_cd ";
        $query .= "        FROM expense_grp_s_dat ";
        $query .= "       WHERE year = '".($model->year - 1)."') T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT expense_m_cd,expense_s_cd ";
        $query .= "        FROM v_expense_s_mst ";
        $query .= "       WHERE year = '".$model->year."') T2 ";
        $query .= " ON ";
        $query .= "         T1.expense_m_cd = T2.expense_m_cd AND ";
        $query .= "         T1.expense_s_cd = T2.expense_s_cd ";

        return $query;
    }


    //追加・更新
    function &getUpdateQuery($model,&$db)
    {
    
        //費目グループマスタ
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["EXPENSE_GRP_CD"][TEXT]   = sprintf("%04d",$model->field["EXPENSE_GRP_CD"]);
        $data["EXPENSE_GRP_NAME"][TEXT] = $model->field["EXPENSE_GRP_NAME"];
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "EXPENSE_GRP_MST");
        $db->query($query);


        //費目グループHRクラスデータ
        if ($model->field["CLASS"])
        {
            $class = explode(",",$model->field["CLASS"]);
            foreach ($class as $val)
            {
                $data = array();
                $data["YEAR"][TEXT]             = $model->year;
                $data["GRADE"][TEXT]            = $model->field["GRADE"];
                $data["HR_CLASS"][TEXT]         = $val;
                $data["EXPENSE_GRP_CD"][TEXT]   = sprintf("%04d",$model->field["EXPENSE_GRP_CD"]);
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "EXPENSE_GRP_HR_DAT");
                $db->query($query);
            }
        }


        //費目中分類
        if ($model->field["EXPENSE_M_CD"])
        {
            $expmcd = explode(",",$model->field["EXPENSE_M_CD"]);
            foreach ($expmcd as $val)
            {
                //費目グループ費目中分類データ
                $data = array();
                $data["YEAR"][TEXT]             = $model->year;
                $data["EXPENSE_GRP_CD"][TEXT]   = sprintf("%04d",$model->field["EXPENSE_GRP_CD"]);
                $data["EXPENSE_M_CD"][TEXT]     = $val;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "EXPENSE_GRP_M_DAT");
                $db->query($query);

                //各クラス毎の生徒の学籍番号を取得
                $query = " SELECT
                              T2.schregno
                          FROM
                              expense_grp_hr_dat T1,
                              schreg_regd_dat T2
                          WHERE
                              T1.year = T2.year AND
                              T1.grade = T2.grade AND
                              T1.hr_class = T2.hr_class AND
                              T2.semester = '".CTRL_SEMESTER."' AND
                              T1.year = '".$model->year."' AND
                              T1.grade = '".$model->field["GRADE"]."' AND
                              T1.expense_grp_cd = '".sprintf("%04d",$model->field["EXPENSE_GRP_CD"])."' ";

                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    //入金予定中分類データ
                    $query  = "";
                    $query  = " INSERT INTO money_due_m_dat ";
                    $query .= "     ( YEAR, ";
                    $query .= "       SCHREGNO, ";
                    $query .= "       EXPENSE_M_CD, ";
                    $query .= "       MONEY_DUE, ";
                    $query .= "       registercd, ";
                    $query .= "       updated ) ";
                    $query .= "   VALUES ( '".$model->year."', ";
                    $query .= "            '".$row["SCHREGNO"]."', ";
                    $query .= "            '".$val."', ";
                    $query .= "            (SELECT expense_m_money ";
                    $query .= "               FROM expense_m_mst ";
                    $query .= "              WHERE year = '".$model->year."' ";
                    $query .= "                AND expense_m_cd = '".$val."' ), ";
                    $query .= "            '".STAFFCD."', ";
                    $query .= "            sysdate() ";
                    $query .= "           ) ";

                    $db->query($query);
                }
            }
        }


        //費目小分類
        if ($model->field["EXPENSE_S_CD"])
        {
            $expscd = explode(",",$model->field["EXPENSE_S_CD"]);
            foreach ($expscd as $val)
            {
                //費目グループ費目小分類データ
                $data = array();
                $data["YEAR"][TEXT]             = $model->year;
                $data["EXPENSE_GRP_CD"][TEXT]   = sprintf("%04d",$model->field["EXPENSE_GRP_CD"]);
                $data["EXPENSE_M_CD"][TEXT]     = substr($val,2,2);
                $data["EXPENSE_S_CD"][TEXT]     = substr($val,0,2);
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "EXPENSE_GRP_S_DAT");
                $db->query($query);

                //各クラス毎の生徒の学籍番号と性別,小分類の金額と対象性別を取得
                $query = " SELECT
                                T4.schregno,T5.sex,
                                T3.expense_s_money,T3.sex as scd_sex
                            FROM
                                expense_grp_hr_dat T1,
                                expense_grp_s_dat T2,
                                v_expense_s_mst T3,
                                schreg_regd_dat T4,
                                schreg_base_mst T5
                            WHERE
                                T1.year = T2.year AND
                                T1.expense_grp_cd = T2.expense_grp_cd AND
                                T1.year = T3.year AND
                                T2.expense_m_cd = T3.expense_m_cd AND
                                T2.expense_s_cd = T3.expense_s_cd AND
                                T1.year = T4.year AND
                                T1.grade = T4.grade AND
                                T1.hr_class = T4.hr_class AND
                                T4.schregno = T5.schregno AND
                                T4.semester = '".CTRL_SEMESTER."' AND
                                T1.year = '".$model->year."' AND
                                T1.grade = '".$model->field["GRADE"]."' AND
                                T1.expense_grp_cd = '".sprintf("%04d",$model->field["EXPENSE_GRP_CD"])."' AND
                                T3.expense_s_cd = '".substr($val,0,2)."' ";

                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    //小分類の対象性別がNULLまたは、生徒の性別と小分類の対象性別が一緒ならデータ作成
                    if ($row["SCD_SEX"] == "" || $row["SEX"] == $row["SCD_SEX"]) {

                        //入金予定小分類データ
                        $data = array();
                        $data["YEAR"][TEXT]             = $model->year;
                        $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                        $data["EXPENSE_M_CD"][TEXT]     = substr($val,2,2);
                        $data["EXPENSE_S_CD"][TEXT]     = substr($val,0,2);
                        $data["MONEY_DUE"][NUMBER]      = $row["EXPENSE_S_MONEY"];
                        $data["REGISTERCD"][TEXT]       = STAFFCD;
                        $data["UPDATED"][FUNC]          = "sysdate()";

                        $query = Query::insertSQL($data, "MONEY_DUE_S_DAT");
                        $db->query($query);
                    }
                }
            }
        }
        return;
    }

    
    //削除
    function &getDeleteQuery($model,&$db)
    {

        //費目グループマスタ
        $query  = " DELETE FROM expense_grp_mst ";
        $query .= "  WHERE year = '".$model->year."'";
        $query .= "    AND expense_grp_cd  = '".sprintf("%04d",$model->field["EXPENSE_GRP_CD"])."'";
        $db->query($query);


        //費目グループ費目中分類データ
        $query  = " DELETE FROM expense_grp_m_dat ";
        $query .= "  WHERE year = '".$model->year."'";
        $query .= "    AND expense_grp_cd  = '".sprintf("%04d",$model->field["EXPENSE_GRP_CD"])."'";
        $db->query($query);


        //費目グループ費目小分類データ
        $query  = " DELETE FROM expense_grp_s_dat ";
        $query .= "  WHERE year = '".$model->year."'";
        $query .= "    AND expense_grp_cd  = '".sprintf("%04d",$model->field["EXPENSE_GRP_CD"])."'";
        $db->query($query);


        //入金予定中分類データ
        $query  = " DELETE FROM money_due_m_dat ";
        $query .= "  WHERE year = '".$model->year."'";
        $query .= "    AND schregno IN (SELECT T2.schregno ";
        $query .= "                       FROM expense_grp_hr_dat T1, ";
        $query .= "                            schreg_regd_dat T2 ";
        $query .= "                      WHERE T1.year = T2.year ";
        $query .= "                        AND T1.grade = T2.grade ";
        $query .= "                        AND T1.hr_class = T2.hr_class ";
        $query .= "                        AND T2.semester = '".CTRL_SEMESTER."'";
        $query .= "                        AND T1.year = '".$model->year."'";
#        $query .= "                        AND T1.grade = '".$model->field["GRADE"]."'";
        $query .= "                        AND T1.expense_grp_cd = '".sprintf("%04d",$model->field["EXPENSE_GRP_CD"])."'";
        $query .= "                    )";
        $db->query($query);

        //入金予定小分類データ
        $query  = " DELETE FROM money_due_s_dat ";
        $query .= "  WHERE year = '".$model->year."'";
        $query .= "    AND schregno IN (SELECT T2.schregno ";
        $query .= "                       FROM expense_grp_hr_dat T1, ";
        $query .= "                            schreg_regd_dat T2 ";
        $query .= "                      WHERE T1.year = T2.year ";
        $query .= "                        AND T1.grade = T2.grade ";
        $query .= "                        AND T1.hr_class = T2.hr_class ";
        $query .= "                        AND T2.semester = '".CTRL_SEMESTER."'";
        $query .= "                        AND T1.year = '".$model->year."'";
#        $query .= "                        AND T1.grade = '".$model->field["GRADE"]."'";
        $query .= "                        AND T1.expense_grp_cd = '".sprintf("%04d",$model->field["EXPENSE_GRP_CD"])."'";
        $query .= "                    )";
        $db->query($query);


        //費目グループHRクラスデータ
        $query  = " DELETE FROM expense_grp_hr_dat ";
        $query .= "  WHERE year = '".$model->year."'";
#        $query .= "    AND grade = '".$model->field["GRADE"]."'";
        $query .= "    AND expense_grp_cd  = '".sprintf("%04d",$model->field["EXPENSE_GRP_CD"])."'";
        $db->query($query);

        return;
    }


    //前年度からデータをコピー
    function &getCopyYearQuery($year)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //費目グループマスタ
        $query  = " DELETE FROM expense_grp_mst ";
        $query .= "  WHERE year = '".$year."'";
        $db->query($query);

        $query  = "INSERT INTO expense_grp_mst(";
        $query .= " year,";
        $query .= " expense_grp_cd,";
        $query .= " expense_grp_name,";
        $query .= " grade,";
        $query .= " registercd,";
        $query .= " updated)";
        $query .= " SELECT";
        $query .= " '$year',";
        $query .= " expense_grp_cd,";
        $query .= " expense_grp_name,";
        $query .= " grade,";
        $query .= " '" .STAFFCD. "', sysdate()";
        $query .= " FROM expense_grp_mst";
        $query .= " WHERE ";
        $query .= "     year='" . ($year - 1) . "' ";
        $db->query($query);


        //費目グループHRクラスデータ
        $query  = " DELETE FROM expense_grp_hr_dat ";
        $query .= "  WHERE year = '".$year."'";
        $db->query($query);

        $query  = "INSERT INTO expense_grp_hr_dat(";
        $query .= " year,";
        $query .= " grade,";
        $query .= " hr_class,";
        $query .= " expense_grp_cd,";
        $query .= " registercd,";
        $query .= " updated)";
        $query .= " SELECT";
        $query .= "     '$year',";
        $query .= "     T1.grade,";
        $query .= "     T1.hr_class,";
        $query .= "     T1.expense_grp_cd,";
        $query .= "     '" .STAFFCD. "', sysdate()";
        $query .= " FROM";
        $query .= "     (SELECT grade,hr_class,expense_grp_cd";
        $query .= "        FROM expense_grp_hr_dat ";
        $query .= "       WHERE year = '".($year - 1)."'";
        $query .= "     ) T1,";
        $query .= "     (SELECT grade,hr_class";
        $query .= "        FROM schreg_regd_hdat";
        $query .= "       WHERE year = '".$year."'";
        $query .= "         AND semester = '".CTRL_SEMESTER."'";
        $query .= "     ) T2";
        $query .= " WHERE ";
        $query .= "     T1.grade = T2.grade AND ";
        $query .= "     T1.hr_class = T2.hr_class ";
        $db->query($query);


        //費目グループ費目中分類データ
        $query  = " DELETE FROM expense_grp_m_dat ";
        $query .= "  WHERE year = '".$year."'";
        $db->query($query);

        $query  = "INSERT INTO expense_grp_m_dat(";
        $query .= " year,";
        $query .= " expense_grp_cd,";
        $query .= " expense_m_cd,";
        $query .= " registercd,";
        $query .= " updated)";
        $query .= " SELECT";
        $query .= "     '$year',";
        $query .= "     T1.expense_grp_cd,";
        $query .= "     T1.expense_m_cd,";
        $query .= "     '" .STAFFCD. "', sysdate()";
        $query .= " FROM";
        $query .= "     (SELECT expense_grp_cd,expense_m_cd";
        $query .= "        FROM expense_grp_m_dat ";
        $query .= "       WHERE year = '".($year - 1)."'";
        $query .= "     ) T1,";
        $query .= "     (SELECT expense_m_cd";
        $query .= "        FROM expense_m_mst ";
        $query .= "       WHERE year = '".$year."'";
        $query .= "     ) T2";        
        $query .= " WHERE ";
        $query .= "     T1.expense_m_cd = T2.expense_m_cd ";
        $db->query($query);


        //費目グループ費目小分類データ
        $query  = " DELETE FROM expense_grp_s_dat ";
        $query .= "  WHERE year = '".$year."'";
        $db->query($query);

        $query  = "INSERT INTO expense_grp_s_dat(";
        $query .= " year,";
        $query .= " expense_grp_cd,";
        $query .= " expense_m_cd,";
        $query .= " expense_s_cd,";
        $query .= " registercd,";
        $query .= " updated)";
        $query .= " SELECT";
        $query .= "     '$year',";
        $query .= "     T1.expense_grp_cd,";
        $query .= "     T1.expense_m_cd,";
        $query .= "     T1.expense_s_cd,";
        $query .= "     '" .STAFFCD. "', sysdate()";
        $query .= " FROM";
        $query .= "     (SELECT expense_grp_cd,expense_m_cd,expense_s_cd";
        $query .= "        FROM expense_grp_s_dat ";
        $query .= "       WHERE year = '".($year - 1)."'";
        $query .= "     ) T1,";
        $query .= "     (SELECT expense_m_cd,expense_s_cd";
        $query .= "        FROM v_expense_s_mst ";
        $query .= "       WHERE year = '".$year."'";
        $query .= "     ) T2";        
        $query .= " WHERE ";
        $query .= "     T1.expense_m_cd = T2.expense_m_cd ";
        $query .= "     AND T1.expense_s_cd = T2.expense_s_cd ";
        $db->query($query);


        //入金予定中分類データ
        $query  = " DELETE FROM money_due_m_dat ";
        $query .= "  WHERE year = '".$year."'";
        $db->query($query);

        //各クラス毎の生徒の学籍番号と中分類のデータを取得
        $query = " SELECT
                       T2.schregno,
                       T3.expense_m_cd,
                       T4.expense_m_money
                   FROM
                       expense_grp_hr_dat T1,
                       schreg_regd_dat T2,
                       expense_grp_m_dat T3,
                       expense_m_mst T4
                   WHERE
                       T1.year = T2.year AND
                       T1.grade = T2.grade AND
                       T1.hr_class = T2.hr_class AND
                       T2.semester = '".CTRL_SEMESTER."' AND
                       T1.year = T3.year AND
                       T1.expense_grp_cd = T3.expense_grp_cd AND
                       T1.year = T4.year AND
                       T3.expense_m_cd = T4.expense_m_cd AND
                       T1.year = '".$year."' ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $data = array();
            $data["YEAR"][TEXT]             = $year;
            $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
            $data["EXPENSE_M_CD"][TEXT]     = $row["EXPENSE_M_CD"];
            $data["MONEY_DUE"][NUMBER]      = $row["EXPENSE_M_MONEY"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "MONEY_DUE_M_DAT");
            $db->query($query);
        }


        //入金予定小分類データ
        $query  = " DELETE FROM money_due_s_dat ";
        $query .= "  WHERE year = '".$year."'";
        $db->query($query);

        //各クラス毎の生徒の学籍番号と性別,小分類の金額と対象性別を取得
        $query = " SELECT
                       T2.schregno,
                       T3.sex,
                       T4.expense_m_cd,
                       T4.expense_s_cd,
                       T5.expense_s_money,
                       T5.sex as scd_sex
                   FROM
                       expense_grp_hr_dat T1,
                       schreg_regd_dat T2,
                       schreg_base_mst T3,
                       expense_grp_s_dat T4,
                       v_expense_s_mst T5
                   WHERE
                       T1.year = T2.year AND
                       T1.grade = T2.grade AND
                       T1.hr_class = T2.hr_class AND
                       T2.semester = '".CTRL_SEMESTER."' AND
                       T2.schregno = T3.schregno AND
                       T1.year = T4.year AND
                       T1.expense_grp_cd = T4.expense_grp_cd AND
                       T1.year = T5.year AND
                       T4.expense_m_cd = T5.expense_m_cd AND
                       T4.expense_s_cd = T5.expense_s_cd AND
                       T1.year = '".$year."' ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //小分類の対象性別がNULLまたは、生徒の性別と小分類の対象性別が一緒ならデータ作成
            if ($row["SCD_SEX"] == "" || $row["SEX"] == $row["SCD_SEX"]) {

                $data = array();
                $data["YEAR"][TEXT]             = $year;
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["EXPENSE_M_CD"][TEXT]     = $row["EXPENSE_M_CD"];
                $data["EXPENSE_S_CD"][TEXT]     = $row["EXPENSE_S_CD"];
                $data["MONEY_DUE"][NUMBER]      = $row["EXPENSE_S_MONEY"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "MONEY_DUE_S_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
