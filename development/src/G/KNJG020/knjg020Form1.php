<?php

require_once('for_php7.php');


class knjg020Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjg020Form1", "POST", "knjg020index.php", "", "knjg020Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //出力対象ラジオボタン 1:卒業生 2:在校生 3:卒業生・在校生
        $opt1 = array(1, 2, 3);
        $model->field["RADIO"] = ($model->field["RADIO"] == "") ? "1" : $model->field["RADIO"];
        $extra = array("id=\"RADIO1\"", "id=\"RADIO2\"", "id=\"RADIO3\"");
        $radioArray = knjCreateRadio($objForm, "RADIO", $model->field["RADIO"], $extra, $opt1, get_count($opt1));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //証明書発行日付
        $model->field["DATE"] = ($model->field["DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);


        //出力順ラジオボタン 1:発行日順 2:発行番号順
        $opt2 = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt2, get_count($opt2));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //証明書種類コンボ作成
        $query  = knjg020Query::get_certif_kind($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "CERTIF_KIND", $model->field["CERTIF_KIND"], $extra, 1);

        if ($model->Properties["knjg020CertifDivNewPage"] != "1" && $model->Properties["knjg020CertifKindcdNewPage"] != "1") {
            $arg["usePage"] = "1";
            //印刷ページ番号条件ラジオボタン 1:連番出力 2:開始ページ指定
            $opt3 = array(1, 2);
            $model->field["OUTPUT2"] = ($model->field["OUTPUT2"] == "") ? "1" : $model->field["OUTPUT2"];
            $click = " onclick =\" disopt3(this.value);\"";
            $extra = array("id=\"OUTPUT21\"".$click, "id=\"OUTPUT22\"".$click);
            $radioArray = knjCreateRadio($objForm, "OUTPUT2", $model->field["OUTPUT2"], $extra, $opt3, get_count($opt3));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            //開始ページテキストボックス
            $extra  = ($model->field["OUTPUT2"] == 2) ? "" : "disabled";
            $extra .= " style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
            $value = isset($model->field["PAGE"]) ? $model->field["PAGE"] : 1;
            $arg["data"]["PAGE"] = knjCreateTextBox($objForm, $value, "PAGE", 5, 5, $extra);
        }

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJG020");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "certifNoSyudou", $model->Properties["certifNoSyudou"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "certif_no_8keta", $model->Properties["certif_no_8keta"]);
        knjCreateHidden($objForm, "knjg020CertifDivNewPage", $model->Properties["knjg020CertifDivNewPage"]);
        knjCreateHidden($objForm, "knjg020CertifKindcdNewPage", $model->Properties["knjg020CertifKindcdNewPage"]);
        

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg020Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    if ($name == "CERTIF_KIND")  $opt[] = array("label" => "000　全て", "value" => "000");
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
