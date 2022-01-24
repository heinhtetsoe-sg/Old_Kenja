<?php

require_once('for_php7.php');

class knja195Form1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja195Form1", "POST", "knja195index.php", "", "knja195Form1");

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
            for ( $i = 0; $i < (int)$model->control["学期数"]; $i++ ){
                $opt[]= array("label" => $model->control["学期名"][$i+1],
                              "value" => sprintf("%d", $i+1)
                             );
            }
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => isset($model->field["GAKKI"])?$model->field["GAKKI"]:$model->control["学期"],
                            "extrahtml"  => "onChange=\"return btn_submit('knja195');\"",
                            "options"    => $opt));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

        if(isset($model->field["GAKKI"])) {
            $ga = $model->field["GAKKI"];
        }
        else {
            $ga = $model->control["学期"];
        }


        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knja195Query::getAuth($model->control["年度"],$ga);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        if(!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => " onChange=\"return btn_submit('change_class');\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");


        //生徒選択コンボボックスを作成する
        $opt_left = array();
        $selectleft = $model->selectleft ? explode(",", $model->selectleft) : array();
        $selectleftval = explode(",", $model->selectleftval);
        $query = knja195Query::getSchno($model,$model->control["年度"],$ga);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                                         'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
            if($model->cmd == 'change_class' ) {
                if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)){
                    $row2[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            } else {
                $row2[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
            }
        }

        //左リストで選択されたものを再セット
        if($model->cmd == 'change_class' ) {
            for ($i = 0; $i < get_count($selectleft); $i++) {
                $opt_left[] = array("label" => $selectleftval[$i],
                "value" => $selectleft[$i]);
            }
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

        //印刷対象ラジオボタンを作成する////////////////////////////////////////////////////////////////////////////////////
        $opt2[0]=1;
        $opt2[1]=2;
        $opt2[2]=3;
        $opt2[3]=4;

        if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;

        for ($i = 1; $i <= 4; $i++) {
            $name = "OUTPUT".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "OUTPUT",
                                "value"      => $model->field["OUTPUT"],
                                "extrahtml"  => "onclick=\" dischange();\" id=\"$name\"",    //NO001
                                "multiple"   => $opt2));

            $arg["data"][$name] = $objForm->ge("OUTPUT",$i);
        }

        //印刷対象ラジオボタンを作成する NO001
        $opt3[0]=1;
        $opt3[1]=2;

        if (!$model->field["OUTPUT2"]) $model->field["OUTPUT2"] = 1;

        for ($i = 1; $i <= 2; $i++) {
            $name = "OUTPUT2".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "OUTPUT2",
                                "value"      => $model->field["OUTPUT2"],
                                "extrahtml"  => "id=\"$name\"",
                                "multiple"   => $opt3));

            $arg["data"][$name] = $objForm->ge("OUTPUT2",$i);
        }

        //生徒名チェックボックスを作成する NO001
        if ($model->field["OUTPUT"] == 2){
            $abled = "disabled" ;
        } else {
            $abled = "" ;
        }

        if ($model->field["CHECK1"] == "1"){
            $check = "checked"  ;
        }else {
            $check = ""  ;
        }

        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "CHECK1",
                            "value"      => "1",
                            "extrahtml"  => "$abled"." $check"." id=\"CHECK1\"" ) );

        $arg["data"]["CHECK1"] = $objForm->ge("CHECK1");

        //出力条件チェックボックス
        $extra = ($model->field["GRDDIV"] == "1") ? "checked" : "";
        $extra .= " id=\"GRDDIV\"";
        $arg["data"]["GRDDIV"] = knjCreateCheckBox($objForm, "GRDDIV", "1", $extra, "");

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
                            "value"     => "KNJA195"
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
        View::toHTML($model, "knja195Form1.html", $arg);
    }
}
?>
