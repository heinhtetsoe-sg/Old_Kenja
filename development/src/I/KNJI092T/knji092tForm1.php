<?php

require_once('for_php7.php');


class knji092tForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knji092tForm1", "POST", "knji092tindex.php", "", "knji092tForm1");

        //卒業年度
        $db = Query::dbCheckOut();
        $opt_year = array();
        $query = knji092tQuery::selectYear($model);
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
                            "extrahtml"  => "onChange=\"return btn_submit('knji092t');\"",
                            "value"      => $model->field["YEAR"],
                            "options"    => $opt_year));

        $arg["data"]["YEAR"] = $objForm->ge("YEAR");

        //校種コンボ
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query = knji092tQuery::getA023($model);
            $extra = "onchange=\"return btn_submit('knji092t');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1);
        }

        //卒業見込み出力チェックボックス
        $extra  = ($model->field["MIKOMI"] == "on") ? "checked" : "";
        $extra .= " id=\"MIKOMI\"";
        $arg["data"]["MIKOMI"] = knjCreateCheckBox($objForm, "MIKOMI", "on", $extra, "");

        //クラス、学科
        $opt = array(1, 2); //1:クラス 2:学科
        $model->field["CLASS_MAJOR"] = ($model->field["CLASS_MAJOR"] == "") ? "1" : $model->field["CLASS_MAJOR"];
        $extra = array("id=\"CLASS_MAJOR1\" onClick=\"return btn_submit('knji092t');\"", "id=\"CLASS_MAJOR2\" onClick=\"return btn_submit('knji092t');\"");
        $radioArray = knjCreateRadio($objForm, "CLASS_MAJOR", $model->field["CLASS_MAJOR"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "H")
            || $model->field["SCHOOL_KIND"] != "H") {
            $arg["DIS_MAJOR"] = "";
        } else {
            $arg["DIS_MAJOR"] = "1";
        }

        //学科表示順
        $opt = array(1, 2); //1:五十音順 2:クラス五十音順
        $model->field["MAJOR_ORDER"] = ($model->field["MAJOR_ORDER"] == "") ? "1" : $model->field["MAJOR_ORDER"];
        $extra = array("id=\"MAJOR_ORDER1\" onClick=\"kubun();\"", "id=\"MAJOR_ORDER2\" onClick=\"kubun();\"");
        $radioArray = knjCreateRadio($objForm, "MAJOR_ORDER", $model->field["MAJOR_ORDER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学期コード・学年数上限
        $query = knji092tQuery::selectGradeSemesterDiv($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
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

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"      => $model->field["GAKKI"]
                            ) );

        //クラス一覧リスト作成する

        //中高一貫
        $opt_Grade = $db->getOne(knji092tQuery::getNameMst()) == "1" ? "1" : "";

        $query = knji092tQuery::getAuth($model, $opt_Grade);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:180px\" width=\"180px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:180px\" width=\"180px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
                            "options"    => array()));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //縦書き、横書きラジオ
        $opt = array(1, 2);
        $model->field["WRITE_DIV"] = ($model->field["WRITE_DIV"] == "") ? "1" : $model->field["WRITE_DIV"];
        $extra = array("id=\"WRITE_DIV1\"", "id=\"WRITE_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "WRITE_DIV", $model->field["WRITE_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //表紙チェックボックス
        $extra  = ($model->field["OUTPUT1"] == "on") ? "checked" : "";
        $extra .= ($model->field["CLASS_MAJOR"] == "2") ? " disabled" : "";
        $extra .= " id=\"OUTPUT1\"";
        $arg["data"]["OUTPUT1"] = knjCreateCheckBox($objForm, "OUTPUT1", "on", $extra, "");

        //名簿チェックボックス
        $extra  = ($model->field["OUTPUT2"] == "on") ? "checked" : "";
        $extra .= ($model->cmd == "") ? " checked" : "";
        $extra .= " id=\"OUTPUT2\"";
        $arg["data"]["OUTPUT2"] = knjCreateCheckBox($objForm, "OUTPUT2", "on", $extra, "");

        if ($db->getOne(knji092tQuery::getCheckSchool()) == 0) {
            //性別チェックボックス
            $extra  = ($model->field["OUTPUT3"] == "on") ? "checked" : "";
            $extra .= " id=\"OUTPUT3\"";
            $arg["check"]["OUTPUT3"] = knjCreateCheckBox($objForm, "OUTPUT3", "on", $extra, "");
        }

        //入学年月日
        if ($model->field["ENT_DATE"] == "") $model->field["ENT_DATE"] = ((int)$model->field["YEAR"] - 2) . "/04/01";
        $arg["data"]["ENT_DATE"] = View::popUpCalendar($objForm, "ENT_DATE", str_replace("-","/",$model->field["ENT_DATE"]));

        //入学・卒業日付は年月で表示する
        $extra  = ($model->field["ENT_GRD_DATE_FORMAT"] == "on") ? "checked" : "";
        $extra .= " id=\"ENT_GRD_DATE_FORMAT\"";
        $arg["data"]["ENT_GRD_DATE_FORMAT"] = knjCreateCheckBox($objForm, "ENT_GRD_DATE_FORMAT", "on", $extra, "");

        //パターン
        $opt = array(1, 2); //1:Ａパターン 2:Ｂパターン
        $model->field["PATTERN"] = ($model->field["PATTERN"] == "") ? "1" : $model->field["PATTERN"];
        $extra = array("id=\"PATTERN1\"", "id=\"PATTERN2\"");
        $radioArray = knjCreateRadio($objForm, "PATTERN", $model->field["PATTERN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

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
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJI092T"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "useSchregRegdHdat",
                            "value"     => $model->useSchregRegdHdat
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "SCHOOLKIND", $model->field["SCHOOL_KIND"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knji092tForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
