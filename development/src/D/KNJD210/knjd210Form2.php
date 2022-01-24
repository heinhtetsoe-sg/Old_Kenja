<?php

require_once('for_php7.php');

class knjd210Form2{
    function main(&$model){
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd210", "POST", "knjd120index.php", "", "knjd210");
        //学年をまたいでいる場合
        if ($model->warning){
            $prm["GRADE"] = "disabled";
            $prm["CLASS"] = "checked";
            $mod_avg =  $model->average["CLASS"];
        }else{
            $prm["GRADE"] = "checked";
            $mod_avg =  $model->average["GRADE"];
        }
        //学年平均
        $arg["GRADE"] = "<input type=\"radio\" name=\"STATUS\" value=\"" .$model->average["GRADE"] ."\" id=\"grd\" ".$prm["GRADE"] ." onClick=\"setAvg(this)\"><label for=\"grd\"><tt>学年平均を使用する　　→　</tt>" .$model->average["GRADE"] ."</label>";

        //クラス平均
        $arg["CLASS"] = "<input type=\"radio\" name=\"STATUS\" value=\"" .$model->average["CLASS"] ."\" id=\"cls\" ".$prm["CLASS"] ." onClick=\"setAvg(this)\"><label for=\"cls\"><tt>クラスを使用する　　　→　</tt>" .$model->average["CLASS"] ."</label>";

        //平均基準点
        $objForm->ae( array("type"        => "text",
                            "name"        => "AVG_HOSEI_RATE",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "STYLE=\"text-align: right\" onblur=\"check(this)\"",
                            "value"       => 60));

        $arg["AVG_HOSEI_RATE"] = $objForm->ge("AVG_HOSEI_RATE");

        //平均点
        $objForm->ae( array("type"        => "text",
                            "name"        => "MOD_AVG",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "STYLE=\"text-align: right\" onblur=\"check(this)\"",
                            "value"       => $mod_avg));

        $arg["MOD_AVG"] = $objForm->ge("MOD_AVG");
        //更新ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"calc()\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_udpate");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "キャンセル",
                            "extrahtml"   => "onclick=\"top.main_frame.right_frame.closeit();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd210Form2.html", $arg); 
    }
}
?>
