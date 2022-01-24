<?php

require_once('for_php7.php');

class knjp830Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp830index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != 'changeCmb') {
            $Row = knjp830Query::getRow($model, $model->gradeHrClass);
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        $model->year = ($model->year != '') ? $model->year: CTRL_YEAR;

        //クラス名称名
        $arg["data"]["HR_NAME"] = ($model->gradeHrClass != '') ? $model->gradeHrClass.':'.$model->hrClassName: '';

        //設置コンボ
        $query = knjp830Query::getSglSchoolKind();
        $extra = "onchange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SGL_SCHOOLKIND"], "SGL_SCHOOLKIND", $extra, 1, "BLANK");

        //学部コンボ
        $query = knjp830Query::getSglMajorCd($Row["SGL_SCHOOLKIND"]);
        $extra = "onchange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SGL_MAJORCD"], "SGL_MAJORCD", $extra, 1, "BLANK");

        //学科コンボ
        $query = knjp830Query::getSglCourseCd($Row["SGL_SCHOOLKIND"], $Row["SGL_MAJORCD"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["SGL_COURSECODE"], "SGL_COURSECODE", $extra, 1, "BLANK");

        //修正
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete')\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリア
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "GRADE", $model->grade);

        if (VARS::get("cmd") != "edit" && $model->cmd != "changeCmb"){
            $arg["reload"]  = "window.open('knjp830index.php?cmd=list&YEAR={$model->year}&GRADE={$model->grade}','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp830Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
