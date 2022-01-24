<?php

require_once('for_php7.php');

class knjz200aForm2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz200aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != "edit2" && $model->ibgrade_course && $model->ibclasscd && $model->ibprg_course && $model->ibcurriculum_cd && $model->ibsubclasscd) {
            $query = knjz200aQuery::getRow($model->ibgrade_course, $model->ibclasscd, $model->ibprg_course, $model->ibcurriculum_cd, $model->ibsubclasscd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //IB科目コンボ
        $query = knjz200aQuery::getIBSubclasscd($model, "list");
        $value = $Row["IBCLASSCD"].'-'.$Row["IBPRG_COURSE"].'-'.$Row["IBCURRICULUM_CD"].'-'.$Row["IBSUBCLASSCD"];
        makeCmb($objForm, $arg, $db, $query, "IBSUBCLASS", $value, "", 1, "BLANK");

        //必要時間
        $extra = "onblur=\"checkDecimal(this)\" STYLE=\"text-align: right\"";
        $arg["data"]["NEED_TIME"] = knjCreateTextBox($objForm, $Row["NEED_TIME"], "NEED_TIME", 5, 5, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "edit2") {
            $arg["reload"] = "window.open('knjz200aindex.php?cmd=list&shori=add','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz200aForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $BLANK="") {
    $opt = array();
    $value_flg = false;
    if ($BLANK) $opt[] = array('label' => "", 'value' => "");
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
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
    //一括処理ボタン
    $link = REQUESTROOT."/Z/KNJZ200A/knjz200aindex.php?cmd=replace";
    $extra = "onclick=\"Page_jumper('{$link}');\"";
    $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "一括処理", $extra);
}
?>
