<?php
class knjl142kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db           = Query::dbCheckOut();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl142kindex.php", "", "main");

        //年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR + 1;

        if ($model->cmd == "back" || $model->cmd == "back2") {
            $model->StrExamno = $model->StrExamno - 1;
        } else if ($model->cmd == "next" || $model->cmd == "next2") {
            $model->StrExamno = $model->EndExamno + 1;
        } else {
            $model->StrExamno = $model->StrExamno ? $model->StrExamno : "0000";
            $model->EndExamno = $model->EndExamno ? $model->EndExamno : "0000";
        }

        //生徒抽出
        $result = $db->query(knjl142kQuery::SelectQuery($model));
        $cnt = 1;
        $rowArray;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rowArray[$cnt] = $row;
            $cnt++;
            if ($cnt > 100) {
                break;
            }
        }
        $result->free();

        $cnt = 1;
        if (is_array($rowArray)) {
            if ($model->cmd == "back" || $model->cmd == "back2") {
                krsort($rowArray);
            } else if ($model->cmd == "next" || $model->cmd == "next2") {
                ksort($rowArray);
            }
            foreach ($rowArray as $key => $val) {
                if ($cnt == 1) {
                    $model->StrExamno = $val["EXAMNO"];
                }
                $model->EndExamno = $val["EXAMNO"];
                $checkVal = $val["EXAMNO"];
                $val["DELCHK"] = createCheckBox($objForm, "DELCHK", $checkVal, "", "1");
                $arg["data"][]  = $val;
                $cnt++;
            }
        }

        $dataCnt = $db->getOne(knjl142kQuery::getExam($model, "BACK"));
        $dispBack = 0 < $dataCnt ? "" : "disabled ";
        $dataCnt = $db->getOne(knjl142kQuery::getExam($model, "NEXT"));
        $dispNext = 0 < $dataCnt ? "" : "disabled ";

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => " << ",
                            "extrahtml"   => $dispBack."onClick=\"btn_submit('back2');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => " >> ",
                            "extrahtml"   => $dispNext."onClick=\"btn_submit('next2');\"" ) );

        $arg["TOP"]["button"] = $objForm->ge("btn_back").$objForm->ge("btn_next");

        Query::dbCheckIn($db);
        //ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onClick=\"btn_submit('update');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update_back",
                            "value"       => "更新後前の100名",
                            "extrahtml"   => $dispBack."onClick=\"btn_submit('back');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update_next",
                            "value"       => "更新後次の100名",
                            "extrahtml"   => $dispNext."onClick=\"btn_submit('next');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onClick=\"btn_submit('reset');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        
        $arg["btn_update"]  = $objForm->ge("btn_update");
        $arg["update_back"] = $objForm->ge("btn_update_back");
        $arg["update_next"] = $objForm->ge("btn_update_next");
        $arg["btn_reset"]   = $objForm->ge("btn_reset");
        $arg["btn_end"]     = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_TESTDIV",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_TESTSUBCLASSCD",
                            "value"     => "") );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl142kForm1.html", $arg); 
    }
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}

?>
