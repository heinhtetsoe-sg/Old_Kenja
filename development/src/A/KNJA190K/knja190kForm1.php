<?php

require_once('for_php7.php');

class knja190kForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja190kForm1", "POST", "knja190kindex.php", "", "knja190kForm1");

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
                            "extrahtml"  => "onChange=\"return btn_submit('knja190k');\"",
                            "options"    => $opt));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

        if(isset($model->field["GAKKI"])) {
            $ga = $model->field["GAKKI"];
        }
        else {
            $ga = $model->control["学期"];
        }

        //1:個人,2:クラス表示指定
        $opt[0]=1;
        $opt[1]=2;

        if (!$model->field["DISP"]) $model->field["DISP"] = 1;

        $objForm->ae( array("type"       => "radio",
                            "name"       => "DISP",
                            "value"      => $model->field["DISP"],
                            "extrahtml"     => "onclick =\" return btn_submit('knja190k');\"",
                            "multiple"   => $opt));

        $arg["data"]["DISP1"] = $objForm->ge("DISP",1);
        $arg["data"]["DISP2"] = $objForm->ge("DISP",2);

        if ($model->field["DISP"] == 1) $arg["schno"] = $model->field["DISP"];
        if ($model->field["DISP"] == 2) $arg["clsno"] = $model->field["DISP"];

        //クラス一覧リスト
        $db = Query::dbCheckOut();
        $row1 = array();
        $class_flg = false;
        $max_len = 0;
        $query = common::getHrClassAuth($model->control["年度"],$ga,AUTHORITY,STAFFCD);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;

            if ($model->field["DISP"] == 1) {
                //年組のMAX文字数取得
                $zenkaku = (strlen($row["LABEL"]) - mb_strlen($row["LABEL"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["LABEL"]) - $zenkaku : mb_strlen($row["LABEL"]);
                $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
            }
        }

        //1:個人表示指定用
        $opt_left = array();
        if ($model->field["DISP"] == 1) {
            if ($model->field["GRADE_HR_CLASS"]=="" || !$class_flg) $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

            $objForm->ae( array("type"       => "select",
                                "name"       => "GRADE_HR_CLASS",
                                "size"       => "1",
                                "value"      => $model->field["GRADE_HR_CLASS"],
                                "extrahtml"  => "onChange=\"return btn_submit('change_class');\"",
                                "options"    => $row1));

            $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

            $row1 = array();
            //生徒単位
            $selectleft = explode(",", $model->selectleft);
            $query = knja190kQuery::getSchno($model,$model->control["年度"],$ga);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //クラス名称調整
                $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
                $len = $zenkaku * 2 + $hankaku;
                $hr_name = $row["HR_NAME"];
                for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

                $model->select_opt[$row["SCHREGNO"]] = array("label" => $hr_name."　".$row["ATTENDNO"]."番　".$row["NAME"], 
                                                             "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

                if($model->cmd == 'change_class' ) {
                    if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)){
                        $row1[] = array('label' => $hr_name."　".$row["ATTENDNO"]."番　".$row["NAME"],
                                        'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                    }
                } else {
                    $row1[] = array('label' => $hr_name."　".$row["ATTENDNO"]."番　".$row["NAME"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            }
            //左リストで選択されたものを再セット
            if($model->cmd == 'change_class' ) {
                foreach ($model->select_opt as $key => $val){
                    if (in_array($key, $selectleft)) {
                        $opt_left[] = $val;
                    }
                }
            }
        }

        $result->free();
        Query::dbCheckIn($db);

        $chdt = $model->field["DISP"];

        //対象者リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right',$chdt)\"",
                            "size"       => "20",
                            "options"    => $opt_left));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left',$chdt)\"",
                            "size"       => "20",
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

        //印刷対象ラジオボタンを作成する
        $opt2[0]=1;
        $opt2[1]=2;
        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUT",
                            "value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:"1",
                            "multiple"   => $opt2));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

        //出力順
        $opt3[0]=1;
        $opt3[1]=2;
        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUT2",
                            "value"      => isset($model->field["OUTPUT2"])?$model->field["OUTPUT2"]:"2",//順番入れ替えのため初期値を2と変更
                            "multiple"   => $opt3));

        $arg["data"]["OUTPUT21"] = $objForm->ge("OUTPUT2",1);
        $arg["data"]["OUTPUT22"] = $objForm->ge("OUTPUT2",2);

        //開始位置（行）コンボボックスを作成する
        $row = array(array('label' => "１行",'value' => 1),
                     array('label' => "２行",'value' => 2),
                     array('label' => "３行",'value' => 3),
                     array('label' => "４行",'value' => 4),
                     array('label' => "５行",'value' => 5),
                     array('label' => "６行",'value' => 6),
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
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJA190K"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //左のリストを保持
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectleft") );  

        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja190kForm1.html", $arg); 
    }
}
?>
