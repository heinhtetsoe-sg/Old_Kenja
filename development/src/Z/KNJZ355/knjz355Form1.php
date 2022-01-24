<?php

require_once('for_php7.php');

class knjz355Form1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //DB接続
        $db = Query::dbCheckOut();
        //今年度・今学期名及びタイトルの表示
        $arg["data"]["TITLE"] = "アクセスログ一覧 (CSV)";
        if (!$model->field["YEAR"]) $model->field["YEAR"] = CTRL_YEAR;

        //開始日
        $model->field["S_DATE"] = ($model->field["S_DATE"]) ? $model->field["S_DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["S_DATE"] = View::popUpCalendar2($objForm, "S_DATE", $model->field["S_DATE"], "", "", "");

        //終了日
        $model->field["E_DATE"] = ($model->field["E_DATE"]) ? $model->field["E_DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["E_DATE"] = View::popUpCalendar2($objForm, "E_DATE", $model->field["E_DATE"], "", "", "");

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            if($model->cmd == "") {
                $check_header = "checked";
            } else {
                $check_header = "";
            }
        }

        $extra = " id=\"HEADER\"";
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "HEADER",
                            "value"     => "on",
                            "extrahtml" =>$check_header.$extra ));
        $arg["data"]["HEADER"] = $objForm->ge("HEADER");

        /**********/
        /* ボタン */
        /**********/
        //実行
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz355index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz355Form1.html", $arg);
    }
}
/******************************************************* 以下関数 *******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model) {
    $opt = array();
    $value_flg = false;
    if ($name == 'KINTAI') {
        if ($model->field["HANI_DIV"] == '1') {
            $kintai = "'1','2','3','6','14'";
        } else {
            $kintai = "'1','2','3','6'";
        }
        $query = knjz355Query::getDiCd($kintai, $model->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
    } elseif ($name == 'YEAR') {
        $query     = knjz355Query::getThisYear();
        $thisYear  = $db->getOne($query); //コントロールマスタのMAX(CTRL_YEAR)を取得
        $startYear = (int)$thisYear - (int)$model->properties["useAdminYearPast"];   //menuInfo.propertiesの値を引く
        $endYear   = (int)$thisYear + (int)$model->properties["useAdminYearFuture"]; //menuInfo.propertiesの値を足す
        $opt       = array();
        for ($i = $startYear; $i <= $endYear; $i++) {
            $opt[] = array("label" => $i,"value" => $i);
        }
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
