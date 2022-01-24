<?php

require_once('for_php7.php');

class knjz071Form2
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz071index.php", "", "edit");
        if (isset($model->subclasscd) && !isset($model->warning) && $model->cmd != "edit2") {
            $Row = knjz071Query::getRow($model, $model->school_kind, $model->curriculum_cd, $model->subclasscd);
        } else {
            $Row =& $model->field;
        }
        
        $db = Query::dbCheckOut();
        
        //教科取得
        $query = knjz071Query::getClassData($model);
        $result = $db->query($query);
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $Row["CLASSCD_SET"] = ($Row["CLASSCD_SET"]) ? $Row["CLASSCD_SET"] : $opt[0]["value"];
        if (strlen($Row["CLASSCD_SET"]) > 7) {
            $Row["CLASSCD_SET"] = substr($Row["CLASSCD_SET"], 0, 7);
        }
        $Row["CLASSCD_SET"] = ($model->leftField["S_CLASSCD"] != "" && $model->leftField["S_CLASSCD"] != "99") ? $model->leftField["S_CLASSCD"] : $Row["CLASSCD_SET"];
        $extra = "";
        $arg["data"]["CLASSCD"] = knjCreateCombo($objForm, "CLASSCD", $Row["CLASSCD_SET"], $opt, $extra, 1);        
        
        //教育課程コード
        $opt_kyouiku = array();
        $query = knjz071Query::getNamecd('Z018');
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_kyouiku[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $Row["CURRICULUM_CD"] = ($model->leftField["S_CURRICULUM_CD"] != "" && $model->leftField["S_CURRICULUM_CD"] != "99") ? $model->leftField["S_CURRICULUM_CD"] : $Row["CURRICULUM_CD"];
        $value = ($value && $value_flg) ? $value : $opt_kyouiku[0]["value"];
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CURRICULUM_CD"] = knjCreateCombo($objForm, "CURRICULUM_CD", $Row["CURRICULUM_CD"], $opt_kyouiku, $extra, 1);

        //科目コード
        $setSubclasscd = "";
        $setSubclasscd = substr($Row["SUBCLASSCD"] ,2,5);
        if (substr($setSubclasscd, 0, 1) == '-') {
            $setSubclasscd = substr($model->field["SUBCLASSCD"], 7, 4);
        }
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSCD",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $setSubclasscd ));
        $arg["data"]["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");

        //科目名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "value"       => $Row["SUBCLASSNAME"] ));
        $arg["data"]["SUBCLASSNAME"] = $objForm->ge("SUBCLASSNAME");

        //科目略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSABBV",
                            "size"        => 6,
                            "maxlength"   => 9,
                            "value"       => $Row["SUBCLASSABBV"] ));
        $arg["data"]["SUBCLASSABBV"] = $objForm->ge("SUBCLASSABBV");

        //科目名称英字
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSNAME_ENG",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "onblur=\"this.value=toAlphanumeric(this.value)\"",
                            "value"       => $Row["SUBCLASSNAME_ENG"] ));
        $arg["data"]["SUBCLASSNAME_ENG"] = $objForm->ge("SUBCLASSNAME_ENG");

        //科目略称英字
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSABBV_ENG",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "extrahtml"   => "onblur=\"this.value=toAlphanumeric(this.value)\"",
                            "value"       => $Row["SUBCLASSABBV_ENG"] ));
        $arg["data"]["SUBCLASSABBV_ENG"] = $objForm->ge("SUBCLASSABBV_ENG");

        //科目名その他１
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSORDERNAME1",
                            "size"        => 60,
                            "maxlength"   => 60,
                            "value"       => $Row["SUBCLASSORDERNAME1"] ));
        $arg["data"]["SUBCLASSORDERNAME1"] = $objForm->ge("SUBCLASSORDERNAME1");

        //科目名その他２
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSORDERNAME2",
                            "size"        => 60,
                            "maxlength"   => 60,
                            "value"       => $Row["SUBCLASSORDERNAME2"] ));
        $arg["data"]["SUBCLASSORDERNAME2"] = $objForm->ge("SUBCLASSORDERNAME2");

        //科目名その他３
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSORDERNAME3",
                            "size"        => 60,
                            "maxlength"   => 60,
                            "value"       => $Row["SUBCLASSORDERNAME3"] ));
        $arg["data"]["SUBCLASSORDERNAME3"] = $objForm->ge("SUBCLASSORDERNAME3");

        //表示順
        $objForm->ae( array("type"        => "text",
                            "name"        => "SHOWORDER",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"",
                            "value"       => $Row["SHOWORDER"] ));
        $arg["data"]["SHOWORDER"] = $objForm->ge("SHOWORDER");

        //調査書用表示順
        $objForm->ae( array("type"        => "text",
                            "name"        => "SHOWORDER2",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"",
                            "value"       => $Row["SHOWORDER2"] ));
        $arg["data"]["SHOWORDER2"] = $objForm->ge("SHOWORDER2");

        //通知表用表示順
        $objForm->ae( array("type"        => "text",
                            "name"        => "SHOWORDER3",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"",
                            "value"       => $Row["SHOWORDER3"] ));
        $arg["data"]["SHOWORDER3"] = $objForm->ge("SHOWORDER3");

        //調査書・指導要録用科目グループコード
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSCD2",
                            "size"        => 6,
                            "maxlength"   => 6,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["SUBCLASSCD2"] ));
        $arg["data"]["SUBCLASSCD2"] = $objForm->ge("SUBCLASSCD2");

        //通知表用科目グループコード
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSCD3",
                            "size"        => 6,
                            "maxlength"   => 6,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["SUBCLASSCD3"] ));
        $arg["data"]["SUBCLASSCD3"] = $objForm->ge("SUBCLASSCD3");

        //選択
        $objForm->ae( array("type"        => "text",
                            "name"        => "VALUATION",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"",
                            "value"       => $Row["VALUATION"] ));
        $arg["data"]["VALUATION"] = $objForm->ge("VALUATION");

        //修得単位数
        $objForm->ae( array("type"        => "text",
                            "name"        => "GET_CREDIT",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"",
                            "value"       => $Row["GET_CREDIT"] ));
        $arg["data"]["GET_CREDIT"] = $objForm->ge("GET_CREDIT");

        //選択
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "ELECTDIV",
                            "extrahtml"   => ($Row["ELECTDIV"]==1)? "checked" : "nocheck",
                            "value"       => "1" ));
        $arg["data"]["ELECTDIV"] = $objForm->ge("ELECTDIV");

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return Btn_reset('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        if ($model->leftSField["SEND_PRGID"]) {
            $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"closeMethod();\"");
        } else {
            $extra  = "onclick=\"closeWin();\"";
            $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
        }

        //教科コード
        $extra  = " onClick=\" wopen('".REQUESTROOT."/Z/KNJZ061/knjz061index.php?";
        $extra .= "SEND_PRGID=KNJZ071&cmd=call";
        $extra .= "&SEND_AUTH=".$model->auth;
        $extra .= "&SEND_selectSchoolKind=".$model->selectSchoolKind;
        $extra .= "&SUBWIN=SUBWIN3','SUBWIN3',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["Z061"] = knjCreateBtn($objForm, "Z061", "教科コード", $extra);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_code",
                            "value"     => $model->year_code
                            ) );

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){ 
            $arg["reload"]  = "parent.left_frame.location.href='knjz071index.php?cmd=list';";
        }
        View::toHTML($model, "knjz071Form2.html", $arg); 
    }
}
?>
