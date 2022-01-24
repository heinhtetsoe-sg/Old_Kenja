<?php

require_once('for_php7.php');

class knjb0210Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjb0210Form1", "POST", "knjb0210index.php", "", "knjb0210Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTER;

        // データ区分ラジオボタン
        // 1:履修条件 2:必履修条件
        $opt_data = array(1, 2);
        $model->data_div = ($model->data_div == "") ? "2" : $model->data_div;
        $extra = array();
        foreach($opt_data as $key => $val) {
            $link = $val == "1" ? REQUESTROOT."/B/KNJB0200/knjb0200index.php?mode=1" : REQUESTROOT."/B/KNJB0210/knjb0210index.php?";
            array_push($extra, " id=\"DATA_DIV{$val}\" onClick=\"document.location.href='$link'\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATA_DIV", $model->data_div, $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学年
        $query = knjb0210Query::getGrade($model);
        $extra = " onChange=\"btn_submit('knjb0210')\"";
        makeCmb($objForm, $arg, $db, $query, $model->setGrade, "SET_GRADE", $extra, 1, "BLANK");

        //校種
        $model->schoolKind = "";
        $query = knjb0210Query::getSchoolKind($model);
        $model->schoolKind = $db->getOne($query);

        //設定グループ
        $query = knjb0210Query::getGroup($model);
        $extra = " onChange=\"btn_submit('knjb0210')\"";
        makeCmb($objForm, $arg, $db, $query, $model->setGroupCd, "SET_GROUP_CD", $extra, 1, "BLANK");

        //条件取得
        $joukenAri = false;
        if ($model->cmd == 'knjb0210' || $model->cmd == 'read') {
            $query = knjb0210Query::getSubclassName($model);
            $result = $db->query($query);
            $model->subclassArray = array();
            $sep = "";
            $model->selectleft = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->subclassArray[$row["SUBCLASSCD"]] = $row["SUBCLASSNAME"];
                $model->selectleft .= $sep.$row["SUBCLASSCD"];
                $sep = ",";

            }
            $result->free();

            $query = knjb0210Query::getJouken($model);
            $result = $db->query($query);
            $model->joukenField = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                foreach ($model->subclassArray as $subclassCd => $name) {
                    $row["CONDITION"] = str_replace("'".$subclassCd."'", $name, $row["CONDITION"]);
                }
                $model->field["JOUKEN".$row["CONDITION_SEQ"]] = $row["CONDITION"];
                $joukenAri = $row["CONDITION"] ? true : $joukenAri;
                $model->joukenField["JOUKEN{$row["CONDITION_SEQ"]}_HIDEEN"] = $row["CONDITION_NUM"];
                $model->joukenField["UPD{$row["CONDITION_SEQ"]}_HIDEEN"] = $row["CONDITION"];
            }
            $result->free();
        }

        knjCreateHidden($objForm, "joukenCnt", $model->joukenCnt);

        $readOnley = "";
        if ($joukenAri) {
            $readOnley = " readOnly style=\"background-color:#CCCCCC;\" ";
        }
        for ($i = 1; $i <= $model->joukenCnt; $i++) {
            //条件
            $extra = "class=\"eisuFuka\" id=\"JOUKEN{$i}\" onMouseUp=\" document.forms[0].TEXTNAME.value = 'JOUKEN{$i}';\"";
            $arg["data"]["JOUKEN{$i}"] = knjCreateTextBox($objForm, $model->field["JOUKEN{$i}"], "JOUKEN{$i}", 115, 200, $extra.$readOnley);
            //hidden
            knjCreateHidden($objForm, "JOUKEN{$i}_HIDEEN", $model->joukenField["JOUKEN{$i}_HIDEEN"]);
            //hidden
            knjCreateHidden($objForm, "UPD{$i}_HIDEEN", $model->joukenField["UPD{$i}_HIDEEN"]);
        }

        //対象者リストを作成する
        $opt1 = array();
        $opt_left = array();
        $selectleft = explode(",", $model->selectleft);
        $query = knjb0210Query::getSubclass($model);

        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->select_opt[$row["VALUE"]] = array('label' => $row["LABEL"], 
                                                      'value' => $row["VALUE"]);
                if (!in_array($row["VALUE"], $selectleft)) {
                    $opt1[]= array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
                }
        }
        $result->free();

        //左リストで選択されたものを再セット
        $leftCnt = 1;
        foreach ($selectleft as $key => $val) {
            if (is_array($model->select_opt[$val])) {
                $model->select_opt[$val]["label"] = "【".$leftCnt."】".$model->select_opt[$val]["label"];
                $opt_left[] = $model->select_opt[$val];
                $leftCnt++;
            }
        }

        //一覧
        $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", $value, isset($opt1)? $opt1 : array(), $extra, 12);

        //選択
        $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", $value, $opt_left, $extra, 12);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //ボタンを作成する
        makeBtn($objForm, $arg, $model, $joukenAri);

        //hiddenを作成する(必須)
        makeHidden($objForm, $arg);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML2($model, "knjb0210Form1.html", $arg); 
    }
}

function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $joukenAri) {
    //前年度からコピー
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
    //ANDボタン
    $extra = "onClick=\"btn_submit('setAnd');\"";
    $arg["button"]["btn_and"] = knjCreateBtn($objForm, "btn_and", "AND", $extra);
    //ORボタン
    $extra = "onClick=\"btn_submit('setOr');\"";
    $arg["button"]["btn_or"] = knjCreateBtn($objForm, "btn_or", "OR", $extra);
    //()ボタン
    $extra = "onClick=\"btn_submit('setKakko');\"";
    $arg["button"]["btn_kakko"] = knjCreateBtn($objForm, "btn_kakko", "()", $extra);

    //表示条件あり時は使用不可
    $disabled = "";
    if ($joukenAri) {
        $disabled = " disabled ";
    }
    //反映ボタン
    $extra = "onClick=\"subclassHanei();\"";
    $arg["button"]["btn_set"] = knjCreateBtn($objForm, "btn_set", "科目反映", $extra.$disabled);
    //取消ボタン
    $extra = "onClick=\"retry();\"";
    $arg["button"]["btn_retry"] = knjCreateBtn($objForm, "btn_retry", "編集モード", $extra);
    //更新ボタン
    $extra = "onClick=\"btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //削除ボタン
    $extra = "onClick=\"btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

function makeHidden(&$objForm, &$arg) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "TEXTNAME");
}

?>
