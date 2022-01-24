<?php

require_once('for_php7.php');

class knjc160Query extends Query {

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
    
    //生成済み一覧
    function GetList($year)
    {
        $query  = "SELECT YEAR, SEMESTER, SMALLINT(MONTH) AS MONTH, MAX(UPDATED) as UPDATED";
        $query .= "  FROM ATTEND_SEMES_DAT";
        $query .= " WHERE COPYCD = '0' AND";
        $query .= "       YEAR   = '".$year."' ";
        $query .= " GROUP BY YEAR, SEMESTER, MONTH";
        return $query;
    }

    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
            
        if ($model->cmd2 == "1") //出欠入力制御日付のみ更新 
        {
            $query = knjc160Query::UpdateLdate($model->limit_date);
            $db->query($query);

        } else { //実行ボタン

            //TERM_GETの値を先に取得する
            knjc160Query::getTerm_Get($db,$model->year,$model->month);

            //月別------------------------------------------------------
            $query = "LOCK TABLE attend_semes_dat IN SHARE MODE";
            $db->query($query);

            $query = knjc160Query::DeleteQuery("attend_semes_dat",$model->year,$this->tmp_semester,$this->tmp_month);
            $db->query($query);

            $query = knjc160Query::InsertSemesterQuery($model->year,$this->tmp_semester,$this->tmp_month,$this->tmp_sdate,$this->tmp_edate); 

            $db->query($query);

            //科目別------------------------------------------------------
            $query = "LOCK TABLE attend_subclass_dat IN SHARE MODE";
            $db->query($query);

            $query = knjc160Query::DeleteQuery("attend_subclass_dat",$model->year,$this->tmp_semester,$this->tmp_month);
            $db->query($query);

            $query = knjc160Query::InsertSubclassQuery($model->year,$this->tmp_semester,$this->tmp_month,$this->tmp_sdate,$this->tmp_edate);
            $db->query($query);

            //コントロールマスタの更新
            $query = knjc160Query::UpdateLdate($model->limit_date);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    function DeleteQuery($table,$year,$semester,$month)
    {
        $query  = "DELETE FROM ".$table;
        $query .= " WHERE year     = '".$year."'";
        $query .= "   AND semester = '".$semester."'";
        $query .= "   AND month    = '".$month."'";
        $query .= "   AND copycd   = '0'";
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
        $next_month = ($mon<13) ? $mon : ($mon-12);//翌月
        //それ以外はその月の２日から翌月の１日まで集計
        if ($parameter[2] == "0")
        {
            $this->tmp_sdate    = $year."-".$parameter[1]."-02";
            $this->tmp_edate    = $year."-".$next_month."-01";
            //$this->tmp_sdate    = $year."-".$parameter[1]."-01";
            //$this->tmp_edate    = $db->getOne("VALUES LAST_DAY(DATE('".$year."-".$parameter[1]."-01'))");

        //開始月の場合は開始日以降翌月の１日まで集計
        } elseif ($parameter[2] == "1") {
            
            $this->tmp_sdate = $sdate;
            $this->tmp_edate = $year."-".$next_month."-01";
            //$this->tmp_sdate = $sdate;
            //$this->tmp_edate = $db->getOne("VALUES LAST_DAY(DATE('".$year."-".$parameter[1]."-01'))");
        
        //終了月の場合はその月の２日から終了日まで集計
        } elseif ($parameter[2] == "2") {
        
            $this->tmp_sdate = $year."-".$parameter[1]."-02";
            $this->tmp_edate = $edate;
            //$this->tmp_sdate = $year."-".$parameter[1]."-01";
            //$this->tmp_edate = $edate;
        }
    }

    function InsertSemesterQuery($year,$semester,$month,$sdate,$edate) //学期別集計
    {   
       /* 時間割データと整合性がとれた出欠データのみを集計 */
       /* 時間割データに登録されていない講座の出欠データがあっても無視します */
        
        $query  = "INSERT INTO attend_semes_dat ";
                                    /* 時間割データに登録されている講座のみの出欠データ */
        $query .= "WITH T_attend_dat (schregno, attenddate, periodcd, chaircd,di_cd) ";
        $query .= "               AS (SELECT T0.schregno, T0.attenddate, T0.periodcd, T0.chaircd, T0.di_cd ";
        $query .= "                     FROM attend_dat T0, ";
        $query .= "                        (SELECT T2.schregno ";
        $query .= "                               ,T1.executedate,T1.periodcd ";
        $query .= "                           FROM sch_chr_dat T1, chair_std_dat T2 ";
        $query .= "                          WHERE T1.year      = '".$year."' ";
        $query .= "                            AND T1.semester  = '".$semester."' ";
        $query .= "                            AND T1.executedate BETWEEN T2.appdate AND T2.appenddate ";
        $query .= "                            AND T1.executedate BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "                            AND T1.year      = T2.year ";
        $query .= "                            AND T1.semester  = T2.semester ";
        $query .= "                            AND T1.chaircd   = T2.chaircd ";
        $query .= "                            AND T1.periodcd != '0' ";
        $query .= "                          group by T2.schregno,T1.executedate,T1.periodcd) T1 ";
        $query .= "                   WHERE T0.year      = '".$year."' ";
        $query .= "                     AND T0.attenddate BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "                     and T0.schregno   = T1.schregno ";
        $query .= "                     AND T0.attenddate = T1.executedate ";
        $query .= "                     AND T0.periodcd   = T1.periodcd ";
        $query .= "                  ), ";
                                     /* 時間割データにある各生徒の受講すべき日と、それぞれの日の最初の校時、最後の校時、校時数 */
        $query .= "     T_period_cnt (schregno, executedate, first_period, last_period, period_cnt) ";
        $query .= "               AS (SELECT T2.schregno ";
        $query .= "                           ,T1.executedate ";
        $query .= "                           ,MIN(T1.periodcd)             AS first_period ";
        $query .= "                           ,MAX(T1.periodcd)             AS last_period ";
        $query .= "                           ,COUNT(DISTINCT T1.periodcd)  AS period_cnt ";
        $query .= "                     FROM sch_chr_dat T1, chair_std_dat T2 ";
        $query .= "                    WHERE T1.year      = '".$year."' ";
        $query .= "                      AND T1.semester  = '".$semester."' ";
        $query .= "                      AND T1.executedate BETWEEN T2.appdate AND T2.appenddate ";
        $query .= "                      AND T1.executedate BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "                      AND T1.year      = T2.year ";
        $query .= "                      AND T1.semester  = T2.semester ";
        $query .= "                      AND T1.chaircd   = T2.chaircd ";
        $query .= "                      AND T1.periodcd != '0' ";
        $query .= "                      GROUP BY T2.schregno, T1.executedate) ";
        $query .= "SELECT '0' ";
        $query .= "      ,'".$year."' ";
        $query .= "      ,'".$month."' ";
        $query .= "      ,'".$semester."' ";
        $query .= "      ,TT1.schregno ";
        $query .= "      ,TT1.lesson ";
        $query .= "      ,COALESCE(TT2.absent,0)   AS absent ";
        $query .= "      ,COALESCE(TT3.suspend,0)  AS suspend ";
        $query .= "      ,COALESCE(TT4.mourning,0) AS mourning ";
        $query .= "      ,COALESCE(TT5.sick,0)     AS sick ";
        $query .= "      ,COALESCE(TT5.notice,0)   AS notice ";
        $query .= "      ,COALESCE(TT5.nonotice,0) AS nonotice ";
        $query .= "      ,COALESCE(TT6.late,0)     AS late ";
        $query .= "      ,COALESCE(TT6.early,0)    AS early ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM  ";
        /* 授業日数 */
        $query .= "   (SELECT T2.schregno ";
        $query .= "          ,COUNT(DISTINCT T1.executedate) AS lesson ";
        $query .= "      FROM sch_chr_dat T1 ";
        $query .= "          ,chair_std_dat T2 ";
        $query .= "     WHERE T1.chaircd   = T2.chaircd ";
        $query .= "       AND T1.year      = T2.year ";
        $query .= "       AND T1.semester  = T2.semester ";
        $query .= "       AND T1.year      = '".$year."' ";
        $query .= "       AND T1.semester  = '".$semester."' ";
        $query .= "       AND T1.executedate BETWEEN T2.appdate AND T2.appenddate ";
        $query .= "       AND T1.executedate BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "       AND T1.periodcd != '0' ";
        $query .= "     GROUP BY T2.schregno) TT1 ";
        /* 公欠日数 */
        $query .= "LEFT OUTER JOIN (SELECT schregno, COUNT(DISTINCT attenddate) AS absent ";
        $query .= "                   FROM T_attend_dat  ";
        $query .= "                  WHERE di_cd IN ('1','8') ";
        $query .= "                  GROUP BY schregno) TT2 ";
        $query .= "  ON TT1.schregno = TT2.schregno ";
        /* 出停日数 */
        $query .= "LEFT OUTER JOIN (SELECT schregno, COUNT(DISTINCT attenddate) AS suspend ";
        $query .= "                   FROM T_attend_dat  ";
        $query .= "                  WHERE di_cd IN ('2','9') ";
        $query .= "                  GROUP BY schregno) TT3 ";
        $query .= "  ON TT1.schregno = TT3.schregno ";
        /* 忌引日数 */
        $query .= "LEFT OUTER JOIN (SELECT schregno, COUNT(DISTINCT attenddate) AS mourning ";
        $query .= "                   FROM T_attend_dat  ";
        $query .= "                  WHERE di_cd IN ('3','10') ";
        $query .= "                  GROUP BY schregno) TT4  ";
        $query .= "  ON TT1.schregno = TT4.schregno ";
        /* 病欠、事故欠(届)、事故欠(無)日数 */
        $query .= "LEFT OUTER JOIN ";
        $query .= "   (SELECT W0.schregno, ";
        $query .= "           SUM(CASE W0.di_cd WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS sick, ";
        $query .= "           SUM(CASE W0.di_cd WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS notice, ";
        $query .= "           SUM(CASE W0.di_cd WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS nonotice ";
        $query .= "      FROM attend_dat W0, ";
                              /* 受講すべき日と同じ最初の校時に勤怠コードがあって校時数が同じ(つまりは出席数が0)を求める */
        $query .= "            (SELECT T0.schregno, T0.executedate, T0.first_period ";
        $query .= "               FROM T_period_cnt T0, ";
        $query .= "                    (SELECT W1.schregno ";
        $query .= "                           ,W1.attenddate ";
        $query .= "                           ,MIN(W1.periodcd) AS first_period ";
        $query .= "                           ,COUNT(W1.periodcd) AS period_cnt ";
        $query .= "                       FROM T_attend_dat W1 ";
        $query .= "                      WHERE W1.di_cd IN ('4','5','6','11','12','13') ";
        $query .= "                      GROUP BY W1.schregno, W1.attenddate) T1 ";
        $query .= "              WHERE T0.schregno     = T1.schregno ";
        $query .= "                AND T0.executedate  = T1.attenddate ";
        $query .= "                AND T0.first_period = T1.first_period ";
        $query .= "                AND T0.period_cnt   = T1.period_cnt) W1 ";
        $query .= "     WHERE W0.schregno   = W1.schregno ";
        $query .= "       AND W0.attenddate = W1.executedate ";
        $query .= "       AND W0.periodcd   = W1.first_period ";
        $query .= "       AND W0.periodcd  != '0' ";
        $query .= "     GROUP BY W0.schregno ) TT5 ";
        $query .= "  ON TT1.schregno = TT5.schregno ";
        /* 遅刻、早退日数 */
        $query .= "LEFT OUTER JOIN  ";
        $query .= "   (SELECT T0.schregno ";
        $query .= "      ,COUNT(T2.attenddate) AS late ";
        $query .= "      ,COUNT(T3.attenddate) AS early    ";
                         /* 1日の校時数が出席すべき校時数と異なる(つまりは出席した校時がある)日のみ */
        $query .= "      FROM T_period_cnt T0           ";

        $query .= "           INNER JOIN (SELECT W1.schregno, W1.attenddate, COUNT(W1.periodcd) AS period_cnt ";
        $query .= "                         FROM T_attend_dat W1 ";
        $query .= "                        WHERE W1.di_cd NOT IN ('0','14') ";  //出席扱いの勤怠コードを除いて１日の校時数をカウント
        $query .= "                        GROUP BY W1.schregno, W1.attenddate) T1 ON T0.schregno    = T1.schregno ";
        $query .= "                                                               AND T0.executedate = T1.attenddate ";
        $query .= "                                                               AND T0.period_cnt != T1.period_cnt ";
        $query .= "      LEFT OUTER JOIN (SELECT schregno ,attenddate ,periodcd ";
        $query .= "                         FROM T_attend_dat  ";
        $query .= "                        WHERE di_cd IN ('4','5','6','15','11','12','13','3','10') "; // alp modify
        $query .= "                      ) T2 ON T0.schregno     = T2.schregno ";
        $query .= "                          AND T0.executedate  = T2.attenddate ";
        $query .= "                          AND T0.first_period = T2.periodcd ";
        $query .= "      LEFT OUTER JOIN (SELECT schregno ,attenddate ,periodcd ";
        $query .= "                         FROM T_attend_dat  ";
        $query .= "                        WHERE di_cd IN ('4','5','6','16','3','10') ";
        $query .= "                      ) T3 ON T0.schregno    = T3.schregno ";
        $query .= "                          AND T0.executedate = T3.attenddate ";
        $query .= "                          AND T0.last_period = T3.periodcd ";
        $query .= "       GROUP BY T0.schregno) TT6 ";
        $query .= "  ON TT1.schregno = TT6.schregno ";
//echo $query."<BR>";
        return $query;
    }
    
    //科目別集計
    function InsertSubclassQuery($year,$semester,$month,$sdate,$edate)
    {
        $query  = " INSERT INTO attend_subclass_dat ";
        $query .= " (copycd ";
        $query .= " ,year ";
        $query .= " ,month ";
        $query .= " ,semester ";
        $query .= " ,schregno ";
        $query .= " ,classcd ";
        $query .= " ,subclasscd ";
        $query .= " ,lesson ";
        $query .= " ,absent ";
        $query .= " ,suspend ";
        $query .= " ,mourning ";
        $query .= " ,sick ";
        $query .= " ,notice ";
        $query .= " ,nonotice ";
        $query .= " ,nurseoff ";
        $query .= " ,late ";
        $query .= " ,early ";
        $query .= " ,registercd ";
        $query .= " ,updated) ";
        $query .= " SELECT '0' ";
        $query .= "      ,'".$year."' ";
        $query .= "      ,'".$month."' ";
        $query .= "      ,'".$semester."' ";
        $query .= "      ,S1.SCHREGNO ";
        $query .= "      ,SUBSTR(S1.SUBCLASSCD,1,2) AS CLASSCD ";
        $query .= "      ,S1.SUBCLASSCD ";
        $query .= "      ,COUNT(*)  AS LESSON ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END)  AS  ABSENT ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END)  AS  SUSPEND ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '15' THEN 1 ELSE 0 END)  AS  LATE ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY ";
        $query .= "      ,'".STAFFCD."'";
        $query .= "      ,SYSDATE() ";
        $query .= "FROM ";
        $query .= " (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, SUBCLASSCD ";
        $query .= "  FROM   SCH_CHR_DAT T1 ";
        $query .= "         ,CHAIR_STD_DAT T2 ";
        $query .= "         ,CHAIR_DAT T3 ";
        $query .= "  WHERE      T1.YEAR     = '".$year."' ";
        $query .= "         AND T1.SEMESTER = '".$semester."' ";
        $query .= "         AND T1.EXECUTEDATE BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "         AND T1.PERIODCD != '0' ";
        $query .= "         AND T1.CHAIRCD  = T2.CHAIRCD ";
        $query .= "         AND T1.YEAR     = T2.YEAR ";
        $query .= "         AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "         AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "         AND T1.CHAIRCD  = T3.CHAIRCD ";
        $query .= "         AND T1.YEAR     = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "  GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, SUBCLASSCD)S1 ";
        $query .= "LEFT JOIN ATTEND_DAT S2 ON S2.YEAR = '".$year."' ";
        $query .= "                        AND S2.ATTENDDATE BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "                        AND S2.ATTENDDATE = S1.EXECUTEDATE ";
        $query .= "                        AND S2.PERIODCD   = S1.PERIODCD ";
        $query .= "                        AND S1.SCHREGNO   = S2.SCHREGNO ";
        $query .= "GROUP BY S1.SCHREGNO, S1.SUBCLASSCD ";
//echo $query."<BR>";
        return $query;
    }
} 
?>