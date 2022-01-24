<?php

require_once('for_php7.php');

class knjl310qForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        /******************/
        /* コンボボックス */
        /******************/
        //入試制度
        $query = knjl310qQuery::getNameMst($model->ObjYear, "L003");
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分
        if (SCHOOLKIND == "P") {
            $nameCd = "LP24";
            $name   = "TESTDIV";
        } elseif (SCHOOLKIND == "J") {
            $nameCd = "L024";
            $name   = "TESTDIV";
        } else {
            $nameCd = "L045";
            $name   = "TESTDIV0";
        }
        $query = knjl310qQuery::getNameMst($model->ObjYear, $nameCd);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $name, $model->field[$name], $extra, 1);

        //データ種類
        $opt = array();
        $opt[] = array('label' => "1:願書",'value' => "1");
        if (SCHOOLKIND == "H") {
            $opt[] = array('label' => "2:調査書",'value' => "2");
        }
        if (SCHOOLKIND == "H") {
            $opt[] = array('label' => "3:活動実績",'value' => "3");
        }
        $extra = "";
        $arg["data"]["DATADIV"] = knjCreateCombo($objForm, "DATADIV", $model->field["DATADIV"], $opt, $extra, 1);

        //処理名
        $opt   = array();
        $opt[] = array("label" => "更新","value" => "1");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt, $extra, 1);

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        $extra = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        /****************/
        /* ラジオボタン */
        /****************/
        //出力取込種別 (1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /************/
        /* ファイル */
        /************/
        //ファイルからの取り込み
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        /**********/
        /* ボタン */
        /**********/
        //実行
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl310qindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl310qForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    if (SCHOOLKIND == 'J' && $name == 'TESTDIV') {
        $opt[] = array("label" => "-- 全て --", "value" => "99");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
