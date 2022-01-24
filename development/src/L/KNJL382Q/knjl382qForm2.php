<?php

require_once('for_php7.php');

class knjl382qForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl382qindex.php", "", "edit");

        $model->mockyear = ($model->mockyear) ? $model->mockyear : CTRL_YEAR;

        //DB接続
        $db = Query::dbCheckOut();
        
        //PLACECDが入っているとき
        if ($model->GROUP_CD != "") {
            $query = knjl382qQuery::getRow($model->GROUP_CD);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->right_field;
        }

        
        //処理
        $opt1 = array(1, 2);
        $label = array("SHORI1"=>"新規","SHORI2"=>"修正");
        $extra = array("id=\"SHORI1\" onclick=\"btn_submit('edit');\"", "id=\"SHORI2\" onclick=\"btn_submit('edit');\"");
        $radioArray = knjCreateRadio($objForm, "SHORI", $model->right_field["SHORI"], $extra, $opt1, get_count($opt1));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val."<LABEL for=".$key.">".$label[$key]."</LABEL>";
        
        //コード
        $extra = "";
        $arg["data"]["GROUPCD"] = knjCreateTextBox($objForm, $Row["GROUPCD"], "GROUPCD", 10, 5, $extra);
        
        //団体名
        $arg["data"]["GROUPNAME"] = knjCreateTextBox($objForm, $Row["GROUPNAME"], "GROUPNAME", 50, 50, $extra);
        
        //郵便番号
        $arg["data"]["GROUPZIP"] = View::popUpZipCode($objForm, "GROUPZIP", $Row["GROUPZIP"], "GROUPADDR1");

        //県名
        $prefQuery = knjl382qQuery::getPrefcdAll();
        $prefResult = $db->query($prefQuery);
        $opt = array();
        $opt[0] = array("value"    => "",
                        "label"    => "");
        while($prefRow = $prefResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("value"  =>  $prefRow["PREF_CD"],
                           "label"  =>  $prefRow["PREF_CD"]."：".$prefRow["PREF_NAME"]);
        }
        $extra = "";
        $arg["data"]["PREFCD"] = knjCreateCombo($objForm, "GROUPPREF", $Row["GROUPPREF"], $opt, $extra, 1);

        //住所1
        $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $Row["GROUPADDR1"], "GROUPADDR1", 50, 50, $extra);

        //住所2
        $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $Row["GROUPADDR2"], "GROUPADDR2", 50, 50, $extra);

        //電話
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["GROUPTEL"], "GROUPTEL", 15, 15, $extra);

        //担当者
        $arg["data"]["STAFFNAME"] = knjCreateTextBox($objForm, $Row["GROUPSTAFF"], "GROUPSTAFF", 10, 10, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "cmd", $model->cmd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "edit2") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl382qindex.php?cmd=list&SEARCH_PREF=".$model->right_field["GROUPPREF"]."';";
        }else if($model->cmd == "edit3"){
            $arg["reload"]  = "parent.left_frame.location.href='knjl382qindex.php?cmd=list&SEARCH_PREF=".$model->right_field["GROUPPREF"]."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl382qForm2.html", $arg); 
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
    if($model->right_field["SHORI"] != 2){
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
