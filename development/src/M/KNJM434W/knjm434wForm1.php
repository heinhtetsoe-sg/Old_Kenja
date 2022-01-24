<?php

require_once('for_php7.php');

class knjm434wForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm434wForm1", "POST", "knjm434windex.php", "", "knjm434wForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //ログイン年度・学期
        $arg["data"]["YEAR"] = CTRL_YEAR;
        $arg["data"]["CTRL_SEMESTERNAME"] = CTRL_SEMESTERNAME;

        //ラジオボタンを作成 1:クラス別 2:科目別
        $opt = array(1, 2);
        if (!$model->field["OUTPUT_DIV"]) $model->field["OUTPUT_DIV"] = "2";
        $onclick = "onclick =\" return btn_submit('knjm434w');\"";
        $extra = array("id=\"OUTPUT_DIV1\" ".$onclick , "id=\"OUTPUT_DIV2\" ".$onclick);
        $radioArray = knjCreateRadio($objForm, "OUTPUT_DIV", $model->field["OUTPUT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //卒業予定のみ
        $extra  = "id=\"SOTUGYO_YOTEI\"";
        $extra .= ($model->field["SOTUGYO_YOTEI"] == '1' || $model->cmd == "") ? " checked" : "";
        $arg["data"]["SOTUGYO_YOTEI"] = knjCreateCheckBox($objForm, "SOTUGYO_YOTEI", "1", $extra);

        //対象科目ラジオボタン 1:前期科目 2:通年、後期科目 3:全て
        $model->useZenkiKouki = $db->getOne(knjm434wQuery::getZenkiKoukiCount("M015")) > 0 || $db->getOne(knjm434wQuery::getZenkiKoukiCount("M016")) > 0;
        if ($model->useZenkiKouki) {
            $arg["showSubclassFlg"] = "1";
            $opt_subclass = array(1, 2, 3);
            $model->field["SUBCLASS_FLG"] = $model->field["SUBCLASS_FLG"] ? $model->field["SUBCLASS_FLG"] : '1';
            $extra = array("id=\"SUBCLASS_FLG1\"", "id=\"SUBCLASS_FLG2\"", "id=\"SUBCLASS_FLG3\"");
            $radioArray = knjCreateRadio($objForm, "SUBCLASS_FLG", $model->field["SUBCLASS_FLG"], $extra, $opt_subclass, get_count($opt_subclass));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        } else {
            $model->field["SUBCLASS_FLG"] = '3';
        }

        //クリア処理とコピー処理のラジオボタン 1:クリア処理 2:コピー処理
        $opt_shori = array(1, 2);
        $model->field["SHORI"] = $model->field["SHORI"] ? $model->field["SHORI"] : '1';
        $extra = array("id=\"SHORI1\"", "id=\"SHORI2\"");
        $radioArray = knjCreateRadio($objForm, "SHORI", $model->field["SHORI"], $extra, $opt_shori, get_count($opt_shori));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //クリア処理とコピー処理の各チェックボックス
        foreach (array("CLEAR1", "CLEAR2", "CLEAR3", "COPY1", "COPY2", "COPY3") as $ce) {
            $extra = "id=\"$ce\"";
            $extra .= ($model->field[$ce] == '1' || $model->cmd == "") ? " checked" : "";
            $extra .= " onclick=\"return chk(this);\"";
            $arg["data"][$ce] = knjCreateCheckBox($objForm, $ce, "1", $extra);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm434wForm1.html", $arg); 
    }
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //対象者(左のリスト）
    $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
    //クラス一覧
    $leftList = $rightList = array();
    if ($model->field["OUTPUT_DIV"] == "1") {
        $arg["data"]["OUTPUT_DIV_NAME"] = "クラス";
        $query = knjm434wQuery::getHrClass($model);
    } else {
        $arg["data"]["OUTPUT_DIV_NAME"] = "科目";
        $query = knjm434wQuery::getSubclass($model);
    }
    knjCreateHidden($objForm, "OUTPUT_DIV_NAME", $arg["data"]["OUTPUT_DIV_NAME"]);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $array)) {
            $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        } else {
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $rightList, $extra, 12);

    //出力対象作成
    $extra = "multiple style=\"width:250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $leftList, $extra, 12);

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
function makeBtn(&$objForm, &$arg, $model) {
    //実行ボタン
    $arg["button"]["btn_exe"] = knjCreateBtn($objForm, "btn_exe", "実 行", "onclick=\"return doSubmit();\"");
    //閉じるボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
