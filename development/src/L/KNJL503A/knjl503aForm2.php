<?php

require_once('for_php7.php');

class knjl503aForm2
{
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl503aindex.php", "", "edit");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //警告メッセージを表示しない場合
        if (isset($model->field["ENTEXAM_SCHOOLCD"])) {
            //データを取得
            $Row = knjl503aQuery::getRow($model);
        } else {
            $Row =& $model->field;
            //$Row["STAFFCD"] = $model->field["USERSCD"];
        }

        //愛知県学校コード
        $extra = "";
        $arg["data"]["ENTEXAM_SCHOOLCD"] = knjCreateTextBox($objForm, $Row["ENTEXAM_SCHOOLCD"], "ENTEXAM_SCHOOLCD", 4, 4, $extra);

        //出身学校コード検索ボタン
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.x, event.y, 500, 380);\"";
        $arg["button"]["btn_searchfs"] = knjCreateBtn($objForm, "btn_searchfs", "検 索", $extra);

        //出身学校コード
        $extra = "";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", 12, 12, $extra);
        $arg["data"]["FINSCHOOL_NAME"] = $Row["FINSCHOOL_NAME"];

        $disabledExtra = "";

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタンを作成する
        $extra = $disabledExtra."onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //削除ボタンを作成する
        $extra = $disabledExtra."onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタンを作成する
        $extra = $disabledExtra."onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        $arg["reload"]="";
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjl503aindex.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl503aForm2.html", $arg); 
    }
}
//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model) {
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header  = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
?>
