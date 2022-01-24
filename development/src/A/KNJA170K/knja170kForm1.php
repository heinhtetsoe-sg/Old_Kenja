<?php

require_once('for_php7.php');

class knja170kForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja170kForm1", "POST", "knja170kindex.php", "", "knja170kForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $opt = array();
        if (is_numeric($model->control["学期数"])) {
            for ($semeIdx = 0; $semeIdx < (int)$model->control["学期数"]; $semeIdx++) {
                $opt[] = array( "label" => $model->control["学期名"][$semeIdx + 1],
                                "value" => sprintf("%d", $semeIdx + 1));
            }
        }
        $model->field["OUTPUT"] = isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : CTRL_SEMESTER;
        $extra = "onChange=\"return btn_submit('knja170k');\"";
        $arg["data"]["OUTPUT"] = knjCreateCombo($objForm, "OUTPUT", $model->field["OUTPUT"], $opt, $extra, "1");

        if ($model->Properties["dispMTokuHouJituGrdMixChkRad"] == "1") {
            $arg["dispJituGrdMix"] = 1;
            //印刷指定ラジオ 1:クラス指定 2:個人指定 3:実クラス
            $opt_change = array(1, 2, 3);
            $model->field["CHANGE"] = ($model->field["CHANGE"] == "") ? "1" : $model->field["CHANGE"];
            $extra = array("id=\"CHANGE1\" onclick =\" return btn_submit('knja170k');\"", "id=\"CHANGE2\" onclick =\" return btn_submit('knja170k');\"", "id=\"CHANGE3\" onclick =\" return btn_submit('knja170k');\"");
            $radioArray = knjCreateRadio($objForm, "CHANGE", $model->field["CHANGE"], $extra, $opt_change, get_count($opt_change));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }

            //学年混合(チェックボックス)
            $extra  = $model->field["GAKUNEN_KONGOU"] == "1" ? "checked" : "";
            $extra .= " id=\"GAKUNEN_KONGOU\" onclick=\"return btn_submit('knja170k');\"";
            if ($model->field["CHANGE"] != "1") {
                $extra .= "disabled ";
            }
            $arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");
        } else {
            $arg["nodispJituGrdMix"] = "1";
            //印刷指定ラジオ 1:クラス指定 2:個人指定
            $opt_change = array(1, 2);
            $model->field["CHANGE"] = ($model->field["CHANGE"] == "") ? "1" : $model->field["CHANGE"];
            $extra = array("id=\"CHANGE1\" onclick =\" return btn_submit('knja170k');\"", "id=\"CHANGE2\" onclick =\" return btn_submit('knja170k');\"");
            $radioArray = knjCreateRadio($objForm, "CHANGE", $model->field["CHANGE"], $extra, $opt_change, get_count($opt_change));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //表示名称切替
        if ($model->field["CHANGE"] == 2) {
            $arg["data"]["LABEL1"] = '個人';
            $arg["data"]["LABEL2"] = '生徒';
        } else {
            $arg["data"]["LABEL1"] = 'クラス';
            $arg["data"]["LABEL2"] = 'クラス';
        }

        //クラスコンボ作成
        if ($model->field["CHANGE"] == 2) {
            $query = knja170kQuery::getAuth($model, $model->field["OUTPUT"]);
            $extra = " onChange=\"return btn_submit('read');\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja170kForm1.html", $arg);
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストtoリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{

    //一覧取得
    if ($model->field["CHANGE"] == 2) {
        $query = knja170kQuery::getStudent($model);
    } else {
        $query = knja170kQuery::getAuth($model, $model->field["OUTPUT"]);
    }
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();

    //出力対象リスト作成（左）
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move('right',{$model->field["CHANGE"]})\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", array(), $extra, 20);

    //対象リスト作成（右）
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move('left',{$model->field["CHANGE"]})\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt1, $extra, 20);

    //対象取消ボタン（全て）
    $extra = "style=\"height:20px;width:40px\"  onclick=\"move('rightall',{$model->field["CHANGE"]});\"";
    $arg["button"]["btn_right2"] = knjCreateBtn($objForm, "btn_right2", ">>", $extra);
    //対象選択ボタン（全て）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('leftall',{$model->field["CHANGE"]});\"";
    $arg["button"]["btn_left2"] = knjCreateBtn($objForm, "btn_left2", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('right',{$model->field["CHANGE"]});\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('left',{$model->field["CHANGE"]});\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJA170K");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "GAKKI", $model->field["OUTPUT"]);
    knjCreateHidden($objForm, "dispMTokuHouJituGrdMixChkRad", $model->Properties["dispMTokuHouJituGrdMixChkRad"]);
    knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);
    knjCreateHidden($objForm, "useFi_Hrclass", $model->Properties["useFi_Hrclass"]);
    knjCreateHidden($objForm, "use_finSchool_teNyuryoku_P", $model->Properties["use_finSchool_teNyuryoku_P"]);
}
