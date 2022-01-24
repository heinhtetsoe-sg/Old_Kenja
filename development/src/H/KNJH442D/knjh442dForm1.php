<?php
class knjh442dForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh442dForm1", "POST", "knjh442dindex.php", "", "knjh442dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //入直値をクリア
        if ($model->cmd == "change_grade") {
            $model->field["TENTATIVE_FLG"] = "";
            $model->field["PERCENTAGE"] = "";
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        $model->field["YEAR"] = ($model->field["YEAR"]) ? $model->field["YEAR"] : CTRL_YEAR;
        $model->field["SEMESTER"] = ($model->field["SEMESTER"]) ? $model->field["SEMESTER"] : CTRL_SEMESTER;

        //学年コンボ作成
        $query = knjh442dQuery::getGradeHrClass($model, "GRADE");
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //画面情報の取得
        $searchFlg = false;
        if ($model->cmd != "edit") {
            $query = knjh442dQuery::getAftRecommendationRankHead($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($Row) {
                $model->field["TENTATIVE_FLG"] = $Row["TENTATIVE_FLG"];
                $model->field["PERCENTAGE"] = $Row["PERCENTAGE"];
                $searchFlg = true;
            }
        } else {
            $searchFlg = true;
        }

        //チェックボックス作成
        makeChkBox($objForm, $arg, $model, $searchFlg);

        //リストToリスト作成
        makeTestList($objForm, $arg, $db, $model, 3, "進研模試", 2, "21", "29");
        makeTestList($objForm, $arg, $db, $model, 4, "スタディサポート", 2, "11", "19");
        makeTestList($objForm, $arg, $db, $model, 5, "駿台模試", 1, "01", "09");
        makeTestList($objForm, $arg, $db, $model, 6, "河合塾模試", 3, "41", "49");

        //評定の実力に対する割合
        if ($model->cmd != "edit") {
            $model->field["PERCENTAGE"] = ($model->field["PERCENTAGE"]) ? $model->field["PERCENTAGE"] : "80";
        }
        $extra = "";
        $arg["data"]["PERCENTAGE"] = knjCreateTextBox($objForm, $model->field["PERCENTAGE"], "PERCENTAGE", 3, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $model->firstFlg = false;  //初期処理終了

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh442dForm1.html", $arg);
    }
}

function makeChkBox(&$objForm, &$arg, $model, $searchFlg)
{
    //仮評定含める
    makeChkBox1($objForm, $arg, $model, "TENTATIVE_FLG", $extra, $searchFlg);

    //ベネッセ
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_BENESSE_TEST", $extra, $searchFlg);

    //スタディサポート
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_STUDY_SUP", $extra, $searchFlg);

    //駿台
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_SUNDAI", $extra, $searchFlg);

    //河合塾
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_KAWAI", $extra, $searchFlg);
}

function makeChkBox1(&$objForm, &$arg, $model, $name, $extra, $searchFlg)
{
    $eWk = "";
    if ($name == "TENTATIVE_FLG") {
        if (!$searchFlg) {
            $model->field[$name] = "1"; //初期値
        }
    } else {
        if ($model->firstFlg && is_null($model->field[$name])) {
            $model->field[$name] = "1"; //初期値
        }
    }
    if ($model->field[$name] != "") {
        $eWk = " checked";
    }
    $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra.$eWk);
}

function makeTestList(&$objForm, &$arg, $db, $model, $numbr, $titleName, $ghosyaCd, $searchCdStrt, $searchCdEnd)
{
    //対象クラスリストを作成する
    $query = knjh442dQuery::getTestType($model, $ghosyaCd, $searchCdStrt, $searchCdEnd);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:350px\" ondblclick=\"moven('left', 0, {$numbr})\"";
    $arg["data"]["CATEGORY_NAME".$numbr] = knjCreateCombo($objForm, "CATEGORY_NAME".$numbr, "", $opt1, $extra, 6);

    $arg["data"]["NAME_LIST".$numbr] = $titleName;

    //出力対象一覧リストを作成する
    $extra = "multiple style=\"width:350px\" ondblclick=\"moven('right', 0, {$numbr})\"";
    $arg["data"]["CATEGORY_SELECTED".$numbr] = knjCreateCombo($objForm, "CATEGORY_SELECTED".$numbr, "", array(), $extra, 6);

    $extra_rights = "style=\"height:20px;width:40px\" onclick=\"movesn('right', 0, {$numbr});\"";
    $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"movesn('left', 0, {$numbr});\"";
    $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"moven('right', 0, {$numbr});\"";
    $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"moven('left', 0, {$numbr});\"";
    //対象選択ボタンを作成する
    $arg["button"]["btn_rights".$numbr] = knjCreateBtn($objForm, "btn_rights".$numbr, ">>", $extra_rights);
    //対象取消ボタンを作成する
    $arg["button"]["btn_lefts".$numbr]  = knjCreateBtn($objForm, "btn_lefts".$numbr, "<<", $extra_lefts);
    //対象選択ボタンを作成する
    $arg["button"]["btn_right".$numbr] = knjCreateBtn($objForm, "btn_right".$numbr, "＞", $extra_right1);
    //対象取消ボタンを作成する
    $arg["button"]["btn_left".$numbr]  = knjCreateBtn($objForm, "btn_left".$numbr, "＜", $extra_left1);
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

function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJH442D");
    knjCreateHidden($objForm, "IMAGE_PATH", "/usr/local/development/src/image");
    knjCreateHidden($objForm, "SUBCLASS_GROUP", $model->subclassGroup);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "HID_CATEGORY_SELECTED3");
    knjCreateHidden($objForm, "HID_CATEGORY_SELECTED4");
    knjCreateHidden($objForm, "HID_CATEGORY_SELECTED5");
    knjCreateHidden($objForm, "HID_CATEGORY_SELECTED6");
    knjCreateHidden($objForm, "cmd");
}
