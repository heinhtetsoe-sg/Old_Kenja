<?php

require_once('for_php7.php');

class knjc310Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc310index.php", "", "main");
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //伝言一覧表示
        makeList($objForm, $arg, $db, $model);
        
        //担当者
        $extra = "onchange=\"return btn_submit('changeCombo')\" ";
        $query = knjc310Query::getStaff();
        $arg["data2"]["C_STAFF"] = makeCombo($objForm, $arg, $db, $query, $model->field["C_STAFF"], "C_STAFF", $extra, 1, "BLANK");
        
        //ラジオ（既読・未読・全部）
        $joken = array(3, 2, 1);
        $model->joken = $model->joken ? $model->joken : "1";
        $extraRadio = array("id=\"JOKEN1\" onClick=\"btn_submit('changeRadio')\" ", "id=\"JOKEN2\" onClick=\"btn_submit('changeRadio')\" ", "id=\"JOKEN3\" onClick=\"btn_submit('changeRadio')\" ");
        $radioArray = knjCreateRadio($objForm, "JOKEN", $model->joken, $extraRadio, $joken, get_count($joken));
        //$radioArray = createRadio($objForm, "JOKEN", $model->joken, $extraRadio, $joken, get_count($joken));
        foreach($radioArray as $key => $val) $arg[$key] = $val;
        
        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);
        
        //hidden作成
        makeHidden($objForm, $model);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc310Form1.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "- - すべての伝言 - -",
                        "value" => "");
    }
    $result = $db->query($query);
    
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"]."の伝言",
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    //return createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//起案データ表示
function makeList(&$objForm, &$arg, $db, $model)
{
    $count = 0;
    
    $query = knjc310Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        
        //列番号
        $checkVal               = $row["MESSAGENO"];
        
        //メッセージ番号
        $setdata["MESSAGENO"]   = $row["MESSAGENO"];
        
        //ラベル
        $setdata["CHECKLABEL"]  = "DELCHK".$checkVal;
        
        //チェックボックス
        $extra                  = "id=DELCHK".$checkVal;
        $setdata["DELCHK"]      = knjCreateCheckBox($objForm, "DELCHK", $checkVal, $extra, "1");
        //$setdata["DELCHK"]      = createCheckBox($objForm, "DELCHK", $checkVal, $extra, "1");
        
        //発信日
        $setdata["SENDDATE"]    = str_replace("-","/",$row["SENDDATE"]);
        
        //発信者名
        $setdata["STAFFNAME"]   = $row["STAFFNAME"];
        
        //生徒番号
        $setdata["SCHREGNO"]    = $row["SCHREGNO"];
        
        //生徒氏名
        $setdata["NAME"]        = $row["NAME"];
        
        //内容はまとめる
        $setdata["MESSAGE"]    = $row["MESSAGE1"].$row["MESSAGE2"];
        
        //有効期限
        $setdata["TERMDATE"]    = str_replace("-","/",$row["TERMDATE"]);
        
        //読出日
        $setdata["READDATE"]    = str_replace("-","/",$row["READDATE"]);
        
        //未読
        if ($row["READDATE"] == ""){
            $setdata["NOTREAD"] = "未読";
        } else {
            $setdata["NOTREAD"] = "既読";
        }
        
        //画面にセット
        $arg["data"][] = $setdata;
        
        //カウントに＋１
        $count++;
        
    }
    
    //カウント１０以上だとスクロールバー表示のため高さを指定
    if ($count > 10){
        $arg["DATAHEIGHT"]      = "height=550;";
    } else {
        $arg["DATAHEIGHT"]      = "";
    }
    
    if ($count > 0){
        $extra         = "id=DELALL onclick=\"check_chg(this)\" ";
        $arg["DELALL"] = knjCreateCheckBox($objForm, "DELALL", "1", $extra, "1");
    }
    
    $result->free();
    
    $setdata = array();
    
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db)
{
    
    //削除
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削　除", $extra);
    //$arg["button"]["btn_delete"] = createBtn($objForm, "btn_delete", "削　除", $extra);
    
    //全て削除
    $extra = "onclick=\"return btn_submit('deleteAll');\"";
    $arg["button"]["btn_deleteAll"] = knjCreateBtn($objForm, "btn_deleteAll", "全て削除", $extra);
    //$arg["button"]["btn_deleteAll"] = createBtn($objForm, "btn_deleteAll", "全て削除", $extra);
    
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終　了", "onclick=\"closeWin();\"");
    //$arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終　了", "onclick=\"closeWin();\"");
    
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    //createHidden($objForm, "cmd");
}

?>
