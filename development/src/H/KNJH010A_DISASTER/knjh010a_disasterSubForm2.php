<?php

require_once('for_php7.php');

class knjh010a_disasterSubForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjh010a_disasterindex.php", "", "sel");

        $row = knjh010a_disasterQuery::getSchregEnvirDat($model);

        $db = Query::dbCheckOut();

        //通学手段
        $opt_shubetsu = array(1, 2, 4); //1:電車通学 2:その他通学 4:バス通学
        $value = ($model->field["TUGAKU"]) ? $model->field["TUGAKU"] : "1";
        foreach ($opt_shubetsu as $key => $val) {
            $extra  = "id=\"TUGAKU{$val}\"";
            $extra .= " onclick=\"tugakuChange();\"";
            $extra .= ($value == $val) ? " checked" : "";
            $arg["data"]["TUGAKU".$val] = "<input type='radio' name='TUGAKU' value='{$val}' {$extra}>";
        }

        /******************/
        /* コンボボックス */
        /******************/
        //経路番号コンボ
        $opt[1] = array("label" => "自宅",
                        "value" => "1");
        for ($i = 2; $i <= 6; $i++) {
            $opt[$i] = array("label" => "経路" . ($i - 1),
                             "value" => "{$i}");
        }
        $opt[7] = array('label' => '学校',
                        'value' => '7');



        $extra = "onChange=\"selectKeiro();\"";
        $arg["data"][KEIRO_NO] = knjCreateCombo($objForm, "KEIRO_NO", "", $opt, $extra, 1);

        //エリア選択コンボ
        $query = knjh010a_disasterQuery::getArea();
        $extra = "onChange=\"selectArea('get_rosen','rosen');\""; //引数は([コマンド]、[divタグのid])
        $arg["data"][AREA_SENTAKU] = makeCmb($objForm, $arg, $db, $query, "AREA_SENTAKU", $row["AREA_SENTAKU"], $extra, 1, "BLANK");

        if ($model->cmd == "get_rosen" || $model->cmd == "get_rosen_from_keiro") {
            //路線名--コンボ
            $query = knjh010a_disasterQuery::getRosen($model->field["AREA_SENTAKU"]);
            $extra = " onclick=\"selectRosen('get_station');\" class=\"eki_select_a\"";
            echo makeCmb($objForm, $arg, $db, $query, "ROSEN_SELECT", "", $extra, "20", "BLANK");
            die();
        }

        if ($model->cmd == "get_station" || $model->cmd == "get_station_from_keiro") {
            if ($model->field["ROSEN_SELECT"]) {
                //乗車駅--コンボ
                $query = knjh010a_disasterQuery::getStation($model->field["ROSEN_SELECT"]);
                $extra = " onclick=\"selectStation('josya', this);\" class=\"eki_select_b\"";
                $response = makeCmb($objForm, $arg, $db, $query, "JOSYA_SELECT", "", $extra, "20", "BLANK");

                $query = knjh010a_disasterQuery::getStation($model->field["ROSEN_SELECT"]);
                $extra = " onclick=\"selectStation('gesya', this);\" class=\"eki_select_b\"";
                $response .= "::" . makeCmb($objForm, $arg, $db, $query, "GESYA_SELECT", "", $extra, "20", "BLANK");
                echo $response;
            }
            die();
        }


        /********************/
        /* テキストボックス */
        /********************/
        //路線名--テキスト
        $extra = "disabled=\"disabled\" class=\"eki_select_a\"";
        $arg["data"]["ROSEN_TEXT"] = knjCreateTextBox($objForm, $row["ROSEN_TEXT"], "ROSEN_TEXT", "", "", $extra);

        //乗車駅--テキスト
        $extra = "disabled=\"disabled\" class=\"eki_select_b\"";
        $arg["data"]["JOSYA_TEXT"] = knjCreateTextBox($objForm, $row["JOSYA_TEXT"], "JOSYA_TEXT", "", "", $extra);

        //下車駅--テキスト
        $extra = "disabled=\"disabled\" class=\"eki_select_b\"";
        $arg["data"]["GESYA_TEXT"] = knjCreateTextBox($objForm, $row["GESYA_TEXT"], "GESYA_TEXT", "", "", $extra);


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
        View::toHTML($model, "knjh010a_disasterSubForm2.html", $arg);
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
