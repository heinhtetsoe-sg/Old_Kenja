<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjb130Form1.php 56585 2017-10-22 12:47:53Z maeshiro $

class knjb130Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb130Form1", "POST", "knjb130index.php", "", "knjb130Form1");

        $arg["data"]["YEAR"] = $model->control["年度"];
        
        $opt=array();
        //ラジオボタンを作成//時間割種別（基本時間割/通常時間割）
        $opt[0]=1;
        $opt[1]=2;
        for ($i = 1; $i <= 2; $i++) {
            $name = "RADIO".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "RADIO",
                                "value"      => isset($model->field["RADIO"])?$model->field["RADIO"]:"1",
                                "extrahtml"  => "onclick=\"jikanwari(this);\" id=\"$name\"",
                                "multiple"   => $opt));

            $arg["data"][$name] = $objForm->ge("RADIO",$i);
        }

        if( $model->field["RADIO"] == 2 ) {     //通常時間割選択時
            $dis_jikan = "disabled";                //時間割選択コンボ使用不可
            $dis_date  = "";                        //指定日付テキスト使用可
            $arg["Dis_Date"]  = " dis_date(false); " ;
        } else {                                //基本時間割選択時
            $dis_jikan = "";                        //時間割選択コンボ使用可
            $dis_date  = "disabled";                //指定日付テキスト使用不可
            $arg["Dis_Date"]  = " dis_date(true); " ;
        }
        
        
        //時間割選択コンボボックスを作成
        $row2 = knjb130Query::getBscHdQuery($model);
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "TITLE",
                            "size"       => "1",
                            "value"      => $model->field["TITLE"],
                            "options"    => isset($row2)?$row2:array(),
                            "extrahtml"  => "$dis_jikan "));
//                            "extrahtml"  => "$dis_jikan onchange=\"conbo_select();\""));
        
        $arg["data"]["TITLE"] = $objForm->ge("TITLE");
        
        
        //指定日付テキストボックスを作成
/*        $arg["data"]["DATE1"] = View::popUpCalendar($objForm,"DATE1",$model->control["学籍処理日"]);
        
        $objForm->ae( array("type"       => "text",
                            "name"       => "DATE2",
                            "size"       => "12",
                            "value"      => $model->control["学籍処理日"],
                            "extrahtml"  => "disabled"));
        
        $arg["data"]["DATE2"] = $objForm->ge("DATE2");
*/
        //指定日付テキストボックスを作成
        if ($model->field["RADIO"] == 2){
            if (!isset($model->field["DATE1"]))
                $model->field["DATE1"] = $model->control["学籍処理日"];
            //指定日を含む指定週の開始日(月曜日)と終了日(日曜日)を取得
            common::DateConv2($model->field["DATE1"],$OutDate1,$OutDate2,1);
            $model->field["DATE2"] = $OutDate2;
        } else {
            $model->field["DATE1"] = "";
            $model->field["DATE2"] = "";
        }
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm,"DATE1",$model->field["DATE1"],"reload=true");

        $objForm->ae( array("type"       => "text",
                            "name"       => "DATE2",
                            "size"       => "12",
                            "value"      => $model->field["DATE2"],
                            "extrahtml"  => "disabled"));

        $arg["data"]["DATE2"] = $objForm->ge("DATE2");


        
        //所属選択コンボボックスを作成
        $db = Query::dbCheckOut();
        $row1 = knjb130Query::getSectQuery();
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "SECTION_CD_NAME1",
                            "size"       => "1",
                            "value"      => $model->field["SECTION_CD_NAME1"],
                            "options"    => $row1));
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "SECTION_CD_NAME2",
                            "size"       => "1",
                            "value"      => isset($model->field["SECTION_CD_NAME2"])?$model->field["SECTION_CD_NAME2"]:$row1[get_count($row1)-1]["value"],
                            "options"    => $row1));
        
        $arg["data"]["SECTION_CD_NAME1"] = $objForm->ge("SECTION_CD_NAME1");
        $arg["data"]["SECTION_CD_NAME2"] = $objForm->ge("SECTION_CD_NAME2");
        
        
        //出力順オプションボタンを作成/////////////////////////////////////////////////////////////////////////////////////
        $opt2[0]=1;
        $opt2[1]=2;
        for ($i = 1; $i <= 2; $i++) {
            $name2 = "OUTPUT".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "OUTPUT",
                                "value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:"1",
                                "extrahtml"	=> "id=\"$name2\"",
                                "multiple"   => $opt2));

            $arg["data"][$name2] = $objForm->ge("OUTPUT",$i);
        }
        
        //印刷ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );
        
        $arg["button"]["btn_print"] = $objForm->ge("btn_print");
        
        /*
        //プレビューボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_preview",
                            "value"       => "プレビュー",
                            "extrahtml"   => "onclick=\"return newwin();\"" ) );
        
        $arg["button"]["btn_preview"] = $objForm->ge("btn_preview");
        */
        
        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
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
                            "value"     => "KNJB130"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "T_YEAR"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "T_BSCSEQ"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "T_SEMESTER"
                            ) );
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "notShowStaffcd", $model->Properties["notShowStaffcd"]);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb130Form1.html", $arg); 
    }
}
?>
