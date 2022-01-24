<?php

require_once('for_php7.php');

/********************************************************************/
/* １期分授業料・生活行事費納付処理                 山城 2006/04/10 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：交付データ更新時に終了日付と金額を追加   山城 2006/04/19 */
/********************************************************************/

class knjp391kForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp391kForm1", "POST", "knjp391kindex.php", "", "knjp391kForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度を作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //中高判定フラグを作成する
        $row = $db->getOne(knjp391kQuery::GetJorH());
        if ($row == 1){
            $jhflg = 1;
        } else {
            $jhflg = 2;
        }

        //処理ラジオ
        $opt_output = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"" , "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //支払日
        if ($model->field["DATE1"] == "") $model->field["DATE1"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm    ,"DATE1"    ,$model->field["DATE1"]);

        //登録開始日付
        if ($model->field["DATE2"] == "") $model->field["DATE2"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm    ,"DATE2"    ,$model->field["DATE2"]);

        //終了日付 NO001
        $model->edate = $model->control["学期終了日付"][9];
        $arg["data"]["DATE3"] = $model->edate;

        //金額 NO001
        $extraInt = " onblur=\"this.value=toInteger(this.value)\"";
        $extraRight = " style=\"text-align:right\"";
        $arg["data"]["MONEY"] = knjCreateTextBox($objForm, $model->field["MONEY"], "MONEY", 8, 8, $extraInt.$extraRight);

        //クラスコンボ
        $query = knjp391kQuery::GetClass();
                $extra = "onchange =\" return btn_submit('knjp391k');\"";
        makeCombo($objForm, $arg, $db, $query, $model->hrclass, "HR_CLASS", $extra, 1, "BLANK");

        //対象者リストを作成する
        $opt1 = array();
        $opt_left = array();
        $selectleft = explode(",", $model->selectleft);
        $query = knjp391kQuery::GetSchreg($model);

        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->select_opt[$row["VALUE"]] = array('label' => $row["LABEL"], 
                                                      'value' => $row["VALUE"]);
            if (!in_array($row["VALUE"], $selectleft)) {
                $opt1[]= array('label' =>  $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
        }
        //左リストで選択されたものを再セット
        if ($model->select_opt) {
            foreach ($model->select_opt as $key => $val) {
                if (in_array($key, $selectleft)) {
                    $opt_left[] = $val;
                }
            }
        }
        $result->free();

        //生徒一覧リストを作成する
        $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

        //除外リストを作成する
        $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $arg, $jhflg);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp391kForm1.html", $arg); 
    }
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
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //実行ボタン
    $extra = "onclick=\"return btn_submit('execute');\"";
    $arg["button"]["btn_exe"] = knjCreateBtn($objForm, "btn_exe", "実 行", $extra);
    //更新ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, &$arg, $jhflg) {
    knjCreateHidden($objForm, "JHFLG", $jhflg);
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "cmd");
}

?>
