<?php

require_once('for_php7.php');

class knjl381qForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に

        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl381qindex.php", "", "edit");

        //データベース接続
        $db = Query::dbCheckOut();

        
        //科目選択ラジオ
        $opt = array(1, 2, 3);
        $extra = array("id=\"Radio1\" onclick=\"gamen_reload('1');\"", "id=\"Radio2\" onclick=\"gamen_reload('2');\"", "id=\"Radio3\" onclick=\"gamen_reload('3');\"");
        $label = array("Radio1" => "英語", "Radio2" => "国語", "Radio3" => "数学");
        $radioArray = knjCreateRadio($objForm, "Radio", $model->topRadio, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        
        
        //点数下限・上限
        $extra = "";
        $extra = $extraInt;
        $table["MIN"] = knjCreateTextBox($objForm, $model->top_field["MIN0"], "MIN0", 5, 3, $extra);
        $table["MAX"] = knjCreateTextBox($objForm, $model->top_field["MAX0"], "MAX0", 5, 3, $extra);
        
        
        //コメント欄
        for($i=0;$i<2;$i++){
            $extra = "";
            $table["COMMENT_{$i}"] = knjCreateTextBox($objForm, $model->top_field["COMMENT0{$i}"], "COMMENT0{$i}", 100, 50, $extra);
        }
        
        
        $arg["data"] = $table;

        if ($model->cmd == "list2") {
            $arg["reload"]  = "    parent.bottom_frame.location.href='knjl381qindex.php?cmd=edit&KAMOKU={$model->topRadio}';";
        }
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl381qForm2.html", $arg); 
        
    }
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //追加
    $extra = "onclick=\"btn_submit('insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);

    //終了
    $extra = "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    
}
?>
