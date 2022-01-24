<?php

require_once('for_php7.php');

class knjm812bForm1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm812bForm1", "POST", "knjm812bindex.php", "", "knjm812bForm1");

        $opt=array();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        //年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->control["年度"]
                            ) );

        if (is_numeric($model->control["学期数"])){
            //年度,学期コンボの設定
            for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ){
                $opt[]= array("label" => $model->control["学期名"][$i+1],
                              "value" => sprintf("%d", $i+1)
                             );
            }
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => isset($model->field["GAKKI"])?$model->field["GAKKI"]:$model->control["学期"],
                            "extrahtml"  => "onChange=\"return btn_submit('knjm812b');\"",
                            "options"    => $opt));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

        if(isset($model->field["GAKKI"])) {
            $ga = $model->field["GAKKI"];
        }
        else {
            $ga = $model->control["学期"];
        }
        
        //地域別･住所別コンボ
        $opt = array(1, 2);
        $model->field["LIST_TYPE"] = ($model->field["LIST_TYPE"] == "") ? "1" : $model->field["LIST_TYPE"];
        $extra = array("id=\"LIST_TYPE1\" onClick=\"return btn_submit('knjm812b')\"", "id=\"LIST_TYPE2\" onClick=\"return btn_submit('knjm812b')\"");
        $radioArray = knjCreateRadio($objForm, "LIST_TYPE", $model->field["LIST_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        if ($model->field["LIST_TYPE"] === '1') {
            $arg["AREA"] = "1";
        } else {
            $arg["ZIPCD"] = "1";
        }
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];

        //生徒選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $opt_left = array();
        $selectleft = $model->selectleft ? explode(",", $model->selectleft) : array();
        $selectleftval = explode(",", $model->selectleftval);
        $query = knjm812bQuery::getList($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row2[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //対象者リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"",
                            "size"       => "20",
                            "options"    => $opt_left));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"",
                            "size"       => "20",
                            "options"    => isset($row2)?$row2:array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


        //対象取り消しボタンを作成する(個別)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "　＞　",
                            "extrahtml"   => " onclick=\"move('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取り消しボタンを作成する(全て)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right2",
                            "value"       => "　≫　",
                            "extrahtml"   => " onclick=\"move('rightall');\"" ) );

        $arg["button"]["btn_right2"] = $objForm->ge("btn_right2");

        //対象選択ボタンを作成する(個別)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "　＜　",
                            "extrahtml"   => " onclick=\"move('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //対象選択ボタンを作成する(全て)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left2",
                            "value"       => "　≪　",
                            "extrahtml"   => " onclick=\"move('leftall');\"" ) );

        $arg["button"]["btn_left2"] = $objForm->ge("btn_left2");

        //印刷対象ラジオボタンを作成
        $opt2 = array(1, 2, 3, 4, 5);
        //$model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $disabled = " onclick=\"radioChange('this');\"";
        $extra = array("id=\"OUTPUT1\"onClick=\"return btn_submit('knjm812b')\"", "id=\"OUTPUT2\"onClick=\"return btn_submit('knjm812b')\"", "id=\"OUTPUT3\"onClick=\"return btn_submit('knjm812b')\"", "id=\"OUTPUT4\"onClick=\"return btn_submit('knjm812b')\"", "id=\"OUTPUT5\"onClick=\"return btn_submit('knjm812b')\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt2, get_count($opt2));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷対象ラジオボタンを作成
        $opt3 = array(1, 2);
        $model->field["OUTPUT2"] = ($model->field["OUTPUT2"] == "") ? "1" : $model->field["OUTPUT2"];
        $extra = array("id=\"OUTPUT21\"", "id=\"OUTPUT22\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT2", $model->field["OUTPUT2"], $extra, $opt3, get_count($opt3));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //生徒名チェックボックスを作成
        if ($model->field["OUTPUT"] !== "5") {
            $disabled = "";
        } else {
            $disabled = "disabled";
        }
        if ($model->field["CHECK1"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["CHECK1"] = knjCreateCheckBox($objForm, "CHECK1", "1", $extra.$disabled);

        //学籍番号チェックボックスを作成
        if ($model->field["OUTPUT"] !== "5") {
            $disabled = "disabled";
        } else {
            $disabled = "";
        }
        if ($model->field["CHECK2"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["CHECK2"] = knjCreateCheckBox($objForm, "CHECK2", "1", $extra.$disabled);

        //出力条件チェックボックス
        $extra = ($model->field["GRDDIV"] == "1") ? "checked" : "";
        $extra .= " id=\"GRDDIV\"";
        $arg["data"]["GRDDIV"] = knjCreateCheckBox($objForm, "GRDDIV", "1", $extra, "");

        //開始位置（行）コンボボックスを作成する///////////////////////////////////////////////////////////////////////
        $row = array(array('label' => "１行",'value' => 1),
                    array('label' => "２行",'value' => 2),
                    array('label' => "３行",'value' => 3),
                    array('label' => "４行",'value' => 4),
                    array('label' => "５行",'value' => 5),
                    array('label' => "６行",'value' => 6),
                    array('label' => "７行",'value' => 7),
                    array('label' => "８行",'value' => 8)
                    );

        $objForm->ae( array("type"       => "select",
                            "name"       => "POROW",
                            "size"       => "1",
                            "value"      => $model->field["POROW"],
                            "options"    => isset($row)?$row:array()));

        $arg["data"]["POROW"] = $objForm->ge("POROW");


        //開始位置（列）コンボボックスを作成する////////////////////////////////////////////////////////////////////////
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

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJM812B"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_DATE",
                            "value"     => CTRL_DATE
                            ) );

        //左のリストを保持
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectleft") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectleftval") );

        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);


        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm812bForm1.html", $arg);
    }
}
?>
