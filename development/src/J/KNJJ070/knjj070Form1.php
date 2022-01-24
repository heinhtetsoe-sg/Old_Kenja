<?php

require_once('for_php7.php');


class knjj070Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjj070Form1", "POST", "knjj070index.php", "", "knjj070Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1" && $model->Properties["useClubMultiSchoolKind"] != "1") {
            $arg["schkind"] = "1";
            $query = knjj070Query::getSchkind($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        //DB切断
        Query::dbCheckIn($db);

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID","KNJJ070");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
        knjCreateHidden($objForm, "simo", $simo);
        knjCreateHidden($objForm, "fuseji", $fuseji);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useClubMultiSchoolKind", $model->Properties["useClubMultiSchoolKind"]);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj070Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name == "SCHKIND") {
        $opt[] = array("label" => "全て", "value" => "");
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
