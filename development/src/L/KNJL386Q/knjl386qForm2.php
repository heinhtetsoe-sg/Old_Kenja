<?php

require_once('for_php7.php');

class knjl386qForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl386qindex.php", "", "edit");

        $model->mockyear = ($model->mockyear) ? $model->mockyear : CTRL_YEAR;

        //DB接続
        $db = Query::dbCheckOut();
        
        //RECNOが入っているとき
        if ($model->Recno != "") {
            $query = knjl386qQuery::getRow($model->Recno);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }
        
        
        //処理
        $opt = array(1, 2);
        $label = array("SHORI1"=>"新規","SHORI2"=>"修正");
        $extra = array("id=\"SHORI1\" onclick=\"btn_submit('edit');\"", "id=\"SHORI2\" onclick=\"btn_submit('edit');\"");
        $radioArray = knjCreateRadio($objForm, "SHORI", $model->field["SHORI"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val."<LABEL for=".$key.">".$label[$key]."</LABEL>";
        
        //国内外
        $inoutQuery = knjl386qQuery::getInout();
        $inoutResult = $db->query($inoutQuery);
        $opt2 = array();
        $extra = "";
        $opt2[0] = array("value"    =>  "",
                         "label"    =>  "");
        while($inoutRow = $inoutResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt2[] = array("value"     =>  $inoutRow["NAMECD2"],
                            "label"     =>  $inoutRow["NAME1"]
                           );
        }
        $arg["data"]["INOUT_KUBUN"] = knjCreateCombo($objForm, "INOUT_KUBUN", $Row["INOUT_KUBUN"], $opt2, $extra, "1");
        
        //受験番号範囲
        $extra = "";
        $arg["data"]["JUKEN_FROM"] = knjCreateTextBox($objForm, $Row["JUKEN_NO_FROM"], "JUKEN_NO_FROM", 10, 10, $extra);
        $arg["data"]["JUKEN_TO"] = knjCreateTextBox($objForm, $Row["JUKEN_NO_TO"], "JUKEN_NO_TO", 10, 10, $extra);
        
        //会場名
        $extra = "";
        $placeQuery = knjl386qQuery::getPlacecd();
        $placeResult = $db->query($placeQuery);
        $opt3 = array();
        $opt3[0] = array("value" => "",
                         "label" => "");
        while($placeRow = $placeResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt3[] = array("value"     =>  $placeRow["PLACE"],
                            "label"     =>  $placeRow["PLACE_NAME"]);
        }
        $arg["data"]["PLACE"] = knjCreateCombo($objForm, "PLACE", $Row["PLACECD"], $opt3, $extra, "1");
        
        //個人/団体
        $extra = "";
        $indQuery = knjl386qQuery::getIndkubun();
        $indResult = $db->query($indQuery);
        $opt4 = array();
        $opt4[0] = array("value"    =>  "",
                         "label"    =>  "");
        while($indRow = $indResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt4[] = array("value"     =>  $indRow["NAMECD2"],
                            "label"     =>  $indRow["NAME1"]);
        }
        $arg["data"]["IND_KUBUN"] = knjCreateCombo($objForm, "IND_KUBUN", $Row["IND_KUBUN"], $opt4, $extra, "1");

        //郵送/窓口
        $extra = "";
        $sendQuery = knjl386qQuery::getSendkubun();
        $sendResult = $db->query($sendQuery);
        $opt5 = array();
        $opt5[0] = array("value"    =>  "",
                         "label"    =>  "");
        while($sendRow = $sendResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt5[] = array("value"     =>  $sendRow["NAMECD2"],
                            "label"     =>  $sendRow["NAME1"]);
        }
        $arg["data"]["SEND_KUBUN"] = knjCreateCombo($objForm, "SEND_KUBUN", $Row["SEND_KUBUN"], $opt5, $extra, "1");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "cmd", $model->cmd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "edit2") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl386qindex.php?cmd=list&RECNO=".$model->Recno."';";
        }else if($model->cmd == "edit3"){
            $arg["reload"]  = "parent.left_frame.location.href='knjl386qindex.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl386qForm2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $opt = array();
    $result = $db->query($query);
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model)
{
    if($model->field["SHORI"] != 2){
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    }else{
        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    }
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
