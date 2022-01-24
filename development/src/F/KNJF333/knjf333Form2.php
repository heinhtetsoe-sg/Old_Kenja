<?php

require_once('for_php7.php');

class knjf333form2 {
    function main(&$model) {
        $objForm = new form;
        //DB接続
        $db = Query::dbCheckOut();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf333index.php", "", "edit");

        $query = knjf333Query::getExistData($model);
        $hasDataCnt = $db->getOne($query);

        if (isset($model->schregno) && !isset($model->warning) && $model->cmd != 'qualifiedCd' && $model->cmd != 'conditionDiv') {
            if ($hasDataCnt > 0) {
                $query = knjf333Query::getSchInfoUpd($model);
            } else {
                $query = knjf333Query::getSchInfoAdd($model);
            }
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        $m1 = ($Row["HR_NAME"] && $Row["ATTENDNO"]) ? "-" : "";
        $m2 = ($Row["ATTENDNO"]) ? "番　" : "";
        $m3 = ($Row["AGE"] || $Row["SEX_NAME"]) ? "(" : "";
        $m4 = ($Row["AGE"]) ? "才" : "";
        $m5 = ($Row["AGE"] && $Row["SEX_NAME"]) ? "　" : "";
        $m6 = ($Row["AGE"] || $Row["SEX_NAME"]) ? ")" : "";
        $arg["data"]["SCH_INFO"] = $Row["HR_NAME"].$m1.$Row["ATTENDNO"].$m2.$Row["NAME"].$m3.$Row["AGE"].$m4.$m5.$Row["SEX_NAME"].$m6;

        //数値用
        $extraInt = "onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\" ";

        //病気
        $extra = "";
        $arg["data"]["DATA001_01"] = knjCreateTextBox($objForm, $Row["CDATA001_01"], "DATA001_01", 50, 150, $extra);

        //けが
        $extra = "";
        $arg["data"]["DATA001_02"] = knjCreateTextBox($objForm, $Row["CDATA001_02"], "DATA001_02", 50, 150, $extra);

        //欠席日数
        $extra = "";
        $arg["data"]["DATA002_01"] = knjCreateTextBox($objForm, $Row["IDATA002_01"], "DATA002_01", 3, 3, $extraInt);

        //継続区分
        $query = knjf333Query::getKoteiCmb(array("1" => "継続", "2" => "断続"));
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["IDATA002_02"], "DATA002_02", $extra, 1, "BLANK");

        //入院
        $query = knjf333Query::getKoteiCmb(array("1" => "有", "2" => "無"));
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["IDATA003_01"], "DATA003_01", $extra, 1, "BLANK");

        //保健室登校理由
        $query = knjf333Query::getKoteiCmb(array("1" => "病気", "2" => "けが", "3" => "その他"));
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["IDATA004_01"], "DATA004_01", $extra, 1, "BLANK");

        //医師の診断
        $query = knjf333Query::getKoteiCmb(array("1" => "有", "2" => "無"));
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["IDATA005_01"], "DATA005_01", $extra, 1, "BLANK");

        //保健室登校日数
        $extra = "";
        $arg["data"]["DATA006_01"] = knjCreateTextBox($objForm, $Row["IDATA006_01"], "DATA006_01", 3, 3, $extraInt);

        //継続区分
        $query = knjf333Query::getKoteiCmb(array("1" => "継続", "2" => "断続"));
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["IDATA006_02"], "DATA006_02", $extra, 1, "BLANK");

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリア
        $extra = "onclick=\"return Btn_reset('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        //回数
        knjCreateHidden($objForm, "SEQ", $Row["SEQ"]);
        //更新日
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
        //学籍番号
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        //年齢
        knjCreateHidden($objForm, "AGE", $Row["AGE"]);
        //性別
        knjCreateHidden($objForm, "SEX", $Row["SEX"]);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
                $arg["reload"]  = "window.open('knjf333index.php?cmd=list&SCHREGNO={$model->schregno}','right_frame');";
        }

        View::toHTML($model, "knjf333Form2.html", $arg);
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
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
