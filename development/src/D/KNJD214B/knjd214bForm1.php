<?php

require_once('for_php7.php');

class knjd214bForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd214bForm1", "POST", "knjd214bindex.php", "", "knjd214bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //ログイン年度・学期
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd214bQuery::getSemester();
        $extra = "onChange=\"return btn_submit('knjd214b');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $query = knjd214bQuery::getSelectGrade($model);
        $extra = "onChange=\"return btn_submit('knjd214b');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //クリア処理とコピー処理のラジオボタン 1:クリア処理 2:コピー処理
        $model->field["SHORI"] = $model->field["SHORI"] ? $model->field["SHORI"] : '1';
        $opt_shori = array(1, 2);
        $extra = array("id=\"SHORI1\" onClick=\"disCmb();\"", "id=\"SHORI2\" onClick=\"disCmb();\"");
        $radioArray = knjCreateRadio($objForm, "SHORI", $model->field["SHORI"], $extra, $opt_shori, get_count($opt_shori));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //コピー元になる評価コンボ作成
        $query = "";
        $extra = $model->field["SHORI"] == "1" ? "disabled" : "";
        testCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], $extra, 1, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd214bForm1.html", $arg); 
    }
}

//コピー元になる評価コンボ作成
function testCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = "")
{
    //学期マスタ
    $optSem = array();
    $query = knjd214bQuery::getSemester($model->field["SEMESTER"]);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $optSem[$row["VALUE"]] = $row["LABEL"];
    }
    $result->free();
    //テスト項目マスタ
    $opt = array();
    foreach($optSem as $sem => $semName) {
        $arg["data"]["SEMESTERNAME"] = $semName; //文言
        $query = knjd214bQuery::getTestItem($model, $sem);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $sem .$row["VALUE"] ."：" .$semName .$row["LABEL"],
                           'value' => $sem .$row["VALUE"]);
        }
        $result->free();
    }

    $value = ($value == "") ? $opt[0]["value"] : $value;

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = "")
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "") ? CTRL_SEMESTER : $value;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    if ($name == "GRADE") {
        $opt[] = array('label' => '全学年', 'value' => '999');
    }

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //対象者(左のリスト）
    $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
    //クラス一覧
    $leftList = $rightList = array();
    $result = $db->query(knjd214bQuery::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $array)) {
            $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        } else {
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = createCombo($objForm, "CLASS_NAME", "", $rightList, $extra, 12);

    //出力対象作成
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = createCombo($objForm, "CLASS_SELECTED", "", $leftList, $extra, 12);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    $arg["button"]["btn_exe"] = createBtn($objForm, "btn_exe", "実 行", "onclick=\"return doSubmit();\"");
    //閉じるボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("selectdata"));
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
