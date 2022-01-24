<?php

require_once('for_php7.php');

class knjd110dQuery extends Query {

    // var $tmp_semester;   //UDFのTERM_GETの値を一時代入
    // var $tmp_month;      //選択月
    // var $tmp_sdate;      //データ抽出範囲の開始日付の値を一時代入
    // var $tmp_edate;      //データ抽出範囲の終了日付の値を一時代入

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
        //NO003-->
        $query  = "SELECT CASE WHEN T1.MONTH < 4 THEN 1 ELSE 0 END AS SORT2, ";
        $query .= "       T1.YEAR, T1.SEMESTER, T1.MONTH, T1.APPOINTED_DAY, T1.UPDATED ";
        $query .= "  FROM ( ";
        //NO003<--
        $query .= "SELECT YEAR, SEMESTER, SMALLINT(MONTH) AS MONTH, APPOINTED_DAY, MAX(UPDATED) as UPDATED";
        $query .= "  FROM ATTEND_SEMES_DAT";
        $query .= " WHERE COPYCD = '0' AND";
        $query .= "       YEAR   = '".$year."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110dQuery::getSchregnoInSql($model, $year, "").") ";
        }
        $query .= " GROUP BY YEAR, SEMESTER, MONTH,APPOINTED_DAY";
        //NO003-->
        $query .= ") T1 ";
        $query .= "ORDER BY T1.SEMESTER, 1, T1.MONTH ";
        //NO003<--
        return $query;
    }

    //登録者を取得(生成済み一覧)
    function getRegister($model, $row) {
        $query  = " WITH MAX_REGISTERCD AS ( ";
        $query .= "     SELECT ";
        $query .= "         MAX(W1.REGISTERCD) as REGISTERCD ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT W1 ";
        $query .= "     WHERE ";
        $query .= "             W1.COPYCD         = '0' ";
        $query .= "         AND W1.YEAR           = '{$row["YEAR"]}' ";
        $query .= "         AND W1.SEMESTER       = '{$row["SEMESTER"]}' ";
        $query .= "         AND W1.MONTH          = '".sprintf("%02d", $row["MONTH"])."' ";
        $query .= "         AND W1.APPOINTED_DAY  = '{$row["APPOINTED_DAY"]}' ";
        $query .= "         AND W1.UPDATED        = '{$row["UPDATED"]}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110dQuery::getSchregnoInSql($model, $row["YEAR"], "").") ";
        }
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
            $query = knjd110dQuery::UpdateLdate($model->limit_date);
            $db->query($query);
            $db->commit();

        } else { //実行ボタン

            //選択された年度内と月以前の未実施チェック
            if ($model->cmd != "execute2") {
                //sort($model->allMonth);
                //reset($model->allMonth);
                foreach ($model->allMonth as $key => $val)
                {
                    if ($val == $model->month) break;

                    knjd110dQuery::getTerm_Get($db,$model->year,$val, $model);
                    $arr = explode("-",$val);

                    //データ存在チェック
                    $query  = "SELECT COUNT(*) FROM attend_semes_dat ";
                    $query .= " WHERE year     = '".$model->year."'";
                    $query .= "   AND semester = '".$arr[0]."'";
                    $query .= "   AND month    = '".$arr[1]."'";
                    if ($model->Properties["useSchool_KindField"] == "1") {
                        $query .= "   AND SCHREGNO IN (".knjd110dQuery::getSchregnoInSql($model, $model->year, $arr[0]).") ";
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
                        $query .= "   AND SCHREGNO IN (".knjd110dQuery::getSchregnoInSql($model, $model->year, $arr[0]).") ";
                    }

                    $cur_day = $db->getOne($query);
                    $app_day = strftime("%d",strtotime($model->tmp_edate));
                    if ((int)$cur_day < (int)$app_day) {
                        $model->error_msg = "処理漏れの月があります。\\n（ ".$model->ctrl["学期名"][$arr[0]].(int)$arr[1]."月".$cur_day."日以降 ）";
                        Query::dbCheckIn($db);
                        return false;
                    } 
                }
            }

            knjd110dQuery::getTerm_Get($db,$model->year,$model->month, $model);

            //開始日より前はエラー
            if (strlen($model->day) && (int)$model->day < (int)strftime("%d",strtotime($model->tmp_sdate))) {
                $model->setWarning("MSG901", "開始日以前の日は指定できません。\\n\\n開始日：".strftime("%Y年%m月%e日",strtotime($model->tmp_sdate)));
                return false;
            }

            //処理月コンボの最終日を終了日にセット
            if (!strlen($model->day) || (int)$model->day > (int)strftime("%d",strtotime($model->tmp_edate))) {
                $model->day = (int)strftime("%d",strtotime($model->tmp_edate));

            //指定日を終了日にセット
            } elseif (strlen($model->day) && (int)$model->day < (int)strftime("%d",strtotime($model->tmp_edate))) {
                $model->tmp_edate = strftime("%Y-%m",strtotime($model->tmp_edate))."-".$model->day;
            }

            $db->autoCommit(false);

            //月別(SEMES)
            //（武蔵野東から要望）ATTEND_SEMES_DATの授業日数（LESSON）を保持
            //　入学日付（ENT_DATE）卒業日付（GRD_DATE）が指定月の生徒　かつ　授業日数（LESSON）が1以上（0またはnull以外）の生徒
            $keepLesson = array();
            $query = knjd110dQuery::keepAttendSemes($model->year,$model->tmp_semester,$model->tmp_month,$model->tmp_sdate,$model->tmp_edate,$model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $keepLesson[$row["SCHREGNO"]] = $row["LESSON"];
            }
            $query = knjd110dQuery::DeleteQuery("attend_semes_dat",$model->year,$model->tmp_semester,$model->tmp_month,$model);
            $db->query($query);
            $query = knjd110dQuery::InsertSemesterQuery($model->year,$model->tmp_semester,$model->tmp_month,$model->tmp_sdate,$model->tmp_edate,$model->day,$model);
            $db->query($query);
            //（武蔵野東から要望）保持したATTEND_SEMES_DATの授業日数（LESSON）で更新
            if (0 < get_count($keepLesson)) {
                foreach ($keepLesson as $schregno => $lesson) {
                    $query = knjd110dQuery::updAttendSemes($model->year,$model->tmp_semester,$model->tmp_month,$schregno,$lesson);
                    $db->query($query);
                }
            }
            //月別(SUBL)
            $query = knjd110dQuery::DeleteQuery("attend_semes_subl_dat",$model->year,$model->tmp_semester,$model->tmp_month,$model);
            $db->query($query);
            $query = knjd110dQuery::InsertSemesterSubQuery("SUBL",$model->year,$model->tmp_semester,$model->tmp_month,$model->tmp_sdate,$model->tmp_edate,$model);
            $db->query($query);
            //月別(SUBM)
            $query = knjd110dQuery::DeleteQuery("attend_semes_subm_dat",$model->year,$model->tmp_semester,$model->tmp_month,$model);
            $db->query($query);
            $query = knjd110dQuery::InsertSemesterSubQuery("SUBM",$model->year,$model->tmp_semester,$model->tmp_month,$model->tmp_sdate,$model->tmp_edate,$model);
            $db->query($query);

            //コミット(1)
            $db->commit();

            //コントロールマスタの更新
            $query = knjd110dQuery::UpdateLdate($model->limit_date);
            $db->query($query);

            //指定日（締め日）マスタ
            if ($model->Properties["useSchool_KindField"] == "1") {
                if ($model->school_kind != "ALL") {
                    $query = knjd110dQuery::deleteAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->school_kind);
                    $db->query($query);
                    $query = knjd110dQuery::insertAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->day, $model->school_kind); 
                    $db->query($query);
                } else {
                    foreach ($model->allSchoolKind as $key => $school_kind) {
                        $query = knjd110dQuery::deleteAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, $school_kind);
                        $db->query($query);
                        $query = knjd110dQuery::insertAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->day, $school_kind); 
                        $db->query($query);
                    }
                }
            } else {
                $query = knjd110dQuery::deleteAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, "");
                $db->query($query);
                $query = knjd110dQuery::insertAppointedDayQuery($model->year, $model->tmp_semester, $model->tmp_month, $model->day, ""); 
                $db->query($query);
            }

            //欠席日付の登録
            $query = knjd110dQuery::attendAbsenceDeleteQuery($model->year,$model->tmp_semester,$model->tmp_month,$model->tmp_sdate,$model->tmp_edate,$model);
            $db->query($query);
            $query = knjd110dQuery::attendAbsenceInsertQuery($model->year,$model->tmp_semester,$model->tmp_month,$model->tmp_sdate,$model->tmp_edate,$model);
            $db->query($query);

            //コミット(2)
            $db->commit();

            //更新データ件数チェック
            $query  = "SELECT COUNT(*) FROM attend_semes_dat ";
            $query .= " WHERE year     = '".$model->year."'";
            $query .= "   AND semester = '".$model->tmp_semester."'";
            $query .= "   AND month    = '".$model->tmp_month."'";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "   AND SCHREGNO IN (".knjd110dQuery::getSchregnoInSql($model, $model->year, $model->tmp_semester).") ";
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

    //欠席日付の削除
    function attendAbsenceDeleteQuery($year,$semester,$month,$sdate,$edate,$model) {
        $query  = " DELETE FROM ATTEND_ABSENCE_DAT ";
        $query .= " WHERE ";
        $query .= "     ABSENCE_DATE BETWEEN date('{$sdate}') AND date('{$edate}') ";
        $query .= "     AND YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110dQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        return $query;
    }

    //欠席日付の追加・・・ATTEND_SEMES_DAT(SICK,NOTICE,NONOTICE)をカウントした日付を登録する
    function attendAbsenceInsertQuery($year,$semester,$month,$sdate,$edate,$model) {
        $query  = " INSERT INTO ATTEND_ABSENCE_DAT ";
        //共通・・・出欠および名簿(chair_std_dat)と時間割(sch_chr_dat)の表
        $query .= knjd110dQuery::getCommonAttendAndChairQuery($year,$semester,$sdate,$edate,"DAY");

        //メイン
        $query .= " SELECT W0.schregno   AS SCHREGNO, ";
        $query .= "        W0.attenddate AS ABSENCE_DATE, ";
        $query .= "        '{$year}'     AS YEAR, ";
        $query .= "        '{$semester}' AS SEMESTER, ";
        $query .= "        '".STAFFCD."' AS REGISTERCD, ";
        $query .= "        SYSDATE()     AS UPDATED ";
        $query .= "   FROM T_attend_dat W0 ";
        $query .= "  WHERE W0.di_cd IN ('4','5','6','11','12','13') ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND W0.SCHREGNO IN (".knjd110dQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        $query .= "  GROUP BY W0.schregno,W0.attenddate ";

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

    //生成済みデータ削除
    function getDeleteQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->del_check as $val) {
            $arrDel = explode("-", $val);
            $year = $arrDel[0];
            $month = $arrDel[1];
            $semester = $arrDel[2];
            //月別
            $query = knjd110dQuery::DeleteQuery("ATTEND_SEMES_DAT", $year, $semester, $month,$model);
            $db->query($query);
            //月別
            $query = knjd110dQuery::DeleteQuery("ATTEND_SEMES_SUBL_DAT", $year, $semester, $month,$model);
            $db->query($query);
            //月別
            $query = knjd110dQuery::DeleteQuery("ATTEND_SEMES_SUBM_DAT", $year, $semester, $month,$model);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    function DeleteQuery($table,$year,$semester,$month,$model)
    {
        $query  = "DELETE FROM ".$table;
        $query .= " WHERE year     = '".$year."'";
        $query .= "   AND semester = '".$semester."'";
        $query .= "   AND month    = '".$month."'";
        $query .= "   AND copycd   = '0'";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHREGNO IN (".knjd110dQuery::getSchregnoInSql($model, $year, $semester).") ";
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
    function &getTerm_Get($db,$year,$month, &$model)
    {
        //parameter[0] => 学期, parameter[1] => 月, parameter[2] => 開始月または終了月フラグ
        $parameter = explode("-",$month);

        //学期
        $model->tmp_semester = $parameter[0];

        //月
        $model->tmp_month = $parameter[1];

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

    //（武蔵野東から要望）ATTEND_SEMES_DATの授業日数（LESSON）を保持
    //　入学日付（ENT_DATE）卒業日付（GRD_DATE）が指定月の生徒　かつ　授業日数（LESSON）が1以上（0またはnull以外）の生徒
    function keepAttendSemes($year,$semester,$month,$sdate,$edate,$model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.LESSON ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.COPYCD = '0' ";
        $query .= "     AND T1.YEAR = '{$year}' ";
        $query .= "     AND T1.MONTH = '{$month}' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        $query .= "     AND T1.LESSON > 0 ";
        $query .= "     AND (EXISTS ( ";
        $query .= "         SELECT  'X' FROM SCHREG_BASE_MST B1 ";
        $query .= "         WHERE   B1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "               ((B1.GRD_DIV IN('1','2','3','6')     AND B1.GRD_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')) OR ";
        $query .= "                (B1.ENT_DIV IN('1','2','3','4','5') AND B1.ENT_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}'))) ";
        $query .= "                 ) ";
        //復学前の在籍を含む
        $query .= "          OR EXISTS (";
        $query .= "             SELECT  'X' FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT B1 ";
        $query .= "             WHERE   B1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                     ('{$sdate}' BETWEEN B1.ENT_DATE AND B1.GRD_DATE ";
        $query .= "                     OR '{$edate}' BETWEEN B1.ENT_DATE AND B1.GRD_DATE) ";
        $query .= "                    ) ";
        $query .= "         ) ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND T1.SCHREGNO IN (".knjd110dQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        return $query;
    }
    //（武蔵野東から要望）保持したATTEND_SEMES_DATの授業日数（LESSON）で更新
    function updAttendSemes($year,$semester,$month,$schregno,$lesson) {
        $query  = " UPDATE ATTEND_SEMES_DAT ";
        $query .= "    SET LESSON       = {$lesson} ";
        $query .= "       ,REGISTERCD   = '".STAFFCD."' ";
        $query .= "       ,UPDATED      = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     COPYCD = '0' ";
        $query .= "     AND YEAR = '{$year}' ";
        $query .= "     AND MONTH = '{$month}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        return $query;
    }

    function InsertSemesterQuery($year,$semester,$month,$sdate,$edate,$day,$model) {
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
        //共通・・・出欠および名簿(chair_std_dat)と時間割(sch_chr_dat)の表
        $query .= knjd110dQuery::getCommonAttendAndChairQuery($year,$semester,$sdate,$edate,"DAY");

        $query .= ", LESSON_DATA_ALL AS (";
        $query .= "     SELECT ";
        $query .= "         1 AS TABLESEQ, ";
        $query .= "         T1.LESSON, ";
        $query .= "         L1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_LESSON_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "              AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "              AND T1.GRADE = L1.GRADE ";
        $query .= "              AND T1.HR_CLASS = L1.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$year}' ";
        $query .= "         AND T1.MONTH = '{$month}' ";
        $query .= "         AND T1.SEMESTER = '{$semester}' ";
        //転学者について、転学後のレコードは作成しない。転学月は作成する。
        //転学者などを除く
        //転学(2)・退学(3)者 異動日が指定開始日より小さい場合
        //転入(4)・編入(5)者 異動日が指定終了日より大きい場合
        $query .= "         AND (NOT EXISTS ( ";
        $query .= "             SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "             WHERE   T6.SCHREGNO = L1.SCHREGNO AND ";
        $query .= "                   ((T6.GRD_DIV IN('1','2','3','6') AND T6.GRD_DATE < DATE('{$sdate}')) OR ";
        $query .= "                    (T6.ENT_DIV IN('1','2','3','4','5') AND T6.ENT_DATE > DATE('{$edate}'))) ";
        $query .= "                         ) ";
        //復学前の在籍を含む
        $query .= "             OR EXISTS  (";
        $query .= "             SELECT  'X' FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T6 ";
        $query .= "             WHERE   T6.SCHREGNO = L1.SCHREGNO AND ";
        $query .= "                        ('{$sdate}' BETWEEN T6.ENT_DATE AND T6.GRD_DATE ";
        $query .= "                        OR '{$edate}' BETWEEN T6.ENT_DATE AND T6.GRD_DATE) ";
        $query .= "                        ) ";
        $query .= "            ) ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         2 AS TABLESEQ, ";
        $query .= "         T1.LESSON, ";
        $query .= "         L1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         ATTEND_LESSON_MST T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "              AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "              AND T1.GRADE = L1.GRADE ";
        $query .= "              AND T1.COURSECD = L1.COURSECD ";
        $query .= "              AND T1.MAJORCD = L1.MAJORCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$year}' ";
        $query .= "         AND T1.MONTH = '{$month}' ";
        $query .= "         AND T1.SEMESTER = '{$semester}' ";
        //転学者について、転学後のレコードは作成しない。転学月は作成する。
        //転学者などを除く
        //転学(2)・退学(3)者 異動日が指定開始日より小さい場合
        //転入(4)・編入(5)者 異動日が指定終了日より大きい場合
        $query .= "         AND (NOT EXISTS ( ";
        $query .= "             SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "             WHERE   T6.SCHREGNO = L1.SCHREGNO AND ";
        $query .= "                   ((T6.GRD_DIV IN('1','2','3','6') AND T6.GRD_DATE < DATE('{$sdate}')) OR ";
        $query .= "                    (T6.ENT_DIV IN('1','2','3','4','5') AND T6.ENT_DATE > DATE('{$edate}'))) ";
        $query .= "                         ) ";
        //復学前の在籍を含む
        $query .= "             OR EXISTS  (";
        $query .= "             SELECT  'X' FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T6 ";
        $query .= "             WHERE   T6.SCHREGNO = L1.SCHREGNO AND ";
        $query .= "                        ('{$sdate}' BETWEEN T6.ENT_DATE AND T6.GRD_DATE ";
        $query .= "                        OR '{$edate}' BETWEEN T6.ENT_DATE AND T6.GRD_DATE) ";
        $query .= "                        ) ";
        $query .= "             ) ";
        $query .= "     UNION ALL ";
        //ATTEND_LESSON_MST 'すべて'
        $query .= "     SELECT ";
        $query .= "         3 AS TABLESEQ, ";
        $query .= "         T1.LESSON, ";
        $query .= "         L1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         ATTEND_LESSON_MST T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "              AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "              AND T1.GRADE = L1.GRADE ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$year}' ";
        $query .= "         AND T1.MONTH = '{$month}' ";
        $query .= "         AND T1.SEMESTER = '{$semester}' ";
        $query .= "         AND T1.COURSECD = '0' ";
        $query .= "         AND T1.MAJORCD = '000' ";
        //転学者について、転学後のレコードは作成しない。転学月は作成する。
        //転学者などを除く
        //転学(2)・退学(3)者 異動日が指定開始日より小さい場合
        //転入(4)・編入(5)者 異動日が指定終了日より大きい場合
        $query .= "         AND (NOT EXISTS ( ";
        $query .= "             SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "             WHERE   T6.SCHREGNO = L1.SCHREGNO AND ";
        $query .= "                   ((T6.GRD_DIV IN('1','2','3','6') AND T6.GRD_DATE < DATE('{$sdate}')) OR ";
        $query .= "                    (T6.ENT_DIV IN('1','2','3','4','5') AND T6.ENT_DATE > DATE('{$edate}'))) ";
        $query .= "                         ) ";
        //復学前の在籍を含む
        $query .= "             OR EXISTS (";
        $query .= "             SELECT  'X' FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T6 ";
        $query .= "             WHERE   T6.SCHREGNO = L1.SCHREGNO AND ";
        $query .= "                        ('{$sdate}' BETWEEN T6.ENT_DATE AND T6.GRD_DATE ";
        $query .= "                        OR '{$edate}' BETWEEN T6.ENT_DATE AND T6.GRD_DATE) ";
        $query .= "                       ) ";
        $query .= "             ) ";
        $query .= "     UNION ALL ";
        //訪問生
        $query .= "     SELECT ";
        $query .= "         0 AS TABLESEQ, ";
        $query .= "         DATE('{$edate}') - DATE('{$sdate}') + 1 - T1.HOLIDAY_CNT AS LESSON, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             S1.SCHREGNO, ";
        $query .= "             COUNT(S2.HOLIDAY_FLG) AS HOLIDAY_CNT ";
        $query .= "         FROM ";
        $query .= "             SCHREG_BASE_YEAR_DETAIL_MST S1, ";
        $query .= "             EVENT_SCHREG_DAT S2 ";
        $query .= "         WHERE ";
        $query .= "             S1.YEAR         = '{$year}' AND ";
        $query .= "             S1.BASE_SEQ     = '004' AND ";
        $query .= "             S1.BASE_REMARK2 = '1' AND ";
        $query .= "             S1.SCHREGNO     = S2.SCHREGNO AND ";
        $query .= "             S2.EXECUTEDATE BETWEEN '{$sdate}' AND '{$edate}' ";
        $query .= "         GROUP BY ";
        $query .= "             S1.SCHREGNO ";
        $query .= "         ) T1 ";
        $query .= "     WHERE ";
        //転学者について、転学後のレコードは作成しない。転学月は作成する。
        //転学者などを除く
        //転学(2)・退学(3)者 異動日が指定開始日より小さい場合
        //転入(4)・編入(5)者 異動日が指定終了日より大きい場合
        $query .= "         (NOT EXISTS ( ";
        $query .= "             SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "             WHERE   T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                   ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < DATE('{$sdate}')) OR ";
        $query .= "                    (T6.ENT_DIV IN('1','2','3','4','5') AND T6.ENT_DATE > DATE('{$edate}'))) ";
        $query .= "                     ) ";
        //復学前の在籍を含む
        $query .= "             OR EXISTS (";
        $query .= "             SELECT  'X' FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T6 ";
        $query .= "             WHERE   T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                        ('{$sdate}' BETWEEN T6.ENT_DATE AND T6.GRD_DATE ";
        $query .= "                        OR '{$edate}' BETWEEN T6.ENT_DATE AND T6.GRD_DATE) ";
        $query .= "                       ) ";
        $query .= "         ) ";
        $query .= " ) ";
        $query .= ", LESSON_DATA AS (";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.LESSON ";
        $query .= "     FROM ";
        $query .= "         LESSON_DATA_ALL T1 ";
        $query .= "         INNER JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 T1.SCHREGNO, ";
        $query .= "                 MIN(TABLESEQ) AS TABLESEQ "; // 訪問生、ATTEND_SEMES_LESSON_DATの順に優先
        $query .= "             FROM ";
        $query .= "                 LESSON_DATA_ALL T1 ";
        $query .= "             GROUP BY ";
        $query .= "                 T1.SCHREGNO ";
        $query .= "         ) T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND T2.TABLESEQ = T1.TABLESEQ ";
        $query .= " ) ";

        //メイン
        $query .= "SELECT '0' ";
        $query .= "      ,'{$year}' ";
        $query .= "      ,'{$month}' ";
        $query .= "      ,'{$semester}' ";
        $query .= "      ,TT1.schregno ";
        $query .= "      ,'{$day}'";
        $query .= "      ,TT1.lesson ";
        $query .= "      ,0 AS offdays ";
        $query .= "      ,VALUE(TT2.absent,0)   AS absent ";
        $query .= "      ,VALUE(TT3.suspend,0)  AS suspend ";
        $query .= "      ,VALUE(TT4.mourning,0) AS mourning ";
        $query .= "      ,0 AS abroad ";
        $query .= "      ,VALUE(TT5.sick,0)     AS sick ";
        $query .= "      ,VALUE(TT6.notice,0)   AS notice ";
        $query .= "      ,VALUE(TT7.nonotice,0) AS nonotice ";
        $query .= "      ,VALUE(TT8.late,0)     AS late ";
        $query .= "      ,VALUE(TT9.early,0)    AS early ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM  ";
        /* 授業日数 */
        $query .= "      LESSON_DATA TT1 ";
        /* 公欠日数 */
        $query .= " LEFT JOIN (SELECT schregno, COUNT(attenddate) AS absent ";
        $query .= "              FROM T_attend_dat  ";
        $query .= "             WHERE di_cd IN ('1','8') ";
        $query .= "             GROUP BY schregno ";
        $query .= "     ) TT2 ON TT1.schregno = TT2.schregno ";
        /* 出停日数 */
        $query .= " LEFT JOIN (SELECT W1.schregno, COUNT(W1.attenddate) AS suspend ";
        $query .= "              FROM T_attend_dat W1 ";
        $query .= "             WHERE W1.di_cd IN ('2','9') ";
        $query .= "             GROUP BY W1.schregno ";
        $query .= "     ) TT3 ON TT1.schregno = TT3.schregno ";
        /* 忌引日数 */
        $query .= " LEFT JOIN (SELECT W1.schregno, COUNT(W1.attenddate) AS mourning ";
        $query .= "              FROM T_attend_dat W1 ";
        $query .= "             WHERE W1.di_cd IN ('3','10') ";
        $query .= "             GROUP BY W1.schregno ";
        $query .= "     ) TT4 ON TT1.schregno = TT4.schregno ";
        /* 病欠日数 */
        $query .= " LEFT JOIN (SELECT W1.schregno, COUNT(W1.attenddate) AS sick ";
        $query .= "              FROM T_attend_dat W1 ";
        $query .= "             WHERE W1.di_cd IN ('4','11') ";
        $query .= "             GROUP BY W1.schregno ";
        $query .= "     ) TT5 ON TT1.schregno = TT5.schregno ";
        /* 事故欠(届)日数 */
        $query .= " LEFT JOIN (SELECT W1.schregno, COUNT(W1.attenddate) AS notice ";
        $query .= "              FROM T_attend_dat W1 ";
        $query .= "             WHERE W1.di_cd IN ('5','12') ";
        $query .= "             GROUP BY W1.schregno ";
        $query .= "     ) TT6 ON TT1.schregno = TT6.schregno ";
        /* 事故欠(無)日数 */
        $query .= " LEFT JOIN (SELECT W1.schregno, COUNT(W1.attenddate) AS nonotice ";
        $query .= "              FROM T_attend_dat W1 ";
        $query .= "             WHERE W1.di_cd IN ('6','13') ";
        $query .= "             GROUP BY W1.schregno ";
        $query .= "     ) TT7 ON TT1.schregno = TT7.schregno ";
        /* 遅刻日数 */
        $query .= " LEFT JOIN (SELECT W1.schregno, COUNT(W1.attenddate) AS late ";
        $query .= "              FROM T_attend_dat W1 ";
        $query .= "             WHERE W1.di_cd IN ('15') ";
        $query .= "             GROUP BY W1.schregno ";
        $query .= "     ) TT8 ON TT1.schregno = TT8.schregno ";
        /* 早退日数 */
        $query .= " LEFT JOIN (SELECT W1.schregno, COUNT(W1.attenddate) AS early ";
        $query .= "              FROM T_attend_dat W1 ";
        $query .= "             WHERE W1.di_cd IN ('16') ";
        $query .= "             GROUP BY W1.schregno ";
        $query .= "     ) TT9 ON TT1.schregno = TT9.schregno ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   WHERE TT1.SCHREGNO IN (".knjd110dQuery::getSchregnoInSql($model, $year, $semester).") ";
        }

        return $query;
    }

    //SUBL or SUBM
    function InsertSemesterSubQuery($attendTableFlg,$year,$semester,$month,$sdate,$edate,$model) {
        if ($attendTableFlg == "SUBM") {
            $query  = "INSERT INTO ATTEND_SEMES_SUBM_DAT ";
        } else {
            $query  = "INSERT INTO ATTEND_SEMES_SUBL_DAT ";
        }
        //共通・・・出欠および名簿(chair_std_dat)と時間割(sch_chr_dat)の表
        $query .= knjd110dQuery::getCommonAttendAndChairQuery($year,$semester,$sdate,$edate,$attendTableFlg);
        //メイン
        $query .= " SELECT ";
        $query .= "     '0' ";
        $query .= "    ,'{$year}' ";
        $query .= "    ,'{$month}' ";
        $query .= "    ,'{$semester}' ";
        $query .= "    ,SCHREGNO ";
        $query .= "    ,DI_CD ";
        $query .= "    ,SUBL_CD ";
        if ($attendTableFlg == "SUBM") {
            $query .= "    ,SUBM_CD ";
        }
        $query .= "    ,COUNT(ATTENDDATE) AS CNT ";
        $query .= "    ,'".STAFFCD."' ";
        $query .= "    ,SYSDATE() ";
        $query .= " FROM ";
        $query .= "     T_attend_dat ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   WHERE SCHREGNO IN (".knjd110dQuery::getSchregnoInSql($model, $year, $semester).") ";
        }
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= "    ,DI_CD ";
        $query .= "    ,SUBL_CD ";
        if ($attendTableFlg == "SUBM") {
            $query .= "    ,SUBM_CD ";
        }
        return $query;
    }

    //共通・・・出欠の表
    function getCommonAttendAndChairQuery($year,$semester,$sdate,$edate,$attendTableFlg) {
        $query  = "";
        //ATTEND_DAY_DATの表
        $query .= " with T_attend_dat AS ( ";
        $query .= "     SELECT ";
        $query .= "         T0.SCHREGNO, ";
        $query .= "         T0.ATTENDDATE, ";
        $query .= "         T0.DI_CD ";
        if ($attendTableFlg == "SUBM" || $attendTableFlg == "SUBL") {
            $query .= "    ,T0.SUBL_CD ";
        }
        if ($attendTableFlg == "SUBM") {
            $query .= "    ,T0.SUBM_CD ";
        }
        $query .= "     FROM ";
        if ($attendTableFlg == "SUBM") {
            $query .= "     ATTEND_DAY_SUBM_DAT T0 ";
        } else if ($attendTableFlg == "SUBL") {
            $query .= "     ATTEND_DAY_SUBL_DAT T0 ";
        } else {
            $query .= "     ATTEND_DAY_DAT T0 ";
        }
        $query .= "     WHERE ";
        $query .= "         T0.ATTENDDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
        $query .= "         AND T0.YEAR = '{$year}' ";
        //転学者などの不在日数を除く
        //転学(2)・退学(3)者 異動日がATTENDDATEより小さい場合
        //転入(4)・編入(5)者 異動日がATTENDDATEより大きい場合
        $query .= "         AND (NOT EXISTS ( ";
        $query .= "             SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "             WHERE   T6.SCHREGNO = T0.SCHREGNO AND ";
        $query .= "                   ((T6.GRD_DIV IN('1','2','3','6') AND T6.GRD_DATE < T0.ATTENDDATE) OR ";
        $query .= "                    (T6.ENT_DIV IN('1','2','3','4','5') AND T6.ENT_DATE > T0.ATTENDDATE)) ";
        $query .= "                        ) ";
        //復学前の在籍を含む
        $query .= "              OR EXISTS (";
        $query .= "             SELECT  'X' FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T6 ";
        $query .= "             WHERE   T6.SCHREGNO = T0.SCHREGNO AND ";
        $query .= "                     T0.ATTENDDATE BETWEEN T6.ENT_DATE AND T6.GRD_DATE ";
        $query .= "                        ) ";
        $query .= "             ) ";
        $query .= "     ) ";

        return $query;
    }
} 
?>
