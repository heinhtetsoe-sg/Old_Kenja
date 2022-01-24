<?php

require_once('for_php7.php');

class knjj181form {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjj181index.php", "", "right_list");

        $db = Query::dbCheckOut();

        $arg["YEAR"]        = $model->year;
        $arg["YEAR_ADD"]    = $model->year_add;

        //データ作成
        $this->makeData($objForm, $arg, $db, $model);

        //実行ボタンを作成する
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["button"]["btn_execute"] = knjCreateBtn($objForm, "btn_execute", "実 行", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "YEAR_ADD", $model->year_add);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj181Form.html", $arg);
    }

    //
    function makeData(&$objForm, &$arg, $db, $model) {
        $setval = array();  //出力データ配列
        $getData = array();
        $getData = $this->setData();
        $disabled = "";
        for ($i = 0; $i < get_count($getData); $i++) {
            $query = knjj181Query::cnt_table($getData[$i]["VALUE"]);
            $exist_flg = $db->getOne($query) > 0 ? true : false;
            if ($exist_flg) {
                $setval["KEKKA"]   = $db->getOne(knjj181Query::getkekka($model->year, $model->year_add, $getData[$i]["VALUE"]));
            } else {
                $setval["KEKKA"]   = '今年度データなし';
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

    //表示用データ配列作成
    function setData() {
        $setD = array();
        $setD[] = ( array("NAME" => "保護者と教職員の会データ", "VALUE" => "SCHREG_BRANCH_DAT"));

       return $setD;
    }
}
?>
