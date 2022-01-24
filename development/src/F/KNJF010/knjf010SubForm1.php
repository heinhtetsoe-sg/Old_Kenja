<?php

require_once('for_php7.php');

class knjf010SubForm1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjf010index.php", "", "sel");

        //生徒一覧
        $opt_left = $opt_right = array();
        //置換処理選択時の生徒の情報
        $array = explode(",", $model->replace_data["selectdata"]);
        if ($array[0]=="") $array[0] = $model->schregno;
        //生徒情報    
        $RowH = knjf010Query::getMedexam_hdat($model);      //生徒健康診断ヘッダデータ取得
        $RowD = knjf010Query::getMedexam_det_dat($model);   //生徒健康診断詳細データ取得

        $db = Query::dbCheckOut();

        $result   = $db->query(knjf010Query::GetStudent($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if(!in_array($row["SCHREGNO"],$array)){
                $opt_right[]  = array("label" => $row["ATTENDNO"]." ".$row["SCHREGNO"]." ".$row["NAME_SHOW"],
                                      "value" => $row["SCHREGNO"]);
            }else{
                $opt_left[]   = array("label" => $row["ATTENDNO"]." ".$row["SCHREGNO"]." ".$row["NAME_SHOW"],
                                      "value" => $row["SCHREGNO"]);
            }
        }
        $result->free();

/* 編集項目 */
        //チェックボックス
        for ($i=0;$i<10;$i++)
        {
            if ($i==9) {
                $objForm->ae(array("type"       => "checkbox",
                                    "name"      => "RCHECK".$i,
                                    "value"     => "1",
                                    "checked"   => (($model->replace_data["check"][9] == "1") ? 1 : 0),
                                    "extrahtml" => "onClick=\"return check_all(this);\""));
                $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
            } else {
                $objForm->ae(array("type"       => "checkbox",
                                   "name"       => "RCHECK".$i,
                                   "value"      => "1",
                                   "checked"    => (($model->replace_data["check"][$i] == "1") ? 1 : 0)));
                $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
            }
        }
        //健康診断実施日付
        if($RowH["DATE"] == ""){
            $RowH["DATE"] = CTRL_DATE;
        }
        $RowH["DATE"] = str_replace("-","/",$RowH["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE" ,$RowH["DATE"]);
        //視力・右裸眼（数字）
        $objForm->ae( array("type"        => "text",
                            "name"        => "R_BAREVISION",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "",
                            "value"       => $RowD["R_BAREVISION"] ));
        $arg["data"]["R_BAREVISION"] = $objForm->ge("R_BAREVISION");
        //視力・右矯正（数字）
        $objForm->ae( array("type"        => "text",
                            "name"        => "R_VISION",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "",
                            "value"       => $RowD["R_VISION"] ));
        $arg["data"]["R_VISION"] = $objForm->ge("R_VISION");
        //視力・左裸眼（数字）
        $objForm->ae( array("type"        => "text",
                            "name"        => "L_BAREVISION",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "",
                            "value"       => $RowD["L_BAREVISION"] ));
        $arg["data"]["L_BAREVISION"] = $objForm->ge("L_BAREVISION");
        //視力・左矯正（数字）
        $objForm->ae( array("type"        => "text",
                            "name"        => "L_VISION",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "",
                            "value"       => $RowD["L_VISION"] ));
        $arg["data"]["L_VISION"] = $objForm->ge("L_VISION");

        //視力・右裸眼（文字）
        makeTextBox($objForm, $arg, "R_BAREVISION_MARK", 1, 1, $RowD["R_BAREVISION_MARK"], "");

        //視力・右矯正（文字）
        makeTextBox($objForm, $arg, "R_VISION_MARK", 1, 1, $RowD["R_VISION_MARK"], "");

        //視力・左矯正（文字）
        makeTextBox($objForm, $arg, "L_BAREVISION_MARK", 1, 1, $RowD["L_BAREVISION_MARK"], "");

        //視力・左裸眼（文字）
        makeTextBox($objForm, $arg, "L_VISION_MARK", 1, 1, $RowD["L_VISION_MARK"], "");

        //聴力・右DB
        $objForm->ae( array("type"        => "text",
                            "name"        => "R_EAR_DB",
                            "size"        => 4,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["R_EAR_DB"] ));
        $arg["data"]["R_EAR_DB"] = $objForm->ge("R_EAR_DB");
        //聴力・右状態コンボ
        $optnull    = array("label" => "","value" => "");   //初期値：空白項目
        $result     = $db->query(knjf010Query::getLR_EAR($model));
        $opt        = array();
        $opt[]      = $optnull;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $namecd = substr($row["NAMECD2"],0,2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "R_EAR",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["R_EAR"],
                            "options"     => $opt ));
        $arg["data"]["R_EAR"] = $objForm->ge("R_EAR");
        //聴力・左DB
        $objForm->ae( array("type"        => "text",
                            "name"        => "L_EAR_DB",
                            "size"        => 4,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["L_EAR_DB"] ));
        $arg["data"]["L_EAR_DB"] = $objForm->ge("L_EAR_DB");
        //聴力・左状態コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "L_EAR",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["L_EAR"],
                            "options"     => $opt ));
        $arg["data"]["L_EAR"] = $objForm->ge("L_EAR");

        Query::dbCheckIn($db);

/* ボタン作成 */
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit()\"" ) );
        //戻るボタン
        $link = REQUESTROOT."/F/KNJF010/knjf010index.php?cmd=back&ini2=1";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"window.open('$link','_self');\"" ) );
        $arg["BUTTONS"] = $objForm->ge("btn_update")."    ".$objForm->ge("btn_back");
        //対象者一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "left_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ",
                            "options"     => $opt_left));
        //生徒一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "right_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ",
                            "options"     => $opt_right));
        //全て追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','left_select','right_select',1);\"" ) );
        //追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','left_select','right_select',1);\"" ) );
        //削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','left_select','right_select',1);\"" ) );
        //全て削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','left_select','right_select',1);\"" ) ); 
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("left_select"),
                                   "RIGHT_PART"  => $objForm->ge("right_select"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));
/* ヘッダ */
        $arg["info"]    = array("TOP"        =>  $model->year."年度  "
                                                .$model->control_data["学期名"][$model->semester]
                                                ."  対象クラス  ".$model->Hrname,
                                "LEFT_LIST"  => "対象者一覧",
                                "RIGHT_LIST" => "生徒一覧");
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );  
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "REPLACEHIDDENDATE",
                            "value"     => $RowH["DATE"]) );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf010SubForm1.html", $arg); 
    }
}

function makeTextBox($objForm, $arg, $name, $size, $maxlength, $value, $extra="onblur=\"return Num_Check(this);\"") {
        $objForm->ae( array("type"        => "text",
                            "name"        => $name,
                            "size"        => $size,
                            "maxlength"   => $maxlength,
                            "extrahtml"   => $extra,
                            "value"       => $value ));
        $arg["data"][$name] = $objForm->ge($name);
}
?>

