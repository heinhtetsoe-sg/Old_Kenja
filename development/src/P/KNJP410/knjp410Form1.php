<?php

require_once('for_php7.php');

/********************************************************************/
/* 授業料納入台帳                                   山城 2006/03/14 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjp410Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp410Form1", "POST", "knjp410index.php", "", "knjp410Form1");

        $opt=array();

        $arg["data"]["YEAR"] = CTRL_YEAR;

        //切替ラジオ（1:学年,2:クラス）
        $opt_output = array();
        $opt_output[0] = 1;
        $opt_output[1] = 2;

        if (!$model->output) {
            $model->output = 1;
        }

        $objForm->ae(array("type"      => "radio",
                            "name"      => "OUTPUT",
                            "value"     => $model->output,
                            "extrahtml" => "onclick =\" return btn_submit('knjp410');\"",
                            "multiple"  => $opt_output ));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT", 1);
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT", 2);

        if ($model->output == 1) {
            $arg["grade"] = $model->output;
        }
        if ($model->output == 2) {
            $arg["classno"] = $model->output;
        }

        /*--------------*/
        /* 終了月コンボ */
        /*--------------*/
        $month = array();

        $month[] = array('label' => "１２月" ,'value' => 12);
        $month[] = array('label' => "１月"   ,'value' => 1);
        $month[] = array('label' => "２月"   ,'value' => 2);
        $month[] = array('label' => "３月"   ,'value' => 3);

        if (!$model->month) {
            $model->month = $month[0]["value"];
        }

        $objForm->ae(array("type"      => "select",
                            "name"      => "MONTH",
                            "size"      => "1",
                            "value"     => "$model->month",
                            "options"   => isset($month)?$month:array()));

        $arg["data"]["MONTH"] = $objForm->ge("MONTH");

        $db = Query::dbCheckOut();
        if ($model->output != 1) {
            /*------------*/
            /* 学年コンボ */
            /*------------*/
            $grade = array();

            $result = $db->query(knjp410Query::getGrade());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $grade[] = array('label' => sprintf("%d", $row["NAME"]) ."学年",
                                 'value' => $row["CD"]);
            }
            $result->free();

            if (!$model->grade) {
                $model->grade = $grade[0]["value"];
            }

            $objForm->ae(array("type"      => "select",
                                "name"      => "GRADE",
                                "size"      => "1",
                                "value"     => "$model->grade",
                                "extrahtml" => "onchange =\" return btn_submit('knjp410');\"",
                                "options"   => isset($grade)?$grade:array()));

            $arg["data"]["GRADE"] = $objForm->ge("GRADE");
        }

        /*------------*/
        /* 一覧リスト */
        /*------------*/
        $row1 = array();

        if ($model->output == 1) {
            $query = knjp410Query::getGrade();
        }
        if ($model->output == 2) {
            $query = knjp410Query::getClass($model);
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
        Query::dbCheckIn($db);
        
        $objForm->ae(array("type"      => "select",
                            "name"      => "SELECT_NAME",
                            "extrahtml" => "multiple style=\"width=200px\" width=\"200px\" ondblclick=\"move1('left','up')\"",
                            "size"      => "20",
                            "options"   => isset($row1)?$row1:array()));

        $arg["data"]["SELECT_NAME"] = $objForm->ge("SELECT_NAME");

        //出力対象クラスリスト
        $objForm->ae(array("type"      => "select",
                            "name"      => "SELECT_SELECTED",
                            "extrahtml" => "multiple style=\"width=200px\" width=\"200px\" ondblclick=\"move1('right','up')\"",
                            "size"      => "20",
                            "options"   => array()));

        $arg["data"]["SELECT_SELECTED"] = $objForm->ge("SELECT_SELECTED");

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

        //中高判定フラグを作成する
        $db = Query::dbCheckOut();
        $row = $db->getOne(knjp410Query::getJorH());
        if ($row == 1) {
            $jhflg = 1;
        } else {
            $jhflg = 2;
        }
        Query::dbCheckIn($db);

        //CSV
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "ＣＳＶ出力", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "JHFLG", $jhflg);
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJP410");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp410Form1.html", $arg);
    }
}
