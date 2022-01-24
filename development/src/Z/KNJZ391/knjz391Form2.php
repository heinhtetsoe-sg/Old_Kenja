<?php

require_once('for_php7.php');

class knjz391Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz391index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->year) && isset($model->course_cd)) {
            $Row = $db->getRow(knjz391Query::getChildcareBusYmst($model, $model->course_cd), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //時間帯ラジオボタン 1:前半 2:後半
        $opt = array(1, 2);
        $Row["SCHEDULE_CD"] = ($Row["SCHEDULE_CD"] == "") ? "1" : $Row["SCHEDULE_CD"];
        $extra = array("id=\"SCHEDULE_CD1\"", "id=\"SCHEDULE_CD2\"");
        $radioArray = knjCreateRadio($objForm, "SCHEDULE_CD", $Row["SCHEDULE_CD"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //コード
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COURSE_CD"] = knjCreateTextBox($objForm, $Row["COURSE_CD"], "COURSE_CD", 3, 2, $extra);

        //名称
        $extra = "";
        $arg["data"]["BUS_NAME"] = knjCreateTextBox($objForm, $Row["BUS_NAME"], "BUS_NAME", 20, 20, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knjz391index.php?cmd=list&shori=update','left_frame');";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz391Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->year >= CTRL_YEAR) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
    //削除ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->year >= CTRL_YEAR) ? "onclick=\"return btn_submit('delete');\"" : "disabled";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
