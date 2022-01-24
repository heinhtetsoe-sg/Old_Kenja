<?php

require_once('for_php7.php');

class knjl018aForm1
{
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl018aindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        //年度
        $arg['data']['YEAR'] = $model->year;

        //受験校種
        $query = knjl018aQuery::getNameMst($model->year, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //試験区分
        $query = knjl018aQuery::getTestdivMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //塾
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; onkeydown=\"changeEnterToTab(this)\" disabled=\"disabled\"";
        $arg["data"]["PRISCHOOLCD_DISP"] = knjCreateTextBox($objForm, $model->preischoolcd, "PRISCHOOLCD_DISP", 7, 7, $extra);
        //教室コード
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; onkeydown=\"changeEnterToTab(this)\" disabled=\"disabled\"";
        $arg["data"]["PRISCHOOL_CLASS_CD_DISP"] = knjCreateTextBox($objForm, $model->preischoolClassCd, "PRISCHOOL_CLASS_CD_DISP", 7, 7, $extra);
        //かな検索ボタン（塾）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=searchMain&pricd=PRISCHOOLCD_ID&priname=label_priName&priclasscd=PRISCHOOL_CLASS_CD_ID&priclassname=label_priClassName&priaddr=&submitFlg=1&prischool_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 20 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 280)\"";
        $arg["button"]["btn_pri_kana_reference"] = knjCreateBtn($objForm, "btn_pri_kana_reference", "検 索", $extra);
        
        //塾名
        $query = knjl018aQuery::getPriSchoolName($model->preischoolcd);
        $arg["data"]["PRISCHOOL_NAME"] = $db->getOne($query);
        //教室名
        $query = knjl018aQuery::getPriSchoolClassName($model->preischoolcd, $model->preischoolClassCd);
        $arg["data"]["PRISCHOOL_CLASS_NAME"] = $db->getOne($query);
        
        //ボタン作成
        makeBtn($objForm, $arg);
        //リスト作成
        makeListToList($objForm, $arg, $db, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl018aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //塾に通っている生徒
    $leftList = array();
    $left_schregno = array();

    if ($model->preischoolcd != "") {
        $query = knjl018aQuery::getSelectStudent($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            $left_schregno[] = $row["RECEPTNO"];
        }
        $result->free();
    }

    //生徒一覧
    $rightList = array();
    if ($model->preischoolcd != "") {
        $query = knjl018aQuery::getStudent($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["RECEPTNO"], $left_schregno)) {
                continue;
            }
            if (is_array($model->selected)) {
                if (!in_array($row["VALUE"], $model->selected)) {
                    $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
                }
            } else {
                $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            }
        }
        $result->free();
    }

    //クラス一覧作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CATEGORY_SELECTED_DATA");
    
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "PRISCHOOLCD",
                        "value"     => $model->preischoolcd,
                        "extrahtml" => " id=\"PRISCHOOLCD_ID\"" ));

    $objForm->ge($name);
    
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "PRISCHOOL_CLASS_CD",
                        "value"     => $model->preischoolClassCd,
                        "extrahtml" => " id=\"PRISCHOOL_CLASS_CD_ID\"" ));

    $objForm->ge($name);
    
    //knjCreateHidden($objForm, "PRISCHOOLCD", $model->preischoolcd);
    //knjCreateHidden($objForm, "PRISCHOOL_CLASS_CD", $model->preischoolClassCd);
}
?>
