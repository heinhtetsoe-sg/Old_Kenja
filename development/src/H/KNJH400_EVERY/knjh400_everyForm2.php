<?php

require_once('for_php7.php');

class knjh400_everyForm2
{
    function main(&$model)
    {
        //権限チェック
        /*if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }*/

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh400_everyindex.php", "", "edit");

        $db = Query::dbCheckOut();
        
        if($model->cmd == "choice"){
            //選択されたデータを取得する
            $query = knjh400_everyQuery::getChoiceData($model->schregno, $model->recNo);
            $result = $db->query($query);
            $cnt = 0;
            $sp = "";
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($cnt == 0){
                    $model->field = $row;
                    $model->field["TAG"] = "";
                }
                $model->field["TAG"] .= $sp.$row["TAG"];
                $sp = "　";
                
                $cnt++;
            }
        }else if($model->cmd == "new"){
            $model->field = array();    //初期化
            $model->field["REGISTERCD"] = STAFFCD;
        }else if($model->cmd == "copy"){
            //コピーしたときは入力者だけSTAFFCDに変える
            $model->field["REGISTERCD"] = STAFFCD;
        }
        
        //入力者
        $query = knjh400_everyQuery::getStaffName($model->field["REGISTERCD"]);
        $staffname = $db->getOne($query);
        $arg["STAFFNAME"] = $model->field["REGISTERCD"]."：".$staffname;
        
        
        //日付
        $arg["DATE"] =  View::popUpCalendar($objForm, "DATE", str_replace("-","/",$model->field["DATE"]),"");
        //時間
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["H_TIME"] = knjCreateTextBox($objForm, $model->field["H_TIME"], "H_TIME", 2, 2, $extra);
        $arg["M_TIME"] = knjCreateTextBox($objForm, $model->field["M_TIME"], "M_TIME", 2, 2, $extra);
        
        
        //件名
        $extra = "";
        $arg["TITLE"] = knjCreateTextBox($objForm, $model->field["TITLE"], "TITLE", 60, 30, $extra);
        
        //内容
        $extra = "";
        $arg["TEXT"] = knjCreateTextArea($objForm, "TEXT", 5, 60, "", $extra, $model->field["TEXT"]);
        
        //項目
        $extra = "";
        $arg["TAG"] = knjCreateTextBox($objForm, $model->field["TAG"], "TAG", 60, 30, $extra);
        
        //タグの頻出データを表示
        $query = knjh400_everyQuery::getTagCnt();
        $result = $db->query($query);
        $sp = "";
        $linkData = "";
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $linkData .= $sp."<a onclick=\"clickTag('".$row["TAG"]."');\">#".$row["TAG"]."</a>";
            $sp = "　";
        }
        $arg["TAGLINK"] = $linkData;
        
        Query::dbCheckIn($db);

        //ボタン作成
        makeBtn($objForm, $arg, $model);
        //hidden作成
        makeHidden($objForm, $model);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh400_everyForm2.html", $arg);
    }
} 
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg["data2"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}


//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    if($model->recNo == ""){
        //追加
        $extra = " onclick=\"btn_submit('add');\"";
        $arg["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    }else if($model->field["REGISTERCD"] == STAFFCD){
        //更新
        $extra = " onclick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = " onclick=\"btn_submit('delete');\"";
        $arg["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    }
    //新規
    $extra = " onclick=\"btn_submit('new');\"";
    $arg["btn_new"] = knjCreateBtn($objForm, "btn_new", "新 規", $extra);
    
    if($model->recNo != ""){
        //コピー
        $extra = " onclick=\"btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "コピー", $extra);
    }
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd", $model->cmd);
    knjCreateHidden($objForm, "REGISTERCD", $model->field["REGISTERCD"]);   //入力者は表示だけなのでhiddenで持っておく
    knjCreateHidden($objForm, "RECNO", $model->recNo);
}
?>
