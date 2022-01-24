<?php

require_once('for_php7.php');

class knjz072Form2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz072index.php", "", "edit");
        if (isset($model->subclasscd) && !isset($model->warning)) {
            if ($model->cmd !== 'main') {
                $Row = knjz072Query::getRow($model, $model->school_kind, $model->curriculum_cd, $model->subclasscd);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //教育委員会用の学校コード取得
        $model->schoolcd = "";
        $model->schoolcd = $db->getOne(knjz072Query::getSchoolCd());

        //教科取得
        $query = knjz072Query::getClassData($model);
        $result = $db->query($query);
        $opt = array();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => htmlspecialchars($row["CLASSCD"].'-'.$row["SCHOOL_KIND"]."：".$row["CLASSNAME"]),
                               "value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CLASSCD"]);
            }
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => htmlspecialchars($row["CLASSCD"]."：".$row["CLASSNAME"]),
                               "value" => $row["CLASSCD"]);
            }
        }
        $result->free();

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
            //教育課程
            $opt_kyouiku = array();
            $query = knjz072Query::getNamecd('Z018');
            $value_flg = false;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_kyouiku[] = array('label' => $row["LABEL"],
                                       'value' => $row["VALUE"]);
                if ($value == $row["VALUE"]) {
                    $value_flg = true;
                }
            }
            $value = ($value && $value_flg) ? $value : $opt_kyouiku[0]["value"];
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["CURRICULUM_CD"] = knjCreateCombo($objForm, "CURRICULUM_CD", $Row["CURRICULUM_CD"], $opt_kyouiku, $extra, 1);
        }
        
        //科目コード
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $setSubclasscd = "";
            $setSubclasscd = substr($Row["SUBCLASSCD"], 2, 5);
            if (substr($setSubclasscd, 0, 1) == '-') {
                $setSubclasscd = substr($model->field["SUBCLASSCD"], 7, 4);
            }
            $objForm->ae(array("type"        => "text",
                                "name"        => "SUBCLASSCD",
                                "size"        => 4,
                                "maxlength"   => 4,
                                "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                                "value"       => $setSubclasscd ));
            $arg["data"]["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");
        } else {
            $objForm->ae(array("type"        => "text",
                                "name"        => "SUBCLASSCD",
                                "size"        => 4,
                                "maxlength"   => 4,
                                "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                                "value"       => substr($Row["SUBCLASSCD"], 2, 5) ));
            $arg["data"]["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");
        }
        
        //科目名称
        $objForm->ae(array("type"        => "text",
                            "name"        => "SUBCLASSNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "value"       => $Row["SUBCLASSNAME"] ));
        $arg["data"]["SUBCLASSNAME"] = $objForm->ge("SUBCLASSNAME");

        //科目略称
        if ($model->Properties["SUBCLASS_MST_SUBCLASSABBV_SIZE"] != "") {
            $model->set_abbv = $model->Properties["SUBCLASS_MST_SUBCLASSABBV_SIZE"] * 2;
            $model->set_maxabbv = $model->Properties["SUBCLASS_MST_SUBCLASSABBV_SIZE"] * 3;
        } else {
            $model->set_abbv = 6;
            $model->set_maxabbv = 9;
        }
        $objForm->ae(array("type"        => "text",
                            "name"        => "SUBCLASSABBV",
                            "size"        => $model->set_abbv,
                            "maxlength"   => $model->set_maxabbv,
                            "value"       => $Row["SUBCLASSABBV"] ));
        $arg["data"]["SUBCLASSABBV"] = $objForm->ge("SUBCLASSABBV");

        //科目名称英字
        $objForm->ae(array("type"        => "text",
                            "name"        => "SUBCLASSNAME_ENG",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "onblur=\"this.value=toAlphanumeric(this.value)\"",
                            "value"       => $Row["SUBCLASSNAME_ENG"] ));
        $arg["data"]["SUBCLASSNAME_ENG"] = $objForm->ge("SUBCLASSNAME_ENG");

        //科目略称英字
        $objForm->ae(array("type"        => "text",
                            "name"        => "SUBCLASSABBV_ENG",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "extrahtml"   => "onblur=\"this.value=toAlphanumeric(this.value)\"",
                            "value"       => $Row["SUBCLASSABBV_ENG"] ));
        $arg["data"]["SUBCLASSABBV_ENG"] = $objForm->ge("SUBCLASSABBV_ENG");

        //科目名その他１
        $objForm->ae(array("type"        => "text",
                            "name"        => "SUBCLASSORDERNAME1",
                            "size"        => 60,
                            "maxlength"   => 60,
                            "value"       => $Row["SUBCLASSORDERNAME1"] ));
        $arg["data"]["SUBCLASSORDERNAME1"] = $objForm->ge("SUBCLASSORDERNAME1");

        //科目名その他２
        $objForm->ae(array("type"        => "text",
                            "name"        => "SUBCLASSORDERNAME2",
                            "size"        => 60,
                            "maxlength"   => 60,
                            "value"       => $Row["SUBCLASSORDERNAME2"] ));
        $arg["data"]["SUBCLASSORDERNAME2"] = $objForm->ge("SUBCLASSORDERNAME2");

        //科目名その他３
        $objForm->ae(array("type"        => "text",
                            "name"        => "SUBCLASSORDERNAME3",
                            "size"        => 60,
                            "maxlength"   => 60,
                            "value"       => $Row["SUBCLASSORDERNAME3"] ));
        $arg["data"]["SUBCLASSORDERNAME3"] = $objForm->ge("SUBCLASSORDERNAME3");

        //表示順
        $objForm->ae(array("type"        => "text",
                            "name"        => "SHOWORDER",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"",
                            "value"       => $Row["SHOWORDER"] ));
        $arg["data"]["SHOWORDER"] = $objForm->ge("SHOWORDER");

        //調査書用表示順
        $objForm->ae(array("type"        => "text",
                            "name"        => "SHOWORDER2",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"",
                            "value"       => $Row["SHOWORDER2"] ));
        $arg["data"]["SHOWORDER2"] = $objForm->ge("SHOWORDER2");

        //通知表用表示順
        $objForm->ae(array("type"        => "text",
                            "name"        => "SHOWORDER3",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"",
                            "value"       => $Row["SHOWORDER3"] ));
        $arg["data"]["SHOWORDER3"] = $objForm->ge("SHOWORDER3");

        //教科コード
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $setcd = "";
            $setcd = $Row["CLASSCD"].'-'.$Row["SCHOOL_KIND"].'-'.$Row["CLASSCD"];
            //エラー時セット用
            if ($setcd == '--') {
                $setcd = substr($model->field["SUBCLASSCD"], 0, 7);
            }
            $objForm->ae(array("type"       => "select",
                                "name"       => "CLASSCD",
                                "size"       => "1",
                                "value"      => $setcd,
                                "options"    => $opt));
        } else {
            $objForm->ae(array("type"       => "select",
                                "name"       => "CLASSCD",
                                "size"       => "1",
                                "value"      => substr($Row["SUBCLASSCD"], 0, 2),
                                "options"    => $opt));
        }
        $arg["data"]["CLASSCD"] = $objForm->ge("CLASSCD");

        //選択
        $objForm->ae(array("type"        => "checkbox",
                            "name"        => "ELECTDIV",
                            "extrahtml"   => ($Row["ELECTDIV"]==1)? "checked" : "nocheck",
                            "value"       => "1" ));
        $arg["data"]["ELECTDIV"] = $objForm->ge("ELECTDIV");

        //追加ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ));
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ));
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return Btn_reset('reset');\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "year_code",
                            "value"     => $model->year_code
                            ));

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz072index.php?cmd=list';";
        }
        View::toHTML($model, "knjz072Form2.html", $arg);
    }
}
