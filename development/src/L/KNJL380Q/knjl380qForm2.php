<?php

require_once('for_php7.php');

class knjl380qForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }


        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl380qindex.php", "", "edit");

        //データベース接続
        $db = Query::dbCheckOut();


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
        $table["JUDGE"] = knjCreateCombo($objForm, "JUDGE0", $model->top_field["JUDGE0"], $opt, $extra, 1);
        
        //コメント欄
        for($i=0;$i<7;$i++){
            $extra = "";
            $table["COMMENT_{$i}"] = knjCreateTextBox($objForm, $model->top_field["COMMENT0{$i}"], "COMMENT0{$i}", 100, 50, $extra);
        }
        
        //チェックボックス
        $opt2 = array();
        $opt2 = array("県内", "国内", "海外");
        foreach($opt2 as $key => $val){
            $extra = " id=\"".$key."\"";
            if(!empty($model->top_field["CHECK0"])){
                if(in_array($key, $model->top_field["CHECK0"])){
                    $extra .= " checked";
                }
            }
            $table["CHECK{$key}"] = knjCreateCheckBox($objForm, "CHECK0[]", $key, $extra)."<LABEL for=".$key.">".$val."</LABEL>";
        }
        
        //ノート
        $table["NOTE"] = knjCreateTextBox($objForm, $model->top_field["NOTE0"], "NOTE0", 15, 15, $extra);
        
        $arg["data"] = $table;

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
        View::toHTML($model, "knjl380qForm2.html", $arg); 
        
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
