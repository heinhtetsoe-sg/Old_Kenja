<?php

require_once('for_php7.php');

class knjf303_schregForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjf303_schregForm1", "POST", "knjf303_schregindex.php", "", "knjf303_schregForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->getfield["YEAR"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->getfield["YEAR"],
                            ) );

        //現在の学期コードを送る（hidden）
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => $model->control["学期"],
                            ) );

        //集計区分表示
        if ($model->total_div == "1") {
            $arg["data"]["TOTAL_DIV"] = '出席停止';
        } else if ($model->total_div == "2") {
            $arg["data"]["TOTAL_DIV"] = '欠席';
        } else if ($model->total_div == "3") {
            $arg["data"]["TOTAL_DIV"] = '登校';
        }

        //クラス選択コンボボックスを作成する
        $query = knjf303_schregQuery::getAuth($model->getfield["YEAR"], $model->control["学期"], $model);

        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if(!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjf303_schreg');\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //対象者リストを作成する
        $db = Query::dbCheckOut();
        $query = knjf303_schregQuery::getList($model);

        $result = $db->query($query);

        $opt1 = array();
        $opt2 = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knjf303_schregQuery::getAddtion3SchregDatCnt($model, $row["SCHREGNO"]);
            $cnt = get_count($db->getCol($query));
            //if (!in_array($row["SCHREGNO"],$model->select_data["selectdata"])) {
            if (0 < $cnt) {
                $opt2[]= array('label' => $row["NAME"],
                               'value' => $row["SCHREGNO"].'-'.$row["ATTENDNO"]);
            } else {
                $opt1[]= array('label' => $row["NAME"],
                               'value' => $row["SCHREGNO"].'-'.$row["ATTENDNO"]);
            }
        }

        /*if ($model->select_data["selectdata"][0]) {
            for ($i = 0; $i < get_count($model->select_data["selectdata"]); $i++) {
                $query = knjf303_schregQuery::getList2($model->select_data["selectdata"][$i], $model);
                $seitoData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $opt2[]= array('label' => $seitoData["NAME"],
                               'value' => $seitoData["SCHREGNO"]);
            }
        }*/

        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:280px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:280px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
                            "options"    => isset($opt2)?$opt2:array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //更新ボタン
        $extra = "onclick=\"return doSubmit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        
        //戻るボタン
        $link = REQUESTROOT."/F/KNJF303/knjf303index.php?cmd=back&SEND_PRGRID=KNJF303_SCHREG&SEND_YEAR={$model->getfield["YEAR"]}&SEND_DATA_DIV={$model->getfield["DATA_DIV"]}";
        $extra = "onclick=\"document.location.href='$link';\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hiddenを作成する(必須)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJF303_SCHREG"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );

        //今年度---NO001
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_YEAR",
                            "value"     => CTRL_YEAR
                            ) );

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf303_schregForm1.html", $arg); 
    }
}
?>
