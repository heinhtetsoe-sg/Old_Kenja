<?php

require_once('for_php7.php');

class knjd636Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd636index.php", "", "edit");

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $semester = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
            $row = knjd636Query::getTrainRow($semester, $model->schregno);
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        
        //通信欄
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALSTUDYTIME",
                            "cols"        => 30,
                            "rows"        => 8,
                            "extrahtml"   => "style=\"height:118px;width: 240px;\"",
                            "value"       => $row["TOTALSTUDYTIME"] ));
        $arg["data"]["TOTALSTUDYTIME"] = $objForm->ge("TOTALSTUDYTIME");

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $query = knjd636Query::getSemeter(CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $semester = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        $extra = "onChange=\"btn_submit('edit')\";";
        $arg["SEMESTER"] = createCombo($objForm, "SEMESTER", $semester, $opt, $extra, 1);

        //DB切断
        Query::dbCheckIn($db);


/*        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の生徒へ",
                            "extrahtml" =>  " onclick=\"return updateNextStudent('".$model->schregno ."', 1);\" style=\"width:130px\""));
        $arg["button"]["btn_up_pre"]    = $objForm->ge("btn_up_pre");

        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の生徒へ",
                            "extrahtml" =>  " onclick=\"return updateNextStudent('".$model->schregno ."', 0);\" style=\"width:130px\""));
        $arg["button"]["btn_up_next"]    = $objForm->ge("btn_up_next");
*/

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');

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

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd636Form1.html", $arg);
    }
}
//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}
?>
