<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjz202Query extends Query {

    //欠課数オーバーの前警告
    function getNameMst($namecd1, $namecd2) {
        $query  = " SELECT NAMESPARE1 FROM NAME_MST ";
        $query .= "  WHERE NAMECD1 = '{$namecd1}' ";
        $query .= "    AND NAMECD2 = '{$namecd2}' ";
        return $query;
    }

    //既に登録されているデータがあるのか カウントで確かめる(欠課数オーバー)
    function countGetAbsenceWarn($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '". $model->year ."' ";
        $query .= "     AND ( ";
        $query .= "                ABSENCE_WARN  IS NOT NULL ";
        if (in_array("2", $model->control["SEMESTER"])) {
            $query .= "         OR ABSENCE_WARN2 IS NOT NULL ";
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $query .= "         OR ABSENCE_WARN3 IS NOT NULL ";
        }
        $query .= "         ) ";

        return $query;
    }

    //既に登録されているデータがあるのか カウントで確かめる(欠課数オーバー)
    function countGetAbsenceWarnSpecial($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CREDIT_SPECIAL_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '". $model->year ."' ";
        $query .= "     AND ( ";
        $query .= "                ABSENCE_WARN  IS NOT NULL ";
        if (in_array("2", $model->control["SEMESTER"])) {
            $query .= "         OR ABSENCE_WARN2 IS NOT NULL ";
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $query .= "         OR ABSENCE_WARN3 IS NOT NULL ";
        }
        $query .= "         ) ";

        return $query;
    }

    //起動チェック
    function ChecktoStart($db)
    {
        return $db->getOne("SELECT COUNT(*) FROM SEMESTER_MST");
    }

    function getYear()
    {
        return "SELECT YEAR FROM SEMESTER_MST GROUP BY YEAR";
    }

    //学期の間の月を取得
    function getSemesterMonth($year)
    {
        return "SELECT * FROM semester_mst WHERE year = '".$year."' AND semester <> '9' ORDER BY semester, sdate";
    }

    //日付の年度内チェック
    function getCheckDate($model) {
        //DB接続
        $db = Query::dbCheckOut();
        //SQL
        $query  = " SELECT COUNT(*) FROM SEMESTER_MST ";
        $query .= "  WHERE YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER = '9' ";
        $query .= "    AND '".$model->date."' BETWEEN SDATE AND EDATE ";
        $rtnCnt = $db->getOne($query);
        //DB切断
        Query::dbCheckIn($db);

        return (0 < $rtnCnt) ? false : true;
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
        //DB接続
        $db = Query::dbCheckOut();
        //オートコミット
        $db->autoCommit(false);

        //学校マスタの情報を取得。
        $knjSchoolMst = knjz202Query::getSchoolMst($db, $model->year, $model);

        //学期マスタの情報を取得。
        $knjSemesterMst1 = knjz202Query::getSemesterMst($db, $model->year, "1");
        $knjSemesterMst9 = knjz202Query::getSemesterMst($db, $model->year, "9");

//echo date("H:i:s") ."<BR>";
        //欠課数上限値（実授業数）
        if ($knjSchoolMst["JUGYOU_JISU_FLG"] == "2") {
            //出欠関連共通
            $attendSemAllMap = AttendAccumulate::getAttendSemesMap($db, "", $model->year);
            $hasuuMap1 = AttendAccumulate::getHasuuMap($attendSemAllMap, $knjSemesterMst1["SDATE"], $knjSemesterMst9["EDATE"]);
            $hasuuMap2 = AttendAccumulate::getHasuuMap($attendSemAllMap, $knjSemesterMst1["SDATE"], str_replace("/", "-", $model->date));
            //---------- 通常科目の上限値を生成 ----------//
            $combinedFlg = "";
            //1:年間 学期マスタ９の終了日まで
            $query = knjz202Query::deleteAbsenceHigh($model->year, "1", "", $combinedFlg, $model);
            $db->query($query);
            $query = knjz202Query::insertAbsenceHigh(
                                    $model->year,
                                    "1",
                                    $knjSemesterMst9["EDATE"],
                                    $hasuuMap1["attendSemesInState"],
                                    $hasuuMap1["befDayFrom"],
                                    $hasuuMap1["befDayTo"],
                                    $hasuuMap1["aftDayFrom"],
                                    $hasuuMap1["aftDayTo"],
                                    $knjSchoolMst,
                                    "","", $combinedFlg, $model);
            $db->query($query);
            //2:随時 指定日まで
            $query = knjz202Query::deleteAbsenceHigh($model->year, "2", "", $combinedFlg, $model);
            $db->query($query);
            $query = knjz202Query::insertAbsenceHigh(
                                    $model->year,
                                    "2",
                                    str_replace("/", "-", $model->date),
                                    $hasuuMap2["attendSemesInState"],
                                    $hasuuMap2["befDayFrom"],
                                    $hasuuMap2["befDayTo"],
                                    $hasuuMap2["aftDayFrom"],
                                    $hasuuMap2["aftDayTo"],
                                    $knjSchoolMst,
                                    "","", $combinedFlg, $model);
            $db->query($query);
            //---------- 合併先科目の上限値を生成（合併元科目の授業数から算出）----------//
            $combinedFlg = "on";
            //1:年間 学期マスタ９の終了日まで
            $query = knjz202Query::deleteAbsenceHigh($model->year, "1", "", $combinedFlg, $model);
            $db->query($query);
            $query = knjz202Query::insertAbsenceHigh(
                                    $model->year,
                                    "1",
                                    $knjSemesterMst9["EDATE"],
                                    $hasuuMap1["attendSemesInState"],
                                    $hasuuMap1["befDayFrom"],
                                    $hasuuMap1["befDayTo"],
                                    $hasuuMap1["aftDayFrom"],
                                    $hasuuMap1["aftDayTo"],
                                    $knjSchoolMst,
                                    "", "", $combinedFlg, $model);
            $db->query($query);
            //2:随時 指定日まで
            $query = knjz202Query::deleteAbsenceHigh($model->year, "2", "", $combinedFlg, $model);
            $db->query($query);
            $query = knjz202Query::insertAbsenceHigh(
                                    $model->year,
                                    "2",
                                    str_replace("/", "-", $model->date),
                                    $hasuuMap2["attendSemesInState"],
                                    $hasuuMap2["befDayFrom"],
                                    $hasuuMap2["befDayTo"],
                                    $hasuuMap2["aftDayFrom"],
                                    $hasuuMap2["aftDayTo"],
                                    $knjSchoolMst,
                                    "", "", $combinedFlg, $model);
            $db->query($query);
            //---------------- 特別活動科目グループ毎 ----------------//
            $combinedFlg = "";
            //1:年間 学期マスタ９の終了日まで
            $query = knjz202Query::deleteAbsenceHigh($model->year, "1", "sp", $combinedFlg, $model);
            $db->query($query);
            $query = knjz202Query::insertAbsenceHigh(
                                    $model->year,
                                    "1",
                                    $knjSemesterMst9["EDATE"],
                                    $hasuuMap1["attendSemesInState"],
                                    $hasuuMap1["befDayFrom"],
                                    $hasuuMap1["befDayTo"],
                                    $hasuuMap1["aftDayFrom"],
                                    $hasuuMap1["aftDayTo"],
                                    $knjSchoolMst,
                                    "sp","", $combinedFlg, $model);
            $db->query($query);
            $query = knjz202Query::insertAbsenceHigh(
                                    $model->year,
                                    "1",
                                    $knjSemesterMst9["EDATE"],
                                    $hasuuMap1["attendSemesInState"],
                                    $hasuuMap1["befDayFrom"],
                                    $hasuuMap1["befDayTo"],
                                    $hasuuMap1["aftDayFrom"],
                                    $hasuuMap1["aftDayTo"],
                                    $knjSchoolMst,
                                    "sp","total", $combinedFlg, $model);
            $db->query($query);
            //2:随時 指定日まで
            $query = knjz202Query::deleteAbsenceHigh($model->year, "2", "sp", $combinedFlg, $model);
            $db->query($query);
            $query = knjz202Query::insertAbsenceHigh(
                                    $model->year,
                                    "2",
                                    str_replace("/", "-", $model->date),
                                    $hasuuMap2["attendSemesInState"],
                                    $hasuuMap2["befDayFrom"],
                                    $hasuuMap2["befDayTo"],
                                    $hasuuMap2["aftDayFrom"],
                                    $hasuuMap2["aftDayTo"],
                                    $knjSchoolMst,
                                    "sp","", $combinedFlg, $model);
            $db->query($query);
            $query = knjz202Query::insertAbsenceHigh(
                                    $model->year,
                                    "2",
                                    str_replace("/", "-", $model->date),
                                    $hasuuMap2["attendSemesInState"],
                                    $hasuuMap2["befDayFrom"],
                                    $hasuuMap2["befDayTo"],
                                    $hasuuMap2["aftDayFrom"],
                                    $hasuuMap2["aftDayTo"],
                                    $knjSchoolMst,
                                    "sp","total", $combinedFlg, $model);
            $db->query($query);
        }
//echo date("H:i:s") ."<BR>";

        //コミット
        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //学校マスタの情報を取得。
    function getSchoolMst($db, $year, $model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SUB_OFFDAYS, ";
        $query .= "     SUB_SUSPEND, ";
        if ($model->virus == 'true') {
            $query .= "     SUB_VIRUS, ";
        }
        if ($model->koudome == 'true') {
            $query .= "     SUB_KOUDOME, ";
        }
        $query .= "     SUB_MOURNING, ";
        $query .= "     JUGYOU_JISU_FLG, ";
        $query .= "     RISYU_BUNSI, ";
        $query .= "     RISYU_BUNBO, ";
        $query .= "     SYUTOKU_BUNSI, ";
        $query .= "     SYUTOKU_BUNBO, ";
        $query .= "     RISYU_BUNSI_SPECIAL, ";
        $query .= "     RISYU_BUNBO_SPECIAL, ";
        $query .= "     SYUTOKU_BUNSI_SPECIAL, ";
        $query .= "     SYUTOKU_BUNBO_SPECIAL, ";
        $query .= "     JITU_JIFUN_SPECIAL, ";
        $query .= "     JOUGENTI_SANSYUTU_HOU ";
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

    function getAppointedDate($year, $div) {
        $query  = " SELECT MAX(APPOINTED_DATE) as APPOINTED_DATE, MAX(UPDATED) as UPDATED ";
        $query .= "   FROM SCHREG_ABSENCE_HIGH_DAT ";
        $query .= "  WHERE YEAR = '{$year}' ";
        $query .= "    AND DIV  = '{$div}' ";
        return $query;
    }

    //名称マスタ「C040」に登録されている、年度・学期・月は、集計テーブルを参照する
    function getSemeMonthC040($db, $year, $semester, $month) {
        $query  = " SELECT COUNT(*) FROM NAME_MST ";
        $query .= "  WHERE NAMECD1 = 'C040' ";
        $query .= "    AND NAME1 = '{$year}' ";
        $query .= "    AND NAME2 = '{$semester}' ";
        $query .= "    AND NAME3 = '{$month}' ";
        $rtnCnt = $db->getOne($query);

        return 0 < $rtnCnt;
    }

    //締め日を取得
    function getSimeDate($db, $year, $semester, $month) {
        $query  = " SELECT max(APPOINTED_DAY) FROM ATTEND_SEMES_DAT ";
        $query .= "  WHERE YEAR = '{$year}' ";
        $query .= "    AND SEMESTER = '{$semester}' ";
        $query .= "    AND MONTH = '{$month}' ";
        $day = $db->getOne($query);

        $nen = ((int)$month < 4) ? ((int)$year + 1) : $year;
        $simeDate = $nen ."-" .$month ."-" .$day;
        return strlen($day) ? $simeDate : "";
    }

    //翌月の開始日（１日）を取得
    function getNextMonthSdate($db, $year, $semester, $month, $flg) {
        $nen = ((int)$month < 4) ? ((int)$year + 1) : $year;
        if ($flg == "1") {
            $rtnDate = $db->getOne("SELECT sdate FROM semester_mst WHERE year = '{$year}' AND semester = '{$semester}'");
        } else {
            $rtnDate = $nen ."-" .$month ."-" .$day;
        }
        return $rtnDate;
    }

    //締め日の翌日を取得
    function getNextSdate($db, $edate, $edate9) {
        //実行月の指定日（締め日）の翌日
        $sdate = $db->getOne("VALUES ADD_DAYS(DATE('{$edate}'), 1)");

        //学期マスタ９の終了日を超えたらブランク。
        $nextSdate = ($edate9 < $sdate) ? "" : $sdate;

        return $nextSdate;
    }

    function deleteAbsenceHigh($year, $div, $sp, $combinedFlg, $model) {
        $table = ($sp == "sp") ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";

        $query  = " DELETE FROM ".$table." ";
        $query .= "  WHERE YEAR = '{$year}' ";
        $query .= "    AND DIV  = '{$div}' ";
        if ($combinedFlg == "on" && $sp != "sp") {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD in ( ";
            } else {
                $query .= "    AND SUBCLASSCD in ( ";
            }
            $query .= "         SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
            } else {
                $query .= "             COMBINED_SUBCLASSCD ";
            }
            $query .= "         FROM ";
            $query .= "             SUBCLASS_REPLACE_COMBINED_DAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR = '{$year}' ";
            $query .= "         GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             COMBINED_CLASSCD, ";
                $query .= "             COMBINED_SCHOOL_KIND, ";
                $query .= "             COMBINED_CURRICULUM_CD, ";
            }
            $query .= "             COMBINED_SUBCLASSCD ";
            $query .= "    ) ";
        }
        return $query;
    }

    //欠課数上限値
    function insertAbsenceHigh($year, $div, $edate, $semesInState, $befDayFrom, $befDayTo, $aftDayFrom, $aftDayTo, $knjSchoolMst, $sp, $total, $combinedFlg, $model) {
        $table = ($sp == "sp") ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subcd = ($sp == "sp") ? "TT2.SPECIAL_GROUP_CD" : "TT1.CLASSCD, TT1.SCHOOL_KIND, TT1.CURRICULUM_CD, TT1.SUBCLASSCD";
            if ($combinedFlg == "on" && $sp != "sp") $subcd = "TT3.COMBINED_CLASSCD, TT3.COMBINED_SCHOOL_KIND, TT3.COMBINED_CURRICULUM_CD, TT3.COMBINED_SUBCLASSCD";
        } else {
            $subcd = ($sp == "sp") ? "TT2.SPECIAL_GROUP_CD" : "TT1.SUBCLASSCD";
            if ($combinedFlg == "on" && $sp != "sp") $subcd = "TT3.COMBINED_SUBCLASSCD";
        }
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
        } else if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "2") {
            $sKeisan = "CEIL(";
            $eKeisan = ")";
        } else if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "3") {
            $sKeisan = "FLOOR(";
            $eKeisan = ")";
        } else if ($knjSchoolMst["JOUGENTI_SANSYUTU_HOU"] == "4") {
            $sKeisan = "ROUND(";
            $eKeisan = " * 10, 0) / 10";
        }

        $query  = " INSERT INTO ".$table." ";
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
        $query .= "     ".$subcd.", ";
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
        if ($semesInState != "") {
            $query .= "     SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         CLASSCD, ";
                $query .= "         SCHOOL_KIND, ";
                $query .= "         CURRICULUM_CD, ";
            }
            $query .= "         SUBCLASSCD, ";
            $query .= "         SCHREGNO, ";
            $query .= "         SUM(VALUE(LESSON,0)) AS LESSON, ";
            if ($model->virus == 'true') {
                if ($model->koudome == 'true') {
                    $query .= "         SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) - VALUE(VIRUS,0) - VALUE(KOUDOME,0) ";
                } else {
                    $query .= "         SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) - VALUE(VIRUS,0) ";
                }
            } else {
                if ($model->koudome == 'true') {
                    $query .= "         SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) - VALUE(KOUDOME,0) ";
                } else {
                    $query .= "         SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) ";
                }
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
                $query .= "      + VALUE(VIRUS,0) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "      + VALUE(KOUDOME,0) ";
            }
            $query .= "         ) AS MLESSON ";
            $query .= "     FROM ";
            $query .= "         ATTEND_SUBCLASS_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR = '{$year}' ";
            $query .= "         AND SEMESTER || MONTH IN ".$semesInState." ";
            $query .= "     GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         CLASSCD, ";
                $query .= "         SCHOOL_KIND, ";
                $query .= "         CURRICULUM_CD, ";
            }
            $query .= "         SUBCLASSCD, ";
            $query .= "         SCHREGNO ";
        }
        if (($semesInState != "") && ($befDayFrom != "" || $aftDayFrom != "")) {
            $query .= "     UNION ALL ";
        }
        //時間割テーブル参照
        if ($befDayFrom != "" || $aftDayFrom != "") {
            $query .= "     SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         TBL.CLASSCD, ";
                $query .= "         TBL.SCHOOL_KIND, ";
                $query .= "         TBL.CURRICULUM_CD, ";
            }
            $query .= "         TBL.SUBCLASSCD, ";
            $query .= "         TBL.SCHREGNO, ";
            $query .= "         COUNT(*) AS LESSON, ";
            if ($model->virus == 'true') {
                if ($model->koudome == 'true') {
                    $query .= "         COUNT(*) - COUNT(S3.SCHREGNO) - COUNT(S4.SCHREGNO) - SUM(CASE WHEN S2.DI_CD IN('2','9','3','10','19','20','25','26') THEN 1 ELSE 0 END) ";
                } else {
                    $query .= "         COUNT(*) - COUNT(S3.SCHREGNO) - COUNT(S4.SCHREGNO) - SUM(CASE WHEN S2.DI_CD IN('2','9','3','10','19','20') THEN 1 ELSE 0 END) ";
                }
            } else {
                if ($model->koudome == 'true') {
                    $query .= "         COUNT(*) - COUNT(S3.SCHREGNO) - COUNT(S4.SCHREGNO) - SUM(CASE WHEN S2.DI_CD IN('2','9','3','10','25','26') THEN 1 ELSE 0 END) ";
                } else {
                    $query .= "         COUNT(*) - COUNT(S3.SCHREGNO) - COUNT(S4.SCHREGNO) - SUM(CASE WHEN S2.DI_CD IN('2','9','3','10') THEN 1 ELSE 0 END) ";
                }
            }
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
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         SELECT T2.SCHREGNO, T4.CLASSCD, T4.SCHOOL_KIND, T4.CURRICULUM_CD, T4.SUBCLASSCD, T1.EXECUTEDATE, T1.PERIODCD ";
            } else {
                $query .= "         SELECT T2.SCHREGNO, T4.SUBCLASSCD, T1.EXECUTEDATE, T1.PERIODCD ";
            }
            $query .= "           FROM SCH_CHR_DAT T1 ";
            $query .= "               ,CHAIR_STD_DAT T2 ";
            $query .= "               ,SCHREG_REGD_DAT T3 ";
            $query .= "               ,CHAIR_DAT T4 ";
            $query .= "          WHERE T1.YEAR     = '{$year}' ";
            if ($befDayFrom != "" && $aftDayFrom != "") {
                $query .= "        AND ((T1.EXECUTEDATE BETWEEN DATE('{$befDayFrom}') AND DATE('{$befDayTo}')) ";
                $query .= "          OR (T1.EXECUTEDATE BETWEEN DATE('{$aftDayFrom}') AND DATE('{$aftDayTo}'))) ";
            } else if ($befDayFrom != "") {
                $query .= "        AND T1.EXECUTEDATE BETWEEN DATE('{$befDayFrom}') AND DATE('{$befDayTo}') ";
            } else if ($aftDayFrom != "") {
                $query .= "        AND T1.EXECUTEDATE BETWEEN DATE('{$aftDayFrom}') AND DATE('{$aftDayTo}') ";
            }
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
            $query .= "                                  E1.COUNTFLG      = '0') ";
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST E2 ";
            $query .= "                            WHERE E2.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "                                  ((E2.GRD_DIV IN('1','2','3') AND E2.GRD_DATE < T1.EXECUTEDATE) OR ";
            $query .= "                                   (E2.ENT_DIV IN('4','5')     AND E2.ENT_DATE > T1.EXECUTEDATE))) ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         GROUP BY T2.SCHREGNO, T4.CLASSCD, T4.SCHOOL_KIND, T4.CURRICULUM_CD, T4.SUBCLASSCD, T1.EXECUTEDATE, T1.PERIODCD ";
            } else {
                $query .= "         GROUP BY T2.SCHREGNO, T4.SUBCLASSCD, T1.EXECUTEDATE, T1.PERIODCD ";
            }
            $query .= "         ) TBL ";
            $query .= "         LEFT JOIN ( ";
            $query .= "         SELECT T1.SCHREGNO,T1.ATTENDDATE,T1.PERIODCD,ATT_DI.REP_DI_CD AS DI_CD ";
            $query .= "           FROM ATTEND_DAT T1 ";
            $query .= "                LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = T1.YEAR AND ATT_DI.DI_CD = T1.DI_CD ";
            if ($model->virus == 'true') {
                if ($model->koudome == 'true') {
                    $query .= "          WHERE ATT_DI.REP_DI_CD IN('2','9','3','10','19','20','25','26') ";
                } else {
                    $query .= "          WHERE ATT_DI.REP_DI_CD IN('2','9','3','10','19','20') ";
                }
            } else {
                if ($model->koudome == 'true') {
                    $query .= "          WHERE ATT_DI.REP_DI_CD IN('2','9','3','10','25','26') ";
                } else {
                    $query .= "          WHERE ATT_DI.REP_DI_CD IN('2','9','3','10') ";
                }
            }
            if ($befDayFrom != "" && $aftDayFrom != "") {
                $query .= "        AND ((T1.ATTENDDATE BETWEEN DATE('{$befDayFrom}') AND DATE('{$befDayTo}')) ";
                $query .= "          OR (T1.ATTENDDATE BETWEEN DATE('{$aftDayFrom}') AND DATE('{$aftDayTo}'))) ";
            } else if ($befDayFrom != "") {
                $query .= "        AND T1.ATTENDDATE BETWEEN DATE('{$befDayFrom}') AND DATE('{$befDayTo}') ";
            } else if ($aftDayFrom != "") {
                $query .= "        AND T1.ATTENDDATE BETWEEN DATE('{$aftDayFrom}') AND DATE('{$aftDayTo}') ";
            }
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
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     GROUP BY TBL.SCHREGNO, TBL.CLASSCD, TBL.SCHOOL_KIND, TBL.CURRICULUM_CD, TBL.SUBCLASSCD ";
            } else {
                $query .= "     GROUP BY TBL.SCHREGNO, TBL.SUBCLASSCD ";
            }
        }
        $query .= "     ) TT1 ";
        if ($sp == "sp") {
            $query .= "     INNER JOIN ATTEND_SUBCLASS_SPECIAL_DAT TT2 ON TT2.YEAR = '{$year}' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                               AND TT2.CLASSCD       = TT1.CLASSCD ";
                $query .= "                                               AND TT2.SCHOOL_KIND   = TT1.SCHOOL_KIND ";
                $query .= "                                               AND TT2.CURRICULUM_CD = TT1.CURRICULUM_CD ";
            }
            $query .= "                                               AND TT2.SUBCLASSCD = TT1.SUBCLASSCD ";
        }
        if ($combinedFlg == "on" && $sp != "sp") {
            $query .= "     INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT TT3 ON TT3.YEAR = '{$year}' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                               AND TT3.ATTEND_CLASSCD        = TT1.CLASSCD ";
                $query .= "                                               AND TT3.ATTEND_SCHOOL_KIND    = TT1.SCHOOL_KIND ";
                $query .= "                                               AND TT3.ATTEND_CURRICULUM_CD  = TT1.CURRICULUM_CD ";
            }
            $query .= "                                                 AND TT3.ATTEND_SUBCLASSCD = TT1.SUBCLASSCD ";
        }
        $query .= " GROUP BY TT1.SCHREGNO, ".$subcd." ";
        if ($sp == "sp" && $total == "total") {
            $query .= "     ) TT9 ";
            $query .= " GROUP BY TT9.SCHREGNO ";
        }
        return $query;
    }

    //更新(欠課数オーバー)
    function executeQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " UPDATE ";
        $query .= "     CREDIT_MST ";
        $query .= " SET ";
        $query .= "     REGISTERCD      = '".STAFFCD."' ";
        $query .= "    ,UPDATED         = SYSDATE() ";

        if ($model->field["ABSENCE_WARN_CHECK"] == "on" && strlen($model->field["ABSENCE_WARN"])) {
            $query .= ",ABSENCE_WARN    = {$model->field["ABSENCE_WARN"]} ";
        }
        if ($model->field["ABSENCE_WARN_CHECK2"] == "on" && strlen($model->field["ABSENCE_WARN2"])) {
            $query .= ",ABSENCE_WARN2   = {$model->field["ABSENCE_WARN2"]} ";
        }
        if ($model->field["ABSENCE_WARN_CHECK3"] == "on" && strlen($model->field["ABSENCE_WARN3"])) {
            $query .= ",ABSENCE_WARN3   = {$model->field["ABSENCE_WARN3"]} ";
        }

        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //更新(欠課数オーバー)
    function executeQuerySpecial($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " UPDATE ";
        $query .= "     CREDIT_SPECIAL_MST ";
        $query .= " SET ";
        $query .= "     REGISTERCD      = '".STAFFCD."' ";
        $query .= "    ,UPDATED         = SYSDATE() ";

        if ($model->field["ABSENCE_WARN_CHECK"] == "on" && strlen($model->field["ABSENCE_WARN"])) {
            $query .= ",ABSENCE_WARN    = {$model->field["ABSENCE_WARN"]} ";
        }
        if ($model->field["ABSENCE_WARN_CHECK2"] == "on" && strlen($model->field["ABSENCE_WARN2"])) {
            $query .= ",ABSENCE_WARN2   = {$model->field["ABSENCE_WARN2"]} ";
        }
        if ($model->field["ABSENCE_WARN_CHECK3"] == "on" && strlen($model->field["ABSENCE_WARN3"])) {
            $query .= ",ABSENCE_WARN3   = {$model->field["ABSENCE_WARN3"]} ";
        }

        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

}
?>