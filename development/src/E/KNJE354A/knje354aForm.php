<?php

require_once('for_php7.php');

class knje354aform {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje354aindex.php", "", "right_list");

        $db = Query::dbCheckOut();

        $arg["YEAR"]        = $model->year;
        $arg["YEAR_ADD"]    = $model->year_add;

        //ALLチェック
        $extra = " onClick=\"return check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //データ作成
        $this->makeData($objForm, $arg, $db, $model);

        //実行ボタンを作成する
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["btn_execute"] = knjCreateBtn($objForm, "btn_execute", "実 行", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "YEAR_ADD", $model->year_add);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje354aForm.html", $arg);
    }

    //
    function makeData(&$objForm, &$arg, $db, $model) {
        $setval = array();  //出力データ配列
        $getData = array();
        $getData = $this->setData();
        $disabled = "";
        for ($i = 0; $i < get_count($getData); $i++) {
            $query = knje354aQuery::cnt_table($getData[$i]["VALUE"]);
            $exist_flg = $db->getOne($query) > 0 ? true : false;
            if ($exist_flg) {
                $setval["KEKKA"]   = $db->getOne(knje354aQuery::getkekka($model->year, $model->year_add, $getData[$i]["VALUE"]));
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
        $setD[] = ( array("NAME" => "大学入試カレンダー",   "VALUE" => "COLLEGE_EXAM_CALENDAR"));

       return $setD;
    }
}
?>
