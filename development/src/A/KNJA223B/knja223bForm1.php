<?php

require_once('for_php7.php');

class knja223bForm1 {
    function main(&$model){
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja223bForm1", "POST", "knja223bindex.php", "", "knja223bForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //駒澤大判定用
        $koma_flg = "0";
        $cnt = $db->getOne(knja223bQuery::checkKoma());
        if ($cnt != 0) {
            $koma_flg = "1";
        }
        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        if ($koma_flg == "0") {
            if ($model->Properties["useFi_Hrclass"] == "1") {
                $arg["useFi_HrclassSelect"] = "1";
                //クラス方式選択 (1:法定クラス 2:複式クラス)
                $opt = array(1, 2);
                $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
                $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"return btn_submit('knja223b');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"return btn_submit('knja223b');\"");
                $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
                foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
            }
        }

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //出力件数
        $model->field["KENSUU"] = ($model->field["KENSUU"]) ? $model->field["KENSUU"] : 1;
        $arg["data"]["KENSUU"] = createTextBox($objForm, $model->field["KENSUU"], "KENSUU", 3, 2, $extraInt.$extraRight);

        if ($koma_flg == "0" && strpos($model->Properties["useFormNameA223B"], "KNJA223B_6") === false) {
            //フォーム選択
            $arg["title_FORM_SELECT"] = "<b>フォーム選択</b><br>";

            $arg["label_FORM_DIV1"] = "<LABEL for=\"FORM_DIV1\">45行×15列</LABEL><br>";
            $arg["label_FORM_DIV2"] = "<LABEL for=\"FORM_DIV2\">40行×12列</LABEL><br>";
            $opt = array(1, 2);
            if ($model->schoolName == "sundaikoufu" && ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND == "P")) {
                $model->field["FORM_DIV"] = ($model->field["FORM_DIV"] == "") ? "2" : $model->field["FORM_DIV"];
            } else {
                $model->field["FORM_DIV"] = ($model->field["FORM_DIV"] == "") ? "1" : $model->field["FORM_DIV"];
            }
            $extra = array("onclick=\"kubun();\" id=\"FORM_DIV1\"", "onclick=\"kubun();\" id=\"FORM_DIV2\"");
            $radioArray = knjCreateRadio($objForm, "FORM_DIV", $model->field["FORM_DIV"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            //かな氏名印字
            $arg["label_KANA"] = "<LABEL for=\"KANA\">かな氏名印字する</LABEL><br>";
            $extra = $model->field["KANA"] == "1" ? "checked" : "";
            $extra .= " id=\"KANA\"";
            $arg["data"]["KANA"] = knjCreateCheckBox($objForm, "KANA", "1", $extra, "");

            //両面ともに氏名印字
            $arg["label_RYOMEN"] = "<LABEL for=\"RYOMEN\">両面ともに氏名印字する</LABEL><br>";
            $extra = $model->field["RYOMEN"] == "1" ? "checked" : "";
            $extra .= " id=\"RYOMEN\"";
            $arg["data"]["RYOMEN"] = knjCreateCheckBox($objForm, "RYOMEN", "1", $extra, "");
        }

        if (strpos($model->Properties["useFormNameA223B"], "KNJA223B_6") !== false) {
            $arg["is_doushisha_kokusai"] = 1;
            //カレンダーコントロール
            $value = isset($model->field["DATE"]) ? $model->field["DATE"] : $model->control["学籍処理日"];
            $arg["el"]["DATE"] = View::popUpCalendar2($objForm, "DATE", $value, "reload=true", " btn_submit('knja223b')", "");
        } else {
            $arg["is_doushisha_kokusai"] = 0;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);
        knjCreateHidden($objForm, "KOMA_FLG", $koma_flg);
        knjCreateHidden($objForm, "useFormNameA223B", $model->Properties["useFormNameA223B"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja223bForm1.html", $arg);
    }
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knja223bQuery::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = createCombo($objForm, "CLASS_NAME", "", $row1, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = createCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $arg["button"]["btn_end"]   = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg)
{
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("PRGID", "KNJA223B"));
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("selectdata"));
    $objForm->ae(createHiddenAe("GAKKI", CTRL_SEMESTER));
    $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
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

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{
    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}

//テキスト作成
function createTextBox(&$objForm, $data, $name, $size, $maxlen, $extra)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $data,
                        "extrahtml" => $extra) );
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
