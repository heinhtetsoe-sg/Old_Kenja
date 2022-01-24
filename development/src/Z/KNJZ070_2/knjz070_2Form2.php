<?php

require_once('for_php7.php');

class knjz070_2Form2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz070_2index.php", "", "edit");
        if (isset($model->subclasscd) && !isset($model->warning)) {
            if ($model->cmd !== 'main') {
                $Row = knjz070_2Query::getRow($model, $model->school_kind, $model->curriculum_cd, $model->subclasscd);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //学校名称取得
        $query = knjz070_2Query::getSchoolName($model);
        $model->schoolName = $db->getOne($query);

        //教科取得
        $query = knjz070_2Query::getClassData($model);
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
            $query = knjz070_2Query::getNamecd('Z018');
            $value_flg = false;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_kyouiku[] = array('label' => $row["LABEL"],
                                       'value' => $row["VALUE"]);
                if ($Row["CURRICULUM_CD"] == $row["VALUE"]) {
                    $value_flg = true;
                }
            }
            $Row["CURRICULUM_CD"] = ($model->list_curriculum_cd != "" && $model->list_curriculum_cd != "99") ? $model->list_curriculum_cd : $Row["CURRICULUM_CD"];
            $Row["CURRICULUM_CD"] = ($Row["CURRICULUM_CD"] && $value_flg) ? $Row["CURRICULUM_CD"] : $opt_kyouiku[0]["value"];
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
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["SUBCLASSCD"] = knjCreateTextBox($objForm, $setSubclasscd, "SUBCLASSCD", 4, 4, $extra);
        } else {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["SUBCLASSCD"] = knjCreateTextBox($objForm, substr($Row["SUBCLASSCD"], 2, 5), "SUBCLASSCD", 4, 4, $extra);
        }

        //科目名称
        $extra = "";
        $arg["data"]["SUBCLASSNAME"] = knjCreateTextBox($objForm, $Row["SUBCLASSNAME"], "SUBCLASSNAME", 40, 60, $extra);

        //科目略称
        if ($model->Properties["SUBCLASS_MST_SUBCLASSABBV_SIZE"] != "") {
            $model->set_abbv = $model->Properties["SUBCLASS_MST_SUBCLASSABBV_SIZE"] * 2;
            $model->set_maxabbv = $model->Properties["SUBCLASS_MST_SUBCLASSABBV_SIZE"] * 3;
        } else {
            $model->set_abbv = 6;
            $model->set_maxabbv = 9;
        }
        $extra = "";
        $arg["data"]["SUBCLASSABBV"] = knjCreateTextBox($objForm, $Row["SUBCLASSABBV"], "SUBCLASSABBV", $model->set_abbv, $model->set_maxabbv, $extra);

        //科目名称英字
        $extra = "";
        $arg["data"]["SUBCLASSNAME_ENG"] = knjCreateTextBox($objForm, $Row["SUBCLASSNAME_ENG"], "SUBCLASSNAME_ENG", 50, 50, $extra);

        //科目略称英字
        $extra = "";
        $arg["data"]["SUBCLASSABBV_ENG"] = knjCreateTextBox($objForm, $Row["SUBCLASSABBV_ENG"], "SUBCLASSABBV_ENG", 20, 20, $extra);

        //科目名その他１
        $extra = "";
        $arg["data"]["SUBCLASSORDERNAME1"] = knjCreateTextBox($objForm, $Row["SUBCLASSORDERNAME1"], "SUBCLASSORDERNAME1", 60, 60, $extra);

        //科目名その他２
        $extra = "";
        $arg["data"]["SUBCLASSORDERNAME2"] = knjCreateTextBox($objForm, $Row["SUBCLASSORDERNAME2"], "SUBCLASSORDERNAME2", 60, 90, $extra);

        //科目名その他３
        $extra = "";
        $arg["data"]["SUBCLASSORDERNAME3"] = knjCreateTextBox($objForm, $Row["SUBCLASSORDERNAME3"], "SUBCLASSORDERNAME3", 60, 90, $extra);

        //表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["SHOWORDER"] = knjCreateTextBox($objForm, $Row["SHOWORDER"], "SHOWORDER", 2, 2, $extra);

        //調査書用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["SHOWORDER2"] = knjCreateTextBox($objForm, $Row["SHOWORDER2"], "SHOWORDER2", 2, 2, $extra);


        //通知表用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["SHOWORDER3"] = knjCreateTextBox($objForm, $Row["SHOWORDER3"], "SHOWORDER3", 2, 2, $extra);

        //調査書・指導要録用科目グループコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SUBCLASSCD2"] = knjCreateTextBox($objForm, $Row["SUBCLASSCD2"], "SUBCLASSCD2", 6, 6, $extra);

        //通知表用科目グループコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SUBCLASSCD3"] = knjCreateTextBox($objForm, $Row["SUBCLASSCD3"], "SUBCLASSCD3", 6, 6, $extra);


        //教科コード
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $setcd = "";
            $setcd = $Row["CLASSCD"].'-'.$Row["SCHOOL_KIND"].'-'.$Row["CLASSCD"];
            $setcd = ($model->list_classcd != "" && $model->list_classcd != "99-99-99") ? $model->list_classcd : $setcd;
            //エラー時セット用
            if ($setcd == '--') {
                $setcd = substr($model->field["SUBCLASSCD"], 0, 7);
            }
            $extra = "";
            $arg["data"]["CLASSCD"] = knjCreateCombo($objForm, "CLASSCD", $setcd, $opt, $extra, 1);
        } else {
            $extra = "";
            $arg["data"]["CLASSCD"] = knjCreateCombo($objForm, "CLASSCD", substr($Row["SUBCLASSCD"], 0, 2), $opt, $extra, 1);
        }

        //チェックボックス
        $extra  = " id=\"ELECTDIV\" ";
        $extra .= ($Row["ELECTDIV"] == 1) ? "checked" : "nocheck";
        $arg["data"]["ELECTDIV"] = knjCreateCheckBox($objForm, "ELECTDIV", "1", $extra);

        //特別支援
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $arg["support"] = 1;
            //状態区分
            $opt = array();
            $opt[] = array('label' => "", 'value' => "");
            $value_flg = false;
            $query = knjz070_2Query::getNamecd('A033');
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array( 'label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
                if ($Row["DETAIL001"] == $row["VALUE"]) {
                    $value_flg = true;
                }
            }
            $Row["DETAIL001"] = ($Row["DETAIL001"] && $value_flg) ? $Row["DETAIL001"] : $opt[0]["value"];
            $arg["data"]["DETAIL001"] = knjCreateCombo($objForm, "DETAIL001", $Row["DETAIL001"], $opt, "", 1);
        }

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return Btn_reset('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ070/knjz070index.php?year_code=".$model->year_code;
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        knjCreateHidden($objForm, "year_code", $model->year_code);

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz070_2index.php?cmd=list';";
        }
        View::toHTML($model, "knjz070_2Form2.html", $arg);
    }
}
