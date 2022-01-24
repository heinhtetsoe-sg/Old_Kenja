<?php

require_once('for_php7.php');

class knja062aForm1
{
    public function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm      = new form();
        $arg["start"] = $objForm->get_start("list", "POST", "knja062aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //ラジオボタン 1:複式クラス 2:法定クラス
        $opt_shubetsu = array(1, 2);
        $model->fields["CLASS_RADIO"] = ($model->fields["CLASS_RADIO"]) ? $model->fields["CLASS_RADIO"] : "1";
        $extra = array();
        foreach ($opt_shubetsu as $key => $val) {
            array_push($extra, " id=\"CLASS_RADIO{$val}\" onClick=\"btn_submit('changeRadio')\"");
        }
        $radioArray = knjCreateRadio($objForm, "CLASS_RADIO", $model->fields["CLASS_RADIO"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["radio"][$key] = $val;
        }

        //--------------最終学期の場合の制御    start----
        /* 最終学期の場合、次の年度の処理が行える */

        //最終学期かを判定
        $year_control = (trim(CTRL_SEMESTER) == trim($model->control["学期数"]))? true : false ;

        //変数をコントロール
        if ($year_control) {
            if ($model->fields["CLASS_RADIO"] == '2') {
                $ctrl_year = CTRL_YEAR;    //年度はログイン年度
                $select_ctrl = CTRL_SEMESTER;   //選択されている学期をログイン学期に
                $semester_ctrl = CTRL_SEMESTER; //選択されている学期をログイン学期に
            } else {
                $ctrl_year = CTRL_YEAR + 1 ;    //年度を1年先に
                $select_ctrl = 1;               //選択されている学期を1学期に
                $semester_ctrl = 1;             //選択されている学期を1学期に
            }
        } else {
            $ctrl_year = CTRL_YEAR;
            $select_ctrl = CTRL_SEMESTER + 1;   //選択されている学期を1学期先に
            $semester_ctrl = CTRL_SEMESTER + 1; //選択されている学期を1学期先に
        }

        //年度コンボボックス
        for ($i = 0; $i < $model->control["学期数"]; $i++) {
            $opt[$i] = array("label" => $ctrl_year."年度  ".$model->control["学期名"][$i+1],
                             "value" => $ctrl_year."-".($i+1));
        }

        //対象年度コンボボックス
        if (!isset($model->term)) {
            $semester    = $select_ctrl;
            $model->term = $ctrl_year. "-" .$semester;
            $selected    = $select_ctrl ;
        }
        if ($model->cmd == "changeRadio") {
            $model->term = $ctrl_year. "-" .$select_ctrl;
        }
        $objForm->ae(array("type"      => "select",
                            "name"      => "term",
                            "size"      => "1",
                            "extrahtml" => "onChange=\"btn_submit('list')\"",
                            "value"     => $model->term,
                            "options"   => $opt));
        $arg["term"] = $objForm->ge("term");

        $objForm->ae(array("type"      =>    "button",
                            "name"      =>    "btn_copy",
                            "value"     =>    "左の学期のデータをコピー",
                            "extrahtml" =>    "onClick=\"return btn_submit('copy');\""));
        $arg["btn_copy"] = $objForm->ge("btn_copy");

        //参照年度コンボボックス
        for ($i = 0; $i < $model->control["学期数"]; $i++) {
            $opt2[$i] = array("label" => CTRL_YEAR."年度  ".$model->control["学期名"][$i+1],
                              "value" => CTRL_YEAR."-".($i+1));
        }
        if (!isset($model->term2)) {
            $model->term2 = CTRL_YEAR . "-" . CTRL_SEMESTER;
        }
        $objForm->ae(array("type"      => "select",
                            "name"      => "term2",
                            "size"      => "1",
                            "value"     => $model->term2,
                            "options"   => $opt2));
        $arg["term2"] = $objForm->ge("term2");

        //生徒項目名切替処理
        $arg["SCH_LABEL"] = $model->sch_label;

        //生徒もコピーチェックボックス
        $extra  = ($model->check == "1" || $model->defFlg == "on") ? "checked" : "";
        $extra .= " id=\"check\"";
        $extra .= (substr($model->term, 0, 4) == substr($model->term2, 0, 4)) ? "" : " disabled";
        $extra .= " onclick=\"OptionUse('this');\"";
        $arg["btn_check"] = knjCreateCheckBox($objForm, "check", "1", $extra, "");

        //除籍者も含むチェックボックス
        $extra  = ($model->grd_div == "1" || $model->defFlg == "on") ? "checked" : "";
        $extra .= " id=\"grd_div\"";
        $extra .= ($model->check == "1" || $model->defFlg == "on") ? "" : " disabled";
        $arg["btn_grddiv"] = knjCreateCheckBox($objForm, "grd_div", "1", $extra, "");

        $model->defFlg = "off";

        //ＨＲクラス一覧
        $result = $db->query(knja062aquery::selectList($model, $model->term));
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $row["URL"] = View::alink(
                "knja062aindex.php",
                $row["HR_CLASS"],
                "target=right_frame",
                array("cmd"        => "edit",
                                              "GRADE"       => $row["GRADE"],
                                              "HR_CLASS"    => $row["HR_CLASS"],
                                              "term"        => $model->term)
            );
            if ($row["RECORD_DIV"] === '1') {
                $row["RECORD_DIV_NAME"] = '健常者';
            } else {
                $row["RECORD_DIV_NAME"] = 'その他';
            }
            $row["backcolor"] = ($i%2 == 0) ? "#ffffff" : "#ccffcc";  //#ccffff
            $arg["data"][] = $row;
            $i++;
        }
        $result->free();

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (($model->cmd == "list" || $model->cmd == "changeRadio") && VARS::get("ed") != "1") {
            $arg["reload"] = "window.open('knja062aindex.php?cmd=edit&init=1','right_frame');";
        }

        View::toHTML($model, "knja062aForm1.html", $arg);
    }
}
