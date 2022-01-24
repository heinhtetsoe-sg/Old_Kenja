<?php

require_once('for_php7.php');

class knjh711Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjh711index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学力テストコンボボックス
        $query = knjh711Query::getTestName();
        $extra = "onchange=\"return btn_submit('change_test');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTID", $model->field["TESTID"], $extra, 1);

        //時限コンボボックス
        $query = knjh711Query::getPeriod();
        $extra = "onchange=\"return btn_submit('change_period');\"";
        makeCmb($objForm, $arg, $db, $query, "PERIODID", $model->field["PERIODID"], $extra, 1);

        //科目コンボボックス
        $query = knjh711Query::getSubclass($model);
        $extra = "onchange=\"return btn_submit('change_subclass');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "BLANK");

        //教室コンボボックス
        $query = knjh711Query::getFacility($model);
        $extra = "onchange=\"return btn_submit('change_facility');\"";
        if ($model->field["SUBCLASSCD"] == "") {
            $blank = "BLANK";
        } else {
            //科目が選択されている場合はリストの一番上の教室を表示
            $blank = "";
        }
        makeCmb($objForm, $arg, $db, $query, "FACCD", $model->field["FACCD"], $extra, 1, $blank);

        //クラスコンボボックス
        $query = knjh711Query::getHrClass($model);
        $extra = "onchange=\"return btn_submit('change_hrclass');\"";
        if ($model->field["SUBCLASSCD"] == "") {
            $blank = "BLANK";
        } else {
            //科目が選択されている場合はリストの一番上のクラスを表示
            $blank = "";
        }
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1, $blank);

        //定員
        $query = knjh711Query::getCapacity($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["CAPACITY"] = $row["CAPACITY"];
        }
        $result->free();
        $model->capacity = $arg["CAPACITY"];

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "capacity", $model->capacity);

        //ボタン作成
        makeBtn($objForm, $arg);

        $arg["info"] = array("LEFT_LIST"  => "割当て対象者一覧",
                             "RIGHT_LIST" => "生徒一覧");
        $arg["finish"] = $objForm->get_finish();

        //ローカルのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh711Form1.html", $arg);
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model)
{
    //登録済み生徒取得
    $query = knjh711Query::getExistData($model);
    $result = $db->query($query);
    $schregnoList = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $schregnoList .= "'".$row["SCHREGNO"]."',";
    }
    $result->free();
    $schregnoList = substr($schregnoList, 0, -1);
    if ($schregnoList == "") {
        $schregnoList = "''";
    }

    //生徒一覧リストを作成する
    $query = knjh711Query::getStudents($model, $schregnoList, false);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["GRADE"]."年".$row["HR_CLASS"]."組".$row["ATTENDNO"]."番 "."　".$row["NAME"],
                        'value' => $row["SCHREGNO"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left', 1)\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

    //割り当て対象者一覧リストを作成する
    $query = knjh711Query::getStudents($model, $schregnoList, true);
    $result = $db->query($query);
    $opt2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt2[] = array('label' => $row["GRADE"]."年".$row["HR_CLASS"]."組".$row["ATTENDNO"]."番 "."　".$row["NAME"],
                        'value' => $row["SCHREGNO"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right', 1)\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt2, $extra, 20);

    //割り当て人数
    $arg["count"] = get_count($opt2);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //全追加ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', 1);\"";
    $arg["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //追加ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', 1);\"";
    $arg["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //削除ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', 1);\"";
    $arg["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //全削除ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', 1);\"";
    $arg["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => "",
                       'value' => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
