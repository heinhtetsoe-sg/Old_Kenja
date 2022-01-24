<?php

require_once('for_php7.php');

class knja200aForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja200aForm1", "POST", "knja200aindex.php", "", "knja200aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1" && $model->Properties["useClubMultiSchoolKind"] != "1") {
            $arg["schkind"] = "1";
            $query = knja200aQuery::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knja200a');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHKIND"], "SCHKIND", $extra, 1);
        }

        //学期コンボ
        $query = knja200aQuery::getSemester(CTRL_YEAR);
        $extra = "onchange=\"return btn_submit('knja200a');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //radio
        $opt = array(1, 2, 3);
        $model->field["DATA_DIV"] = $model->getSetDefaultVal($model->field["DATA_DIV"], "1", $model->PrgDefaultVal["DATA_DIV"], $model->cmd);
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATA_DIV{$val}\" onClick=\"btn_submit('knja200a')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATA_DIV", $model->field["DATA_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //対象データ
        if ($model->field["DATA_DIV"] == "1") {
            $query = knja200aQuery::getClub($model);
        } else if ($model->field["DATA_DIV"] == "2") {
            $query = knja200aQuery::getCommittee($model, "1");
        } else {
            $query = knja200aQuery::getHR($model);
        }

        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["DATA_CMB"], "DATA_CMB", $extra, 1, "");

        //作成日
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $value);

        //電話番号チェックボックスを作成する
        $model->field["TEL"] = $model->getSetDefaultVal($model->field["TEL"], "1", $model->PrgDefaultVal["TEL"], $model->cmd);
        if ($model->field["TEL"] == "1") {
            $check_tel = "checked";
        } else {
            $check_tel = "";
        }
        $extra = $check_tel." id=\"TEL\"";
        $arg["data"]["TEL"] = knjCreateCheckBox($objForm, "TEL", "1", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        if (AUTHORITY == DEF_UPDATABLE) {
            //Default更新ボタンを作成する
            $extra = " onclick=\"return updDefault('defaultUpd', 'KNJA200A');\" accesskey=?";
            $arg["button"]["defaultUpd"] = knjCreateBtn($objForm, "defaultUpd", "初期値設定", $extra);
        }
        knjCreateHidden($objForm, "UPD_DEFAULT_VALUE");

        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA200A");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            knjCreateHidden($objForm, "SCHOOLKIND", $model->field["SCHKIND"]);
        } else {
            knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        }
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useClubMultiSchoolKind", $model->Properties["useClubMultiSchoolKind"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja200aForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
