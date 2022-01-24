<?php

require_once('for_php7.php');

class knjh060Form1
{
    function main(&$model)
    {       
        $temp_cd = "";
        $arg["disable"] = "";
       //権限チェック
       if (AUTHORITY == DEF_UPDATE_RESTRICT){
               $arg["disable"] = "OnNotUse(".CTRL_SEMESTER.");";
       }

        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh060index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)) {
            $record = knjh060Query::getTrainRow($model->schregno);
        } else {
            $record =& $model->field;
        }

        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $Row2  = $db->getRow(knjh060Query::getSchregno_name($model->schregno),DB_FETCHMODE_ASSOC);

        $arg["SCHREGNO"] = $Row2["SCHREGNO"];
        $arg["NAME"]     = $Row2["NAME"];
        
        $i=0;

        $result = $db->query(knjh060Query::getSemester());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {            
            $row["CHECK_NAME"] = $row["SEMESTER"];

            //配列の要素数が学期マスタと合わない場合に出るWARNINGを回避
            if (in_array($row["SEMESTER"],array_keys($record))) {
                $row["CAUTION_CHECKED"]    = ($record[$row["SEMESTER"]]["caution"] == "1") ? "checked" : "";
                $row["ADMONITION_CHECKED"] = ($record[$row["SEMESTER"]]["admonition"] == "1") ? "checked" : "";
            } else {
                $row["CAUTION_CHECKED"]    = "";
                $row["ADMONITION_CHECKED"] = "";
            }

            $model->semester_arr[$i] = $row["SEMESTER"];

            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

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

        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjh060Form1.html", $arg);
    }
}
?>
