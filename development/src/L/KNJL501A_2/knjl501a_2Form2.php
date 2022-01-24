<?php

require_once('for_php7.php');

class knjl501a_2Form2 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl501a_2index.php", "", "edit");

        $db  = Query::dbCheckOut();

        if (!isset($model->warning)) {
            $query = knjl501a_2Query::getHopeCourseDat($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //志望区分コード
        $extra = "";
        $arg["data"]["HOPE_COURSECODE"] = knjCreateTextBox($objForm, $Row["HOPE_COURSECODE"], "HOPE_COURSECODE", 4, 4, $extra);
        knjCreateHidden($objForm, "DISP_HOPECOURSECODE", $Row["HOPE_COURSECODE"]);

        $useYearCnt = $db->getOne(knjl501a_2Query::getUseYearHopeCourseYDat($model));
        
        //志望区分名称
        $extra = "";
        $arg["data"]["HOPE_NAME"] = knjCreateTextBox($objForm, $Row["HOPE_NAME"], "HOPE_NAME", 40, 20, $extra);

        //学籍コースコード＆名称
        $query = knjl501a_2Query::getCourseCodeList($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["COURSECODE"], "COURSECODE", $extra, 1, "BLANK");

        //通知用名称
        $extra = "";
        $arg["data"]["NOTICE_NAME"] = knjCreateTextBox($objForm, $Row["NOTICE_NAME"], "NOTICE_NAME", 40, 20, $extra);

        //コース合格名称
        $extra = "";
        $arg["data"]["PASS_NAME"] = knjCreateTextBox($objForm, $Row["PASS_NAME"], "PASS_NAME", 40, 20, $extra);

        //コース不合格名称
        $extra = "";
        $arg["data"]["NOT_PASS_NAME"] = knjCreateTextBox($objForm, $Row["NOT_PASS_NAME"], "NOT_PASS_NAME", 40, 20, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $dirstr = substr($model->getPrgId, 3,1);
        $prglowstr = strtolower($model->getPrgId);
        $link = REQUESTROOT."/{$dirstr}/{$model->getPrgId}/{$prglowstr}index.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_USEYEAR_CNT", $useYearCnt);

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        $arg["reload"]  = "";
        $arg["reload"]  = "window.open('knjl501a_2index.php?cmd=list&ed=1','left_frame');";
        View::toHTML($model, "knjl501a_2Form2.html", $arg);
    }
}
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
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
