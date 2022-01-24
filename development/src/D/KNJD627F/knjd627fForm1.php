<?php

require_once('for_php7.php');

class knjd627fForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        //ポストバック処理定義
        $postback = "onchange=\"return btn_submit('change');\"";

        //学期コンボ作成
        $query = knjd627fQuery::getSemester($model);
        $this->makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $postback, 1);

        //データ種別コンボ作成
        $query = knjd627fQuery::getDataDiv($model);
        $this->makeCmb($objForm, $arg, $db, $query, "PROFICIENCYDIV", $model->proficiencyDiv, $postback, 1);

        //テスト名称コンボ作成
        $query = knjd627fQuery::getProName($model);
        $list = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $list[] = array(
                'label' => $row["LABEL"],
                'value' => $row["VALUE"]
            );

            if ($model->proficiencyCd == "") {
                $model->proficiencyCd = $row["VALUE"];
            }

            if ($model->grade == "") {
                $model->grade = $row["GRADE"];
            }
        }
        $result->free();
        $arg["data"]["PROFICIENCYCD"] = knjCreateCombo($objForm, "PROFICIENCYCD", $model->proficiencyCd, $list, $postback, 1);

        //印刷対象選択ラジオボタン
        $values = array("1", "2");
        $extra = array();
        foreach ($values as $val) {
            array_push($extra, " id=\"PRINT_TARGET_KIND{$val}\"");

            //ラジオボタンラベルFor～指定
            $arg["radio"]["PRINT_TARGET_KIND{$val}_ID"] = "PRINT_TARGET_KIND{$val}";
        }
        $radioArray = knjCreateRadio($objForm, "PRINT_TARGET_KIND", $model->printTarget, $extra, $values, count($values));
        foreach ($radioArray as $name => $radio) {
            $arg["radio"]["{$name}"] = $radio;
        }

        //ボタン作成
        $arg["button"]["btn_outputcsv"] = knjCreateBtn($objForm, "btn_outputcsv", "CSV出力", " onclick=\"btn_submit('csvoutput');\"");
        $arg["button"]["btn_end"]       = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //プレビュー・印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //非表示項目
        knjCreateHidden($objForm, "cmd", $model->cmd);
        knjCreateHidden($objForm, "HID_EVENT_FROM");
        knjCreateHidden($objForm, "HID_YEAR", $model->year);
        knjCreateHidden($objForm, "HID_SCHOOLCD", $model->urlSchoolCd);
        knjCreateHidden($objForm, "HID_SCHOOLKIND", $model->isIndicateSchoolKind() ? $model->selectSchoolKind : "");
        knjCreateHidden($objForm, "HID_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "HID_GRADE", $model->grade);
        knjCreateHidden($objForm, "PRGID", $model->programID);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //HTML出力終了
        $arg["start"]  = $objForm->get_start("main", "POST", "knjd627findex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjd627fForm1.html", $arg);
    }

    //コンボ作成
    private function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
    {
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array(
                'label' => $row["LABEL"],
                'value' => $row["VALUE"],
                'grade' => $row["VALUE"]
            );

            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $result->free();

        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }
}
