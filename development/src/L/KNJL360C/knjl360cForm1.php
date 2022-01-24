<?php

require_once('for_php7.php');


class knjl360cForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl360cForm1", "POST", "knjl360cindex.php", "", "knjl360cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //extra
        $extra = "onchange=\"OptionUse('this');\"";

        //入試制度コンボの設定
        $query = knjl360cQuery::getApctDiv("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボの設定
        $query = knjl360cQuery::getTestDiv("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //出力選択ラジオボタン 1:合格者全員 2:受験者全員 3:受験者指定 4:志願者全員
        $opt_print = array(1, 2, 3, 4);
        $model->field["PRINT_TYPE"] = ($model->field["PRINT_TYPE"] == "") ? "1" : $model->field["PRINT_TYPE"];
        $disabled = ($model->field["APPLICANTDIV"] == '1' && $model->field["TESTDIV"] == '6') ? " disabled" : "";
        $click = " onClick=\"return btn_submit('knjl360c');\"";
        $extra = array("id=\"PRINT_TYPE1\"".$click, "id=\"PRINT_TYPE2\"".$click.$disabled, "id=\"PRINT_TYPE3\"".$click.$disabled, "id=\"PRINT_TYPE4\"".$click.$disabled);
        $radioArray = knjCreateRadio($objForm, "PRINT_TYPE", $model->field["PRINT_TYPE"], $extra, $opt_print, get_count($opt_print));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //合格者チェックボックス 初期値チェック有
        if ($model->cmd == "") {
            $model->field["GOUKAKUSHA"] = "1";
        }
        if ($model->field["GOUKAKUSHA"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["GOUKAKUSHA"] = knjCreateCheckBox($objForm, "GOUKAKUSHA", "1", $extra);

        //受験番号テキストボックス
        $value = ($model->field["EXAMNO"]) ? $model->field["EXAMNO"] : "";
        if($model->field["PRINT_TYPE"] == "3"){
            $extra = " STYLE=\"text-align: right\"; onBlur=\"this.value=toInteger(this.value);\"";
        } else {
            $extra = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $value, "EXAMNO", 5, 5, $extra);

        //入学金なしcheckbox
        if ($model->isGojou) {
            $arg["gojo"] = "1";
        } else if ($model->isCollege) {
            $arg["isCollege"] = "1";
        } else {
            $arg["wakayama"] = "1";
        }
        
        $disabled = "";
        if ($model->isGojou && $model->field["APPLICANTDIV"] == "1") {
            $disabled = " disabled ";
        }
        if ($model->isGojou) {
            $arg["data"]["CHECK_NAME"]  = "入学金内金あり";
            $arg["data"]["CHECK_NAME1"] = "入学金/入学金内金";
            $arg["data"]["CHECK_NAME2"] = "制定学用品代";
        } else if ($model->isCollege) {
            $arg["data"]["CHECK_NAME"]  = "入学金なし";
            $arg["data"]["CHECK_NAME1"] = "入学金";
            $arg["data"]["CHECK_NAME2"] = "制定品";
        } else {
            $arg["data"]["CHECK_NAME"] = "入学金なし";
        }

        //カレッジは初期値チェックなし
        if ($model->isCollege) {
            $extra  = ($model->field["CHECK"] == "on") ? "checked" : "";
        } else {
            $extra  = ($model->field["CHECK"] == "on" || $model->cmd == "" || $model->cmd == 'knjl360c') ? "checked" : "";
        }
        $extra .= " id=\"CHECK\"";
        $arg["data"]["CHECK"] = knjCreateCheckBox($objForm, "CHECK", "on", $extra.$disabled);
        
        //入学手続き日、取扱い期間の設定        
        $value1 = isset($model->field["ENT_DATE"])?$model->field["ENT_DATE"]:$model->control["学籍処理日"];
        $value2 = isset($model->field["STR_DATE"])?$model->field["STR_DATE"]:$model->control["学籍処理日"];
        $arg["data"]["ENT_DATE"] = View::popUpCalendar($objForm,"ENT_DATE",$value1);
        $arg["data"]["STR_DATE"] = View::popUpCalendar($objForm,"STR_DATE",$value2);
        //カレッジは不要
        if (!$model->isCollege) {
            //2日後の設定（メニューから起動時）
            $value3 = isset($model->field["END_DATE"])?$model->field["END_DATE"]:$model->control["学籍処理日"];
            if ($model->cmd == "") {
                list($year, $month, $day) = preg_split("/\//",$value2);
                $value3 = date("Y/m/d", mktime(0,0,0,$month,$day+2,$year));
            }
            $arg["data"]["END_DATE"] = View::popUpCalendar($objForm,"END_DATE",$value3);
        }
        
        //時間
        $extra = " onChange=\"this.value = toInteger(this.value); ckTime(this);\"";
        $arg["data"]["PRINT_TIME"] = knjCreateTextBox($objForm, $model->field["PRINT_TIME"], "PRINT_TIME", 2, 2, $extra);
        
        //五条の制定学用品代の設定
        if ($model->isGojou || $model->isCollege) {
            //取扱い期間の設定        
            $value4 = isset($model->field["STR_DATE2"])?$model->field["STR_DATE2"]:$model->control["学籍処理日"];
            $arg["data"]["STR_DATE2"] = View::popUpCalendar($objForm,"STR_DATE2",$value4);
            //カレッジは不要
            if (!$model->isCollege) {
                //2日後の設定（メニューから起動時）
                $value5 = isset($model->field["END_DATE2"])?$model->field["END_DATE2"]:$model->control["学籍処理日"];
                if ($model->cmd == "") {
                    list($year, $month, $day) = preg_split("/\//",$value4);
                    $value5 = date("Y/m/d", mktime(0,0,0,$month,$day+2,$year));
                }
                $arg["data"]["END_DATE2"] = View::popUpCalendar($objForm,"END_DATE2",$value5);
            }
            //時間(五条の制定学用品代用)
            $extra = " onChange=\"this.value = toInteger(this.value); ckTime(this);\"";
            $arg["data"]["PRINT_TIME2"] = knjCreateTextBox($objForm, $model->field["PRINT_TIME2"], "PRINT_TIME2", 2, 2, $extra);
        }
        
        //男女選択ラジオボタン 1:男子 2:女子
        $opt_sex = array(1, 2);
        $model->field["SEX"] = ($model->field["SEX"] == "") ? "1" : $model->field["SEX"];
        $extra = array("id=\"SEX1\"", "id=\"SEX2\"");
        $radioArray = knjCreateRadio($objForm, "SEX", $model->field["SEX"], $extra, $opt_sex, get_count($opt_sex));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl360cForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
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
    $value = (($value && $value_flg) || $value == '9') ? $value : $opt[$default]["value"];

    if($name == "TESTDIV"){
        $opt[]= array("label" => "-- 全て --", "value" => "9");
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL360C");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CHECK_EXAMNO", $model->field["PRINT_TYPE"]);
    knjCreateHidden($objForm, "CHECK_GOJO", $model->isGojou ? 1 : 0);
    knjCreateHidden($objForm, "IS_COLLEGE", $model->isCollege ? 1 : 0);

}
?>
