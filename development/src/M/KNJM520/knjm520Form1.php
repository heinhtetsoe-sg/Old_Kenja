<?php

require_once('for_php7.php');


class knjm520Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm520Form1", "POST", "knjm520index.php", "", "knjm520Form1");

        //ラジオボタンを作成する
        $extra = "onClick=\"useableTextBox()\" ";
        $arg["data"] = knjCreateRadio($objForm, 'PRINT_DIV', '1', $extra, '', 2);

        //年度テキストを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        //集計日付を作成する
        $date_box = View::popUpCalendarAlp($objForm, "DATE", str_replace("-", "/", CTRL_DATE), "");
        $arg["date"]["DATE"] = $date_box;

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, 'btn_print', 'プレビュー／印刷', $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, 'DBNAME', DB_DATABASE);

        knjCreateHidden($objForm, 'PRGID', 'KNJM520');

        knjCreateHidden($objForm, 'SEMESTER', CTRL_SEMESTER);

        knjCreateHidden($objForm, 'cmd');

        knjCreateHidden($objForm, 'YEAR', CTRL_YEAR);

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm520Form1.html", $arg);
    }
}
?>
