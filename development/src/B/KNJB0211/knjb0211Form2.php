<?php

require_once('for_php7.php');

class knjb0211Form2
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjb0211index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $query = knjb0211Query::getRow($model->groupcd, $model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //学年
        $query = knjb0211Query::getGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra, 1, "BLANK");

        //校種
        $model->schoolKind = "";
        $query = knjb0211Query::getSchoolKind($model);
        $model->schoolKind = $db->getOne($query);

        //群コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["GROUP_CD"] = knjCreateTextBox($objForm, $Row["GROUP_CD"], "GROUP_CD", 3, 3, $extra);

        //群名称
        $extra = "";
        $arg["data"]["GROUP_NAME"] = knjCreateTextBox($objForm, $Row["GROUP_NAME"], "GROUP_NAME", 40, 40, $extra);

        //群略称
        $extra = "";
        $arg["data"]["GROUP_ABBV"] = knjCreateTextBox($objForm, $Row["GROUP_ABBV"], "GROUP_ABBV", 30, 30, $extra);

        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタンを作成する
        $extra = "onclick=\"return ShowConfirm('clear')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "YEAR", $Row["YEAR"]);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjb0211index.php?cmd=list';";
        }

        View::toHTML($model, "knjb0211Form2.html", $arg); 
    }
}

//コンボ作成
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
