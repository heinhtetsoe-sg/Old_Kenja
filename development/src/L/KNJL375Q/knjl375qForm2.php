<?php

require_once('for_php7.php');

class knjl375qForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }


        //フォーム作成
        $objForm = new form;

        //データベース接続
        $db = Query::dbCheckOut();
        
        $onclick = " onclick=\" btn_submit('change');\"";
        
        //国内海外区分
        $opt = array(1, 2, 3);
        $extra = array("id=\"L_KUBUN1\" {$onclick}", "id=\"L_KUBUN2\" {$onclick}", "id=\"L_KUBUN3\" {$onclick}");
        $label = array("L_KUBUN1" => "すべて", "L_KUBUN2" => "国内", "L_KUBUN3" => "海外");
        $radioArray = knjCreateRadio($objForm, "L_KUBUN", $model->top_field["L_KUBUN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        
        //科目
        //左
        $opt = array(1, 2, 3);
        $extra = array("id=\"L_KAMOKU1\" {$onclick}", "id=\"L_KAMOKU2\" {$onclick}", "id=\"L_KAMOKU3\" {$onclick}");
        $label = array("L_KAMOKU1" => "英語", "L_KAMOKU2" => "国語", "L_KAMOKU3" => "数学");
        $radioArray = knjCreateRadio($objForm, "L_KAMOKU", $model->top_field["L_KAMOKU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        //右
        $opt = array(1, 2, 3);
        $extra = array("id=\"R_KAMOKU1\"", "id=\"R_KAMOKU2\"", "id=\"R_KAMOKU3\"");
        $label = array("R_KAMOKU1" => "英語", "R_KAMOKU2" => "国語", "R_KAMOKU3" => "数学");
        $radioArray = knjCreateRadio($objForm, "R_KAMOKU", $model->top_field["R_KAMOKU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        
        //回数
        //左
        $opt = array(1, 2);
        $extra = array("id=\"L_KAISU1\" {$onclick}", "id=\"L_KAISU2\" {$onclick}");
        $label = array("L_KAISU1" => "1回目", "L_KAISU2" => "2回目");
        $radioArray = knjCreateRadio($objForm, "L_KAISU", $model->top_field["L_KAISU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        
        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        if ($model->cmd == "list2" || $model->cmd == "change") {
            $arg["reload"]  = "parent.bottom_frame.location.href='knjl375qindex.php?cmd=edit&KUBUN=".$model->top_field["L_KUBUN"]."&KAMOKU=".$model->top_field["L_KAMOKU"]."&KAISU=".$model->top_field["L_KAISU"]."';";
        }
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);


        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl375qindex.php", "", "edit");
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl375qForm2.html", $arg); 
        
    }
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //表示
    //$para = "KUBUN=".$model->top_field["L_KUBUN"]."&KAMOKU=".$model->top_field["L_KAMOKU"]."&KAISU=".$model->top_field["L_KAISU"];
    //$extra = "onclick=\"bottom_load('{$para}');\"";
    //$arg["button"]["btn_load"] = knjCreateBtn($objForm, "btn_load", "表 示", $extra);

    //実行
    $extra = "onclick=\"btn_submit('import');\"";
    $arg["button"]["btn_import"] = knjCreateBtn($objForm, "btn_import", "実 行", $extra);

    //終了
    $extra = "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    
}
?>
