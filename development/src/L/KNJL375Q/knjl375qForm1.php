<?php

require_once('for_php7.php');

class knjl375qForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }


        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjl375qindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        $kamoku = array("1" =>  "英語", "2" => "国語", "3" => "数学");
        $arg["KAMOKU"] = $kamoku[$model->req_field["KAMOKU"]];
        
        $arg["KAISU"] = $model->req_field["KAISU"];
        
        $kyouka = array("1" =>  "ENGLISH", "2" => "JAPANESE", "3" => "MATH");
        if($model->req_field["KAISU"] != "1"){
            $kaisu = "2";
        }else{
            $kaisu = "";
        }

        //表示データ取得
        $dataQuery = knjl375qQuery::getData($kyouka, $model->req_field);
        $dataResult = $db->query($dataQuery);
        
        $cnt = 0;
        $extra = "";
        
        $model->satNo = array();
        $choice = $kyouka[$model->req_field["KAMOKU"]];
        
        while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["EXAM_NO"] = $dataRow["SAT_NO"];
            $data["NAME"] = $dataRow["NAME1"];
            
            $extra = " id=\"TOKUTEN{$cnt}\"";
            //$extra .= " onBlur=\"this.value=toInteger(this.value);\"";
            $extra .= " onBlur=\"updateTokuten(this);\"";
            $extra .= " onKeydown=\"toNextText(this);\"";
            $data["TOKUTEN"] = knjCreateTextBox($objForm, $dataRow["SCORE_{$choice}{$kaisu}"], "TOKUTEN{$dataRow["SAT_NO"]}", 5, 3, $extra);
            
            if($dataRow["ABSENCE_{$choice}"] != 1){
                $chkextra = " checked ";
            }else{
                $chkextra = "";
            }
            $data["ABSENCE"] = knjCreateCheckBox($objForm, "ABSENCE", $dataRow["SAT_NO"], $chkextra, "");
            

            $extra = " onclick=\" delBtn('".$dataRow["SAT_NO"]."');\"";
            $data["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "clear", $extra);


            $arg["data"][] = $data;
            
            //更新するときに使いたい
            $model->satNo[$cnt] = $dataRow["SAT_NO"];
            
            $cnt++;
        }
        
        

        if ($model->cmd == "list2") {
            $arg["reload"]  = "parent.bottom_frame.location.href='knjl375qindex.php?cmd=edit';";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "ctrlYear", CTRL_YEAR);
        knjCreateHidden($objForm, "staffcd", STAFFCD);
        knjCreateHidden($objForm, "kamoku", $model->req_field["KAMOKU"]);
        knjCreateHidden($objForm, "kaisu", $model->req_field["KAISU"]);
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML6($model, "knjl375qForm1.html", $arg, "jquery-1.11.0.min.js"); 
    }
}
//コンボ作成　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　
function makeCombo(&$objForm, &$arg, $array, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }


    foreach($array as $key => $val){
        //$val = str_pad($val,2,"0",STR_PAD_LEFT);
        $opt[] = array ("label" => $val,
                        "value" => $key);
        if ($value == $key) $value_flg = true;
    }

    //$value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新
    $extra = " onclick=\" btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //終了
    $extra = "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd", $model->cmd);
    
    knjCreateHidden($objForm, "COMMENT_NO", $model->CommentNo);
    knjCreateHidden($objForm, "COUNT_NO", $model->CountNo);
    
}
?>
