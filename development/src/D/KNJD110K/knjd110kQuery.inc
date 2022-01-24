<?php

require_once('for_php7.php');

class knjd110kQuery extends Query {

    var $tmp_semester;   //UDFのTERM_GETの値を一時代入
    var $tmp_month;      //選択月
    var $tmp_sdate;      //データ抽出範囲の開始日付の値を一時代入
    var $tmp_edate;      //データ抽出範囲の終了日付の値を一時代入

    //起動チェック
    function ChecktoStart($db)
    {
        return $db->getOne("SELECT COUNT(*) FROM SEMESTER_MST");
    }

    function getYear()
    {
        return "SELECT YEAR FROM SEMESTER_MST GROUP BY YEAR";
    }

    function getAttendDate()
    {
        return "SELECT attend_ctrl_date FROM CONTROL_MST";
    }

    //学期の間の月を取得
    function getSemesterMonth($year)
    {
        return "SELECT * FROM semester_mst WHERE year = '".$year."' AND semester <> '9'";
    }
    
    //学校種別の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND NAME1 IN ('".SCHOOLKIND."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生成済み一覧
    function GetList($year, $model)
    {
        //NO003-->
        $query  = "SELECT CASE WHEN T1.MONTH < 4 THEN 1 ELSE 0 END AS SORT2, ";
        $query .= "       T1.YEAR, T1.SEMESTER, T1.MONTH, T1.APPOINTED_DAY, T1.UPDATED, T1.REGISTERCD, L1.STAFFNAME ";
        $query .= "  FROM ( ";
        //NO003<--
        $query .= "SELECT YEAR, SEMESTER, SMALLINT(MONTH) AS MONTH, APPOINTED_DAY, MAX(REGISTERCD) as REGISTERCD, MAX(UPDATED) as UPDATED";    //alp 2005/02/03
        $query .= "  FROM ATTEND_SEMES_DAT";
        $query .= " WHERE COPYCD = '0' AND";
        $query .= "       YEAR   = '".$year."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110kQuery::getSchregnoInSql($model, $year, "").") ";
        }
        $query .= " GROUP BY YEAR, SEMESTER, MONTH,APPOINTED_DAY";    //alp 2005/02/03
        //NO003-->
        $query .= ") T1 ";
        $query .= " LEFT JOIN STAFF_MST L1 ON T1.REGISTERCD = L1.STAFFCD ";
        $query .= "ORDER BY SEMESTER, 1, MONTH ";
        //NO003<--
        return $query;
    }

    //校種対応用
    function getSchregnoInSql($model, $year, $semester) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     REG_D.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REG_D ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_G.YEAR = REG_D.YEAR ";
        $query .= "         AND REG_G.GRADE = REG_D.GRADE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            if ($model->school_kind != "ALL") {
                $query .= "         AND REG_G.SCHOOL_KIND = '".$model->school_kind."' ";
            } else {
                $query .= "         AND REG_G.SCHOOL_KIND IN ('".implode($model->allSchoolKind,"','")."') ";
            }
        }
        $query .= " WHERE ";
        $query .= "     REG_D.YEAR = '".$year."' ";
        if (strlen($semester)) {
            $query .= "     AND REG_D.SEMESTER = '".$semester."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     REG_D.SCHREGNO ";
        return $query;
    }

    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
            
        if ($model->cmd2 == "1") //出欠入力制御日付のみ更新 
        {
            $db->autoCommit(false);
            $query = knjd110kQuery::UpdateLdate($model->limit_date);
            $db->query($query);
            $db->commit();

        } else { //実行ボタン

            //TERM_GETの値を先に取得する
            knjd110kQuery::getTerm_Get($db,$model->year,$model->month);

            //--------alp 2005/02/03------------------------

            //指定日を終了日にセット（２～３１）
            if (strlen($model->day) && (int)$model->day > 1) {
                $this->tmp_edate = strftime("%Y-%m",strtotime($this->tmp_sdate))."-".sprintf("%02d",$model->day);

            //処理月コンボの最終日を終了日にセット
            } else {
                $model->day = strftime("%d",strtotime($this->tmp_edate));
            }

            $db->autoCommit(false);

            //月別------------------------------------------------------
            $query = "LOCK TABLE attend_semes_dat IN SHARE MODE";
            $db->query($query);

            $query = knjd110kQuery::DeleteQuery("attend_semes_dat",$model->year,$this->tmp_semester,$this->tmp_month,$model);
            $db->query($query);

            $query = knjd110kQuery::InsertSemesterQuery($model->year,$this->tmp_semester,$this->tmp_month,$this->tmp_sdate,$this->tmp_edate,$model->day,$model); 
            $db->query($query);

            $db->commit();

            //科目別------------------------------------------------------
            $query = "LOCK TABLE attend_subclass_dat IN SHARE MODE";
            $db->query($query);

            $query = knjd110kQuery::DeleteQuery("attend_subclass_dat",$model->year,$this->tmp_semester,$this->tmp_month,$model);
            $db->query($query);

            $query = knjd110kQuery::InsertSubclassQuery($model->year,$this->tmp_semester,$this->tmp_month,$this->tmp_sdate,$this->tmp_edate,$model->day,$model);
            $db->query($query);

            //コントロールマスタの更新
            $query = knjd110kQuery::UpdateLdate($model->limit_date);
            $db->query($query);

            //指定日（締め日）マスタ
            if ($model->Properties["useSchool_KindField"] == "1") {
                if ($model->school_kind != "ALL") {
                    $query = knjd110kQuery::deleteAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, $model->school_kind);
                    $db->query($query);
                    $query = knjd110kQuery::insertAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, $model->day, $model->school_kind); 
                    $db->query($query);
                } else {
                    foreach ($model->allSchoolKind as $key => $school_kind) {
                        $query = knjd110kQuery::deleteAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, $school_kind);
                        $db->query($query);
                        $query = knjd110kQuery::insertAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, $model->day, $school_kind); 
                        $db->query($query);
                    }
                }
            } else {
                $query = knjd110kQuery::deleteAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, "");
                $db->query($query);
                $query = knjd110kQuery::insertAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, $model->day, ""); 
                $db->query($query);
            }

            $db->commit();
        }

        Query::dbCheckIn($db);
        return;
    }

    function deleteAppointedDayQuery($year, $semester, $month, $school_kind) {
        $query  = " DELETE FROM APPOINTED_DAY_MST ";
        $query .= "  WHERE YEAR     = '$year' ";
        if ($school_kind) {
            $query .= "    AND SCHOOL_KIND = '".$school_kind."' ";
        }
        $query .= "    AND MONTH    = '$month' ";
        $query .= "    AND SEMESTER = '$semester' ";
        return $query;
    }

    function insertAppointedDayQuery($year, $semester, $month, $day, $school_kind) {
        $query  = " INSERT INTO APPOINTED_DAY_MST( ";
        $query .= "     YEAR,  ";
        if ($school_kind) {
            $query .= "     SCHOOL_KIND,  ";
        }
        $query .= "     MONTH,  ";
        $query .= "     SEMESTER,  ";
        $query .= "     APPOINTED_DAY,  ";
        $query .= "     REGISTERCD,  ";
        $query .= "     UPDATED ";
        $query .= " ) VALUES( ";
        $query .= "     '$year',  ";
        if ($school_kind) {
            $query .= "     '$school_kind',  ";
        }
        $query .= "     '$month',  ";
        $query .= "     '$semester',  ";
        $query .= "     '$day',  ";
        $query .= "     '".STAFFCD."',  ";
        $query .= "     SYSDATE() ";
        $query .= " ) ";
        return $query;
    }

    function DeleteQuery($table,$year,$semester,$month,$model)
    {
        $query  = "DELETE FROM ".$table;
        $query .= " WHERE year     = '".$year."'";
        $query .= "   AND semester = '".$semester."'";
        $query .= "   AND month    = '".$month."'";
        $query .= "   AND copycd   = '0'";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110kQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        return $query;
    }

    function UpdateLdate($limit_date)
    {
        $query  = "UPDATE control_mst";
        $query .= "   SET attend_ctrl_date = DATE('" .str_replace("/","-",$limit_date). "')";
        $query .= "      ,registercd       = '".STAFFCD."'";
        $query .= "      ,updated          = SYSDATE()";
        return $query;
    }

    /****************************************************************************************
      UDFのTERM_GETを直接SQL文で使うとメモリが足りない(コードSQL0954C)になるので一度変数に代入
      
      04/07/09 変更 指示画面の月のコンボで既にセットされている学期をセット
                    開始日付と終了日付を取得
     ***************************************************************************************/
    function &getTerm_Get($db,$year,$month)
    {
        //parameter[0] => 学期, parameter[1] => 月, parameter[2] => 開始月または終了月フラグ
        $parameter = explode("-",$month);

        //学期
        $this->tmp_semester = $parameter[0];

        //月
        $this->tmp_month = $parameter[1];

        //選択された月の学期開始日
        $sdate = $db->getOne("SELECT sdate FROM semester_mst WHERE year = '".$year."' AND semester = '".$parameter[0]."'");

        //選択された月の学期終了日
        $edate = $db->getOne("SELECT edate FROM semester_mst WHERE year = '".$year."' AND semester = '".$parameter[0]."'");

        //１月から３月の場合は年度を１増やす
        if ((int)$parameter[1] < 4 )
        {   
            $year++;
        }

        //04/11/01 修正依頼 :alpokinawa n.miyagi
        $mon = sprintf("%02d",((int)$parameter[1] + 1) );
        $next_month = ($mon<13) ? $mon : sprintf("%02d",($mon-12));//翌月
        $next_year  = ($mon<13) ? $year : ($year + 1);//翌年

        //中学か高校かを判断---2005.05.17---alp nakamoto---
        $school = $db->getOne("SELECT COUNT(*) FROM SCHOOL_MST WHERE SCHOOLNAME1 LIKE '%中学%'");

        //それ以外はその月の２日から翌月の１日まで集計
        if ($parameter[2] == "0")
        {
            //高校---2005.05.17---alp nakamoto---
            if ($school == 0)
            {
                $this->tmp_sdate    = $year."-".$parameter[1]."-02";
                $this->tmp_edate    = $next_year."-".$next_month."-01";
            //中学---2005.05.17---alp nakamoto---
            } else {
                $this->tmp_sdate    = $year."-".$parameter[1]."-01";
                $this->tmp_edate    = $db->getOne("VALUES LAST_DAY(DATE('".$year."-".$parameter[1]."-01'))");
            }

        //開始月の場合は開始日以降翌月の１日まで集計
        } elseif ($parameter[2] == "1") {
            
            //高校---2005.05.17---alp nakamoto---
            if ($school == 0)
            {
                $this->tmp_sdate = $sdate;
                $this->tmp_edate = $next_year."-".$next_month."-01";
            //中学---2005.05.17---alp nakamoto---
            } else {
                $this->tmp_sdate = $sdate;
                $this->tmp_edate = $db->getOne("VALUES LAST_DAY(DATE('".$year."-".$parameter[1]."-01'))");
            }
        
        //終了月の場合はその月の２日から終了日まで集計
        } elseif ($parameter[2] == "2") {
        
            //高校---2005.05.17---alp nakamoto---
            if ($school == 0)
            {
                $this->tmp_sdate = $year."-".$parameter[1]."-02";
                $this->tmp_edate = $edate;
            //中学---2005.05.17---alp nakamoto---
            } else {
                $this->tmp_sdate = $year."-".$parameter[1]."-01";
                $this->tmp_edate = $edate;
            }
        }

    }

    function InsertSemesterQuery($year,$semester,$month,$sdate,$edate,$day,$model) //学期別集計
    {   
       /* 時間割データと整合性がとれた出欠データのみを集計 */
       /* 時間割データに登録されていない講座の出欠データがあっても無視します */

        $query  = "INSERT INTO attend_semes_dat ";
        $query .= " (copycd ";
        $query .= " ,year ";
        $query .= " ,month ";
        $query .= " ,semester ";
        $query .= " ,schregno ";
        $query .= " ,appointed_day ";
        $query .= " ,lesson ";
        $query .= " ,offdays ";
        $query .= " ,absent ";
        $query .= " ,suspend ";
        $query .= " ,mourning ";
        $query .= " ,abroad ";
        $query .= " ,sick ";
        $query .= " ,notice ";
        $query .= " ,nonotice ";
        $query .= " ,late ";
        $query .= " ,early ";
        $query .= " ,registercd ";
        $query .= " ,updated) ";

        $query .= "WITH ";
        $query .= "SCHNO AS(";
        $query .= "       SELECT  W1.SCHREGNO,W1.GRADE,W1.HR_CLASS ";
        $query .= "       FROM    SCHREG_REGD_DAT W1 ";
        $query .= "       WHERE       W1.YEAR     = '".$year."' ";
        $query .= "                  AND W1.SEMESTER = '".$semester."' ";
        $query .= ")";
        
        $query .= ",SCH_SCHEDULE AS(";
        $query .= "    SELECT  T2.SCHREGNO ,T1.EXECUTEDATE,T1.PERIODCD,T1.CHAIRCD ";
        $query .= "    FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2, SCHNO T3 ";
        $query .= "    WHERE       T1.YEAR      = '".$year."' ";
        $query .= "            AND T1.SEMESTER  = '".$semester."' ";
        $query .= "            AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "            AND T1.EXECUTEDATE BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "            AND T1.YEAR      = T2.YEAR ";
        $query .= "            AND T1.SEMESTER  = T2.SEMESTER ";
        $query .= "            AND T1.CHAIRCD   = T2.CHAIRCD ";
        $query .= "            AND T3.SCHREGNO = T2.SCHREGNO ";
        //不在日数を除く---2005.10.07Add
        //転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
        //転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合
        $query .= "            AND    NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "                           WHERE   T6.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "                              ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                               (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
        // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "         AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '27' ";
        $query .= "                  ) ";
        // 勤怠コードが'28'は時間割にカウントしない
        $query .= "         AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '28' ";
        $query .= "                  ) ";
        $query .= "    GROUP BY T2.SCHREGNO ,T1.EXECUTEDATE,T1.PERIODCD,T1.CHAIRCD ";
        $query .= ")";

        $query .= ",T_attend_dat (schregno, attenddate, periodcd, chaircd, YOMIKAEMAE, di_cd) AS (";
        $query .= "    SELECT  T0.schregno, T0.attenddate, T0.periodcd, T0.chaircd, ATT_DI.REP_DI_CD AS YOMIKAEMAE, ";
        $query .= "            CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END AS DI_CD ";
        $query .= "    FROM    attend_dat T0 ";
        $query .= "            LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = T0.YEAR AND ATT_DI.DI_CD = T0.DI_CD, ";
        $query .= "            (SELECT  T1.schregno, T1.executedate, T1.periodcd ";
        $query .= "             FROM    SCH_SCHEDULE T1 ";
        $query .= "             group by T1.schregno, T1.executedate, T1.periodcd) T1 ";
        $query .= "    WHERE       T0.year      = '".$year."' ";
        $query .= "            AND T0.attenddate BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "            and T0.schregno   = T1.schregno ";
        $query .= "            AND T0.attenddate = T1.executedate ";
        $query .= "            AND T0.periodcd   = T1.periodcd ";
        //NO002-->
        $query .= "            AND    NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ";
        $query .= "                           WHERE   T7.SCHREGNO = T0.SCHREGNO AND ";
        $query .= "                                T7.TRANSFERCD IN('1','2') AND ";
        $query .= "                                T0.attenddate BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        //NO002<--
        $query .= ") ";

                                     /* 時間割データにある各生徒の受講すべき日と、それぞれの日の最初の校時、最後の校時、校時数 */
        $query .= ",T_period_cnt (schregno, executedate, first_period, last_period, period_cnt) AS (";
        $query .= "    SELECT  T1.schregno ";
        $query .= "           ,T1.executedate ";
        $query .= "           ,MIN(T1.periodcd)             AS first_period ";
        $query .= "           ,MAX(T1.periodcd)             AS last_period ";
        $query .= "           ,COUNT(DISTINCT T1.periodcd)  AS period_cnt ";
        $query .= "    FROM    SCH_SCHEDULE T1 ";
        $query .= "    GROUP BY T1.schregno, T1.executedate ";
        $query .= ") ";
# 2005/05/02 ALP Modify End
        //遅刻・早退で使用---2005.10.03Add
        $query .= " ,T_attend_dat2 AS ( ";
        $query .= "     SELECT schregno, attenddate ";
        $query .= "       FROM T_attend_dat ";
        $query .= "      WHERE di_cd IN ('2','9','3','10')  ";
        $query .= "     GROUP BY schregno, attenddate ) ";
        $query .= ",T_period_cnt2 AS (";
        $query .= "    SELECT  T1.schregno ";
        $query .= "           ,T1.executedate ";
        $query .= "           ,T1.first_period ";
        $query .= "           ,T1.last_period ";
        $query .= "           ,T1.period_cnt ";
        $query .= "    FROM    T_period_cnt T1 ";
                        //出停・忌引が一つでもあれば、遅刻・早退はカウントしない---2005.10.03ALP
        $query .= "     WHERE (T1.schregno ,T1.executedate) NOT IN (SELECT schregno ,attenddate FROM T_attend_dat2 W2 )  ";
        $query .= ") ";
        //留学日数を算出  休学を含める---2005.10.03Add
        $query .= ",TRANSFER_SCHREG AS(";
        $query .=      "SELECT  T3.SCHREGNO, T3.TRANSFERCD, COUNT(DISTINCT T1.EXECUTEDATE)AS TRANSFER_DATE ";
        $query .=      "FROM    SCHREG_TRANSFER_DAT T3, SCH_CHR_DAT T1, CHAIR_STD_DAT T2 ";
        $query .=      "WHERE   T1.YEAR = '".$year."' AND ";
        $query .=              "T1.SEMESTER = '".$semester."' AND ";
        $query .=              "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ";
        $query .=              "T1.EXECUTEDATE BETWEEN DATE('".$sdate."') AND DATE('".$edate."') AND ";
        $query .=              "T1.YEAR = T2.YEAR AND ";
        $query .=              "T1.SEMESTER = T2.SEMESTER AND ";
        $query .=              "T1.CHAIRCD = T2.CHAIRCD AND ";
        $query .=              "T3.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) AND ";
        $query .=              "T3.SCHREGNO = T2.SCHREGNO AND ";
        $query .=              "T3.TRANSFERCD IN('1','2') AND ";
        $query .=              "T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ";
        //不在日数を除く---2005.10.07Add
        //転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
        //転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合
        $query .= "            AND    NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "                           WHERE   T6.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "                              ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                               (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
        // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "         AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '27' ";
        $query .= "                  ) ";
        // 勤怠コードが'28'は時間割にカウントしない
        $query .= "         AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '28' ";
        $query .= "                  ) ";
        $query .=      "GROUP BY T3.SCHREGNO, T3.TRANSFERCD ";
        $query .=      ") ";


        $query .= "SELECT '0' ";
        $query .= "      ,'".$year."' ";
        $query .= "      ,'".$month."' ";
        $query .= "      ,'".$semester."' ";
        $query .= "      ,TT1.schregno ";
        $query .= "      ,'".$day."'";                                 //alp 2005/02/03
        $query .= "      ,TT1.lesson ";
        $query .= "      ,COALESCE(TT11.offdays,0) AS offdays ";    //2005.10.03Add
        $query .= "      ,COALESCE(TT2.absent,0)   AS absent ";
        $query .= "      ,COALESCE(TT3.suspend,0)  AS suspend ";
        $query .= "      ,COALESCE(TT4.mourning,0) AS mourning ";
        $query .= "      ,COALESCE(TT12.abroad,0)  AS abroad ";     //2005.10.03Add
        $query .= "      ,COALESCE(TT5.sick,0)     AS sick ";
        $query .= "      ,COALESCE(TT5.notice,0)   AS notice ";
        $query .= "      ,COALESCE(TT5.nonotice,0) AS nonotice ";
        $query .= "      ,COALESCE(TT6.late,0) + COALESCE(TT7.late,0)   AS late ";          //05/03/03AlpModify
        $query .= "      ,COALESCE(TT6.early,0) + COALESCE(TT8.early,0)  AS early ";        //05/03/03AlpModify
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM  ";
        /* 授業日数 */
        $query .= "   (SELECT  T1.schregno ";
        $query .= "           ,COUNT(DISTINCT T1.executedate) AS lesson ";
        $query .= "    FROM SCH_SCHEDULE T1 ";
        $query .= "    GROUP BY T1.schregno) TT1 ";

# 2005/05/02 ALP Modify End

        /* 公欠日数 */
        $query .= "LEFT OUTER JOIN (SELECT schregno, COUNT(DISTINCT attenddate) AS absent ";
        $query .= "                   FROM T_attend_dat  ";
        $query .= "                  WHERE YOMIKAEMAE IN ('1','8') ";
        $query .= "                  GROUP BY schregno) TT2 ";
        $query .= "  ON TT1.schregno = TT2.schregno ";
        /* 出停日数 */
        $query .= "LEFT OUTER JOIN (SELECT schregno, COUNT(DISTINCT attenddate) AS suspend ";
        $query .= "                   FROM T_attend_dat  ";
        $query .= "                  WHERE YOMIKAEMAE IN ('2','9') ";
        $query .= "                  GROUP BY schregno) TT3 ";
        $query .= "  ON TT1.schregno = TT3.schregno ";
        /* 忌引日数 */
        $query .= "LEFT OUTER JOIN (SELECT schregno, COUNT(DISTINCT attenddate) AS mourning ";
        $query .= "                   FROM T_attend_dat  ";
        $query .= "                  WHERE YOMIKAEMAE IN ('3','10') ";
        $query .= "                  GROUP BY schregno) TT4  ";
        $query .= "  ON TT1.schregno = TT4.schregno ";
        /* 病欠、事故欠(届)、事故欠(無)日数 */
        $query .= "LEFT OUTER JOIN ";
        $query .= "   (SELECT W0.schregno, ";
        $query .= "           SUM(CASE ATT_DI.REP_DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS sick, ";
        $query .= "           SUM(CASE ATT_DI.REP_DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS notice, ";
        $query .= "           SUM(CASE ATT_DI.REP_DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS nonotice ";
        $query .= "      FROM attend_dat W0 ";
        $query .= "           LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = W0.YEAR AND ATT_DI.DI_CD = W0.DI_CD, ";
                              /* 受講すべき日と同じ最初の校時に勤怠コードがあって校時数が同じ(つまりは出席数が0)を求める */
        $query .= "            (SELECT T0.schregno, T0.executedate, T0.first_period ";
        $query .= "               FROM T_period_cnt T0, ";
        $query .= "                    (SELECT W1.schregno ";
        $query .= "                           ,W1.attenddate ";
        $query .= "                           ,MIN(W1.periodcd) AS first_period ";
        $query .= "                           ,COUNT(W1.periodcd) AS period_cnt ";
        $query .= "                       FROM T_attend_dat W1 ";
        $query .= "                      WHERE W1.YOMIKAEMAE IN ('4','5','6','11','12','13') ";
        $query .= "                      GROUP BY W1.schregno, W1.attenddate) T1 ";
        $query .= "              WHERE T0.schregno     = T1.schregno ";
        $query .= "                AND T0.executedate  = T1.attenddate ";
        $query .= "                AND T0.first_period = T1.first_period ";
        $query .= "                AND T0.period_cnt   = T1.period_cnt) W1 ";
        $query .= "     WHERE W0.schregno   = W1.schregno ";
        $query .= "       AND W0.attenddate = W1.executedate ";
        $query .= "       AND W0.periodcd   = W1.first_period ";
        $query .= "     GROUP BY W0.schregno ) TT5 ";
        $query .= "  ON TT1.schregno = TT5.schregno ";
        /* 遅刻、早退日数 
         *    2004/11/05 alp yamashiro (仕様の変更により)忌引は遅刻、早退の対象となるように修正
         *    2005/02/25 alp yamashiro (仕様の変更により)最初の校時の忌引は遅刻の対象とならないように修正
         *               また、最後の校時の忌引は早退の対象とならないように修正
         */
        $query .= "LEFT OUTER JOIN  ";
        $query .= "   (SELECT T0.schregno ";
        $query .= "      ,COUNT(T2.attenddate) AS late ";
        $query .= "      ,COUNT(T3.attenddate) AS early    ";
        $query .= "      FROM T_period_cnt2 T0           ";//2005.10.07

        $query .= "           INNER JOIN (SELECT W1.schregno, W1.attenddate, COUNT(W1.periodcd) AS period_cnt ";                    //AlpModify
        $query .= "                         FROM T_attend_dat W1 ";                                                                    //AlpModify
        $query .= "                        WHERE W1.YOMIKAEMAE NOT IN ('0','14','15','16','29','30','31','32') ";                                                  //2005/03/03AlpModify
        $query .= "                        GROUP BY W1.schregno, W1.attenddate) T1 ON T0.schregno    = T1.schregno ";                //AlpModify
        $query .= "                                                               AND T0.executedate = T1.attenddate ";                //AlpModify
        $query .= "                                                               AND T0.period_cnt != T1.period_cnt ";                //AlpModify
        $query .= "      LEFT OUTER JOIN (SELECT schregno ,attenddate ,periodcd ";
        $query .= "                         FROM T_attend_dat W1 ";
        $query .= "                        WHERE YOMIKAEMAE IN ('4','5','6','11','12','13') ";                          //05/03/03AlpModify
        $query .= "                      ) T2 ON T0.schregno     = T2.schregno ";
        $query .= "                          AND T0.executedate  = T2.attenddate ";
        $query .= "                          AND T0.first_period = T2.periodcd ";
        $query .= "      LEFT OUTER JOIN (SELECT schregno ,attenddate ,periodcd ";
        $query .= "                         FROM T_attend_dat W1 ";
        $query .= "                        WHERE YOMIKAEMAE IN ('4','5','6') ";                                //05/03/03AlpModify
        $query .= "                      ) T3 ON T0.schregno    = T3.schregno ";
        $query .= "                          AND T0.executedate = T3.attenddate ";
        $query .= "                          AND T0.last_period = T3.periodcd ";
        $query .= "       GROUP BY T0.schregno) TT6 ";
        $query .= "  ON TT1.schregno = TT6.schregno ";
        // 遅刻日数2
        $query .= "LEFT OUTER JOIN  ";                                                                    //05/03/03AlpModify
        $query .= "   (SELECT T0.schregno, COUNT(T2.attenddate) AS late ";                                //05/03/03AlpModify
        $query .= "    FROM T_period_cnt2 T0           ";                                                //05/03/03AlpModify
        $query .= "    LEFT JOIN (SELECT schregno ,attenddate ,periodcd ";                                //05/03/03AlpModify
        $query .= "                FROM T_attend_dat W1 ";                                                //05/03/03AlpModify
        $query .= "                WHERE YOMIKAEMAE IN ('15','29','31','32') ";                                                //05/03/03AlpModify
        $query .= "               ) T2 ON T0.schregno     = T2.schregno ";                                //05/03/03AlpModify
        $query .= "                       AND T0.executedate  = T2.attenddate ";                        //05/03/03AlpModify
        $query .= "                       AND T0.first_period = T2.periodcd ";                            //05/03/03AlpModify
        $query .= "    GROUP BY T0.schregno) TT7 ON TT1.schregno = TT7.schregno ";                        //05/03/03AlpModify
        // 早退日数2
        $query .= "LEFT OUTER JOIN  ";                                                                    //05/03/03AlpModify
        $query .= "   (SELECT T0.schregno, COUNT(T3.attenddate) AS early    ";                            //05/03/03AlpModify
        $query .= "    FROM T_period_cnt T0           ";                                                //05/03/03AlpModify
        $query .= "    LEFT JOIN (SELECT schregno ,attenddate ,periodcd ";                                //05/03/03AlpModify
        $query .= "                FROM T_attend_dat W1 ";                                                //05/03/03AlpModify
        $query .= "                WHERE YOMIKAEMAE IN ('16','30','31','32') ";                                                //05/03/03AlpModify
        $query .= "               ) T3 ON T0.schregno    = T3.schregno ";                                //05/03/03AlpModify
        $query .= "                       AND T0.executedate = T3.attenddate ";                            //05/03/03AlpModify
        $query .= "                       AND T0.last_period = T3.periodcd ";                            //05/03/03AlpModify
        $query .= "    GROUP BY T0.schregno) TT8 ON TT1.schregno = TT8.schregno ";                        //05/03/03AlpModify

        //休学日数の表---2005.10.03Add
        $query .=    "LEFT JOIN(";
        $query .=       "select schregno, sum(transfer_date) as offdays ";
        $query .=       "from   TRANSFER_SCHREG ";
        $query .=       "where  transfercd = '2' ";
        $query .=       "group by schregno ";
        $query .=    ")tt11 ON TT11.SCHREGNO=TT1.SCHREGNO ";
        //留学日数の表---2005.10.03Add
        $query .=    "LEFT JOIN(";
        $query .=       "select schregno, sum(transfer_date) as abroad ";
        $query .=       "from   TRANSFER_SCHREG ";
        $query .=       "where  transfercd = '1' ";
        $query .=       "group by schregno ";
        $query .=    ")tt12 ON TT12.SCHREGNO=TT1.SCHREGNO ";

        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   WHERE TT1.SCHREGNO IN (".knjd110kQuery::getSchregnoInSql($model, $year, $semester).") ";
        }

//echo $query."<BR>";
        return $query;
    }
    
    //科目別集計
    function InsertSubclassQuery($year,$semester,$month,$sdate,$edate,$day,$model)
    {
        $query  = " INSERT INTO attend_subclass_dat ";
        $query .= " (copycd ";
        $query .= " ,year ";
        $query .= " ,month ";
        $query .= " ,semester ";
        $query .= " ,schregno ";
        $query .= " ,classcd ";
        $query .= " ,SCHOOL_KIND ";
        $query .= " ,CURRICULUM_CD ";
        $query .= " ,subclasscd ";
        $query .= " ,appointed_day ";  //alp 2005/02/03
        $query .= " ,lesson ";
        $query .= " ,offdays "; //休学---2005.10.07Add
        $query .= " ,absent ";
        $query .= " ,suspend ";
        $query .= " ,mourning ";
        $query .= " ,abroad ";  //留学---2005.10.07Add
        $query .= " ,sick ";
        $query .= " ,notice ";
        $query .= " ,nonotice ";
        $query .= " ,nurseoff ";
        $query .= " ,late ";
        $query .= " ,early ";
        $query .= " ,registercd ";
        $query .= " ,updated) ";

# 2005/05/01 ALP Modify Start
        $query .= "WITH SCHEDULE AS(";
        $query .= "    SELECT T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, value(T1.DATADIV,'0') as DATADIV ";
        $query .= "    FROM   SCH_CHR_DAT T1 ";
        $query .= "    WHERE      T1.YEAR     = '".$year."' ";
        $query .= "           AND T1.SEMESTER = '".$semester."' ";
        $query .= "           AND T1.EXECUTEDATE BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= ")";
        $query .= ",SCHNO AS(";
        $query .= "       SELECT  W1.SCHREGNO,W1.GRADE,W1.HR_CLASS ";
        $query .= "    FROM    SCHREG_REGD_DAT W1 ";
        $query .= "    WHERE       W1.YEAR     = '".$year."' ";
        $query .= "              AND W1.SEMESTER = '".$semester."' ";
        $query .= ")";
        //テスト項目マスタの集計フラグの表
        $query .= " , TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        $query .= "         TESTITEM_MST_COUNTFLG T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR       = '".$year."' ";
        $query .= "         AND T1.SEMESTER   = '".$semester."' ";
        $query .= "         AND T1.EXECUTEDATE BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "         AND T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        $query .= "     ) ";

        //NO002-->
        $query .= ",T_attend_dat AS(";
        $query .= "     SELECT W1.SCHREGNO,W1.ATTENDDATE,W1.PERIODCD, ";
        $query .= "            CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM   ATTEND_DAT W1 ";
        $query .= "            LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = W1.YEAR AND ATT_DI.DI_CD = W1.DI_CD ";
        $query .= "     WHERE  W1.ATTENDDATE BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T7 ";
        $query .= "                           WHERE T7.SCHREGNO = W1.SCHREGNO AND ";
        $query .= "                              T7.TRANSFERCD IN('1','2') AND ";
        $query .= "                              W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .= ")";
        //NO002<--

# 2005/05/01 ALP Modify End

        $query .= " SELECT '0' ";
        $query .= "      ,'".$year."' ";
        $query .= "      ,'".$month."' ";
        $query .= "      ,'".$semester."' ";
        $query .= "      ,S1.SCHREGNO ";
        $query .= "      ,SUBSTR(S1.SUBCLASSCD,1,2) AS CLASSCD ";
        $query .= "      ,S1.SCHOOL_KIND ";
        $query .= "      ,S1.CURRICULUM_CD ";
        $query .= "      ,S1.SUBCLASSCD ";
        $query .= "      ,'".$day."'";                                 //alp 2005/02/03
        $query .= "      ,COUNT(*)  AS LESSON ";
        $query .= "      ,COUNT(S3.SCHREGNO)  AS offdays "; //休学---2005.10.07Add
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END)  AS  ABSENT ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END)  AS  SUSPEND ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING ";
        $query .= "      ,COUNT(S4.SCHREGNO)  AS abroad ";  //留学---2005.10.07Add
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '15' THEN 1 ELSE 0 END)  AS  LATE ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY ";
        $query .= "      ,'".STAFFCD."'";
        $query .= "      ,SYSDATE() ";
        $query .= "FROM ";
        $query .= " (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ";
        $query .= "  FROM   SCHEDULE T1 ";
        $query .= "        ,CHAIR_STD_DAT T2 ";
        $query .= "        ,CHAIR_DAT T3 ";
        $query .= "           ,SCHNO T4 ";
        $query .= "  WHERE      T1.CHAIRCD  = T3.CHAIRCD ";
        $query .= "            AND T3.YEAR     = '".$year."' ";
        $query .= "            AND T3.SEMESTER = '".$semester."' ";
        $query .= "            AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "            AND T2.YEAR     = '".$year."' ";
        $query .= "            AND T2.SEMESTER = '".$semester."' ";
        $query .= "            AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "            AND T4.SCHREGNO = T2.SCHREGNO ";
        $query .= "            AND    NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T5 ";
        $query .= "                           WHERE   T5.EXECUTEDATE = T1.EXECUTEDATE AND ";
        $query .= "                                   T5.PERIODCD = T1.PERIODCD AND ";
        $query .= "                                   T5.CHAIRCD = T1.CHAIRCD AND ";
        $query .= "                                   T5.GRADE = T4.GRADE AND ";
        $query .= "                                   T5.HR_CLASS = T4.HR_CLASS AND ";
        $query .= "                                T1.DATADIV IN ('0','1') AND "; //テスト(DATADIV=2)以外
        $query .= "                                   T5.COUNTFLG = '0')";
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                         WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                           AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                           AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                           AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
# 2005/05/01 ALP Modify End
        //不在日数を除く---2005.10.07Add
        //転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
        //転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合
        $query .= "            AND    NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "                           WHERE   T6.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "                              ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                               (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
        $query .= "         AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '27' ";
        $query .= "                  ) ";
        // 勤怠コードが'28'は時間割にカウントしない
        $query .= "         AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '28' ";
        $query .= "                  ) ";

        $query .= "  GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)S1 ";

        //NO002-->
        $query .= " LEFT JOIN T_attend_dat S2 ON  S2.SCHREGNO   = S1.SCHREGNO  ";
        $query .= "                         AND S2.ATTENDDATE = S1.EXECUTEDATE  ";
        $query .= "                         AND S2.PERIODCD   = S1.PERIODCD  ";
        //NO002<--

        //休学---2005.10.07Add
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S3 ON S3.SCHREGNO = S1.SCHREGNO AND S3.TRANSFERCD = '2' ";
        $query .= "                         AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE  ";
        //留学---2005.10.07Add
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S4 ON S4.SCHREGNO = S1.SCHREGNO AND S4.TRANSFERCD = '1' ";
        $query .= "                         AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE  ";

        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   WHERE S1.SCHREGNO IN (".knjd110kQuery::getSchregnoInSql($model, $year, $semester).") ";
        }

        $query .= "GROUP BY S1.SCHREGNO, S1.SCHOOL_KIND, S1.CURRICULUM_CD, S1.SUBCLASSCD ";
# <= alp modify

    
//echo $query."<BR>";
        return $query;
    }
} 
?>