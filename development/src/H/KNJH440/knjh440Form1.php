<?php

require_once('for_php7.php');

class knjh440Form1
{
    function main(&$model)
    {
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjh440index.php", "", "sel");
        $db             = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->mockyear;

        //学年
        $extra = "onChange=\"btn_submit('grade')\";";
        $query = knjh440Query::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");

        //模試リスト
        $query = knjh440Query::getListMock($model);
        makeListToList($objForm, $arg, $db, $query, "MAIN_MOCK", "LEFT_MOCK", "RIGHT_MOCK");

        //科目リスト
        $query = knjh440Query::getListSub($model);
        makeListToList($objForm, $arg, $db, $query, "MAIN_SUB", "LEFT_SUB", "RIGHT_SUB");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdataMock");
        knjCreateHidden($objForm, "selectdataSub");

        $arg["finish"]  = $objForm->get_finish();

        //DB切断
        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh440Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストTOリスト
function makeListToList(&$objForm, &$arg, $db, $query, $mainName, $leftName, $rightName) {
    //リスト取得
    $opt_left = $opt_right = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (strlen($row["LEFT_CD"])) {
            $opt_left[]  = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        } else {
            $opt_right[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        }
    }
    $result->free();
    //左
    $extra  = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ";
    $extra .= "ondblclick=\"move('right','{$leftName}','{$rightName}',1)\"";
    $arg[$mainName]["LEFT_PART"] = knjCreateCombo($objForm, $leftName, "left", $opt_left, $extra, "10");
    //右
    $extra  = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ";
    $extra .= "ondblclick=\"move('left','{$leftName}','{$rightName}',1)\"";
    $arg[$mainName]["RIGHT_PART"] = knjCreateCombo($objForm, $rightName, "right", $opt_right, $extra, "10");
    //全追加
    $extra = "onclick=\"return move('sel_add_all','{$leftName}','{$rightName}',1);\"";
    $arg[$mainName]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "SEL_ADD_ALL", "≪", $extra);
    //追加
    $extra = "onclick=\"return move('left','{$leftName}','{$rightName}',1);\"";
    $arg[$mainName]["SEL_ADD"] = knjCreateBtn($objForm, "SEL_ADD", "＜", $extra);
    //削除
    $extra = "onclick=\"return move('right','{$leftName}','{$rightName}',1);\"";
    $arg[$mainName]["SEL_DEL"] = knjCreateBtn($objForm, "SEL_DEL", "＞", $extra);
    //全削除
    $extra = "onclick=\"return move('sel_del_all','{$leftName}','{$rightName}',1);\"";
    $arg[$mainName]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "SEL_DEL_ALL", "≫", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //コピーボタン
    $extra = "style=\"width:130px\" onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
    //更新ボタン(模試)
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //更新ボタン(科目)
    $extra = "onclick=\"return btn_submit('update2');\"";
    $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", $extra);
    //クリアボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
