<?php
class knjo151Form2
{
    function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjo151index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->dataRow) && ($model->cmd == "edit_select" || $model->cmd == "reset")) {
            $rowData = explode("-", $model->dataRow);
            $model->field = $db->getRow(knjo151Query::getAnother($db, $model, $rowData[0], $rowData[1]), DB_FETCHMODE_ASSOC);
            $model->left_field = $model->field;
        }

        //教育課程コンボ
        $arg["data"]["CURRICULUM_CD"] = $model->left_field["APP_CURRICULUM_NAME"];

        //教育課程コンボ
        $query = knjo151Query::getNameMst("Z018");
        $extra = "onChange=\"return btn_submit('edit_src');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["REP_CURRICULUM_CD"], "REP_CURRICULUM_CD", $extra, 1, "BLANK");

        //教科
        if($model->left_field["CLASS_NAME"] != "" && $model->left_field["CLASS_NAME"] != "S99"){
            $arg["data"]["CLASSCD"] = $model->left_field["CLASS_NAME"]."：".$model->left_field["APP_CLASSNAME"];
        }else if($model->left_field["SCHOOL_CLASS_NAME"] != ""){
            $arg["data"]["CLASSCD"] = "学設：".$model->left_field["SCHOOL_CLASS_NAME"];
        }

        //教科コンボ
        $query = knjo151Query::getClass_mst($model, "ALL", "ANOTHER");
        $extra = "onChange=\"return btn_submit('edit_src');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["REP_CLASSCD"], "REP_CLASSCD", $extra, 1, "BLANK");
        
        //科目
        if($model->left_field["SUBCLASS_NAME"] != "" && $model->left_field["SUBCLASS_NAME"] != "S99-01"){
            $arg["data"]["SUBCLASSCD"] = $model->left_field["SUBCLASS_NAME"]."：".$model->left_field["APP_SUBCLASSNAME"];
        }else if($model->left_field["SCHOOL_SUBCLASS_NAME"] != ""){
            $arg["data"]["SUBCLASSCD"] = "学設：".$model->left_field["SCHOOL_SUBCLASS_NAME"];
        }

        //科目コンボ
        $query = knjo151Query::getSubclass_mst($model, $model->field["REP_CLASSCD"], $model->field["REP_CURRICULUM_CD"], "ANOTHER");
        $extra = "onChange=\"return btn_submit('edit_src');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["REP_SUBCLASSCD"], "REP_SUBCLASSCD", $extra, 1, "BLANK");


        //修得単位
        if($model->left_field["GET_CREDIT"] != ""){
            $arg["data"]["GET_CREDIT"] = $model->left_field["GET_CREDIT"];
        }else if($model->left_field["SCHOOL_SUBCLASS_TANNI"] != ""){
            $arg["data"]["GET_CREDIT"] = "学設：".$model->left_field["SCHOOL_SUBCLASS_TANNI"];
        }



        //学籍番号
        if ($model->knjo151schreg != "") {
            $arg["data"]["SCHREGNO"] = "(学籍番号：".$model->knjo151schreg.")";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        switch (VARS::post("cmd")) {
            case "add" :
            case "preupdate" :
            case "delete" :
                $arg["jscript"] = "window.open('knjo151index.php?cmd=right_list','right_frame');";
                break;
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjo151Form2.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $data_val = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        $data_val[] = $row["VALUE"];
    }
    $result->free();

    if (!in_array($value, $data_val)) {
        $value = "";
    }

    if($name == "CURRICULUM_CD"){
        $default = $db->getOne(knjo151Query::getNameDefault("Z018", ""));
        $count = $db->getOne(knjo151Query::getNameDefault("Z018", "COUNT"));
        $setvalue;
        for ($i = 0; $i <= $count; $i++) {
            if ($opt[$i]["value"] == $default) {
                $setvalue = $i;
            }
        }
        if ($setvalue != "") {
            $value = ($value || $value === "0") ? $value : $opt[$setvalue]["value"];
        } else {
            $value = ($value || $value === "0") ? $value : $opt[0]["value"];
        }
    } else {
        $value = ($value) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//空コンボ作成
function makeNullCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db)
{
    //科目コード
    $extra  = " onClick=\" wopen('".REQUESTROOT."/Z/KNJZ071/knjz071index.php?";
    $extra .= "SEND_PRGID=KNJo151&cmd=call";
    $extra .= "&SEND_AUTH=".$model->auth;
    $extra .= "&SEND_CLASSCD=".$model->select_class;
    $extra .= "&SEND_CURRICULUM_CD=".$model->select_curriculum_cd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availheight);\"";
    $arg["button"]["Z071"] = knjCreateBtn($objForm, "Z071", "前籍校科目コード", $extra);

    //教科コード
    $extra  = " onClick=\" wopen('".REQUESTROOT."/Z/KNJZ061/knjz061index.php?";
    $extra .= "SEND_PRGID=KNJo151&cmd=call";
    $extra .= "&SEND_AUTH=".$model->auth;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availheight);\"";
    $arg["button"]["Z061"] = knjCreateBtn($objForm, "Z061", "教科コード", $extra);

    //更新ボタンを作成する
    if (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->schregno != "" && $model->dataRow != "") {
        $extra  = "onclick=\"return btn_submit('preupdate');\"";
        $arg["button"]["btn_preupdate"] = knjCreateBtn($objForm, "btn_preupdate", "仮 更 新", $extra);
    }

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

    //前籍校情報ボタン
    $extra = "&SUBWIN=SUBWIN3','SUBWIN3',0,0,screen.availWidth,screen.availheight);";
    $prgId = KNJX_ANOTHER;
    $subdata = "wopen('".REQUESTROOT."/X/KNJX_ANOTHER/knjx_anotherindex.php?&SEND_AUTH=".$model->auth."&SEND_PRGID=KNJo151&SEND_SCHREGNO=".$model->schregno.$extra;
    $arg["button"]["btn_zenseki"] = knjCreateBtn($objForm, "btn_zenseki", "前籍校情報", "onclick=\"$subdata\"");
}

//Hidden作成
function makeHidden(&$objForm, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
}

?>
