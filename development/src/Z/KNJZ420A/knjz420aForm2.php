<?php

require_once('for_php7.php');

class knjz420aForm2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //警告メッセージを表示しない場合
        if (isset($model->company_cd) && !isset($model->warning) && $model->cmd != 'chenge_cd') {
            $Row = knjz420aQuery::getRow($model->company_cd);
        } else {
            $Row =& $model->field;
        }
        //職種取得
        $db = Query::dbCheckOut();
        $result = $db->query(knjz420aQuery::getCompanycd());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_companycd[]= array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
        }
        $result->free();

        //募集対象
        $opt_target[] = array("label" => "","value" => "0");
        $result = $db->query(knjz420aQuery::getTarget());
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_target[]= array('label' => $row2["LABEL"],
                                 'value' => $row2["VALUE"]);
        }

        $result->free();

        //会社コード
        $objForm->ae(array("type"        => "text",
                            "name"        => "COMPANY_CD",
                            "size"        => 8,
                            "maxlength"   => 8,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["COMPANY_CD"]));

        $arg["data"]["COMPANY_CD"] = $objForm->ge("COMPANY_CD");

        //会社名
        $objForm->ae(array("type"        => "text",
                            "name"        => "COMPANY_NAME",
                            "size"        => 80,
                            "maxlength"   => 120,
                            "extrahtml"   => "",
                            "value"       => $Row["COMPANY_NAME"] ));

        $arg["data"]["COMPANY_NAME"] = $objForm->ge("COMPANY_NAME");

        //就業場所
        $objForm->ae(array("type"        => "text",
                            "name"        => "SHUSHOKU_ADDR",
                            "size"        => 80,
                            "maxlength"   => 120,
                            "extrahtml"   => "",
                            "value"       => $Row["SHUSHOKU_ADDR"] ));

        $arg["data"]["SHUSHOKU_ADDR"] = $objForm->ge("SHUSHOKU_ADDR");

        //資本金
        $objForm->ae(array("type"        => "text",
                            "name"        => "SHIHONKIN",
                            "size"        => 17,
                            "maxlength"   => 17,
                            "extrahtml"   => "",
                            "value"       => $Row["SHIHONKIN"] ));

        $arg["data"]["SHIHONKIN"] = $objForm->ge("SHIHONKIN");

        //全体人数
        $objForm->ae(array("type"        => "text",
                            "name"        => "SONINZU",
                            "size"        => 8,
                            "maxlength"   => 8,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["SONINZU"] ));

        $arg["data"]["SONINZU"] = $objForm->ge("SONINZU");

        //事務所人数
        $objForm->ae(array("type"        => "text",
                            "name"        => "TONINZU",
                            "size"        => 8,
                            "maxlength"   => 8,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["TONINZU"] ));

        $arg["data"]["TONINZU"] = $objForm->ge("TONINZU");

        //産業大コード
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        $value_flg = false;
        $query = knjz420aQuery::getIndustryLcd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["INDUSTRY_LCD"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $Row["INDUSTRY_LCD"] = ($Row["INDUSTRY_LCD"] && $value_flg) ? $Row["INDUSTRY_LCD"] : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('chenge_cd')\"";
        $arg["data"]["INDUSTRY_LCD"] = knjCreateCombo($objForm, "INDUSTRY_LCD", $Row["INDUSTRY_LCD"], $opt, $extra, 1);

        //産業中コード
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        $value_flg = false;
        $query = knjz420aQuery::getIndustryMcd($Row["INDUSTRY_LCD"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["INDUSTRY_MCD"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $Row["INDUSTRY_MCD"] = ($Row["INDUSTRY_MCD"] && $value_flg) ? $Row["INDUSTRY_MCD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["INDUSTRY_MCD"] = knjCreateCombo($objForm, "INDUSTRY_MCD", $Row["INDUSTRY_MCD"], $opt, $extra, 1);

        //会社職種コンボボックス
        $objForm->ae(array("type"        => "select",
                            "name"        => "COMPANY_SORT",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $Row["COMPANY_SORT"],
                            "options"     => isset($opt_companycd)?$opt_companycd:array() ));

        $arg["data"]["COMPANY_SORT"] = $objForm->ge("COMPANY_SORT");

        //募集対象コンボボックス
        $objForm->ae(array("type"        => "select",
                            "name"        => "TARGET_SEX",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $Row["TARGET_SEX"],
                            "options"     => isset($opt_target)?$opt_target:array() ));

        $arg["data"]["TARGET_SEX"] = $objForm->ge("TARGET_SEX");

        //郵便番号
        $arg["data"]["ZIPCD"] = View::popUpZipCode($objForm, "ZIPCD", $Row["ZIPCD"], "ADDR1");

        //住所１
        $objForm->ae(array("type"        => "text",
                            "name"        => "ADDR1",
                            "size"        => 60,
                            "maxlength"   => 90,
                            "extrahtml"   => "",
                            "value"       => $Row["ADDR1"] ));

        $arg["data"]["ADDR1"] = $objForm->ge("ADDR1");

        //住所２
        $objForm->ae(array("type"        => "text",
                            "name"        => "ADDR2",
                            "size"        => 60,
                            "maxlength"   => 90,
                            "extrahtml"   => "",
                            "value"       => $Row["ADDR2"] ));

        $arg["data"]["ADDR2"] = $objForm->ge("ADDR2");

        //電話番号
        $objForm->ae(array("type"        => "text",
                            "name"        => "TELNO",
                            "size"        => 16,
                            "maxlength"   => 16,
                            "extrahtml"   => "",
                            "value"       => $Row["TELNO"] ));

        $arg["data"]["TELNO"] = $objForm->ge("TELNO");

        //備考
        $objForm->ae(array("type"        => "text",
                            "name"        => "REMARK",
                            "size"        => 80,
                            "maxlength"   => 120,
                            "extrahtml"   => "",
                            "value"       => $Row["REMARK"] ));

        $arg["data"]["REMARK"] = $objForm->ge("REMARK");

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model);

        //追加ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追 加",
                            "extrahtml" => "onclick=\"return btn_submit('add');\"" ));

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_update",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return btn_submit('update');\"" ));

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => "onclick=\"return btn_submit('delete');\"" ));

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('reset');\"" ));

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ));

        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae(array("type"    => "hidden",
                            "name"    => "cmd"
                            ));

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz420aindex.php", "", "edit");
        $arg["finish"]  = $objForm->get_finish();
        Query::dbCheckIn($db);
        if (VARS::get("cmd") != "edit" && $model->cmd != 'chenge_cd') {
            $arg["reload"]  = "parent.left_frame.location.href='knjz420aindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz420aForm2.html", $arg);
    }
}

//CSV作成
function makeCsv(&$objForm, &$arg, $model)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header  = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
