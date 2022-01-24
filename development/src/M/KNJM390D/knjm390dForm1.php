<?php

require_once('for_php7.php');

class knjm390dForm1 {
    function main(&$model) {
        $objForm = new form;

        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjm390dQuery::getSemecmb();
        $extra = "onChange=\"btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        //出力順ラジオボタン(1:学籍番号順 2:クラス番号順)
        $opt = array(1, 2);
        $model->order = ($model->order == "") ? "1" : $model->order;
        $extra = array("id=\"ORDER1\""." onclick=\"btn_submit('change_order');\"", "id=\"ORDER2\""." onclick=\"btn_submit('change_order');\"");
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->order, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //講座リスト
        $query = knjm390dQuery::getChrSubCd($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $model->chairCd, "CHAIRCD", $extra, 1, "BLANK");

        //スクーリング回数・最低出席回数
        $setRow = $db->getRow(knjm390dQuery::getChairCorresSemesDat($model), DB_FETCHMODE_ASSOC);
        $arg["SCHOOLING_MAX_CNT"]   = $setRow["SCHOOLING_MAX_CNT"];
        $arg["SCHOOLING_LIMIT_CNT"] = $setRow["SCHOOLING_LIMIT_CNT"];

        //初期化
        $model->data = array();

        //出席回数のリスト
        $query = knjm390dQuery::getSchAttendSemesDat($model);
        $result  = $db->query($query);
        $counts = 1;       //ページ内での行数
        $colorFlg = false; //５行毎に背景色を変更
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //得点、評定が入力済みの生徒は、readonly
            $readonly = ($row["INTO_CHECK"] != "") ? " readonly": "";

            //出席回数
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"".$readonly;
            $row["SCHOOLING_CNT"] = knjCreateTextBox($objForm, $row["SCHOOLING_CNT"], "SCHOOLING_CNT-".$row["SCHREGNO"], 2, 2, $extra);

            //５行毎に背景色を変更
            if ($counts % 5 == 1) {
                $colorFlg = !$colorFlg;
            }
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $arg["data"][] = $row;
            $counts++;
        }
        $result->free();

        //ボタン作成
        $extra = $disabled ."onClick=\"btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onClick=\"btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm390dindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm390dForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
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
