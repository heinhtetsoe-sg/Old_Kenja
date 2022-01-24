<?php

require_once('for_php7.php');

class knja610Form1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knja610Form1", "POST", "knja610index.php", "", "knja610Form1");

        $opt=array();

        //年度コンボボックスを作成する/////////////////////////////////////
        $db = Query::dbCheckOut();
        $query = knja610Query::getSelectYear();
        $extra = "onchange=\"return btn_submit('init');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1);
        Query::dbCheckIn($db);

        //学期コンボボックスを作成する/////////////////////////////////////
        $opt_sem = array();
        $db = Query::dbCheckOut();
        $query = knja610Query::getSelectSeme($model->field["YEAR"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_sem[]= array('label' => $row["SEMESTERNAME"],
                              'value' => $row["SEMESTER"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($model->field["GAKKI"]=="") $model->field["GAKKI"] = $model->control["学期"];
        
        $extra = "onChange=\"btn_submit('init');\"";
        $arg["data"]["GAKKI"] = knjCreateCombo($objForm, "GAKKI", $model->field["GAKKI"], $opt_sem, $extra, 1);

        //テスト種別リスト
//      if(($model->tableName == 'TESTITEM_MST_COUNTFLG' && $model->field["GAKKI"] != "9") || $model->tableName == 'TESTITEM_MST_COUNTFLG_NEW'){
        if ($model->field["GAKKI"] != "9") {
            $query = knja610Query::getTestItem($model);
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_kind[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
            }
            Query::dbCheckIn($db);
        }

        if($model->tableName == 'TESTITEM_MST_COUNTFLG') {
            $opt_kind[] = array('label' => '評価成績', 'value' => '9900');
        } else if ($model->field["GAKKI"] == "9") {
            $opt_kind[] = array('label' => '学年評定', 'value' => '9900');
            $opt_kind[] = array('label' => '学年評価', 'value' => '9901');
        }

        //出欠集計日付
        $model->field["ATTENDDATE"] = ($model->field["ATTENDDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : $model->field["ATTENDDATE"];
        $arg["data"]["ATTENDDATE"] = View::popUpCalendar2($objForm, "ATTENDDATE", $model->field["ATTENDDATE"], "", "", "");

        //初期化処理
        $test_flg = false;
        for ($i=0; $i<get_count($opt_kind); $i++) {
            if ($model->field["TESTKINDCD"] == $opt_kind[$i]["value"]) $test_flg = true;
        }
        if (!$test_flg) $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

        if ($opt_kind != null) {
            $extra = "";
            $arg["data"]["TESTKINDCD"] = knjCreateCombo($objForm, "TESTKINDCD", $model->field["TESTKINDCD"], $opt_kind, $extra, 1);
        } else {
            $arg["data"]["TESTKINDCD"] = "該当データなし";
        }

        //クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
        $db = Query::dbCheckOut();
        $opt_class_left = $opt_class_right = array();
        $opt_class=array();
        $query = knja610Query::getAuth($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (!in_array($row["VALUE"], $model->select_data["selectdata"])) {
                $opt_class_right[]= array('label' => $row["LABEL"],
                                        'value' => $row["VALUE"]);
            } else {
                $opt_class_left[]= array('label' => $row["LABEL"],
                                        'value' => $row["VALUE"]);
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $value, $opt_class_right, $extra, 15);

        //出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
                            "options"    => $opt_class_left));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //csvボタンを作成する
        $extra = ($model->field["TESTKINDCD"]) ? "onclick=\"return btn_submit('csv');\"" : "disabled";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "ＣＳＶ出力", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);


        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        knjCreateHidden($objForm, "cmd");

        knjCreateHidden($objForm, "selectdata");


        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja610Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
