<?php

require_once('for_php7.php');

class knjl002vSubPerfect
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl002vindex.php", "", "sel");

        $db = Query::dbCheckOut();

        //入試年度
        $arg["YEAR"] = $model->leftYear;

        //試験名
        $query = knjl002vQuery::getRow($model);
        $examRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //試験ID
        $arg["EXAM_ID"] = $model->examId." : ".$examRow["EXAM_NAME"];

        //科目コンボ
        $extra = " onchange=\"return btn_submit('perfectChange')\" ";
        $query = knjl002vQuery::getSubclassList($model);
        makeCmb($objForm, $arg, $db, $query, "EXAM_SUBCLASS", $model->examSubclass, $extra, 1);

        if ($model->cmd != "mantendivChange" && $model->cmd != "perfectKakutei") {
            $query = knjl002vQuery::selectPerfectMainQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row["STEPS"] == "0") {
                $model->mantendiv = "";
            } elseif ($row["STEPS"] == $model->mantenMark) {
                $model->mantendiv = "1";
            } else {
                $model->mantendiv = "2";
            }
        }

        //満点/評価コンボ
        $opt[] = array('label' => "", 'value' => "");
        $opt[] = array('label' => '満点', 'value' => '1');
        $opt[] = array('label' => '評価', 'value' => '2');
        $extra = " onchange=\"return btn_submit('mantendivChange')\" ";
        $arg["MANTENDIV"] = knjCreateCombo($objForm, "MANTENDIV", $model->mantendiv, $opt, $extra, 1);

        $arg["isManten"] = 0;
        $arg["isHyoka"] = 0;
        if ($model->mantendiv == "1") {
            $arg["isManten"] = 1;
        } elseif ($model->mantendiv == "2") {
            $arg["isHyoka"] = 1;
        }

        //評価段階数
        if (($model->cmd == "perfect" || $model->cmd == "perfectChange" || $model->cmd == "perfectReset")
            && $model->mantendiv == "2") {
            $levelCnt = $db->getOne(knjl002vQuery::selectPerfectMainQuery($model, "cnt"));
            $model->maxLevel = $levelCnt;
        }
        if ($model->maxLevel == "") {
            $model->maxLevel = 0;
        }
        $extra = "style=\"text-align:right\" onblur=\"level(this, '{$model->maxLevel}');\"";
        $arg["MAX_POINTLEVEL"] = knjCreateTextBox($objForm, $model->maxLevel, "MAX_POINTLEVEL", 3, 3, $extra);
        knjCreateHidden($objForm, "HID_MAX_POINTLEVEL", $model->maxLevel); //エラーになった場合に入力前の値に戻すためのhidden

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $query = knjl002vQuery::selectPerfectMainQuery($model);
            $result = $db->query($query);

            $mainData = array();
            if ($model->cmd != "perfectKakutei") { //※確定後は指定の段階数分の入力欄を未入力状態で出すのでデータは空
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $mainData[$row["STEPS"]] = $row;
                }
            }
        } else {
            $mainData =& $model->dataPerfectField;
            $result = "";
        }

        if ($model->mantendiv == "1") {
            //値
            $extra = "id=\"VALUE1\" style=\"text-align:center\" onblur=\"this.value=toInteger(this.value);\"";
            $mainData[$model->mantenMark]["VALUE"] = knjCreateTextBox($objForm, $mainData[$model->mantenMark]["VALUE"], "VALUE1", 3, 3, $extra);

            $arg["data"][] = $mainData[$model->mantenMark];
        } else {
            //確定後は指定の段階数分の入力欄を未入力状態で出す
            if ($model->cmd == "perfectKakutei") {
                $mainData = array();
            }
            //設定段階値
            $model->maxLevel = ($model->maxLevel != "") ? $model->maxLevel : 0;

            for ($i = 1; $i <= $model->maxLevel; $i++) {
                //段階値
                $mainData[$i]["STEPS"] = $i;
                knjCreateHidden($objForm, "STEPS".$i, $i); //更新ボタン押し時に値を格納
                //表示
                $extra = "id=\"LABEL{$i}\" style=\"text-align:center\"";
                $mainData[$i]["LABEL"] = knjCreateTextBox($objForm, $mainData[$i]["LABEL"], "LABEL".$i, 2, 2, $extra);
                //値
                $extra = "id=\"VALUE{$i}\" style=\"text-align:center\" onblur=\"this.value=toInteger(this.value);\"";
                $mainData[$i]["VALUE"] = knjCreateTextBox($objForm, $mainData[$i]["VALUE"], "VALUE".$i, 3, 3, $extra);

                $arg["data"][] = $mainData[$i];
            }
        }

        //更新ボタン
        $disabled = ($model->mantendiv == "") ? "disabled " : "";
        $extra = $disabled."onclick=\"return btn_submit('perfectUpd')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消
        $extra = "onclick=\"return btn_submit('perfectReset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL002V/knjl002vindex.php?cmd=back&ini2=1";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"return btn_submit('perfectKakutei');\"";
        $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確定", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "notKakuteiFlg", "0"); //評価段階数を変更するとフラグが立つ、段階数を確定する前に更新を防ぐために使用

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl002vSubPerfect.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank != "") {
        $opt[] = array('label' => "", 'value' => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
