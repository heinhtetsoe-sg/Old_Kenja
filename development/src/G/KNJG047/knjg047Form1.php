<?php

require_once('for_php7.php');

class knjg047Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjg047Form1", "POST", "knjg047index.php", "", "knjg047Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //開始日付作成
        $model->field["DATE_FROM"] = $model->field["DATE_FROM"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE_FROM"];
        $arg["data"]["DATE_FROM"] = View::popUpCalendar($objForm, "DATE_FROM", $model->field["DATE_FROM"]);

        //終了日付作成
        $model->field["DATE_TO"] = $model->field["DATE_TO"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE_TO"];
        $arg["data"]["DATE_TO"] = View::popUpCalendar($objForm, "DATE_TO", $model->field["DATE_TO"]);

        //日付チェック情報取得
        $checkStartDate = str_replace("-", "/", $db->getOne(knjg047Query::getSemedayQuery("SDATE")));
        $checkEndDate = str_replace("-", "/", $db->getOne(knjg047Query::getSemedayQuery("")));
        
        //hidden
        knjCreateHidden($objForm, "CHECK_SDATE", $checkStartDate);
        knjCreateHidden($objForm, "CHECK_EDATE", $checkEndDate);

        //ＣＳＶ出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg047Form1.html", $arg); 
    }
}
?>
