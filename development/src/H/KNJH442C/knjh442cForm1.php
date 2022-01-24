<?php
class knjh442cForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh442cForm1", "POST", "knjh442cindex.php", "", "knjh442cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //帳票タイトルラジオボタン（1:指定校推薦、2:日大付属特別選抜）
        $opt = array(1, 2);
        $model->field["TITLEDIV"] = ($model->field["TITLEDIV"] == "") ? "1" : $model->field["TITLEDIV"];
        $extra = array("id=\"TITLEDIV1\" onClick=\"return btn_submit('knjh442c')\"", "id=\"TITLEDIV2\" onClick=\"return btn_submit('knjh442c')\"");
        $radioArray = knjCreateRadio($objForm, "TITLEDIV", $model->field["TITLEDIV"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //年組コンボ
        $extra = "onChange=\"return btn_submit('knjh442c');\"";
        $query = knjh442cQuery::getSelectGradeHrClass($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);


        //出欠集計範囲（月）
        $info = array();
        $opt = array();
        $value_flgS = $value_flgE = false;
        $query = knjh442cQuery::getSemester();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($i=$row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
                $year = ($i > 12) ? CTRL_YEAR + 1 : CTRL_YEAR;
                $month = sprintf("%02d", (($i > 12) ? $i - 12 : $i));
                $month_show = ($i > 12) ? $i - 12 : $i;

                // $label = $row["LABEL"].' '.(strlen($month_show) > 1 ? '' : "&nbsp;").$month_show.'月';
                $label = $year.'/'.$month_show;
                $value = $year.'-'.$month;

                //開始日
                $sdate = ($i == $row["S_MONTH"]) ? $row["SDATE"] : $year.'-'.$month.'-01';
                //終了日
                $last_day = $db->getOne("VALUES LAST_DAY(DATE('".$year."-".$month."-01'))");
                $edate = ($i == $row["E_MONTH"]) ? $row["EDATE"] : $last_day;

                // $opt[] = array('label' => $label,
                //                'value' => $value);
                $opt[] = $value;

                if ($model->field["S_MONTH"] == $value) {
                    $value_flgS = true;
                }
                if ($model->field["E_MONTH"] == $value) {
                    $value_flgE = true;
                }

                //月初め、末日情報格納
                $info[$row["VALUE"]][$month] = array("SDATE" => str_replace("-", "/", $sdate),
                                                     "EDATE" => str_replace("-", "/", $edate));
            }
        }
        $result->free();

        //重複行の削除
        $monthArr = array();
        $monthArr = array_unique($opt);
        $opt = array();
        foreach ($monthArr as $value) {
            list($year, $month) = explode('-', $value);
            $month_show = ($month > 12) ? $month - 12 : $month;
            $label = $year.'/'.$month_show;
            $opt[] = array("label" => $label, "value" => $value);
        }

        //初期値
        if ($model->cmd == "") {
            //開始月
            $semS = $db->getRow(knjh442cQuery::getSemester(), DB_FETCHMODE_ASSOC);
            $year = ($semS["S_MONTH"] > 12) ? CTRL_YEAR + 1 : CTRL_YEAR;
            $defaultS = $year.'-'.sprintf("%02d", $semS["S_MONTH"]);

            //終了月
            $semE = $db->getOne(knjh442cQuery::getSemester(CTRL_DATE));
            list($y, $m, $d) = explode('-', CTRL_DATE);
            $year = ($m > 12) ? CTRL_YEAR + 1 : CTRL_YEAR;
            $defaultE = $year.'-'.sprintf("%02d", $m);
        }

        //開始月コンボ
        // $extra = "onchange=\"return btn_submit('change');\"";
        // $extra = "";
        // $model->field["S_MONTH"] = ($model->field["S_MONTH"] && $value_flgS) ? $model->field["S_MONTH"] : $defaultS;
        // $arg["data"]["S_MONTH"] = knjCreateCombo($objForm, "S_MONTH", $model->field["S_MONTH"], $opt, $extra, "");
        $model->field["S_MONTH"] = ($model->field["S_MONTH"] && $value_flgS) ? $model->field["S_MONTH"] : $defaultS;
        knjCreateHidden($objForm, "S_MONTH", $model->field["S_MONTH"]);

        //終了月コンボ
        // $extra = "onchange=\"return btn_submit('change');\"";
        $extra = "";
        $model->field["E_MONTH"] = ($model->field["E_MONTH"] && $value_flgE) ? $model->field["E_MONTH"] : $defaultE;
        $arg["data"]["E_MONTH"] = knjCreateCombo($objForm, "E_MONTH", $model->field["E_MONTH"], $opt, $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        // テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh442cForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = "";
    $result = $db->query(knjh442cQuery::getList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //生徒一覧
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 15);

    //対象者一覧
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 15);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //実行ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //CSVボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

    //閉じるボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJH442C");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "HID_CATEGORY_SELECTED");
}
