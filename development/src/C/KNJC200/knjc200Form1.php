<?php

require_once('for_php7.php');

class knjc200Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjc200Form1", "POST", "knjc200index.php", "", "knjc200Form1");

        //年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR ) );
        $arg["data"]["YEAR"] = CTRL_YEAR;
        //学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => CTRL_SEMESTER ) );
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        $db = Query::dbCheckOut();

        //クラス選択 1:年・組順,2:担任あいうえお順
        $model->field["OUTPUT"] = isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : 1;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "OUTPUT",
                            "value"     => $model->field["OUTPUT"] ) );

        //年・組順
        $cmp_grade = "";
        $opt_hrname1 = array();
        $result = $db->query(knjc200Query::getHrName($model, 1));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($cmp_grade != $row["GRADE"] && $cmp_grade != "") $str1 .= "</optgroup>";
            if ($cmp_grade != $row["GRADE"]) $str1 .= "<optgroup label=\"".$row["GRADE_NAME1"]."\">";
            $str1 .= "<option value='".$row["GRADE"] .$row["HR_CLASS"]."'>" .$row["HR_NAME"] ." - " .$row["STAFFNAME"];
            $cmp_grade = $row["GRADE"];
        }
        if ($cmp_grade != "") $str1 .= "</optgroup>";
        //担任あいうえお順
        $cmp_kana1 = "";
        $opt_hrname2 = array();
        $result = $db->query(knjc200Query::getHrName($model, 2));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($cmp_kana1 != $row["KANA1"] && $cmp_kana1 != "") $str2 .= "</optgroup>";
            if ($cmp_kana1 != $row["KANA1"]) $str2 .= "<optgroup label=\"".$row["KANA1"]."\">";
            $str2 .= "<option value='".$row["GRADE"] .$row["HR_CLASS"]."'>" .$row["STAFFNAME"] ." - " .$row["HR_NAME"];
            $cmp_kana1 = $row["KANA1"];
        }
        if ($cmp_kana1 != "") $str2 .= "</optgroup>";

        $arg["data"]["SORT_NAME"] = ($model->field["OUTPUT"]==1) ? "クラス選択画面<br>(年・組順)" : "クラス選択画面<br>(担任あいうえお順)";
        $arg["data"]["HR_NAME"] = ($model->field["OUTPUT"]==1) ? $str1 : $str2;

        $result->free();
        Query::dbCheckIn($db);


        //クラス選択画面へ
        $sort_name = ($model->field["OUTPUT"]==2) ? "クラス選択画面\n(年・組順)" : "クラス選択画面\n(担任あいうえお順)";
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_sort",
                            "value"       => $sort_name,
                            "extrahtml"   => "style=\"width:130px\" onclick=\"return btn_submit('output');\"" ) );
        $arg["button"]["btn_sort"] = $objForm->ge("btn_sort");

        //リンク先作成・・・プログラムＩＤ変更のため修正
        $jumping2 = REQUESTROOT."/C/KNJC200_2/knjc200_2index.php";
        $jumping3 = REQUESTROOT."/C/KNJC200_3/knjc200_3index.php";

        //詳細入力画面へ
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_disp1",
                            "value"       => "詳細入力画面へ",
                            "extrahtml"   => "onclick=\"btn_select('".$jumping2."');\"" ) );
        $arg["button"]["btn_disp1"] = $objForm->ge("btn_disp1");

/*
        //出欠届け履歴画面へ
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_disp2",
                            "value"       => "出欠届け履歴画面へ",
                            "extrahtml"   => "onclick=\"btn_select('".$jumping3."');\"" ) );
        $arg["button"]["btn_disp2"] = $objForm->ge("btn_disp2");
*/
        //生徒検索
        $jump_search = REQUESTROOT."/C/KNJC200_SEARCH/knjc200_searchindex.php";
        $extra = "onclick=\"btn_seito('".$jump_search."');\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "生徒検索", $extra);

        //ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );


        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc200Form1.html", $arg); 
    }
}
?>
