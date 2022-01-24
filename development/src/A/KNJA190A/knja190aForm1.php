<?php

require_once('for_php7.php');

class knja190aForm1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja190aForm1", "POST", "knja190aindex.php", "", "knja190aForm1");

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
                            "extrahtml"  => "onChange=\"return btn_submit('knja190a');\"",
                            "options"    => $opt));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

        if(isset($model->field["GAKKI"])) {
            $ga = $model->field["GAKKI"];
        }
        else {
            $ga = $model->control["学期"];
        }

        //出力指定選択 (1:個人指定 2:クラス指定)
        $opt = array(1, 2);
        $model->field["CHOICE"] = ($model->field["CHOICE"] == "") ? "1" : $model->field["CHOICE"];
        $click = " onClick=\"return btn_submit('knja190a');\"";
        $extra = array("id=\"CHOICE1\"".$click, "id=\"CHOICE2\"".$click);
        $radioArray = knjCreateRadio($objForm, "CHOICE", $model->field["CHOICE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //クラス切替
        if ($model->cmd == "hukusiki") {
            $model->select_opt = array();
        }
        $opt = array(1, 2);
        $model->field["HUKUSIKI_KIRIKAE"] = ($model->field["HUKUSIKI_KIRIKAE"] == "") ? "1" : $model->field["HUKUSIKI_KIRIKAE"];
        $extra = array("id=\"HUKUSIKI_KIRIKAE1\" onclick=\"return btn_submit('hukusiki');\"", "id=\"HUKUSIKI_KIRIKAE2\" onclick=\"return btn_submit('hukusiki');\"");
        $radioArray = knjCreateRadio($objForm, "HUKUSIKI_KIRIKAE", $model->field["HUKUSIKI_KIRIKAE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        //実クラス
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $arg["HUKUSIKI_KIRIKAE2_HYOUJI"] = "1";
            $arg["data"]["HUKUSIKI_KIRIKAE2_NAME"] = '実クラス';
        //FI複式クラス
        } else if ($model->Properties["useFi_Hrclass"] == '1') {
            $arg["HUKUSIKI_KIRIKAE2_HYOUJI"] = "1";
            $arg["data"]["HUKUSIKI_KIRIKAE2_NAME"] = 'FI複式クラス';
        }

        //クラス一覧リスト
        $db = Query::dbCheckOut();
        $row1 = array();
        $value_flg = false;
        $max_len = 0;
        $query = knja190aQuery::getAuth($model, $model->control["年度"],$ga);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $value_flg = true;

            if ($model->field["CHOICE"] == "1") {
                //年組のMAX文字数取得
                $zenkaku = (strlen($row["LABEL"]) - mb_strlen($row["LABEL"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["LABEL"]) - $zenkaku : mb_strlen($row["LABEL"]);
                $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
            }
        }

        //1:個人表示指定用
        $opt_left = array();
        if ($model->field["CHOICE"] == "1") {
            if(!isset($model->field["GRADE_HR_CLASS"]) || !$value_flg) {
                $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            }

            $objForm->ae( array("type"       => "select",
                                "name"       => "GRADE_HR_CLASS",
                                "size"       => "1",
                                "value"      => $model->field["GRADE_HR_CLASS"],
                                "extrahtml"  => " onChange=\"return btn_submit('change_class');\"",
                                "options"    => $row1));

            $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

            $row1 = array();
            //生徒単位
            $selectleft = $model->selectleft ? explode(",", $model->selectleft) : array();
            $selectleftval = explode(",", $model->selectleftval);
            $query = knja190aQuery::getSchno($model,$model->control["年度"],$ga);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //クラス名称調整
                $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
                $len = $zenkaku * 2 + $hankaku;
                $hr_name = $row["HR_NAME"];
                for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

                $model->select_opt[$row["SCHREGNO"]] = array("label" => $hr_name."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                                             'value' => $row["SCHREGNO"]."-".$row["GRADE_HR_CLASS"].$row["ATTENDNO"]);
                if($model->cmd == 'change_class' ) {
                    if (!in_array($row["SCHREGNO"]."-".$row["GRADE_HR_CLASS"].$row["ATTENDNO"], $selectleft)){
                        $row1[] = array('label' => $hr_name."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                        'value' => $row["SCHREGNO"]."-".$row["GRADE_HR_CLASS"].$row["ATTENDNO"]);
                    }
                } else {
                    $row1[] = array('label' => $hr_name."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE_HR_CLASS"].$row["ATTENDNO"]);
                }
            }
            //左リストで選択されたものを再セット
            if($model->cmd == 'change_class' ) {
                for ($i = 0; $i < get_count($selectleft); $i++) {
                    $opt_left[] = array("label" => $selectleftval[$i],
                    "value" => $selectleft[$i]);
                }
            }
        }

        $result->free();
        Query::dbCheckIn($db);

        $chdt = $model->field["CHOICE"];

        //対象者リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right',$chdt)\"",
                            "size"       => "15",
                            "options"    => $opt_left));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left',$chdt)\"",
                            "size"       => "15",
                            "options"    => $row1));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


        //対象取り消しボタンを作成する(個別)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "　＞　",
                            "extrahtml"   => " onclick=\"move('right',$chdt);\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取り消しボタンを作成する(全て)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right2",
                            "value"       => "　≫　",
                            "extrahtml"   => " onclick=\"move('rightall',$chdt);\"" ) );

        $arg["button"]["btn_right2"] = $objForm->ge("btn_right2");

        //対象選択ボタンを作成する(個別)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "　＜　",
                            "extrahtml"   => " onclick=\"move('left',$chdt);\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //対象選択ボタンを作成する(全て)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left2",
                            "value"       => "　≪　",
                            "extrahtml"   => " onclick=\"move('leftall',$chdt);\"" ) );

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

        if ($model->field["CHECK1"] == "on"){
            $check = "checked"  ;
        }else {
            $check = ""  ;
        }

        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "CHECK1",
                            "value"      => "on",
                            "extrahtml"  => "$abled"." $check"." id=\"CHECK1\"" ) );

        $arg["data"]["CHECK1"] = $objForm->ge("CHECK1");

        //学籍番号チェックボックスを作成する NO001

        if ($model->field["OUTPUT"] == 2){
            $abled = "" ;
        }else {
            $abled = "disabled" ;
        }

        if ($model->field["CHECK2"] == "on"){
            $check = "checked"  ;
        }else {
            $check = ""  ;
        }

        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "CHECK2",
                            "value"      => "on",
                            "extrahtml"  => "$abled"." $check"." id=\"CHECK2\"" ) );

        $arg["data"]["CHECK2"] = $objForm->ge("CHECK2");

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
                            "value"     => "KNJA190A"
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
        knjCreateHidden($objForm, "useSpecial_Support_Hrclass" , $model->Properties["useSpecial_Support_Hrclass"]);
        knjCreateHidden($objForm, "useFi_Hrclass" , $model->Properties["useFi_Hrclass"]);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja190aForm1.html", $arg);
    }
}
?>
