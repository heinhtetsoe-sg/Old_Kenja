<?php

require_once('for_php7.php');

class knjc154Form1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjc154index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //月コンボ
        if ($model->field["MONTH"] == '') {
            $yearmonthday = explode('-', CTRL_DATE);
            $model->field["MONTH"] = $yearmonthday[1]; 
        }
        $opt = array();
        $value_flg = false;
        $query = knjc154Query::getNameMst("C044", "INT(VALUE(NAMESPARE1, '99'))");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $extra = "onchange=\"return btn_submit('edit');\"";
        $arg["data"]["MONTH"] = knjCreateCombo($objForm, "MONTH", $model->field["MONTH"], $opt, $extra, 1);

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knjc154Query::getTrainRow($model, $model->schregno),DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //通信欄
        $arg["data"]["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", 6, 35 * 2 + 1, "soft", $extra, $row["REMARK1"]);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model);

        if(get_count($model->warning)== 0 && $model->cmd !="clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif($model->cmd =="clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjc154Form1.html", $arg);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SCHREGNOS");
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {
    //更新
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : "disabled";
    $extra = $disable." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_update", "更 新", $extra);
    //更新後前後の生徒へ
    if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
        $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');
    } else {
        $extra = "disabled style=\"width:130px\"";
        $arg["button"]["btn_up_pre"] = createBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
        $arg["button"]["btn_up_next"] = createBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
    }
    //取消
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", $extra);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra) {
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

?>
