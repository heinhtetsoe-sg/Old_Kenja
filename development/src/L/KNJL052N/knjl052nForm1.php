<?php

require_once('for_php7.php');

class knjl052nForm1 {
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
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl052nQuery::getApplicantdiv($model);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], "", 1);

        //入試区分
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl052nQuery::getTestDiv($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1);

        //志望区分
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl052nQuery::getEntExamCourse($model);
        makeCmb($objForm, $arg, $db, $query, "TOTALCD", $model->field["TOTALCD"], "", 1);
        
        //受験科目
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl052nQuery::getTestSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->field["TESTSUBCLASSCD"], "", 1);

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        if($model->field["HEADER"] == "on") {
            $extra = "checked";
        } else {
            $extra = ($model->cmd == "") ? "checked" : "";
        }
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        /****************/
        /* ラジオボタン */
        /****************/
        //出力取込種別 (1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = "";
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
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl052nindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl052nForm1.html", $arg);
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
