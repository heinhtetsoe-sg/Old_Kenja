<?php

require_once('for_php7.php');


class knjm530Form1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjm530Form1", "POST", "knjm530index.php", "", "knjm530Form1");

    //エラー情報取得
    $model->errDataUmu();

    //クッキー設定用画面
    $arg["data"]["COOKIE_NAME"] = knjCreateTextBox($objForm, $model->cookie_name, "COOKIE_NAME", 14, 14, "");

    //ボタンを作成する
    makeBtn($objForm, $arg, $model);

    //hiddenを作成する
    makeHidden($objForm);

    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjm530Form1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{

    if ($model->errAttendFlg) {
        //実行
        $extra = "onclick=\"return btn_submit('errA');\"";
        $arg["button"]["btn_errA"] = knjCreateBtn($objForm, "btn_errA", "出欠エラー", $extra);
    }
    if ($model->errReportFlg) {
        //実行
        $extra = "onclick=\"return btn_submit('errR');\"";
        $arg["button"]["btn_errR"] = knjCreateBtn($objForm, "btn_errR", "レポートエラー", $extra);
    }
    //COOKIE実行
    $extra = "onclick=\"return btn_submit('updateCookie');\"";
    $arg["button"]["btn_cookie"] = knjCreateBtn($objForm, "btn_cookie", "実 行", $extra);
    //ファイル更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_exe"] = knjCreateBtn($objForm, "btn_exe", "実 行", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

    //個人
    $extra = "onclick=\"return btn_submit('seito');\"";
    $arg["button"]["btn_kojin"] = knjCreateBtn($objForm, "btn_kojin", "生徒情報", $extra);
    //職員
    $extra = "onclick=\"return btn_submit('staff');\"";
    $arg["button"]["btn_shokuin"] = knjCreateBtn($objForm, "btn_shokuin", "職員情報", $extra);
    //校時
    $extra = "onclick=\"return btn_submit('kouji');\"";
    $arg["button"]["btn_kouji"] = knjCreateBtn($objForm, "btn_kouji", "校時", $extra);
    //講座
    $extra = "onclick=\"return btn_submit('kouza');\"";
    $arg["button"]["btn_kouza"] = knjCreateBtn($objForm, "btn_kouza", "講座", $extra);
    //時間割
    $extra = "onclick=\"return btn_submit('jikanwari');\"";
    $arg["button"]["btn_jikanwari"] = knjCreateBtn($objForm, "btn_jikanwari", "時間割", $extra);

}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
}
?>
