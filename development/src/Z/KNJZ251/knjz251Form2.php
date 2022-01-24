<?php

require_once('for_php7.php');
class knjz251Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz251index.php", "", "edit");
        //警告メッセージを表示しない場合
        if (isset($model->certif_kindcd) && !isset($model->warning))
        {
            $Row = knjz251Query::getDispData($model->certif_kindcd);
            $temp_cd = $Row["CERTIF_KINDCD"];
        }else{
            $Row =& $model->field;
        }
        $db = Query::dbCheckOut();

        // データチェック
        $count = knjz251Query::checkCertif($Row["CERTIF_KINDCD"], CTRL_YEAR, "on");
        $dataflg = 0 < $count;

        //校務分掌部コード
        $arg["data"]["CERTIF_KINDCD"] = $this->createText($objForm, "CERTIF_KINDCD", $Row["CERTIF_KINDCD"], "onblur=\"this.value=toInteger(this.value)\"", 4, 3);

        //分掌部名
        $arg["data"]["KINDNAME"] = $this->createText($objForm, "KINDNAME", $Row["KINDNAME"], "", 16, 24);

        //発行番号
        $certif = array();
        $certif[0] = array("label" => "0 印字あり",
                           "value" => "0");
        $certif[1] = array("label" => "1 印字なし",
                           "value" => "1");
        $arg["data"]["CERTIF_NO"] = $this->createCombo($objForm, "CERTIF_NO", $Row["CERTIF_NO"], $certif, "", 1);

        //証書名
        $arg["data"]["SYOSYO_NAME"] = $this->createText($objForm, "SYOSYO_NAME", $Row["SYOSYO_NAME"], "", 100, 150);

        //証書名2
        $arg["data"]["SYOSYO_NAME2"] = $this->createText($objForm, "SYOSYO_NAME2", $Row["SYOSYO_NAME2"], "", 20, 30);

        //学校名
        $arg["data"]["SCHOOL_NAME"] = $this->createText($objForm, "SCHOOL_NAME", $Row["SCHOOL_NAME"], "", 60, 90);

        //責任者職種名
        $arg["data"]["JOB_NAME"] = $this->createText($objForm, "JOB_NAME", $Row["JOB_NAME"], "", 90, 135);

        //記載責任者名
        $arg["data"]["PRINCIPAL_NAME"] = $this->createText($objForm, "PRINCIPAL_NAME", $Row["PRINCIPAL_NAME"], "", 60, 90);

        //備考
        for ($i = 1; $i <= 10; $i++) {
            $arg["data"]["REMARK".$i] = $this->createText($objForm, "REMARK".$i, $Row["REMARK".$i], "", 100, 150);
        }

        //追加ボタンを作成する
        if ($dataflg) {
            $ext  = "onclick=\"return noadd('(証明書種類コード)');\"";
            $ext .= " style=\"color: 808080;\" ";
        } else {
            $ext = "onclick=\"return btn_submit('add');\"";
        }
        $arg["button"]["btn_add"] = $this->createBtn($objForm, "btn_add", "追 加", $ext, "button");

        //修正ボタンを作成する
        if ($dataflg) {
            $ext  = "onclick=\"return btn_submit('update');\"";
        } else {
            $ext  = "onclick=\"return nodata('追加ボタンを押してください。');\"";
            $ext .= " style=\"color: 808080;\" ";
        }
        $arg["button"]["btn_update"] = $this->createBtn($objForm, "btn_update", "更 新", $ext, "button");

        //削除ボタンを作成する
        if ($dataflg) {
            $ext  = "onclick=\"return btn_submit('delete');\"";
        } else {
            $ext  = "onclick=\"return nodata('');\"";
            $ext .= " style=\"color: 808080;\" ";
        }
        $arg["button"]["btn_del"] = $this->createBtn($objForm, "btn_del", "削 除", $ext, "button");

        //クリアボタンを作成する
        $arg["button"]["btn_reset"] = $this->createBtn($objForm, "btn_reset", "取 消", "onclick=\"return Btn_reset('reset');\"", "reset");

        //終了ボタンを作成する
        $arg["button"]["btn_end"] = $this->createBtn($objForm, "btn_end", "終了", "onclick=\"closeWin();\"", "button");

        if (strlen($Row["CERTIF_KINDCD"]) && $Row["CERTIF_KINDCD"] < "100") {
            //印刷ボタンを作成する
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
            $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー", $extra);
        }

        makeHidden($objForm, $model, $Row);


        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz251index.php?cmd=list';";
        }

        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz251Form2.html", $arg);
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

    //ボタン作成
    function createBtn(&$objForm, $name, $value, $extra, $type)
    {
        $objForm->ae( array("type"        => $type,
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ) );
        return $objForm->ge($name);
    }
}

function makeHidden(&$objForm, $model, $Row) {
    knjCreateHidden($objForm, "cmd", "");
    if (strlen($Row["CERTIF_KINDCD"]) && $Row["CERTIF_KINDCD"] < "100") {
        knjCreateHidden($objForm, "PRGID", "KNJZ251");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CERTIF_KINDCD", $Row["CERTIF_KINDCD"]);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    }
}

?>
