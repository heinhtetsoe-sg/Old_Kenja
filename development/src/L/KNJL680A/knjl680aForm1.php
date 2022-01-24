<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjl680aForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度取得
        $result = $db->query(knjl680aQuery::getEntExamYear());
        $opt = array();
        $flg = false;
        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) {
            $opt[] = array("label" => CTRL_YEAR+1, "value" => CTRL_YEAR+1);
            unset($model->examyear);
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["ENTEXAMYEAR"], "value" => $row["ENTEXAMYEAR"]);
                if ($model->examyear == $row["ENTEXAMYEAR"]) {
                    $flg = true;
                }
            }
        }
        $result->free();

        //初期表示の年度設定
        if (!$flg) {
            if (!isset($model->examyear)) {
                $model->examyear = CTRL_YEAR + 1;
            } elseif ($model->examyear > $opt[0]["value"]) {
                $model->examyear = $opt[0]["value"];
            } elseif ($model->examyear < $opt[get_count($opt) - 1]["value"]) {
                $model->examyear = $opt[get_count($opt) - 1]["value"];
            } else {
                $model->examyear = $db->getOne(knjl680aQuery::deleteAtExist($model));
            }
        }

        //年度コンボ
        $extra = "onchange=\"return btn_submit('main');\"";
        $arg["ENTEXAMYEAR"] = knjCreateCombo($objForm, "ENTEXAMYEAR", $model->examyear, $opt, $extra, 1);

        //入試制度
        $opt = array();
        $query = knjl680aQuery::getVNameMst($model, "L003");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $extra = " onchange=\"return btn_submit('main');\" ";
        $arg["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->applicantdiv, $opt, $extra, "1");

        //出力対象ラジオボタンを作成する
        $opt        = array(1,2); //1:入試区分を選択する 2:全ての入試区分
        if ($model->field["OUTPUTDIV"] == '') {
            $model->field["OUTPUTDIV"] = "1";
        }
        //$preextra   = " onchange=\"return btn_submit('main');\" ";
        //$extra      = array("id=\"OUTPUTDIV1\"".$preextra, "id=\"OUTPUTDIV2\"".$preextra,  "id=\"OUTPUTDIV3\"".$preextra);
        $extra        = array("id=\"OUTPUTDIV1\"", "id=\"OUTPUTDIV2\"",  "id=\"OUTPUTDIV3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUTDIV", $model->field["OUTPUTDIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        $extra = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SELECTED_DATA");
        knjCreateHidden($objForm, "SELECTED_DATA_LABEL");
        knjCreateHidden($objForm, "SCHOOLNAME", $model->schoolName);

        //DB切断
        Query::dbCheckIn($db);
        $arg["start"]  = $objForm->get_start("main", "POST", "knjl680aindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjl680aForm1.html", $arg);
    }
}
/********************************************** ここから下関数 ****************************************************/
//////////////
//コンボ作成//
//////////////
function makeCmb(&$objForm, &$arg, $db, $query, $value, $name, $extra, $size, $model, $blank = "")
{
    $result = $db->query($query);
    $opt    = array();
    $serch  = array();

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    if ($name == "GAKKI") {
        $value = ($value) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];
    }
    var_dump($opt);
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//////////////////////
//リストToリスト作成//
//////////////////////
function makeListToList(&$objForm, &$arg, $db, $model)
{

    //入試区分一覧
    $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
    $query = knjl680aQuery::getVNameMst($model, $namecd1);
    $result = $db->query($query);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["NAME1"],
                     "value" => $row["NAMECD2"]);
    }
    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $opt, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 20);

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
//////////////
//ボタン作成//
//////////////
function makeBtn(&$objForm, &$arg)
{
    //実行
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('exec');\"");
    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}
?>
