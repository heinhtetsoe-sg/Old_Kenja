<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knje354Form1
{
    public function main($model)
    {
        $objForm = new form();

        //DB接続
        $db  = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //対象年度
        $arg["data"]["DISP_YEAR"] = CTRL_YEAR;
        $arg["data"]["NOWDATE"]  = knje354Query::getMaxUpdate($db);

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力
        $opt_shubetsu = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"]=="") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //処理名コンボボックス
        $opt_shori   = array();
        $opt_shori[] = array("label" => "更新","value" => "1");
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", 1);

        //年度コンボボックス
        $opt_year   = array();
        $opt_year[] = array("label" => CTRL_YEAR."年度","value" => CTRL_YEAR);
        $opt_year[] = array("label" => (CTRL_YEAR + 1)."年度","value" => (CTRL_YEAR + 1));
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt_year, "", 1);

        //ヘッダ有無
        $extra = "checked id=\"HEADERCHECK\"";
        $arg["data"]["HEADERCHECK"] = knjCreateCheckBox($objForm, "HEADERCHECK", "1", $extra, "");

        //ファイルからの取り込み
        $arg["data"]["FILE"] = knjCreateFile($objForm, "FILE", "", 2048000);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje354index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje354Form1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_cancel", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
}
