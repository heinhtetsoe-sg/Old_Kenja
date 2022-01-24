<?php

require_once('for_php7.php');

class knjx153Form1
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
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = "onclick=\"return btn_submit('');\"";
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", 1);

        //テストコンボボックス（取込）
        $year_seme = CTRL_YEAR.CTRL_SEMESTER;
        $query = knjx153Query::getTest($year_seme, $model);
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], "", 1);

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, "");

        //年度＆学期コンボボックス
        $query = knjx153Query::getSelectFieldSQL();
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //年組一覧コンボボックス
        $query = knjx153Query::getSelectFieldSQL2($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], "", 1, $model);

        //学期コンボ
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        $query = knjx153Query::getSemesterMst();
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //テスト種別コンボボックス
        $query = knjx153Query::getTest($model->field["YEAR"], $model);
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "", 1);

        //ヘッダ有チェックボックス
        $extra = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx153index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx153Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = "")
{
    $opt = array();
    if ($name == "GRADE_HR_CLASS" && ($model->auth == DEF_REFERABLE || $model->auth == DEF_UPDATABLE)) {
        $opt[] = array("label" => "(全て出力)","value" => "");
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
    } elseif ($name == "GRADE_HR_CLASS" && ($model->auth == DEF_REFERABLE || $model->auth == DEF_UPDATABLE)) {
        $value = ($value && $value_flg) ? $value : "";
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    if ($model->field["OUTPUT"] == "2") {
        if ($model->auth == DEF_UPDATABLE || $model->auth == DEF_UPDATE_RESTRICT) {
            $extra = "onclick=\"return btn_submit('exec');\"";
        } else {
            $extra = "disabled";
        }
    } else {
        $extra = "onclick=\"return btn_submit('exec');\"";
    }
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
}
