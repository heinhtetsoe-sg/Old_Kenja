<?php

require_once('for_php7.php');

class knjj143Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjj143Form1", "POST", "knjj143index.php", "", "knjj143Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //委員会区分
        $query = knjj143Query::getCommitteeFlg();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "COMMITTEE_FLG", $value, $extra, 1, "");

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'pdf');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //CSVボタン
        $extra = "onclick=\"btn_submit('csv_council');\"";
        $arg["button"]["btn_csv1"] = knjCreateBtn($objForm, "btn_csv1", "生徒会ＣＳＶ出力", $extra);
        $extra = "onclick=\"btn_submit('csv_committee');\"";
        $arg["button"]["btn_csv2"] = knjCreateBtn($objForm, "btn_csv2", "委員会ＣＳＶ出力", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJJ143");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj143Form1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
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
