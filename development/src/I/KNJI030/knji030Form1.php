<?php

require_once('for_php7.php');

class knji030Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"] = $objForm->get_start("edit", "POST", "knji030index.php", "", "edit");

        if (isset($model->schregno) && !isset($model->warning)) {
            $grad_Row = knji030Query::getGradData($model->schregno);
        } else {
            $grad_Row =& $model->field;
        }

        $db = Query::dbCheckOut();
        if ($model->schregno !="") {
            $arg["header"]["NAME_SHOW"] = $grad_Row["SCHREGNO"]."　：　".$grad_Row["NAME_SHOW"];
        }
        //--------------------------旧氏名--------------------------
        $arg["data"] = $grad_Row;
        //--------------------------現氏名--------------------------
        //現氏名
        $extra = "onChange=\"\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $grad_Row["NAME"], "NAME", 80, 120, $extra);

        //現氏名表示用
        $extra = "onChange=\"\"";
        $arg["data"]["NAME_SHOW"] = knjCreateTextBox($objForm, $grad_Row["NAME_SHOW"], "NAME_SHOW", 20, 30, $extra);

        //現氏名かな
        $extra = "onChange=\"\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $grad_Row["NAME_KANA"], "NAME_KANA", 80, 240, $extra);

        //現氏名英字
        $extra = "onblur=\"moji_hantei(this);\" STYLE=\"ime-mode:disabled\"";
        $arg["data"]["NAME_ENG"] = knjCreateTextBox($objForm, $grad_Row["NAME_ENG"], "NAME_ENG", 40, 40, $extra);

        //--------------------------現住所------------------------------------------
        //郵便番号
        $arg["data"]["CUR_ZIPCD"] = View::popUpZipCode(
            $objForm,
            "CUR_ZIPCD",
            $grad_Row["CUR_ZIPCD"],
            "CUR_ADDR1"
        ) .'&nbsp;' .
                                                        $objForm->ge(
                                                            "btn_search1"
                                                        );

        //地区コード
        $query  = knji030Query::getAreaData();
        $result = $db->query($query);

        //地区コンボボックスの中身を作成------------------------------
        $opt_areacd   = array();
        $opt_areacd[] = array("label" => "","value" => "00");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_areacd[] = array("label" => substr($row["NAMECD2"], "0", "1").":".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]
                                );
        }
        $extra = "onChange=\"\"";
        $arg["data"]["CUR_AREACD"] = knjCreateCombo($objForm, "CUR_AREACD", $grad_Row["CUR_AREACD"], $opt_areacd, $extra, 1);

        //住所１
        $extra = "onChange=\"\"";
        $arg["data"]["CUR_ADDR1"] = knjCreateTextBox($objForm, $grad_Row["CUR_ADDR1"], "CUR_ADDR1", 50, 90, $extra);

        //住所２
        $extra = "onChange=\"\"";
        $arg["data"]["CUR_ADDR2"] = knjCreateTextBox($objForm, $grad_Row["CUR_ADDR2"], "CUR_ADDR2", 50, 90, $extra);

        //住所フラグ
        if ($grad_Row["CUR_ADDR_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["CUR_ADDR_FLG"] = knjCreateCheckBox($objForm, "CUR_ADDR_FLG", "1", $extra);

        //住所不明フラグ
        if ($grad_Row["UNKNOWN_ADDR_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["UNKNOWN_ADDR_FLG"] = knjCreateCheckBox($objForm, "UNKNOWN_ADDR_FLG", "1", $extra);

        //(英字)住所１
        $extra = "onblur=\"moji_hantei(this);\" STYLE=\"ime-mode:disabled\"";
        $arg["data"]["CUR_ADDR1_ENG"] = knjCreateTextBox($objForm, $grad_Row["CUR_ADDR1_ENG"], "CUR_ADDR1_ENG", 50, 70, $extra);

        //(英字)住所２
        $extra = "onblur=\"moji_hantei(this);\" STYLE=\"ime-mode:disabled\"";
        $arg["data"]["CUR_ADDR2_ENG"] = knjCreateTextBox($objForm, $grad_Row["CUR_ADDR2_ENG"], "CUR_ADDR2_ENG", 50, 70, $extra);

        //電話番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["CUR_TELNO"] = knjCreateTextBox($objForm, $grad_Row["CUR_TELNO"], "CUR_TELNO", 14, 14, $extra);

        //ＦＡＸ番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["CUR_FAXNO"] = knjCreateTextBox($objForm, $grad_Row["CUR_FAXNO"], "CUR_FAXNO", 14, 14, $extra);

        //Ｅ－ＭＡＩＬ
        $extra = "onblur=\"this.value=checkEmail(this.value)\"";
        $arg["data"]["CUR_EMAIL"] = knjCreateTextBox($objForm, $grad_Row["CUR_EMAIL"], "CUR_EMAIL", 20, 20, $extra);

        //急用連絡先
        $extra = "onChange=\"\"";
        $arg["data"]["CUR_EMERGENCYCALL"] = knjCreateTextBox($objForm, $grad_Row["CUR_EMERGENCYCALL"], "CUR_EMERGENCYCALL", 50, 75, $extra);

        //急用電話番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["CUR_EMERGENCYTELNO"] = knjCreateTextBox($objForm, $grad_Row["CUR_EMERGENCYTELNO"], "CUR_EMERGENCYTELNO", 14, 14, $extra);

        //--------------------------実家------------------------------------------
        //郵便番号
        $arg["data"]["ZIPCD"] = View::popUpZipCode(
            $objForm,
            "ZIPCD",
            $grad_Row["ZIPCD"],
            "ADDR1"
        ) .'&nbsp;' .
                                                    $objForm->ge(
                                                        "btn_search2"
                                                    );
        $extra = "onChange=\"\"";
        $arg["data"]["AREACD"] = knjCreateCombo($objForm, "AREACD", $grad_Row["AREACD"], $opt_areacd, $extra, 1);

        //住所１
        $extra = "onChange=\"\"";
        $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $grad_Row["ADDR1"], "ADDR1", 50, 90, $extra);

        //住所２
        $extra = "onChange=\"\"";
        $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $grad_Row["ADDR2"], "ADDR2", 50, 90, $extra);

        //電話番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $grad_Row["TELNO"], "TELNO", 14, 14, $extra);

        //ＦＡＸ番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["FAXNO"] = knjCreateTextBox($objForm, $grad_Row["FAXNO"], "FAXNO", 14, 14, $extra);

        //備考
        $extra = "onChange=\"\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $grad_Row["REMARK"], "REMARK", 80, 80, $extra);

        //ボタン
        $extra = "$disabled onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "OLD_NAME", $grad_Row["OLD_NAME"]);
        knjCreateHidden($objForm, "OLD_NAME_SHOW", $grad_Row["OLD_NAME_SHOW"]);
        knjCreateHidden($objForm, "OLD_NAME_KANA", $grad_Row["OLD_NAME_KANA"]);
        knjCreateHidden($objForm, "OLD_NAME_ENG", $grad_Row["OLD_NAME_ENG"]);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knji030Form1.html", $arg);
    }
}
