<?php

require_once('for_php7.php');

class knjz250_2Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz250_2index.php", "", "edit");
        //警告メッセージを表示しない場合
        if (isset($model->certif_kindcd) && !isset($model->warning))
        {
            $Row = knjz250_2Query::getRow($model->certif_kindcd);
            $temp_cd = $Row["CERTIF_KINDCD"];
        }else{
            $Row =& $model->field;
        }
        //校務分掌部コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "CERTIF_KINDCD",
                            "size"        => 4,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["CERTIF_KINDCD"] ));

        $arg["data"]["CERTIF_KINDCD"] = $objForm->ge("CERTIF_KINDCD");

        //分掌部名
        $objForm->ae( array("type"        => "text",
                            "name"        => "KINDNAME",
                            "size"        => 16,
                            "maxlength"   => 24,
                            "value"       => $Row["KINDNAME"] ));

        $arg["data"]["KINDNAME"] = $objForm->ge("KINDNAME");

        $db = Query::dbCheckOut();

            $result = $db->query(knjz250_2Query::getIssue());
            $issue = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $issue[] = array("label" => $row["NAMECD2"]."  ".$row["NAME1"], "value" => $row["NAMECD2"]);
            }

            $result = $db->query(knjz250_2Query::getStudent());
            $student = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $student[] = array("label" => $row["NAMECD2"]."  ".$row["NAME1"], "value" => $row["NAMECD2"]);
            }

        $result->free();
        Query::dbCheckIn($db);

        //事務発行区分
        $objForm->ae( array("type"       => "select",
                            "name"       => "ISSUECD",
                            "size"       => "1",
                            "value"      => $Row["ISSUECD"],
        //                    "extrahtml"   => "onchange=\"return btn_ctrl('');\"",
                            "options"    => $issue));

        $arg["data"]["ISSUECD"] = $objForm->ge("ISSUECD");

        //在学生
        $objForm->ae( array("type"       => "select",
                            "name"       => "STUDENTCD",
                            "size"       => "1",
                            "value"      => $Row["STUDENTCD"],
        //                    "extrahtml"   => "onchange=\"return btn_ctrl('');\"",
                            "options"    => $student));

        $arg["data"]["STUDENTCD"] = $objForm->ge("STUDENTCD");

        //卒業生
        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADUATECD",
                            "size"       => "1",
                            "value"      => $Row["GRADUATECD"],
        //                    "extrahtml"   => "onchange=\"return btn_ctrl('');\"",
                            "options"    => $student));

        $arg["data"]["GRADUATECD"] = $objForm->ge("GRADUATECD");

        //転出退学者
        $objForm->ae( array("type"       => "select",
                            "name"       => "DROPOUTCD",
                            "size"       => "1",
                            "value"      => $Row["DROPOUTCD"],
        //                    "extrahtml"   => "onchange=\"return btn_ctrl('');\"",
                            "options"    => $student));

        $arg["data"]["DROPOUTCD"] = $objForm->ge("DROPOUTCD");

        //卒業後の経過年数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ELAPSED_YEARS"] = knjCreateTextBox($objForm, $Row["ELAPSED_YEARS"], "ELAPSED_YEARS", 2, 2, $extra);

        if ($model->Properties["certif_no_8keta"] == "1") {
            $arg["certif_no_8keta"] = 1;

            $db = Query::dbCheckOut();

            $result = $db->query(knjz250_2Query::getCertifDivCd());
            $cdiv = array(array("label" => "", "value" => NULL));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if ($row["CERTIF_DIV"] != "") {
                    $cdiv[] = array("label" => $row["CERTIF_DIV"].":".$row["CERTIF_DIV_NAME"], "value" => $row["CERTIF_DIV"]);
                }
            }

            $result->free();
            Query::dbCheckIn($db);

            //証明書区分
            $objForm->ae( array("type"       => "select",
                                "name"       => "CERTIF_DIV",
                                "size"       => "1",
                                "value"      => $Row["CERTIF_DIV"],
            //                  "extrahtml"   => "onchange=\"return btn_ctrl('');\"",
                                "options"    => $cdiv));

            $arg["data"]["CERTIF_DIV"] = $objForm->ge("CERTIF_DIV");

            //$extra = "onblur=\"this.value=toInteger(this.value)\"";
            //$arg["data"]["CERTIF_DIV"] = knjCreateTextBox($objForm, $Row["CERTIF_DIV"], "CERTIF_DIV", 1, 1, $extra);
        }

        //在学生発行手数料
        $extra = " style=\"text-align:right;\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CURRENT_PRICE"] = knjCreateTextBox($objForm, $Row["CURRENT_PRICE"], "CURRENT_PRICE", 4, 4, $extra);

        //卒業生発行手数料
        $extra = " style=\"text-align:right;\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["GRADUATED_PRICE"] = knjCreateTextBox($objForm, $Row["GRADUATED_PRICE"], "GRADUATED_PRICE", 4, 4, $extra);

        //発行番号採番チェックボックス
        $extra = "id=\"ISSUENO_AUTOFLG\"";
        if ($Row["ISSUENO_AUTOFLG"] == '1') {
        	$extra = "checked " . $extra;
        }
        $arg["data"]["ISSUENO_AUTOFLG"] = knjCreateCheckBox($objForm, "ISSUENO_AUTOFLG", "1", $extra, "");

        // 証明書校種
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $arg["use_certif_kind_mst_school_kind"] = "1";
            $db = Query::dbCheckOut();
            $extra = "";
            $query = knjz250_2Query::getA023();
            makeCmb($objForm, $arg, $db, $query, "CERTIF_SCHOOL_KIND", $Row["CERTIF_SCHOOL_KIND"], $extra, 1, "BLANK");
            Query::dbCheckIn($db);
        }
        
        //追加ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type" => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return Btn_reset('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //戻るボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ250/knjz250index.php?year_code=".$model->year_code;
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );
                    
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );
                   
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_code",
                            "value"     => $model->year_code
                            ) );

        $cd_change = false;
        if ($temp_cd==$Row["CERTIF_KINDCD"] ) $cd_change = true;

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz250_2index.php?cmd=list';";
        }

        $arg["TITLE"] = "マスタメンテナンスー証明書種類マスタ";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz250_2Form2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank) {
    $opt = array();
    if ($blank) {
        $opt[] = array('label' => '',
                       'value' => '');
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
