<?php

require_once('for_php7.php');


class knje372Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje372Form1", "POST", "knje372index.php", "", "knje372Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //対象ラジオ 1:合格 2:最終進路 3:進路調査（在籍者のみ）4:受験
        $model->field["OUT_DIV"] = $model->field["OUT_DIV"] ? $model->field["OUT_DIV"] : '1';
        $opt_outdiv = array(1, 2, 3, 4);
        $extra = array("id=\"OUT_DIV1\" onclick =\" return btn_submit('knje372');\"", "id=\"OUT_DIV2\" onclick =\" return btn_submit('knje372');\"", "id=\"OUT_DIV3\" onclick =\" return btn_submit('knje372');\"", "id=\"OUT_DIV4\" onclick =\" return btn_submit('knje372');\"");
        $radioArray = knjCreateRadio($objForm, "OUT_DIV", $model->field["OUT_DIV"], $extra, $opt_outdiv, get_count($opt_outdiv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["OUT_DIV"] == "3") {
            $arg["shinro_tyousa"] = 2;
        } else {
            $arg["shinro_saki"] = 1;
        }

        /************/
        /* 進 路 先 */
        /************/
        //受験方式ラジオ 1:出力あり 2:出力なし
        $model->field["JUKEN_DIV"] = $model->field["JUKEN_DIV"] ? $model->field["JUKEN_DIV"] : '1';
        $opt_jukendiv = array(1, 2);
        $extra = array("id=\"JUKEN_DIV1\"", "id=\"JUKEN_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "JUKEN_DIV", $model->field["JUKEN_DIV"], $extra, $opt_jukendiv, get_count($opt_jukendiv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //既卒者ラジオ 1:出力あり 2:出力なし
        $model->field["KISOTU_DIV"] = $model->field["KISOTU_DIV"] ? $model->field["KISOTU_DIV"] : '1';
        $opt_kisotudiv = array(1, 2);
        $extra = array("id=\"KISOTU_DIV1\"", "id=\"KISOTU_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "KISOTU_DIV", $model->field["KISOTU_DIV"], $extra, $opt_kisotudiv, get_count($opt_kisotudiv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //延べ人数で出力チェックボックス
        $extra = ($model->field["CNT_NOBE"] == "1") ? "checked" : "";
        $extra .= " id=\"CNT_NOBE\"";
        $arg["data"]["CNT_NOBE"] = knjCreateCheckBox($objForm, "CNT_NOBE", "1", $extra, "");

        //地区コード別checkbox
        if ($model->Properties["useAreaCd_KNJE372"] == '1') {
            //checkbox
            $extra  = ($model->field["AREACD"] == "1") ? " checked" : "";
            $extra .= " id=\"AREACD\"";
            $arg["data"]["AREACD"] = knjCreateCheckBox($objForm, "AREACD", "1", $extra);
            $arg["useAreaCd_KNJE372"] = "1";
        }

        /***************/
        /* 進 路 調 査 */
        /***************/
        //調査名コンボボックス
        $query = knje372Query::getQuestionnaire();
        makeCmb($objForm, $arg, $db, $query, "QUESTIONNAIRECD", $model->field["QUESTIONNAIRECD"], "", 1);

        //調査名ラジオ 1:第1希望 2:第2希望
        $model->field["CHOICE"] = $model->field["CHOICE"] ? $model->field["CHOICE"] : '1';
        $opt_choice = array(1, 2);
        $extra = array("id=\"CHOICE1\"", "id=\"CHOICE2\"");
        $radioArray = knjCreateRadio($objForm, "CHOICE", $model->field["CHOICE"], $extra, $opt_choice, get_count($opt_choice));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学校系列ラジオ 1:専門学校 2:専門学校以外 3:全て
        $model->field["SCHOOL_GROUP"] = $model->field["SCHOOL_GROUP"] ? $model->field["SCHOOL_GROUP"] : '1';
        $opt_school_div = array(1, 2, 3);
        $extra = array("id=\"SCHOOL_GROUP1\"", "id=\"SCHOOL_GROUP2\"", "id=\"SCHOOL_GROUP3\"");
        $radioArray = knjCreateRadio($objForm, "SCHOOL_GROUP", $model->field["SCHOOL_GROUP"], $extra, $opt_school_div, get_count($opt_school_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ＣＳＶボタンを作成する
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJE372");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje372Form1.html", $arg); 
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

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
