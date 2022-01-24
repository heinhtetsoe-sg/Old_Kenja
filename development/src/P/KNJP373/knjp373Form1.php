<?php

require_once('for_php7.php');


class knjp373Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp373Form1", "POST", "knjp373index.php", "", "knjp373Form1");

        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = CTRL_YEAR;
/*
        //担当者コンボを作成する
        $query = knjp373Query::GetStaff($model);
        makeCombo($objForm, $arg, $db, $query, $model->staffcd, "STAFF", "", 1, "BLANK");
*/
        //対象年月コンボボックス
        $query = knjp373Query::getYearMonth();
        $extra = "onchange=\"return btn_submit('change_class');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["YEAR_MONTH"], "YEAR_MONTH", $extra, 1);

        //取扱指定日
        if ($model->date == "") {
            $model->date = str_replace("-","/",CTRL_DATE);
        }
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->date);

        //クラス選択コンボボックスの設定
        $query = knjp373Query::getclass($model);
        $extra = "onchange=\"return btn_submit('change_class');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        /**********************/
        /* 対象者リストの設定 */
        /**********************/
        $opt1 = array();
        $opt_left = array();

        //生徒単位
        $selectleft = explode(",", $model->selectleft);

        $query = knjp373Query::getsch($model);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"], 
                                                         "value" => $row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":");

            if ($model->cmd == 'change_class') {
                if (!in_array($row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":", $selectleft)) {
                    $opt1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":");
                }
            } else {
                $opt1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":");
            }
        }
        //左リストで選択されたものを再セット
        if ($model->cmd == 'change_class') {
            foreach ($model->select_opt as $key => $val) {
                if (in_array($key, $selectleft)) {
                    $opt_left[] = $val;
                }
            }
        }

        //対象
        $disable = 1;
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left', $disable)\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);
        //対象外
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right',$disable)\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //対象選択ボタンの設定
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right',$disable);\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンの設定
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left',$disable);\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンの設定
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right',$disable);\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンの設定
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left',$disable);\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        makeBtn($objForm, $arg);

        makeHidden($objForm, $arg, $model);

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp373Form1.html", $arg); 
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
function makeBtn(&$objForm, &$arg)
{
    //ＣＳＶ出力
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, &$arg, $model)
{
    $arg["TOP"]["YEAR"] = knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    $arg["TOP"]["SEMESTER"] = knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    $arg["TOP"]["DBNAME"] = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    $arg["TOP"]["PRGID"] = knjCreateHidden($objForm, "PRGID", "KNJP373");

    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
}
?>
