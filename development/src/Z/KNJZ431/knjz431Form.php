<?php

require_once('for_php7.php');

class knjz431form {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz431index.php", "", "right_list");

        //DB接続
        $db = Query::dbCheckOut();

        //教育委員会チェック
        if ($db->getOne(knjz431Query::checkEdboard()) != 1) {
            $arg["jscript"] = "OnAuthError();";
        }
        //セキュリティーチェック
        if (AUTHORITY < DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        //処理年度
        $arg["YEAR"] = $model->year;

        //次年度
        $arg["YEAR_ADD"] = $model->year_add;

        //ALLチェックボックス
        $extra = "onClick=\"return check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //データ作成
        makeData($objForm, $arg, $db, $model);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["btn_execute"] = knjCreateBtn($objForm, "btn_execute", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz431Form.html", $arg);
    }
}

//データ作成
function makeData(&$objForm, &$arg, $db, $model) {
    $setval = array();      //出力データ配列
    $getData = array();
    $getData = setTableData();
    $disabled = "";
    for ($i = 0; $i < get_count($getData); $i++) {
        //テーブル存在チェック
        $query = knjz431Query::cnt_table($getData[$i]["VALUE"]);
        $exist_flg = $db->getOne($query) > 0 ? true : false;
        if ($exist_flg) {
            $setval["KEKKA"] = $db->getOne(knjz431Query::getkekka($model->year, $model->year_add, $getData[$i]["VALUE"]));
        } else {
            $setval["KEKKA"] = '今年度データなし';
        }

        $setval["MSTNAME"] = $getData[$i]["NAME"];
        if ($setval["KEKKA"] != "") {
            $disabled = "disabled";
        } else {
            $disabled = "";
        }
        $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $getData[$i]["VALUE"], $disabled, "1");
        $arg["data"][] = $setval;
    }
}

//表示用テーブル配列作成
function setTableData() {
    $setD = array();
    $setD[] = ( array("NAME" => "学校マスタ",       "VALUE" => "SCHOOL_MST"));
    $setD[] = ( array("NAME" => "名称マスタ",       "VALUE" => "NAME_YDAT"));
    $setD[] = ( array("NAME" => "グループマスタ",   "VALUE" => "USERGROUP_DAT"));

   return $setD;
}
?>
