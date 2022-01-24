<?php

require_once('for_php7.php');

class knjd050Form1
{
    function main(&$model){

        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd050Form1", "POST", "knjd050index.php", "", "knjd050Form1");

        //読込ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_toukei",
                            "value"       => "･･･",
                            "extrahtml"   => "onclick=\"wopen('../../X/KNJXTOKE4/knjxtoke4index.php?DISP=CLASS&PROGRAMID=$model->programid','KNJXTOKE4',0,0,900,550);\"") );
                                                                                                    // 04/10/30
        $arg["explore"] = $objForm->ge("btn_toukei");
        //学習記録エクスプローラー
        if(!isset($model->cmd)) {
            $arg["ONLOAD"] = "wopen('../../X/KNJXTOKE4/knjxtoke4index.php?DISP=CLASS&PROGRAMID=$model->programid','KNJXTOKE4',0,0,900,550);";
        }                                                               // 04/10/30

        $cd =& $model->attendclasscd;

        if (isset($cd)){ 
            $db = Query::dbCheckOut();
            $query = knjd050Query::SQLGet_Main($model);

            //教科、科目、クラス取得
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $title  = "[" . $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]."　". htmlspecialchars($row["CLASSNAME"]) ."]-[" .$row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."　".htmlspecialchars($row["SUBCLASSNAME"]) ."]";
                    $subclasscd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
                } else {
                    $title  = "[" . $row["CLASSCD"]."　". htmlspecialchars($row["CLASSNAME"]) ."]-[" .$row["SUBCLASSCD"]."　".htmlspecialchars($row["SUBCLASSNAME"]) ."]";
                    $subclasscd = $row["SUBCLASSCD"];
                }
                $checked = (is_array($model->checked_attend) && in_array($row["ATTENDCLASSCD"], $model->checked_attend))? true:false;
        //2004-07-30 naka
                $checked2 = (is_array($model->checked_staff) && in_array($row["STAFFCD"], $model->checked_staff))? true:false;
        //2004/06/30 nakamoto-------------------------------------
                if ($row["CHARGEDIV"] == 1) {
                    $row["CHARGEDIV"] = ' ＊';
                } else {
                    $row["CHARGEDIV"] = ' ';
                }
                if ($checked==0 || $checked2==0) {   //2004-07-30 naka
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $objForm->add_element(array("type"      => "checkbox",
                                                     "name"     => "chk",
                                                    //"checked"  => $checked,   2004-07-30 naka
                                                    "checked"  => false,
                                                     "value"    => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"],
                                                     "extrahtml"   => "multiple" ));
                    } else {
                        $objForm->add_element(array("type"      => "checkbox",
                                                     "name"     => "chk",
                                                    //"checked"  => $checked,   2004-07-30 naka
                                                    "checked"  => false,
                                                     "value"    => $row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"],
                                                     "extrahtml"   => "multiple" ));
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
                    $arg["data"][] = $row; 
                } else {
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $objForm->add_element(array("type"      => "checkbox",
                                                     "name"     => "chk1",
                                                     "checked"  => $checked,
                                                     "value"    => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"],
                                                     "extrahtml"   => "disabled" ));
                    } else {
                        $objForm->add_element(array("type"      => "checkbox",
                                                     "name"     => "chk1",
                                                     "checked"  => $checked,
                                                     "value"    => $row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"],
                                                     "extrahtml"   => "disabled" ));
                    }
                    $row["CHECK"] = $objForm->ge("chk1");
                    $row["TERM"] = str_replace("-","/",$row["STARTDAY"]) ."～" .str_replace("-","/",$row["ENDDAY"]);
                    $arg["data1"][] = $row; 
                    $objForm->ae( array("type"      => "hidden",
                                        "name"      => "STARTDAY",
                                        "value"     => $row["STARTDAY"]
                                        ) );
                }
            }
        /* 04/10/30 テスト名表示をカット
            //テスト名取得
            if($title!=""){
                $query = knjd050Query::SQLGet_Test($model);
                $result = $db->query($query);
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $title .= sprintf("-[%02d%02d　%s]", (int) $row["TESTITEMCD"],(int) $row["TESTKINDCD"], htmlspecialchars($row["TESTITEMNAME"]));
                }
            }   */
            Query::dbCheckIn($db);
        }
        $objForm->add_element(array("type"      => "checkbox",
                                    "name"      => "chk_all",
                                    "extrahtml"   => "disabled" ));

        $arg["CHECK_ALL"] = $objForm->ge("chk_all");


        //ラジオボタンを作成する
        for ($i = 1; $i <= 4; $i++) {
            $name1 = ($i <= 2) ? "OUT1" : "OUT2";
            $name2 = "OUTPUT".$i;
            $value = ($i <= 2) ? $i : $i - 2;
            $objForm->add_element(array("type"      => "radio",
                                        "value"     => 1,
                                        "extrahtml" => "id=\"$name2\"",
                                        "name"      => $name1));

            $arg[$name2] = $objForm->ge($name1,$value);
        }

        //テスト種別リストを追加
        $opt_kind = array();
        $db = Query::dbCheckOut();
        $query = knjd050Query::SQLGet_Test($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_kind[] = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTKINDCD",
                            "size"       => "1",
                            "value"      => $model->testkindcd,
                            "options"    => $opt_kind));
        $arg["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

        //ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_ok",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return opener_submit('" . SERVLET_URL . "');\"" ) );

        $arg["btn_ok"] = $objForm->ge("btn_ok");

        //ボタンを作成する
        $objForm->ae( array("type" => "button",
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
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD050"
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

        /* 04/10/30 カット
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTKINDCD",
                            "value"     => $model->testkindcd,
                            ) );
        */
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTITEMCD",
                            "value"     => $model->testitemcd,
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTENDCLASSCD"
                            ) );

        //2004-07-30 naka
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STAFF_CD"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTENDCLASSCD1"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTENDCLASSCD2"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GROUPCD"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TAI"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TOKE"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DISP"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //テスト参照テーブル
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "COUNTFLG",
                            "value"     => $model->testTable
                            ) );

        //教育課程コード
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "useCurriculumcd",
                            "value"     => $model->Properties["useCurriculumcd"]
                            ) );

        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd050Form1.html", $arg); 
    }
}
?>
