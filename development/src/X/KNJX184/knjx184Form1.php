<?php

require_once('for_php7.php');

class knjx184Form1 {
    function main(&$model) {

        $objForm = new form;

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjx184Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //出力対象radio(1:知的障害, 2:知的障害以外)
        $opt = array(1, 2);
        $model->field["TAISYOU"] = ($model->field["TAISYOU"] == "") ? "1" : $model->field["TAISYOU"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TAISYOU{$val}\" onclick=\"return changeRadioT(this);\"");
        }
        $radioArray = knjCreateRadio($objForm, "TAISYOU", $model->field["TAISYOU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //要録ページ種類radio(1:学習, 2:所見)
        $opt = array(1, 2);
        $model->field["PAGEDIV"] = ($model->field["PAGEDIV"] == "") ? "1" : $model->field["PAGEDIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PAGEDIV{$val}\" onclick=\"return changeRadioP(this);\"");
        }
        $radioArray = knjCreateRadio($objForm, "PAGEDIV", $model->field["PAGEDIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["PAGEDIV"] == "1") {
            $disRadio = "disabled";
        } else {
            $disRadio = "";
        }

        //データ種類radio(1:通年データ, 2:毎年度データ)
        $opt = array(1, 2);
        $model->field["DATADIV"] = ($model->field["DATADIV"] == "") ? "1" : $model->field["DATADIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATADIV{$val}\"".$disRadio);
        }
        $radioArray = knjCreateRadio($objForm, "DATADIV", $model->field["DATADIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力取込種別ラジオボタン(1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, $extra, 1);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //年度一覧コンボボックス
        $query = knjx184query::getYearSeme();
        $extra = "onchange=\"btn_submit('changeCmb');\"";
        $model->field["YEAR"] = $model->field["YEAR"] == "" ? CTRL_YEAR.CTRL_SEMESTER: $model->field["YEAR"];
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1, "");

        //年組一覧コンボボックス
        $query = knjx184query::getGradeHrClass($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "");

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";

        //ボタン作成
        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJX184");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx184index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx184Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($name == "GRADE_HR_CLASS") {
        $opt[] = array("label" => "(全て出力)", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
