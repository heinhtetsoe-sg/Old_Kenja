<?php

require_once('for_php7.php');

class knjz051_3aForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz051_3aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->coursecd && $model->majorcd && $model->year) {
            $Row = $db->getRow(knjz051_3aQuery::getRow($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //選択した課程学科を表示
        $arg["data"]["COURSE_MAJORCD"] = $Row["COURSECD"].$Row["MAJORCD"].'　'.$Row["COURSENAME"].'　'.$Row["MAJORNAME"];

        //学校区分コンボ
        $opt = array();
        $value_flg = false;
        $opt[] = array('label' => "",'value' => "");
        $query = knjz051_3aQuery::getMajorSchoolDiv();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["SCHOOLDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["SCHOOLDIV"] = ($Row["SCHOOLDIV"] != "" && $value_flg) ? $Row["SCHOOLDIV"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["SCHOOLDIV"] = knjCreateCombo($objForm, "SCHOOLDIV", $Row["SCHOOLDIV"], $opt, $extra, 1);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/Z/KNJZ051A/knjz051aindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"] = "parent.left_frame.location.href='knjz051_3aindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz051_3aForm2.html", $arg);
    }
}
?>
