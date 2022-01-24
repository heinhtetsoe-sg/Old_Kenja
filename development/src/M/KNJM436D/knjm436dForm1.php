<?php

require_once('for_php7.php');

class knjm436dForm1 {
    function main(&$model) {
        $objForm = new form;

        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR;

        //出力順ラジオボタン(1:学籍番号順 2:クラス番号順)
        $opt = array(1, 2);
        $model->order = ($model->order == "") ? "1" : $model->order;
        $extra = array("id=\"ORDER1\""." onclick=\"btn_submit('change_order');\"", "id=\"ORDER2\""." onclick=\"btn_submit('change_order');\"");
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->order, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //科目リスト
        $query = knjm436dQuery::selectSubclassQuery($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $model->subclassCd, "SUBCLASSCD", $extra, 1, "BLANK");

        //初期化
        $model->data = array();
        $model->schCredit = array();

        //成績データのリスト
        $query = knjm436dQuery::GetRecordDatdata($model);
        $result  = $db->query($query);

        $setIndx = 1;
        $counts  = 1;       //ページ内での行数
        $colorFlg = false; //５行毎に背景色を変更
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            $creChk = ($row["GET_CREDIT"] != "") ? " checked": "";

            //評定
            $extra  = " style=\"text-align:right\" onblur=\"calc(this, '".$row["SCHREGNO"]."');\"";
            $extra .= " class=\"VALUE\" onkeydown=\"changeEnterToTab(this, {$setIndx})\"";
            $row["VALUE"] = knjCreateTextBox($objForm, $row["VALUE"], "VALUE-".$row["SCHREGNO"], 3, 1, $extra);

            //修得単位
            $extra  = " style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $extra .= " class=\"GET_CREDIT\" onkeydown=\"changeEnterToTab(this, {$setIndx})\"";
            $row["GET_CREDIT"] = knjCreateTextBox($objForm, $row["GET_CREDIT"], "GET_CREDIT-".$row["SCHREGNO"], 2, 2, $extra);

            //履修単位
            $extra  = " style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $extra .= " class=\"COMP_CREDIT\" onkeydown=\"changeEnterToTab(this, {$setIndx})\"";
            $row["COMP_CREDIT"] = knjCreateTextBox($objForm, $row["COMP_CREDIT"], "COMP_CREDIT-".$row["SCHREGNO"], 2, 2, $extra);

            //単位
            $model->schCredit[$row["SCHREGNO"]] = $row["CREDITS"];

            //５行毎に背景色を変更
            if ($counts % 5 == 1) {
                $colorFlg = !$colorFlg;
            }
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $arg["data"][] = $row;
            $counts++;
            $setIndx++;
        }
        $result->free();

        /************/
        /** ボタン **/
        /************/
        //更新
        $extra = $disabled ."onClick=\"btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onClick=\"btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm436dindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm436dForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
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
    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
