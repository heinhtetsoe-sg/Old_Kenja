<?php

require_once('for_php7.php');

class knjz211form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz211index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //評定区分コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjz211Query::getZ040();
        makeCmb($objForm, $arg, $db, $query, $model->sepa, "sepa", $extra, 1);

        //課程学科コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjz211Query::getCourseMajor($model);
        makeCmb($objForm, $arg, $db, $query, $model->courseMajor, "COURSEMAJOR", $extra, 1);
        //コースコンボ
        $query = knjz211Query::getCourseCode($model);
        makeCmb($objForm, $arg, $db, $query, $model->courseCode, "COURSECODE", $extra, 1);
        //評定段階数
        $assesslevelcnt = 0;
        $query = knjz211Query::getAssessLevelCnt($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $assesslevelcnt = $row["ASSESSLEVELCNT"];
            $assessmark = $row["ASSESSMEMO"];
        }
        if ($model->cmd=="level" || ($model->cmd=="main" && $model->level !="")) {
            $assesslevelcnt = $model->level;
        }
        $cnt = $assesslevelcnt;

        //リスト表示
        //SQL文発行
        $ar[] = array();

        //警告メッセージを表示しない場合
        if (isset($model->sepa) && !isset($model->warning)) {
            $query = knjz211Query::selectQuery($model);
            $result = $db->query($query);
        } else {
            $row =& $model->field;
            $result = "";
        }

        for ($i = 1; $i <= $cnt; $i++) {
            if ($result != "") {
                if ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //レコードを連想配列のまま配列$arg[data]に追加していく。
                    array_walk($row, "htmlspecialchars_array");
                    //評定区分1と2は小数点を取り除く。
                    if($model->sepa == 4){
                        $row["ASSESSLOW"] = $row["ASSESSLOW"];
                        $row["ASSESSHIGH"] = $row["ASSESSHIGH"];
                        $up = $row["UPDATED"];
                    }else{
                        $ar =explode(".",$row["ASSESSLOW"]);
                        $row["ASSESSLOW"] = $ar[0];
                        $ar =explode(".",$row["ASSESSHIGH"]);
                        $row["ASSESSHIGH"] = $ar[0];
                        $up = $row["UPDATED"];
                    }
                }
            }
            $row["ASSESSLEVEL"] = $i;

            //textの有無設定(下限部分)
            if ($row["ASSESSLEVEL"] == 1) {
                $row["ASSESSLOWTEXT"] = "0";
            } else {
                $row["ASSESSLOWTEXT"]  = "<input type=\"text\" name=\"";
                $row["ASSESSLOWTEXT"] .= "ASSESSLOW".$row["ASSESSLEVEL"];
                $row["ASSESSLOWTEXT"] .= "\" value=\"";
                if ($model->cmd !="level") {
                    if ($result != "") {
                        $row["ASSESSLOWTEXT"] .= $row["ASSESSLOW"];
                    } else if ($result == "") {
                        $row["ASSESSLOWTEXT"] .= $row["ASSESSLOW$i"];
                    }
                }
                $row["ASSESSLOWTEXT"] .= "\" size=\"";
                $row["ASSESSLOWTEXT"] .= "4";
                $row["ASSESSLOWTEXT"] .= "\" maxlength=\"";
                if($model->sepa == 4){
                    $row["ASSESSLOWTEXT"] .= "4";
                } else {
                    $row["ASSESSLOWTEXT"] .= "3";
                }
                $row["ASSESSLOWTEXT"] .= "\" onblur=\"isNumb(this,".($row["ASSESSLEVEL"] - 1).",'".(($model->sepa == 4)? "ABCD" : "ELSE" )."');\"";
                $row["ASSESSLOWTEXT"] .= " STYLE=\"text-align: right\"> ";
                $stock[] = $row["ASSESSLOW"];
            }

            $sepaMax = $model->sepa == '4' ? 5 : 100;
            //上限部分作成
            if ($row["ASSESSLEVEL"] == $cnt) {
                $row["ASSESSHIGHTEXT"] = $sepaMax;
            } else {
                $row["ASSESSHIGHTEXT"]  = "<span id=\"strID";
                $row["ASSESSHIGHTEXT"] .= $row["ASSESSLEVEL"];
                $row["ASSESSHIGHTEXT"] .= "\">";
                if ($result != "") {
                    if ($model->cmd !="level") {
                        $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                    }
                } else if ($result == "") {
                    $row["ASSESSHIGHTEXT"] .= ($row["ASSESSLOW".($i + 1)] - (($model->sepa == 4)? 0.1 : 1));
                }
                $row["ASSESSHIGHTEXT"] .= "</span>";
            }
            //記号部分作成
            $row["ASSESSMARKTEXT"]  = "<input type=\"text\" name=\"";
            $row["ASSESSMARKTEXT"] .= "ASSESSMARK".$row["ASSESSLEVEL"];
            $row["ASSESSMARKTEXT"] .= "\" value=\"";
            if ($model->cmd != "level") {
                if ($result != "") {
                    $row["ASSESSMARKTEXT"] .= $row["ASSESSMARK"];
                } else if ($result == "") {
                    $row["ASSESSMARKTEXT"] .= $row["ASSESSMARK$i"];
                }
            } else {
//                $row["ASSESSMARKTEXT"] .= $i;
                $row["ASSESSMARKTEXT"] .= "";
            }
            $row["ASSESSMARKTEXT"] .= "\" size=\"";
            $row["ASSESSMARKTEXT"] .= "8";
            $row["ASSESSMARKTEXT"] .= "\" maxlength=\"";
            $row["ASSESSMARKTEXT"] .= "6";
            $row["ASSESSMARKTEXT"] .= "\" STYLE=\"text-align: right\"> ";

            $arg["data"][] = $row;
        }

        Query::dbCheckIn($db);

        //段階数
        $objForm->ae( array("type"      => "text",
                            "name"      => "ASSESSLEVELCNT",
                            "size"      => 6,
                            "maxlength" => 3,
                            "extrahtml" => "",
                            "value"     => $assesslevelcnt ));
        $arg["sepa"]["ASSESSLEVELCNT"] = $objForm->ge("ASSESSLEVELCNT");
        //確定ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_level",
                            "value"       => "確 定",
                            "extrahtml"   => "onclick=\"return level(".$cnt.");\" " ) );
        $arg["sepa"]["btn_level"] = $objForm->ge("btn_level");



        //更新ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"return closeWin();\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $up  ) );


        $arg["finish"]  = $objForm->get_finish();

        if ($model->sec_competence != DEF_UPDATABLE) {
            $arg["Closing"] = " closing_window(); " ;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz211Form1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["sepa"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
