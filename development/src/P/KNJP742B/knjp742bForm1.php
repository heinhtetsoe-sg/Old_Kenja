<?php
class knjp742bForm1 {
    function main(&$model) {

        $objForm =& new form();

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //タイトルの表示
        $arg["data"]["TITLE"] = CTRL_YEAR."年度　　入金／返金処理（ＣＳＶ取込）";

        $securityCnt = $db->getOne(knjp742bQuery::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //取込（1:引落し, 2:返金）
        $opt = array(1, 2);
        $model->field["INPUT"] = ($model->field["INPUT"] == "") ? "1" : $model->field["INPUT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"INPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "INPUT", $model->field["INPUT"], $extra, $opt, count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力取込種別ラジオボタン(1:データ取込 2:エラー出力)
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //校種コンボ
        $query = knjp742bQuery::getSchkind($model);
        $extra = "onchange=\"btn_submit('chgSchKind')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "");

        //取扱銀行コンボ
        $query = knjp742bQuery::getTargetBank($model);
        $extra = "";
        $model->targetBankCd = array();
        $model->targetBankCd = makeCmb($objForm, $arg, $db, $query, $model->field["BANK_CD"], "BANK_CD", $extra, 1, "");

        //引落し月コンボ
        if ($model->field["INPUT"] == "1") {
            $arg["HIKIOTOSHI"] = "1";
            $query = knjp742bQuery::getPaidMonth($model);
            $extra = "";
            $model->field["PAID_MONTH"] = ($model->cmd != "chgSchKind") ? $model->field["PAID_MONTH"] : "";
            makeCmb($objForm, $arg, $db, $query, $model->field["PAID_MONTH"], "PAID_MONTH", $extra, 1, "BLANK");
        }

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

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
        knjCreateHidden($objForm, "PRGID", "KNJP742B");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp742bindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp742bForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $retArray = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
            'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
        if ($name == "BANK_CD") {
            $retArray[$row["VALUE"]] = $row["TARGET_BANK_CD"];
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
    return $retArray;
}
?>
