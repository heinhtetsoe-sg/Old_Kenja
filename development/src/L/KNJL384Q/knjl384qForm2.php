<?php

require_once('for_php7.php');

class knjl384qForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl384qindex.php", "", "edit");

        $model->mockyear = ($model->mockyear) ? $model->mockyear : CTRL_YEAR;

        //DB接続
        $db = Query::dbCheckOut();
        
        //PLACECDが入っているとき
        if ($model->PLACECD != "") {
            $query = knjl384qQuery::getRow($model->PLACECD);
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
        
        //会場コード
        $extra = "";
        $arg["data"]["PLACECD"] = knjCreateTextBox($objForm, $Row["PLACECD"], "PLACECD", 10, 10, $extra);
        
        //会場地区名
        $arg["data"]["PLACEAREA"] = knjCreateTextBox($objForm, $Row["PLACEAREA"], "PLACEAREA", 10, 10, $extra);
        
        //会場略称
        $arg["data"]["PLACENAME_SHORT"] = knjCreateTextBox($objForm, $Row["PLACENAME_SHORT"], "PLACENAME_SHORT", 50, 40, $extra);

        //会場
        $arg["data"]["PLACENAME"] = knjCreateTextBox($objForm, $Row["PLACENAME"], "PLACENAME", 50, 40, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "cmd", $model->cmd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "edit2") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl384qindex.php?cmd=list&PLACECD=".$model->PLACECD."';";
        }else if($model->cmd == "edit3"){
            $arg["reload"]  = "parent.left_frame.location.href='knjl384qindex.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl384qForm2.html", $arg); 
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
