<?php

require_once('for_php7.php');

class knjh186form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh186index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->schregno) && isset($model->date)) {
            $Row = $db->getRow(knjh186Query::getChildcareDat($model, $model->date), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //預かり日
        $date = str_replace("-", "/", $Row["CARE_DATE"]);
        $arg["data"]["CARE_DATE"] = View::popUpCalendar($objForm, "CARE_DATE", $date);

        //金額をキーでソート
        ksort($model->fare_array);

        //金額取得
        $opt = $extra = array();
        foreach ($model->fare_array as $key => $val) {
            $opt[] = $key;
            $extra[$key] = "id=\"FARE_CD{$key}\"";
        }
        //金額ラジオボタン
        if ($Row["FARE_CD"] == "") $Row["FARE_CD"] = 999;    //未設定
        $radio = $sep = "";
        $cnt = 0;
        foreach ($model->fare_array as $key => $val) {

            $objForm->ae( array("type"      => "radio",
                                "name"      => "FARE_CD",
                                "value"     => $Row["FARE_CD"],
                                "extrahtml" => $extra[$key],
                                "multiple"  => $opt));

            $radio .= $sep;
            $radio .= ($cnt != 0 && $cnt%6 == 0) ? "<br>" : "";
            $radio .= $objForm->ge("FARE_CD", $key);
            $radio .= "<LABEL for=\"FARE_CD{$key}\">".$val."</LABEL>";

            $sep = "&nbsp;&nbsp;";
            $cnt++;
        }
        $arg["data"]["FARE_CD"] = $radio;

        //テキスト入力
        foreach ($model->txt_array as $key => $val) {
            if ($val["gyo"] == 1) {
                $tmp["TITLE"] = $val["title"];
                $tmp["TEXT"] = knjCreateTextBox($objForm, $Row[$key], $key, ($val["moji"] * 2), ($val["moji"] * 2), "");
                $tmp["COMMENT"] = "(全角{$val["moji"]}文字まで)";
            } else {
                $tmp["TITLE"] = $val["title"];
                $height = $val["gyo"] * 13.5 + ($val["gyo"] - 1) * 3 + 5;
                $tmp["TEXT"] = KnjCreateTextArea($objForm, $key, $val["gyo"], ($val["moji"] * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row[$key]);
                $tmp["COMMENT"] = "(全角{$val["moji"]}文字X{$val["gyo"]}行まで)";
            }
            $arg["data1"][] = $tmp;
        }

        //更新ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('delete');\"" : "disabled";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "reset") {
            $arg["reload"] = "window.open('knjh186index.php?cmd=list','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh186Form2.html", $arg);
    }
}
?>
