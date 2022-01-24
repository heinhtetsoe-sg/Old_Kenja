<?php

require_once('for_php7.php');


class knje131tForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje131tForm1", "POST", "knje131tindex.php", "", "knje131tForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => CTRL_YEAR ) );

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => CTRL_SEMESTER ) );

        //学年リストボックスを作成する
        $query = knje131tQuery::getSelectGrade($model);
        $extra = "onchange=\"return btn_submit('knje131t')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //クラス一覧リスト作成する
        $query = knje131tQuery::getAuth($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"height:180px; width:200px\" ondblclick=\"move1('left')\"",
                            "size"       => "10",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"height:180px; width:200px\" ondblclick=\"move1('right')\"",
                            "size"       => "10",
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

        //学校校種を取得
        $getSchoolKind = $db->getOne(knje131tQuery::getSelectGrade($model, "SCHOOL_KIND"));
        knjCreateHidden($objForm, "GET_SCHOOL_KIND", $getSchoolKind);

        //教科一覧リスト作成する
        $query = knje131tQuery::getSelectClassMst($model, $getSchoolKind);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row2[] = array('label' => $row["VALUE"] . "　" . $row["CLASSNAME"],
                            'value' => $row["VALUE"]);
        }
        $result->free();


        //対象ラジオボタン 1:学年評価 2:評定
        $model->field["OUT_DIV"] = $model->field["OUT_DIV"] ? $model->field["OUT_DIV"] : '1';
        $opt_outdiv = array(1, 2);
        $extra = array("id=\"OUT_DIV1\"", "id=\"OUT_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "OUT_DIV", $model->field["OUT_DIV"], $extra, $opt_outdiv, get_count($opt_outdiv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //評定読替するかしないかのフラグ 1:表示 1以外:非表示
        if ($model->Properties["hyoteiYomikae"] == '1') {
            $arg["HYOTEI_YOMIKAE_FLG"] = '1'; //null以外なら何でもいい
        } else {
            unset($arg["HYOTEI_YOMIKAE_FLG"]);
        }

        //評定読替チェックボックス
        if ($model->Properties["useProvFlg"] == "1") {
            $arg["data"]["KARI_MOJI"] = "仮";
        }
        $extra  = ($model->field["HYOTEI_YOMIKAE"] == "1") ? "checked" : "";
        $extra .= " id=\"HYOTEI_YOMIKAE\"";
        $arg["data"]["HYOTEI_YOMIKAE"] = knjCreateCheckBox($objForm, "HYOTEI_YOMIKAE", "1", $extra, "");


        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASSCD_NAME",
                            "extrahtml"  => "multiple style=\"width:200px\" ondblclick=\"move2('left')\"",
                            "size"       => "10",
                            "options"    => isset($row2)?$row2:array()));

        $arg["data"]["CLASSCD_NAME"] = $objForm->ge("CLASSCD_NAME");

        //出力対象教科リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASSCD_SELECTED",
                            "extrahtml"  => "multiple style=\"width:200px\" ondblclick=\"move2('right')\"",
                            "size"       => "10",
                            "options"    => array()));

        $arg["data"]["CLASSCD_SELECTED"] = $objForm->ge("CLASSCD_SELECTED");

        $model->isKumamoto = "";
        $query = knje131tQuery::getNameMstZ010();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($row['NAME1'] == 'kumamoto') {
            $model->isKumamoto = "1";
        }
        //熊本対応
        if ($model->isKumamoto == "1") {
            $arg["KUMAMOTO_FLG"] = '1';
            $opt = array(1, 2, 3, 4, 5);
            for ($i=0; $i < get_count($opt); $i++) { 
                $optName = "CSV_OPT".$opt[$i];
                $extra  = ($model->field[$optName] == "1") ? "checked" : "";
                $extra .= " id=\"{$optName}\"";
                $arg["data"][$optName] = knjCreateCheckBox($objForm, $optName, "1", $extra, "");
            }
            knjCreateHidden($objForm, "CSV_OPT_CNT", get_count($opt));
        }

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn2_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves2('right');\"" ) );

        $arg["button"]["btn2_rights"] = $objForm->ge("btn2_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn2_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves2('left');\"" ) );

        $arg["button"]["btn2_lefts"] = $objForm->ge("btn2_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn2_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move2('right');\"" ) );

        $arg["button"]["btn2_right1"] = $objForm->ge("btn2_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn2_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move2('left');\"" ) );

        $arg["button"]["btn2_left1"] = $objForm->ge("btn2_left1");


        //印刷ボタンを作成する
        $objForm->ae( array("type"           => "button",
                            "name"        => "btn_csv",
                            "value"       => "ＣＳＶ出力",
                            "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

        $arg["button"]["btn_csv"] = $objForm->ge("btn_csv");

        //終了ボタンを作成する
        $objForm->ae( array("type"           => "button",
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
                            "value"     => "KNJE131T"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );  

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata2") );  

        $schooldiv = $db->getOne(knje131tQuery::getSchoolDiv($model, $getSchoolKind));
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHOOLDIV",
                            "value"     => $schooldiv
                            ) );

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje131tForm1.html", $arg); 
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $gradeH3 = "";
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($row["SCHOOL_KIND"] == "H" && (int)$row["GRADE_CD"] == 3) $gradeH3 = $row["VALUE"];
    }
    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "GRADE" && strlen($gradeH3)) ? $gradeH3 : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
