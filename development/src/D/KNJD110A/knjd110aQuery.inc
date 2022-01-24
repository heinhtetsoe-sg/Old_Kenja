<?php

require_once('for_php7.php');


class knjd110aQuery extends Query {

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
        return "SELECT * FROM semester_mst WHERE year = '".$year."' AND semester <> '9' ORDER BY semester, sdate";
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
    function GetList($year,$model)
    {
        $query  = "SELECT CASE WHEN T1.MONTH < 4 THEN 1 ELSE 0 END AS SORT2, ";
        $query .= "       T1.YEAR, T1.SEMESTER, T1.MONTH, T1.APPOINTED_DAY, T1.UPDATED, T1.REGISTERCD, L1.STAFFNAME ";
        $query .= "  FROM ( ";
        $query .= "SELECT T1.YEAR, T1.SEMESTER, SMALLINT(T1.MONTH) AS MONTH, T1.APPOINTED_DAY, MAX(REGISTERCD) as REGISTERCD, MAX(T1.UPDATED) as UPDATED";
        $query .= "  FROM ATTEND_SEMES_DAT T1 ";
        $query .= " WHERE T1.COPYCD = '0' AND";
        $query .= "       T1.YEAR   = '".$year."' AND ";
        $query .= "       NOT EXISTS ( ";
        $query .= "           SELECT ";
        $query .= "               'X' ";
        $query .= "           FROM ";
        $query .= "               SCHREG_REGD_DETAIL D1 ";
        $query .= "           WHERE ";
        $query .= "               D1.SCHREGNO=T1.SCHREGNO AND ";
        $query .= "               D1.YEAR=T1.YEAR AND ";
        $query .= "               D1.SEMESTER=T1.SEMESTER AND ";
        $query .= "               D1.COUNTFLG='0' "; // 0:集計しない
        $query .= "           ) ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND T1.SCHREGNO IN (".knjd110aQuery::getSchregnoInSql($model, $year, "").") ";
        }
        $query .= " GROUP BY T1.YEAR, T1.SEMESTER, T1.MONTH, T1.APPOINTED_DAY";
        $query .= ") T1 ";
        $query .= " LEFT JOIN STAFF_MST L1 ON T1.REGISTERCD = L1.STAFFCD ";
        $query .= "ORDER BY T1.SEMESTER, 1, T1.MONTH ";
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

    //名称マスタ「C040」に登録されている、年度・学期・月は、実行不可とする。
    function getNameMstC040($model) {
        //DB接続
        $db = Query::dbCheckOut();
        //param[0] => 学期, param[1] => 月, param[2] => 開始月または終了月フラグ
        $param = explode("-",$model->month);
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

    function &getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        if ($model->cmd2 == "1") //出欠入力制御日付のみ更新 
        {
            $db->autoCommit(false);
            $query = knjd110aQuery::UpdateLdate($model->limit_date);
            $db->query($query);
            $db->commit();

        } else { //実行ボタン

            //選択された年度内と月以前の未実施チェック
            if ($model->cmd != "execute2") {
                foreach ($model->allMonth as $key => $val)
                {
                    if ($val == $model->month) break;

                    knjd110aQuery::getTerm_Get($db,$model->year,$val);
                    $arr = explode("-",$val);

                    //データ存在チェック
                    $query  = "SELECT COUNT(*) FROM attend_semes_dat ";
                    $query .= " WHERE year     = '".$model->year."'";
                    $query .= "   AND semester = '".$arr[0]."'";
                    $query .= "   AND month    = '".$arr[1]."'";
                    if ($model->Properties["useSchool_KindField"] == "1") {
                        $query .= "   AND SCHREGNO IN (".knjd110aQuery::getSchregnoInSql($model, $model->year, $arr[0]).") ";
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
                        $query .= "   AND SCHREGNO IN (".knjd110aQuery::getSchregnoInSql($model, $model->year, $arr[0]).") ";
                    }

                    $cur_day = $db->getOne($query);
                    $app_day = strftime("%d",strtotime($this->tmp_edate));
                    if ((int)$cur_day < (int)$app_day) {
                        $model->error_msg = "処理漏れの月があります。\\n（ ".$model->ctrl["学期名"][$arr[0]].(int)$arr[1]."月".$cur_day."日以降 ）";
                        Query::dbCheckIn($db);
                        return false;
                    } 
                }
            }

            knjd110aQuery::getTerm_Get($db,$model->year,$model->month);

            //開始日より前はエラー
            if (strlen($model->day) && (int)$model->day < (int)strftime("%d",strtotime($this->tmp_sdate))) {
                $model->setWarning("MSG901", "開始日以前の日は指定できません。\\n\\n開始日：".strftime("%Y年%m月%e日",strtotime($this->tmp_sdate)));
                return false;
            }

            //処理月コンボの最終日を終了日にセット
            if (!strlen($model->day) || (int)$model->day > (int)strftime("%d",strtotime($this->tmp_edate))) {
                $model->day = (int)strftime("%d",strtotime($this->tmp_edate));

            //指定日を終了日にセット
            } elseif (strlen($model->day) && (int)$model->day < (int)strftime("%d",strtotime($this->tmp_edate))) {
                $this->tmp_edate = strftime("%Y-%m",strtotime($this->tmp_edate))."-".$model->day;
            }

            //SCHOOL_MSTの情報を取得。
            $knjSchoolMst = knjd110aQuery::getSchoolMst($db, $model->year);

            //生徒（学籍番号）を配列にセット・・・年組番号順
            $optSchno = array();
            $result = $db->query(knjd110aQuery::getSchno($model->year, $this->tmp_semester, $model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $optSchno[] = $row["SCHREGNO"];
            }

            $db->autoCommit(false);

//echo date("H:i:s") ."<BR>";
            //月別------------------------------------------------------
            $query = knjd110aQuery::DeleteQuery("attend_semes_dat",$model->year,$this->tmp_semester,$this->tmp_month,$model);
            $db->query($query);

            $query = knjd110aQuery::InsertSemesterQuery($model->year,$this->tmp_semester,$this->tmp_month,$this->tmp_sdate,$this->tmp_edate,$model->day,$model);
            $db->query($query);

            $db->commit();

            //科目別----------------------------------------------------
            $query = knjd110aQuery::DeleteQuery("attend_subclass_dat",$model->year,$this->tmp_semester,$this->tmp_month,$model);
            $db->query($query);

            $query = knjd110aQuery::InsertSubclassQuery($model->year,$this->tmp_semester,$this->tmp_month,$this->tmp_sdate,$this->tmp_edate,$model->day, $model);
            $db->query($query);

            $db->commit();

            //月別・科目別の処理・・・生徒（学籍番号）毎に処理
            foreach ($optSchno as $key => $schno) {

                //授業の遅刻を取得し更新
                knjd110aQuery::updateLateDetail($db, $model->year, $this->tmp_semester, $this->tmp_month, $schno);

                //生徒の欠課の存在チェックおよび欠課時数を更新
                $kekkaJisuu = knjd110aQuery::getExistsKekka($db, $model->year, $this->tmp_semester, $this->tmp_month, $schno, $knjSchoolMst);
                //欠課回数を算出し更新
                if (0 < $kekkaJisuu) knjd110aQuery::updateKekka($db, $model->year, $this->tmp_semester, $this->tmp_month, $this->tmp_sdate, $this->tmp_edate, $schno, $knjSchoolMst);

            }
//echo date("H:i:s") ."<BR>";

            //コントロールマスタの更新
            $query = knjd110aQuery::UpdateLdate($model->limit_date);
            $db->query($query);

            //指定日（締め日）マスタ
            if ($model->Properties["useSchool_KindField"] == "1") {
                if ($model->school_kind != "ALL") {
                    $query = knjd110aQuery::deleteAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, $model->school_kind);
                    $db->query($query);
                    $query = knjd110aQuery::insertAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, $model->day, $model->school_kind); 
                    $db->query($query);
                } else {
                    foreach ($model->allSchoolKind as $key => $school_kind) {
                        $query = knjd110aQuery::deleteAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, $school_kind);
                        $db->query($query);
                        $query = knjd110aQuery::insertAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, $model->day, $school_kind); 
                        $db->query($query);
                    }
                }
            } else {
                $query = knjd110aQuery::deleteAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, "");
                $db->query($query);
                $query = knjd110aQuery::insertAppointedDayQuery($model->year, $this->tmp_semester, $this->tmp_month, $model->day, ""); 
                $db->query($query);
            }

            $db->commit();

/***
echo "JUGYOU_JISU_FLG=" .$knjSchoolMst["JUGYOU_JISU_FLG"] ."<BR>";
echo "RISYU_BUNSI=" .$knjSchoolMst["RISYU_BUNSI"] ."<BR>";
echo "RISYU_BUNBO=" .$knjSchoolMst["RISYU_BUNBO"] ."<BR>";
echo "SYUTOKU_BUNSI=" .$knjSchoolMst["SYUTOKU_BUNSI"] ."<BR>";
echo "SYUTOKU_BUNBO=" .$knjSchoolMst["SYUTOKU_BUNBO"] ."<BR>";
***/
            //学期マスタの情報を取得。
            $knjSemesterMst = knjd110aQuery::getSemesterMst($db, $model->year, "9");

//echo date("H:i:s") ."<BR>";
            //欠課数上限値（実授業数）
            if ($knjSchoolMst["JUGYOU_JISU_FLG"] == "2") {
                //実行月の指定日（締め日）の翌日を取得
                $nextSdate = knjd110aQuery::getNextSdate($db, $this->tmp_edate, $knjSemesterMst["EDATE"]);
                //「学期+月」の配列
                $semeMonthArray = array();
                foreach ($model->allMonth as $key => $val) {
                    $param = explode("-",$val);
                    $semeMonthArray[] = $param[0] .$param[1];
                    if ($val == $model->month) break;
                }
                //集計テーブル参照
                //if (0 < get_count($semeMonthArray)) echo "semeMonthIn = " ."('" .implode("','", $semeMonthArray) ."')" ."<BR>";
                //時間割テーブル参照
                //if (strlen($nextSdate)) echo "nextSdate = " .$nextSdate ."<BR>";

                //1:年間 学期マスタ９の終了日まで
                $query = knjd110aQuery::deleteAbsenceHigh($model->year, "1", $model);
                $db->query($query);
                $query = knjd110aQuery::insertAbsenceHigh($model->year, "1", $nextSdate, $knjSemesterMst["EDATE"], $knjSchoolMst, $semeMonthArray, $model);
                $db->query($query);
                //2:随時 指定日まで
                $query = knjd110aQuery::deleteAbsenceHigh($model->year, "2", $model);
                $db->query($query);
                $query = knjd110aQuery::insertAbsenceHigh($model->year, "2", "", $this->tmp_edate, $knjSchoolMst, $semeMonthArray, $model);
                $db->query($query);
                //コミット
                $db->commit();
            }
//echo date("H:i:s") ."<BR>";

            //更新データ件数チェック
            $query  = "SELECT COUNT(*) FROM attend_semes_dat ";
            $query .= " WHERE year     = '".$model->year."'";
            $query .= "   AND semester = '".$this->tmp_semester."'";
            $query .= "   AND month    = '".$this->tmp_month."'";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "   AND SCHREGNO IN (".knjd110aQuery::getSchregnoInSql($model, $model->year, $this->tmp_semester).") ";
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

    //学校マスタの情報を取得。
    function getSchoolMst($db, $year) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SUB_OFFDAYS, ";
        $query .= "     SUB_ABSENT, ";
        $query .= "     SUB_SUSPEND, ";
        $query .= "     SUB_MOURNING, ";
        $query .= "     SUB_VIRUS, ";
        $query .= "     JUGYOU_JISU_FLG, ";
        $query .= "     RISYU_BUNSI, ";
        $query .= "     RISYU_BUNBO, ";
        $query .= "     SYUTOKU_BUNSI, ";
        $query .= "     SYUTOKU_BUNBO ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $rtnRow;
    }

    //学期マスタの情報を取得。
    function getSemesterMst($db, $year, $semester) {
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

    function getAppointedDate($year, $div, $model) {
        $query  = " SELECT MAX(APPOINTED_DATE) as APPOINTED_DATE, MAX(UPDATED) as UPDATED ";
        $query .= "   FROM SCHREG_ABSENCE_HIGH_DAT ";
        $query .= "  WHERE YEAR = '{$year}' ";
        $query .= "    AND DIV  = '{$div}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110aQuery::getSchregnoInSql($model, $year, "").") ";
        }
        return $query;
    }

    //実行月の指定日（締め日）の翌日を取得
    function getNextSdate($db, $edate, $edate9) {
        //実行月の指定日（締め日）の翌日
        $sdate = $db->getOne("VALUES ADD_DAYS(DATE('{$edate}'), 1)");

        //学期マスタ９の終了日を超えたらブランク。
        $nextSdate = ($edate9 < $sdate) ? "" : $sdate;

        return $nextSdate;
    }

    function deleteAbsenceHigh($year, $div, $model) {
        $query  = " DELETE FROM SCHREG_ABSENCE_HIGH_DAT ";
        $query .= "  WHERE YEAR = '{$year}' ";
        $query .= "    AND DIV  = '{$div}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110aQuery::getSchregnoInSql($model, $year, "").") ";
        }
        return $query;
    }

    //欠課数上限値
    function insertAbsenceHigh($year, $div, $sdate, $edate, $knjSchoolMst, $semeMonthArray, $model) {
        $query  = " INSERT INTO SCHREG_ABSENCE_HIGH_DAT ";
        //テスト項目マスタの集計フラグの表
        $query .= " WITH TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        $query .= "         AND T2.TESTKINDCD IN ('01','02') ";
        $query .= "     ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "     '{$year}' AS YEAR, ";
        $query .= "     '{$div}' AS DIV, "; // 1:年間、2:随時
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " TT1.CLASSCD, ";
            $query .= " TT1.SCHOOL_KIND, ";
            $query .= " TT1.CURRICULUM_CD, ";
        }
        $query .= "     TT1.SUBCLASSCD, ";
        $query .= "     TT1.SCHREGNO, ";
        $query .= "     CEIL(FLOAT(SUM(VALUE(TT1.LESSON,0))) * ".$knjSchoolMst["RISYU_BUNSI"]." / ".$knjSchoolMst["RISYU_BUNBO"].") AS COMP_ABSENCE_HIGH, ";
        $query .= "     CEIL(FLOAT(SUM(VALUE(TT1.LESSON,0))) * ".$knjSchoolMst["SYUTOKU_BUNSI"]." / ".$knjSchoolMst["SYUTOKU_BUNBO"].") AS GET_ABSENCE_HIGH, ";
        $query .= "     0 AS LESSON, "; // TODO:
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
        $query .= "         SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0)) AS LESSON ";
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
            $query .= "         COUNT(*) AS LESSON ";
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
            $query .= "               ,CHAIR_STD_DAT T2 ";
            $query .= "               ,SCHREG_REGD_DAT T3 ";
            $query .= "               ,CHAIR_DAT T4 ";
            $query .= "          WHERE T1.YEAR     = '{$year}' ";
            $query .= "            AND T1.EXECUTEDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
            $query .= "            AND T1.CHAIRCD  = T2.CHAIRCD ";
            $query .= "            AND T1.YEAR     = T2.YEAR ";
            $query .= "            AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "            AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
            $query .= "            AND T3.SCHREGNO = T2.SCHREGNO ";
            $query .= "            AND T3.YEAR     = T2.YEAR ";
            $query .= "            AND T3.SEMESTER = T2.SEMESTER ";
            $query .= "            AND T4.YEAR     = T2.YEAR ";
            $query .= "            AND T4.SEMESTER = T2.SEMESTER ";
            $query .= "            AND T4.CHAIRCD  = T2.CHAIRCD ";
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
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST E2 ";
            $query .= "                            WHERE E2.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "                                  ((E2.GRD_DIV IN('1','2','3') AND E2.GRD_DATE < T1.EXECUTEDATE) OR ";
            $query .= "                                   (E2.ENT_DIV IN('4','5')     AND E2.ENT_DATE > T1.EXECUTEDATE))) ";
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT E3 ";
            $query .= "                            WHERE E3.SCHREGNO = T2.SCHREGNO AND ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "                              E3.TRANSFERCD IN('1') AND ";
            } else {
                $query .= "                              E3.TRANSFERCD IN('1','2') AND ";
            }
            $query .= "                                  T1.EXECUTEDATE BETWEEN E3.TRANSFER_SDATE AND E3.TRANSFER_EDATE) ";
	        $query .= "            AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
	        $query .= "                   WHERE ";
	        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
	        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
	        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
	        $query .= "                       AND L4.DI_CD = '27') ";
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM ATTEND_DAT L4 ";
            $query .= "                   WHERE ";
            $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
            $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
            $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
            $query .= "                       AND L4.DI_CD = '28') ";
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
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE TT1.SCHREGNO IN (".knjd110aQuery::getSchregnoInSql($model, $year, "").") ";
        }
        $query .= " GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " TT1.CLASSCD, ";
            $query .= " TT1.SCHOOL_KIND, ";
            $query .= " TT1.CURRICULUM_CD, ";
        }
        $query .= "     TT1.SUBCLASSCD, ";
        $query .= "     TT1.SCHREGNO ";
        return $query;
    }

    //生徒の欠課の存在チェックおよび欠課時数を更新
    function getExistsKekka(&$db, $year, $semester, $month, $schno, $knjSchoolMst) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     value(sum(SICK),0) + value(sum(NOTICE),0) + value(sum(NONOTICE),0) + value(sum(NURSEOFF),0) ";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "      + value(sum(OFFDAYS),0) ";
        }
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .= "      + value(sum(ABSENT),0) ";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .= "      + value(sum(SUSPEND),0) ";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .= "      + value(sum(MOURNING),0) ";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .= "      + value(sum(VIRUS),0) ";
        }
        $query .= "      as KEKKA_JISUU ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     COPYCD='0' AND ";
        $query .= "     YEAR='$year' AND ";
        $query .= "     MONTH='$month' AND ";
        $query .= "     SEMESTER='$semester' AND ";
        $query .= "     SCHREGNO='$schno' ";
        $kekkaJisuu = $db->getOne($query);
        //更新
        $data = array();
        $data["KEKKA_JISU"][NUMBER] = $kekkaJisuu;
        $where  = " WHERE ";
        $where .= "     COPYCD='0' AND ";
        $where .= "     YEAR='$year' AND ";
        $where .= "     MONTH='$month' AND ";
        $where .= "     SEMESTER='$semester' AND ";
        $where .= "     SCHREGNO='$schno' ";
        $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
        $db->query($query);
        return $kekkaJisuu;
    }

    function updateKekka(&$db, $year, $semester, $month, $sdate, $edate, $schno, $knjSchoolMst) {
        //欠課時数を取得
        $optDay = array();
        $optKekka = array();
        $query = knjd110aQuery::getKekka($year, $semester, $sdate, $edate, $schno, $knjSchoolMst);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $optDay[$row["DAY"]] = $row["EXECUTEDATE"];
            $optKekka[$row["DAY"]][$row["PERIODCD"]] = $row["KEKKA"];
        }
        //欠課回数を算出
        $totalCntKekka = 0;
        foreach ($optDay as $day => $executedate) {
            //echo $day ."  " .$executedate ."<BR>";
            $zeroFlg = true;
            $cntKekka = 0;
            $preKekka = -1;
            foreach ($optKekka[$day] as $periodcd => $kekka) {
                if ($kekka == 0) $zeroFlg = false;
                if ($kekka == 1 && $preKekka != 1) $cntKekka++; //欠課の数。ただし、連続は1つと数える。
                //echo $executedate ."  " .$periodcd ."  " .$kekka ."  " .$cntKekka ."  " .$preKekka ."<BR>";
                $preKekka = $kekka; //連続の判断用
            }
            if ($zeroFlg) $cntKekka = 0; //1日欠席は、欠課回数：ゼロ。
            $totalCntKekka += $cntKekka; //１日の欠課回数を足していく。
            //echo $executedate ."  " .$totalCntKekka ."  " .$zeroFlg ."<BR>";
        }
        //更新
        $data = array();
        $data["KEKKA"][NUMBER] = $totalCntKekka;
        $where  = " WHERE ";
        $where .= "     COPYCD='0' AND ";
        $where .= "     YEAR='$year' AND ";
        $where .= "     MONTH='$month' AND ";
        $where .= "     SEMESTER='$semester' AND ";
        $where .= "     SCHREGNO='$schno' ";
        $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
        $db->query($query);
    }

    //生徒の時間割と欠課を取得・・・欠課回数を算出するため。
    function getKekka($year, $semester, $sdate, $edate, $schno, $knjSchoolMst) {
        $query  = "";
        $query .= " WITH COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_COUNTFLG ";
        $query .= "     WHERE ";
        $query .= "         EXECUTEDATE BETWEEN DATE('$sdate') AND ";
        $query .= "         DATE('$edate') ";
        $query .= "     )  ";
        //テスト項目マスタの集計フラグの表
        $query .= " , TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        $query .= "         AND T2.TESTKINDCD IN ('01','02') ";
        $query .= "     ) ";
        $query .= " ,T_attend_dat AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         W1.ATTENDDATE, ";
        $query .= "         W1.PERIODCD, ";
        $query .= "         W1.DI_CD ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT W1 ";
        $query .= "     WHERE ";
        $query .= "         W1.ATTENDDATE BETWEEN DATE('$sdate') AND ";
        $query .= "         DATE('$edate') AND ";
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
        $query .= "         W1.SCHREGNO = '$schno' ";
        $query .= "     )  ";
        $query .= " ,T_sch_chr_dat AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO , ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD ";
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
        $query .= "             t1.YEAR = '$year' AND ";
        $query .= "             t1.SEMESTER = '$semester' AND ";
        $query .= "             t1.EXECUTEDATE BETWEEN DATE('$sdate') AND ";
        $query .= "             DATE('$edate') AND ";
        $query .= "             t1.CHAIRCD = t2.CHAIRCD AND ";
        $query .= "             t1.YEAR = t2.YEAR AND ";
        $query .= "             t1.SEMESTER = t2.SEMESTER AND ";
        $query .= "             t1.EXECUTEDATE BETWEEN t2.APPDATE AND ";
        $query .= "             t2.APPENDDATE AND ";
        $query .= "             t3.SCHREGNO = t2.SCHREGNO AND ";
        $query .= "             t3.YEAR = t2.YEAR AND ";
        $query .= "             t3.SEMESTER = t2.SEMESTER AND ";
        $query .= "             t3.SCHREGNO = '$schno' ";
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
        $query .= "                         T11.COUNTFLG = '0' ";
        $query .= "                     ) AND ";
        $query .= "         NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                     WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                       AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                       AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                       AND TEST.DATADIV     = T1.DATADIV) AND "; //テスト(DATADIV=2)
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_BASE_MST T6 ";
        $query .= "                     WHERE ";
        $query .= "                         T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                         ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR  ";
        $query .= "                          (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ";
        $query .= "                     ) ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD ";
        $query .= "     )  ";

        $query .= " SELECT ";
        $query .= "     S1.SCHREGNO , ";
        $query .= "     day(S1.EXECUTEDATE) as DAY, ";
        $query .= "     S1.EXECUTEDATE, ";
        $query .= "     S1.PERIODCD, ";
        $query .= "     CASE WHEN S2.DI_CD IN ('4','11') THEN 1 ";
        $query .= "          WHEN S2.DI_CD IN ('5','12') THEN 1 ";
        $query .= "          WHEN S2.DI_CD IN ('6','13') THEN 1 ";
        $query .= "          WHEN S2.DI_CD IN ('14') THEN 1 ";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "      WHEN S3.SCHREGNO IS NOT NULL THEN 1 ";
        }
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .= "      WHEN S2.DI_CD IN ('1','8') THEN 1 ";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .= "      WHEN S2.DI_CD IN ('2','9') THEN 1 ";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .= "      WHEN S2.DI_CD IN ('3','10') THEN 1 ";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .= "      WHEN S2.DI_CD IN ('19','20') THEN 1 ";
        }
        $query .= "          ELSE 0 END AS KEKKA ";
        $query .= "  FROM  ";
        $query .= "     T_sch_chr_dat S1  ";
        $query .= "     LEFT JOIN T_attend_dat S2 ON S2.SCHREGNO = S1.SCHREGNO ";
        $query .= "                              AND S2.ATTENDDATE = S1.EXECUTEDATE ";
        $query .= "                              AND S2.PERIODCD = S1.PERIODCD  ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S3 ON S3.SCHREGNO = S1.SCHREGNO ";
        $query .= "                                     AND S3.TRANSFERCD = '2' ";
        $query .= "                                     AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE  ";
        $query .= " ORDER BY  ";
        $query .= "     S1.SCHREGNO,  ";
        $query .= "     S1.EXECUTEDATE,  ";
        $query .= "     S1.PERIODCD ";
        return $query;
    }

    //授業の遅刻を取得し更新
    function updateLateDetail(&$db, $year, $semester, $month, $schno) {
        //取得
        $query  = "";
        $query .= " SELECT ";
        $query .= "     value(sum(LATE),0) as LATEDETAIL ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     COPYCD='0' AND ";
        $query .= "     YEAR='$year' AND ";
        $query .= "     MONTH='$month' AND ";
        $query .= "     SEMESTER='$semester' AND ";
        $query .= "     SCHREGNO='$schno' AND ";
        $query .= "     0 < LATE ";
        $lateDetail = $db->getOne($query);
        //更新
        $data = array();
        $data["LATEDETAIL"][NUMBER] = $lateDetail;
        $where  = " WHERE ";
        $where .= "     COPYCD='0' AND ";
        $where .= "     YEAR='$year' AND ";
        $where .= "     MONTH='$month' AND ";
        $where .= "     SEMESTER='$semester' AND ";
        $where .= "     SCHREGNO='$schno' ";
        $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
        $db->query($query);
    }

    //学籍番号を取得・・・集計しない生徒は対象外。
    function getSchno($year, $semester, $model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T0 ON T0.SCHREGNO=T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='$year' AND ";
        $query .= "     T1.SEMESTER='$semester' AND ";
//        $query .= "     T1.GRADE='01' AND ";
//        $query .= "     T1.HR_CLASS='J04' AND ";
//        $query .= "     T1.SCHREGNO='20055197' AND ";
        $query .= "     NOT EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DETAIL D1 ";
        $query .= "         WHERE ";
        $query .= "             D1.SCHREGNO=T1.SCHREGNO AND ";
        $query .= "             D1.YEAR=T1.YEAR AND ";
        $query .= "             D1.SEMESTER=T1.SEMESTER AND ";
        $query .= "             D1.COUNTFLG='0' "; // 0:集計しない
        $query .= "         ) ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND T1.SCHREGNO IN (".knjd110aQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        return $query;
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
        $query  = "DELETE FROM ".$table." T1";
        $query .= " WHERE T1.year     = '".$year."'";
        $query .= "   AND T1.semester = '".$semester."'";
        $query .= "   AND T1.month    = '".$month."'";
        $query .= "   AND T1.copycd   = '0'";
        $query .= "   AND NOT EXISTS ( ";
        $query .= "           SELECT ";
        $query .= "               'X' ";
        $query .= "           FROM ";
        $query .= "               SCHREG_REGD_DETAIL D1 ";
        $query .= "           WHERE ";
        $query .= "               D1.SCHREGNO=T1.SCHREGNO AND ";
        $query .= "               D1.YEAR='".$year."' AND ";
        $query .= "               D1.SEMESTER='".$semester."' AND ";
        $query .= "               D1.COUNTFLG='0' "; // 0:集計しない
        $query .= "           ) ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND T1.SCHREGNO IN (".knjd110aQuery::getSchregnoInSql($model, $year, $semester).") ";
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

        if ($parameter[2] == "0")
        {
            $this->tmp_sdate    = $year."-".$parameter[1]."-01";
            $this->tmp_edate    = $db->getOne("VALUES LAST_DAY(DATE('".$year."-".$parameter[1]."-01'))");

        } elseif ($parameter[2] == "1") {
            
            $this->tmp_sdate = $sdate;
            $this->tmp_edate = $db->getOne("VALUES LAST_DAY(DATE('".$year."-".$parameter[1]."-01'))");
        
        } elseif ($parameter[2] == "2") {
        
            $this->tmp_sdate = $year."-".$parameter[1]."-01";
            $this->tmp_edate = $edate;
        }
    }

    function InsertSemesterQuery($year,$semester,$month,$sdate,$edate,$day,$model) //学期別集計
    {   
        /* 時間割データとの整合性を無視してattend_dat にあるレコードをそのまま集計 */
        /* chair_std_datまたはsch_chr_datにない講座の出欠データがattend_datにあった場合等はおかしくなります */
        /* 時間割データと整合性がとれた出欠データのみを集計 */
        /* 時間割データに登録されていない講座の出欠データがあっても無視します */
        /* 各生徒が所属する課程の開始校時から終了校時までの範囲を集計する */
        /* 遅刻、早退回数は課程の開始校時から終了校時までの範囲を指定しないで集計する */

        $query  = "INSERT INTO attend_semes_dat ";
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
        $query .= ", T_attend_dat (schregno, attenddate, periodcd, chaircd,di_cd) ";
        $query .= "               AS (SELECT T0.schregno, T0.attenddate, T0.periodcd, T0.chaircd, T0.di_cd ";
        $query .= "                     FROM attend_dat T0, ";
        $query .= "                        (SELECT T1.schregno ";
        $query .= "                               ,T1.executedate,T1.periodcd, T1.chaircd ";
        $query .= "                           FROM t_sch_std T1 ";
        $query .= "                          WHERE ";
                            //不在日数を除く
                            //転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
                            //転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合
        $query .= "                NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "                           WHERE   T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                                 ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                  (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
        $query .= "                          ) T1 ";
        $query .= "                       ,(SELECT T1.schregno, T2.s_periodcd, T2.e_periodcd ";
        $query .= "                           FROM schreg_regd_dat T1, ";
        $query .= "                               (SELECT T1.coursecd, T1.s_periodcd, T1.e_periodcd ";  
        $query .= "                                  FROM course_mst T1, course_ydat T2 ";
        $query .= "                                 WHERE T1.coursecd = T2.coursecd ";
        $query .= "                                   AND T2.year = '".$year."') T2 ";
        $query .= "                          WHERE T1.coursecd = T2.coursecd  ";
        $query .= "                            AND T1.year     = '".$year."' ";
        $query .= "                            AND T1.semester = '".$semester."') T2 ";


        $query .= "                   WHERE T0.schregno           = T1.schregno ";
        $query .= "                     AND T0.attenddate         = T1.executedate ";
        $query .= "                     AND T0.chaircd            = T1.chaircd ";
        $query .= "                     AND T0.periodcd           = T1.periodcd ";
        $query .= "                     AND T0.schregno           = T2.schregno ";
        $query .= "                     AND T0.periodcd BETWEEN T2.s_periodcd AND T2.e_periodcd ";
        $query .= "            AND    NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ";
        $query .= "                           WHERE   T7.SCHREGNO = T0.SCHREGNO AND ";
        $query .= "                                T7.TRANSFERCD IN('1','2') AND ";
        $query .= "                                T0.attenddate BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .= "                  ), ";
        /* 時間割データにある各生徒の受講すべき日と、それぞれの日の最初の校時、最後の校時、校時数 */
        $query .= "     T_period_cnt (schregno, executedate, first_period, last_period, period_cnt) ";
        $query .= "               AS (SELECT T1.schregno ";
        $query .= "                           ,T1.executedate ";
        $query .= "                           ,MIN(T1.periodcd)             AS first_period ";
        $query .= "                           ,MAX(T1.periodcd)             AS last_period ";
        $query .= "                           ,COUNT(DISTINCT T1.periodcd)  AS period_cnt ";
        $query .= "                     FROM ";
        $query .= "                         (SELECT  T1.schregno ";
        $query .= "                                 ,T1.executedate,T1.periodcd, T1.chaircd ";
        $query .= "                            FROM t_sch_std T1 ";
        $query .= "                           WHERE ";
                            //不在日数を除く
                            //転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
                            //転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合
        $query .= "                NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "                           WHERE   T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                                 ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                  (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
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

        //出停・忌引が一つでもあれば、遅刻・早退はカウントしない
        //遅刻・早退で使用
        $query .= " ,T_attend_dat3 AS ( ";
        $query .= "     SELECT schregno, attenddate ";
        $query .= "       FROM T_attend_dat ";
        $query .= "      WHERE di_cd IN ('2','9','3','10','19','20')  ";
        $query .= "     GROUP BY schregno, attenddate ) ";
        $query .= ",T_period_cnt3 AS (";
        $query .= "    SELECT  T1.schregno ";
        $query .= "           ,T1.executedate ";
        $query .= "           ,T1.first_period ";
        $query .= "           ,T1.last_period ";
        $query .= "           ,T1.period_cnt ";
        $query .= "    FROM    T_period_cnt T1 ";
        $query .= "     WHERE (T1.schregno ,T1.executedate) NOT IN (SELECT schregno ,attenddate FROM T_attend_dat3 W2 )  ";
        $query .= ") ";

        //留学日数を算出  休学を含める
        $query .= ",TRANSFER_SCHREG AS(";
        $query .=      "SELECT  T3.SCHREGNO, T3.TRANSFERCD, COUNT(DISTINCT T1.EXECUTEDATE)AS TRANSFER_DATE ";
        $query .=      "FROM    SCHREG_TRANSFER_DAT T3, t_sch_std T1 ";
        $query .=      "WHERE   T3.SCHREGNO = T1.SCHREGNO AND ";
        $query .=              "T3.TRANSFERCD IN('1','2') AND ";
        $query .=              "T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ";
                            //コアタイムの条件
        $query .=           "AND EXISTS(SELECT 'X' FROM COURSE_MST S1, schreg_regd_dat S2 ";
        $query .=                      "WHERE S1.COURSECD = S2.COURSECD ";
        $query .= "                       AND S2.year     = '".$year."' ";
        $query .= "                       AND S2.semester = '".$semester."' ";
        $query .=                        "AND T1.PERIODCD BETWEEN S1.S_PERIODCD AND S1.E_PERIODCD ";
        $query .=                        "AND T1.SCHREGNO = S2.SCHREGNO) ";
                            //不在日数を除く
                            //転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
                            //転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合
        $query .= "          AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "                         WHERE   T6.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "                               ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
        $query .=      "GROUP BY T3.SCHREGNO, T3.TRANSFERCD ";
        $query .=      ") ";

        $query .= "SELECT '0' ";
        $query .= "      ,'".$year."' ";
        $query .= "      ,'".$month."' ";
        $query .= "      ,'".$semester."' ";
        $query .= "      ,TT1.schregno ";
        $query .= "      ,'".$day."'";
        $query .= "      ,TT1.lesson ";
        $query .= "      ,COALESCE(TT11.offdays,0) AS offdays ";
        $query .= "      ,COALESCE(TT2.absent,0)   AS absent ";
        $query .= "      ,COALESCE(TT3.suspend,0)  AS suspend ";
        $query .= "      ,COALESCE(TT4.mourning,0) AS mourning ";
        $query .= "      ,COALESCE(TT12.abroad,0)  AS abroad ";
        $query .= "      ,0 AS sick ";
        $query .= "      ,0 AS notice ";
        $query .= "      ,COALESCE(TT5.sick,0) + COALESCE(TT5.notice,0) + COALESCE(TT5.nonotice,0) AS nonotice ";
        $query .= "      ,COALESCE(TT6.late,0) + COALESCE(TT8.late,0)   AS late ";
        $query .= "      ,COALESCE(TT6.early,0) + COALESCE(TT9.early,0) AS early ";
        $query .= "      ,0 as KEKKA_JISU ";
        $query .= "      ,0 as KEKKA ";
        $query .= "      ,0 as LATEDETAIL ";
        $query .= "      ,COALESCE(TT13.VIRUS,0)   AS VIRUS ";
        $query .= "      ,0 as KOUDOME "; // TODO:
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM  ";
        /* 授業日数 */
        $query .= "   (SELECT T2.schregno ";
        $query .= "          ,COUNT(DISTINCT T2.executedate) AS lesson ";
        $query .= "      FROM  ";
        $query .= "     (SELECT T1.schregno ";
        $query .= "            ,T1.executedate,T1.periodcd, T1.chaircd ";
        $query .= "        FROM t_sch_std T1 ";
        $query .= "       WHERE ";
                            //不在日数を除く
                            //転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
                            //転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合
        $query .= "             NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "                        WHERE   T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                              ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                               (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ) ";
        $query .= "         AND NOT EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DETAIL D1 ";
        $query .= "                 WHERE ";
        $query .= "                     D1.SCHREGNO=T1.SCHREGNO AND ";
        $query .= "                     D1.YEAR='".$year."' AND ";
        $query .= "                     D1.SEMESTER='".$semester."' AND ";
        $query .= "                     D1.COUNTFLG='0' "; // 0:集計しない
        $query .= "                 ) ";
		                    // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "         AND NOT EXISTS( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     ATTEND_DAT L4 ";
        $query .= "                 WHERE ";
        $query .= "                     L4.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                     L4.ATTENDDATE = T1.EXECUTEDATE AND ";
        $query .= "                     L4.DI_CD = '27') ";
		                    // 勤怠コードが'28'の校時は時間割にカウントしない
        $query .= "         AND NOT EXISTS( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     ATTEND_DAT L4 ";
        $query .= "                 WHERE ";
        $query .= "                     L4.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                     L4.ATTENDDATE = T1.EXECUTEDATE AND ";
        $query .= "                     L4.PERIODCD = T1.PERIODCD AND ";
        $query .= "                     L4.DI_CD = '28') ";
        $query .= "       ) T2 ";
        $query .= "          ,(SELECT T1.schregno, T2.s_periodcd, T2.e_periodcd ";
        $query .= "              FROM schreg_regd_dat T1, ";
        $query .= "                  (SELECT T1.coursecd, T1.s_periodcd, T1.e_periodcd ";
        $query .= "                     FROM course_mst T1, course_ydat T2 ";
        $query .= "                    WHERE T1.coursecd = T2.coursecd ";
        $query .= "                      AND T2.year = '".$year."') T2 ";
        $query .= "             WHERE T1.coursecd = T2.coursecd  ";
        $query .= "               AND T1.year     = '".$year."' ";
        $query .= "               AND T1.semester = '".$semester."') T3 ";
        $query .= "     WHERE T2.schregno  = T3.schregno ";
        $query .= "       AND T2.periodcd BETWEEN T3.s_periodcd AND T3.e_periodcd ";
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
        $query .= "                           ,MIN(W1.periodcd)   AS first_period ";
        $query .= "                           ,COUNT(W1.periodcd) AS period_cnt ";
        $query .= "                       FROM T_attend_dat W1 ";
        $query .= "                      WHERE W1.di_cd IN ('4','5','6','11','12','13') ";
        $query .= "                      GROUP BY W1.schregno, W1.attenddate) T1 ";
        $query .= "              WHERE T0.schregno     = T1.schregno ";
        $query .= "                AND T0.executedate  = T1.attenddate ";
        $query .= "                AND T0.first_period = T1.first_period ";
        $query .= "                AND T0.period_cnt   = T1.period_cnt) W1 ";
        $query .= "     WHERE W0.schregno           = W1.schregno ";
        $query .= "       AND W0.attenddate         = W1.executedate ";
        $query .= "       AND W0.periodcd           = W1.first_period ";
        $query .= "     GROUP BY W0.schregno ) TT5 ";
        $query .= "  ON TT1.schregno = TT5.schregno ";
        /* 遅刻・早退日数 */
        $query .= "LEFT OUTER JOIN  ";
        $query .= "   (SELECT T0.schregno ";
        $query .= "      ,COUNT(T2.attenddate) AS late ";
        $query .= "      ,COUNT(T3.attenddate) AS early ";
        $query .= "      FROM T_period_cnt3 T0 ";
        $query .= "           INNER JOIN (SELECT W1.schregno, W1.attenddate, COUNT(W1.periodcd) AS period_cnt ";
        $query .= "                         FROM T_attend_dat W1 ";
        $query .= "                        WHERE W1.di_cd NOT IN ('0','14','15','16','21','22') ";
        $query .= "                        GROUP BY W1.schregno, W1.attenddate) T1 ON T0.schregno    = T1.schregno ";
        $query .= "                                                               AND T0.executedate = T1.attenddate ";
        $query .= "                                                               AND T0.period_cnt != T1.period_cnt ";
        $query .= "            LEFT JOIN (SELECT schregno ,attenddate ,periodcd ";
        $query .= "                         FROM T_attend_dat  ";
        $query .= "                        WHERE di_cd IN ('4','5','6','11','12','13') ";
        $query .= "                      ) T2 ON T0.schregno     = T2.schregno ";
        $query .= "                          AND T0.executedate  = T2.attenddate ";
        $query .= "                          AND T0.first_period = T2.periodcd ";
        $query .= "            LEFT JOIN (SELECT schregno ,attenddate ,periodcd ";
        $query .= "                         FROM T_attend_dat  ";
        $query .= "                        WHERE di_cd IN ('4','5','6') ";
        $query .= "                      ) T3 ON T0.schregno    = T3.schregno ";
        $query .= "                          AND T0.executedate = T3.attenddate ";
        $query .= "                          AND T0.last_period = T3.periodcd ";
        $query .= "       GROUP BY T0.schregno) TT6 ";
        $query .= "  ON TT1.schregno = TT6.schregno ";
        /* 遅刻日数 */
        $query .= "LEFT OUTER JOIN  ";
        $query .= "   (SELECT T0.schregno ";
        $query .= "      ,COUNT(T2.attenddate) AS late ";
        $query .= "      FROM T_period_cnt3 T0 ";
        $query .= "           INNER JOIN (SELECT schregno ,attenddate ,periodcd ";
        $query .= "                         FROM T_attend_dat  ";
        $query .= "                        WHERE di_cd IN ('15','21') ";
        $query .= "                      ) T2 ON T0.schregno     = T2.schregno ";
        $query .= "                          AND T0.executedate  = T2.attenddate ";
        $query .= "                          AND T0.first_period = T2.periodcd ";
        $query .= "       GROUP BY T0.schregno) TT8 ";
        $query .= "  ON TT1.schregno = TT8.schregno ";
        /* 早退日数 */
        $query .= "LEFT OUTER JOIN  ";
        $query .= "   (SELECT T0.schregno ";
        $query .= "      ,COUNT(T3.attenddate) AS early ";
        $query .= "      FROM T_period_cnt3 T0 ";
        $query .= "           INNER JOIN (SELECT schregno ,attenddate ,periodcd ";
        $query .= "                         FROM T_attend_dat  ";
        $query .= "                        WHERE di_cd IN ('16','22') ";
        $query .= "                      ) T3 ON T0.schregno    = T3.schregno ";
        $query .= "                          AND T0.executedate = T3.attenddate ";
        $query .= "                          AND T0.last_period = T3.periodcd ";
        $query .= "       GROUP BY T0.schregno) TT9 ";
        $query .= "  ON TT1.schregno = TT9.schregno ";

        //休学日数の表
        $query .=    "LEFT JOIN(";
        $query .=       "select schregno, sum(transfer_date) as offdays ";
        $query .=       "from   TRANSFER_SCHREG ";
        $query .=       "where  transfercd = '2' ";
        $query .=       "group by schregno ";
        $query .=    ")tt11 ON TT11.SCHREGNO=TT1.SCHREGNO ";
        //留学日数の表
        $query .=    "LEFT JOIN(";
        $query .=       "select schregno, sum(transfer_date) as abroad ";
        $query .=       "from   TRANSFER_SCHREG ";
        $query .=       "where  transfercd = '1' ";
        $query .=       "group by schregno ";
        $query .=    ")tt12 ON TT12.SCHREGNO=TT1.SCHREGNO ";

        /* 出停(伝染病) */
        $query .= "LEFT OUTER JOIN (SELECT schregno, COUNT(DISTINCT attenddate) AS VIRUS ";
        $query .= "                   FROM T_attend_dat  ";
        $query .= "                  WHERE di_cd IN ('19','20') ";
        $query .= "                  GROUP BY schregno) TT13 ";
        $query .= "  ON TT1.schregno = TT13.schregno ";

        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   WHERE TT1.SCHREGNO IN (".knjd110aQuery::getSchregnoInSql($model, $year, $semester).") ";
        }

        return $query;
    }

    //科目別集計
    function InsertSubclassQuery($year,$semester,$month,$sdate,$edate,$day, $model)
    {
        $query  = " INSERT INTO attend_subclass_dat ";

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
        $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        $query .= "         AND T2.TESTKINDCD IN ('01','02') ";
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
        $query .= "                         SCHREG_BASE_MST T6 ";
        $query .= "                     WHERE ";
        $query .= "                         T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                         ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR  ";
        $query .= "                          (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) ";
        $query .= "                     ) ";
        $query .= "         AND NOT EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DETAIL D1 ";
        $query .= "                 WHERE ";
        $query .= "                     D1.SCHREGNO=T1.SCHREGNO AND ";
        $query .= "                     D1.YEAR='{$year}' AND ";
        $query .= "                     D1.SEMESTER='{$semester}' AND ";
        $query .= "                     D1.COUNTFLG='0' "; // 0:集計しない
        $query .= "                 ) ";
        $query .= "         AND NOT EXISTS( ";
        $query .= "                 SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND L4.DI_CD = '27' "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "                  ) ";
        $query .= "         AND NOT EXISTS( ";
        $query .= "                 SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND L4.DI_CD = '28' "; // 勤怠コードが'28'は時間割にカウントしない
        $query .= "                  ) ";
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
        $query .= "         W1.DI_CD, ";
        $query .= "         T1.CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT W1, ";
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
        //１日欠席を求める-------------START
        $query .= " ,T_attend_dat2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDDATE, ";
        $query .= "         T1.PERIODCD ";
        $query .= "     FROM ";
        $query .= "         T_attend_dat T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.DI_CD IN ('4','11','5','12','6','13','14') ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDDATE, ";
        $query .= "         T1.PERIODCD ";
        $query .= "     )  ";
        $query .= " ,T_sch_chr_dat2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD ";
        $query .= "     FROM ";
        $query .= "         T_sch_chr_dat T1 ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD ";
        $query .= "     )  ";
        $query .= " ,T_attend_dat_not_exists AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHREGNO , ";
        $query .= "         S1.EXECUTEDATE, ";
        $query .= "         COUNT(S1.PERIODCD) AS SCH_CNT, ";
        $query .= "         COUNT(S2.PERIODCD) AS ATT_CNT ";
        $query .= "     FROM  ";
        $query .= "         T_sch_chr_dat2 S1 ";
        $query .= "         LEFT JOIN T_attend_dat2 S2 ON S2.SCHREGNO = S1.SCHREGNO ";
        $query .= "                                  AND S2.ATTENDDATE = S1.EXECUTEDATE ";
        $query .= "                                  AND S2.PERIODCD = S1.PERIODCD  ";
        $query .= "     GROUP BY  ";
        $query .= "         S1.SCHREGNO,  ";
        $query .= "         S1.EXECUTEDATE  ";
        $query .= "     HAVING  ";
        $query .= "         COUNT(S1.PERIODCD) = COUNT(S2.PERIODCD)  ";
        $query .= "     )  ";
        //１日欠席を求める-------------END
        //時数など
        $query .= " ,CNT_LESSON AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         COUNT(*) AS LESSON, ";
        $query .= "         COUNT(S3.SCHREGNO) AS OFFDAYS, ";
        $query .= "         COUNT(S4.SCHREGNO) AS ABROAD ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "             T1.SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             T_sch_chr_dat T1 ";
        $query .= "         GROUP BY ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "             T1.SUBCLASSCD ";
        $query .= "         ) T1  ";
        $query .= "         LEFT JOIN SCHREG_TRANSFER_DAT S3 ON S3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND S3.TRANSFERCD = '2' ";
        $query .= "                                         AND T1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE ";
        $query .= "                                         AND S3.TRANSFER_EDATE ";
        $query .= "         LEFT JOIN SCHREG_TRANSFER_DAT S4 ON S4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND S4.TRANSFERCD = '1' ";
        $query .= "                                         AND T1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE ";
        $query .= "                                         AND S4.TRANSFER_EDATE ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     )  ";
        //欠時数など
        $query .= " ,CNT_ATTEND AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         SUM(T1.ABSENT)      AS  ABSENT, ";
        $query .= "         SUM(T1.SUSPEND)     AS  SUSPEND, ";
        $query .= "         SUM(T1.MOURNING)    AS  MOURNING, ";
        $query .= "         SUM(T1.SICK)        AS  SICK, ";
        $query .= "         SUM(T1.NOTICE)      AS  NOTICE, ";
        $query .= "         SUM(T1.NONOTICE)    AS  NONOTICE, ";
        $query .= "         SUM(T1.NURSEOFF)    AS  NURSEOFF, ";
        $query .= "         SUM(T1.LATE)        AS  LATE, ";
        $query .= "         SUM(T1.EARLY)       AS  EARLY, ";
        $query .= "         SUM(T1.VIRUS)       AS  VIRUS ";
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
        $query .= "             MAX(CASE T1.DI_CD WHEN  '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END)  AS  ABSENT, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN  '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END)  AS  SUSPEND, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN '15' THEN 1 WHEN '21' THEN 1 ELSE 0 END)  AS  LATE, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN '16' THEN 1 WHEN '22' THEN 1 ELSE 0 END)  AS  EARLY, ";
        $query .= "             MAX(CASE T1.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS ";
        $query .= "         FROM ";
        $query .= "             T_attend_dat T1 ";
        $query .= "         WHERE ";
                                //１日欠席を除く
        $query .= "             NOT EXISTS(SELECT 'X' FROM T_attend_dat_not_exists N7 ";
        $query .= "                         WHERE N7.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                               N7.EXECUTEDATE = T1.ATTENDDATE ) ";
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
        $query .= "     '{$day}', ";
        $query .= "     T1.LESSON, ";
        $query .= "     T1.OFFDAYS, ";
        $query .= "     value(L1.ABSENT,0) as ABSENT, ";
        $query .= "     value(L1.SUSPEND,0) as SUSPEND, ";
        $query .= "     value(L1.MOURNING,0) as MOURNING, ";
        $query .= "     T1.ABROAD, ";
        $query .= "     value(L1.SICK,0) as SICK, ";
        $query .= "     value(L1.NOTICE,0) as NOTICE, ";
        $query .= "     value(L1.NONOTICE,0) as NONOTICE, ";
        $query .= "     value(L1.NURSEOFF,0) as NURSEOFF, ";
        $query .= "     value(L1.LATE,0) as LATE, ";
        $query .= "     value(L1.EARLY,0) as EARLY, ";
        $query .= "     value(L1.VIRUS,0) as VIRUS, ";
        $query .= "     0 as KOUDOME, "; // TODO:
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     CNT_LESSON T1 ";
        $query .= "     LEFT JOIN CNT_ATTEND L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                                      AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "                                                      AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                                      AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }

        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   WHERE T1.SCHREGNO IN (".knjd110aQuery::getSchregnoInSql($model, $year, $semester).") ";
        }

        return $query;
    }
}
?>