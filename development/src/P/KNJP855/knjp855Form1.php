<?php

require_once('for_php7.php');

class knjp855Form1
{
    public function main(&$model)
    {
        //Ajax ※CSV出力押下時に入金パターンに指定した徴収月が含まれるかを取得
        if ($model->cmd == "chk_month") {
            $monthExistsFlg = false;
            for ($i = 1; $i <= 12; $i++) {
                if ($model->field["COLLECT_MONTH"] == $model->patternInfo["COLLECT_MONTH_".$i]) {
                    $monthExistsFlg = true;
                }
            }
            $responseData = array("EXIST_FLG" => $monthExistsFlg);
            echo json_encode($responseData);
            die();
        }

        //オブジェクト作成
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp855Form1", "POST", "knjp855index.php", "", "knjp855Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->year;

        //最大出力項目数
        $arg["data"]["OUTPUT_MAX_CNT"] = $model->outputMaxCnt;

        //出力取込種別ラジオボタン 1:エラー出力 2:データ取込 3:データ出力
        $opt_shubetsu = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //学年コンボ作成
        if ($model->cmd == "change_grade" && $model->field["GRADE"] == "ALL") {
            $model->field["HR_CLASS"] = "ALL";
        } elseif ($model->field["GRADE"] == "ALL" && $model->field["HR_CLASS"] != "ALL") {
            list($grade, $hrClass) = explode("-", $model->field["HR_CLASS"]);
            $model->field["GRADE"] = $grade;
        }
        $query = knjp855Query::getGradeHrClass($model, "GRADE");
        $extra = "onchange=\"return btn_submit('change_grade');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "ALL");

        //クラスコンボ作成
        $query = knjp855Query::getGradeHrClass($model, "HR_LASS");
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1, "ALL");

        //徴収月
        $monthOpt = array();
        for ($i = 4; $i <= 15; $i++) {
            $monthVal = ($i <= 12) ? $i : $i - 12;
            $monthVal = sprintf("%02d", $monthVal);
        
            $monthOpt[] = array('label' => $monthVal,
                                'value' => $monthVal);
        }
        $extra = "";
        $arg["data"]["COLLECT_MONTH"] = knjCreateCombo($objForm, "COLLECT_MONTH", $model->field["COLLECT_MONTH"], $monthOpt, $extra, 1);

        //入金パターンコンボ
        $extra = "";
        $query = knjp855Query::getPatternList($model);
        makeCmb($objForm, $arg, $db, $query, "COLLECT_PATTERN_CD", $model->field["COLLECT_PATTERN_CD"], $extra, 1);

        //帳票日付
        $model->field["SLIP_DATE"] = $model->field["SLIP_DATE"] ? $model->field["SLIP_DATE"] : CTRL_DATE;
        $model->field["SLIP_DATE"] = str_replace("-", "/", $model->field["SLIP_DATE"]);
        $arg["data"]["SLIP_DATE"] = View::popUpCalendar($objForm, "SLIP_DATE", $model->field["SLIP_DATE"]);

        //ヘッダ有
        $extra = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        //ファイルからの取り込み
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        //リストToリスト作成
        makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjp855Form1.html", $arg, "main5_JqueryOnly.html");
    }
}

function makeList(&$objForm, &$arg, $db, $model)
{
    //対象クラスリストを作成する
    $query = knjp855Query::getKagaihiList($model);
    $result = $db->query($query);
    $opt1 = $opt2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $model->selectdata)) {
            $opt2[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        } else {
            $opt1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"doMove('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

    //出力対象一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"doMove('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt2, $extra, 20);

    //項目件数(画面更新後の初期値)
    $arg["data"]["RIGHT_CNT"] = get_count($opt1);
    $arg["data"]["LEFT_CNT"]  = 0;

    //extra
    $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"doMove('right');\"";
    $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"doMove('left');\"";

    //対象選択ボタンを作成する
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra_rights);
    //対象取消ボタンを作成する
    $arg["button"]["btn_lefts"]  = knjCreateBtn($objForm, "btn_lefts", "<<", $extra_lefts);
    //対象選択ボタンを作成する
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra_right1);
    //対象取消ボタンを作成する
    $arg["button"]["btn_left1"]  = knjCreateBtn($objForm, "btn_left1", "＜", $extra_left1);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $all = "")
{
    $opt = array();
    if ($all == "ALL") {
        $opt[] = array("label" => "--全て--", "value" => "ALL");
    }
    $value_flg = false;
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

function makeBtn(&$objForm, &$arg)
{
    //CSVボタンを作成する
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "実 行", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "SCHOOLCD", $model->schoolCd);
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJP855");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "OUTPUT_MAX_CNT", $model->outputMaxCnt);
    knjCreateHidden($objForm, "GRPCD_FROM", $model->grpCdFromNo);
    knjCreateHidden($objForm, "GRPCD_TO", $model->grpCdToNo);
    knjCreateHidden($objForm, "NEXT_GRPCD", $model->nextGrpCd);
}
