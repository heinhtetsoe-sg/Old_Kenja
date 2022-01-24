<?php

require_once('for_php7.php');

class knjz281_2aForm2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz281_2aQuery::getRow($model->jobcd);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //職名コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["JOBCD"] = knjCreateTextBox($objForm, $Row["JOBCD"], "JOBCD", 4, 4, $extra);

        //職名名称
        $extra = "";
        $arg["data"]["JOBNAME"] = knjCreateTextBox($objForm, $Row["JOBNAME"], "JOBNAME", 40, 20, $extra);

        //学校基本調査名称
        $extra = "";
        $arg["data"]["BASE_JOBNAME"] = knjCreateTextBox($objForm, $Row["BASE_JOBNAME"], "BASE_JOBNAME", 40, 20, $extra);

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ281A/knjz281aindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz281_2aindex.php", "", "edit");
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"] = "parent.left_frame.location.href='knjz281_2aindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz281_2aForm2.html", $arg);
    }
}

//CSV作成
function makeCsv(&$objForm, &$arg, $model)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header  = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
