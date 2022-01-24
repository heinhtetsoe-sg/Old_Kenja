<?php

require_once('for_php7.php');

class knje360SubForm6_2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform6_2", "POST", "knje360index.php", "", "subform6_2");

        //DB接続
        $db = Query::dbCheckOut();

        // 入試カレンダーの使用フラグ
        $arg["useCollegeExamCalendar"] = "";
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $arg["useCollegeExamCalendar"] = "1";
        }

        //生徒情報
        $info = $db->getRow(knje360Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //検索方法（1:学校名, 2:学校コード）
        $opt = array(1, 2);
        $model->field["SEARCH_DIV"] = ($model->field["SEARCH_DIV"] == "") ? "1" : $model->field["SEARCH_DIV"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SEARCH_DIV{$val}\" onClick=\"btn_submit('replace6')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEARCH_DIV", $model->field["SEARCH_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        if ($model->field["SEARCH_DIV"] == "1") {
            //学校名入力
            $arg["SEARCH_DIV_1"] = "1";
            $arg["SEARCH_DIV_2"] = "";
            $extra = "onkeydown=\"return keydownEvent('replace6_search');\"";
            $arg["SEARCH_TXT"] = knjCreateTextBox($objForm, $model->replace["field"]["SEARCH_TXT"], "SEARCH_TXT", 20, 20, $extra);
        } else {
            //学校コード入力
            $arg["SEARCH_DIV_1"] = "";
            $arg["SEARCH_DIV_2"] = "1";
            $extra = "onkeydown=\"return keydownEvent('replace6_search');\" onblur=\"this.value=toInteger2(this.value);\" id=\"eisuFuka\"";
            $arg["SEARCH_NO"] = knjCreateTextBox($objForm, $model->replace["field"]["SEARCH_NO"], "SEARCH_NO", 26, "", $extra);
        }

        //登録日
        $model->replace["field"]["TOROKU_DATE"] = ($model->replace["field"]["TOROKU_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $model->replace["field"]["TOROKU_DATE"]);
        $arg["TOROKU_DATE"] = View::popUpCalendar($objForm, "TOROKU_DATE", $model->replace["field"]["TOROKU_DATE"]);

        //ALLチェック
        $extra  = "onClick=\"return check_all(this);\" id=\"CHECKALL\"";
        $extra .= ($model->replace["check_all"] == "1") ? " checked" : "";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "1", $extra, "");

        //選択済みの学校取得
        $selected = $db->getCol(knje360Query::getSelectedSchool($model));

        //データを取得
        $counter = 0;
        if ($model->schregno != "" && $model->cmd != "replace6" && ($model->replace["field"]["SEARCH_TXT"] || $model->replace["field"]["SEARCH_NO"])) {
            $query = knje360Query::getSubQuery6_2($model);
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row["EXAM_DATE"]         = makeDate2($row["EXAM_DATE"]);
                    $row["EXAM_PASS_DATE"]    = makeDate2($row["EXAM_PASS_DATE"]);
                    $row["LIMIT_DATE_WINDOW"] = makeDate2($row["LIMIT_DATE_WINDOW"]);
                    $row["LIMIT_DATE_MAIL"]   = makeDate2($row["LIMIT_DATE_MAIL"]);
                    $val  = $row["SCHOOL_CD"].':'.$row["FACULTYCD"].':'.$row["DEPARTMENTCD"].':'.$row["ADVERTISE_DIV"].':'.$row["PROGRAM_CD"].':'.$row["FORM_CD"].':'.$row["S_CD"];
                    $val .= ':'.$row["EXAM_DATE"].':'.$row["EXAM_PASS_DATE"].':'.$row["PREF_CD"].':'.$row["L_CD"].':'.$row["LIMIT_DATE_WINDOW"].':'.$row["LIMIT_DATE_MAIL"].':'.$row["LIMIT_MAIL_DIV"];
                    $val2 = $row["SCHOOL_CD"].':'.$row["FACULTYCD"].':'.$row["DEPARTMENTCD"].':'.$row["ADVERTISE_DIV"].':'.$row["PROGRAM_CD"].':'.$row["FORM_CD"].':'.$row["S_CD"];
                    if (is_array($model->replace["data_chk"])) {
                        $extra = (in_array($val, $model->replace["data_chk"])) ? "checked" : "";
                    } else {
                        $extra = "";
                    }

                    $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $val, $extra, "1");

                    //背景色
                    if (in_array($val2, $selected)) {
                        $row["BGCOLOR"] = "yellow";
                    } else {
                        $row["BGCOLOR"] = ($counter % 2 == 0) ? "#ffffff" : "#cccccc";
                    }

                    $arg["data"][] = $row;
                    $counter++;
                }
                $result->free();
            }
        }

        //高さ調整
        $arg["HEIGHT"] = "height:370;";

        //検索結果件数
        $arg["COUNTER"] = $counter;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360SubForm6_2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //検索ボタン
    $extra = "onclick=\"return btn_submit('replace6_search');\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);

    //登録ボタン
    $extra = "onclick=\"return doSubmit()\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('subform6A');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "受験報告（進学）一括入力へ", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "cmd");

    $semes = $db->getRow(knje360Query::getSemesterMst(), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
    knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));
}

//日付チェック２
function makeDate2($monthDay)
{
    if ($monthDay == "") {
        return "";
    }
    if (strlen($monthDay) != 4) {
        return "";
    }
    $month = substr($monthDay, 0, 2);
    $day = substr($monthDay, 2);
    $year = ((int)$month * 1) < 4 ? CTRL_YEAR + 1 : CTRL_YEAR;
    if (checkdate($month, $day, $year)) {
        return $year."-".$month."-".$day;
    } else {
        return "";
    }
}
