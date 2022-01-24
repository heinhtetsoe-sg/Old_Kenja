<?php

require_once('for_php7.php');

class knjz025form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz025index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //起動時チェック
        $arg["Closing"] = "";
        //教育委員会チェック
        if ($db->getOne(knjz025Query::checkEdboard()) != 1) {
           $arg["Closing"] = " closing_window(1);";
        }
        //セキュリティーチェック
        if(AUTHORITY != DEF_UPDATABLE) {
            $arg["Closing"] = " closing_window(1); ";
        }
        //データの存在チェック
        if ($db->getOne(knjz025Query::checkControlMst()) == 0) {
           $arg["Closing"] = " closing_window(2);";
        }

        //年度コンボ作成
        makeYear($objForm, $arg, $db, $model);

        //教育委員会情報取得
        if (!isset($model->warning)) {
            $result = $db->query(knjz025Query::selectQuery($model->year));
            $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        } else {
            $row =& $model->field;
        }

        //都道府県コンボ
        $query = knjz025Query::getPrefCd();
        makeCmb($objForm, $arg, $db, $query, "PREF_CD", $row["PREF_CD"], "", 1);

        //教育委員会名称１
        $arg["data"]["SCHOOLNAME1"] = knjCreateTextBox($objForm, $row["SCHOOLNAME1"], "SCHOOLNAME1", 60, 90, "");

        //教育委員会名称２
        $arg["data"]["SCHOOLNAME2"] = knjCreateTextBox($objForm, $row["SCHOOLNAME2"], "SCHOOLNAME2", 60, 90, "");

        //教育委員会名称３
        $arg["data"]["SCHOOLNAME3"] = knjCreateTextBox($objForm, $row["SCHOOLNAME3"], "SCHOOLNAME3", 60, 90, "");

        //教育委員会名称英字
        $arg["data"]["SCHOOLNAME_ENG"] = knjCreateTextBox($objForm, $row["SCHOOLNAME_ENG"], "SCHOOLNAME_ENG", 60, 60, "");

        //郵便番号
        $arg["data"]["SCHOOLZIPCD"] = View::popUpZipCode($objForm, "SCHOOLZIPCD", $row["SCHOOLZIPCD"],"SCHOOLADDR1");

        //住所１
        $arg["data"]["SCHOOLADDR1"] = knjCreateTextBox($objForm, $row["SCHOOLADDR1"], "SCHOOLADDR1", 60, 90, "");

        //住所２
        $arg["data"]["SCHOOLADDR2"] = knjCreateTextBox($objForm, $row["SCHOOLADDR2"], "SCHOOLADDR2", 60, 90, "");

        //住所１英字
        $arg["data"]["SCHOOLADDR1_ENG"] = knjCreateTextBox($objForm, $row["SCHOOLADDR1_ENG"], "SCHOOLADDR1_ENG", 60, 70, "");

        //住所２英字
        $arg["data"]["SCHOOLADDR2_ENG"] = knjCreateTextBox($objForm, $row["SCHOOLADDR2_ENG"], "SCHOOLADDR2_ENG", 60, 70, "");

        //電話番号
        $arg["data"]["SCHOOLTELNO"] = knjCreateTextBox($objForm, $row["SCHOOLTELNO"], "SCHOOLTELNO", 20, 14, "");

        //FAX番号
        $arg["data"]["SCHOOLFAXNO"] = knjCreateTextBox($objForm, $row["SCHOOLFAXNO"], "SCHOOLFAXNO", 20, 14, "");

        //メールアドレス
        $arg["data"]["SCHOOLMAIL"] = knjCreateTextBox($objForm, $row["SCHOOLMAIL"], "SCHOOLMAIL", 30, 25, "");

        //ホームページ
        $arg["data"]["SCHOOLURL"] = knjCreateTextBox($objForm, $row["SCHOOLURL"], "SCHOOLURL", 30, 30, "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjz025Form1.html", $arg);
    }
}
//年度コンボ作成
function makeYear(&$objForm, &$arg, $db, $model) {
    $opt = array();
    $value_flg = false;
    $query = knjz025Query::selectYearQuery();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["YEAR"], 
                       "value" => $row["YEAR"]);
        if ($row["YEAR"] == $model->year) $value_flg = true;
    }
    $result->free();
    $model->year = ($model->year && $value_flg) ? $model->year : CTRL_YEAR;

    $extra = "onchange=\"return btn_submit('');\"";
    $arg["year"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

    //追加年度
    $extra = "onblur=\"this.value=toInteger(this.value);\"";
    $arg["year_add"] = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);

    //年度追加ボタン
    $extra = "onclick=\"return add('');\"";
    $arg["btn_year_add"] = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {

    //更新ボタン
    $extra = "onClick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onClick=\"return showConfirm();\"";
    $arg["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "取 消", $extra);
    //終了ボタン
    $extra = "onClick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //教育委員会集計用学校番号ボタン
    $link = REQUESTROOT."/Z/KNJZ081/knjz081index.php?cmd=&SEND_PRGID=KNJZ025&SEND_SUBMIT=1&SEND_AUTH=".AUTHORITY;
    $extra = "onclick=\"document.location.href='$link'\"";
    $arg["btn_knjz081"] = knjCreateBtn($objForm, "btn_knjz081", " 教育委員会集計用学校番号 ", $extra);
}
?>
