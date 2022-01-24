<?php

require_once('for_php7.php');

class knjp122kForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp122kForm1", "POST", "knjp122kindex.php", "", "knjp122kForm1");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = CTRL_YEAR;

        //切替ラジオ（1:学年,2:クラス）
        $opt = array(1, 2);
        $model->output = ($model->output == "") ? "1" : $model->output;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\" onClick=\" return btn_submit('knjp122k')\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->output, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        if ($model->output == 1) {
            $arg["grade"] = $model->output;
        }
        if ($model->output == 2) {
            $arg["classno"] = $model->output;
        }

        //個人別
        $extra = " id=\"KOJIN_BETSU\" ";
        $extra .= $model->kojinBetsu == "1" ? " checked " : "";
        $arg["data"]["KOJIN_BETSU"] = knjCreateCheckBox($objForm, "KOJIN_BETSU", "1", $extra);

        if ($model->output != 1) {
            /*------------*/
            /* 学年コンボ */
            /*------------*/
            $grade = array();

            $result = $db->query(knjp122kQuery::getGrade());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $grade[] = array('label' => sprintf("%d", $row["NAME"]) ."学年",
                                 'value' => $row["CD"]);
            }
            $result->free();

            if (!$model->grade) {
                $model->grade = $grade[0]["value"];
            }

            $extra = "onchange =\" return btn_submit('knjp122k');\"";
            $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->grade, $grade, $extra, 1);
        }

        /*------------*/
        /* 一覧リスト */
        /*------------*/
        $row1 = array();
        if ($model->output == 1) {
            $query = knjp122kQuery::getGrade();
        }
        if ($model->output == 2) {
            $query = knjp122kQuery::getClass($model);
        }

        $result = $db->query($query);
        if ($model->output == 1) {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row1[] = array('label' => sprintf("%d", $row["NAME"]) ."学年",
                                'value' => $row["CD"]);
            }
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row1[] = array('label' => $row["NAME"],
                                'value' => $row["CD"]);
            }
        }
        $result->free();
        $extra = "multiple style=\"width:200px\" ondblclick=\"move1('left','up')\"";
        $arg["data"]["SELECT_NAME"] = knjCreateCombo($objForm, "SELECT_NAME", $value, $row1, $extra, 20);

        //出力対象クラスリスト
        $extra = "multiple style=\"width:200px\" ondblclick=\"move1('right','up')\"";
        $arg["data"]["SELECT_SELECTED"] = knjCreateCombo($objForm, "SELECT_SELECTED", $value, array(), $extra, 20);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right','up');\"";
        $arg["button"]["select_rights"] = knjCreateBtn($objForm, "select_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left','up');\"";
        $arg["button"]["select_lefts"] = knjCreateBtn($objForm, "select_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right','up');\"";
        $arg["button"]["select_right1"] = knjCreateBtn($objForm, "select_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left','up');\"";
        $arg["button"]["select_left1"] = knjCreateBtn($objForm, "select_left1", "＜", $extra);

        //CSV
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "ＣＳＶ出力", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJP122K");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp122kForm1.html", $arg);
    }
}
