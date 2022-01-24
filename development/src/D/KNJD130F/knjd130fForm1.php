<?php

require_once('for_php7.php');

class knjd130fForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd130findex.php", "", "edit");
        $arg["IFRAME"] = View::setIframeJs();

        //学期コンボ
        $db = Query::dbCheckOut();
        $opt=$opt_seme=array();
        $query = knjd130fQuery::getSemesterQuery();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[]= array('label' => $row["SEMESTERNAME"],
                          'value' => $row["SEMESTER"]);
            $opt_seme[$row["SEMESTER"]] = $row["SEMESTERNAME"];
        }
        $result->free();
        Query::dbCheckIn($db);

        if (!isset($model->semester)) $model->semester = CTRL_SEMESTER;

        $objForm->ae( array("type"       => "select",
                            "name"       => "SEMESTER",
                            "size"       => "1",
                            "value"      => $model->semester,
                            "extrahtml"  => "onChange=\"btn_submit('edit');\"",
                            "options"    => $opt));
        $arg["SEMESTER"] = $objForm->ge("SEMESTER");
        $arg["SEMESTER_NAME"] = $opt_seme[$model->semester];

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = knjd130fQuery::getRow($model->schregno, $model->semester);
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //出欠の記録備考反映
        $db = Query::dbCheckOut();
        if ($model->cmd === 'attend') {
            $attend_remark = "";
            $query = knjd130fQuery::getAttendSemesRemarkDat($model);
            $result = $db->query($query);
            while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($attend_remark == "") {
                    $attend_remark .= $row1["REMARK1"];
                } else {
                    if ($row1["REMARK1"] != "") {
                        $attend_remark .= "／".$row1["REMARK1"];
                    }
                }
            }
            $row["COMMUNICATION"] = $attend_remark;
        }
        Query::dbCheckIn($db);

        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                $setname = 'まとめ出欠備考取込';
                $extra = $disabled ." style=\"color:#1E90FF;font:bold\" onclick=\"return btn_submit('attend');\"";
            } else {
                $setname = 'まとめ出欠備考参照';
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR=".CTRL_YEAR."&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["button"]["btn_attendremark"] = KnjCreateBtn($objForm, "btn_attendremark", $setname, $extra);
        }
        
        //出欠備考
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "COMMUNICATION",
                            "cols"        => 14,
                            "rows"        => 9,
                            "extrahtml"   => "style=\"height:132px;\"",
                            "value"       => $row["COMMUNICATION"] ));
        $arg["data"]["COMMUNICATION"] = $objForm->ge("COMMUNICATION");

        //ボタン
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
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

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

        View::toHTML($model, "knjd130fForm1.html", $arg);
    }
}
?>
