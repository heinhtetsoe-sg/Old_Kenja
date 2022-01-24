<?php

require_once('for_php7.php');


class knjz220aForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz220aindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //算出比率背景色
        $arg["BG_COLOR"] = "#00bfff";

        //校種コンボ作成
        $query = knjz220aQuery::getSchkind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, 1, 1);

        //教科コンボ作成
        $query = knjz220aQuery::getClass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, 1, 1);

        //科目コンボ作成
        $query = knjz220aQuery::getSubclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, 1, 1);

        //学期配列を作成
        $semesValList = array();
        $query = knjz220aQuery::getSemesterList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["semesTitleList"][]["LABEL"] = $row["LABEL"];

            $semesValList[] = $row["VALUE"];
        }

        //レイアウト設定用
        $arg["SEMES_COUNT"] = get_count($semesValList) > 0 ?get_count($semesValList) : 1;
        $lastSemes = $semesValList[get_count($semesValList) - 1];

        $arg["GORIOSI_WIDTH"] = get_count($semesValList) == 2 ? "320" : (get_count($semesValList) == 3 ? "211" : "0") ; //何学期あるかでで幅を変更する
        //初期化
        $model->data = array();
        $model->nameList = array();
        $tmpRow      = array();
        $extra = "";
        //一覧表示
        $result = $db->query(knjz220aQuery::selectQuery($model, $semesValList));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $tmpRow[$row["SUBCLASSCD"]]["SUBCLASSCD"]       = $row["SUBCLASSCD"];
            $tmpRow[$row["SUBCLASSCD"]]["SUBCLASSNAME"]     = $row["SUBCLASSNAME"];
            $tmpRow[$row["SUBCLASSCD"]]["SUBCLASSABBV"]     = $row["SUBCLASSABBV"];
            $extra = " onblur=\"this.value=calc(this);\" ";
            $formName = "RATE"."_".$row["SUBCLASSCD"]."_".$row["SEMESTER"];
            $model->nameList[] = $formName;
            $textBox = knjCreateTextBox($objForm, $row["RATE"], $formName, 10, 10, $extra);
            $lastFlg = ($row["SEMESTER"] == $lastSemes); //比率が最終学期のものかどうか
            $tmpRow[$row["SUBCLASSCD"]]["RATE_LIST"][]      = array("SEMESTER" => $row["SEMESTER"], "RATE" => $textBox, "LASTFLG" => $lastFlg, "NOTLASTFLG" => !$lastFlg);
        }
        $arg["data"] = array_values($tmpRow);
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz220aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", $all="")
{
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    if($all != "")   $opt[] = array('label' => "--全て--", 'value' => "ALL");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($value == "ALL" && $all != "") {
        $value = ($value == "ALL" && $all != "") ? "ALL": $value;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
