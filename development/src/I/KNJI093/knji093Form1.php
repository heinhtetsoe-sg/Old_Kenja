<?php

require_once('for_php7.php');

class knji093Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knji093Form1", "POST", "knji093index.php", "", "knji093Form1");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["data"]["SCHCMB"] = "";
        //校種コンボ
        if ($model->Properties["useSchool_KindField"] == "1") {
            $extra = " onchange=\"return btn_submit('changeSchKind');\" ";
            $arg["data"]["SCHCMB"] = "1";
            $query = knji093Query::getSchoolKind($model);
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
        }

        //卒業年度コンボ
        $opt = array();
        $query = knji093Query::getYear();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[]= array('label' => $row["YEAR"]."年度卒",
                          'value' => $row["YEAR"]);
        }
        $result->free();

        if ($model->field["YEAR"] == "") $model->field["YEAR"] = CTRL_YEAR;

        $extra = "onChange=\"return btn_submit('knji093');\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        //卒業見込み出力チェックボックス
        $extra  = ($model->field["MIKOMI"] == "on") ? "checked" : "";
        $extra .= " id=\"MIKOMI\"";
        $arg["data"]["MIKOMI"] = knjCreateCheckBox($objForm, "MIKOMI", "on", $extra, "");

        //学期コード
        $query = knji093Query::selectGradeSemesterDiv($model);
        $semesterdiv = $db->getOne($query);
        $semester = isset($semesterdiv) ? $semesterdiv : "3";   //データ無しはデフォルトで３学期を設定

        if ($model->field["YEAR"] == CTRL_YEAR) {
            $model->field["GAKKI"] = CTRL_SEMESTER;
        } else {
            $model->field["GAKKI"] = $semester;
        }
        knjCreateHidden($objForm, "GAKKI", $model->field["GAKKI"]);

        $arg["CHK"] = "";
        if ($model->Properties["useSchool_KindField"] == "1" && in_array($model->field["SCHOOL_KIND"], array("A", "H"))) {
            //クラス、学科

            $arg["CHK"] = "1";
            $opt = array(1, 2); //1:クラス 2:学科
            $model->field["CLASS_MAJOR"] = ($model->field["CLASS_MAJOR"] == "") ? "1" : $model->field["CLASS_MAJOR"];
            $extra = array("id=\"CLASS_MAJOR1\" onClick=\"return btn_submit('knji093');\"", "id=\"CLASS_MAJOR2\" onClick=\"return btn_submit('knji093');\"");
            $radioArray = knjCreateRadio($objForm, "CLASS_MAJOR", $model->field["CLASS_MAJOR"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
            if ($model->field["CLASS_MAJOR"] == '1') {
                $arg["CLASS_MAJOR_NAME"] = "クラス";
            } else if ($model->field["CLASS_MAJOR"] == '2') {
                $arg["CLASS_MAJOR_NAME"] = "学科";
            }

            if (($model->Properties["useSchool_KindField"] == "1" && !in_array(SCHOOLKIND, array("A", "H")))
                || $model->field["SCHOOL_KIND"] != "" && !in_array($model->field["SCHOOL_KIND"], array("A", "H"))) {
                $arg["DISP_MAJOR"] = "";
            } else {
                $arg["DISP_MAJOR"] = "1";
            }
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "date", date('Y-m-d'));
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJI093");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFormNameI093", $model->Properties["useFormNameI093"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "selectedSchKind", $model->field["SCHOOL_KIND"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knji093Form1.html", $arg);
    }
}

//コンボ作成関数
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧取得
    $opt = array();
    $query = knji093Query::getAuth($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $opt, $extra, 15);

    //出力対象クラス
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}
?>
