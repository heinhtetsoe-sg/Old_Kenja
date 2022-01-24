<?php

require_once('for_php7.php');

class knjl014tForm1 {
    function main(&$model) {
        $objForm = new form;

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR"]         = $model->test_year;

        //DB接続
        $db = Query::dbCheckOut();

        /******************/
        /* コンボボックス */
        /******************/
        //処理名
        $opt   = array();
        $opt[] = array("label" => "更新","value" => "1");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt, $extra, 1);

        //入試制度
        $query = knjl014tQuery::getApplicantdiv($model);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], "", 1);

        /********************/
        /* チェックボックス */
        /********************/
        //追検査者のみ
        if ($model->field["SUPPLEMENT"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id=\"SUPPLEMENT\"";
        $arg["data"]["SUPPLEMENT"] = knjCreateCheckBox($objForm, "SUPPLEMENT", "1", $extra);

        /****************/
        /* ラジオボタン */
        /****************/
        //出力順
        $opt = array(1, 2); //(1:受検番号 2:出身学校)
        $model->field["SYUTURYOKUJUN"] = ($model->field["SYUTURYOKUJUN"] == "") ? "1" : $model->field["SYUTURYOKUJUN"];
        $extra = array("id=\"SYUTURYOKUJUN1\"", "id=\"SYUTURYOKUJUN2\"");
        $radioArray = knjCreateRadio($objForm, "SYUTURYOKUJUN", $model->field["SYUTURYOKUJUN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        if($model->field["HEADER"] == "on") {
            $extra = "checked";
        } else {
            $extra = ($model->cmd == "") ? "checked" : "";
        }
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        /****************/
        /* ラジオボタン */
        /****************/
        //出力取込種別 (1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

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

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl014tindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl014tForm1.html", $arg);
    }
}
//コンボ作成
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

    if (!strlen($opt[0]["value"])) {
        $opt[] = array('label' => '　　　　', 'value' => '');
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
