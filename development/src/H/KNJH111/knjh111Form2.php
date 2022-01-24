<?php

require_once('for_php7.php');

class knjh111form2 {
    function main(&$model) {
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh111index.php", "", "edit");
        $firstFlg = false;
        if (isset($model->schregno) && isset($model->regddate) && !isset($model->warning) && $model->cmd != "contedit"){
            $query = knjh111Query::getRow($model->regddate,$model->schregno,$model->subclasscd,$model->condition,$model->seq);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $contents = $Row["CONTENTS"];

            if (!is_array($Row)) {
                $firstFlg = true;
            }
        } else {
            $Row =& $model->field;
            $contents = $Row["CONTENTSTEXT"];
            if ($Row["CONDITION_DIV"] == 1) {
                $Row["CONTENTS"] = $model->field["CONTENTS1"];
            } else {
                $Row["CONTENTS"] = $model->field["CONTENTS2"];
            }
        }

        //学校校種取得 ※教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query = knjh111Query::getSchoolKind($model);
            $model->schoolkind = $db->getOne($query);
        }

        //登録日付
        $Row["REGDDATE"] = $Row["REGDDATE"] ? $Row["REGDDATE"] : CTRL_DATE;
        $date_ymd = strtr($Row["REGDDATE"],"-","/");
        $arg["data"]["REGDDATE"] = View::popUpCalendar($objForm, "REGDDATE", $date_ymd);

        //設定区分ラジオボタン 1:資格 2:その他
        $opt_condition = array(1, 2);
        $Row["CONDITION_DIV"] = (!$Row["CONDITION_DIV"]) ? "1":$Row["CONDITION_DIV"];
        $click = " OnClick=\" changecondition(this);\"";
        $extra = array("id=\"CONDITION1\"".$click, "id=\"CONDITION2\"".$click);
        $radioArray = knjCreateRadio($objForm, "CONDITION_DIV", $Row["CONDITION_DIV"], $extra, $opt_condition, get_count($opt_condition));
        foreach($radioArray as $key => $val) $arg["data"][str_replace("_DIV", "", $key)] = $val;

        //資格内容コンボ
        $cont1able = "";
        $cont2able = "";
        if ($Row["CONDITION_DIV"] == 1){
            $cont2able = "disabled";
            $contval = "";
            $arg["CONTENTS1_ON"] = "style=visibility:visible";
            $arg["CONTENTS2_ON"] = "style=visibility:hidden";
        } else {
            $cont1able = "disabled";
            $contval = $Row["CONTENTS"];
            $arg["CONTENTS1_ON"] = "style=visibility:hidden";
            $arg["CONTENTS2_ON"] = "style=visibility:visible";
        }

        $opt_contents = $this->makeContent($db);
        $dataFlg = false;
        for ($i = 0; $i < get_count($opt_contents); $i++) {
            if ($opt_contents[$i]["label"] == $contents) {
                $dataFlg = true;
                $contentsCmb = $opt_contents[$i]["value"];
            }
        }
        if (!$dataFlg) {
            $contentsCmb = $opt_contents["0"]["value"];
        }
        if ($firstFlg) {//何も選択されていない一番最初の時
            $contents = $opt_contents["0"]["label"];
        }
        $arg["data"]["CONTENTS1"] =  $this->createCombo($objForm, "CONTENTS1", $contentsCmb, $opt_contents, $cont1able." onChange=\"return btn_submit('contedit')\"", 1);

        //資格テキスト
        $contname = "";
        if ($Row["CONDITION_DIV"] == "1") {
            if (isset($model->warning)) {
                $contname = $Row["CONTENTSTEXT"];
            } else {
                $contname = $contents;
            }
        }
        $arg["data"]["CONTENTSTEXT"] = $this->createText($objForm, "CONTENTSTEXT", $contname, $cont1able, 40, 60);

        //その他内容
        $arg["data"]["CONTENTS2"] = $this->createText($objForm, "CONTENTS2", $contval, $cont2able, 60, 90);

        //備考
        $arg["data"]["REMARK"] = $this->createText($objForm, "REMARK", $Row["REMARK"], "", 60, 90);

        //単位数
        $arg["data"]["CREDITS"] = $this->createText($objForm, "CREDITS", $Row["CREDITS"], "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"", 2, 2);

        //追加ボタン
        $arg["button"]["btn_add"] = $this->createBtn($objForm, "btn_add", "追 加", "onclick=\"return btn_submit('add');\"");

        //修正ボタン
        $arg["button"]["btn_update"] = $this->createBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");

        //削除ボタン
        $arg["button"]["btn_del"] = $this->createBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete');\"");

        //クリアボタン
        $arg["button"]["btn_reset"] = $this->createBtn($objForm, "btn_reset", "取 消", "onclick=\"return Btn_reset('clear');\"");

        //終了ボタン
        $arg["button"]["btn_end"] = $this->createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hidden
        //回数
        $objForm->ae($this->createHidden("SEQ",$Row["SEQ"]));
        //コマンド
        $objForm->ae($this->createHidden("cmd"));
        //更新日
        $objForm->ae($this->createHidden("UPDATED",$Row["UPDATED"]));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
                $arg["reload"]  = "window.open('knjh111index.php?cmd=list&SCHREGNO={$model->schregno}','right_frame');";
        }

        View::toHTML($model, "knjh111Form2.html", $arg);
    }

/******************************************** 以下関数 *************************************************/

    //資格内容コンボ設定
    function makeContent($db) {
        $opt_data = array();
        $opt_data[0] = array("label" => "",
                             "value" => "");
        $query = knjh111Query::getContents();
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $opt_data[] = array("label" => $row["NAME1"],
                                "value" => $row["NAMECD2"]);
        }
        $result->free();

        return $opt_data;
    }

    function getContVal($opt, $cd) {

        $dataFlg = false;
        for ($i = 0; $i < get_count($opt); $i++) {
            if ($opt[$i]["value"] == $cd) $dataFlg = true;
        }
        if (!$dataFlg) $cd = $opt["0"]["value"];
        return $cd;
    }

    //コンボ作成
    function createCombo(&$objForm, $name, $value, $options, $extra, $size) {
        $objForm->ae( array("type"      => "select",
                            "name"      => $name,
                            "size"      => $size,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "options"   => $options));
        return $objForm->ge($name);
    }

    //テキスト作成
    function createText(&$objForm, $name, $value, $extra, $size, $maxlen) {
        //単位数
        $objForm->ae( array("type"      => "text",
                            "name"      => $name,
                            "size"      => $size,
                            "maxlength" => $maxlen,
                            "extrahtml" => $extra,
                            "value"     => $value));
        return $objForm->ge($name);
    }

    //ボタン作成
    function createBtn(&$objForm, $name, $value, $extra) {
        $objForm->ae( array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ) );
        return $objForm->ge($name);
    }

    //Hidden作成
    function createHidden($name, $value = "") {
        $opt_hidden = array();
        $opt_hidden = array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value);
        return $opt_hidden;
    }

}
?>
