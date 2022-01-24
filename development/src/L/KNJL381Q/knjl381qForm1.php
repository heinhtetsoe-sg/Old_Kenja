<?php

require_once('for_php7.php');

class knjl381qForm1
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


        $arg["start"]   = $objForm->get_start("main", "POST", "knjl381qindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        if($model->btmRadio == "1"){
            $tablename = "SAT_COMMENT_ENGLISH_DAT";
        }else if($model->btmRadio == "2"){
            $tablename = "SAT_COMMENT_JAPANESE_DAT";
        }else{
            $tablename = "SAT_COMMENT_MATH_DAT";
        }
        

        //コピー用年度コンボ
        $yearQuery = knjl381qQuery::getCopyYear($tablename);
        $yearResult = $db->query($yearQuery);
        $opt = array();
        $exist = array();
        $opt[0] = array("value"    => "",
                        "label"    => "");
        while($yearRow = $yearResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("value"  =>  $yearRow["YEAR"],
                           "label"  =>  $yearRow["YEAR"]);
        }
        $extra = " ";
        
        $arg["COPY_YEAR"] = knjCreateCombo($objForm, "COPY_YEAR", $model->left_field["COPY_YEAR"], $opt, $extra, 1);

        //保存されてるデータを取得
        $dataQuery = knjl381qQuery::getComment($tablename);
        $dataResult = $db->query($dataQuery);
        
        $cnt=1;
        
        while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->bottom_field["MIN{$cnt}"] = $dataRow["SCORE_FROM"];
            $model->bottom_field["MAX{$cnt}"] = $dataRow["SCORE_TO"];
            for($i=0;$i<2;$i++){
                $ii = $i+1;
                $model->bottom_field["COMMENT{$cnt}{$i}"] = $dataRow["COMMENT{$ii}"];
            }
            
                
            //点数下限・上限
            $extra = "";
            $extra = $extraInt;
            $table["MIN"] = knjCreateTextBox($objForm, $model->bottom_field["MIN{$cnt}"], "MIN{$cnt}", 5, 3, $extra);
            $table["MAX"] = knjCreateTextBox($objForm, $model->bottom_field["MAX{$cnt}"], "MAX{$cnt}", 5, 3, $extra);
            
            
            //コメント欄
            for($i=0;$i<2;$i++){
                $extra = "";
                $table["COMMENT_{$i}"] = knjCreateTextBox($objForm, $model->bottom_field["COMMENT{$cnt}{$i}"], "COMMENT{$cnt}{$i}", 100, 50, $extra);
            }
            
            //hidden
            knjCreateHidden($objForm, "COMMENTNO{$cnt}", $dataRow["COMMENT_NO"]);
            
            $arg["data"][] = $table;
            
            $cnt++;
        }
        $model->dataCnt = $cnt-1;
        knjCreateHidden($objForm, "dataCnt", $model->dataCnt);


        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl381qForm1.html", $arg); 
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

    //年度コピーボタン
    $extra = " onclick=\"btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の年度からコピー", $extra);


    //更新
    $extra = " onclick=\"btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //学習指針反映
    //$extra = "";
    //$arg["button"]["btn_examupdate"] = knjCreateBtn($objForm, "btn_examupdate", "学習指針反映", $extra);

    //終了
    $extra = "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd", $model->cmd);
    
    knjCreateHidden($objForm, "KAMOKU", $model->btmRadio);
}
?>
