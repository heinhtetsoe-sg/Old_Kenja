<?php

require_once('for_php7.php');

class knjx_l018vForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx_l018vindex.php", "", "main");

        //年度
        $arg["data"]["EXAM_YEAR"] = $model->field["EXAM_YEAR"];

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ有チェックボックス
        $extra  = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
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

        //校種コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjx_l018vQuery::getExamSchoolKind($model);
        makeCmb($objForm, $arg, $db, $query, "EXAM_SCHOOL_KIND", $model->field["EXAM_SCHOOL_KIND"], $extra, 1);

        //試験IDコンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjx_l018vQuery::getExamID($model);
        makeCmb($objForm, $arg, $db, $query, "EXAM_ID", $model->field["EXAM_ID"], $extra, 1);

        //会場コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjx_l018vQuery::getPlaceID($model);
        makeCmb($objForm, $arg, $db, $query, "PLACE_ID", $model->field["PLACE_ID"], $extra, 1, "ALL");

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //ボタン作成
        makeBtn($objForm, $arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "EXAM_YEAR", $model->field["EXAM_YEAR"]);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJX_L018V");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_l018vForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    } elseif ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
    }
    $value_flg = ($blank == "ALL") ? true : false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if (($name == 'EXAM_SCHOOL_KIND') && ($row["NAMESPARE2"] == '1') && $default_flg && $value != "ALL") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg, $db, $model)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    //今年度・今学期名及びタイトルの表示
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
