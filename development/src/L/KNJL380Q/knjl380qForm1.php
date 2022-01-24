<?php

require_once('for_php7.php');

class knjl380qForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }


        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjl380qindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();


        //コピー用年度コンボ
        $yearQuery = knjl380qQuery::getCopyYear();
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
        $dataQuery = knjl380qQuery::getJudgeComment();
        $dataResult = $db->query($dataQuery);
        
        $cnt=1;
        
        while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
            //保存されてるデータをmodelに入れる
            $model->bottom_field["JUDGE{$cnt}"] = $dataRow["JUDGE"];
            $model->bottom_field["CHECK{$cnt}"] = array();
            if($dataRow["PREF_FLG"] != 0){
                $model->bottom_field["CHECK{$cnt}"][] = 0;
            }
            if($dataRow["IN_FLG"] != 0){
                $model->bottom_field["CHECK{$cnt}"][] = 1;
            }
            if($dataRow["OUT_FLG"] != 0){
                $model->bottom_field["CHECK{$cnt}"][] = 2;
            }
            $model->bottom_field["NOTE{$cnt}"] = $dataRow["NOTE"];
            
            for($j=0;$j<3;$j++){
                $jj = $j+1;
                $model->bottom_field["COMMENT{$cnt}{$j}"] = $dataRow["COMMENT{$jj}"];
            }
            for($k=3;$k<7;$k++){
                $kk = $k-2;
                $model->bottom_field["COMMENT{$cnt}{$k}"] = $dataRow["COMMENT2_{$kk}"];
            }
            
            
            
            //判定コンボ
            $judgeQuery = knjl380qQuery::getJudge();
            $judgeResult = $db->query($judgeQuery);
            $opt = array();
            $opt[0] = array("value"     =>  "",
                            "label"     =>  "");
            while($judgeRow = $judgeResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array("value"  =>  $judgeRow["NAMECD2"],
                               "label"  =>  $judgeRow["NAME1"]);
            }
            $extra = "";
            $table["JUDGE"] = knjCreateCombo($objForm, "JUDGE{$cnt}", $model->bottom_field["JUDGE{$cnt}"], $opt, $extra, 1);
            
            //コメント欄
            for($i=0;$i<7;$i++){
                $extra = "";
                $table["COMMENT_{$i}"] = knjCreateTextBox($objForm, $model->bottom_field["COMMENT{$cnt}{$i}"], "COMMENT{$cnt}{$i}", 100, 50, $extra);
            }
            
            //チェックボックス
            $opt2 = array();
            $opt2 = array("県内", "国内", "海外");
            foreach($opt2 as $key => $val){
                $extra = " id=\"".$cnt.$key."\"";
                if(!empty($model->bottom_field["CHECK{$cnt}"])){
                    if(in_array($key, $model->bottom_field["CHECK{$cnt}"])){
                        $extra .= " checked";
                    }
                }
                $table["CHECK{$key}"] = knjCreateCheckBox($objForm, "CHECK{$cnt}[]", $key, $extra)."<LABEL for=".$cnt.$key.">".$val."</LABEL>";
            }
            
            //ノート
            $table["NOTE"] = knjCreateTextBox($objForm, $model->bottom_field["NOTE{$cnt}"], "NOTE{$cnt}", 15, 15, $extra);
            
            //更新ボタン
            $extra = " onclick=\"update_check('update', '{$dataRow["COMMENTNO"]},{$dataRow["COMMENTNO2"]}', '{$cnt}');\"";
            $table["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
            
            //削除ボタン
            $extra = " onclick=\"update_check('delete', '{$dataRow["COMMENTNO"]},{$dataRow["COMMENTNO2"]}', '{$cnt}');\"";
            $table["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);

            $arg["data"][] = $table;
            
            $cnt++;
        }
        $model->dataCnt = $cnt-1;

        if ($model->cmd == "list2") {
            $arg["reload"]  = "parent.bottom_frame.location.href='knjl380qindex.php?cmd=edit';";
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
        View::toHTML($model, "knjl380qForm1.html", $arg); 
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
    //成績評価コメント反映
    //$extra = "";
    //$arg["button"]["btn_examupdate"] = knjCreateBtn($objForm, "btn_examupdate", "成績評価コメント反映", $extra);

    //年度コピーボタン
    $extra = " onclick=\"btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の年度からコピー", $extra);

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
