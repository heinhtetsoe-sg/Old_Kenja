<?php

require_once('for_php7.php');

class knjd130hForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd130hindex.php", "", "edit");

        //学期コンボ
        $db = Query::dbCheckOut();
        $opt=$opt_seme=array();
        $query = knjd130hQuery::getSemesterQuery();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    	    $opt[]= array('label' => $row["SEMESTERNAME"],
    	                  'value' => $row["SEMESTER"]);
    	    $opt_seme[$row["SEMESTER"]] = $row["SEMESTERNAME"];
        }
        $result->free();
        Query::dbCheckIn($db);

        if (!isset($model->gakki)) $model->gakki = CTRL_SEMESTER;

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->gakki,
                            "extrahtml"  => "onChange=\"btn_submit('gakki');\"",
                            "options"    => $opt));
        $arg["GAKKI"] = $objForm->ge("GAKKI");
        $arg["GAKKI_NAME"] = $opt_seme[$model->gakki];

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = knjd130hQuery::getTrainRow($model->schregno, $model->gakki);
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //通信欄
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "COMMUNICATION",
                            "cols"        => 43,
                            "rows"        => 4,
                            "extrahtml"   => "style=\"height:65px;\"",
                            "value"       => $row["COMMUNICATION"] ));
        $arg["data"]["COMMUNICATION"] = $objForm->ge("COMMUNICATION");

        //成績参照ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_subform1",
                            "value"     => "成績・出欠参照",
                            "extrahtml" => "onclick=\"return btn_submit('subform1');\"" ) );

        $arg["button"]["btn_subform1"] = $objForm->ge("btn_subform1");

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");

        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno
                            ));

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //所見入力チェック用 ---> 今学期のみ入力とする。他の学期は参照のみ可能となる。(広島国際の仕様)
        $arg["gakki_chk"] = ($model->gakki != CTRL_SEMESTER) ? "DisBtn();" : "";

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjd130hForm1.html", $arg);
    }
}
?>
