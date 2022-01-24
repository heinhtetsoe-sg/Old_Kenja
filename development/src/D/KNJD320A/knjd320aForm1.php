<?php

require_once('for_php7.php');

class knjd320aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd320aForm1", "POST", "knjd320aindex.php", "", "knjd320aForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度を作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //総合順位出力ラジオボタン 1:学年 2:コース
        $opt_rank = array(1, 2); 
        $model->field["OUTPUT_RANK"] = ($model->field["OUTPUT_RANK"] == "") ? "1" : $model->field["OUTPUT_RANK"];
        $extra = array("id=\"OUTPUT_RANK1\"", "id=\"OUTPUT_RANK2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_RANK", $model->field["OUTPUT_RANK"], $extra, $opt_rank, get_count($opt_rank));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学期コンボボックスを作成する
        $extra = "onchange=\"return btn_submit('knjd320a');\"";
        $query = knjd320aQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //テスト種別コンボボックスを作成する
        $extra = "onchange=\"return btn_submit('knjd320a');\"";
        $query = knjd320aQuery::getTestcd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTCD"], "TESTCD", $extra, 1);
        //出力条件　順位の名称設定
        $setname = "";
        $setname = '順位';
        $arg["data"]["NAME"] = '順位';
        $arg["data"]["JYOUKEN1"] = '不振者';
        $arg["data"]["JYOUKEN2"] = '位まで';

        //学年コンボボックスを作成する
        $query = knjd320aQuery::getSelectGrade($model);
        $extra = "onchange=\"return btn_submit('knjd320a');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        //得点、順位または評価・評定ラジオボタン
        $opt = array(1, 2); 
        $model->field["OUTPUT_JYOUKEN"] = ($model->field["OUTPUT_JYOUKEN"] == "") ? "1" : $model->field["OUTPUT_JYOUKEN"];
        $extra = array("id=\"OUTPUT_JYOUKEN1\" onclick=\"return btn_submit('knjd320a');\"", "id=\"OUTPUT_JYOUKEN2\" onclick=\"return btn_submit('knjd320a');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_JYOUKEN", $model->field["OUTPUT_JYOUKEN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //hidden
        knjCreateHidden($objForm, "SETNAME", $setname);
        knjCreateHidden($objForm, "JYOUKEN", $model->field["OUTPUT_JYOUKEN"]);

        //得点指定
        if ($model->field["OUTPUT_JYOUKEN"] !== '1') {
            $extra = "disabled style=\"background-color:#D0D0D0;\"";
        } else {
            $extra = "";
        }
        if ($model->field["TESTCD"] === '9900') {
            $value = ($model->field["SCORE_RANGE"]) ? $model->field["SCORE_RANGE"] : "1";
        } else {
            $value = ($model->field["SCORE_RANGE"]) ? $model->field["SCORE_RANGE"] : "30";
        }
        $extra .= " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; ";
        $arg["data"]["SCORE_RANGE"] = createText($objForm, "SCORE_RANGE", $value, $extra, 3, 3);
        
        //不振者順位指定
        if ($model->field["OUTPUT_JYOUKEN"] !== '2') {
            $extra = "disabled style=\"background-color:#D0D0D0;\"";
        } else {
            $extra = "";
        }
        $value = ($model->field["RANK_RANGE"]) ? $model->field["RANK_RANGE"] : "20";
        $extra .= " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; ";
        $arg["data"]["RANK_RANGE"] = createText($objForm, "RANK_RANGE", $value, $extra, 4, 4);

        //出力対象一覧リストを作成する
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd320aForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    if ($query) {
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        if ($name == "SEMESTER") {
            $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
        } else {
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        }
        $result->free();
    }

    if ($blank == "ALL") {
        $opt[] = array("label" => "総合計",
                       "value" => "999999");
    }

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//出力対象一覧リストを作成する
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $query = knjd320aQuery::getSubclass($model);

    //一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    makeCmb($objForm, $arg, $db, $query, $model->field["DUMMY"], "CATEGORY_NAME", $extra, 20, "ALL");

    //出力対象リストを作成する
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    makeCmb($objForm, $arg, $db, "", $model->field["DUMMY"], "CATEGORY_SELECTED", $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //ＣＳＶ出力ボタン
    $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return btn_submit('csv');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $db, $model)
{
    $objForm->ae(createHiddenAe("PRGID", "KNJD320A"));
    $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
    $objForm->ae(createHiddenAe("CTRL_SEMESTER", CTRL_SEMESTER));
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("selectdata"));
    $objForm->ae(createHiddenAe("useCurriculumcd", $model->Properties["useCurriculumcd"]));
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

//テキスト作成
function createText(&$objForm, $name, $value, $extra, $size, $maxlen)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "extrahtml" => $extra,
                        "value"     => $value));
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
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
