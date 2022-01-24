<?php

require_once('for_php7.php');

class knjh110form2
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh110index.php", "", "edit");
        //NO001
        $valueIsField = false;
        if (isset($model->schregno) && isset($model->regddate) && !isset($model->warning) && $model->cmd != "contedit"){
            $Row = knjh110Query::getRow($model->regddate,$model->schregno,$model->subclasscd,$model->condition,$model->seq);
            $contcd = $Row["CONTENTS"];
        }else{
            $Row =& $model->field;
            $contcd = $Row["CONTENTS1"];
            $valueIsField = true;
        }

        //登録日付
        $date_ymd = strtr($Row["REGDDATE"],"-","/");
        $arg["data"]["REGDDATE"] = View::popUpCalendar($objForm, "REGDDATE", $date_ymd);

        $db = Query::dbCheckOut();

        //科目コンボ
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query = knjh110Query::getSchoolKind($model);
            $model->schoolkind = $db->getOne($query);
        }
        $query = knjh110Query::getName($model);
        $result = $db->query($query);
        $opt_subclasscd = array();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	        {
		        array_walk($row, "htmlspecialchars_array");
		        //NO002
		        $opt_subclasscd[] = array("label" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."&nbsp;".$row["SUBCLASSNAME"],"value" =>  $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]);
	        }
        } else {
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");
                //NO002
                $opt_subclasscd[] = array("label" => $row["SUBCLASSCD"]."&nbsp;".$row["SUBCLASSNAME"],"value" => $row["SUBCLASSCD"]);
            }
        }
        $result->free();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        if (strlen($Row["SUBCLASSCD"]) == 6) {
		        $Row["SUBCLASSCD"] = $model->subclasscd;
	        }
        }
        $arg["data"]["SUBCLASSCD"] = $this->createCombo($objForm, "SUBCLASSCD", $Row["SUBCLASSCD"], $opt_subclasscd, "", 1);

        //設定区分ラジオボタン 1:増加単位認定 2:学校外認定(他校履修) 3:高等学校卒業程度認定単位
        $opt_condition = array(1, 2, 3);
        $Row["CONDITION_DIV"] = (!$Row["CONDITION_DIV"]) ? "1":$Row["CONDITION_DIV"];
        $click = " OnClick=\" changecondition(this); return btn_submit('contedit')\"";
        $extra = array("id=\"CONDITION1\"".$click, "id=\"CONDITION2\"".$click, "id=\"CONDITION3\"".$click);
        $radioArray = knjCreateRadio($objForm, "CONDITION_DIV", $Row["CONDITION_DIV"], $extra, $opt_condition, get_count($opt_condition));
        foreach($radioArray as $key => $val) $arg["data"][str_replace("_DIV", "", $key)] = $val;

        //資格内容コンボ
        $cont1able = "";
        $cont2able = "";
        $contval = $valueIsField ? $Row["CONTENTS2"] : $Row["CONTENTS"];
        $combomode = 1;
        $namecd = "H305";
        if ($Row["CONDITION_DIV"] == 3){
            $cont1able = "disabled";
            $arg["CONTENTS1_ON"] = "style=visibility:hidden";
            $arg["CONTENTS2_ON"] = "style=visibility:visible";
        }else {
            $cont2able = "disabled";
            $contval = "";
            $arg["CONTENTS2_ON"] = "style=visibility:hidden";
            $arg["CONTENTS1_ON"] = "style=visibility:visible";
            if ($Row["CONDITION_DIV"] == 2) {
                $combomode = 2;
                $namecd = "H306";
            }
        }

        $opt_contents = $this->makeContent($db, $objForm, $combomode, $contcd);
        $contcd = $this->getContVal($opt_contents, $contcd);

        $arg["data"]["CONTENTS1"] =  $this->createCombo($objForm, "CONTENTS1", $contcd, $opt_contents, $cont1able." onChange=\"return btn_submit('contedit')\"", 1);

        //資格テキスト
        $result = $db->query(knjh110Query::getContName($contcd,$namecd));
        $contname = "";
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");
            $contname = $row["NAME1"];
        }

        $result->free();
        Query::dbCheckIn($db);
        $arg["data"]["CONTENTSTEXT"] = $this->createText($objForm, "CONTENTSTEXT", $contname, $cont1able, 40, 60);

        //合格科目名 NO001
        $arg["data"]["CONTENTS2"] = $this->createText($objForm, "CONTENTS2", $contval, $cont2able, 60, 90);

        //備考
        $arg["data"]["REMARK"] = $this->createText($objForm, "REMARK", $Row["REMARK"], "", 60, 90);

        //単位数
        $arg["data"]["CREDITS"] = $this->createText($objForm, "CREDITS", $Row["CREDITS"], "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\";", 2, 2);

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
        //学籍番号
        $objForm->ae($this->createHidden("SCHREGNO", $model->schregno));

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
                $arg["reload"]  = "window.open('knjh110index.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";
        }

        View::toHTML($model, "knjh110Form2.html", $arg);
    }

    //資格内容コンボ設定
    function makeContent($db, $objForm, $combomode, $contcd) {

        $opt_contents = array();

        if ($combomode == "1") {
            //H305コンボ作成
            $opt_contents = $this->getCmbVal($db, 'H305');
        } else {
            //H306コンボ作成
            $opt_contents = $this->getCmbVal($db, 'H306');
        }

        return $opt_contents;
    }

    function getCmbVal($db, $namecd) {

        $opt_data = array();
        $query = knjh110Query::getContents($namecd);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");
            $opt_data[] = array("label" => $row["NAMECD2"]."&nbsp;".$row["NAME1"],"value" => $row["NAMECD2"]);
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
    function createCombo(&$objForm, $name, $value, $options, $extra, $size)
    {
        $objForm->ae( array("type"      => "select",
                            "name"      => $name,
                            "size"      => $size,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "options"   => $options));
        return $objForm->ge($name);
    }

    //テキスト作成
    function createText(&$objForm, $name, $value, $extra, $size, $maxlen)
    {
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
    function createBtn(&$objForm, $name, $value, $extra)
    {
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
