<?php

require_once('for_php7.php');

class knjd110bQuery extends Query
{
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
        $query .= "  FROM ATTEND_SEMES_DAT W1 ";
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
        $query .= "         ATTEND_SEMES_DAT W1 ";
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
            $query .= "   AND SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, "").") ";
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
            $query = knjd110bQuery::updateLdate($model->limit_date);
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

                    knjd110bQuery::getTermGet($model, $db, $model->year, $val);
                    $arr = explode("-", $val);

                    //データ存在チェック
                    $query  = "SELECT COUNT(*) FROM attend_semes_dat ";
                    $query .= " WHERE year     = '".$model->year."'";
                    $query .= "   AND semester = '".$arr[0]."'";
                    $query .= "   AND month    = '".$arr[1]."'";
                    if ($model->Properties["useSchool_KindField"] == "1") {
                        $query .= "   AND SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $model->year, $arr[0]).") ";
                    }

                    if ($db->getOne($query) == "0") {
                        $model->error_msg = "未処理の月があります。\\n（ ".$model->ctrl["学期名"][$arr[0]].(int)$arr[1]."月 ）";
                        Query::dbCheckIn($db);
                        return false;
                    }

                    //日指定最終日チェック
                    $query  = "SELECT appointed_day FROM attend_semes_dat ";
                    $query .= " WHERE year     = '".$model->year."'";
                    $query .= "   AND semester = '".$arr[0]."'";
                    $query .= "   AND month    = '".$arr[1]."'";
                    if ($model->Properties["useSchool_KindField"] == "1") {
                        $query .= "   AND SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $model->year, $arr[0]).") ";
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

            knjd110bQuery::getTermGet($model, $db, $model->year, $model->month);

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

            //echo "commit1" ."<BR>";
            //echo date("H:i:s") ."<BR>";
            $db->autoCommit(false);

            //学校マスタの情報を取得。
            $knjSchoolMst = knjd110bQuery::getSchoolMst($db, $model->year);

            //月別------------------------------------------------------
            $query = "LOCK TABLE attend_semes_dat IN SHARE MODE";
            $db->query($query);

            $query = knjd110bQuery::deleteQuery("attend_semes_dat", $model->year, $model->tmp_semester, $model->tmp_month, $model);
            $db->query($query);

            $query = knjd110bQuery::insertSemesterQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->tmp_sdate, $model->tmp_edate, $model->day, $knjSchoolMst, $model);
            $db->query($query);
            $db->commit();
            //echo date("H:i:s") ."<BR>";

            //echo "commit2" ."<BR>";
            //echo date("H:i:s") ."<BR>";
            //科目別----------------------------------------------------
            $query = "LOCK TABLE attend_subclass_dat IN SHARE MODE";
            $db->query($query);

            $query = knjd110bQuery::deleteQuery("attend_subclass_dat", $model->year, $model->tmp_semester, $model->tmp_month, $model);
            $db->query($query);

            $query = knjd110bQuery::insertSubclassQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->tmp_sdate, $model->tmp_edate, $model->day, $model);
            $db->query($query);

            //コントロールマスタの更新
            $query = knjd110bQuery::updateLdate($model->limit_date);
            $db->query($query);

            //指定日（締め日）マスタ
            if ($model->Properties["useSchool_KindField"] == "1") {
                if ($model->school_kind != "ALL") {
                    $query = knjd110bQuery::deleteAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->school_kind);
                    $db->query($query);
                    $query = knjd110bQuery::insertAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->day, $model->school_kind);
                    $db->query($query);
                } else {
                    foreach ($model->allSchoolKind as $key => $school_kind) {
                        $query = knjd110bQuery::deleteAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, $school_kind);
                        $db->query($query);
                        $query = knjd110bQuery::insertAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->day, $school_kind);
                        $db->query($query);
                    }
                }
            } else {
                $query = knjd110bQuery::deleteAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, "");
                $db->query($query);
                $query = knjd110bQuery::insertAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->day, "");
                $db->query($query);
            }

            $db->commit();
            //echo date("H:i:s") ."<BR>";

            //echo "commit3" ."<BR>";
            //echo date("H:i:s") ."<BR>";
            //欠席日付の登録
            $query = knjd110bQuery::attendAbsenceDeleteQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->tmp_sdate, $model->tmp_edate, $model);
            $db->query($query);
            $query = knjd110bQuery::attendAbsenceInsertQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->tmp_sdate, $model->tmp_edate, $knjSchoolMst, $model);
            $db->query($query);
            $db->commit();
            //echo date("H:i:s") ."<BR>";

            //echo "commit4" ."<BR>";
            //echo date("H:i:s") ."<BR>";
            //日付と出欠コードの登録
            $query = knjd110bQuery::attendDayDatDeleteQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->tmp_sdate, $model->tmp_edate, $model);
            $db->query($query);
            $query = knjd110bQuery::attendDayDatInsertQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->tmp_sdate, $model->tmp_edate, $knjSchoolMst, $model);
            $db->query($query);
            $db->commit();
            //echo date("H:i:s") ."<BR>";

            //科目別詳細SEQ存在チェック
            $subclassSeqArray = array();
            $result = $db->query(knjd110bQuery::getNameMstSeq($model->year, "C003"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclassSeqArray[] = $row["SEQ"];
            }
            //月別詳細SEQ存在チェック
            $semesSeqArray = array();
            $result = $db->query(knjd110bQuery::getNameMstSeq($model->year, "C002"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $semesSeqArray[] = $row["SEQ"];
            }

            //科目別詳細----------------------------------------------------
            if (0 < get_count($subclassSeqArray)) {
                //echo "commit5" ."<BR>";
                //echo date("H:i:s") ."<BR>";
                $table = "ATTEND_SUBCLASS_DETAIL_DAT";
                $query = knjd110bQuery::deleteQuery($table, $model->year, $model->tmp_semester, $model->tmp_month, $model);
                $db->query($query);
                $query = knjd110bQuery::insertSubclassDetailQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->tmp_sdate, $model->tmp_edate, $model);
                $db->query($query);
                $db->commit();
                //echo date("H:i:s") ."<BR>";
            }

            //月別詳細----------------------------------------------------
            if (0 < get_count($semesSeqArray)) {
                //echo "commit6" ."<BR>";
                //echo date("H:i:s") ."<BR>";
                $table = "ATTEND_SEMES_DETAIL_DAT";
                $query = knjd110bQuery::deleteQuery($table, $model->year, $model->tmp_semester, $model->tmp_month, $model);
                $db->query($query);
                $query = knjd110bQuery::insertSemesterDetailQuery($model->year, $model->tmp_semester, $model->tmp_month, $model);
                $db->query($query);
                $db->commit();
                //echo date("H:i:s") ."<BR>";
            }

            //学期マスタの情報を取得。
            $knjSemesterMst = knjd110bQuery::getSemesterMst($db, $model->year, "9");

            //欠課数上限値（実授業数）
            if ($knjSchoolMst["JUGYOU_JISU_FLG"] == "2") {
                //実行月の指定日（締め日）の翌日を取得
                $nextSdate = knjd110bQuery::getNextSdate($db, $model->tmp_edate, $knjSemesterMst["EDATE"]);
                //「学期+月」の配列
                $semeMonthArray = array();
                foreach ($model->allMonth as $key => $val) {
                    $param = explode("-", $val);
                    $semeMonthArray[] = $param[0] .$param[1];
                    if ($val == $model->month) {
                        break;
                    }
                }
                //集計テーブル参照
                //if (0 < get_count($semeMonthArray)) echo "semeMonthIn = " ."('" .implode("','", $semeMonthArray) ."')" ."<BR>";
                //時間割テーブル参照
                //if (strlen($nextSdate)) echo "nextSdate = " .$nextSdate ."<BR>";

                //---------- 通常科目の上限値を生成 ----------//
                $combinedFlg = "";
                //1:年間 学期マスタ９の終了日まで
                //echo "commit7" ."<BR>";
                //echo date("H:i:s") ."<BR>";
                $query = knjd110bQuery::deleteAbsenceHigh($model->year, "1", "", $combinedFlg, $model);
                $db->query($query);
                $query = knjd110bQuery::insertAbsenceHigh($model->year, "1", $nextSdate, $knjSemesterMst["EDATE"], $knjSchoolMst, $semeMonthArray, "", "", $combinedFlg, $model);
                $db->query($query);
                //コミット
                $db->commit();
                //echo date("H:i:s") ."<BR>";
                //2:随時 指定日まで
                //echo "commit8" ."<BR>";
                //echo date("H:i:s") ."<BR>";
                $query = knjd110bQuery::deleteAbsenceHigh($model->year, "2", "", $combinedFlg, $model);
                $db->query($query);
                $query = knjd110bQuery::insertAbsenceHigh($model->year, "2", "", $model->tmp_edate, $knjSchoolMst, $semeMonthArray, "", "", $combinedFlg, $model);
                $db->query($query);
                //コミット
                $db->commit();
                //echo date("H:i:s") ."<BR>";
                //---------- 合併先科目の上限値を生成（合併元科目の授業数から算出）----------//
                $combinedFlg = "on";
                //1:年間 学期マスタ９の終了日まで
                //echo "commit9" ."<BR>";
                //echo date("H:i:s") ."<BR>";
                $query = knjd110bQuery::deleteAbsenceHigh($model->year, "1", "", $combinedFlg, $model);
                $db->query($query);
                $query = knjd110bQuery::insertAbsenceHigh($model->year, "1", $nextSdate, $knjSemesterMst["EDATE"], $knjSchoolMst, $semeMonthArray, "", "", $combinedFlg, $model);
                $db->query($query);
                //コミット
                $db->commit();
                //echo date("H:i:s") ."<BR>";
                //2:随時 指定日まで
                //echo "commit10" ."<BR>";
                //echo date("H:i:s") ."<BR>";
                $query = knjd110bQuery::deleteAbsenceHigh($model->year, "2", "", $combinedFlg, $model);
                $db->query($query);
                $query = knjd110bQuery::insertAbsenceHigh($model->year, "2", "", $model->tmp_edate, $knjSchoolMst, $semeMonthArray, "", "", $combinedFlg, $model);
                $db->query($query);
                //コミット
                $db->commit();
                //echo date("H:i:s") ."<BR>";
                //---------------- 特別活動科目グループ毎 ----------------//
                $combinedFlg = "";
                //1:年間 学期マスタ９の終了日まで
                //echo "commit11" ."<BR>";
                //echo date("H:i:s") ."<BR>";
                $query = knjd110bQuery::deleteAbsenceHigh($model->year, "1", "sp", $combinedFlg, $model);
                $db->query($query);
                $query = knjd110bQuery::insertAbsenceHigh($model->year, "1", $nextSdate, $knjSemesterMst["EDATE"], $knjSchoolMst, $semeMonthArray, "sp", "", $combinedFlg, $model);
                $db->query($query);
                $query = knjd110bQuery::insertAbsenceHigh($model->year, "1", $nextSdate, $knjSemesterMst["EDATE"], $knjSchoolMst, $semeMonthArray, "sp", "total", $combinedFlg, $model);
                $db->query($query);
                //コミット
                $db->commit();
                //echo date("H:i:s") ."<BR>";
                //2:随時 指定日まで
                //echo "commit12" ."<BR>";
                //echo date("H:i:s") ."<BR>";
                $query = knjd110bQuery::deleteAbsenceHigh($model->year, "2", "sp", $combinedFlg, $model);
                $db->query($query);
                $query = knjd110bQuery::insertAbsenceHigh($model->year, "2", "", $model->tmp_edate, $knjSchoolMst, $semeMonthArray, "sp", "", $combinedFlg, $model);
                $db->query($query);
                $query = knjd110bQuery::insertAbsenceHigh($model->year, "2", "", $model->tmp_edate, $knjSchoolMst, $semeMonthArray, "sp", "total", $combinedFlg, $model);
                $db->query($query);
                //コミット
                $db->commit();
                //echo date("H:i:s") ."<BR>";
            }

            //更新データ件数チェック
            $query  = "SELECT COUNT(*) FROM attend_semes_dat ";
            $query .= " WHERE year     = '".$model->year."'";
            $query .= "   AND semester = '".$model->tmp_semester."'";
            $query .= "   AND month    = '".$model->tmp_month."'";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "   AND SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $model->year, $model->tmp_semester).") ";
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

    //詳細SEQ
    public function getNameMstSeq($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS SEQ ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";

        return $query;
    }

    //欠席日付の削除
    public function attendAbsenceDeleteQuery($year, $semester, $month, $sdate, $edate, $model)
    {
        $query  = " DELETE FROM ATTEND_ABSENCE_DAT ";
        $query .= " WHERE ";
        $query .= "     ABSENCE_DATE BETWEEN date('{$sdate}') AND date('{$edate}') ";
        $query .= "     AND YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        return $query;
    }

    //欠席日付の追加・・・ATTEND_SEMES_DAT(SICK,NOTICE,NONOTICE)をカウントした日付を登録する
    public function attendAbsenceInsertQuery($year, $semester, $month, $sdate, $edate, $knjSchoolMst, $model)
    {
        $query  = " INSERT INTO ATTEND_ABSENCE_DAT ";
        // chair_std_datまたはsch_chr_datの表
        $query .= " with t_sch_std as ( ";
        $query .= "     SELECT ";
        $query .= "         T2.schregno , ";
        $query .= "         T1.executedate, ";
        $query .= "         T1.periodcd, ";
        $query .= "         T1.chaircd ";
        $query .= "     FROM ";
        $query .= "         sch_chr_dat T1 ";
        $query .= "         left join chair_std_dat T2 on  ";
        $query .= "         T1.executedate BETWEEN T2.appdate AND ";
        $query .= "         T2.appenddate AND ";
        $query .= "         T1.year = T2.year AND ";
        $query .= "         T1.semester = T2.semester AND ";
        $query .= "         T1.chaircd = T2.chaircd ";
        $query .= "     WHERE ";
        $query .= "         T1.year = '{$year}' AND ";
        $query .= "         T1.semester = '{$semester}' AND ";
        $query .= "         T1.executedate BETWEEN DATE('{$sdate}') AND ";
        $query .= "         DATE('{$edate}') ";
        $query .= "     GROUP BY ";
        $query .= "         T2.schregno, ";
        $query .= "         T1.executedate, ";
        $query .= "         T1.periodcd, ";
        $query .= "         T1.chaircd ";
        $query .= "     ) ";
        // 時間割データに登録されている講座のみの出欠データ
        $query .= ", T_attend_dat (schregno, attenddate, periodcd, chaircd,di_cd) ";
        $query .= "    AS (SELECT T0.schregno, T0.attenddate, T0.periodcd, T0.chaircd, ATT_DI.REP_DI_CD ";
        $query .= "          FROM attend_dat T0 ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = T0.YEAR AND ATT_DI.DI_CD = T0.DI_CD, ";
        $query .= "             (SELECT T1.schregno ";
        $query .= "                    ,T1.executedate,T1.periodcd, T1.chaircd ";
        $query .= "                FROM t_sch_std T1 ";
        $query .= "               WHERE ";
        $query .= "                     NOT EXISTS(SELECT  'X' FROM SCHREG_ENT_GRD_HIST_DAT T6 ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_DAT R6 ";
        $query .= "                                                     ON  R6.SCHREGNO = T6.SCHREGNO ";
        $query .= "                                                     AND R6.YEAR     = '{$year}' ";
        $query .= "                                                     AND R6.SEMESTER = '{$semester}' ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_GDAT G6 ";
        $query .= "                                                     ON  G6.YEAR         = R6.YEAR ";
        $query .= "                                                     AND G6.GRADE        = R6.GRADE ";
        $query .= "                                                     AND G6.SCHOOL_KIND  = T6.SCHOOL_KIND ";
        $query .= "                                WHERE   T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                                      ((T6.GRD_DIV IN('1','2','3','6','7') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                       (T6.ENT_DIV IN('1','2','3','4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
        $query .= "                  AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '27') "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "                  AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                   LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '28') "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "                  AND NOT EXISTS(SELECT 'X' FROM CHAIR_DETAIL_DAT L4 ";
        $query .= "                                 WHERE L4.YEAR = '{$year}' ";
        $query .= "                                   AND L4.SEMESTER = '{$semester}' ";
        $query .= "                                   AND L4.CHAIRCD = T1.CHAIRCD ";
        $query .= "                                   AND L4.SEQ = '001' ";
        $query .= "                                   AND L4.REMARK2 = '1') "; // 1日出欠の対象外
        $query .= "               ) T1 ";
        $query .= "            ,(SELECT T1.schregno, T2.s_periodcd, T2.e_periodcd ";
        $query .= "                FROM schreg_regd_dat T1, ";
        $query .= "                    (SELECT T1.coursecd, T1.s_periodcd, T1.e_periodcd ";
        $query .= "                       FROM course_mst T1, course_ydat T2 ";
        $query .= "                      WHERE T1.coursecd = T2.coursecd ";
        $query .= "                        AND T2.year = '{$year}') T2 ";
        $query .= "               WHERE T1.coursecd = T2.coursecd  ";
        $query .= "                 AND T1.year     = '{$year}' ";
        $query .= "                 AND T1.semester = '{$semester}') T2 ";
        $query .= "        WHERE T0.schregno           = T1.schregno ";
        $query .= "          AND T0.attenddate         = T1.executedate ";
        $query .= "          AND T0.chaircd            = T1.chaircd ";
        $query .= "          AND T0.periodcd           = T1.periodcd ";
        $query .= "          AND T0.schregno           = T2.schregno ";
        $query .= "          AND T0.periodcd BETWEEN T2.s_periodcd AND T2.e_periodcd ";
        $query .= "          AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ";
        $query .= "                         WHERE   T7.SCHREGNO = T0.SCHREGNO AND ";
        $query .= "                                 T7.TRANSFERCD IN('1','2') AND ";
        $query .= "                                 T0.attenddate BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .= "     ) ";
        // 時間割データにある各生徒の受講すべき日と、それぞれの日の最初の校時、最後の校時、校時数
        $query .= ", T_period_cnt (schregno, executedate, first_period, last_period, period_cnt) ";
        $query .= "    AS (SELECT  T1.schregno ";
        $query .= "                ,T1.executedate ";
        $query .= "                ,MIN(T1.periodcd)             AS first_period ";
        $query .= "                ,MAX(T1.periodcd)             AS last_period ";
        $query .= "                ,COUNT(DISTINCT T1.periodcd)  AS period_cnt ";
        $query .= "          FROM ";
        $query .= "              (SELECT T1.schregno ";
        $query .= "                      ,T1.executedate,T1.periodcd, T1.chaircd ";
        $query .= "                 FROM t_sch_std T1 ";
        $query .= "                WHERE ";
        $query .= "                      NOT EXISTS(SELECT  'X' FROM SCHREG_ENT_GRD_HIST_DAT T6 ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_DAT R6 ";
        $query .= "                                                     ON  R6.SCHREGNO = T6.SCHREGNO ";
        $query .= "                                                     AND R6.YEAR     = '{$year}' ";
        $query .= "                                                     AND R6.SEMESTER = '{$semester}' ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_GDAT G6 ";
        $query .= "                                                     ON  G6.YEAR         = R6.YEAR ";
        $query .= "                                                     AND G6.GRADE        = R6.GRADE ";
        $query .= "                                                     AND G6.SCHOOL_KIND  = T6.SCHOOL_KIND ";
        $query .= "                                 WHERE   T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                                       ((T6.GRD_DIV IN('1','2','3','6','7') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                        (T6.ENT_DIV IN('1','2','3','4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
        $query .= "                  AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '27') "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "                  AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '28') "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "                  AND NOT EXISTS(SELECT 'X' FROM CHAIR_DETAIL_DAT L4 ";
        $query .= "                                 WHERE L4.YEAR = '{$year}' ";
        $query .= "                                   AND L4.SEMESTER = '{$semester}' ";
        $query .= "                                   AND L4.CHAIRCD = T1.CHAIRCD ";
        $query .= "                                   AND L4.SEQ = '001' ";
        $query .= "                                   AND L4.REMARK2 = '1') "; // 1日出欠の対象外
        $query .= "                ) T1 ";
        $query .= "              ,(SELECT T1.schregno, T2.s_periodcd, T2.e_periodcd ";
        $query .= "                  FROM schreg_regd_dat T1, ";
        $query .= "                      (SELECT T1.coursecd, T1.s_periodcd, T1.e_periodcd ";
        $query .= "                         FROM course_mst T1, course_ydat T2 ";
        $query .= "                        WHERE T1.coursecd = T2.coursecd ";
        $query .= "                          AND T2.year = '{$year}') T2 ";
        $query .= "                 WHERE T1.coursecd = T2.coursecd  ";
        $query .= "                   AND T1.year     = '{$year}' ";
        $query .= "                   AND T1.semester = '{$semester}') T3 ";
        $query .= "         WHERE T1.schregno  = T3.schregno ";
        $query .= "           AND T1.periodcd BETWEEN T3.s_periodcd AND T3.e_periodcd ";
        $query .= "           GROUP BY T1.schregno, T1.executedate) ";
        if ($knjSchoolMst["SYUKESSEKI_HANTEI_HOU"] == "1") {
            $query .= " ,T_attend_dat3 AS ( ";
            $query .= "     SELECT schregno, attenddate, MIN(periodcd) AS first_period, COUNT(periodcd) AS period_cnt ";
            $query .= "       FROM T_attend_dat ";
            $query .= "      WHERE di_cd IN ('2','9','3','10','19','20','25','26')  ";
            $query .= "     GROUP BY schregno, attenddate ) ";
            $query .= " ,T_period_suspend_mourning AS ( ";
            $query .= "     SELECT T0.schregno, T0.executedate ";
            $query .= "       FROM T_period_cnt T0, ";
            $query .= "            T_attend_dat3 T1 ";
            $query .= "      WHERE T0.schregno     = T1.schregno ";
            $query .= "        AND T0.executedate  = T1.attenddate ";
            $query .= "        AND T0.first_period = T1.first_period ";
            $query .= "        AND T0.period_cnt   = T1.period_cnt ) ";
        }
        //メイン
        $query .= " SELECT W0.schregno   AS SCHREGNO, ";
        $query .= "        W0.attenddate AS ABSENCE_DATE, ";
        $query .= "        '{$year}'     AS YEAR, ";
        $query .= "        '{$semester}' AS SEMESTER, ";
        $query .= "        '".STAFFCD."' AS REGISTERCD, ";
        $query .= "        SYSDATE()     AS UPDATED ";
        $query .= "   FROM attend_dat W0, ";
        $query .= "         (SELECT T0.schregno, T0.executedate ";
        if ($knjSchoolMst["SYUKESSEKI_HANTEI_HOU"] == "1") {
            $query .= "            ,T2.first_period ";
        } else {
            $query .= "            ,T0.first_period ";
        }
        $query .= "            FROM T_period_cnt T0, ";
        $query .= "                 (SELECT W1.schregno ";
        $query .= "                        ,W1.attenddate ";
        $query .= "                        ,MIN(W1.periodcd)   AS first_period ";
        $query .= "                        ,COUNT(W1.periodcd) AS period_cnt ";
        $query .= "                    FROM T_attend_dat W1 ";
        $query .= "                   WHERE W1.di_cd IN ('4','5','6','11','12','13' ";
        if ($knjSchoolMst["SYUKESSEKI_HANTEI_HOU"] == "1") {
            $query .= "                                 ,'2','9','3','10','19','20','25','26' ";
        }
        $query .= "                                                                ) ";
        $query .= "                   GROUP BY W1.schregno, W1.attenddate) T1 ";
        if ($knjSchoolMst["SYUKESSEKI_HANTEI_HOU"] == "1") {
            $query .= "                INNER JOIN ";
            $query .= "                (SELECT W1.schregno ";
            $query .= "                       ,W1.attenddate ";
            $query .= "                       ,MIN(W1.periodcd)   AS first_period ";
            $query .= "                       ,COUNT(W1.periodcd) AS period_cnt ";
            $query .= "                   FROM T_attend_dat W1 ";
            $query .= "                  WHERE W1.di_cd IN ('4','5','6','11','12','13') ";
            $query .= "                  GROUP BY W1.schregno, W1.attenddate) T2 ";
            $query .= "                 ON T2.schregno    = T1.schregno ";
            $query .= "                AND T2.attenddate  = T1.attenddate ";
        }
        $query .= "           WHERE T0.schregno     = T1.schregno ";
        $query .= "             AND T0.executedate  = T1.attenddate ";
        $query .= "             AND T0.first_period = T1.first_period ";
        $query .= "             AND T0.period_cnt   = T1.period_cnt) W1 ";
        $query .= "  WHERE W0.schregno           = W1.schregno ";
        $query .= "    AND W0.attenddate         = W1.executedate ";
        $query .= "    AND W0.periodcd           = W1.first_period ";
        if ($knjSchoolMst["SYUKESSEKI_HANTEI_HOU"] == "1") {
            $query .= "   AND (W1.schregno, W1.executedate) NOT IN (SELECT T0.schregno, T0.executedate FROM T_period_suspend_mourning T0) ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND W0.SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        $query .= "  GROUP BY W0.schregno,W0.attenddate ";
        return $query;
    }

    //日付と出欠コードの削除
    public function attendDayDatDeleteQuery($year, $semester, $month, $sdate, $edate, $model)
    {
        $query  = " DELETE FROM ATTEND_DAY_DAT ";
        $query .= " WHERE ";
        $query .= "     ATTENDDATE BETWEEN date('{$sdate}') AND date('{$edate}') ";
        $query .= "     AND YEAR     = '{$year}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        return $query;
    }

    //日付と出欠コードの追加・・・ATTEND_SEMES_DAT(SICK,NOTICE,NONOTICE)をカウントした日付と出欠コードを登録する
    public function attendDayDatInsertQuery($year, $semester, $month, $sdate, $edate, $knjSchoolMst, $model)
    {
        $query  = " INSERT INTO ATTEND_DAY_DAT ";
        // chair_std_datまたはsch_chr_datの表
        $query .= " with t_sch_std as ( ";
        $query .= "     SELECT ";
        $query .= "         T2.schregno , ";
        $query .= "         T1.executedate, ";
        $query .= "         T1.periodcd, ";
        $query .= "         T1.chaircd ";
        $query .= "     FROM ";
        $query .= "         sch_chr_dat T1 ";
        $query .= "         left join chair_std_dat T2 on  ";
        $query .= "         T1.executedate BETWEEN T2.appdate AND ";
        $query .= "         T2.appenddate AND ";
        $query .= "         T1.year = T2.year AND ";
        $query .= "         T1.semester = T2.semester AND ";
        $query .= "         T1.chaircd = T2.chaircd ";
        $query .= "     WHERE ";
        $query .= "         T1.year = '{$year}' AND ";
        $query .= "         T1.semester = '{$semester}' AND ";
        $query .= "         T1.executedate BETWEEN DATE('{$sdate}') AND ";
        $query .= "         DATE('{$edate}') ";
        $query .= "     GROUP BY ";
        $query .= "         T2.schregno, ";
        $query .= "         T1.executedate, ";
        $query .= "         T1.periodcd, ";
        $query .= "         T1.chaircd ";
        $query .= "     ) ";
        // 時間割データに登録されている講座のみの出欠データ
        $query .= ", T_attend_dat (schregno, attenddate, periodcd, chaircd, di_cd, di_remark, di_remark_cd) ";
        $query .= "    AS (SELECT T0.schregno, T0.attenddate, T0.periodcd, T0.chaircd, ATT_DI.REP_DI_CD, T0.di_remark, T0.di_remark_cd ";
        $query .= "          FROM attend_dat T0 ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = T0.YEAR AND ATT_DI.DI_CD = T0.DI_CD, ";
        $query .= "             (SELECT T1.schregno ";
        $query .= "                    ,T1.executedate,T1.periodcd, T1.chaircd ";
        $query .= "                FROM t_sch_std T1 ";
        $query .= "               WHERE ";
        $query .= "                     NOT EXISTS(SELECT  'X' FROM SCHREG_ENT_GRD_HIST_DAT T6 ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_DAT R6 ";
        $query .= "                                                     ON  R6.SCHREGNO = T6.SCHREGNO ";
        $query .= "                                                     AND R6.YEAR     = '{$year}' ";
        $query .= "                                                     AND R6.SEMESTER = '{$semester}' ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_GDAT G6 ";
        $query .= "                                                     ON  G6.YEAR         = R6.YEAR ";
        $query .= "                                                     AND G6.GRADE        = R6.GRADE ";
        $query .= "                                                     AND G6.SCHOOL_KIND  = T6.SCHOOL_KIND ";
        $query .= "                                WHERE   T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                                      ((T6.GRD_DIV IN('1','2','3','6','7') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                       (T6.ENT_DIV IN('1','2','3','4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
        $query .= "                 AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '27') "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "                 AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '28') "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "               ) T1 ";
        $query .= "            ,(SELECT T1.schregno, T2.s_periodcd, T2.e_periodcd ";
        $query .= "                FROM schreg_regd_dat T1, ";
        $query .= "                    (SELECT T1.coursecd, T1.s_periodcd, T1.e_periodcd ";
        $query .= "                       FROM course_mst T1, course_ydat T2 ";
        $query .= "                      WHERE T1.coursecd = T2.coursecd ";
        $query .= "                        AND T2.year = '{$year}') T2 ";
        $query .= "               WHERE T1.coursecd = T2.coursecd  ";
        $query .= "                 AND T1.year     = '{$year}' ";
        $query .= "                 AND T1.semester = '{$semester}') T2 ";
        $query .= "        WHERE T0.schregno           = T1.schregno ";
        $query .= "          AND T0.attenddate         = T1.executedate ";
        $query .= "          AND T0.chaircd            = T1.chaircd ";
        $query .= "          AND T0.periodcd           = T1.periodcd ";
        $query .= "          AND T0.schregno           = T2.schregno ";
        $query .= "          AND T0.periodcd BETWEEN T2.s_periodcd AND T2.e_periodcd ";
        $query .= "     ) ";
        // 時間割データにある各生徒の受講すべき日と、それぞれの日の最初の校時、最後の校時、校時数
        $query .= ", T_period_cnt (schregno, executedate, first_period, last_period, period_cnt) ";
        $query .= "    AS (SELECT  T1.schregno ";
        $query .= "                ,T1.executedate ";
        $query .= "                ,MIN(T1.periodcd)             AS first_period ";
        $query .= "                ,MAX(T1.periodcd)             AS last_period ";
        $query .= "                ,COUNT(DISTINCT T1.periodcd)  AS period_cnt ";
        $query .= "          FROM ";
        $query .= "              (SELECT T1.schregno ";
        $query .= "                      ,T1.executedate,T1.periodcd, T1.chaircd ";
        $query .= "                 FROM t_sch_std T1 ";
        $query .= "                WHERE ";
        $query .= "                      NOT EXISTS(SELECT  'X' FROM SCHREG_ENT_GRD_HIST_DAT T6 ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_DAT R6 ";
        $query .= "                                                     ON  R6.SCHREGNO = T6.SCHREGNO ";
        $query .= "                                                     AND R6.YEAR     = '{$year}' ";
        $query .= "                                                     AND R6.SEMESTER = '{$semester}' ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_GDAT G6 ";
        $query .= "                                                     ON  G6.YEAR         = R6.YEAR ";
        $query .= "                                                     AND G6.GRADE        = R6.GRADE ";
        $query .= "                                                     AND G6.SCHOOL_KIND  = T6.SCHOOL_KIND ";
        $query .= "                                 WHERE   T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                                       ((T6.GRD_DIV IN('1','2','3','6','7') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                        (T6.ENT_DIV IN('1','2','3','4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
        $query .= "                  AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '27') "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "                  AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '28') "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "                ) T1 ";
        $query .= "              ,(SELECT T1.schregno, T2.s_periodcd, T2.e_periodcd ";
        $query .= "                  FROM schreg_regd_dat T1, ";
        $query .= "                      (SELECT T1.coursecd, T1.s_periodcd, T1.e_periodcd ";
        $query .= "                         FROM course_mst T1, course_ydat T2 ";
        $query .= "                        WHERE T1.coursecd = T2.coursecd ";
        $query .= "                          AND T2.year = '{$year}') T2 ";
        $query .= "                 WHERE T1.coursecd = T2.coursecd  ";
        $query .= "                   AND T1.year     = '{$year}' ";
        $query .= "                   AND T1.semester = '{$semester}') T3 ";
        $query .= "         WHERE T1.schregno  = T3.schregno ";
        $query .= "           AND T1.periodcd BETWEEN T3.s_periodcd AND T3.e_periodcd ";
        $query .= "           GROUP BY T1.schregno, T1.executedate) ";
        // 島根県の出欠区分に合わせた1日出欠の計算を行う
        $query .= " ,T_attend_dat_di_cd AS ( ";
        $query .= "     SELECT schregno, attenddate, di_cd, MIN(periodcd) AS first_period, MAX(periodcd) AS last_period, COUNT(periodcd) AS period_cnt ";
        $query .= "       FROM T_attend_dat ";
        $query .= "      WHERE di_cd IN ('0','1','2','3','6','34','36','38')  ";
        $query .= "     GROUP BY schregno, attenddate, di_cd ) ";
        $query .= " ,T_attend_day_dat_di_cd AS ( ";
        $query .= "     SELECT T0.schregno, T0.executedate AS attenddate, T1.di_cd ";
        $query .= "       FROM T_period_cnt T0, ";
        $query .= "            T_attend_dat_di_cd T1 ";
        $query .= "      WHERE T0.schregno     = T1.schregno ";
        $query .= "        AND T0.executedate  = T1.attenddate ";
        $query .= "        AND T0.first_period = T1.first_period ";
        $query .= "        AND T0.period_cnt   = T1.period_cnt ) ";
        $query .= " ,T_attend_day_dat_di_cd_0 AS ( ";
        $query .= "     SELECT T0.schregno, T0.executedate AS attenddate, T1.di_cd ";
        $query .= "       FROM T_period_cnt T0, ";
        $query .= "            T_attend_dat_di_cd T1 ";
        $query .= "      WHERE T0.schregno     = T1.schregno ";
        $query .= "        AND T0.executedate  = T1.attenddate ";
        $query .= "        AND T0.first_period = T1.first_period ";
        $query .= "        AND T0.period_cnt   = T1.period_cnt ";
        $query .= "        AND T1.di_cd        = '0' ";
        $query .= "     UNION ";
        $query .= "     SELECT T0.schregno, T0.executedate AS attenddate, T1.di_cd ";
        $query .= "       FROM T_period_cnt T0, ";
        $query .= "            T_attend_dat_di_cd T1 ";
        $query .= "      WHERE T0.schregno     = T1.schregno ";
        $query .= "        AND T0.executedate  = T1.attenddate ";
        $query .= "        AND T0.first_period = T1.first_period ";
        $query .= "        AND T0.last_period  = T1.last_period ";
        $query .= "        AND T1.di_cd        = '0' ";
        $query .= "     UNION ";
        $query .= "     SELECT T0.schregno, T0.executedate AS attenddate, T1.di_cd ";
        $query .= "       FROM T_period_cnt T0 ";
        $query .= "            LEFT JOIN T_attend_dat T1 ";
        $query .= "         ON T0.schregno     = T1.schregno ";
        $query .= "        AND T0.executedate  = T1.attenddate ";
        $query .= "        AND T0.first_period = T1.periodcd ";
        $query .= "            LEFT JOIN T_attend_dat T2 ";
        $query .= "         ON T0.schregno     = T2.schregno ";
        $query .= "        AND T0.executedate  = T2.attenddate ";
        $query .= "        AND T0.last_period  = T2.periodcd ";
        $query .= "      WHERE T1.di_cd        = '0' ";
        $query .= "        AND T2.di_cd        IN ('1','2','3') ) ";
        $query .= " ,T_attend_dat_period_cnt AS ( ";
        $query .= "     SELECT schregno, attenddate, COUNT(periodcd) AS period_cnt ";
        $query .= "       FROM T_attend_dat ";
        $query .= "      WHERE di_cd NOT IN ('0','14','15','16')  ";
        $query .= "     GROUP BY schregno, attenddate ) ";
        $query .= " ,T_attend_day_dat_di_cd_15_16_35 AS ( ";
        $query .= "     SELECT T0.schregno, T0.executedate AS attenddate, ";
        $query .= "            CASE WHEN T1.di_cd IN ('6','15') AND T2.di_cd IN ('6','16') THEN '35' ";
        $query .= "                 WHEN T1.di_cd IN ('6','15') THEN '15' ";
        $query .= "                 ELSE '16' ";
        $query .= "            END AS di_cd ";
        $query .= "       FROM T_period_cnt T0 ";
        $query .= "            LEFT JOIN T_attend_dat T1 ";
        $query .= "         ON T0.schregno     = T1.schregno ";
        $query .= "        AND T0.executedate  = T1.attenddate ";
        $query .= "        AND T0.first_period = T1.periodcd ";
        $query .= "            LEFT JOIN T_attend_dat T2 ";
        $query .= "         ON T0.schregno     = T2.schregno ";
        $query .= "        AND T0.executedate  = T2.attenddate ";
        $query .= "        AND T0.last_period  = T2.periodcd ";
        $query .= "            LEFT JOIN T_attend_dat_period_cnt T3 ";
        $query .= "         ON T0.schregno     = T3.schregno ";
        $query .= "        AND T0.executedate  = T3.attenddate ";
        $query .= "      WHERE (T1.di_cd IN ('6','15') OR T2.di_cd IN ('6','16')) ";
        $query .= "        AND T0.period_cnt   != VALUE(T3.period_cnt, 0) ) ";
        //メイン
        /* 出席(0) */
        $query .= " SELECT W0.schregno   AS SCHREGNO, ";
        $query .= "        W0.attenddate AS ATTENDDATE, ";
        $query .= "        W0.di_cd      AS DI_CD, ";
        $query .= "        NULL          AS DI_REMARK, ";
        $query .= "        '{$year}'     AS YEAR, ";
        $query .= "        '".STAFFCD."' AS REGISTERCD, ";
        $query .= "        SYSDATE()     AS UPDATED ";
        $query .= "   FROM T_attend_day_dat_di_cd_0 W0 ";
        $query .= "  WHERE W0.di_cd IN ('0') ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND W0.SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        /* 公欠(1)出停(2)忌引(3)欠席(6)留学(34)休学(36)除外(38) */
        $query .= "  UNION ALL ";
        $query .= " SELECT W0.schregno   AS SCHREGNO, ";
        $query .= "        W0.attenddate AS ATTENDDATE, ";
        $query .= "        W0.di_cd      AS DI_CD, ";
        $query .= "        NULL          AS DI_REMARK, ";
        $query .= "        '{$year}'     AS YEAR, ";
        $query .= "        '".STAFFCD."' AS REGISTERCD, ";
        $query .= "        SYSDATE()     AS UPDATED ";
        $query .= "   FROM T_attend_day_dat_di_cd W0 ";
        $query .= "  WHERE W0.di_cd IN ('1','2','3','6','34','36','38') ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND W0.SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        /* 遅刻(15)早退(16)遅刻早退(35) */
        $query .= "  UNION ALL ";
        $query .= " SELECT W0.schregno   AS SCHREGNO, ";
        $query .= "        W0.attenddate AS ATTENDDATE, ";
        $query .= "        W0.di_cd      AS DI_CD, ";
        $query .= "        NULL          AS DI_REMARK, ";
        $query .= "        '{$year}'     AS YEAR, ";
        $query .= "        '".STAFFCD."' AS REGISTERCD, ";
        $query .= "        SYSDATE()     AS UPDATED ";
        $query .= "   FROM T_attend_day_dat_di_cd_15_16_35 W0 ";
        $query .= "  WHERE W0.di_cd IN ('15','16','35') ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND W0.SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        return $query;
    }

    //学校マスタの情報を取得。
    public function getSchoolMst($db, $year)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $rtnRow;
    }

    //学期マスタの情報を取得。
    public function getSemesterMst($db, $year, $semester)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER = '{$semester}' ";
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $rtnRow;
    }

    public function getAppointedDate($year, $div, $model)
    {
        $query  = " SELECT MAX(APPOINTED_DATE) as APPOINTED_DATE, MAX(UPDATED) as UPDATED ";
        $query .= "   FROM SCHREG_ABSENCE_HIGH_DAT ";
        $query .= "  WHERE YEAR = '{$year}' ";
        $query .= "    AND DIV  = '{$div}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, "").") ";
        }
        return $query;
    }

    //実行月の指定日（締め日）の翌日を取得
    public function getNextSdate($db, $edate, $edate9)
    {
        //実行月の指定日（締め日）の翌日
        $sdate = $db->getOne("VALUES ADD_DAYS(DATE('{$edate}'), 1)");

        //学期マスタ９の終了日を超えたらブランク。
        $nextSdate = ($edate9 < $sdate) ? "" : $sdate;

        return $nextSdate;
    }

    public function deleteAbsenceHigh($year, $div, $sp, $combinedFlg, $model)
    {
        $table = ($sp == "sp") ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";

        $query  = " DELETE FROM ".$table." ";
        $query .= "  WHERE YEAR = '{$year}' ";
        $query .= "    AND DIV  = '{$div}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, "").") ";
        }
        if ($combinedFlg == "on" && $sp != "sp") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD in ( ";
                $query .= "         SELECT ";
                $query .= "             COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                $query .= "         FROM ";
                $query .= "             SUBCLASS_REPLACE_COMBINED_DAT ";
                $query .= "         WHERE ";
                $query .= "             YEAR = '{$year}' ";
                $query .= "    ) ";
            } else {
                $query .= "    AND SUBCLASSCD in ( ";
                $query .= "         SELECT ";
                $query .= "             COMBINED_SUBCLASSCD ";
                $query .= "         FROM ";
                $query .= "             SUBCLASS_REPLACE_COMBINED_DAT ";
                $query .= "         WHERE ";
                $query .= "             YEAR = '{$year}' ";
                $query .= "    ) ";
            }
        }
        return $query;
    }

    //欠課数上限値
    public function insertAbsenceHigh($year, $div, $sdate, $edate, $knjSchoolMst, $semeMonthArray, $sp, $total, $combinedFlg, $model)
    {
        $table = ($sp == "sp") ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";
        $jisuu = ($sp == "sp") ? "ROUND(FLOAT(SUM(VALUE(TT1.MLESSON,0) * SMALLINT(TT2.MINUTES))) / ".$knjSchoolMst["JITU_JIFUN_SPECIAL"].", 0)" : "FLOAT(SUM(VALUE(TT1.MLESSON,0)))";
        $lesson = ($sp == "sp") ? "ROUND(FLOAT(SUM(VALUE(TT1.LESSON,0) * SMALLINT(TT2.MINUTES))) / ".$knjSchoolMst["JITU_JIFUN_SPECIAL"].", 0)" : "FLOAT(SUM(VALUE(TT1.LESSON,0)))";
        $strSp = ($sp == "sp") ? "_SPECIAL" : "";
        //上限値の算出方法（初期値：3.切り捨て）
        // 1.四捨五入・・・小数点第1位を四捨五入・・・ROUND(XXX, 0)
        // 2.切り上げ・・・小数点第1位を切り上げ・・・CEIL(XXX)
        // 3.切り捨て・・・小数点第1位を切り捨て・・・FLOOR(XXX)
        // 4.実数・・・・・小数点第2位を四捨五入・・・ROUND(XXX * 10, 0) / 10
        $sKeisan = "FLOOR(";
        $eKeisan = ")";
        if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "1") {
            $sKeisan = "ROUND(";
            $eKeisan = ", 0)";
        } elseif ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "2") {
            $sKeisan = "CEIL(";
            $eKeisan = ")";
        } elseif ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "3") {
            $sKeisan = "FLOOR(";
            $eKeisan = ")";
        } elseif ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "4") {
            $sKeisan = "ROUND(";
            $eKeisan = " * 10, 0) / 10";
        }

        $query  = " INSERT INTO ".$table." ";
        //テスト項目マスタの集計フラグの表
        $query .= " WITH TEST_COUNTFLG AS ( ";
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
        if ($sp == "sp" && $total == "total") {
            $query .= " SELECT ";
            $query .= "     '{$year}' AS YEAR, ";
            $query .= "     '{$div}' AS DIV, "; // 1:年間、2:随時
            $query .= "     '999' as SPECIAL_GROUP_CD, ";
            $query .= "     TT9.SCHREGNO, ";
            $query .= "     ".$sKeisan."(SUM(TT9.MLESSON) * ".$knjSchoolMst["RISYU_BUNSI".$strSp]." / ".$knjSchoolMst["RISYU_BUNBO".$strSp].")".$eKeisan." AS COMP_ABSENCE_HIGH, ";
            $query .= "     ".$sKeisan."(SUM(TT9.MLESSON) * ".$knjSchoolMst["SYUTOKU_BUNSI".$strSp]." / ".$knjSchoolMst["SYUTOKU_BUNBO".$strSp].")".$eKeisan." AS GET_ABSENCE_HIGH, ";
            $query .= "     SUM(TT9.LESSON) AS LESSON, ";
            $query .= "     DATE('{$edate}') AS APPOINTED_DATE, "; // ①年間(1)は学期マスタ９の終了日、②随時(2)は指定日
            $query .= "     '".STAFFCD."' AS REGISTERCD, ";
            $query .= "     SYSDATE() AS UPDATED ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= " SELECT ";
        $query .= "     '{$year}' AS YEAR, ";
        $query .= "     '{$div}' AS DIV, "; // 1:年間、2:随時
        if ($combinedFlg == "on" && $sp != "sp") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " TT3.COMBINED_CLASSCD, ";
                $query .= " TT3.COMBINED_SCHOOL_KIND, ";
                $query .= " TT3.COMBINED_CURRICULUM_CD, ";
            }
            $query .= "     TT3.COMBINED_SUBCLASSCD, ";
        } elseif ($sp == "sp") {
            $query .= "     TT2.SPECIAL_GROUP_CD, ";
        } else {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " TT1.CLASSCD, ";
                $query .= " TT1.SCHOOL_KIND, ";
                $query .= " TT1.CURRICULUM_CD, ";
            }
            $query .= "     TT1.SUBCLASSCD, ";
        }
        $query .= "     TT1.SCHREGNO, ";
        if ($sp == "sp" && $total == "total") {
            $query .= "     ".$jisuu." AS MLESSON, ";
        }
        $query .= "     ".$sKeisan."(".$jisuu." * ".$knjSchoolMst["RISYU_BUNSI".$strSp]." / ".$knjSchoolMst["RISYU_BUNBO".$strSp].")".$eKeisan." AS COMP_ABSENCE_HIGH, ";
        $query .= "     ".$sKeisan."(".$jisuu." * ".$knjSchoolMst["SYUTOKU_BUNSI".$strSp]." / ".$knjSchoolMst["SYUTOKU_BUNBO".$strSp].")".$eKeisan." AS GET_ABSENCE_HIGH, ";
        $query .= "     ".$lesson." AS LESSON, ";
        $query .= "     DATE('{$edate}') AS APPOINTED_DATE, "; // ①年間(1)は学期マスタ９の終了日、②随時(2)は指定日
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     ( ";
        //集計テーブル参照
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(VALUE(LESSON,0)) AS LESSON, ";
        $query .= "         SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) ";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= "     - VALUE(VIRUS,0) ";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= "     - VALUE(KOUDOME,0) ";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "     + VALUE(OFFDAYS,0) ";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .= "     + VALUE(SUSPEND,0) ";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .= "     + VALUE(MOURNING,0) ";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .= "     + VALUE(VIRUS,0) ";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .= "     + VALUE(KOUDOME,0) ";
        }
        $query .= "         ) AS MLESSON ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $semeMonthIn = "('" .implode("','", $semeMonthArray) ."')";
        $query .= "         AND SEMESTER || MONTH IN ".$semeMonthIn." ";
        $query .= "     GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO ";
        //時間割テーブル参照
        if ($div == "1" && strlen($sdate)) {
            $query .= "     UNION ALL ";
            $query .= "     SELECT ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     TBL.CLASSCD, ";
                $query .= "     TBL.SCHOOL_KIND, ";
                $query .= "     TBL.CURRICULUM_CD, ";
            }
            $query .= "         TBL.SUBCLASSCD, ";
            $query .= "         TBL.SCHREGNO, ";
            $query .= "         COUNT(*) AS LESSON, ";
            $query .= "         COUNT(*) - COUNT(S3.SCHREGNO) - COUNT(S4.SCHREGNO) - SUM(CASE WHEN S2.DI_CD IN('2','9','3','10','19','20','25','26') THEN 1 ELSE 0 END) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "     + COUNT(S3.SCHREGNO) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "     + SUM(CASE WHEN S2.DI_CD IN('2','9') THEN 1 ELSE 0 END) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "     + SUM(CASE WHEN S2.DI_CD IN('3','10') THEN 1 ELSE 0 END) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "     + SUM(CASE WHEN S2.DI_CD IN('19','20') THEN 1 ELSE 0 END) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "     + SUM(CASE WHEN S2.DI_CD IN('25','26') THEN 1 ELSE 0 END) ";
            }
            $query .= "         AS MLESSON ";
            $query .= "       FROM ";
            $query .= "         ( ";
            $query .= "         SELECT ";
            $query .= "             T2.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         T4.CLASSCD, ";
                $query .= "         T4.SCHOOL_KIND, ";
                $query .= "         T4.CURRICULUM_CD, ";
            }
            $query .= "             T4.SUBCLASSCD, ";
            $query .= "             T1.EXECUTEDATE, ";
            $query .= "             T1.PERIODCD ";
            $query .= "           FROM SCH_CHR_DAT T1 ";
            $query .= "                LEFT JOIN CHAIR_STD_DAT T2 ";
            $query .= "                     ON  T1.CHAIRCD  = T2.CHAIRCD ";
            $query .= "                     AND T1.YEAR     = T2.YEAR ";
            $query .= "                     AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "                     AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
            $query .= "                LEFT JOIN SCHREG_REGD_DAT T3 ";
            $query .= "                     ON  T3.SCHREGNO = T2.SCHREGNO ";
            $query .= "                     AND T3.YEAR     = T2.YEAR ";
            $query .= "                     AND T3.SEMESTER = T2.SEMESTER ";
            $query .= "                LEFT JOIN CHAIR_DAT T4 ";
            $query .= "                     ON  T4.YEAR     = T2.YEAR ";
            $query .= "                     AND T4.SEMESTER = T2.SEMESTER ";
            $query .= "                     AND T4.CHAIRCD  = T2.CHAIRCD ";
            $query .= "          WHERE T1.YEAR     = '{$year}' ";
            $query .= "            AND T1.EXECUTEDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
            $query .= "            AND T2.SCHREGNO IS NOT NULL ";
            $query .= "            AND T4.SUBCLASSCD IS NOT NULL ";
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCH_CHR_COUNTFLG E1 ";
            $query .= "                            WHERE E1.EXECUTEDATE   = T1.EXECUTEDATE AND ";
            $query .= "                                  E1.PERIODCD      = T1.PERIODCD AND ";
            $query .= "                                  E1.CHAIRCD       = T1.CHAIRCD AND ";
            $query .= "                                  E1.GRADE         = T3.GRADE AND ";
            $query .= "                                  E1.HR_CLASS      = T3.HR_CLASS AND ";
            $query .= "                                  T1.DATADIV       IN ('0','1','3') AND "; //テスト(DATADIV=2)以外
            $query .= "                                  E1.COUNTFLG      = '0') ";
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
            $query .= "                            WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
            $query .= "                              AND TEST.PERIODCD    = T1.PERIODCD ";
            $query .= "                              AND TEST.CHAIRCD     = T1.CHAIRCD ";
            $query .= "                              AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_ENT_GRD_HIST_DAT E2 ";
            $query .= "                                             INNER JOIN ( ";
            $query .= "                                                 SELECT ";
            $query .= "                                                     SCHREGNO, ";
            $query .= "                                                     MAX(YEAR) AS YEAR, ";
            $query .= "                                                     MAX(GRADE) AS GRADE ";
            $query .= "                                                 FROM ";
            $query .= "                                                     SCHREG_REGD_DAT ";
            $query .= "                                                 WHERE ";
            $query .= "                                                     YEAR = '{$year}' ";
            $query .= "                                                 GROUP BY ";
            $query .= "                                                     SCHREGNO ";
            $query .= "                                                 ) R6 ";
            $query .= "                                                 ON  R6.SCHREGNO = E2.SCHREGNO ";
            $query .= "                                                 AND R6.YEAR     = '{$year}' ";
            $query .= "                                             INNER JOIN SCHREG_REGD_GDAT G6 ";
            $query .= "                                                 ON  G6.YEAR         = R6.YEAR ";
            $query .= "                                                 AND G6.GRADE        = R6.GRADE ";
            $query .= "                                                 AND G6.SCHOOL_KIND  = E2.SCHOOL_KIND ";
            $query .= "                            WHERE E2.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "                                  ((E2.GRD_DIV IN('1','2','3','6','7') AND E2.GRD_DATE < T1.EXECUTEDATE) OR ";
            $query .= "                                   (E2.ENT_DIV IN('1','2','3','4','5')     AND E2.ENT_DATE > T1.EXECUTEDATE))) ";
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
            $query .= "                             LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
            $query .= "                            WHERE L4.SCHREGNO = T2.SCHREGNO ";
            $query .= "                              AND L4.ATTENDDATE = T1.EXECUTEDATE ";
            $query .= "                              AND L4.PERIODCD = T1.PERIODCD ";
            $query .= "                              AND ATT_DI.REP_DI_CD = '27') "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
            $query .= "                             LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
            $query .= "                            WHERE L4.SCHREGNO = T2.SCHREGNO ";
            $query .= "                              AND L4.ATTENDDATE = T1.EXECUTEDATE ";
            $query .= "                              AND L4.PERIODCD = T1.PERIODCD ";
            $query .= "                              AND ATT_DI.REP_DI_CD = '28') "; // 勤怠コード'28'は時間割にカウントしない
            $query .= "         GROUP BY ";
            $query .= "             T2.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         T4.CLASSCD, ";
                $query .= "         T4.SCHOOL_KIND, ";
                $query .= "         T4.CURRICULUM_CD, ";
            }
            $query .= "             T4.SUBCLASSCD, ";
            $query .= "             T1.EXECUTEDATE, ";
            $query .= "             T1.PERIODCD ";
            $query .= "         ) TBL ";
            $query .= "         LEFT JOIN ( ";
            $query .= "         SELECT T1.SCHREGNO,T1.ATTENDDATE,T1.PERIODCD, ";
            $query .= "                CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END AS DI_CD ";
            $query .= "           FROM ATTEND_DAT T1 ";
            $query .= "                LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = T1.YEAR AND ATT_DI.DI_CD = T1.DI_CD ";
            $query .= "          WHERE CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END IN('2','9','3','10','19','20','25','26') ";
            $query .= "            AND T1.ATTENDDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT E3 ";
            $query .= "                            WHERE E3.SCHREGNO = T1.SCHREGNO AND ";
            $query .= "                                  E3.TRANSFERCD IN('1','2') AND ";
            $query .= "                                  T1.ATTENDDATE BETWEEN E3.TRANSFER_SDATE AND E3.TRANSFER_EDATE) ";
            $query .= "         ) S2 ON  S2.SCHREGNO   = TBL.SCHREGNO ";
            $query .= "              AND S2.ATTENDDATE = TBL.EXECUTEDATE ";
            $query .= "              AND S2.PERIODCD   = TBL.PERIODCD ";
            //休学(OFFDAYS)
            $query .= "         LEFT JOIN SCHREG_TRANSFER_DAT S3 ";
            $query .= "                      ON S3.SCHREGNO = TBL.SCHREGNO ";
            $query .= "                     AND S3.TRANSFERCD = '2' ";
            $query .= "                     AND TBL.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE ";
            //留学(ABROAD)
            $query .= "         LEFT JOIN SCHREG_TRANSFER_DAT S4 ";
            $query .= "                      ON S4.SCHREGNO = TBL.SCHREGNO ";
            $query .= "                     AND S4.TRANSFERCD = '1' ";
            $query .= "                     AND TBL.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE ";
            $query .= "     GROUP BY ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     TBL.CLASSCD, ";
                $query .= "     TBL.SCHOOL_KIND, ";
                $query .= "     TBL.CURRICULUM_CD, ";
            }
            $query .= "         TBL.SUBCLASSCD, ";
            $query .= "         TBL.SCHREGNO ";
        }
        $query .= "     ) TT1 ";
        if ($sp == "sp") {
            $query .= "     INNER JOIN ATTEND_SUBCLASS_SPECIAL_DAT TT2 ON TT2.YEAR = '{$year}' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                           AND TT2.CLASSCD = TT1.CLASSCD ";
                $query .= "                                           AND TT2.SCHOOL_KIND = TT1.SCHOOL_KIND ";
                $query .= "                                           AND TT2.CURRICULUM_CD = TT1.CURRICULUM_CD ";
            }
            $query .= "                                               AND TT2.SUBCLASSCD = TT1.SUBCLASSCD ";
        }
        if ($combinedFlg == "on" && $sp != "sp") {
            $query .= "     INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT TT3 ON TT3.YEAR = '{$year}' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                             AND TT3.ATTEND_CLASSCD = TT1.CLASSCD ";
                $query .= "                                             AND TT3.ATTEND_SCHOOL_KIND = TT1.SCHOOL_KIND ";
                $query .= "                                             AND TT3.ATTEND_CURRICULUM_CD = TT1.CURRICULUM_CD ";
            }
            $query .= "                                                 AND TT3.ATTEND_SUBCLASSCD = TT1.SUBCLASSCD ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE TT1.SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, "").") ";
        }
        $query .= " GROUP BY ";
        if ($combinedFlg == "on" && $sp != "sp") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " TT3.COMBINED_CLASSCD, ";
                $query .= " TT3.COMBINED_SCHOOL_KIND, ";
                $query .= " TT3.COMBINED_CURRICULUM_CD, ";
            }
            $query .= "     TT3.COMBINED_SUBCLASSCD, ";
        } elseif ($sp == "sp") {
            $query .= "     TT2.SPECIAL_GROUP_CD, ";
        } else {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " TT1.CLASSCD, ";
                $query .= " TT1.SCHOOL_KIND, ";
                $query .= " TT1.CURRICULUM_CD, ";
            }
            $query .= "     TT1.SUBCLASSCD, ";
        }
        $query .= "     TT1.SCHREGNO ";
        if ($sp == "sp" && $total == "total") {
            $query .= "     ) TT9 ";
            $query .= " GROUP BY TT9.SCHREGNO ";
        }
        return $query;
    }

    public function deleteAppointedDayQuery($year, $semester, $month, $school_kind)
    {
        $query  = " DELETE FROM APPOINTED_DAY_MST ";
        $query .= "  WHERE YEAR     = '$year' ";
        if ($school_kind) {
            $query .= "    AND SCHOOL_KIND = '".$school_kind."' ";
        }
        $query .= "    AND MONTH    = '$month' ";
        $query .= "    AND SEMESTER = '$semester' ";
        return $query;
    }

    public function insertAppointedDayQuery($year, $semester, $month, $day, $school_kind)
    {
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
            //月別
            $query = knjd110bQuery::deleteQuery("attend_semes_dat", $year, $semester, $month, $model);
            $db->query($query);
            //科目別
            $query = knjd110bQuery::deleteQuery("attend_subclass_dat", $year, $semester, $month, $model);
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
            $query .= "   AND SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, $semester).") ";
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
    public function &getTermGet(&$model, $db, $year, $month)
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

    public function insertSemesterQuery($year, $semester, $month, $sdate, $edate, $day, $knjSchoolMst, $model) //学期別集計
    {
        /* 時間割データとの整合性を無視してattend_dat にあるレコードをそのまま集計 */
        /* chair_std_datまたはsch_chr_datにない講座の出欠データがattend_datにあった場合等はおかしくなります */
        /* 時間割データと整合性がとれた出欠データのみを集計 */
        /* 時間割データに登録されていない講座の出欠データがあっても無視します */
        /* 2005/01/12 各生徒が所属する課程の開始校時から終了校時までの範囲を集計する */
        /* 2005/02/02 遅刻、早退回数は課程の開始校時から終了校時までの範囲を指定しないで集計する */

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
        if ($model->Properties["useKekkaJisu"] == 'true') {
            $query .= "  ,KEKKA_JISU ";
        }
        if ($model->Properties["useKekka"] == 'true') {
            $query .= "  ,KEKKA ";
        }
        if ($model->Properties["useLatedetail"] == 'true') {
            $query .= "  ,LATEDETAIL ";
        }
        if ($model->Properties["useVirus"] == 'true') {
            $query .= "  ,VIRUS ";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= "  ,KOUDOME ";
        }
        $query .= " ,registercd ";
        $query .= " ,updated) ";
        // chair_std_datまたはsch_chr_datの表
        $query .= " with t_sch_std as ( ";
        $query .= "     SELECT ";
        $query .= "         T2.schregno , ";
        $query .= "         T1.executedate, ";
        $query .= "         T1.periodcd, ";
        $query .= "         T1.chaircd ";
        $query .= "     FROM ";
        $query .= "         sch_chr_dat T1 ";
        $query .= "         left join chair_std_dat T2 on  ";
        $query .= "         T1.executedate BETWEEN T2.appdate AND ";
        $query .= "         T2.appenddate AND ";
        $query .= "         T1.year = T2.year AND ";
        $query .= "         T1.semester = T2.semester AND ";
        $query .= "         T1.chaircd = T2.chaircd ";
        $query .= "     WHERE ";
        $query .= "         T1.year = '".$year."' AND ";
        $query .= "         T1.semester = '".$semester."' AND ";
        $query .= "         T1.executedate BETWEEN DATE('".$sdate."') AND ";
        $query .= "         DATE('".$edate."') ";
        $query .= "     GROUP BY ";
        $query .= "         T2.schregno, ";
        $query .= "         T1.executedate, ";
        $query .= "         T1.periodcd, ";
        $query .= "         T1.chaircd ";
        $query .= "     ) ";
        /* 時間割データに登録されている講座のみの出欠データ */
        $query .= ", T_attend_dat (schregno, attenddate, periodcd, chaircd, YOMIKAEMAE, di_cd) ";
        $query .= "               AS (SELECT T0.schregno, T0.attenddate, T0.periodcd, T0.chaircd, ";
        $query .= "                          ATT_DI.REP_DI_CD AS YOMIKAEMAE, ";
        $query .= "                          CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END AS DI_CD ";
        $query .= "                     FROM attend_dat T0 ";
        $query .= "                          LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = T0.YEAR AND ATT_DI.DI_CD = T0.DI_CD , ";
        $query .= "                        (SELECT T1.schregno ";
        $query .= "                               ,T1.executedate,T1.periodcd, T1.chaircd ";
        $query .= "                           FROM t_sch_std T1 ";
        $query .= "                          WHERE ";
        //2005.11.02ALP----------↓----------
        //不在日数を除く
        //転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
        //転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合
        $query .= "             NOT EXISTS(SELECT  'X' FROM SCHREG_ENT_GRD_HIST_DAT T6 ";
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
        //2005.11.02ALP----------↑----------
        // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '27') ";
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '28') "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '38') "; // 除外(38)は、時数、日数にカウントしない
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM CHAIR_DETAIL_DAT L4 ";
        $query .= "                                 WHERE L4.YEAR = '{$year}' ";
        $query .= "                                   AND L4.SEMESTER = '{$semester}' ";
        $query .= "                                   AND L4.CHAIRCD = T1.CHAIRCD ";
        $query .= "                                   AND L4.SEQ = '001' ";
        $query .= "                                   AND L4.REMARK2 = '1') "; // 1日出欠の対象外
        $query .= "                          ) T1 ";                                          //2005/01/04 alp modify
        $query .= "                       ,(SELECT T1.schregno, T2.s_periodcd, T2.e_periodcd ";                                                         // ↓add 2005/01/12
        $query .= "                           FROM schreg_regd_dat T1, ";
        $query .= "                               (SELECT T1.coursecd, T1.s_periodcd, T1.e_periodcd ";
        $query .= "                                  FROM course_mst T1, course_ydat T2 ";
        $query .= "                                 WHERE T1.coursecd = T2.coursecd ";
        $query .= "                                   AND T2.year = '".$year."') T2 ";
        $query .= "                          WHERE T1.coursecd = T2.coursecd  ";
        $query .= "                            AND T1.year     = '".$year."' ";
        $query .= "                            AND T1.semester = '".$semester."') T2 ";                                                                 // ↑add 2005/01/12


        $query .= "                   WHERE T0.schregno           = T1.schregno ";
        $query .= "                     AND T0.attenddate         = T1.executedate ";
        $query .= "                     AND T0.chaircd            = T1.chaircd ";
        $query .= "                     AND T0.periodcd           = T1.periodcd ";
        $query .= "                     AND T0.schregno           = T2.schregno ";                                                                      // add 2005/01/12
        $query .= "                     AND T0.periodcd BETWEEN T2.s_periodcd AND T2.e_periodcd ";                                                      // add 2005/01/12
        $query .= "                  ), ";
        /* 時間割データにある各生徒の受講すべき日と、それぞれの日の最初の校時、最後の校時、校時数 */
        $query .= "     T_period_cnt (schregno, executedate, first_period, last_period, period_cnt) ";
        $query .= "               AS (SELECT T1.schregno ";
        $query .= "                           ,T1.executedate ";
        $query .= "                           ,MIN(T1.periodcd)             AS first_period ";
        $query .= "                           ,MAX(T1.periodcd)             AS last_period ";
        $query .= "                           ,COUNT(DISTINCT T1.periodcd)  AS period_cnt ";
        $query .= "                     FROM ";
        $query .= "                         (SELECT T1.schregno ";
        $query .= "                                 ,T1.executedate,T1.periodcd, T1.chaircd ";
        $query .= "                            FROM t_sch_std T1 ";
        $query .= "                           WHERE ";
        //2005.11.02ALP----------↓----------
        //不在日数を除く
        //転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
        //転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合
        $query .= "             NOT EXISTS(SELECT  'X' FROM SCHREG_ENT_GRD_HIST_DAT T6 ";
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
        //2005.11.02ALP----------↑----------
        // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '27') ";
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '28') "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '38') "; // 除外(38)は、時数、日数にカウントしない
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM CHAIR_DETAIL_DAT L4 ";
        $query .= "                                 WHERE L4.YEAR = '{$year}' ";
        $query .= "                                   AND L4.SEMESTER = '{$semester}' ";
        $query .= "                                   AND L4.CHAIRCD = T1.CHAIRCD ";
        $query .= "                                   AND L4.SEQ = '001' ";
        $query .= "                                   AND L4.REMARK2 = '1') "; // 1日出欠の対象外
        $query .= "                           ) T1 ";
        $query .= "                         ,(SELECT T1.schregno, T2.s_periodcd, T2.e_periodcd ";
        $query .= "                             FROM schreg_regd_dat T1, ";
        $query .= "                                 (SELECT T1.coursecd, T1.s_periodcd, T1.e_periodcd ";
        $query .= "                                    FROM course_mst T1, course_ydat T2 ";
        $query .= "                                   WHERE T1.coursecd = T2.coursecd ";
        $query .= "                                     AND T2.year = '".$year."') T2 ";
        $query .= "                            WHERE T1.coursecd = T2.coursecd  ";
        $query .= "                              AND T1.year     = '".$year."' ";
        $query .= "                              AND T1.semester = '".$semester."') T3 ";
        $query .= "                    WHERE T1.schregno  = T3.schregno ";
        $query .= "                      AND T1.periodcd BETWEEN T3.s_periodcd AND T3.e_periodcd ";
        $query .= "                      GROUP BY T1.schregno, T1.executedate) ";

        // 島根県の出欠区分に合わせた1日出欠の計算を行う
        $query .= " ,T_attend_dat_di_cd AS ( ";
        $query .= "     SELECT schregno, attenddate, di_cd, MIN(periodcd) AS first_period, MAX(periodcd) AS last_period, COUNT(periodcd) AS period_cnt ";
        $query .= "       FROM T_attend_dat ";
        $query .= "      WHERE di_cd IN ('0','1','2','3','6','34','36','38')  ";
        $query .= "     GROUP BY schregno, attenddate, di_cd ) ";
        $query .= " ,T_attend_day_dat_di_cd AS ( ";
        $query .= "     SELECT T0.schregno, T0.executedate AS attenddate, T1.di_cd ";
        $query .= "       FROM T_period_cnt T0, ";
        $query .= "            T_attend_dat_di_cd T1 ";
        $query .= "      WHERE T0.schregno     = T1.schregno ";
        $query .= "        AND T0.executedate  = T1.attenddate ";
        $query .= "        AND T0.first_period = T1.first_period ";
        $query .= "        AND T0.period_cnt   = T1.period_cnt ) ";
        $query .= " ,T_attend_dat_period_cnt AS ( ";
        $query .= "     SELECT schregno, attenddate, COUNT(periodcd) AS period_cnt ";
        $query .= "       FROM T_attend_dat ";
        $query .= "      WHERE di_cd NOT IN ('0','14','15','16')  ";
        $query .= "     GROUP BY schregno, attenddate ) ";
        $query .= " ,T_attend_day_dat_di_cd_15_16_35 AS ( ";
        $query .= "     SELECT T0.schregno, T0.executedate AS attenddate, ";
        $query .= "            CASE WHEN T1.di_cd IN ('6','15') AND T2.di_cd IN ('6','16') THEN '35' ";
        $query .= "                 WHEN T1.di_cd IN ('6','15') THEN '15' ";
        $query .= "                 ELSE '16' ";
        $query .= "            END AS di_cd ";
        $query .= "       FROM T_period_cnt T0 ";
        $query .= "            LEFT JOIN T_attend_dat T1 ";
        $query .= "         ON T0.schregno     = T1.schregno ";
        $query .= "        AND T0.executedate  = T1.attenddate ";
        $query .= "        AND T0.first_period = T1.periodcd ";
        $query .= "            LEFT JOIN T_attend_dat T2 ";
        $query .= "         ON T0.schregno     = T2.schregno ";
        $query .= "        AND T0.executedate  = T2.attenddate ";
        $query .= "        AND T0.last_period  = T2.periodcd ";
        $query .= "            LEFT JOIN T_attend_dat_period_cnt T3 ";
        $query .= "         ON T0.schregno     = T3.schregno ";
        $query .= "        AND T0.executedate  = T3.attenddate ";
        $query .= "      WHERE (T1.di_cd IN ('6','15') OR T2.di_cd IN ('6','16')) ";
        $query .= "        AND T0.period_cnt   != VALUE(T3.period_cnt, 0) ) ";

        $query .= "SELECT '0' ";
        $query .= "      ,'".$year."' ";
        $query .= "      ,'".$month."' ";
        $query .= "      ,'".$semester."' ";
        $query .= "      ,TT1.schregno ";
        $query .= "      ,'".$day."'";                                                                                                                  //add 2005/01/07
        $query .= "      ,TT1.lesson ";
        $query .= "      ,VALUE(TT5.offdays,0)  AS offdays ";
        $query .= "      ,VALUE(TT5.absent,0)   AS absent ";
        $query .= "      ,VALUE(TT5.suspend,0)  AS suspend ";
        $query .= "      ,VALUE(TT5.mourning,0) AS mourning ";
        $query .= "      ,VALUE(TT5.abroad,0)   AS abroad ";
        $query .= "      ,VALUE(TT5.sick,0)     AS sick ";
        $query .= "      ,VALUE(TT5.notice,0)   AS notice ";
        $query .= "      ,VALUE(TT5.nonotice,0) AS nonotice ";
        $query .= "      ,VALUE(TT6.late,0)     AS late ";
        $query .= "      ,VALUE(TT6.early,0)    AS early ";
        if ($model->Properties["useKekkaJisu"] == 'true') {
            $query .= "  ,0 AS KEKKA_JISU ";
        }
        if ($model->Properties["useKekka"] == 'true') {
            $query .= "  ,0 AS KEKKA ";
        }
        if ($model->Properties["useLatedetail"] == 'true') {
            $query .= "  ,0 AS LATEDETAIL ";
        }
        if ($model->Properties["useVirus"] == 'true') {
            $query .= "  ,VALUE(TT5.VIRUS,0)   AS VIRUS ";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= "  ,VALUE(TT5.KOUDOME,0) AS KOUDOME ";
        }
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM  ";
        /* 授業日数 */
        $query .= "   (SELECT T2.schregno, ";
        $query .= "           COUNT(T2.executedate) AS lesson ";
        $query .= "      FROM T_period_cnt T2 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   WHERE T2.schregno IN (".knjd110bQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        $query .= "     GROUP BY T2.schregno) TT1 ";
        /* 公欠(1)出停(2)忌引(3)欠席(6)留学(34)休学(36) */
        $query .= "LEFT JOIN ";
        $query .= "   (SELECT W0.schregno, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END) AS absent, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END) AS suspend, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END) AS mourning, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS sick, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS notice, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS nonotice, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '34' THEN 1 ELSE 0 END) AS abroad, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '36' THEN 1 ELSE 0 END) AS offdays, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END) AS VIRUS, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END) AS KOUDOME ";
        $query .= "      FROM T_attend_day_dat_di_cd W0 ";
        $query .= "     GROUP BY W0.schregno ) TT5 ";
        $query .= "  ON TT1.schregno = TT5.schregno ";
        /* 遅刻(15)早退(16)遅刻早退(35) */
        $query .= "LEFT JOIN  ";
        $query .= "   (SELECT W0.schregno, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '15' THEN 1 WHEN '35' THEN 1 ELSE 0 END) AS late, ";
        $query .= "           SUM(CASE W0.DI_CD WHEN '16' THEN 1 WHEN '35' THEN 1 ELSE 0 END) AS early ";
        $query .= "      FROM T_attend_day_dat_di_cd_15_16_35 W0 ";
        $query .= "     WHERE W0.di_cd IN ('15','16','35') ";
        $query .= "     GROUP BY W0.schregno ) TT6 ";
        $query .= "  ON TT1.schregno = TT6.schregno ";

        return $query;
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
        $query .= "         AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                                     LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                                 WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                   AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND ATT_DI.REP_DI_CD = '38') "; // 除外(38)は、時数、日数にカウントしない
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
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ('36')       THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  OFFDAYS ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ( '1',  '8') THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  ABSENT ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ( '2',  '9') THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  SUSPEND ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ( '3', '10') THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  MOURNING ";
        $query .= "      ,CEIL(SUM(CASE WHEN S2.DI_CD IN ('34')       THEN S1.PERIOD_MINUTES ELSE 0 END) * 1.0 / S1.JITU_JIFUN) AS  ABROAD ";
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

    //科目別詳細
    public function insertSubclassDetailQuery($year, $semester, $month, $sdate, $edate, $model)
    {
        $query  = " INSERT INTO ATTEND_SUBCLASS_DETAIL_DAT ";

        $query .= " WITH COUNTFLG AS ( ";
        $query .= "     SELECT * FROM SCH_CHR_COUNTFLG ";
        $query .= "     WHERE  EXECUTEDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')) ";
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
        //時間割
        $query .= " ,T_sch_chr_dat AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO , ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             t2.SCHREGNO, ";
        $query .= "             t1.YEAR, ";
        $query .= "             t1.SEMESTER, ";
        $query .= "             t1.CHAIRCD, ";
        $query .= "             t1.EXECUTEDATE, ";
        $query .= "             t1.PERIODCD, ";
        $query .= "             t1.DATADIV, ";
        $query .= "             t3.GRADE, ";
        $query .= "             t3.HR_CLASS ";
        $query .= "         FROM ";
        $query .= "             SCH_CHR_DAT t1 , ";
        $query .= "             CHAIR_STD_DAT t2 , ";
        $query .= "             SCHREG_REGD_DAT t3 ";
        $query .= "         WHERE ";
        $query .= "             t1.YEAR = '{$year}' AND ";
        $query .= "             t1.SEMESTER = '{$semester}' AND ";
        $query .= "             t1.EXECUTEDATE BETWEEN DATE('{$sdate}') AND ";
        $query .= "             DATE('{$edate}') AND ";
        $query .= "             t1.CHAIRCD = t2.CHAIRCD AND ";
        $query .= "             t1.YEAR = t2.YEAR AND ";
        $query .= "             t1.SEMESTER = t2.SEMESTER AND ";
        $query .= "             t1.EXECUTEDATE BETWEEN t2.APPDATE AND ";
        $query .= "             t2.APPENDDATE AND ";
        $query .= "             t3.SCHREGNO = t2.SCHREGNO AND ";
        $query .= "             t3.YEAR = t2.YEAR AND ";
        $query .= "             t3.SEMESTER = t2.SEMESTER ";
        $query .= "         ) T1  ";
        $query .= "         INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR  ";
        $query .= "                                AND T2.SEMESTER = T1.SEMESTER  ";
        $query .= "                                AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         COUNTFLG T11 ";
        $query .= "                     WHERE ";
        $query .= "                         T11.EXECUTEDATE = T1.EXECUTEDATE AND ";
        $query .= "                         T11.PERIODCD = T1.PERIODCD AND ";
        $query .= "                         T11.CHAIRCD = T1.CHAIRCD AND ";
        $query .= "                         T11.GRADE = T1.GRADE AND ";
        $query .= "                         T11.HR_CLASS = T1.HR_CLASS AND ";
        $query .= "                         T1.DATADIV IN ('0','1','3') AND "; //テスト(DATADIV=2)以外
        $query .= "                         T11.COUNTFLG = '0' "; // 0:集計しない
        $query .= "                     ) AND ";
        $query .= "         NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                     WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                       AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                       AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                       AND TEST.DATADIV     = T1.DATADIV) AND "; //テスト(DATADIV=2)
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_ENT_GRD_HIST_DAT T6 ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_DAT R6 ";
        $query .= "                                                     ON  R6.SCHREGNO = T6.SCHREGNO ";
        $query .= "                                                     AND R6.YEAR     = '{$year}' ";
        $query .= "                                                     AND R6.SEMESTER = '{$semester}' ";
        $query .= "                                                 INNER JOIN SCHREG_REGD_GDAT G6 ";
        $query .= "                                                     ON  G6.YEAR         = R6.YEAR ";
        $query .= "                                                     AND G6.GRADE        = R6.GRADE ";
        $query .= "                                                     AND G6.SCHOOL_KIND  = T6.SCHOOL_KIND ";
        $query .= "                     WHERE ";
        $query .= "                         T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                         ((T6.GRD_DIV IN('1','2','3','6','7') AND T6.GRD_DATE < T1.EXECUTEDATE) OR  ";
        $query .= "                          (T6.ENT_DIV IN('1','2','3','4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ";
        $query .= "                     ) AND ";
        // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "         NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                     WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '27') ";
        $query .= "     AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                     WHERE L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '28') "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD ";
        $query .= "     )  ";
        //出欠
        $query .= " ,T_attend_dat AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         W1.ATTENDDATE, ";
        $query .= "         W1.PERIODCD, ";
        $query .= "         CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END AS DI_CD, ";
        $query .= "         T1.CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT W1 ";
        $query .= "         LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = W1.YEAR AND ATT_DI.DI_CD = W1.DI_CD, ";
        $query .= "         T_sch_chr_dat T1 ";
        $query .= "     WHERE ";
        $query .= "         W1.ATTENDDATE BETWEEN DATE('{$sdate}') AND ";
        $query .= "         DATE('{$edate}') AND ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_TRANSFER_DAT T7 ";
        $query .= "                     WHERE ";
        $query .= "                         T7.SCHREGNO = W1.SCHREGNO AND ";
        $query .= "                         T7.TRANSFERCD IN('1','2') AND ";
        $query .= "                         W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND ";
        $query .= "                         T7.TRANSFER_EDATE ";
        $query .= "                     ) AND ";
        $query .= "         W1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "         W1.ATTENDDATE = T1.EXECUTEDATE AND ";
        $query .= "         W1.PERIODCD = T1.PERIODCD AND ";
        $query .= "         W1.CHAIRCD = T1.CHAIRCD ";
        $query .= "     )  ";
        //欠時数など
        $query .= " ,T_ATTEND_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         '001' AS SEQ, ";
        $query .= "         SUM(T1.SICK) + SUM(T1.NOTICE) + SUM(T1.NONOTICE) + SUM(T1.NURSEOFF) AS CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             T1.ATTENDDATE, ";
        $query .= "             T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "             T1.SUBCLASSCD, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF ";
        $query .= "         FROM ";
        $query .= "             T_attend_dat T1 ";
        $query .= "         WHERE ";
        //１日欠席を除く
        $query .= "             NOT EXISTS(SELECT 'X' FROM ATTEND_ABSENCE_DAT N7 ";
        $query .= "                         WHERE N7.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                               N7.ABSENCE_DATE = T1.ATTENDDATE ) ";
        $query .= "         GROUP BY ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             T1.ATTENDDATE, ";
        $query .= "             T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "             T1.SUBCLASSCD ";
        $query .= "         ) T1  ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     )  ";

        //メイン
        $query .= " SELECT ";
        $query .= "     '0', ";
        $query .= "     '{$year}', ";
        $query .= "     '{$month}', ";
        $query .= "     '{$semester}', ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.CLASSCD, ";
            $query .= " T1.SCHOOL_KIND, ";
            $query .= " T1.CURRICULUM_CD, ";
        } else {
            $query .= " SUBSTR(T1.SUBCLASSCD,1,2) AS CLASSCD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.CNT, ";
        $query .= "     CAST(NULL AS VARCHAR(2)) AS VAL, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     T_ATTEND_SEQ T1, ";
        $query .= "     V_NAME_MST N1 ";
        $query .= " WHERE ";
        $query .= "         N1.YEAR    = '{$year}' ";
        $query .= "     AND N1.NAMECD1 = 'C003' ";
        $query .= "     AND N1.NAMECD2 = T1.SEQ ";
        //$query .= "     AND 0 < T1.CNT ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND T1.SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, $semester).") ";
        }

        return $query;
    }

    //月別詳細
    public function insertSemesterDetailQuery($year, $semester, $month, $model)
    {
        $query  = " INSERT INTO ATTEND_SEMES_DETAIL_DAT ";

        //SEQ
        //  001:朝拝欠課
        //  002:授業欠課
        //  003:朝拝遅刻
        //  004:授業遅刻
        //  005:授業早退
        $query .= " WITH T_ATTEND_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         '001' AS SEQ, ";
        $query .= "         SUM(VALUE(CNT,0)) AS CNT ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "             COPYCD   = '0' ";
        $query .= "         AND YEAR     = '{$year}' ";
        $query .= "         AND MONTH    = '{$month}' ";
        $query .= "         AND SEMESTER = '{$semester}' ";
        $query .= "         AND CLASSCD  = '92' ";
        $query .= "         AND SEQ      = '001' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         '002' AS SEQ, ";
        $query .= "         SUM(VALUE(CNT,0)) AS CNT ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "             COPYCD   = '0' ";
        $query .= "         AND YEAR     = '{$year}' ";
        $query .= "         AND MONTH    = '{$month}' ";
        $query .= "         AND SEMESTER = '{$semester}' ";
        $query .= "         AND CLASSCD  <> '92' ";
        $query .= "         AND SEQ      = '001' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         '003' AS SEQ, ";
        $query .= "         SUM(VALUE(LATE,0)) AS CNT ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "             COPYCD   = '0' ";
        $query .= "         AND YEAR     = '{$year}' ";
        $query .= "         AND MONTH    = '{$month}' ";
        $query .= "         AND SEMESTER = '{$semester}' ";
        $query .= "         AND CLASSCD  = '92' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         '004' AS SEQ, ";
        $query .= "         SUM(VALUE(LATE,0)) AS CNT ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "             COPYCD   = '0' ";
        $query .= "         AND YEAR     = '{$year}' ";
        $query .= "         AND MONTH    = '{$month}' ";
        $query .= "         AND SEMESTER = '{$semester}' ";
        $query .= "         AND CLASSCD  <> '92' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         '005' AS SEQ, ";
        $query .= "         SUM(VALUE(EARLY,0)) AS CNT ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "             COPYCD   = '0' ";
        $query .= "         AND YEAR     = '{$year}' ";
        $query .= "         AND MONTH    = '{$month}' ";
        $query .= "         AND SEMESTER = '{$semester}' ";
        $query .= "         AND CLASSCD  <> '92' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     '0' AS COPYCD, ";
        $query .= "     '{$year}' AS YEAR, ";
        $query .= "     '{$month}' AS MONTH, ";
        $query .= "     '{$semester}' AS SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.CNT, ";
        $query .= "     CAST(NULL AS VARCHAR(2)) AS VAL, ";
        $query .= "     CAST(NULL AS DECIMAL(5, 1)) AS CNT_DECIMAL, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     T_ATTEND_SEQ T1, ";
        $query .= "     V_NAME_MST N1 ";
        $query .= " WHERE ";
        $query .= "         N1.YEAR    = '{$year}' ";
        $query .= "     AND N1.NAMECD1 = 'C002' ";
        $query .= "     AND N1.NAMECD2 = T1.SEQ ";
        //$query .= "     AND 0 < T1.CNT ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND T1.SCHREGNO IN (".knjd110bQuery::getSchregnoInSql($model, $year, $semester).") ";
        }

        return $query;
    }
}
