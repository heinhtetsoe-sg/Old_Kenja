<?php

class knjz211kForm2{

    function main(&$model){

        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz211kindex.php", "", "edit");

        //DBオープン
        $db  = Query::dbCheckOut();

        #####  リスト表示  #####
        //初期化
        $array = array();
        $cnt = 0;
        $up   = "";

        //warning処理
        if (!isset($model->warning) && $model->asses_cd != "") {
            $row = $db->getRow(knjz211kQuery::getAssesQuery($model->year, $model->asses_cd), DB_FETCHMODE_ASSOC);
            $result = $db->query(knjz211kQuery::selectQuery($model->year, $model->asses_cd));
        } else {
            $row    =& $model->field;
            $result =  "";
            $up = $model->field["UPDATED"];
        }

        //テーブルテンプレート作成。
        $tmp_tbl      = "<span id=\"strID%s\">%s</span>";
        $tmp_JScript  = " STYLE=\"text-align: right\" ";
        $tmp_JScript .= " onChange=\"document.all['strID%s'].innerHTML=(this.value -1)\" ";
        $tmp_JScript .= " onblur=\"this.value=toInteger(this.value), cleaning_val('off')\" ";
        $JScript = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; ";

        //類型平均_上限値テキストボックス
        $objForm->ae( array("type"      => "text",
                            "name"      => "AVE_HIGH",
                            "size"      => 4,
                            "maxlength" => 3,
                            "extrahtml" => $JScript,
                            "value"     => (isset($row["TYPE_GROUP_AVE_HIGH"])) ? $row["TYPE_GROUP_AVE_HIGH"] : "" ));
        $arg["ave"]["TYPE_GROUP_AVE_HIGH"] = $objForm->ge("AVE_HIGH");
        //類型平均_下限値テキストボックス
        $objForm->ae( array("type"      => "text",
                            "name"      => "AVE_LOW",
                            "size"      => 4,
                            "maxlength" => 3,
                            "extrahtml" => $JScript,
                            "value"     => (isset($row["TYPE_GROUP_AVE_LOW"])) ? $row["TYPE_GROUP_AVE_LOW"] : "" ));
        $arg["ave"]["TYPE_GROUP_AVE_LOW"] = $objForm->ge("AVE_LOW");

        if ($result != "") {
            $cnt = $result->numRows();
        } else {
            $cnt = $row["TBL_COUNT"];
        }
        //テーブル作成
        for ($i = 1; $i <= $cnt; $i++) {
            //代入値初期化
            $textvalue  = 0 ;
            $tablevalue = 0 ;

            if ($result != "") {
                if ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($up == "") {
                        $up = $row["UPDATED"];
                    } else {
                        $up .= ",".$row["UPDATED"];
                    }
                }
                $textvalue  = (isset($row["TYPE_ASSES_LOW"])) ? $row["TYPE_ASSES_LOW"] : "" ;
            }else if ($result == "") {
                $textvalue  = $row["ASSESSLOW$i"];
            }
            $row["TYPE_ASSES_LEVEL"] = $i;

            //テキストボックス作成(ASSESSLOW)
            $objForm->ae( array("type"        => "text",
                                "name"        => "ASSESSLOW".$i,
                                "size"        => "4",
                                "maxlength"   => "3",
                                "extrahtml"   => ($i != 1) ? sprintf($tmp_JScript,($i-1)) : $JScript,
                                "value"       => $textvalue));
            $row["ASSESSLOWTEXT"] = $objForm->ge("ASSESSLOW".$i);

            //非text部分作成
            if ($row["TYPE_ASSES_LEVEL"] == $cnt) {
                $objForm->ae( array("type"        => "text",
                                    "name"        => "ASSESSHIGH",
                                    "size"        => "4",
                                    "maxlength"   => "3",
                                    "extrahtml"   => $JScript,
                                    "value"       => $row["TYPE_ASSES_HIGH"]));
                $row["ASSESSHIGHTEXT"] = $objForm->ge("ASSESSHIGH");
            } else {
                if ($result != "") {
                    $tablevalue     = (isset($row["TYPE_ASSES_HIGH"]))? $row["TYPE_ASSES_HIGH"] : "" ;
                }else if ($result == "") {
                    $tablevalue = ($row["ASSESSLOW".($i + 1)] -1);
                }
                $row["ASSESSHIGHTEXT"] = sprintf($tmp_tbl,$row["TYPE_ASSES_LEVEL"],$tablevalue);
            }
            $arg["data"][] = $row;
        }
        Query::dbCheckIn($db);

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => " onclick=\"return btn_submit('update'), cleaning_val('off');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => " onclick=\"return btn_submit('reset');\""  ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ASSES_CD",
                            "value"     => (isset($model->field["TYPE_ASSES_CD"]) ? $model->field["TYPE_ASSES_CD"] : $model->asses_cd)) );

        //テーブルの作成数
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TBL_COUNT",
                            "value"     => $cnt ) );

        //データ処理日の保持
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => ($model->copy_flg == true) ? $model->field["UPDATED"] : $up ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "Cleaning",
                            "value"     => $model->Clean) );

        $arg["pattern"] = "&nbsp;&nbsp;類型評定パターン　：　" . $model->asses_cd;
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::post("cmd") == "update") {
            $arg["reload"] = "parent.left_frame.location.href='knjz211kindex.php?cmd=list&year=" . $model->year . "';";
        }
        View::toHTML($model, "knjz211kForm2.html", $arg); 
    }
}
?>
