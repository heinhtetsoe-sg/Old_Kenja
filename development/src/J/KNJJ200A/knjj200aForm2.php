<?php

require_once('for_php7.php');

class knjj200aForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjj200aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && isset($model->field["DIV"]) && isset($model->field["EXECUTIVECD"]) && !isset($model->warning) && $model->cmd != "div") {
            //対象レコードを取得
            $Row = $db->getRow(knjj200aQuery::getRow($model->schregno, $model->field["DIV"], $model->field["EXECUTIVECD"]), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //年組
        $ghr = $db->getRow(knjj200aQuery::getHrName($model), DB_FETCHMODE_ASSOC);
        $arg["data"]["HR_NAME"] = $ghr["HR_NAME"];

        //区分
        $extra = "onchange=\"return btn_submit('div');\"";
        $query = knjj200aQuery::getNameMst('J006');
        makeCmb($objForm, $arg, $db, $query, "DIV", $Row["DIV"], $extra, 1, "BLANK");

        //役職名
        $query = knjj200aQuery::getExecutivecd($Row["DIV"]);
        makeCmb($objForm, $arg, $db, $query, "EXECUTIVECD", $Row["EXECUTIVECD"], "", 1, "BLANK");

        //保護者
        $opt = array(1, 2);
        $Row["GUARD_NAME_DIV"] = ($Row["GUARD_NAME_DIV"] == "") ? "1" : $Row["GUARD_NAME_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"GUARD_NAME_DIV{$val}\"");
            //保護者氏名を取得
            $guardName = $db->getOne(knjj200aQuery::getGuardName($model, $val));
            $arg["data"]["GUARD_NAME{$val}"] = ($guardName) ? $guardName : "";
        }
        $radioArray = knjCreateRadio($objForm, "GUARD_NAME_DIV", $Row["GUARD_NAME_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //備考テキスト作成
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 40, 40, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "div" && !isset($model->warning)) {
            $arg["reload"] = "window.open('knjj200aindex.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj200aForm2.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $BLANK="")
{
    $opt = array();
    $value_flg = false;
    if($BLANK) $opt[] = array('label' => "", 'value' => "");
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
    //権限チェック
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";
    //修正ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);
    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disable);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
