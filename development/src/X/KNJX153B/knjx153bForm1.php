<?php

require_once('for_php7.php');

class knjx153bForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $opt_shubetsu = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = $model->field["OUTPUT"] ? $model->field["OUTPUT"] : "1";
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], "", $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //処理名コンボボックス
        $opt_shori = array();
        $opt_shori[] = array("label" => "更新", "value" => "1");
        $opt_shori[] = array("label" => "削除", "value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, $extra, 1);

        //テストコンボボックス
        $year_seme = CTRL_YEAR.CTRL_SEMESTER;
        $query = knjx153bQuery::getTest($year_seme);
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], "", 1);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //年度一覧コンボボックス
        $query = knjx153bQuery::getYearSemester();
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //年組一覧コンボボックス
        $query = knjx153bQuery::getHrClass($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], "", 1, "ALL");

        //テスト種別コンボボックス
        $query = knjx153bQuery::getTest($model->field["YEAR"]);
        makeCmb($objForm, $arg, $db, $query, "MOCKCD", $model->field["MOCKCD"], "", 1);

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx153bindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx153bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array('label' => "(全て出力)",
                       'value' => "");
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

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR.CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //実行ボタン
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('exec');\"");
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm)
{
    KnjCreateHidden($objForm, "cmd");
}
