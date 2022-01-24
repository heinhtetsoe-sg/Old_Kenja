<?php

require_once('for_php7.php');

class knjd210Form3{
    function main(&$model){
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd210", "POST", "knjd210index.php", "", "knjd210");

        $db = Query::dbCheckOut();

        //デフォルト値
        $default_val = "";
        $cnt = 0;
        $ay  = array();

        //学年を求める
        $query = knjd210Query::selectGradeQuery($model);
        $result = $db->query($query);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->grade = $row["GRADE"];

        $disabled = "disabled";

        if ($result->numRows() > 1){        //複数学年の場合、処理無し
        }else{
            //相対評定マスタ.評定上限の最大値を格納
            $query = knjd210Query::selectMaxAssessQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $default_val["ASSESSLEVEL_MAX"] = $row["ASSESSLEVEL"];
            $ar =explode(".",$row["ASSESSLOW"]);   //小数点以下削除
            $default_val["ASSESSLOW_MIN"] = $ar[0];

            $ar =explode(".",$row["ASSESSHIGH"]);   //小数点以下削除
            $default_val["ASSESSHIGH_MAX"] = $ar[0];


            //テーブルテンプレート作成。
            $tmp_tbl      = "<span id=\"strID%s\">%s</span>";
            $tmp_JScript  = " STYLE=\"text-align: right\" ";
            $tmp_JScript .= " onChange=\"document.all['strID%s'].innerHTML=(this.value -1);\" ";
            $tmp_JScript .= " onblur=\"this.value=isNumb(this.value), cleaning_val('off')\" ";

            //相対評価マスタ取得
            $query = knjd210Query::selectAssessQuery($model);
            $result = $db->query($query);

            //テーブル作成
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

                $i=$row["ASSESSLEVEL"];

                if ($row["ASSESSLEVEL"]== 1){
                    $row["ASSESSLOW"]=$row["ASSESSLOW"];
                } else {

                    //テキストボックス作成(ASSESSLOW)
                    $objForm->ae( array("type"        => "text",
                                        "name"        => "ASSESSLOW",
                                        "size"        => "4",
                                        "maxlength"   => "2",
                                        "multiple"    => 1,
                                        "extrahtml"   => "STYLE=\"text-align: right\" onblur=\"check(this,$i)\"",
                                        "value"       => $row["ASSESSLOW"]));
                    $row["ASSESSLOW"] = $objForm->ge("ASSESSLOW");

                }

                //非text部分作成
                if ($row["ASSESSLEVEL"] == $default_val["ASSESSLEVEL_MAX"]){
                    $row["ASSESSHIGH"] = $default_val["ASSESSHIGH_MAX"];
                } else {
                    $tablevalue     = (isset($row["ASSESSHIGH"]))? $row["ASSESSHIGH"] : "" ;
                    $row["ASSESSHIGH"] = sprintf($tmp_tbl,$row["ASSESSLEVEL"],$tablevalue);
                }
                $row["INDEX"] = $i;
                $arg["data"][] = $row;
                $disabled = "";
            }
        }

        Query::dbCheckIn($db);
        //シミュレーションボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_sim",
                            "value"       => "シミュレーション",
                            "extrahtml"   => "$disabled onclick=\" return btn_submit('sim');\"" ) );

        $arg["btn_sim"] = $objForm->ge("btn_sim");

        //相対評価実行ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "相対評価実行",
                            "extrahtml"   => "$disabled onclick=\" return btn_submit('exec');\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_update");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => " 戻 る ",
                            "extrahtml"   => "onclick=\"top.main_frame.right_frame.closeit();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ASSESSLEVELCNT",
                            "value"     => $model->assesslevel
                            ) );

        //hiddenを作成する 相対評定マスタ.評定上限の下限を格納
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "MAX_ASSESSHIGH",
                            "value"     => $default_val["ASSESSHIGH_MAX"]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "MIN_ASSESSLOW",
                            "value"     => $default_val["ASSESSLOW_MIN"]
                            ) );

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd210Form3.html", $arg);
    }
}
?>
