<?php

require_once('for_php7.php');

class knjx_a125jForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        if ($model->prgid == "KNJA126J" && $model->cmd == "sign") {
            //HTTPSをHTTPにする
            $arg["signature"] = "collHttps('".REQUESTROOT."', '')";
            //必須データチェック
            if (!$model->exp_year || !$model->exp_semester || !$model->schregno) {
                $arg["jscript"] = "OnAuthError();";
            }
            $model->flg = "sign";
        } elseif ($model->prgid == "KNJA126J") {
        } else {
            unset($model->flg);
        }

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";

        //DB接続
        $db = Query::dbCheckOut();

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", $size);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $extra = "checked";
        } else {
            $extra = ($model->cmd == "") ? "checked" : "";
        }
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $opt_shubetsu = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

        //年度
        if ($model->flg == "sign") {
            $year_sem = $db->getRow(knjx_a125jquery::getSelectFieldSQL($model, $model->flg), DB_FETCHMODE_ASSOC);
            $arg["data"]["YEAR"]    = $year_sem["LABEL"];
            $model->field["YEAR"]   = $year_sem["VALUE"];
            knjCreateHidden($objForm, "YEAR", $year_sem["VALUE"]);
        } else {
            $query = knjx_a125jquery::getSelectFieldSQL($model);
            $extra = "onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);
        }

        //年組
        if ($model->flg == "sign") {
            $grade_hr = $db->getRow(knjx_a125jquery::getSelectFieldSQL2($model, $model->flg), DB_FETCHMODE_ASSOC);
            $arg["data"]["GRADE_HR_CLASS"]  = $grade_hr["LABEL"];
            $model->field["GRADE_HR_CLASS"] = $grade_hr["VALUE"];
            knjCreateHidden($objForm, "GRADE_HR_CLASS", $grade_hr["VALUE"]);
        } else {
            $query = knjx_a125jquery::getSelectFieldSQL2($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], "", 1);
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useflg", $model->flg);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx_a125jindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_a125jForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    if ($name == "GRADE_HR_CLASS") {
        $opt[] = array("label" => "(全て出力)", "value" => "");
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

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR.CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
