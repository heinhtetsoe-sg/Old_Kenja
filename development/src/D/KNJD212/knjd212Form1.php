<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd212Form1.php 56580 2017-10-22 12:35:29Z maeshiro $

class knjd212Form1{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd212index.php", "", "main");

        //処理年度
        $arg["YEAR"]=CTRL_YEAR;

        $db = Query::dbCheckOut();

        //学年コンボSQL文発行
        $opt_grade = array();
        $query = knjd212Query::getGradeQuery();
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $opt_grade[] = array("label" => "第".sprintf("%1d",$row["GRADE"])."学年",
                                 "value" => $row["GRADE"]
                                 );
        }
        $result->free();
        if ($model->field["GRADE"] == "") $model->field["GRADE"] = $opt_grade[0]["value"];
        //学年コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADE",
                            "size"        => "1",
                            "value"       => $model->field["GRADE"],
                            "options"     => $opt_grade,
                            "extrahtml"   => "onChange=\"btn_submit('grade')\";"
                           ));

        $arg["GRADE"] = $objForm->ge("GRADE");

        //学期SQL文発行
        $disabled_u = "disabled";
        $opt_semester = array();
        $query = knjd212Query::getSemesterNameQuery($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $opt_semester[] = array("label" => $row["NAME1"],
                                    "value" => $row["NAMECD2"]
                                  );

            $disabled_u = "";

        }
        $result->free();
        if ($model->field["SEMESTER"] == "") $model->field["SEMESTER"] = $opt_semester[0]["value"];

        //学期コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEMESTER",
                            "size"        => "1",
                            "value"       => $model->field["SEMESTER"],
                            "options"     => $opt_semester,
                            "extrahtml"   => ""
                           ));

        $arg["SEMESTER"] = $objForm->ge("SEMESTER");

		//科目SQL文発行
        $opt = array();
        $query = knjd212Query::getSubclassNameQuery($model);
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('subclasscd')\"";
        $arg["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $model->field["SUBCLASSCD"], $opt, $extra, 1);


        //基準日
        $arg["ctrl_date"] = View::popUpCalendar($objForm, "ctrl_date", str_replace("-", "/", $model->ctrl_date));

        //類型グループSQL文発行
        $opt_group_left = $opt_group_right = array();
        $query = knjd212Query::getTypeGroupNameQuery($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (in_array($row["TYPE_GROUP_CD"], $model->selectdata)) {
                $opt_group_right[] = array("label" => $row["TYPE_GROUP_CD"]."　".$row["TYPE_GROUP_NAME"],
                                           "value" => $row["TYPE_GROUP_CD"] );
            } else {
                $opt_group_left[] = array("label" => $row["TYPE_GROUP_CD"]."　".$row["TYPE_GROUP_NAME"],
                                          "value" => $row["TYPE_GROUP_CD"] );
            }
        }
        $result->free();

        //一覧リストを作成する
        $extra = "multiple style=\"width=250px\" width=\"250px\" ondblclick=\"move1('left')\"";
        $objForm->ae( array("type"        => "select",
                            "name"        => "CATEGORY_NAME",
                            "size"        => "10",
                            "options"     => $opt_group_left,
                            "extrahtml"   => $extra ) );
        $arg["CATEGORY_NAME"] = $objForm->ge("CATEGORY_NAME");

        //出力対象リストを作成する
        $extra = "multiple style=\"width=250px\" width=\"250px\" ondblclick=\"move1('right')\"";
        $objForm->ae( array("type"        => "select",
                            "name"        => "CATEGORY_SELECTED",
                            "size"        => "10",
                            "options"     => $opt_group_right,
                            "extrahtml"   => $extra ) );
        $arg["CATEGORY_SELECTED"] = $objForm->ge("CATEGORY_SELECTED");

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => $extra ) );
        $arg["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => $extra ) );
        $arg["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => $extra ) );
        $arg["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => $extra ) );
        $arg["btn_left1"] = $objForm->ge("btn_left1");


        //リスト用学期名称を配列にする。
        $judge_semester = array("0101" => "１学期中間",
                                "0102" => "１学期期末",
                                "0103" => "１学期平均",
                                "0201" => "２学期中間",
                                "0202" => "２学期期末",
                                "0203" => "２学期平均",
                                "0302" => "３学期期末",
                                "8003" => "学年平均"
                               );

        //類型グループSQL文発行
        $type_group_cd = array();
        $query = knjd212Query::getTypeGroupNameQuery($model, "all");
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $type_group_cd[$row["TYPE_GROUP_CD"]] = $row["TYPE_GROUP_NAME"];
        }
        $result->free();

        //類型グループ表示平均算出処理済リスト作成
        $showline_max = 0;
        $list_data=array();
        $query = knjd212Query::getTypeGroupHrDatQuery();
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row["TYPE_GROUP_CD"]=$type_group_cd[$row["YEAR"].$row["TYPE_GROUP_CD"]];
            $row["JUDGE_SEMESTER"]=$judge_semester[$row["JUDGE_SEMESTER"]];
            $row["GRADE"]="第".sprintf("%1d",$row["GRADE"])."学年";

            $list_data[]=$row;
            $showline_max++;
            if ($showline_max == 100) break;
        }
        $result->free();
        $arg["data"] = $list_data;

        Query::dbCheckIn($db);

        //更新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "類型平均算出",
                            "extrahtml"   => " $disabled_u onclick=\"return btn_submit('update');\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_udpate");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //類型グループ
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );
//echo $model->selectdata;
//var_dump($model->selectdata);
        $arg["finish"]  = $objForm->get_finish();


        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。

        View::toHTML($model, "knjd212Form1.html", $arg);

    }
}
?>
