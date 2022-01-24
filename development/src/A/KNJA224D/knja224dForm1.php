<?php

require_once('for_php7.php');

class knja224dForm1 {
    function main(&$model){
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja224dForm1", "POST", "knja224dindex.php", "", "knja224dForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //帳票種類radio
        $opt = array(1, 2, 3, 4);
        $model->field["FRM_PATERN"] = getDefVal($model, $model->field["FRM_PATERN"], "1", $model->PrgDefaultVal["FRM_PATERN"], $model->cmd);
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"FRM_PATERN{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "FRM_PATERN", $model->field["FRM_PATERN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ふりがな出力チェックボックス
        $model->field["KANA_PRINT"] = getDefVal($model, $model->field["KANA_PRINT"], "1", $model->PrgDefaultVal["KANA_PRINT"], $model->cmd);
        $extra = $model->field["KANA_PRINT"] == "1" ? " checked " : "";
        $arg["data"]["KANA_PRINT"] = knjCreateCheckBox($objForm, "KANA_PRINT", $model->field["KANA_PRINT"], $extra." id=\"KANA_PRINT\"", "");

        //出力件数
        $extraInt    = " onblur=\"this.value=toInteger(this.value)\";";
        $extraRight  = " STYLE=\"text-align: right\"";
        $model->field["KENSUU"] = ($model->field["KENSUU"]) ? $model->field["KENSUU"] : 1;
        $arg["data"]["KENSUU"] = knjCreateTextBox($objForm, $model->field["KENSUU"], "KENSUU", 3, 2, $extraInt.$extraRight);

        //除籍者名前無し
        $model->field["GRD_NAME_NASI"] = getDefVal($model, $model->field["GRD_NAME_NASI"], "1", $model->PrgDefaultVal["GRD_NAME_NASI"], $model->cmd);
        $extra = $model->field["GRD_NAME_NASI"] == "1" ? " checked " : "";
        $extra .= "id=\"GRD_NAME_NASI\"";
        $arg["data"]["GRD_NAME_NASI"] = knjCreateCheckBox($objForm, "GRD_NAME_NASI", "1", $extra);

        //ボタン作成
        $model->schoolCd = $db->getOne(knja224dQuery::getSchoolCd());
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja224dForm1.html", $arg);
    }
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knja224dQuery::getAuth($model));
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

function getDefVal($model, $setVal, $defVal, $prgDefVal, $cmd) {
    $retVal = $defVal;
    if (method_exists($model, 'getSetDefaultVal')) {
        $retVal = $model->getSetDefaultVal($setVal, $defVal, $prgDefVal, $cmd);
    }
    return $retVal;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('".SERVLET_URL."', 'KNJA224D');\"");
    //EXCEL出力ボタン
    $extra = "onclick=\"return newwin2('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
    $arg["button"]["btn_xls"] = knjCreateBtn($objForm, "btn_xls", "EXCEL出力", $extra);
    //終了ボタン
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg)
{
    //hidden
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA224D");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "TEMPLATE_PATH");
}
?>
