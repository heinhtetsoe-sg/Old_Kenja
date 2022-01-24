<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjd231vForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]    = $objForm->get_start("main", "POST", "knjd231vindex.php", "", "main");
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;
        //学期
        $arg["GAKKI"] = CTRL_SEMESTERNAME;
        //上限値
        $query = knjd231vQuery::getSyuutokuJougenti($model);
        $row_jougen = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //修得上限値
        $arg["SYUTOKU_JOUGENTI"] = $row_jougen["SYUTOKU_JOUGENTI"];
        //履修上限値
        $arg["RISYU_JOUGENTI"]   = $row_jougen["RISYU_JOUGENTI"];

        /******************/
        /* コンボボックス */
        /******************/
        //学期
        $opt_seme = array();
        $query = knjd231vQuery::getSelectSeme();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_seme[] = array('label' => $row["SEMESTERNAME"],
                                'value' => $row["SEMESTER"]);
        }
        $result->free();
        if ($model->field["GAKKI2"] == "") $model->field["GAKKI2"] = '9';
        $extra = "";
        $arg["GAKKI2"] = knjCreateCombo($objForm, "GAKKI2", $model->field["GAKKI2"], $opt_seme, $extra, 1);
        //学年
        $opt_grade = array();
        $query = knjd231vQuery::getSelectGrade($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grade[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();
        $opt_grade[] = array('label' => "全て", 'value' => "99");
        if ($model->field["GRADE"] == "") $model->field["GRADE"] = $opt_grade[0]["value"];
        $extra = "";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt_grade, $extra, 1);
        //科目
        $opt_subclasscd = array();
        $opt_subclasscd[] = array('label' => "", 'value' => "");
        $query = knjd231vQuery::getSubclasscd($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_subclasscd[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
        $result->free();
        if ($model->field["SUBCLASSCD"] == "") $model->field["SUBCLASSCD"] = $opt_subclasscd[0]["value"];
        $extra = "";
        $arg["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $model->field["SUBCLASSCD"], $opt_subclasscd, $extra, 1);

        /********/
        /* 日付 */
        /********/
        //異動対象日付
        $date = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["DATE"]=View::popUpCalendar($objForm, "DATE", $date);

        /********************/
        /* チェックボックス */
        /********************/
        //教科・科目/総合的な時間
        if (!$model->field["KYOUKA_SOUGOU1"] && !$model->field["KYOUKA_SOUGOU2"]) {
            $model->field["KYOUKA_SOUGOU1"] = '1';
        }
        $extra = ($model->field["KYOUKA_SOUGOU1"] == "1") ? $extra = "checked='checked' id=\"KYOUKA_SOUGOU1\"" : "id=\"KYOUKA_SOUGOU1\"";
        $arg["KYOUKA_SOUGOU1"] = knjCreateCheckBox($objForm, "KYOUKA_SOUGOU1", "1", $extra);
        $extra = ($model->field["KYOUKA_SOUGOU2"] == "1") ? $extra = "checked='checked' id=\"KYOUKA_SOUGOU2\"" : "id=\"KYOUKA_SOUGOU2\"";
        $arg["KYOUKA_SOUGOU2"] = knjCreateCheckBox($objForm, "KYOUKA_SOUGOU2", "1", $extra);

        //成績不振者
        $extra = ($model->field["SEISEKI_HUSIN1"] == "1" || !strlen($model->cmd)) ? "checked='checked' id=\"SEISEKI_HUSIN1\"" : "id=\"SEISEKI_HUSIN1\"";
        $arg["SEISEKI_HUSIN1"] = knjCreateCheckBox($objForm, "SEISEKI_HUSIN1", "1", $extra);
        $extra = ($model->field["SEISEKI_HUSIN2"] == "1") ? "checked='checked' id=\"SEISEKI_HUSIN2\"" : "id=\"SEISEKI_HUSIN2\"";
        $arg["SEISEKI_HUSIN2"] = knjCreateCheckBox($objForm, "SEISEKI_HUSIN2", "1", $extra);
        $extra = ($model->field["SEISEKI_HUSIN3"] == "1") ? "checked='checked' id=\"SEISEKI_HUSIN3\"" : "id=\"SEISEKI_HUSIN3\"";
        $arg["SEISEKI_HUSIN3"] = knjCreateCheckBox($objForm, "SEISEKI_HUSIN3", "1", $extra);

        /**********/
        /* その他 */
        /**********/
        //評定 or 評価
        if ($model->field["GAKKI2"] == 9) {
            $arg["HYOUTEI_OR_HYOUKA"] = '評定';
        } else {
            $arg["HYOUTEI_OR_HYOUKA"] = '評価';
        }

        /********************/
        /* テキストボックス */
        /********************/
        //成績不振者 教科・科目
        $extra = "style=\"text-align: right\" onblur=\"this.value=toFloat(this.value)\"";
        $value = (!strlen($model->cmd)) ? '1' : $model->field["SEISEKI_HUSIN_HYOUTEI_FROM"];
        $arg["SEISEKI_HUSIN_HYOUTEI_FROM"] = knjCreateTextBox($objForm, $value, "SEISEKI_HUSIN_HYOUTEI_FROM", 3, 3, $extra);
        $value = (!strlen($model->cmd)) ? '1' : $model->field["SEISEKI_HUSIN_HYOUTEI_TO"];
        $arg["SEISEKI_HUSIN_HYOUTEI_TO"] = knjCreateTextBox($objForm, $value, "SEISEKI_HUSIN_HYOUTEI_TO", 3, 3, $extra);

        /************/
        /* 一覧表示 */
        /************/
        if ($model->cmd == 'read') {
            $attend_sdate = "";
            $attend_seme = "";
            $attend_month = array();
            $query = knjd231vQuery::getAttendDate($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $tmp_attend_sdate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
                if (str_replace("/","-",$model->field["DATE"]) < $tmp_attend_sdate) break;
                $attend_month[] = $row["MONTH"];
                $attend_sdate = $tmp_attend_sdate;
                $attend_seme = $row["SEMESTER"];
            }
            $result->free();
            if ($attend_sdate == "") {
                $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='1'";
                $attend_sdate = $db->getOne($query2);   //学期開始日
            } else {
                $query2 = "VALUES Add_days(date('".$attend_sdate."'), 1)";
                $attend_sdate = $db->getOne($query2);   //次の日
            }

            //校種、学校コード
            $schoolcd = $school_kind = "";
            if ($db->getOne(knjd231vQuery::checkSchoolMst()) > 0) {
                $schoolcd = sprintf("%012d", SCHOOLCD);
                if ($model->Properties["useCurriculumcd"] == '1' && $model->field["SUBCLASSCD"] != "") {
                    $school_kind = SCHOOLKIND;
                } else {
                    $school_kind = $db->getOne(knjd231vQuery::getSchoolKind($model));
                }
            }

            //SCHOOL_MSTの情報を取得。
            $year = CTRL_YEAR;
            $knjSchoolMst = AttendAccumulate::getSchoolMstMap($db, $year, $schoolcd, $school_kind);

            $query = knjd231vQuery::selectListQuery($model, $attend_seme, $attend_month, $attend_sdate, $knjSchoolMst);
            $result = $db->query($query);
            $dataFlg = false;
            $counter = 0;
            $colorFlg = false;
            $model->data=array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //学籍番号を配列で取得
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $model->data["SCHREGNO"][] = $row["SCHREGNO"]."-".$row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
                } else {
                    $model->data["SCHREGNO"][] = $row["SCHREGNO"]."-".$row["SUBCLASSCD"];
                }
                //氏名欄に学籍番号表記
                if ($model->Properties["use_SchregNo_hyoji"] == 1) {
                    $row["SCHREGNO_SHOW"] = $row["SCHREGNO"] . "　";
                }
                //５行毎に背景色を変える
                if ($counter % 5 == 0) {
                    $colorFlg = !$colorFlg;
                }
                $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
                //クラス-出席番(表示)
                if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                    $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
                }
                //テキストボックスを作成
                $extra = "style=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"this.value=toInteger(this.value)\" ";
                $name = "SCORE";
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 3, 3, $extra);
                $name = "COMP_CREDIT";
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 3, 3, $extra);
                $name = "GET_CREDIT";
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 3, 3, $extra);
                //更新チェックボックス
                $name = "UPDATE_DATA";
                $extra = "";
                $row[$name] = knjCreateCheckBox($objForm, $name."-".$counter, "1", $extra);

                //訂正後データは、背景色をピンク表示
                $query = knjd231vQuery::getExistsRecordScoreBefAftDat($model, $row);
                $row["BGCOLOR"] = (0 < $db->getOne($query)) ? "#ffc0cb" : $row["COLOR"];

                $dataFlg = true;
                $counter++;
                $arg["data"][] = $row;
            }
        }


        /**********/
        /* ボタン */
        /**********/
        //読 込
        $extra = "onclick=\"return btn_submit('read');\"";
        $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
        //印刷ボタン
        $disBtn = ($counter > 0) ? "" : " disabled";
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '1');\"";
        $arg["btn_print1"] = knjCreateBtn($objForm, "btn_print1", "訂正前/印刷", $extra.$disBtn);
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '2');\"";
        $arg["btn_print2"] = knjCreateBtn($objForm, "btn_print2", "訂正後/印刷", $extra.$disBtn);
        //更 新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disBtn);
        //取 消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$disBtn);
        //終 了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        //印刷パラメータ
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD231V");
        knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "chikokuHyoujiFlg", $model->Properties["chikokuHyoujiFlg"]);
        knjCreateHidden($objForm, "PRINT_DIV");
        knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);

        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd231vForm1.html", $arg);
    }
}
?>
