<?php

require_once('for_php7.php');

class knjd210kForm1{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd210kindex.php", "", "main");

        //処理年度
        $arg["YEAR"]=CTRL_YEAR;

        $db = Query::dbCheckOut();

            //学年コンボSQL文発行
            $opt_grade = array();
            $query = knjd210kQuery::getGradeQuery();
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

              $opt_grade[] = array("label" => "第".sprintf("%1d",$row["GRADE"])."学年",
                                   "value" => $row["GRADE"]
                                     );
            }
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
            $disabled_u = "disabled";//NO002
            $opt_semester = array();
            $query = knjd210kQuery::getSemesterNameQuery($model);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

              $opt_semester[] = array("label" => $row["NAME1"],
                                      "value" => $row["NAMECD2"]
                                  );

                $disabled_u = "";//NO002

            }

            if ($model->field["SEMESTER"] == "") $model->field["SEMESTER"] = $opt_semester[0]["value"];//NO001

            //学期コンボ
            $objForm->ae( array("type"        => "select",
                                "name"        => "SEMESTER",
                                "size"        => "1",
                                "value"       => $model->field["SEMESTER"],
                                "options"     => $opt_semester,
                                "extrahtml"   => "onChange=\"btn_submit('semester')\";"
                               ));

            $arg["SEMESTER"] = $objForm->ge("SEMESTER");
            $arg["ctrl_date"] = View::popUpCalendar($objForm, "ctrl_date", str_replace("-", "/", $model->ctrl_date));

            //学期コンボで学年平均が選択されていなかったら、追試験データ作成ボタンを押し不可にする
            if ($model->field["SEMESTER"] != "8003") {
                $disabled_s = "disabled";
            }
            if (substr($model->field["SEMESTER"], 2) != "03") {
                $disabled_karinomi = "disabled";
            }

            //リスト用学期名称を配列にする。
            $judge_semester = array("0101" => "１学期中間",
                                    "0102" => "１学期期末",
                                    "0103" => "１学期平均",
                                    "0201" => "２学期中間",
                                    "0202" => "２学期期末",
                                    "0203" => "２学期平均",
                                    "0302" => "３学期期末",
                                    "8003" => "学年平均",
                                    "8013" => "学年平均(総学用)"
                                   );


            //類型グループ表示平均算出処理済リスト作成
            $list_data=array();
            $query = knjd210kQuery::getTypeGroupHrDatQuery();
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
              $row["JUDGE_SEMESTER"]=$judge_semester[$row["JUDGE_SEMESTER"]];
              $row["GRADE"]="第".sprintf("%1d",$row["GRADE"])."学年";
              
              $list_data[]=$row;
            }
                $arg["data"] = $list_data;

        Query::dbCheckIn($db);

        //更新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "②類型平均算出",
                            "extrahtml"   => " style=\"width:110px\" $disabled_u onclick=\"return btn_submit('update');\"" ) );//NO002
//                            "extrahtml"   => " onclick=\"return btn_submit('update');\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_udpate");

        //仮評定フラグラジオ 1:仮評定 2:本評定(学年評定)・・・本評定はNULLで更新する
        //radio
        $opt = array(1, 2);
        $model->field["KARI_DIV"] = ($model->field["KARI_DIV"] == "") ? "1" : $model->field["KARI_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"KARI_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "KARI_DIV", $model->field["KARI_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //学期コンボの値によって科目読替処理ボタンを押し不可にする
        if (!in_array($model->field["SEMESTER"],array("0103","0203","0302","8003"))) {
            $disabled_r = "disabled";
        }

        //科目読替処理ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "replace_sub",
                            "value"       => "①科目読替処理",
                            "extrahtml"   => "style=\"width:110px\" $disabled_r onclick=\"return btn_submit('replace_sub');\"" ) );

        $arg["replace_sub"] = $objForm->ge("replace_sub");

        //追試験データ作成ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "supp_udpate",
                            "value"       => "③追試験データ作成",
                            "extrahtml"   => "style=\"width:140px\" $disabled_s onclick=\"return btn_submit('update2');\"" ) );

        $arg["supp_udpate"] = $objForm->ge("supp_udpate");

        //補点・補充後の評定フラグ設定ボタンを作成する
        $extra = $disabled_karinomi." style=\"width:220px\" $disabled_u onclick=\"return btn_submit('updateKariNomi');\"";
        $arg["kari_udpate"] = knjCreateBtn($objForm, "kari_udpate", "④補点・補充後の評定フラグ設定", $extra);

        //仮評定フラグラジオ 1:仮評定 2:本評定(学年評定)・・・本評定はNULLで更新する
        //radio
        $opt = array(1, 2);
        $model->field["KARI_NOMIDIV"] = ($model->field["KARI_NOMIDIV"] == "") ? "1" : $model->field["KARI_NOMIDIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, $disabled_karinomi." id=\"KARI_NOMIDIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "KARI_NOMIDIV", $model->field["KARI_NOMIDIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //終了ボタンを作成する
        if (!$model->getPrgId) {
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_end",
                                "value"       => "終 了",
                                "extrahtml"   => "onclick=\"closeWin();\"" ) );

            $arg["btn_end"] = $objForm->ge("btn_end");
        } else {
            $extra = "onclick=\"btn_openerSubmit();\"";
            $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
        }

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();


        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。

        View::toHTML($model, "knjd210kForm1.html", $arg);

    }
}
?>
