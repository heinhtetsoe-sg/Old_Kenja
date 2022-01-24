<?php

require_once('for_php7.php');


class knji092Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knji092Form1", "POST", "knji092index.php", "", "knji092Form1");

        //卒業年度
        $db = Query::dbCheckOut();
        $opt_year = array();
        $query = knji092Query::selectYear();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_year[]= array('label' => $row["YEAR"]."年度卒",
                               'value' => $row["YEAR"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($model->field["YEAR"] == "") $model->field["YEAR"] = CTRL_YEAR;     //初期値：現在年度をセット。

        $objForm->ae( array("type"       => "select",
                            "name"       => "YEAR",
                            "size"       => "1",
                            "extrahtml"  => "onChange=\"return btn_submit('knji092');\"",
                            "value"      => $model->field["YEAR"],
                            "options"    => $opt_year));

        $arg["data"]["YEAR"] = $objForm->ge("YEAR");

        //卒業見込み出力
        if ($model->field["MIKOMI"] == "on") {
            $mikomi1 = "checked";
        } else {
            $mikomi1 = "";
        }

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "MIKOMI",
                            "value"     => "on",
                            "extrahtml" => $mikomi1) );

        $arg["data"]["MIKOMI"] = $objForm->ge("MIKOMI");        //表紙

        //学期コード・学年数上限
        $db = Query::dbCheckOut();
        $query = knji092Query::selectGradeSemesterDiv($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
//NO001
//        $opt_Grade    = isset($row["GRADE_HVAL"])?$row["GRADE_HVAL"]:"03";//データ無しはデフォルトで３学年を設定
        $opt_Semester = isset($row["SEMESTERDIV"])?$row["SEMESTERDIV"]:"3";//データ無しはデフォルトで３学期を設定

        /* 学期コードをhiddenで送る。
         * 卒業年度が現在年度の場合：現在学期をセット。
         * 卒業年度が現在年度未満の場合：３学期をセット。
         */
        if ($model->field["YEAR"] == CTRL_YEAR) {
            $model->field["GAKKI"] = CTRL_SEMESTER;
        } else {
            $model->field["GAKKI"] = $opt_Semester;
        }

//NO001
        $query = knji092Query::selectMaxGrade($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opt_Grade  = isset($row["GRADE"])? $row["GRADE"] : "03"; //データ無しはデフォルトで３学年を設定
        $opt_Grade2 = $db->getOne(knji092Query::selectMaxGrade2()) == "1" ? "03" : "";
        Query::dbCheckIn($db);

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"      => $model->field["GAKKI"]
                            ) );
        //クラス一覧リスト作成する
        $db = Query::dbCheckOut();
        $query = knji092Query::getAuth($model, $opt_Grade, $opt_Grade2);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
                            "options"    => array()));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //表紙・名簿
        if ($model->field["OUTPUT1"] == "on") {
            $check1 = "checked";
        } else {
            $check1 = "";
        }
        if ($model->field["OUTPUT2"] == "on") {
            $check2 = "checked";
        } else {
            $check2 = "";
        }
        if ($model->cmd == "") $check2 = "checked";

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT1",
                            "value"     => "on",
                            "extrahtml" => $check1) );
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT2",
                            "value"     => "on",
                            "extrahtml" => $check2) );

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT1");      //表紙
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT2");      //名簿

        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する(必須)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJI092"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knji092Form1.html", $arg); 
    }
}
?>
