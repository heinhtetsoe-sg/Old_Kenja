<?php

require_once('for_php7.php');

class knjh010a_disasterSubForm3
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjh010a_disasterindex.php", "", "sel");

        $row = knjh010a_disasterQuery::getSchregEnvirDat($model);

        $db = Query::dbCheckOut();

        //通学方法名称取得
        $arg["data"]["TUGAKU_NAME"] = $db->getOne(knjh010a_disasterQuery::getHowToCommutecdName());

        /******************/
        /* コンボボックス */
        /******************/

        //経路番号
        knjCreateHidden($objForm, "KEIRO_NO", 2);

        //バスコースコンボ
        $query = knjh010a_disasterQuery::getBusCourse();
        $extra = "onclick=\"selectRosen();\" class=\"eki_select_a\"";
        $arg["data"]["ROSEN_SELECT"] = makeCmb($objForm, $arg, $db, $query, "ROSEN_SELECT", $row["ROSEN_2"], $extra, 10, "BLANK");


        /********************/
        /* テキストボックス */
        /********************/
        //バスコース名--テキスト
        list($rosen_cd, $rosen_mei) = $db->getRow(knjh010a_disasterQuery::getBusCourse($row["ROSEN_2"]));
        $arg["data"]["ROSEN_TEXT"] = knjCreateTextBox($objForm, $rosen_mei, "ROSEN_TEXT", "", "", $extra);

        //乗車駅--テキスト
        $extra = "disabled=\"disabled\" class=\"eki_select_b\"";
        $arg["data"]["JOSYA_TEXT"] = knjCreateTextBox($objForm, $row["JOSYA_2"], "JOSYA_TEXT", "", "", $extra);

        //降車駅--テキスト
        $extra = "disabled=\"disabled\" class=\"eki_select_b\"";
        $arg["data"]["GESYA_TEXT"] = knjCreateTextBox($objForm, $row["GESYA_2"], "GESYA_TEXT", "", "", $extra);


        /**********/
        /* ボタン */
        /**********/
        //入力ボタン
        $extra = "onclick=\"insertDate()\"";
        $arg["btn_up"] = knjCreateBtn($objForm, 'btn_up', '入力', $extra);

        //閉じるボタン
        $extra = "onclick=\"alert('※まだ更新されていません');frame_name.closeit();\"";
        $arg["btn_close"] = knjCreateBtn($objForm, 'btn_reset', '閉じる', $extra);


        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");



        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh010a_disasterSubForm3.html", $arg);
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

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
