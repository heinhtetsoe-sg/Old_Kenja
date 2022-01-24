<?php

require_once('for_php7.php');

class knje066Form2
{
    function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje066index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->checked) && ($model->cmd == "edit_select" || $model->cmd == "reset")) {
            $Row = $db->getRow(knje066Query::getAnother($db, $model, "ONE"), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //入力方法
        $opt_mode_div = array(1, 2);
        if ($model->mode) {
            $model->mode = $model->mode;
        } else {
            $selectSub = preg_split("/-/", $model->select_subclass);
            $model->mode = substr($selectSub[3], 0, 2) != "99" ? "1" : "2";
        }

        $extra = array("id=\"MODE1\" onclick=\"return btn_submit('edit_src');\"", "id=\"MODE2\" onclick=\"return btn_submit('edit_src');\"");
        $radioArray = knjCreateRadio($objForm, "MODE", $model->mode, $extra, $opt_mode_div, get_count($opt_mode_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //処理年度
        $arg["data"]["YEAR"] = knjCreateTextBox($objForm, $model->select_year, "YEAR", 4, 4, $extra);

        //修得方法コンボ
        $query = knje066Query::getSyutoku();
        $extra = "onChange=\"return btn_submit('edit_src');\"";
        makeCombo($objForm, $arg, $db, $query, $model->select_schoolcd, "SCHOOLCD", $extra, 1);

        //教育課程コンボ
        $query = knje066Query::getNameMst("Z018");
        $extra = "onChange=\"return btn_submit('edit_src');\"";
        makeCombo($objForm, $arg, $db, $query, $model->select_curriculum_cd, "CURRICULUM_CD", $extra, 1);

        //教育課程コンボ
        $query = knje066Query::getNameMst("Z018");
        $extra = "onChange=\"return btn_submit('edit_src');\"";
        makeCombo($objForm, $arg, $db, $query, $model->select_rep_curriculum_cd, "REP_CURRICULUM_CD", $extra, 1, "BLANK");

        //教科コンボ
        $query = knje066Query::getClass_mst($model, "ALL", "ANOTHER");
        $extra = "onChange=\"return btn_submit('edit_src');\"";
        makeCombo($objForm, $arg, $db, $query, $model->select_class, "CLASSCD", $extra, 1);

        //教科コンボ
        $query = knje066Query::getClass_mst($model, "ALL", "");
        $extra = "onChange=\"return btn_submit('edit_src');\"";
        makeCombo($objForm, $arg, $db, $query, $model->select_rep_class, "REP_CLASSCD", $extra, 1, "BLANK");
        //入力方法による表示の切り替え
        switch ($model->mode) {
            case "1":
                $arg["MODE1"] = "1";
                
                //科目コンボ
                $query = knje066Query::getSubclass_mst($model, $model->select_class, $model->select_curriculum_cd, "ANOTHER");
                $extra = "onChange=\"return btn_submit('edit_src');\"";
                makeCombo($objForm, $arg, $db, $query, $model->select_all_subclass, "SUBCLASSCD", $extra, 1);

                //登録表示設定
                $curval = $db->getRow(knje066Query::getNameValue("W002", $model->select_curriculum_cd), DB_FETCHMODE_ASSOC);
                $classval = $db->getRow(knje066Query::getClass_mst($model, "ONE"), DB_FETCHMODE_ASSOC);
                $subval = $db->getRow(knje066Query::getSubclassValue($model), DB_FETCHMODE_ASSOC);

                $class_cd_name = $classval["CLASSCD"].$classval["CLASSNAME"] ? $classval["CLASSCD"].":".$classval["CLASSNAME"] : "";

                //登録教育課程
                $arg["data"]["CREDIT_CURRICULUM_CD"] = $curval["VALUE"];
                //登録教科
                $arg["data"]["CREDIT_CLASSCD"]       = $class_cd_name;
                //登録科目
                $arg["data"]["CREDIT_ADMITSCD"]      = $subval["VALUE"];
                
                //科目コンボ
                $query = knje066Query::getSubclass_mst($model, $model->select_rep_class, $model->select_rep_curriculum_cd, "");
                $extra = "onChange=\"return btn_submit('edit_src');\"";
                makeCombo($objForm, $arg, $db, $query, $model->select_rep_all_subclass, "REP_SUBCLASSCD", $extra, 1, "BLANK");
                break;
            case "2":
                $arg["MODE2"] = "1";
                //科目コードテキスト
                $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
                $subclass = $Row["SUBCLASSCD"] ? $Row["SUBCLASSCD"] : $model->select_subclass ;
                $arg["data"]["SUBCLASSCD"] = knjCreateTextBox($objForm, substr($subclass, -4), "SUBCLASSCD", 4, 4, $extra);

                //科目名称
                $arg["data"]["SUBCLASSNAME"] = knjCreateTextBox($objForm, $Row["SUBCLASSNAME"], "SUBCLASSNAME", 60, 60, "");

                //科目略称
                $arg["data"]["SUBCLASSABBV"] = knjCreateTextBox($objForm, $Row["SUBCLASSABBV"], "SUBCLASSABBV", 15, 15, "");

                break;
        }

        //評価テキスト
        $extra = "onblur=\"return check(this)\" STYLE=\"text-align: right\"";
        $arg["data"]["VALUATION"] = knjCreateTextBox($objForm, $Row["VALUATION"], "VALUATION", 2, 2, $extra);

        //修得単位テキスト
        $extra = "onblur=\"return check(this)\" STYLE=\"text-align: right\"";
        $Row["GET_CREDIT"] = $Row["GET_CREDIT"] ? $Row["GET_CREDIT"] : $subval["CREDIT"];
        $arg["data"]["GET_CREDIT"] = knjCreateTextBox($objForm, $Row["GET_CREDIT"], "GET_CREDIT", 2, 2, $extra);

        //履修単位テキスト
        $extra = "onblur=\"return check(this)\" STYLE=\"text-align: right\"";
        $Row["COMP_CREDIT"] = $Row["COMP_CREDIT"] ? $Row["COMP_CREDIT"] : $subval["CREDIT"];
        $arg["data"]["COMP_CREDIT"] = knjCreateTextBox($objForm, $Row["COMP_CREDIT"], "COMP_CREDIT", 2, 2, $extra);

        //評価テキスト
        $extra = "onblur=\"return check(this)\" STYLE=\"text-align: right\"";
        $arg["data"]["REP_VALUATION"] = knjCreateTextBox($objForm, $Row["REP_VALUATION"], "REP_VALUATION", 2, 2, $extra);

        //修得単位テキスト
        $extra = "onblur=\"return check(this)\" STYLE=\"text-align: right\"";
        $arg["data"]["REP_GET_CREDIT"] = knjCreateTextBox($objForm, $Row["REP_GET_CREDIT"], "REP_GET_CREDIT", 2, 2, $extra);

        //履修単位テキスト
        $extra = "onblur=\"return check(this)\" STYLE=\"text-align: right\"";
        $arg["data"]["REP_COMP_CREDIT"] = knjCreateTextBox($objForm, $Row["REP_COMP_CREDIT"], "REP_COMP_CREDIT", 2, 2, $extra);

        //備考テキスト
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 60, 60, "");

        //前籍校コンボ
        $query = knje066Query::getFinSchoolcd($model);
        makeCombo($objForm, $arg, $db, $query, $Row["FORMER_REG_SCHOOLCD"], "FORMER_REG_SCHOOLCD", "", 1, "BLANK");
        
        //修得日
        $arg["data"]["GET_DATE"] = View::popUpCalendar($objForm ,"GET_DATE" ,str_replace("-","/", $Row["GET_DATE"]));

        //学籍番号
        if ($model->knje066schreg != "") {
            $arg["data"]["SCHREGNO"] = "(学籍番号：".$model->knje066schreg.")";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        switch (VARS::post("cmd")) {
            case "add" :
            case "update" :
            //case "edit_src" :
            case "delete" :
                $arg["jscript"] = "window.open('knje066index.php?cmd=right_list','right_frame');";
                break;
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje066Form2.html", $arg); 
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
        $default = $db->getOne(knje066Query::getNameDefault("Z018", ""));
        $count = $db->getOne(knje066Query::getNameDefault("Z018", "COUNT"));
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
    $extra .= "SEND_PRGID=KNJE066&cmd=call";
    $extra .= "&SEND_AUTH=".$model->auth;
    $extra .= "&SEND_CLASSCD=".$model->select_class;
    $extra .= "&SEND_CURRICULUM_CD=".$model->select_curriculum_cd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["Z071"] = knjCreateBtn($objForm, "Z071", "科目コード", $extra);

    //教科コード
    $extra  = " onClick=\" wopen('".REQUESTROOT."/Z/KNJZ061/knjz061index.php?";
    $extra .= "SEND_PRGID=KNJE066&cmd=call";
    $extra .= "&SEND_AUTH=".$model->auth;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["Z061"] = knjCreateBtn($objForm, "Z061", "教科コード", $extra);

    //更新ボタンを作成する
    if (AUTHORITY >= DEF_UPDATE_RESTRICT) {
        $extra  = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    }
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

    //前籍校情報ボタン
    $extra = "&SUBWIN=SUBWIN3','SUBWIN3',0,0,screen.availWidth,screen.availHeight);";
    $prgId = "KNJX_ANOTHER";
    $subdata = "wopen('".REQUESTROOT."/X/KNJX_ANOTHER/knjx_anotherindex.php?&SEND_AUTH=".$model->auth."&SEND_PRGID=KNJE066&SEND_SCHREGNO=".$model->schregno.$extra;
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
