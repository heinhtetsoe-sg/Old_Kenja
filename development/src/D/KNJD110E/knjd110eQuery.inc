<?php

require_once('for_php7.php');

class knjd110eQuery extends Query
{
    // public $tmp_semester;   //UDFのTERM_GETの値を一時代入
    // public $tmp_month;      //選択月
    // public $tmp_sdate;      //データ抽出範囲の開始日付の値を一時代入
    // public $tmp_edate;      //データ抽出範囲の終了日付の値を一時代入

    //起動チェック
    public function checktoStart($db)
    {
        return $db->getOne("SELECT COUNT(*) FROM SEMESTER_MST");
    }

    public function getYear()
    {
        return "SELECT YEAR FROM SEMESTER_MST GROUP BY YEAR";
    }

    public function getAttendDate()
    {
        return "SELECT attend_ctrl_date FROM CONTROL_MST";
    }

    //学期の間の月を取得
    public function getSemesterMonth($year)
    {
        return "SELECT * FROM semester_mst WHERE year = '".$year."' AND semester <> '9' ORDER BY semester, sdate";
    }
    
    //学校種別の取得
    public function getNameMstA023($model)
    {
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
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME1 IN ('".SCHOOLKIND."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生成済み一覧
    public function getList($year, $model)
    {
        //NO003-->
        $query  = "SELECT CASE WHEN T1.MONTH < 4 THEN 1 ELSE 0 END AS SORT2, ";
        $query .= "       T1.YEAR, T1.SEMESTER, T1.MONTH, T1.APPOINTED_DAY, T1.UPDATED ";
        $query .= "  FROM ( ";
        //NO003<--
        $query .= "SELECT W1.YEAR, W1.SEMESTER, SMALLINT(W1.MONTH) AS MONTH, W1.APPOINTED_DAY, MAX(W1.UPDATED) as UPDATED";
        $query .= "  FROM ATTEND_SUBCLASS_DAT W1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_DAT REG_D ON REG_D.SCHREGNO = W1.SCHREGNO ";
            $query .= "           AND REG_D.YEAR = W1.YEAR ";
            $query .= "           AND REG_D.SEMESTER = W1.SEMESTER ";
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_G.YEAR = REG_D.YEAR ";
            $query .= "           AND REG_G.GRADE = REG_D.GRADE ";
            if ($model->school_kind != "ALL") {
                $query .= "           AND REG_G.SCHOOL_KIND = '".$model->school_kind."' ";
            } else {
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode($model->allSchoolKind, "','")."') ";
            }
        }
        $query .= " WHERE W1.COPYCD = '0' AND";
        $query .= "       W1.YEAR   = '".$year."' ";
        $query .= " GROUP BY W1.YEAR, W1.SEMESTER, W1.MONTH,W1.APPOINTED_DAY";
        //NO003-->
        $query .= ") T1 ";
        $query .= "ORDER BY T1.SEMESTER, 1, T1.MONTH ";
        //NO003<--
        return $query;
    }

    //登録者を取得(生成済み一覧)
    public function getRegister($model, $row)
    {
        $query  = " WITH MAX_REGISTERCD AS ( ";
        $query .= "     SELECT ";
        $query .= "         MAX(W1.REGISTERCD) as REGISTERCD ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT W1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_DAT REG_D ON REG_D.SCHREGNO = W1.SCHREGNO ";
            $query .= "           AND REG_D.YEAR = W1.YEAR ";
            $query .= "           AND REG_D.SEMESTER = W1.SEMESTER ";
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_G.YEAR = REG_D.YEAR ";
            $query .= "           AND REG_G.GRADE = REG_D.GRADE ";
            if ($model->school_kind != "ALL") {
                $query .= "           AND REG_G.SCHOOL_KIND = '".$model->school_kind."' ";
            } else {
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode($model->allSchoolKind, "','")."') ";
            }
        }
        $query .= "     WHERE ";
        $query .= "             W1.COPYCD         = '0' ";
        $query .= "         AND W1.YEAR           = '{$row["YEAR"]}' ";
        $query .= "         AND W1.SEMESTER       = '{$row["SEMESTER"]}' ";
        $query .= "         AND W1.MONTH          = '".sprintf("%02d", $row["MONTH"])."' ";
        $query .= "         AND W1.APPOINTED_DAY  = '{$row["APPOINTED_DAY"]}' ";
        $query .= "         AND W1.UPDATED        = '{$row["UPDATED"]}' ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.REGISTERCD, ";
        $query .= "     L1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     MAX_REGISTERCD T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON T1.REGISTERCD = L1.STAFFCD ";

        return $query;
    }

    //校種対応用
    public function getSchregnoInSql($model, $year, $semester)
    {
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
                $query .= "         AND REG_G.SCHOOL_KIND IN ('".implode($model->allSchoolKind, "','")."') ";
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

    //出欠データの存在チェック
    public function getCountAttendDat($year, $model)
    {
        $query  = "SELECT COUNT(*) FROM ATTEND_DAT WHERE YEAR = '{$year}'";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110eQuery::getSchregnoInSql($model, $year, "").") ";
        }
        return $query;
    }

    //名称マスタ「C040」に登録されている、年度・学期・月は、実行不可とする。
    public function getNameMstC040($model)
    {
        //DB接続
        $db = Query::dbCheckOut();
        //param[0] => 学期, param[1] => 月, param[2] => 開始月または終了月フラグ
        $param = explode("-", $model->month);
        //SQL
        $query  = " SELECT COUNT(*) FROM NAME_MST ";
        $query .= "  WHERE NAMECD1 = 'C040' ";
        $query .= "    AND NAME1 = '".$model->year."' ";
        $query .= "    AND NAME2 = '".$param[0]."' ";
        $query .= "    AND NAME3 = '".$param[1]."' ";
        $rtnCnt = $db->getOne($query);
        //DB切断
        Query::dbCheckIn($db);

        return $rtnCnt;
    }

    public function &getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        if ($model->cmd2 == "1") { //出欠入力制御日付のみ更新
            $db->autoCommit(false);
            $query = knjd110eQuery::updateLdate($model->limit_date);
            $db->query($query);
            $db->commit();
        } else { //実行ボタン
            //add 2005/01/07　選択された年度内と月以前の未実施チェック
            if ($model->cmd != "execute2") {
                //sort($model->allMonth);
                //reset($model->allMonth);
                foreach ($model->allMonth as $key => $val) {
                    if ($val == $model->month) {
                        break;
                    }

                    knjd110eQuery::getTermGet($db, $model->year, $val, $model);
                    $arr = explode("-", $val);

                    //データ存在チェック
                    $query  = "SELECT COUNT(*) FROM attend_subclass_dat ";
                    $query .= " WHERE year     = '".$model->year."'";
                    $query .= "   AND semester = '".$arr[0]."'";
                    $query .= "   AND month    = '".$arr[1]."'";
                    if ($model->Properties["useSchool_KindField"] == "1") {
                        $query .= "   AND SCHREGNO IN (".knjd110eQuery::getSchregnoInSql($model, $model->year, $arr[0]).") ";
                    }

                    if ($db->getOne($query) == "0") {
                        $model->error_msg = "未処理の月があります。\\n（ ".$model->ctrl["学期名"][$arr[0]].(int)$arr[1]."月 ）";
                        Query::dbCheckIn($db);
                        return false;
                    }

                    //日指定最終日チェック
                    $query  = "SELECT appointed_day FROM attend_subclass_dat ";
                    $query .= " WHERE year     = '".$model->year."'";
                    $query .= "   AND semester = '".$arr[0]."'";
                    $query .= "   AND month    = '".$arr[1]."'";
                    if ($model->Properties["useSchool_KindField"] == "1") {
                        $query .= "   AND SCHREGNO IN (".knjd110eQuery::getSchregnoInSql($model, $model->year, $arr[0]).") ";
                    }

                    $cur_day = $db->getOne($query);
                    $app_day = strftime("%d", strtotime($model->tmp_edate));
                    if ((int)$cur_day < (int)$app_day) {
                        $model->error_msg = "処理漏れの月があります。\\n（ ".$model->ctrl["学期名"][$arr[0]].(int)$arr[1]."月".$cur_day."日以降 ）";
                        Query::dbCheckIn($db);
                        return false;
                    }
                }
            }

            knjd110eQuery::getTermGet($db, $model->year, $model->month, $model);

            //--------add 2005/01/07------------------------

            //開始日より前はエラー
            if (strlen($model->day) && (int)$model->day < (int)strftime("%d", strtotime($model->tmp_sdate))) {
                $model->setWarning("MSG901", "開始日以前の日は指定できません。\\n\\n開始日：".strftime("%Y年%m月%e日", strtotime($model->tmp_sdate)));
                return false;
            }

            //処理月コンボの最終日を終了日にセット
            if (!strlen($model->day) || (int)$model->day > (int)strftime("%d", strtotime($model->tmp_edate))) {
                $model->day = (int)strftime("%d", strtotime($model->tmp_edate));

            //指定日を終了日にセット
            } elseif (strlen($model->day) && (int)$model->day < (int)strftime("%d", strtotime($model->tmp_edate))) {
                $model->tmp_edate = strftime("%Y-%m", strtotime($model->tmp_edate))."-".$model->day;
            }

            $db->autoCommit(false);


            //科目別----------------------------------------------------
            $query = "LOCK TABLE attend_subclass_dat IN SHARE MODE";
            $db->query($query);

            $query = knjd110eQuery::deleteQuery("attend_subclass_dat", $model->year, $model->tmp_semester, $model->tmp_month, $model);
            $db->query($query);

            $query = knjd110eQuery::insertSubclassQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->tmp_sdate, $model->tmp_edate, $model->day, $model);
            $db->query($query);

            //コントロールマスタの更新
            $query = knjd110eQuery::updateLdate($model->limit_date);
            $db->query($query);

            $db->commit();


            //更新データ件数チェック
            $query  = "SELECT COUNT(*) FROM attend_subclass_dat ";
            $query .= " WHERE year     = '".$model->year."'";
            $query .= "   AND semester = '".$model->tmp_semester."'";
            $query .= "   AND month    = '".$model->tmp_month."'";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "   AND SCHREGNO IN (".knjd110eQuery::getSchregnoInSql($model, $model->year, $model->tmp_semester).") ";
            }
            if ($db->getOne($query) == "0") {
                $model->setWarning("MSG303");
                Query::dbCheckIn($db);
                return false;
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    //生成済みデータ削除
    public function getDeleteQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->del_check as $val) {
            $arrDel = explode("-", $val);
            $year = $arrDel[0];
            $month = $arrDel[1];
            $semester = $arrDel[2];
            //科目別
            $query = knjd110eQuery::deleteQuery("attend_subclass_dat", $year, $semester, $month, $model);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    public function deleteQuery($table, $year, $semester, $month, $model)
    {
        $query  = "DELETE FROM ".$table;
        $query .= " WHERE year     = '".$year."'";
        $query .= "   AND semester = '".$semester."'";
        $query .= "   AND month    = '".$month."'";
        $query .= "   AND copycd   = '0'";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110eQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        return $query;
    }

    public function updateLdate($limit_date)
    {
        $query  = "UPDATE control_mst";
        $query .= "   SET attend_ctrl_date = DATE('" .str_replace("/", "-", $limit_date). "')";
        $query .= "      ,registercd       = '".STAFFCD."'";
        $query .= "      ,updated          = SYSDATE()";
        return $query;
    }

    /****************************************************************************************
      UDFのTERM_GETを直接SQL文で使うとメモリが足りない(コードSQL0954C)になるので一度変数に代入

      04/07/09 変更 指示画面の月のコンボで既にセットされている学期をセット
                    開始日付と終了日付を取得
     ***************************************************************************************/
    public function &getTermGet($db, $year, $month, &$model)
    {
        //parameter[0] => 学期, parameter[1] => 月, parameter[2] => 開始月または終了月フラグ
        $parameter = explode("-", $month);

        //学期
        $model->tmp_semester = $parameter[0];

        //月
        $model->tmp_month = $parameter[1];

        //選択された月の学期開始日
        $sdate = $db->getOne("SELECT sdate FROM semester_mst WHERE year = '".$year."' AND semester = '".$parameter[0]."'");

        //選択された月の学期終了日
        $edate = $db->getOne("SELECT edate FROM semester_mst WHERE year = '".$year."' AND semester = '".$parameter[0]."'");

        //１月から３月の場合は年度を１増やす
        if ((int)$parameter[1] < 4) {
            $year++;
        }

        if ($parameter[2] == "0") {
            $model->tmp_sdate    = $year."-".$parameter[1]."-01";
            $model->tmp_edate    = $db->getOne("VALUES LAST_DAY(DATE('".$year."-".$parameter[1]."-01'))");
        } elseif ($parameter[2] == "1") {
            $model->tmp_sdate = $sdate;
            $model->tmp_edate = $db->getOne("VALUES LAST_DAY(DATE('".$year."-".$parameter[1]."-01'))");
        } elseif ($parameter[2] == "2") {
            $model->tmp_sdate = $year."-".$parameter[1]."-01";
            $model->tmp_edate = $edate;
        }
    }

    //科目別集計
    public function insertSubclassQuery($year, $semester, $month, $sdate, $edate, $day, $model)
    {
        /* 2005/02/02 課程の開始校時から終了校時までの範囲を指定しないで集計するように変更 */

        $query  = " INSERT INTO attend_subclass_dat ";
        $query .= " (copycd ";
        $query .= " ,year ";
        $query .= " ,month ";
        $query .= " ,semester ";
        $query .= " ,schregno ";
        $query .= " ,classcd ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  ,SCHOOL_KIND ";
            $query .= "  ,CURRICULUM_CD ";
        }
        $query .= " ,subclasscd ";
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
        $query .= " ,nurseoff ";
        $query .= " ,late ";
        $query .= " ,early ";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= "  ,VIRUS ";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= "  ,KOUDOME ";
        }
        $query .= " ,registercd ";
        $query .= " ,updated) ";

        //ALP---2005/09/21---処理時間短縮のため修正
        $query .= " WITH ATTEND AS ( ";
        $query .= "     SELECT * FROM ATTEND_DAT ";
        $query .= "     WHERE  ATTENDDATE BETWEEN DATE('".$sdate."') AND DATE('".$edate."')) ";
        $query .= " ,COUNTFLG AS ( ";
        $query .= "     SELECT * FROM SCH_CHR_COUNTFLG ";
        $query .= "     WHERE  EXECUTEDATE BETWEEN DATE('".$sdate."') AND DATE('".$edate."')) ";
        //テスト項目マスタの集計フラグの表
        $query .= " , TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        } else {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        }
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        $query .= "         AND T2.TESTKINDCD IN ('01','02') ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         AND T2.SCORE_DIV = '01' ";
        }
        $query .= "     ) ";

        $query .= ",T_attend_dat AS(";
        $query .= "     SELECT W1.SCHREGNO,W1.ATTENDDATE,W1.PERIODCD, ";
        $query .= "            CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END AS DI_CD, ";
        $query .= "            ATT_DI.MULTIPLY ";
        $query .= "     FROM   ATTEND_DAT W1 ";
        $query .= "            LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = W1.YEAR AND ATT_DI.DI_CD = W1.DI_CD ";
        $query .= "     WHERE  W1.ATTENDDATE BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T7 ";
        $query .= "                        WHERE T7.SCHREGNO = W1.SCHREGNO AND ";
        $query .= "                              T7.TRANSFERCD IN('1','2') AND ";
        $query .= "                              W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .= ")";
        $query .= ", TEMP1 AS (SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.PERIOD_MINUTES, ";
        $query .= "         T1.JITU_JIFUN, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD ";
        $query .= "  FROM   (SELECT t2.SCHREGNO,t1.YEAR,t1.SEMESTER,t1.CHAIRCD,t1.EXECUTEDATE,t1.PERIODCD,t3.GRADE,t3.HR_CLASS,t1.DATADIV ";
        $query .= "               ,INT(VALUE(B001.NAMESPARE3, SCHM.JITU_JIFUN, '50')) AS PERIOD_MINUTES ";
        $query .= "               ,INT(VALUE(SCHM.JITU_JIFUN, '50')) AS JITU_JIFUN ";
        $query .= "            FROM SCH_CHR_DAT t1 ";
        $query .= "                 INNER JOIN CHAIR_STD_DAT t2 ON ";
        $query .= "                        t1.CHAIRCD  = t2.CHAIRCD ";
        $query .= "                    AND t1.YEAR     = t2.YEAR ";
        $query .= "                    AND t1.SEMESTER = t2.SEMESTER ";
        $query .= "                    AND t1.EXECUTEDATE BETWEEN t2.APPDATE AND t2.APPENDDATE ";
        $query .= "                 INNER JOIN SCHREG_REGD_DAT t3 ON ";
        $query .= "                        t3.SCHREGNO = t2.SCHREGNO ";
        $query .= "                    AND t3.YEAR     = t2.YEAR ";
        $query .= "                    AND t3.SEMESTER = t2.SEMESTER ";
        $query .= "                 LEFT JOIN NAME_MST B001 ON B001.NAMECD1 = 'B001' ";
        $query .= "                                        AND B001.NAMECD2 = T1.PERIODCD ";
        $query .= "                 LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = t3.YEAR ";
        $query .= "                                        AND GDAT.GRADE = t3.GRADE ";
        $query .= "                 LEFT JOIN V_SCHOOL_MST SCHM ON SCHM.YEAR = t3.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "                                    AND SCHM.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_G.YEAR = t3.YEAR ";
            $query .= "           AND REG_G.GRADE = t3.GRADE ";
            if ($model->school_kind != "ALL") {
                $query .= "           AND REG_G.SCHOOL_KIND = '".$model->school_kind."' ";
            } else {
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode($model->allSchoolKind, "','")."') ";
            }
        }
        $query .= "           WHERE t1.YEAR     = '".$year."' ";
        $query .= "             AND t1.SEMESTER = '".$semester."' ";
        $query .= "             AND t1.EXECUTEDATE BETWEEN DATE('".$sdate."') AND DATE('".$edate."') ";
        $query .= "         ) T1";
        //ALP---2005/09/21---処理時間短縮のため修正
        $query .= "         INNER JOIN CHAIR_DAT T2  ";
        $query .= "                          ON T2.YEAR     = T1.YEAR  ";
        $query .= "                         AND T2.SEMESTER = T1.SEMESTER  ";
        $query .= "                         AND T2.CHAIRCD  = T1.CHAIRCD  ";
        $query .= "   WHERE NOT EXISTS(SELECT 'X' FROM COUNTFLG T11 ";
        $query .= "                     WHERE T11.EXECUTEDATE   = T1.EXECUTEDATE AND ";
        $query .= "                           T11.PERIODCD      = T1.PERIODCD AND ";
        $query .= "                           T11.CHAIRCD       = T1.CHAIRCD AND ";
        $query .= "                           T11.GRADE         = T1.GRADE AND ";
        $query .= "                           T11.HR_CLASS      = T1.HR_CLASS AND ";
        $query .= "                           T1.DATADIV       IN ('0','1','3') AND "; //テスト(DATADIV=2)以外
        $query .= "                           T11.COUNTFLG      = '0') ";
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                         WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                           AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                           AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                           AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
        //不在日数を除く
        //転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
        //転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合
        $query .= "         AND NOT EXISTS(SELECT  'X' FROM SCHREG_ENT_GRD_HIST_DAT T6 ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_DAT R6 ";
        $query .= "                                                     ON  R6.SCHREGNO = T6.SCHREGNO ";
        $query .= "                                                     AND R6.YEAR     = '{$year}' ";
        $query .= "                                                     AND R6.SEMESTER = '{$semester}' ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_GDAT G6 ";
        $query .= "                                                     ON  G6.YEAR         = R6.YEAR ";
        $query .= "                                                     AND G6.GRADE        = R6.GRADE ";
        $query .= "                                                     AND G6.SCHOOL_KIND  = T6.SCHOOL_KIND ";
        $query .= "                        WHERE   T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                              ((T6.GRD_DIV IN('1','2','3','6','7') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                               (T6.ENT_DIV IN('1','2','3','4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
        // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '27') ";
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '28') "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "   GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.PERIOD_MINUTES, ";
        $query .= "         T1.JITU_JIFUN, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD ";
        $query .= " ) ";

        $query .= " SELECT '0' ";
        $query .= "      ,'".$year."' ";
        $query .= "      ,'".$month."' ";
        $query .= "      ,'".$semester."' ";
        $query .= "      ,S1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  ,S1.CLASSCD ";
            $query .= "  ,S1.SCHOOL_KIND ";
            $query .= "  ,S1.CURRICULUM_CD ";
        } else {
            $query .= "  ,SUBSTR(S1.SUBCLASSCD,1,2) AS CLASSCD ";
        }
        $query .= "      ,S1.SUBCLASSCD ";
        $query .= "      ,'".$day."'";
        $query .= "      ,CEIL(SUM(S1.PERIOD_MINUTES)                                                    * 1.0 / S1.JITU_JIFUN) AS LESSON ";
        $query .= "      ,CEIL(SUM(CASE WHEN S3.SCHREGNO IS NOT NULL  THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  OFFDAYS ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ( '1',  '8') THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  ABSENT ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ( '2',  '9') THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  SUSPEND ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ( '3', '10') THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  MOURNING ";
        $query .= "      ,CEIL(SUM(CASE WHEN S4.SCHREGNO IS NOT NULL  THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  ABROAD ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ( '4', '11') THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  SICK ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ( '5', '12') THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  NOTICE ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ( '6', '13') THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  NONOTICE ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ('14')       THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  NURSEOFF ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) * S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  LATE ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ('16')       THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN)  AS  EARLY ";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= "  ,CEIL(SUM(CASE WHEN S2.DI_CD IN ('19', '20') THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN)  AS  VIRUS ";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= "  ,CEIL(SUM(CASE WHEN S2.DI_CD IN ('25', '26') THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN)  AS  KOUDOME ";
        }
        $query .= "      ,'".STAFFCD."'";
        $query .= "      ,SYSDATE() ";
        $query .= " FROM TEMP1 S1 ";

        $query .= " LEFT JOIN T_attend_dat S2 ON  S2.SCHREGNO   = S1.SCHREGNO  ";
        $query .= "                         AND S2.ATTENDDATE = S1.EXECUTEDATE  ";
        $query .= "                         AND S2.PERIODCD   = S1.PERIODCD  ";

        //休学
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S3 ON S3.SCHREGNO = S1.SCHREGNO AND S3.TRANSFERCD = '2' ";
        $query .= "                         AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE  ";
        //留学
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S4 ON S4.SCHREGNO = S1.SCHREGNO AND S4.TRANSFERCD = '1' ";
        $query .= "                         AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE  ";
        $query .= "GROUP BY ";
        $query .= "     S1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " S1.CLASSCD, ";
            $query .= " S1.SCHOOL_KIND, ";
            $query .= " S1.CURRICULUM_CD, ";
        }
        $query .= "     S1.SUBCLASSCD, ";
        $query .= "     S1.JITU_JIFUN ";

        return $query;
    }
}
