<?php

require_once('for_php7.php');

class knje450SubForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje450index.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knje450Query::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //作成年月日コンボ
        $opt = array();
        $opt[] = array('label' => "(( 新規 ))", 'value' => "");
        $value_flg = false;
        $query = knje450Query::getWritingDateList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => str_replace("-", "/", $row["WRITING_DATE"]),
                           'value' => $row["WRITING_DATE"]);
            if ($model->writing_date == $row["WRITING_DATE"]) $value_flg = true;
        }
        $result->free();

        if (!($model->writing_date && $value_flg)) {
            $model->writing_date = $opt[0]["value"];
            if (!isset($model->warning)) unset($model->field);
        }
        $extra = "onChange=\"return btn_submit('subform1');\"";
        $arg["data"]["WRITING_DATE"] = knjCreateCombo($objForm, "WRITING_DATE", $model->writing_date, $opt, $extra, 1);

        //作成日
        $arg["data"]["WRT_DATE"] = View::popUpCalendar($objForm, "WRT_DATE", $model->field["WRT_DATE"]);

        //項目一覧
        $list = array();
        $list["01"][1]  = "進級";
        $list["01"][2]  = "学習";
        $list["01"][3]  = "提出物"  ;
        $list["01"][4]  = "集団参加";
        $list["01"][5]  = "遅刻";
        $list["01"][6]  = "欠課";
        $list["01"][7]  = "登校";
        $list["01"][8]  = "対人関係・社会性";
        $list["01"][9]  = "コミュニケーション";
        $list["01"][10] = "その他";
        $list["10"][1]  = "国公立大学志望";
        $list["10"][2]  = "理系";
        $list["10"][3]  = "文系"  ;
        $list["10"][4]  = "私立大学";
        $list["10"][5]  = "理系";
        $list["10"][6]  = "文系";
        $list["10"][7]  = "専門学校";
        $list["10"][8]  = "就職";
        $list["10"][9]  = "その他";

        //定型文取得
        $tmp_data = array();
        $query = knje450Query::getAssessmentTempMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tmp_data[$row["DATA_DIV"]] = $row["REMARK"];
        }

        foreach ($model->assess as $key => $val) {
            //項目名
            $arg["data"][$key."_TITLE"] = $val["title"];

            if (isset($model->schregno) && !isset($model->warning) && $model->writing_date != "") {
                //アセスメント解答データ取得
                $query = knje450Query::getAssessmentAnsDat($model, $key);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //設問
                    for ($i=1; $i <= 15; $i++) {
                        $answer[$key."_QUESTION".$i] = $row["QUESTION".$i];
                    }
                    //備考
                    $answer[$key."_REMARK1"] = $row["REMARK1"];
                    $answer[$key."_REMARK2"] = $row["REMARK2"];
                }
            } else {
                $answer =& $model->field;
            }

            //備考入力
            if ($key == "01" || $key == "10" ) {
                $name = $key."_REMARK2";
                if ($key == "01") $extra = ($answer[$key."_QUESTION10"]) ? "" : "disabled";
                if ($key == "10") $extra = ($answer[$key."_QUESTION9"]) ? "" : "disabled";
                $arg["data"][$key."_REMARK"] = knjCreateTextBox($objForm, $answer[$name], $name, ($val["moji"] * 2), ($val["moji"] * 2), $extra);
            } else {
                $name = $key."_REMARK1";
                $value = $answer[$name];
                $height = $val["gyo"] * 13.5 + ($val["gyo"] - 1) * 3 + 5;
                $bgcolor = "#ffffff";

                //総合所見１or総合所見２ かつ データがnullのとき、定型文を表示（背景色あり）
                if (in_array($key, array("05", "06"))) {
                    $arg["data"][$key."_REMARK_TITLE"] = ($key == "05") ?  $tmp_data["01"] : $tmp_data["02"];
                }

                $arg["data"][$key."_REMARK"] = KnjCreateTextArea($objForm, $name, $val["gyo"], ($val["moji"] * 2 + 1), "soft", "style=\"height:{$height}px;background-color:{$bgcolor};\"", $value);
                $arg["data"][$key."_REMARK_COMMENT"] = "(全角{$val["moji"]}文字X{$val["gyo"]}行まで)";
            }

            //設問チェックボックス
            if ($val["check"] == "1") {
                if ($key == "01" || $key == "10") {
                    $question = "";
                    $sep = "";
                    foreach ($list[$key] as $cd => $label) {
                        $name = $key."_QUESTION".$cd;
                        $extra  = ($answer[$name] == "1") ? "checked" : "";
                        $extra .= " id=\"{$name}\"";
                        if (($key == "01" && $cd == "10") || ($key == "10" && in_array($cd, array("1", "4", "9")))) {
                            $div = (in_array($cd, array("1", "4"))) ? "check" : "text";
                            $extra .= " onclick=\"OptionUse(this, '".$div."');\"";
                        } else if ($key == "10" && in_array($cd, array("2", "3", "5", "6"))) {
                            if (in_array($cd, array("2", "3"))) $extra .= ($answer[$key."_QUESTION1"]) ? "" : "disabled";
                            if (in_array($cd, array("5", "6"))) $extra .= ($answer[$key."_QUESTION4"]) ? "" : "disabled";
                        }

                        //表示の調整
                        if ($key == "01") {
                            $question .= $sep.str_replace("\n", "", knjCreateCheckBox($objForm, $name, 1, $extra, "")."<label for=\"{$name}\">{$label}</label>");
                            $sep = ($cd == "8") ? "<br>" : "&nbsp;&nbsp;";
                        } else if ($key == "10") {
                            $question .= $sep.str_replace("\n", "", knjCreateCheckBox($objForm, $name, 1, $extra, "")."<label for=\"{$name}\">{$label}</label>");
                            if ($cd == "8") {
                                $sep = "<br>";
                            } else if ($cd == "1" || $cd == "4") {
                                $sep = "（";
                            } else if ($cd == "2" || $cd == "5") {
                                $sep = "&nbsp;";
                            } else if ($cd == "3" || $cd == "6") {
                                $sep = "）&nbsp;&nbsp;";
                            } else {
                                $sep = "&nbsp;&nbsp;";
                            }
                        }
                    }

                    $arg["data"][$key."_QUESTION"] = $question;

                } else {
                    $question = array();
                    $query = knje450Query::getAssessmentQMst($key);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $name = $key."_QUESTION".$row["ASSESS_CD"];
                        $extra  = ($answer[$name] == "1") ? "checked" : "";
                        $extra .= " id=\"{$name}\"";
                        $question[$key][] = str_replace("\n", "", knjCreateCheckBox($objForm, $name, 1, $extra, "")."<label for=\"{$name}\">{$row["QUESTION"]}</label>");
                    }

                    $arg["data"][$key."_QUESTION"] = (is_array($question[$key])) ? implode("<br>", $question[$key]) : $question[$key];
                }
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje450SubForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = (strlen($model->writing_date) > 0) ? "onclick=\"return btn_submit('subform1_update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = (strlen($model->writing_date) > 0) ? "onclick=\"return btn_submit('subform1_clear');\"" : "disabled";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");

    //追加ボタン
    $extra = "onclick=\"return btn_submit('subform1_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);

    //諸機関との連携歴等ボタン
    $extra = "style=\"height:30px;background:#ADFF2F;color:#006400;font:bold\" onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "諸機関との連携歴等", $extra);

    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "THIS_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "NEXT_YEAR", CTRL_YEAR+1);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJE451");
    knjCreateHidden($objForm, "STAFFCD", STAFFCD);
    knjCreateHidden($objForm, "SCHREG_SELECTED");
}
?>
