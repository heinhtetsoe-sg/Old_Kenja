<?php

require_once('for_php7.php');

class knjl373qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl373qindex.php", "", "main");
        
        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に
        
        $db = Query::dbCheckOut();
        
        //表示区分
        $opt = array(1, 2);
        $extra = array("id=\"Radio1\"", "id=\"Radio2\"");
        $label = array("Radio1" => "全データ", "Radio2" => "欠席者のみ");
        $radioArray = knjCreateRadio($objForm, "Radio", $model->field["RADIO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        
        //会場コンボ
        $placeQuery = knjl373qQuery::getPlace();
        $placeResult = $db->query($placeQuery);
        $opt = array();
        $opt[0] = array("value"     =>  "",
                        "label"     =>  "");
        while($placeRow = $placeResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("value"  =>  $placeRow["PLACECD"],
                           "label"  =>  $placeRow["PLACECD"]."：".$placeRow["PLACENAME_SHORT"]);
        }
        $extra = "";
        $arg["PLACECD"] = knjCreateCombo($objForm, "PLACECD", $model->field["PLACECD"], $opt, $extra, 1);
        
        //データ取得
        $dataQuery = knjl373qQuery::getData($model->field);
        $dataResult = $db->query($dataQuery);
        while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["SAT_NO"] = $dataRow["SAT_NO"];
            $data["NAME"]   = $dataRow["NAME1"];
            $data["PLACE_NAME"] = $dataRow["PLACENAME_SHORT"];
            $data["SCHOOL_NAME"] = $dataRow["FINSCHOOL_NAME"];
            $data["GROUP_NAME"] = $dataRow["GROUPNAME"];

            //チェックボックス
            $extra = "";
            if($dataRow["ABSENCE"] != "1"){
                $extra .= " checked";
            }else{
                $extra = "";
            }
            $data["ABSENCE"] = knjCreateCheckBox($objForm, "ABSENCE", $dataRow["SAT_NO"], $extra, "");
            
            $arg["data"][] = $data;
        }
        
        
        Query::dbCheckIn($db);
        
        //ボタン作成
        makeButton($objForm, $arg, $db, $model);
        
        $arg["REQUESTROOT"] = REQUESTROOT;

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL373Q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        
        knjCreateHidden($objForm, "staffcd", STAFFCD);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        //View::toHTML($model, "knjl373qForm1.html", $arg);
        View::toHTML6($model, "knjl373qForm1.html", $arg, "jquery-1.11.0.min.js");
    }

}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "YEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    //表示ボタン
    $extra = "onclick=\"btn_submit('search');\"";
    $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "表 示", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //欠席者一覧表印刷ボタン
    $extra = "onclick=\"newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "欠席者一覧表印刷", $extra);

    //マークリーダ用名簿作成ボタン
    $extra = "onclick=\"btn_submit('create');\"";
    $arg["btn_create"] = knjCreateBtn($objForm, "btn_create", "マークリーダ用名簿作成", $extra);

}
?>
