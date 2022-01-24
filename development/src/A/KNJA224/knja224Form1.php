<?php

require_once('for_php7.php');

class knja224Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja224Form1", "POST", "knja224index.php", "", "knja224Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        if ($model->schoolName == 'MUSASHI') {
            $arg["is_musashi"] = 1;
            knjCreateHidden($objForm, "OUTPUT", 'musashi');
        } elseif ($model->schoolName == 'CHIBEN') {
            knjCreateHidden($objForm, "OUTPUT", 'chiben');
        } elseif (($model->schoolName == 'hirokoudai' && $model->isTuusin == "1") || $model->schoolName == 'tosajoshi' || $model->schoolName == 'osakatoin') {
            knjCreateHidden($objForm, "OUTPUT", '5');
        } else {
            $arg["is_not_musashi"] = 1;
        }

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //extra
        $extraCheck  = ($model->field["KARA"]) ? "checked" : "";
        $extraInt    = " onblur=\"this.value=toInteger(this.value)\";";
        $extraRight  = " STYLE=\"text-align: right\"";

        //空行チェックボックス
        $model->field["KARA"] = ($model->field["KARA"]) ? $model->field["KARA"] : "1";
        $arg["data"]["KARA"] = knjCreateCheckBox($objForm, "KARA", $model->field["KARA"], $extraCheck." id=\"KARA\"", "");

        if ($model->schoolName != 'MUSASHI' && $model->schoolName != 'CHIBEN' && ($model->schoolName != 'hirokoudai' || $model->isTuusin != "1")
                && $model->schoolName != 'tosajoshi' && $model->schoolName != 'osakatoin') {
            //名票ラジオボタン 1:漢字 2:漢字・かな 3:漢字・出身校 4:Ａ４ヨコ 5:漢字(サイズ指定) 7:漢字・自由
            $opt = array(1, 2, 3, 4, 5, 6 , 7, 8);
            if ($model->schoolName === 'tokiwa') {
                $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "6" : $model->field["OUTPUT"];
            } else {
                if ($model->Properties["useFormNameA224"]) {
                    $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "8" : $model->field["OUTPUT"];
                    $arg["data"]["OUTPUT8_LABEL"] = "　名票（氏名漢字・異動区分）";
                } else {
                    $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
                    $arg["data"]["OUTPUT8_LABEL"] = "";
                }
            }
            $extra = "";
            if ($model->Properties["useFormNameA224"]) {
                $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"", "id=\"OUTPUT5\"", "id=\"OUTPUT6\"", "id=\"OUTPUT7\"", "id=\"OUTPUT8\"");
            } else {
                $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"", "id=\"OUTPUT5\"", "id=\"OUTPUT6\"", "id=\"OUTPUT7\"", "id=\"OUTPUT8\" disabled style=\"visibility: hidden;\"");
            }
            $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //縦サイズ
        $opt = array();
        $opt[] = array("label" => "4ミリ", "value" => "4");
        $opt[] = array("label" => "5ミリ", "value" => "5");
        $default = $db->getOne(knja224Query::getDefaultSize("HEIGHT"));
        $value = ($default) ? $default : $opt[0]["value"];
        $arg["data"]["HEIGHT"] = knjCreateCombo($objForm, "HEIGHT", $value, $opt, "", 1);

        //横サイズ
        $opt = array();
        for ($i = 23; $i <= 33; $i++) {
            $opt[] = array("label" => $i."ミリ", "value" => $i);
        }
        $default = $db->getOne(knja224Query::getDefaultSize("WIDTH"));
        $value = ($default) ? $default : $opt[0]["value"];
        $arg["data"]["WIDTH"] = knjCreateCombo($objForm, "WIDTH", $value, $opt, "", 1);

        //学籍番号を表記するcheckbox
        $extra  = $model->field["PRINT_SCHREGNO"] ? " checked " : "";
        $extra .= "id=\"PRINT_SCHREGNO\"";
        $arg["data"]["PRINT_SCHREGNO"] = knjCreateCheckBox($objForm, "PRINT_SCHREGNO", "1", $extra);

        //radio
        $opt = array(1, 2);
        $model->field["KYOUMU"] = ($model->field["KYOUMU"] == "") ? "1" : $model->field["KYOUMU"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"KYOUMU{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "KYOUMU", $model->field["KYOUMU"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }


        //出力件数
        $model->field["KENSUU"] = ($model->field["KENSUU"]) ? $model->field["KENSUU"] : 1;
        $arg["data"]["KENSUU"] = knjCreateTextBox($objForm, $model->field["KENSUU"], "KENSUU", 3, 2, $extraInt.$extraRight);

        //枠無しcheckbox
        $extra  = $model->field["WAKU_NASI"] ? " checked " : "";
        $extra .= "id=\"WAKU_NASI\"";
        $arg["data"]["WAKU_NASI"] = knjCreateCheckBox($objForm, "WAKU_NASI", "1", $extra);

        //名前無しcheckbox
        if ($model->schoolName === 'tokiwa') {
            $extra  = ($model->field["NAME_NASI"]) ? " checked " : "";
        } else {
            $extra  = ($model->field["NAME_NASI"] || $model->cmd == "") ? " checked " : "";
        }
        $extra .= "id=\"NAME_NASI\"";
        $arg["data"]["NAME_NASI"] = knjCreateCheckBox($objForm, "NAME_NASI", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja224Form1.html", $arg);
    }
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knja224Query::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"";
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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //ＣＳＶ出力ボタン
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return btn_submit('csv');\"");
    //終了ボタン
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg)
{
    //hidden
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA224");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    if ($model->schoolName == 'hirokoudai' && $model->isTuusin == "1") {
        knjCreateHidden($objForm, "WIDTH", '26');
        knjCreateHidden($objForm, "HEIGHT", '4');
        knjCreateHidden($objForm, "PRINT_SCHREGNO", '1');
        knjCreateHidden($objForm, "HIROKOUDAI_TUUSIN", "1");
    }
    if ($model->schoolName == 'tosajoshi') {
        knjCreateHidden($objForm, "WIDTH", '26');
        knjCreateHidden($objForm, "HEIGHT", '4');
        knjCreateHidden($objForm, "PRINT_SCHREGNO", '1');
        knjCreateHidden($objForm, "TOSAJOSHI", "1");
    }
    if ($model->schoolName == 'osakatoin') {
        knjCreateHidden($objForm, "WIDTH", '26');
        knjCreateHidden($objForm, "HEIGHT", '4');
        knjCreateHidden($objForm, "PRINT_SCHREGNO", '1');
        knjCreateHidden($objForm, "OSAKATOIN", "1");
    }
    if ($model->Properties["useFormNameA224"]) {
        knjCreateHidden($objForm, "useFormNameA224", $model->Properties["useFormNameA224"]);
    }
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
}
