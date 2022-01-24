<?php

require_once('for_php7.php');


class knja223mForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja223mForm1", "POST", "knja223mindex.php", "", "knja223mForm1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR,
                            ) );

        //学期コードをhiddenで送る
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => CTRL_SEMESTER,
                            ) );

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->schoolName == 'sagaken') {
            $arg["is_sagatsushin"] = 1;
            //出力順ラジオボタンを作成
            $opt = array(1, 2);     // 1:ふりがな 2:学籍番号
            $model->field["KANA_OUTTYPE"] = ($model->field["KANA_OUTTYPE"] == "") ? "1" : $model->field["KANA_OUTTYPE"];
            $extra = array("id=\"KANA_OUTTYPE1\"", "id=\"KANA_OUTTYPE2\"");
            $radioArray = knjCreateRadio($objForm, "KANA_OUTTYPE", $model->field["KANA_OUTTYPE"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        }else{
            $arg["is_not_sagatsushin"] = 1;
        }

        //クラス一覧リスト作成する
        $query = knja223mQuery::getAuth($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

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

        //性別区分チェックボタンを作成
        $extra = "checked id=\"MARK\"";
        $arg["data"]["MARK"] = knjCreateCheckBox($objForm, "MARK", "on", $extra, "");

        //名票ラジオボタンを作成（全学年用/学級用枠あり/学級用枠なし）
        $opt[0]=1;
        $opt[1]=2;

        for ($i = 1; $i <= 2; $i++) {
            $name = "OUTPUT".($i);
            $objForm->ae( array("type"       => "radio",
                                "name"       => "OUTPUT",
                                "value"      => isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : 1,
                                "extrahtml"  => "id=\"$name\"",
                                "multiple"   => $opt));

            $arg["data"][$name] = $objForm->ge("OUTPUT",$i);
        }


        //出力件数テキストボックス
        $objForm->ae( array("type"        => "text",
                            "name"        => "KENSUU",
                            "size"        => 3,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => isset($model->field["KENSUU"])?$model->field["KENSUU"]:1 ));
        $arg["data"]["KENSUU"] = $objForm->ge("KENSUU");


        //印刷ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "', '', '', '');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA223M");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja223mForm1.html", $arg); 
    }
}
?>
