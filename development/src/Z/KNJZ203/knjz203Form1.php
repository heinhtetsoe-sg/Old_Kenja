<?php

require_once('for_php7.php');

class knjz203Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz203index.php", "", "edit");
        $arg["YEAR"] = CTRL_YEAR;
        $arg["PROGRAMID"] =PROGRAMID;

        $db = Query::dbCheckOut();

       //デフォルト値
        if($model->coursecode ==""){
            $query = knjz203Query::getFirst_CouseKey($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->coursecode = $row["COURSECODE"];
            $model->coursecd   = $row["COURSECD"];
            $model->majorcd    = $row["MAJORCD"];
            $model->grade      = $row["GRADE"];
            $model->coursename="";
        }

        //コースコンボ作成
        $opt = array();
        $result = $db->query(knjz203Query::getCouseName($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

        $course_majaorne = $row["COURSENAME"].$row["MAJORNAME"];
        if(mbereg("[｡-ﾟ]",$course_majaorne)){
            $ct2 = (integer)(substr_count(mbereg_replace("[｡-ﾟ]","0",$course_majaorne),"0"));
            $space_count = 29-(((integer)((strlen($course_majaorne))/3)*2)+((integer)strlen($course_majaorne))%3)+$ct2;
        }else{
            $ct = (integer)(substr_count(mbereg_replace("[ｱ-ﾝ0-9A-Za-z -~]","0",$course_majaorne),"0")/3);
            $space_count = 29-(((integer)((strlen($course_majaorne))/3)*2)+((integer)strlen($course_majaorne))%3)-$ct;
        }

        if ($space_count < 0) {
            $space_count = 0;
        }

        $name = ($row["COURSENAME"].
                $row["MAJORNAME"].
                str_repeat("&nbsp;",$space_count));

           $opt[] = array("label" => ltrim($row["GRADE"],'0')."学年&nbsp;".
                                        "(".$row["COURSECD"].$row["MAJORCD"].")&nbsp;".
                                        $name."&nbsp;".
                                        "(".$row["COURSECODE"].")&nbsp;"
                                        .$row["COURSECODENAME"],
                          "value" => $row["COURSECODE"]." ".$row["COURSECD"]." ".$row["MAJORCD"]." ".$row["GRADE"]);
        }

        //コース名
        $objForm->ae( array("type"      => "select",
                           "name"       => "COURSENAME",
                           "size"       => "1",
                           "value"      => $model->coursename,
                           "extrahtml"  => " onchange=\"btn_submit('coursename');\"",
                           "options"    => $opt
                           ));
        $arg["COURSENAME"] = $objForm->ge("COURSENAME");

        //コース一覧取得
        $query = knjz203Query::getList($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($row["SPECIAL_GROUP_CD"] == $model->SPECIAL_GROUP_CD) {
                $row["CREDITS"] = ($row["CREDITS"]) ? $row["CREDITS"] : "　";
                $row["CREDITS"] = "<a name=\"target\">{$row["CREDITS"]}</a><script>location.href='#target';</script>";
            }
            $arg["data"][] = $row;
        }
        $result->free();

        //欠課数オーバーのタイトル
        if (in_array("1", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN"]  = $model->control["学期名"]["1"];
        }
        if (in_array("2", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN2"] = $model->control["学期名"]["2"];
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN3"] = $model->control["学期名"]["3"];
        }

        //授業時数のフラグ  特活上限値の出力の判定に使う
        $jugyou_jisu_flg = $db->getOne(knjz203Query::getJugyouJisuFlg());   //1:法定授業 2:実授業
        if ($jugyou_jisu_flg != "2") {
            $arg["title"]["ABSENCE_HIGH"] = "1";
        }

        Query::dbCheckIn($db);
        //コピーボタンを作成
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_copy",
                            "value"     => "前年度からコピー",
                            "extrahtml" => " onclick=\"return btn_submit('copy');\""
                            ) );
        $arg["btn_copy"] = $objForm->ge("btn_copy");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz203Form1.html", $arg);
    }
}
?>
