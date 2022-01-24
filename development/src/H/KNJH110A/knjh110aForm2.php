<?php

require_once('for_php7.php');

class knjh110aform2
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh110aindex.php", "", "edit");
        //NO001
        $valueIsField = false;
        if (isset($model->schregno) && isset($model->regddate) && !isset($model->warning) && $model->cmd != "contedit") {
            $Row = knjh110aQuery::getRow($model->regddate, $model->schregno, $model->subclassCd, $model->condition, $model->seq);
            $contcd = $Row["CONTENTS"];

            if ($model->Properties["useCurriculumcd"] == '1') {
                $Row["SUBCLASSCD"]=$Row["CLASSCD"].'-'.$Row["SCHOOL_KIND"].'-'.$Row["CURRICULUM_CD"].'-'.$Row["SUBCLASSCD"];
            }
        } else {
            $Row =& $model->field;
            $contcd = $Row["CONTENTS1"];
            $valueIsField = true;
        }

        //登録日付
        $date_ymd = strtr($Row["REGDDATE"], "-", "/");
        $arg["data"]["REGDDATE"] = View::popUpCalendar($objForm, "REGDDATE", $date_ymd);

        $db = Query::dbCheckOut();

        //科目名
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query = knjh110aQuery::getSchoolKind($model);
            $model->schoolkind = $db->getOne($query);
        }
        $opt = array();
        $value_flg = false;
        $query = knjh110aQuery::getName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["SUBCLASSCD"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $Row["SUBCLASSCD"] = ($Row["SUBCLASSCD"] && $value_flg) ? $Row["SUBCLASSCD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $Row["SUBCLASSCD"], $opt, $extra, 1);

        //設定区分ラジオボタン 1:増加単位認定 2:学校外認定(他校履修) 3:高等学校卒業程度認定単位
        $opt_condition = array(1, 2, 3);
        $Row["CONDITION_DIV"] = (!$Row["CONDITION_DIV"]) ? "1":$Row["CONDITION_DIV"];
        $click = " OnClick=\" changecondition(this); return btn_submit('contedit')\"";
        $extra = array("id=\"CONDITION1\"".$click, "id=\"CONDITION2\"".$click, "id=\"CONDITION3\"".$click);
        $radioArray = knjCreateRadio($objForm, "CONDITION_DIV", $Row["CONDITION_DIV"], $extra, $opt_condition, get_count($opt_condition));
        foreach ($radioArray as $key => $val) {
            $arg["data"][str_replace("_DIV", "", $key)] = $val;
        }

        //資格内容コンボ
        $cont1able = "";
        $cont2able = "";
        $contval = $valueIsField ? $Row["CONTENTS2"] : $Row["CONTENTS"];
        $combomode = 1;
        $namecd = "H305";
        if ($Row["CONDITION_DIV"] == 3) {
            $cont1able = "disabled";
            $arg["CONTENTS1_ON"] = "style=visibility:hidden";
            $arg["CONTENTS2_ON"] = "style=visibility:visible";
        } else {
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
        $result = $db->query(knjh110aQuery::getContName($contcd, $namecd));
        $contname = "";
        while ( $row = $result -> fetchRow(DB_FETCHMODE_ASSOC) ) {
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
        $arg["data"]["CREDITS"] = $this->createText($objForm, "CREDITS", $Row["CREDITS"], "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"", 2, 2);

        if ($model->Properties["knjh110aNotShowCreditBanking"] != "1") {
            $arg["showCreditBanking"] = "1";
            //残数
            $db = Query::dbCheckOut();
            $zansuu = $db->getOne(knjh110aQuery::getZansuu($model->schregno, $Row["SUBCLASSCD"]));
            $arg["data"]["ZANSUU"] = ($Row["CONDITION_DIV"] == 1 && 0 < $zansuu) ? $zansuu : 0;
            Query::dbCheckIn($db);
            //クレジットバンキングボタン
            $extra  = strlen($model->schregno) ? "" : "disabled ";
            //$extra .= "style=\"width:150px;\" ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $extra .= "onClick=\" wopen('".REQUESTROOT."/H/KNJH110A_2/knjh110a_2index.php?program_id=".PROGRAMID."&SEND_PRGID=KNJH110A&SEND_AUTH={$model->auth}&SEND_SCHREGNO={$model->schregno}&SEND_CLASSCD={$Row["CLASSCD"]}&SEND_SUBCLASSCD={$Row["SUBCLASSCD"]}&SEND_CURRICULUM_CD={$Row["CURRICULUM_CD"]}&SEND_GRADE=$model->grade','SUBWIN3',0,0,screen.availWidth,screen.availHeight);\"";
            } else {
                $extra .= "onClick=\" wopen('".REQUESTROOT."/H/KNJH110A_2/knjh110a_2index.php?program_id=".PROGRAMID."&SEND_PRGID=KNJH110A&SEND_AUTH={$model->auth}&SEND_SCHREGNO={$model->schregno}&SEND_SUBCLASSCD={$Row["SUBCLASSCD"]}&SEND_GRADE=$model->grade','SUBWIN3',0,0,screen.availWidth,screen.availHeight);\"";
            }
            $arg["button"]["btn_bank"] = knjCreateBtn($objForm, "btn_bank", "クレジットバンキング", $extra);
        }

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

        //増加単位のCSV処理ボタン
        //セキュリティーチェック
        $db = Query::dbCheckOut();
        $securityCnt = $db->getOne(knjh110aQuery::getSecurityHigh());
        Query::dbCheckIn($db);
        $csvSetName = "ＣＳＶ処理";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル処理";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_H110A/knjx_h110aindex.php?program_id=".PROGRAMID."&SEND_PRGID=KNJH110A&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "増加単位の".$csvSetName, $extra);
        }

        //hidden
        //回数
        $objForm->ae($this->createHidden("SEQ", $Row["SEQ"]));
        //コマンド
        $objForm->ae($this->createHidden("cmd"));
        //更新日
        $objForm->ae($this->createHidden("UPDATED", $Row["UPDATED"]));

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
                $arg["reload"]  = "window.open('knjh110aindex.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";
        }

        View::toHTML($model, "knjh110aForm2.html", $arg);
    }

    //資格内容コンボ設定
    public function makeContent($db, $objForm, $combomode, $contcd)
    {
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

    public function getCmbVal($db, $namecd)
    {

        $opt_data = array();
        $query = knjh110aQuery::getContents($namecd);
        $result = $db->query($query);
        while ( $row = $result-> fetchRow(DB_FETCHMODE_ASSOC) ) {
            array_walk($row, "htmlspecialchars_array");
            $opt_data[] = array("label" => $row["NAMECD2"]."&nbsp;".$row["NAME1"],"value" => $row["NAMECD2"]);
        }
        $result->free();

        return $opt_data;
    }

    public function getContVal($opt, $cd)
    {

        $dataFlg = false;
        for ($i = 0; $i < get_count($opt); $i++) {
            if ($opt[$i]["value"] == $cd) {
                $dataFlg = true;
            }
        }
        if (!$dataFlg) {
            $cd = $opt["0"]["value"];
        }
        return $cd;
    }

    //コンボ作成
    public function createCombo(&$objForm, $name, $value, $options, $extra, $size)
    {
        $objForm->ae(array("type"    => "select",
                            "name"      => $name,
                            "size"      => $size,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "options"   => $options));
        return $objForm->ge($name);
    }

    //テキスト作成
    public function createText(&$objForm, $name, $value, $extra, $size, $maxlen)
    {
        //単位数
        $objForm->ae(array("type"      => "text",
                            "name"      => $name,
                            "size"      => $size,
                            "maxlength" => $maxlen,
                            "extrahtml" => $extra,
                            "value"     => $value));
        return $objForm->ge($name);
    }

    //ボタン作成
    public function createBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae(array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ));
        return $objForm->ge($name);
    }

    //Hidden作成
    public function createHidden($name, $value = "")
    {
        $opt_hidden = array();
        $opt_hidden = array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value);
        return $opt_hidden;
    }
}
