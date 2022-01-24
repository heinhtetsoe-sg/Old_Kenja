<?php

require_once('for_php7.php');

class knjl371qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        
        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に
        
        $db = Query::dbCheckOut();
        
        //データ数カウント
        $query = knjl371qQuery::getCnt();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"] = $row;
        
        Query::dbCheckIn($db);

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //取込時エラーがあったら表示
        if(!empty($model->error)){
            $model->setMessage("登録されている願書データと合致しないデータがあります。");
            foreach($model->error as $val){
                $error["LINE"] = $val;
                $error["SAT_NO"] = $model->data_arr[$val]["SAT_NO"];
                $error["NAME"] = $model->data_arr[$val]["NAME"];
                $error["MOCK_AUG_NO"] = $model->data_arr[$val]["MOCK_AUG_NO"];
                $error["MOCK_SEP_NO"] = $model->data_arr[$val]["MOCK_SEP_NO"];
                
                $arg["err"][] = $error;
            }
        }
        
        //ボタン作成
        makeButton($objForm, $arg, $db, $model);


        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJl371q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl371qindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl371qForm1.html", $arg);
    }

}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "YEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    //取込ボタン
    $extra = "onclick=\"btn_submit('import');\"";
    $arg["button"]["btn_import"] = knjCreateBtn($objForm, "btn_import", "取 込", $extra);

    //出力ボタン
    $extra = "onclick=\"btn_submit('export');\"";
    $arg["button"]["btn_export"] = knjCreateBtn($objForm, "btn_export", "出 力", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
