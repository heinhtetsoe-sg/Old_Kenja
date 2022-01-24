<?php

require_once('for_php7.php');
    class knja350Form1
    {
        function main(&$model)
        {
            $objForm        = new form;
            $arg["start"]   = $objForm->get_start("main", "POST", "knja350index.php", "", "main");

            //起動条件処理
            if ($model->cmd == "") {
                //セキュリティーチェック
                if ($model->sec_competence != DEF_UPDATABLE){
                    $arg["close"] = "close_window();";
                }
            }
            $arg["data"]["THIS_YEAR"] = CTRL_YEAR;

            $db = Query::dbCheckOut();

            $query = knja350Query::getGradeName($model);
            $gradename = $db->getOne($query);
            if ($gradename) {
                $arg["data"]["GRADE_NAME"] = "新".$gradename;
            }
            $query = knja350Query::getTargetCount($model);
            $count = $db->getOne($query);
            $arg["data"]["STD_COUNT"] = $count;

            //処理n回目
            $opt = array(1, 2);
            if ($model->field["PROC"] == "") {
                $model->field["PROC"] = "1";
            }
            $extra = array("id=\"PROC1\"", "id=\"PROC2\"");
            $radioArray = knjCreateRadio($objForm, "PROC", $model->field["PROC"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            //実行ボタン
            $extra = " onclick=\"return btn_submit('update');\"";
            $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

            //終了ボタン
            $extra = " style=\"margin-left: 10px; \" onclick=\"closeWin();\"";
            $arg["button"]["btn_finish"] = knjCreateBtn($objForm, "btn_finish", "終 了", $extra);

            //hidden
            knjCreateHidden($objForm, "cmd");

            Query::dbCheckIn($db);

            $arg["finish"]  = $objForm->get_finish();

            //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
            View::toHTML($model, "knja350Form1.html", $arg); 
        }
    }
?>
