<?php

require_once('for_php7.php');


class knjg021Form1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjg021Form1", "POST", "knjg021index.php", "", "knjg021Form1");

        $db = Query::dbCheckOut();

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
        }
        $query = knjg021Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('knjg021');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);

        //卒業年度
        $opt_year = array();
        $query = knjg021Query::selectYear($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_year[]= array('label' => $row["YEAR"]."年度卒",
                               'value' => $row["YEAR"]);
        }
        $result->free();

        if ($model->field["YEAR"] == "") $model->field["YEAR"] = CTRL_YEAR;     //初期値：現在年度をセット。

        $objForm->ae( array("type"       => "select",
                            "name"       => "YEAR",
                            "size"       => "1",
                            "extrahtml"  => "onChange=\"return btn_submit('knjg021');\"",
                            "value"      => $model->field["YEAR"],
                            "options"    => $opt_year));

        $arg["data"]["YEAR"] = $objForm->ge("YEAR");

        //学期コード・学年数上限
        $query = knjg021Query::selectGradeSemesterDiv($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opt_Grade    = isset($row["GRADE_HVAL"])?$row["GRADE_HVAL"]:"03";//データ無しはデフォルトで３学年を設定
        $opt_Semester = isset($row["SEMESTERDIV"])?$row["SEMESTERDIV"]:"3";//データ無しはデフォルトで３学期を設定
        /* 学期コードをhiddenで送る。
         * 卒業年度が現在年度の場合：現在学期をセット。
         * 卒業年度が現在年度未満の場合：３学期をセット。
         */
        if ($model->field["YEAR"] == CTRL_YEAR) {
            $model->field["GAKKI"] = CTRL_SEMESTER;
        } else {
            $model->field["GAKKI"] = $opt_Semester;
        }

        //発行番号を出力する
        if ($model->field["OUTPUT_CERTIF_NO"] == "1" || $cmd == "") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id=\"OUTPUT_CERTIF_NO\"";
        $arg["data"]["OUTPUT_CERTIF_NO"] = knjCreateCheckBox($objForm, "OUTPUT_CERTIF_NO", "1", $extra);

        knjCreateHidden($objForm, "GAKKI", $model->field["GAKKI"]);

        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJG021");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "hasCheckOutputCertifNo", "1");
        knjCreateHidden($objForm, "certifNoSyudou", $model->Properties["certifNoSyudou"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "certif_no_8keta", $model->Properties["certif_no_8keta"]);

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg021Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
