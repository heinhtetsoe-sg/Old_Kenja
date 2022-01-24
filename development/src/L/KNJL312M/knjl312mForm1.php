<?php

require_once('for_php7.php');

class knjl312mForm1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl312mForm1", "POST", "knjl312mindex.php", "", "knjl312mForm1");

        $opt=array();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $db = Query::dbCheckOut();
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl312mQuery::getApplicantdiv());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('knjl312m');\"";
        $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //試験科目
        $opt = array();
        $value_flg = false;
        $query = knjl312mQuery::getSubclassDetail($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $model->field["TESTPAPERCD"] = ($model->field["TESTPAPERCD"] && $value_flg) ? $model->field["TESTPAPERCD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["TESTPAPERCD"] = knjCreateCombo($objForm, "TESTPAPERCD", $model->field["TESTPAPERCD"], $opt, $extra, 1);

        //会場一覧
        $opt_list = array();
        $value_flg = false;
        $query = knjl312mQuery::getHallDat();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $s_receptno = preg_replace('/^0*/', '', $row["S_RECEPTNO"]);
            $e_receptno = preg_replace('/^0*/', '', $row["E_RECEPTNO"]);
            $opt_list[] = array('label' => $row["EXAMHALL_NAME"]. ' ' . make_space('aaa', $s_receptno) . $s_receptno . ' ～ ' . make_space('aaa', $e_receptno) . $e_receptno,
                                'value' => $row["VALUE"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //対象会場リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CATEGORY_SELECTED",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"",
                            "size"       => "25",
                            "options"    => array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("CATEGORY_SELECTED");

        //会場一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CATEGORY_NAME",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"",
                            "size"       => "25",
                            "options"    => isset($opt_list)?$opt_list:array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("CATEGORY_NAME");

        //対象取り消しボタンを作成する(個別)
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_right1",
                            "value"       => "　＞　",
                            "extrahtml"   => " onclick=\"move('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取り消しボタンを作成する(全て)
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_right2",
                            "value"       => "　≫　",
                            "extrahtml"   => " onclick=\"move('rightall');\"" ) );

        $arg["button"]["btn_right2"] = $objForm->ge("btn_right2");

        //対象選択ボタンを作成する(個別)
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_left1",
                            "value"       => "　＜　",
                            "extrahtml"   => " onclick=\"move('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //対象選択ボタンを作成する(全て)
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_left2",
                            "value"       => "　≪　",
                            "extrahtml"   => " onclick=\"move('leftall');\"" ) );

        $arg["button"]["btn_left2"] = $objForm->ge("btn_left2");

        //印刷ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->ObjYear
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJL312M"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl312mForm1.html", $arg);
    }
}
/****************************/
/* ネスト用のスペースの生成 */
/****************************/
function make_space($longer_name, $name) {
    $mojisuu_no_sa = strlen($longer_name) - strlen($name);
    for ($i = 0; $i < $mojisuu_no_sa; $i++) {
        $spaces .= '&nbsp;';
    }
    return $spaces;
}
?>
