<?php

require_once('for_php7.php');

class knjd130tForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd130tindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();
        //学期コンボ
        $opt=$opt_seme=array();
        $query = knjd130tQuery::getSemesterQuery();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[]= array('label' => $row["SEMESTERNAME"],
                          'value' => $row["SEMESTER"]);
            $opt_seme[$row["SEMESTER"]] = $row["SEMESTERNAME"];
        }
        $result->free();

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
            $query = knjd130tQuery::getTrainRow($model->schregno, $model->gakki);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        //総合的な学習の時間
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALSTUDYTIME",
                            "cols"        => 43,
                            "rows"        => 5,
                            "extrahtml"   => "style=\"height:75px;\"",
                            "value"       => $row["TOTALSTUDYTIME"] ));
        $arg["data"]["TOTALSTUDYTIME"] = $objForm->ge("TOTALSTUDYTIME");

        //奉仕
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "SPECIALACTREMARK",
                            "cols"        => 43,
                            "rows"        => 5,
                            "extrahtml"   => "style=\"height:75px;\"",
                            "value"       => $row["SPECIALACTREMARK"] ));
        $arg["data"]["SPECIALACTREMARK"] = $objForm->ge("SPECIALACTREMARK");

        //通信欄
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "COMMUNICATION",
                            "cols"        => 43,
                            "rows"        => 5,
                            "extrahtml"   => "style=\"height:75px;\"",
                            "value"       => $row["COMMUNICATION"] ));
        $arg["data"]["COMMUNICATION"] = $objForm->ge("COMMUNICATION");

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");

        //取消ボタン
        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //ＣＳＶ処理
        $fieldSize  = "TOTALSTUDYTIME=315,";
        $fieldSize .= "SPECIALACTREMARK=315,";
        $fieldSize .= "COMMUNICATION=315";


        $securityCnt = $db->getOne(knjd130tQuery::getSecurityHigh());
        //セキュリティーチェック
        $csvSetName = "ＣＳＶ出力";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル出力";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            //ＣＳＶ出力ボタン
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_csv",
                                "value"     => $csvSetName,
                                "extrahtml" => " onClick=\" wopen('".REQUESTROOT."/X/KNJX152/knjx152index.php?FIELDSIZE=".$fieldSize."&SEND_PRGID=KNJD130T&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" ));
            $arg["button"]["btn_csv"] = $objForm->ge("btn_csv");
        }

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno
                            ));

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"]  = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd130tForm1.html", $arg);
    }
}
?>
