<?php

require_once('for_php7.php');

class knjh186form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjh186index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //一覧表示
        $query = knjh186Query::getChildcareDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $row["CARE_DATE"]  = str_replace("-", "/", $row["CARE_DATE"]);

            $row["FARE"] = $model->fare_array[$row["FARE_CD"]];

            //データが長いときは10文字まで表示
            foreach ($model->txt_array as $key => $val) {
                $row[$key] = (mb_strlen($row[$key]) > 10) ? mb_substr($row[$key], 0, 10).'...' : $row[$key];
            }

            $arg["data"][] = $row;
        }

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list"){ 
            $arg["reload"] = "window.open('knjh186index.php?cmd=edit&SCHREGNO=$model->schregno','edit_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh186Form1.html", $arg);
    }
}
?>
