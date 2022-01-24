<?php

require_once('for_php7.php');

class knjz091a_3Rosen {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz091a_3index.php", "", "sel");

        $db = Query::dbCheckOut();

        /******************/
        /* コンボボックス */
        /******************/
        //路線番号コンボ
        for ($i = 1; $i <= 5; $i++) {
            $opt[$i] = array("label" => "路線" . ($i),
                             "value" => "{$i}");
        }
        $extra = "";
        $arg["data"]["KEIRO_NO"] = knjCreateCombo($objForm, "KEIRO_NO", "", $opt, $extra, 1);

        //エリア選択コンボ
        $query = knjz091a_3Query::getArea();
        $extra = "onChange=\"selectArea('get_rosen','rosen');\""; //引数は([コマンド]、[divタグのid])
        $arg["data"]["AREA_SENTAKU"] = makeCmb($objForm, $arg, $db, $query, "AREA_SENTAKU", $row["AREA_SENTAKU"], $extra, 1, "BLANK");

        if ($model->cmd == "get_rosen" || $model->cmd == "get_rosen_from_keiro") {
            //路線名--コンボ
            $query = knjz091a_3Query::getRosen($model->field["AREA_SENTAKU"]);
            $extra = " class=\"eki_select_a\"";
            echo makeCmb($objForm, $arg, $db, $query, "ROSEN_SELECT", "", $extra, "20", "");
            die();
        }

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
        View::toHTML($model, "knjz091a_3Rosen.html", $arg);
    }
}
/********************************************** 以下関数 **********************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $value, $extra, $size, $blank = "") {
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

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
