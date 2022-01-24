<?php

require_once('for_php7.php');

class knjf010SubForm3
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
        $RowH = knjf010Query::getMedexam_hdat($model);    //生徒健康診断ヘッダデータ取得
        $RowD = knjf010Query::getMedexam_det_dat($model); //生徒健康診断詳細データ取得

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
        for ($i=0;$i<15;$i++)
        {
            if ($i==14) {
                $objForm->ae(array("type"       => "checkbox",
                                    "name"      => "RCHECK".$i,
                                    "value"     => "1",
                                    "checked"   => (($model->replace_data["check"][$i] == "1") ? 1 : 0),
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
        //結核・撮影日付
        $RowD["TB_FILMDATE"] = str_replace("-","/",$RowD["TB_FILMDATE"]);
        $arg["data"]["TB_FILMDATE"] = View::popUpCalendar($objForm, "TB_FILMDATE" ,$RowD["TB_FILMDATE"]);
        //結核・所見コンボ
        $result     = $db->query(knjf010Query::getTb_remark($model));
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
                            "name"        => "TB_REMARKCD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:260px;\"",
                            "value"       => $RowD["TB_REMARKCD"],
                            "options"     => $opt ));
        $arg["data"]["TB_REMARKCD"] = $objForm->ge("TB_REMARKCD");

        //結核検査(X線)
        $extra = "";
        $arg["data"]["TB_X_RAY"] = knjCreateTextBox($objForm, $RowD["TB_X_RAY"], "TB_X_RAY", 40, 20, $extra);

        //結核・その他検査コンボ
        $result     = $db->query(knjf010Query::getTb_othertest($model));
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
                            "name"        => "TB_OTHERTESTCD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:260px;\"",
                            "value"       => $RowD["TB_OTHERTESTCD"],
                            "options"     => $opt ));
        $arg["data"]["TB_OTHERTESTCD"] = $objForm->ge("TB_OTHERTESTCD");
        //結核・病名コンボ
        $result     = $db->query(knjf010Query::getTb_Name($model));
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
                            "name"        => "TB_NAMECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:260px;\"",
                            "value"       => $RowD["TB_NAMECD"],
                            "options"     => $opt ));
        $arg["data"]["TB_NAMECD"] = $objForm->ge("TB_NAMECD");
        //結核・指導区分コンボ
        $result     = $db->query(knjf010Query::getTb_Advise($model));
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
                            "name"        => "TB_ADVISECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:260px;\"",
                            "value"       => $RowD["TB_ADVISECD"],
                            "options"     => $opt ));
        $arg["data"]["TB_ADVISECD"] = $objForm->ge("TB_ADVISECD");
        //貧血・所見
        $objForm->ae( array("type"        => "text",
                            "name"        => "ANEMIA_REMARK",
                            "size"        => 20,
                            "maxlength"   => 10,
                            "extrahtml"   => "",
                            "value"       => $RowD["ANEMIA_REMARK"] ));
        $arg["data"]["ANEMIA_REMARK"] = $objForm->ge("ANEMIA_REMARK");
        //貧血・ヘモグロビン値
        $objForm->ae( array("type"        => "text",
                            "name"        => "HEMOGLOBIN",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["HEMOGLOBIN"] ));
        $arg["data"]["HEMOGLOBIN"] = $objForm->ge("HEMOGLOBIN");
        //その他疾病及び異常コンボ
        $result     = $db->query(knjf010Query::getOther_disease($model));
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
                            "name"        => "OTHERDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:260px;\"",
                            "value"       => $RowD["OTHERDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["OTHERDISEASECD"] = $objForm->ge("OTHERDISEASECD");
        //学校医・所見
        $objForm->ae( array("type"        => "text",
                            "name"        => "DOC_REMARK",
                            "size"        => 20,
                            "maxlength"   => 10,
                            "extrahtml"   => "",
                            "value"       => $RowD["DOC_REMARK"] ));
        $arg["data"]["DOC_REMARK"] = $objForm->ge("DOC_REMARK");
        //学校医・所見日付
        $RowD["DOC_DATE"] = str_replace("-","/",$RowD["DOC_DATE"]);
        $arg["data"]["DOC_DATE"] = View::popUpCalendar($objForm, "DOC_DATE" ,$RowD["DOC_DATE"]);
        //事後処置コンボ
        $result     = $db->query(knjf010Query::getTreat($model));
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
                            "name"        => "TREATCD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:260px;\"",
                            "value"       => $RowD["TREATCD"],
                            "options"     => $opt ));
        $arg["data"]["TREATCD"] = $objForm->ge("TREATCD");
        //備考
        $objForm->ae( array("type"        => "text",
                            "name"        => "REMARK",
                            "size"        => 20,
                            "maxlength"   => 10,
                            "extrahtml"   => "",
                            "value"       => $RowD["REMARK"] ));
        $arg["data"]["REMARK"] = $objForm->ge("REMARK");

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
        View::toHTML($model, "knjf010SubForm3.html", $arg); 
    }
}
?>

