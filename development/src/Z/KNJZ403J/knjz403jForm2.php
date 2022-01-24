<?php

require_once('for_php7.php');


class knjz403jForm2{

    function main(&$model){

        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz403jindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->school_kind) && isset($model->studyrec_code)) {
            $Row = knjz403jQuery::getRow($model->school_kind, $model->studyrec_code);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //校種
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //コンボ
            $query = knjz403jQuery::getSchoolKind($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $Row["SCHOOL_KIND"], $extra, 1);
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            //表示
            $schoolkind = ($Row["SCHOOL_KIND"]) ? $Row["SCHOOL_KIND"] : SCHOOLKIND;
            $query = knjz403jQuery::getSchoolKind($model, $schoolkind);
            $school_kind_name = $db->getOne($query);
            $arg["data"]["SCHOOL_KIND"] = $school_kind_name;
            knjCreateHidden($objForm, "SCHOOL_KIND", $schoolkind);
        } else {
            //コンボ
            $query = knjz403jQuery::getSchoolKind($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $Row["SCHOOL_KIND"], $extra, 1);
        }

        //行動の記録コード
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["STUDYREC_CODE"] = knjCreateTextBox($objForm, $Row["STUDYREC_CODE"], "STUDYREC_CODE", 2, 2, $extra);

        //行動の記録名称
        $extra = "";
        $arg["data"]["STUDYREC_CODENAME"] = knjCreateTextBox($objForm, $Row["STUDYREC_CODENAME"], "STUDYREC_CODENAME", 30, 30, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"] = "parent.left_frame.location.reload();";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz403jForm2.html", $arg); 
    }
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
?>
