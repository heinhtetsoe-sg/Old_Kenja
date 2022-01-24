<?php

require_once('for_php7.php');


class knja230Form1
{
    function main(&$model) {

        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knja230index.php", "", "main");


        //読込ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_toukei",
                            "value"       => "･･･",
                            "extrahtml"   => "onclick=\"wopen('../../X/KNJXTOKE3/knjxtoke3index.php?DISP=CLASS&PROGRAMID=$model->programid','KNJXTOKE3',0,0,900,550);\"") );
            
        $arg["explore"] = $objForm->ge("btn_toukei");

        //学習記録エクスプローラー
        if ($model->cmd != "toukei") {
            $arg["ONLOAD"] = "wopen('../../X/KNJXTOKE3/knjxtoke3index.php?DISP=CLASS&PROGRAMID=$model->programid','KNJXTOKE3',0,0,900,550);";
        }

        //$cd =& $model->attendclasscd;
        //$cd_name = "ATTENDCLASSCD";

        $cd =& $model->subclasscd;
        if (isset($cd)){ 
            $db = Query::dbCheckOut();
            $query = knja230Query::SQLGet_Main($model);

            $i=0;
            //教科、科目、クラス取得
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $title = "[" . $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]."　".htmlspecialchars($row["CLASSNAME"]) ."]";
                } else {
                    $title = "[" . $row["CLASSCD"]."　".htmlspecialchars($row["CLASSNAME"]) ."]";
                }

                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    //表示用
                    $arg["usecurriculumcd"] = 1;
                    $subclasscd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
                } else {
                    $arg["Nocurriculumcd"] = 1;
                    $subclasscd = $row["SUBCLASSCD"];
                }
                $checked = (is_array($model->checked_attend) && in_array($row["ATTENDCLASSCD"], $model->checked_attend))? true:false;
                if($checked==0) {
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $objForm->add_element(array("type"      => "checkbox",
                                                    "name"      => "chk",
                                                    "checked"   => $checked,
                                                    "value"     => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"].",".$row["STAFFCD"].",".$row["CHARGEDIV"].",".$row["APPDATE"],//2004/07/27 nakamoto
                                                    "extrahtml" => "multiple" ));
                    } else {
                        $objForm->add_element(array("type"      => "checkbox",
                                                    "name"      => "chk",
                                                    "checked"   => $checked,
                                                    "value"     => $row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"].",".$row["STAFFCD"].",".$row["CHARGEDIV"].",".$row["APPDATE"],//2004/07/27 nakamoto
                                                    "extrahtml" => "multiple" ));
                    }
                    
                    $row["CHECK"] = $objForm->ge("chk");

                    $start = str_replace("-","/",$row["STARTDAY"]);
                    $end = str_replace("-","/",$row["ENDDAY"]);
                //学籍処理範囲外の場合背景色を変える
                    if ((strtotime($model->control["学籍処理日"]) < strtotime($start)) ||
                        (strtotime($model->control["学籍処理日"]) > strtotime($end))) {
                        $row["BGCOLOR"] = "#ccffcc";
                    } else {
                        $row["BGCOLOR"] = "#ffffff";
                    }
                    $row["TERM"] = $start ."～" .$end;
        //2004/06/30 nakamoto-------------------------------------
                    if($row["CHARGEDIV"] == 1) {
                        $row["CHARGEDIV"] = ' ＊';
                    }
                    else {
                        $row["CHARGEDIV"] = ' ';
                    }
                    $row["APPDATE"] = str_replace("-","/",$row["APPDATE"]);         //2004-08/07 naka
                    $arg["data"][] = $row; 
                }
                $i++;
                if ($i == 1) {
                    $arg["data1"][] = $row; 
                }
            }
            Query::dbCheckIn($db);
        }
        $objForm->add_element(array("type"      => "checkbox",
                                    "name"      => "chk_all",
                                    "extrahtml" => "onClick=\"return check_all();\"" ));  

        $arg["CHECK_ALL"] = $objForm->ge("chk_all");

        //名票ラジオボタンを作成（全学年用/学級用枠あり/学級用枠なし）
        $opt[0]=1;
        $opt[1]=2;
        $opt[2]=3;
        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUT",
                            "value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:1,
                            "multiple"   => $opt));

        $arg["OUTPUT1"] = $objForm->ge("OUTPUT",1);
        $arg["OUTPUT2"] = $objForm->ge("OUTPUT",2);
        $arg["OUTPUT3"] = $objForm->ge("OUTPUT",3);


        //出力件数テキストボックス
        $objForm->ae( array("type"        => "text",
                            "name"        => "KENSUU",
                            "size"        => 3,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => isset($model->field["KENSUU"])?$model->field["KENSUU"]:1 ));
        $arg["KENSUU"] = $objForm->ge("KENSUU");


        //プレビューボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ok",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return opener_submit('" . SERVLET_URL . "');\"" ) );

        $arg["btn_ok"] = $objForm->ge("btn_ok");


        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_can",
                            "value"       => " 終 了 ",
                            "extrahtml"   => "onClick=\"closeWin();\"" ));

        $arg["btn_can"] = $objForm->ge("btn_can");


        //タイトル
        $arg["TITLE"] = $title;


        //年度・学期（表示）
        if (($model->year != "") && ($model->semester != "")) {
            $arg["YEAR_SEMESTER"] = $model->year."年度&nbsp;" .$model->control["学期名"][$model->semester]."&nbsp;";
        }


        //hiddenを作成する
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJA230"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->year,
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => $model->semester,
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CLASSCD",
                            "value"     => $model->classcd,
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUBCLASSCD",
                            "value"     => $subclasscd,
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTKINDCD",
                            "value"     => $model->testkindcd,
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTITEMCD",
                            "value"     => $model->testitemcd,
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTENDCLASSCD"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GROUPCD"
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DISP"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //科目担任名
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "NAME_SHOW"
                            ) );
        //担任区分2004/07/27 nakamoto
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CHARGEDIV"
                            ) );
        //2004/08/07 nakamoto
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "APPDATE"
                            ) );

        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja230Form1.html", $arg); 
    }
}
?>
