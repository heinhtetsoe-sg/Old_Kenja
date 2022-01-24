<?php

require_once('for_php7.php');
class knjh010aSubForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjh010aindex.php", "", "sel");

        $row = knjh010aQuery::getSchregEnvirDat($model);

        $db = Query::dbCheckOut();
        //読書傾向
        $extra = '';
        $arg["data"]["READING"] = knjCreateTextArea($objForm, "READING", 2, 20, "hard", $extra, $row["READING"]);

        //性質 長所
        $extra = '';
        $arg["data"]["MERITS"] = knjCreateTextArea($objForm, "MERITS", 2, 20, "hard", $extra, $row["MERITS"]);

        //性質 短所
        $extra = '';
        $arg["data"]["DEMERITS"] = knjCreateTextArea($objForm, "DEMERITS", 2, 20, "hard", $extra, $row["DEMERITS"]);

        //学業 得意科目
        $extra = '';
        $arg["data"]["GOOD_SUBJECT"] = knjCreateTextArea($objForm, "GOOD_SUBJECT", 2, 20, "hard", $extra, $row["GOOD_SUBJECT"]);

        //学業 不得意科目
        $extra = '';
        $arg["data"]["BAD_SUBJECT"] = knjCreateTextArea($objForm, "BAD_SUBJECT", 2, 20, "hard", $extra, $row["BAD_SUBJECT"]);

        //趣味・娯楽
        $extra = '';
        $arg["data"]["HOBBY"] = knjCreateTextArea($objForm, "HOBBY", 2, 20, "hard", $extra, $row["HOBBY"]);

        //要注意事項
        $extra = "";
        $arg["data"]["ATTENTIONMATTERS"] = knjCreateTextBox($objForm, $row["ATTENTIONMATTERS"], "ATTENTIONMATTERS", 20, 10, $extra);

        //既往の疾患
        $extra = "";
        $arg["data"]["DISEASE"] = knjCreateTextBox($objForm, $row["DISEASE"], "DISEASE", 20, 10, $extra);

        //現在の健康状態
        $extra = "";
        $arg["data"]["HEALTHCONDITION"] = knjCreateTextBox($objForm, $row["HEALTHCONDITION"], "HEALTHCONDITION", 20, 10, $extra);

        //こづかい(コンボ)
        $query = knjh010aQuery::getVNameMst("H104", $model);
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "POCKETMONEYCD", $row["POCKETMONEYCD"], $extra, 1, "0");

        //こづかい
        $extra = "onBlur=\"return to_Integer(this);\"";
        $arg["data"]["POCKETMONEY"] = knjCreateTextBox($objForm, $row["POCKETMONEY"], "POCKETMONEY", 4, 4, $extra);

        //睡眠 就寝時間
        $extra = "onBlur=\" return onBlur=to_Integer(this);\"";
        $arg["data"]["BEDTIME_HOURS"] = knjCreateTextBox($objForm, $row["BEDTIME_HOURS"], "BEDTIME_HOURS", 2, 2, $extra);

        //睡眠 就寝分
        $extra = "onBlur=\" return onBlur=to_Integer(this);\"";
        $arg["data"]["BEDTIME_MINUTES"] = knjCreateTextBox($objForm, $row["BEDTIME_MINUTES"], "BEDTIME_MINUTES", 2, 2, $extra);

        //睡眠 起床時間
        $extra = "onBlur=\"return onBlur=to_Integer(this);\"";
        $arg["data"]["RISINGTIME_HOURS"] = knjCreateTextBox($objForm, $row["RISINGTIME_HOURS"], "RISINGTIME_HOURS", 2, 2, $extra);

        //睡眠 起床時間
        $extra = "onBlur=\"return onBlur=to_Integer(this);\"";
        $arg["data"]["RISINGTIME_MINUTES"] = knjCreateTextBox($objForm, $row["RISINGTIME_MINUTES"], "RISINGTIME_MINUTES", 2, 2, $extra);

        //テレビの視聴時間(コンボ)
        $query = knjh010aQuery::getVNameMst("H105", $model);
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "TVVIEWINGHOURSCD", $row["TVVIEWINGHOURSCD"], $extra, 1, "0");

        //主に見るテレビ
        $extra = "";
        $arg["data"]["TVPROGRAM"] = knjCreateTextBox($objForm, $row["TVPROGRAM"], "TVPROGRAM", 20, 10, $extra);

        //パソコン時間(コンボ)
        $query = knjh010aQuery::getVNameMst("H105", $model);
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "PC_HOURS", $row["PC_HOURS"], $extra, 1, "0");

        Query::dbCheckIn($db);


        /**********/
        /* ボタン */
        /**********/

        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update2');\"";
        $arg["btn_up"] = knjCreateBtn($objForm, 'btn_up', '更新', $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('reset2');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取消', $extra);

        //プログラム判定
        if (VARS::get("PRG") == "KNJH160A") {
            $model->prg = "KNJH160A";
        }

        //戻るボタン
        if ($model->prg == "KNJH160A") {
            $link = REQUESTROOT."/H/KNJH010A/knjh010aindex.php?cmd=back2&ini2=1&PRG=KNJH160A";
            $model->prg = "KNJH160A";
        } else {
            $link = REQUESTROOT."/H/KNJH010A/knjh010aindex.php?cmd=back&ini2=1";
        }
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻る', $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh010aSubForm1.html", $arg);
    }
}
/********************************************** 以下関数 **********************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
    } elseif ($blank == "0") {
        $opt[] = array('label' => "",
                       'value' => "0");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
