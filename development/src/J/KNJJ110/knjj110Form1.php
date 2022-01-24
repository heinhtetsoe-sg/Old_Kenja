<?php

require_once('for_php7.php');

class knjj110Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjj110Form1", "POST", "knjj110index.php", "", "knjj110Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjj110Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjj110');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHKIND"], "SCHKIND", $extra, 1, "");
        }

        //委員会一覧リスト作成する
        $query = knjj110Query::getIinkai($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["CFLG"]."-".$row["CCD"]."　".$row["LABEL"],
                            'value' => $row["CFLG"].$row["CCD"]);
        }
        $result->free();
        $extra = "multiple style=\"width:170px\" width:\"170px\" ondblclick=\"move1('left')\"";
        $arg["data"]["COMMI_NAME"] = knjCreateCombo($objForm, "COMMI_NAME", $value, isset($row1) ? $row1 : array(), $extra, 15);

        //出力対象クラスリストを作成する
        $extra = "multiple style=\"width:170px\" width:\"170px\" ondblclick=\"move1('right')\"";
        $arg["data"]["COMMI_SELECTED"] = knjCreateCombo($objForm, "COMMI_SELECTED", $value, array(), $extra, 15);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //対象学期
        $query = knjj110Query::getJ004($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["J004"], "J004", $extra, 1, "ALL");

        //帳票パターン
        $opt = array(1, 2);
        $model->field["PATTERN"] = ($model->field["PATTERN"] == "") ? "1" : $model->field["PATTERN"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PATTERN{$val}\" onclick=\"kubun();\"");
        }
        $radioArray = knjCreateRadio($objForm, "PATTERN", $model->field["PATTERN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //生年月日出力チェックボックス
        $extra = $model->field["PATTERN2_PRINT_BIRTHDAY"] == "1" ? "checked" : "";
        $extra .= " id=\"PATTERN2_PRINT_BIRTHDAY\" ";
        if ($model->field["PATTERN"] == "1") {
            $extra .= " disabled=\"disabled\" ";
        }
        $arg["PATTERN2_PRINT_BIRTHDAY"] = knjCreateCheckBox($objForm, "PATTERN2_PRINT_BIRTHDAY", "1", $extra, "");

        //CSV出力
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);


        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJJ110");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        $arg["data"]["GAKKI"] = $model->control["学期名"][CTRL_SEMESTER];

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjj110Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "-全て-", "value" => "ALL");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
