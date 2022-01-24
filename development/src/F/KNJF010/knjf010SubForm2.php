<?php

require_once('for_php7.php');

class knjf010SubForm2
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

/* 編集項目 */
        //チェックボックス
        for ($i=0;$i<17;$i++)
        {
            if ($i==16) {
                $objForm->ae(array("type"       => "checkbox",
                                    "name"      => "RCHECK".$i,
                                    "value"     => "1",
                                    "checked"   => (($model->replace_data["check"][15] == "1") ? 1 : 0),
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
        //尿・１次蛋白コンボ
        $result     = $db->query(knjf010Query::getUric($model));
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
                            "name"        => "ALBUMINURIA1CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["ALBUMINURIA1CD"],
                            "options"     => $opt ));
        $arg["data"]["ALBUMINURIA1CD"] = $objForm->ge("ALBUMINURIA1CD");
        //尿・１次糖コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "URICSUGAR1CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["URICSUGAR1CD"],
                            "options"     => $opt ));
        $arg["data"]["URICSUGAR1CD"] = $objForm->ge("URICSUGAR1CD");
        //尿・１次潜血コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "URICBLEED1CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["URICBLEED1CD"],
                            "options"     => $opt ));
        $arg["data"]["URICBLEED1CD"] = $objForm->ge("URICBLEED1CD");
        //尿・２次蛋白コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "ALBUMINURIA2CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["ALBUMINURIA2CD"],
                            "options"     => $opt ));
        $arg["data"]["ALBUMINURIA2CD"] = $objForm->ge("ALBUMINURIA2CD");
        //尿・２次糖コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "URICSUGAR2CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["URICSUGAR2CD"],
                            "options"     => $opt ));
        $arg["data"]["URICSUGAR2CD"] = $objForm->ge("URICSUGAR2CD");
        //尿・２次潜血コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "URICBLEED2CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["URICBLEED2CD"],
                            "options"     => $opt ));
        $arg["data"]["URICBLEED2CD"] = $objForm->ge("URICBLEED2CD");
        //尿・その他の検査
        $objForm->ae( array("type"        => "text",
                            "name"        => "URICOTHERTEST",
                            "size"        => 40,//NO004
                            "maxlength"   => 20,//NO004
                            "extrahtml"   => "",
                            "value"       => $RowD["URICOTHERTEST"] ));
        $arg["data"]["URICOTHERTEST"] = $objForm->ge("URICOTHERTEST");
        //栄養状態コンボ
        $result     = $db->query(knjf010Query::getNutrition($model));
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
                            "name"        => "NUTRITIONCD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["NUTRITIONCD"],
                            "options"     => $opt ));
        $arg["data"]["NUTRITIONCD"] = $objForm->ge("NUTRITIONCD");
        //脊柱・胸部コンボ
        $result     = $db->query(knjf010Query::getSpinerib($model));
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
                            "name"        => "SPINERIBCD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["SPINERIBCD"],
                            "options"     => $opt ));
        $arg["data"]["SPINERIBCD"] = $objForm->ge("SPINERIBCD");
        //目の疾病及び異常コンボ
        $result     = $db->query(knjf010Query::getEyedisease($model));
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
                            "name"        => "EYEDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["EYEDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["EYEDISEASECD"] = $objForm->ge("EYEDISEASECD");

        //眼科検診結果
        $extra = "";
        $arg["data"]["EYE_TEST_RESULT"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT"], "EYE_TEST_RESULT", 40, 20, $extra);

        //耳鼻咽頭疾患コンボ
        $result     = $db->query(knjf010Query::getNosedisease($model));
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
                            "name"        => "NOSEDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["NOSEDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["NOSEDISEASECD"] = $objForm->ge("NOSEDISEASECD");
        //皮膚疾患コンボ
        $result     = $db->query(knjf010Query::getSkindisease($model));
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
                            "name"        => "SKINDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["SKINDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["SKINDISEASECD"] = $objForm->ge("SKINDISEASECD");
        //心臓・臨床医学的検査コンボ
        $result     = $db->query(knjf010Query::getHeart_medexam($model));
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
                            "name"        => "HEART_MEDEXAM",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["HEART_MEDEXAM"],
                            "options"     => $opt ));
        $arg["data"]["HEART_MEDEXAM"] = $objForm->ge("HEART_MEDEXAM");
        //心臓・疾病及び異常コンボ
        $result     = $db->query(knjf010Query::getHeartdisease($model));
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
                            "name"        => "HEARTDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["HEARTDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["HEARTDISEASECD"] = $objForm->ge("HEARTDISEASECD");

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
        View::toHTML($model, "knjf010SubForm2.html", $arg); 
    }
}
?>

