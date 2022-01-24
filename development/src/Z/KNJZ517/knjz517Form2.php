<?php

require_once('for_php7.php');

class knjz517Form2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz517index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->service_centercd && $model->service_centercd_edaban) {
            $query = knjz517Query::getWelfare_useServiceCenterMst($model->service_centercd, $model->service_centercd_edaban);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SERVICE_CENTERCD"] = knjCreateTextBox($objForm, $Row["SERVICE_CENTERCD"], "SERVICE_CENTERCD", 10, 10, $extra);
        //コードの枝番
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SERVICE_CENTERCD_EDABAN"] = knjCreateTextBox($objForm, $Row["SERVICE_CENTERCD_EDABAN"], "SERVICE_CENTERCD_EDABAN", 2, 2, $extra);

        //事業所名
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 100, 100, "");

        //略称名
        $arg["data"]["ABBV"] = knjCreateTextBox($objForm, $Row["ABBV"], "ABBV", 40, 40, "");

        //圏域
        $kyoto = $db->getOne(knjz517Query::getZ010());
        $arg["data"]["AREA_NAME"] = ($kyoto == 'kyoto') ? "圏域" : "地区";
        $opt = array();
        $value_flg = false;
        $opt[] = array();
        $query = knjz517Query::getNameMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["AREACD"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["AREACD"] = ($Row["AREACD"] && $value_flg) ? $Row["AREACD"] : ($model->areacd ? $model->areacd : $opt[0]["value"]);
        $extra = "";
        $arg["data"]["AREACD"] = knjCreateCombo($objForm, "AREACD", $Row["AREACD"], $opt, $extra, 1);

        //郵便番号
        $arg["data"]["ZIPCD"] = View::popUpZipCode($objForm, "ZIPCD", $Row["ZIPCD"], "ADDR1");

        //住所１
        $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $Row["ADDR1"], "ADDR1", 100, 100, "");

        //住所２
        $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $Row["ADDR2"], "ADDR2", 100, 100, "");

        //住所３
        $arg["data"]["ADDR3"] = knjCreateTextBox($objForm, $Row["ADDR3"], "ADDR3", 100, 100, "");

        //法人(設置名)名
        $arg["data"]["COMMISSION_NAME"] = knjCreateTextBox($objForm, $Row["COMMISSION_NAME"], "COMMISSION_NAME", 60, 60, "");

        //電話番号
        $extra = "onblur=\"this.value=toTelNo(this, '電話')\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 16, 14, $extra);

        //ＦＡＸ番号
        $extra = "onblur=\"this.value=toTelNo(this, 'ＦＡＸ')\"";
        $arg["data"]["FAXNO"] = knjCreateTextBox($objForm, $Row["FAXNO"], "FAXNO", 16, 14, $extra);

        //障害者支援施設
        if ($Row["CHALLENGED_SUPPORT_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["CHALLENGED_SUPPORT_FLG"] = knjCreateCheckBox($objForm, "CHALLENGED_SUPPORT_FLG", "1", $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjz517index.php?cmd=list&shori=update','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz517Form2.html", $arg);
    }
}

?>
