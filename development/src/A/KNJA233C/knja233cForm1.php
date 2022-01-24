<?php

require_once('for_php7.php');

class knja233cForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja233cForm1", "POST", "knja233cindex.php", "", "knja233cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->year;

        /**********/
        /* コンボ */
        /**********/
        //学期コンボボックスを作成する
        $query = knja233cQuery::getSemester($model);
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onchange=\"return btn_submit('gakki');\"", 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }

        //学年コンボ
        $query = knja233cQuery::getClassMst($model);
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], "onChange=\"return btn_submit('knja233c');\"", 1);

        //テスト種別コンボ
        $query = knja233cQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], "onChange=\"return btn_submit('knja233c');\"", 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /********************/
        /* テキストボックス */
        /********************/
        //出力件数テキストボックス
        $extra = "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $model->field["KENSUU"] = $model->field["KENSUU"] ? $model->field["KENSUU"] : 1;
        $arg["data"]["KENSUU"] = knjCreateTextBox($objForm, $model->field["KENSUU"], "KENSUU", 3, 2, $extra);

        /**********/
        /* ラジオ */
        /**********/
        //
        if ($model->schoolName != 'MUSASHI') {
//            //帳票種別　1:講座名簿、2:教務手帳
//            $opt = array(1, 2);
//            $def = ($model->schoolName === 'tokiwa') ? "2" : "1";
//            $model->field["PRINT_DIV"] = ($model->field["PRINT_DIV"] == "") ? $def : $model->field["PRINT_DIV"];
//            $extra = array("onclick=\"hurigana();\" id=\"PRINT_DIV1\"", "onclick=\"hurigana();\" id=\"PRINT_DIV2\"");
//            $radioArray = knjCreateRadio($objForm, "PRINT_DIV", $model->field["PRINT_DIV"], $extra, $opt, get_count($opt));
//            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
            //講座名簿出力パターン　1:Ａ、2:Ｂ(年組を年と組に分ける)
            $opt = array(1, 2);
            $model->field["PATTERN"] = ($model->field["PATTERN"] == "") ? "1" : $model->field["PATTERN"];
            $extra = array("id=\"PATTERN1\"", "id=\"PATTERN2\"");
            $radioArray = knjCreateRadio($objForm, "PATTERN", $model->field["PATTERN"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
            //出力順序指定
            $opt[0]=1;
            $opt[1]=2;
            $opt[2]=3;
            $extra = 'onClick="hurigana();"';
            for ($i = 1; $i <= 3; $i++) {
                $label = " id=OUTPUT".$i;
                $objForm->ae( array("type"       => "radio",
                                    "name"       => "OUTPUT",
                                    "value"      => isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : 1,
                                    "extrahtml"  => $extra.$label,
                                    "multiple"   => $opt));
                $arg['data']["OUTPUT".$i] = $objForm->ge("OUTPUT", $i);
            }
//            $opt = array();
//            $opt[0] = 1;
//            $opt[1] = 2;
//            for ($i = 1; $i <= 2; $i++) {
//                $label = $i == 1 ? " id='OUTPUT4A'" : " id='OUTPUT4B'";
//                $extra = "";
//                $objForm->ae( array("type"       => "radio",
//                                    "name"       => "OUTPUT4AB",
//                                    "value"      => isset($model->field["OUTPUT4AB"]) ? $model->field["OUTPUT4AB"] : 1,
//                                    "extrahtml"  => $extra.$label,
//                                    "multiple"   => $opt));
//                $arg['data'][$i == 1 ? "OUTPUT4A" : "OUTPUT4B"] = $objForm->ge("OUTPUT4AB", $i);
//            }
        }

        //ふりがな出力
        $opt[0]=1;
        $opt[1]=2;
        if ($model->field["OUTPUT"] == '3') {
            $extra = 'disabled=true';
        } else {
            $extra = '';
        }

        for ($i = 1; $i <= 2; $i++) {
            $label = " id=HURIGANA_OUTPUT".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "HURIGANA_OUTPUT",
                                "value"      => isset($model->field["HURIGANA_OUTPUT"]) ? $model->field["HURIGANA_OUTPUT"] : 1,
                                "extrahtml"  => $extra.$label,
                                "multiple"   => $opt));

            $arg['data']["HURIGANA_OUTPUT".$i] = $objForm->ge("HURIGANA_OUTPUT", $i);
        }

//        //縦サイズ
//        $opt = array();
//        $opt[] = array("label" => "4ミリ", "value" => "4");
//        $opt[] = array("label" => "5ミリ", "value" => "5");
//        $default = $db->getRow(knja233cQuery::getDefaultSize(), DB_FETCHMODE_ASSOC);
//        $value = ($default["HEIGHT"]) ? $default["HEIGHT"] : $opt[0]["value"];
//        $arg['data']["HEIGHT"] = knjCreateCombo($objForm, "HEIGHT", $value, $opt, "", 1);

//        //縦サイズ
//        $opt = array();
//        for ($i = 23 ; $i <= 33 ; $i++) {
//            $opt[] = array("label" => $i."ミリ", "value" => $i);
//        }
//        $value = ($default["WIDTH"]) ? $default["WIDTH"] : $opt[0]["value"];
//        $arg['data']["WIDTH"] = knjCreateCombo($objForm, "WIDTH", $value, $opt, "", 1);

        //学籍番号を表記するcheckbox
        $extra  = $model->field["PRINT_SCHREGNO"] ? " checked " : "";
        $extra .= "id=\"PRINT_SCHREGNO\"";
        $arg["data"]["PRINT_SCHREGNO"] = knjCreateCheckBox($objForm, "PRINT_SCHREGNO", "1", $extra);

        //カレンダーコントロール
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : $model->control["学籍処理日"];
        $arg["el"]["DATE"] = View::popUpCalendar2($objForm, "DATE", $value, "reload=true", " btn_submit('knja233c')", "");

        /**********/
        /* ボタン */
        /**********/
        //プレビュー
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //ＣＳＶ出力
        $btnName = "ＣＳＶ出力";
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knja233cQuery::getSchoolCd());
            $extra = "onclick=\"return newwin2('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            $btnName = "エクセル出力";
        } else {
            $extra = "onclick=\"return csv_submit('csv');\"";
        }
        //セキュリティーチェック
        $securityCnt = $db->getOne(knja233cQuery::getSecurityHigh());
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $btnName, $extra);
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "ATTENDCLASSCD");
        knjCreateHidden($objForm, "GROUPCD");
        knjCreateHidden($objForm, "NAME_SHOW");
        knjCreateHidden($objForm, "CHARGEDIV");
        knjCreateHidden($objForm, "APPDATE");
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        
        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja233cForm1.html", $arg); 
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
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $opt = array();
    $result = $db->query(knja233cQuery::getChairDat($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $chargediv_mark = ($row["CHARGEDIV"] == '1') ? '＊' : '　';
        $start = str_replace("-","/",$row["APPDATE"]);
        $end   = str_replace("-","/",$row["APPENDDATE"]);

        $opt[]= array('label' => "{$row["ATTENDCLASSCD"]}:{$row["CLASSALIAS"]} {$start}～{$end} {$row["STAFFNAME_SHOW"]} {$chargediv_mark} {$row["GROUPCD"]}",
                      'value' => "{$row["SUBCLASSCD"]},{$row["GRADE_HR_CLASS"]},{$row["STAFFCD"]},{$row["APPDATE"]},{$row["ATTENDCLASSCD"]},{$row["GROUPCD"]},{$row["CHARGEDIV"]},{$row["APPDATE"]}"
                     );
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:400px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $opt, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:400px;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
?>
