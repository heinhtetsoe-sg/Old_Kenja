<?php

require_once('for_php7.php');

class knjh185Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh185Form1", "POST", "knjh185index.php", "", "knjh185Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //背景色変更
        $bgcolor = "lightpink";
        $arg["bgcolor"] = $bgcolor;
        $arg["jscript"] = "change_bgcolor(3, '{$bgcolor}');";

        //対象幼稚園コンボ
        $query = knjh185Query::getKindergarten();
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $model->field["COURSECODE"], $extra, 1);

        //年組コンボ
        $query = knjh185Query::getHrClass($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //預かり日付初期値セット
        if ($model->field["CARE_DATE"] == "") $model->field["CARE_DATE"] = str_replace("-", "/", CTRL_DATE);
        //預かり日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('change', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["CARE_DATE"], "CARE_DATE", 12, 12, $extra);
        //預かり日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjh185', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=CARE_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['CARE_DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //預かり日付
        $arg["data"]["CARE_DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //預かり費用コードをキーでソート
        ksort($model->fare_array);

        //預かり費用表示用
        $fare_len_max = strlen(number_format(max($model->fare_array)));
        $fare_show = $opt = $extra = array();
        foreach ($model->fare_array as $key => $val) {
            $fare_show[$key] = "&yen;";
            for ($i=0; $i < ($fare_len_max - strlen(number_format($val))); $i++) $fare_show[$key] .= "&nbsp;";
            $fare_show[$key] .= number_format($val);
            knjCreateHidden($objForm, "FARE_SHOW-".$key, $fare_show[$key]);

            //ラジオボタン用
            $opt[] = $key;
            $extra[$key] = "id=\"FARE_CD{$key}\"";
        }

        //預かり費用ラジオボタン
        if ($model->field["FARE_CD"] == "") $model->field["FARE_CD"] = $opt[0];
        $radio = $sep = "";
        $cnt = 0;
        foreach ($model->fare_array as $key => $val) {

            $objForm->ae( array("type"      => "radio",
                                "name"      => "FARE_CD",
                                "value"     => $model->field["FARE_CD"],
                                "extrahtml" => $extra[$key],
                                "multiple"  => $opt));

            $radio .= $sep;
            $radio .= ($cnt != 0 && $cnt%3 == 0) ? "<br>" : "";
            $radio .= $objForm->ge("FARE_CD", $key);
            $radio .= "<LABEL for=\"FARE_CD{$key}\">".number_format($val)."円</LABEL>";

            $sep = "&nbsp;&nbsp;";
            $cnt++;
        }
        $arg["data"]["FARE_CD"] = $radio;

        //氏名のMAX文字数取得
        $max_len = 0;
        $query = knjh185Query::getRightStudent($model, "max");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $zenkaku = (strlen($row["NAME_SHOW"]) - mb_strlen($row["NAME_SHOW"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["NAME_SHOW"]) - $zenkaku : mb_strlen($row["NAME_SHOW"]);
            $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $max_len, $bgcolor, $fare_show);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh185Form1.html", $arg);
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $max_len, $bgcolor, $fare_show) {
    $opt_right = $opt_left = array();

    //左リスト
    if (!isset($model->warning)) {
        $query = knjh185Query::getLeftStudent($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //印字調整（氏名）
            $zenkaku = (strlen($row["NAME_SHOW"]) - mb_strlen($row["NAME_SHOW"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["NAME_SHOW"]) - $zenkaku : mb_strlen($row["NAME_SHOW"]);
            $len = $zenkaku * 2 + $hankaku;
            $sch_name = $row["NAME_SHOW"];
            for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $sch_name .= "&nbsp;";

            $opt_left[] = array('label' => $row["ATTENDNO"]."番　".$sch_name." : (".$fare_show[$row["FARE_CD"]].") ".$row["BUS_COURSE"],
                                'value' => $row["SCHREGNO"].'-'.$row["ATTENDNO"].'-'.$row["FARE_CD"].'-0-L');
        }
        $result->free();

    } else {
        if ($model->selectdataL) {
            $selectdataL = explode(',', $model->selectdataL);
            for ($i=0; $i < get_count($selectdataL); $i++) {
                //データ取得
                $query = knjh185Query::getRightStudent($model, "info", $selectdataL[$i]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

                //印字調整（氏名）
                $zenkaku = (strlen($row["NAME_SHOW"]) - mb_strlen($row["NAME_SHOW"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["NAME_SHOW"]) - $zenkaku : mb_strlen($row["NAME_SHOW"]);
                $len = $zenkaku * 2 + $hankaku;
                $sch_name = $row["NAME_SHOW"];
                for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $sch_name .= "&nbsp;";

                $opt_left[] = array('label' => $row["ATTENDNO"]."番　".$sch_name." : (".$fare_show[$row["FARE_CD"]].") ".$row["BUS_COURSE"],
                                    'value' => $row["SCHREGNO"].'-'.$row["ATTENDNO"].'-'.$row["FARE_CD"].'-'.$row["CHANGEFLG"].'-'.$row["ORIGINAL"]);
            }
        }
    }

    //右リスト
    if (!isset($model->warning)) {
        $query = knjh185Query::getRightStudent($model, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //印字調整（氏名）
            $zenkaku = (strlen($row["NAME_SHOW"]) - mb_strlen($row["NAME_SHOW"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["NAME_SHOW"]) - $zenkaku : mb_strlen($row["NAME_SHOW"]);
            $len = $zenkaku * 2 + $hankaku;
            $sch_name = $row["NAME_SHOW"];
            for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $sch_name .= "&nbsp;";

            $opt_right[] = array('label' => $row["ATTENDNO"]."番　".$sch_name." : ".$row["BUS_COURSE"],
                                 'value' => $row["SCHREGNO"].'-'.$row["ATTENDNO"].'-'.$row["FARE_CD"].'-'.$row["CHANGEFLG"].'-'.$row["ORIGINAL"]);
        }
        $result->free();

    } else {
        if ($model->selectdataR) {
            $selectdataR = explode(',', $model->selectdataR);
            for ($i=0; $i < get_count($selectdataR); $i++) {
                //データ取得
                $query = knjh185Query::getRightStudent($model, "info", $selectdataR[$i]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

                //印字調整（氏名）
                $zenkaku = (strlen($row["NAME_SHOW"]) - mb_strlen($row["NAME_SHOW"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["NAME_SHOW"]) - $zenkaku : mb_strlen($row["NAME_SHOW"]);
                $len = $zenkaku * 2 + $hankaku;
                $sch_name = $row["NAME_SHOW"];
                for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $sch_name .= "&nbsp;";

                $opt_right[] = array('label' => $row["ATTENDNO"]."番　".$sch_name." : ".$row["BUS_COURSE"],
                                     'value' => $row["SCHREGNO"].'-'.$row["ATTENDNO"].'-'.$row["FARE_CD"].'-'.$row["CHANGEFLG"].'-'.$row["ORIGINAL"]);
            }
        }
    }

    //生徒一覧リスト
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left', '{$bgcolor}')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);
    //出力対象一覧リスト
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', '{$bgcolor}')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', '{$bgcolor}');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', '{$bgcolor}');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', '{$bgcolor}');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', '{$bgcolor}');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

    //人数表示
    $arg["data"]["CATEGORY_NAME_COUNT"] = "<span id='CATEGORY_NAME'>".get_count($opt_right)."</span>";
    $arg["data"]["CATEGORY_SELECTED_COUNT"] = "<span id='CATEGORY_SELECTED'>".get_count($opt_left)."</span>";
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdataL");
    knjCreateHidden($objForm, "selectdataR");
}
?>
