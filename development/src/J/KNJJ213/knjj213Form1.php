<?php

require_once('for_php7.php');

class knjj213Form1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjj213Form1", "POST", "knjj213index.php", "", "knjj213Form1");

        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //リスト作成
        makeList($arg, $objForm, $db, $model);

        //開始位置（行）コンボボックスを作成する
        $row = array(array('label' => "１行",'value' => 1),
                     array('label' => "２行",'value' => 2),
                     array('label' => "３行",'value' => 3),
                     array('label' => "４行",'value' => 4),
                     array('label' => "５行",'value' => 5),
                     array('label' => "６行",'value' => 6)
                     );

        $objForm->ae( array("type"       => "select",
                            "name"       => "POROW",
                            "size"       => "1",
                            "value"      => $model->field["POROW"],
                            "options"    => isset($row)?$row:array()));

        $arg["data"]["POROW"] = $objForm->ge("POROW");


        //開始位置（列）コンボボックスを作成する
        $col = array(array('label' => "１列",'value' => 1),
                     array('label' => "２列",'value' => 2),
                     array('label' => "３列",'value' => 3),
                     );

        $objForm->ae( array("type"       => "select",
                            "name"       => "POCOL",
                            "size"       => "1",
                            "value"      => $model->field["POCOL"],
                            "options"    => isset($col)?$col:array()));

        $arg["data"]["POCOL"] = $objForm->ge("POCOL");

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJJ213");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectleft");
        knjCreateHidden($objForm, "selectleftval");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj213Form1.html", $arg);
    }
}

function makeList(&$arg, $objForm, $db, $model) {

    //クラス一覧リスト
    $row1 = $opt_left = array();
    $value_flg = false;
    $max_len = 0;
    $query = knjj213Query::getEvaluationCommittee();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //対象者リストを作成する
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", $val, $opt_left, $extra, 15);

    //生徒一覧リストを作成する
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", $val, $row1, $extra, 15);

    //対象取り消しボタンを作成する(個別)
    $extra = "onclick=\"move('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "　＞　", $extra);

    //対象取り消しボタンを作成する(全て)
    $extra = "onclick=\"move('rightall');\"";
    $arg["button"]["btn_right2"] = knjCreateBtn($objForm, "btn_right2", "　≫　", $extra);

    //対象選択ボタンを作成する(個別)
    $extra = "onclick=\"move('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "　＜　", $extra);

    //対象選択ボタンを作成する(全て)
    $extra = "onclick=\"move('leftall');\"";
    $arg["button"]["btn_left2"] = knjCreateBtn($objForm, "btn_left2", "　≪　", $extra);
}
?>
