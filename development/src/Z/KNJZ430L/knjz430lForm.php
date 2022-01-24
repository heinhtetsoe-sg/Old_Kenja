<?php

require_once('for_php7.php');

class knjz430lform {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz430lindex.php", "", "right_list");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["YEAR"]        = $model->year;
        $arg["YEAR_ADD"]    = $model->year_add;

        //ALLチェック
        $extra = "onClick=\"return check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //データ作成
        $this->makeData($objForm, $arg, $db, $model);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["btn_execute"] = knjCreateBtn($objForm, "btn_execute", "実 行", $extra);

        //削除ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "YEAR_ADD", $model->year_add);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz430lForm.html", $arg);
    }

    //データ作成
    function makeData(&$objForm, &$arg, $db, $model) {
        $setval = array();  //出力データ配列
        $getData = array();
        $getData = $this->setData($model);
        $disabled = "";
        for ($i = 0; $i < get_count($getData); $i++) {
            //マスタ名
            $setval["MSTNAME"] = $getData[$i]["NAME"];

            //備考
            $tablelist = array();
            $tablelist[] = $getData[$i]["VALUE"];
            if ($getData[$i]["VALUE"] == "CERTIF_KIND_YDAT") $tablelist[] = "CERTIF_SCHOOL_DAT";
            $kekka = "";
            foreach($tablelist as $table) {
                //テーブル存在チェック
                $query = knjz430lQuery::cnt_table($table);
                $exist_flg = $db->getOne($query) > 0 ? true : false;
                if ($exist_flg) {
                    $kekka = ($kekka == "") ? $db->getOne(knjz430lQuery::getkekka($model->year, $model->year_add, $table)) : $kekka;
                } else {
                    $kekka = ($kekka == "") ? '今年度データなし' : $kekka;
                }
            }
            $setval["KEKKA"] = $kekka;

            //選択チェックボックス
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
    function setData($model) {
        $setD = array();
        $setD[] = ( array("NAME" => "学校マスタ",                          "VALUE" => "SCHOOL_MST"));
        $setD[] = ( array("NAME" => "課程マスタ",                          "VALUE" => "COURSE_YDAT"));
        $setD[] = ( array("NAME" => "学科マスタ",                          "VALUE" => "MAJOR_YDAT"));
        $setD[] = ( array("NAME" => "コースマスタ",                        "VALUE" => "COURSECODE_YDAT"));
        $setD[] = ( array("NAME" => "出身学校マスタ",                      "VALUE" => "FINSCHOOL_YDAT"));
        $setD[] = ( array("NAME" => "塾マスタ",                            "VALUE" => "PRISCHOOL_YDAT"));
        $setD[] = ( array("NAME" => "名称マスタ",                          "VALUE" => "NAME_YDAT"));
        $setD[] = ( array("NAME" => "証明書マスタ（証明書学校データ含む）","VALUE" => "CERTIF_KIND_YDAT"));

        return $setD;
    }
}
?>
