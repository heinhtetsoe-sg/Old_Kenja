<?php

require_once('for_php7.php');

class knja233aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja233aForm1", "POST", "knja233aindex.php", "", "knja233aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->year;

        /**********/
        /* コンボ */
        /**********/
        //学期コンボボックスを作成する
        $query = knja233aQuery::getSemester($model);
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
        $query = knja233aQuery::getClassMst($model);
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], "onChange=\"return btn_submit('knja233a');\"", 1);

        //テスト種別コンボ
        $query = knja233aQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], "onChange=\"return btn_submit('knja233a');\"", 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /********************/
        /* テキストボックス */
        /********************/
        //出力件数テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $model->field["KENSUU"] = $model->field["KENSUU"] ? $model->field["KENSUU"] : 1;
        $arg["data"]["KENSUU"] = knjCreateTextBox($objForm, $model->field["KENSUU"], "KENSUU", 3, 2, $extra);

        /**********/
        /* ラジオ */
        /**********/
        //
        if ($model->schoolName != 'MUSASHI') {
            //帳票種別　1:講座名簿、2:教務手帳
            $opt = array(1, 2);
            $def = ($model->schoolName === 'tokiwa') ? "2" : "1";
            $model->field["PRINT_DIV"] = ($model->field["PRINT_DIV"] == "") ? $def : $model->field["PRINT_DIV"];
            $extra = array("onclick=\"hurigana();\" id=\"PRINT_DIV1\"", "onclick=\"hurigana();\" id=\"PRINT_DIV2\"");
            $radioArray = knjCreateRadio($objForm, "PRINT_DIV", $model->field["PRINT_DIV"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
            //講座名簿出力パターン
            if ($model->Properties["useFormNameA233A"] == 'KNJA233A_7') {
                //青山学院用　1:Ａ、2:Ｂ(年組を年と組に分ける)、3:C
                $opt = array(1, 2, 3);
                $model->field["PATTERN"] = ($model->field["PATTERN"] == "") ? "1" : $model->field["PATTERN"];
                $off = 'onClick="sizeOff();"';
                $on = 'onClick="sizeOn();"';
                $extra = array("id=\"PATTERN1\"".$on, "id=\"PATTERN2\"".$on, "id=\"PATTERN3\"".$off);
                $radioArray = knjCreateRadio($objForm, "PATTERN", $model->field["PATTERN"], $extra, $opt, get_count($opt));
                foreach ($radioArray as $key => $val) {
                    $arg["data"][$key] = $val;
                }
            } elseif ($model->Properties["useFormNameA233A"] == 'KNJA233A_10') {
                //関西学院用　1:Ａ、2:Ｂ(年組を年と組に分ける)、3:Ｃ1 4:Ｃ2
                $opt = array(1, 2, 3, 4);
                if ($model->field["PATTERN"] == "") {
                    $model->field["PATTERN"] = "3";
                    $disabled = " disabled ";
                } else {
                    $model->field["PATTERN"] = $model->field["PATTERN"];
                    $disabled = "";
                }
                $off = 'onClick="sizeOff();"';
                $on = 'onClick="sizeOn();"';
                $extra = array("id=\"PATTERN1\"".$on, "id=\"PATTERN2\"".$on, "id=\"PATTERN3\"".$off, "id=\"PATTERN4\"".$off);
                $radioArray = knjCreateRadio($objForm, "PATTERN", $model->field["PATTERN"], $extra, $opt, get_count($opt));
                foreach ($radioArray as $key => $val) {
                    $arg["data"][$key] = $val;
                }
            } else {
                //講座名簿出力パターン　1:Ａ、2:Ｂ(年組を年と組に分ける)
                $opt = array(1, 2);
                $model->field["PATTERN"] = ($model->field["PATTERN"] == "") ? "1" : $model->field["PATTERN"];
                $extra = array("id=\"PATTERN1\"", "id=\"PATTERN2\"");
                $radioArray = knjCreateRadio($objForm, "PATTERN", $model->field["PATTERN"], $extra, $opt, get_count($opt));
                foreach ($radioArray as $key => $val) {
                    $arg["data"][$key] = $val;
                }
            }
            //出力順序指定
            $opt = array(1, 2, 3);
            $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
            $click = ' onClick="hurigana();"';
            $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click.$disabled);
            
            $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }

            $opt = array();
            $opt[0] = 1;
            $opt[1] = 2;
            for ($i = 1; $i <= 2; $i++) {
                $label = $i == 1 ? " id='OUTPUT4A'" : " id='OUTPUT4B'";
                $extra = "";
                $objForm->ae(array("type"       => "radio",
                                    "name"       => "OUTPUT4AB",
                                    "value"      => isset($model->field["OUTPUT4AB"]) ? $model->field["OUTPUT4AB"] : 1,
                                    "extrahtml"  => $extra.$label,
                                    "multiple"   => $opt));
                $arg['data'][$i == 1 ? "OUTPUT4A" : "OUTPUT4B"] = $objForm->ge("OUTPUT4AB", $i);
            }
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
            $objForm->ae(array("type"       => "radio",
                                "name"       => "HURIGANA_OUTPUT",
                                "value"      => isset($model->field["HURIGANA_OUTPUT"]) ? $model->field["HURIGANA_OUTPUT"] : 1,
                                "extrahtml"  => $extra.$label,
                                "multiple"   => $opt));

            $arg['data']["HURIGANA_OUTPUT".$i] = $objForm->ge("HURIGANA_OUTPUT", $i);
        }

        //縦サイズ
        $opt = array();
        $opt[] = array("label" => "4ミリ", "value" => "4");
        $opt[] = array("label" => "5ミリ", "value" => "5");
        $default = $db->getRow(knja233aQuery::getDefaultSize(), DB_FETCHMODE_ASSOC);
        $value = ($default["HEIGHT"]) ? $default["HEIGHT"] : $opt[0]["value"];
        $arg['data']["HEIGHT"] = knjCreateCombo($objForm, "HEIGHT", $value, $opt, "", 1);

        //縦サイズ
        $opt = array();
        for ($i = 23; $i <= 33; $i++) {
            $opt[] = array("label" => $i."ミリ", "value" => $i);
        }
        $value = ($default["WIDTH"]) ? $default["WIDTH"] : $opt[0]["value"];
        $arg['data']["WIDTH"] = knjCreateCombo($objForm, "WIDTH", $value, $opt, "", 1);

        //学籍番号を表記するcheckbox
        $extra  = $model->field["PRINT_SCHREGNO"] ? " checked " : "";
        $extra .= "id=\"PRINT_SCHREGNO\"";
        $arg["data"]["PRINT_SCHREGNO"] = knjCreateCheckBox($objForm, "PRINT_SCHREGNO", "1", $extra);

        //カレンダーコントロール
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : $model->control["学籍処理日"];
        $arg["el"]["DATE"] = View::popUpCalendar2($objForm, "DATE", $value, "reload=true", " btn_submit('knja233a')", "");

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
            $model->schoolCd = $db->getOne(knja233aQuery::getSchoolCd());
            $extra = "onclick=\"return newwin2('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            $btnName = "エクセル出力";
        } else {
            $extra = "onclick=\"return csv_submit('csv');\"";
        }
        //セキュリティーチェック
        $securityCnt = $db->getOne(knja233aQuery::getSecurityHigh());
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $btnName, $extra);
        }

        /**********/
        /* hidden */
        /**********/
        if (strpos($model->Properties["useFormNameA233A"], "KNJA233A_6") !== false) {
            $arg["is_not_musashi"] = 1;
        } elseif ($model->schoolName == 'MUSASHI') {
            $arg["is_musashi"] = 1;
            knjCreateHidden($objForm, "OUTPUT", 'musashi');
        } else {
            $arg["is_not_musashi"] = 1;
        }
        if (strpos($model->Properties["useFormNameA233A"], "KNJA233A_6") !== false) {
            knjCreateHidden($objForm, "PRINT_DIV", '1');
            knjCreateHidden($objForm, "PATTERN", '1');
            knjCreateHidden($objForm, "OUTPUT", '1');
        } elseif ($model->schoolName == 'hirokoudai' && $model->isTuusin == "1") {
            $arg["is_hirokou_tuusin"] = 1;
            knjCreateHidden($objForm, "PRINT_DIV", '1');
            knjCreateHidden($objForm, "PATTERN", '2');
            knjCreateHidden($objForm, "OUTPUT", '3');
            knjCreateHidden($objForm, "HEIGHT", '4');
            knjCreateHidden($objForm, "WIDTH", '26');
            knjCreateHidden($objForm, "HIROKOUDAI_TUUSIN", "1");
        } elseif ($model->schoolName == 'tosajoshi') {
            knjCreateHidden($objForm, "PRINT_DIV", '1');
            knjCreateHidden($objForm, "PATTERN", '2');
            knjCreateHidden($objForm, "OUTPUT", '3');
            knjCreateHidden($objForm, "HEIGHT", '4');
            knjCreateHidden($objForm, "WIDTH", '26');
            knjCreateHidden($objForm, "TOSAJOSHI", "1");
        } elseif ($model->schoolName == 'osakatoin') {
            knjCreateHidden($objForm, "PRINT_DIV", '1');
            knjCreateHidden($objForm, "PATTERN", '2');
            knjCreateHidden($objForm, "OUTPUT", '3');
            knjCreateHidden($objForm, "HEIGHT", '4');
            knjCreateHidden($objForm, "WIDTH", '26');
            knjCreateHidden($objForm, "OSAKATOIN", "1");
        } else {
            $arg["is_not_hirokou_tuusin"] = 1;
        }
        if ($model->Properties["useFormNameA233A"] == 'KNJA233A_7') {
            $arg["is_aoyama_gakuin"] = 1;
        }
        if ($model->Properties["useFormNameA233A"] == 'KNJA233A_10') {
            $arg["is_kansei_gakuin"] = 1;
        }
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
        knjCreateHidden($objForm, "useFormNameA233A", $model->Properties["useFormNameA233A"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja233aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
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
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $opt = array();
    $result = $db->query(knja233aQuery::getChairDat($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $chargediv_mark = ($row["CHARGEDIV"] == '1') ? '＊' : '　';
        $start = str_replace("-", "/", $row["APPDATE"]);
        $end   = str_replace("-", "/", $row["APPENDDATE"]);

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
