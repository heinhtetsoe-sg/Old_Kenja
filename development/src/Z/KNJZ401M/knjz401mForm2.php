<?php

require_once('for_php7.php');

class knjz401mForm2
{
    public function main(&$model)
    {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz401mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //科目名表示
        $arg["SUBCLASSNAME"] = $db->getOne(knjz401mQuery::getSubClassName($model, $model->subclasscd));

        //データ取得
        if (isset($model->grade_semes) && isset($model->subclasscd) && !isset($model->warning) && ($model->cmd != "class")) {
            $result = $db->query(knjz401mQuery::getData($model, $model->grade_semes, $model->subclasscd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["REMARK1_".$row["VIEWCD"]] = $row["REMARK1"];
            }
        } else {
            $Row =& $model->field;
        }

        //観点一覧表示
        $tmp = array();
        $cnt = 0;
        $colorFlg = true;
        $result = $db->query(knjz401mQuery::getData($model, $model->grade_semes, $model->subclasscd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tmp["VIEW_SHOW"] = $row["VIEWCD"].':'.$row["VIEWABBV"];

            //めあてテキストエリア
            $height = 5 * 13.5 + (5 - 1) * 3 + 5;
            $tmp["REMARK1"] = KnjCreateTextArea($objForm, "REMARK1_".$row["VIEWCD"], 5, (20 * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row["REMARK1_".$row["VIEWCD"]]);
            $tmp["REMARK1_COMMENT"] = "(全角20文字X5行まで)";

            $arg["data"][] = $tmp;
        }

        //CSV作成
        makeCsv($objForm, $arg, $db, $model);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "GRADE2", $Row["GRADE"]);
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz401mindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz401mForm2.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
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

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $db, $model)
{
    //ヘッダ有チェックボックス
    $extra  = ($model->field["HEADER"] == "on" || $model->field["OUTPUT"] == "") ? "checked" : "";
    $extra .= " id=\"HEADER\"";
    $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:ヘッダ出力（見本）
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["csv"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
