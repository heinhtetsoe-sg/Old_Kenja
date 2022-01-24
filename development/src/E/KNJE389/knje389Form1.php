<?php

require_once('for_php7.php');

class knje389Form1 {
    function main(&$model) {
        $objForm = new form;
        //今年度・今学期名及びタイトルの表示
        $arg["data"]["TITLE"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";
        //DB接続
        $db = Query::dbCheckOut();

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        if($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header);

        /****************/
        /* ラジオボタン */
        /****************/
        //出力取込種別 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力順 1:総学力点順 2:年組番号順
        $opt = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********/
        /* FILE */
        /********/
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        /**********/
        /* コンボ */
        /**********/
        //処理名(更新のみ)
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        //$opt_shori[]    = array("label" => "削除","value" => "2");
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", 1);
        //年度&学期
        $model->field["YEAR_SEMESTER"] = ($model->field["YEAR_SEMESTER"] == "") ? CTRL_YEAR.'-'.CTRL_SEMESTER : $model->field["YEAR_SEMESTER"];
        $query = knje389query::getYearSmester();
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR_SEMESTER", $model->field["YEAR_SEMESTER"], $extra, 1);
        //学年
        $query = knje389query::getGrade($model);
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        /**********/
        /* ボタン */
        /**********/
        //実行
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('exec');\"");
        //終了
        if($model->sendPrgId == "KNJE387"){
            // $link = REQUESTROOT."/E/KNJE387/knje387index.php?cmd=main&SEND_PRGID={$model->sendPrgId}&SEND_AUTH={$model->auth}";
            $link = REQUESTROOT."/E/KNJE387/knje387index.php?cmd=main";
            $extra = "onclick=\"window.open('$link','_self');\"";
            $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
        } else {
            $extra = "onclick=\"closeWin();\"";
            $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje389index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje389Form1.html", $arg);
    }
}

/******************************************************************************************************/
/******************************************************************************************************/
/******************************************************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank == "ALL")   $opt[] = array('label' => "(全て出力)", 'value' => "999999");
    if($blank == "BLANK") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg['data'][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
