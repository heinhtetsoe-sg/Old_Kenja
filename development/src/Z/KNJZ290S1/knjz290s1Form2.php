<?php

require_once('for_php7.php');

class knjz290s1Form2
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz290s1index.php", "", "edit");

        $db = Query::dbCheckOut();

        $query = knjz290s1Query::selectSchoolCd();
        $model->schoolCd = $db->getOne($query);

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->getField["STAFFCD"]) {
            $query = knjz290s1Query::getDispData($model->getField);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $model->field["SCHOOL_KIND"] = $model->getField["SCHOOL_KIND"];
            $Row =& $model->field;
        }

        //校種
        $query = knjz290s1Query::getSchoolKind();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "BLANK");

        //職員
        $query = knjz290s1Query::getStaff($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["STAFFCD"], "STAFFCD", $extra, 1, "BLANK");

        //開始日付
        $arg["data"]["FROM_DATE"] = View::popUpCalendar($objForm, "FROM_DATE", str_replace("-", "/", $Row["FROM_DATE"]));

        //終了日付
        $arg["data"]["TO_DATE"] = str_replace("-", "/", $Row["TO_DATE"]);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //disabled
        $disable = isset($model->warning) && $model->cmd != "addEdit" ? "" : " disabled ";
        if ($model->cmd == "selectEdit" || $model->cmd == "reset" || $model->cmd == "addEdit" || $model->cmd == "updEdit") {
            $query = knjz290s1Query::isNewData($Row);
            $cnt = $db->getOne($query);
            if ($cnt > 0) {
                $disable = "";
            }
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disable);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz290s1index.php?cmd=list';";
        }
                                
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz290s1Form2.html", $arg);
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
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
