<?php

require_once('for_php7.php');

class knjz183Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz183index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //科目コンボ
        $query = knjz183Query::getSubclass($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASS", $model->field["SUBCLASS"], $extra, 1);

        //コースグループコンボ
        if ($model->schoolkind === 'H') {
            $arg["CourseGroup"] = "1";
            $arg["SET_DIV_NAME"] = 'コースグループ';
            $query = knjz183Query::getGroupCd($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "GROUP_CD", $model->field["GROUP_CD"], $extra, 1);
        //コースコンボ
        } else {
            $arg["CourseMajor"] = "1";
            $arg["SET_DIV_NAME"] = 'コース';
            $query = knjz183Query::getCourseMajor($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1);
        }

        //割合
        $extra = "STYLE=\"text-align:right\" onblur=\"this.value=calc(this);\"";
        $arg["data"]["RATE"] = knjCreateTextBox($objForm, $model->field["RATE"], "RATE", 3, 3, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('insert');\"";
        $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "GRADE", $model->grade);
        knjCreateHidden($objForm, "GET_SCHOOL_KIND", $model->schoolkind);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "insert" || VARS::post("cmd") == "delete") {
            $arg["reload"]  = "window.open('knjz183index.php?cmd=list2&GRADE={$model->grade}', 'left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz183Form2.html", $arg);
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
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
