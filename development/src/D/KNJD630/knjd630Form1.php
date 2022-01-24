<?php

require_once('for_php7.php');

class knjd630Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd630index.php", "", "edit");

        //学期コンボ
        $db = Query::dbCheckOut();
        $opt=$opt_seme=array();
        $query = knjd630Query::getSemesterQuery();
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
            $row = knjd630Query::getTrainRow($model->schregno, $model->gakki);
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
                            "rows"        => 7,
                            "extrahtml"   => "style=\"height:105px;\"",
                            "value"       => $row["TOTALSTUDYTIME"] ));
        $arg["data"]["TOTALSTUDYTIME"] = $objForm->ge("TOTALSTUDYTIME");

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');

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
        $fieldSize  = "TOTALSTUDYTIME=441,";
        $fieldSize .= "SPECIALACTREMARK=315,";
        $fieldSize .= "COMMUNICATION=315";

        //ＣＳＶ出力ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_csv",
                            "value"     => "ＣＳＶ出力",
                            "extrahtml" => " onClick=\" wopen('".REQUESTROOT."/X/KNJX152/knjx152index.php?FIELDSIZE=".$fieldSize."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" ));
        $arg["button"]["btn_csv"] = $objForm->ge("btn_csv");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno
                            ));

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }


        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd630Form1.html", $arg);
    }
}
?>
