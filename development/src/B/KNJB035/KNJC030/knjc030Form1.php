<?php

require_once('for_php7.php');

class knjc030Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjc030index.php", "", "edit");

        switch($model->sec_competence){
            case DEF_UPDATE_RESTRICT: //制限付更新可
                $my_Class = knjc030Query::get_myClass($model->cntl_dt_key, $model->staffcd);
                break;
            case DEF_UPDATABLE:       //更新可
            case DEF_REFER_RESTRICT:  //制限付参照        
            case DEF_REFERABLE:       //参照のみ
            case DEF_NOAUTH:          //権限無し
                default:
                break;
        }

        //年度内の処理のみを行う。
        if(!$model->checkCtrlDay($model->cntl_dt_key)){
            $reset_day = knjc030Query::keyMoverQuery($model->cntl_dt_year."-04-01");
            $model->cntl_dt_key = ($reset_day != "")? $reset_day : $model->cntl_dt_year."-04-01" ;
        }

        $thisMonth = explode("-",$model->cntl_dt_key);
        //学期取得
        $db = Query::dbCheckOut();
        //SQL文発行(科目名を保持)
        $query = knjc030Query::getTerm($model->cntl_dt_year,$thisMonth[1]);
        $termIs = $db->getOne($query);
        Query::dbCheckIn($db);

        //カレンダーコントロール
        $arg["control"]["executedate"] = View::popUpCalendar($objForm, "executedate",
                                                             str_replace("-","/",$model->cntl_dt_key),"reload=true");

        //前のデータへボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_before",
                            "value"     => "<< 前のデータ",
                            "extrahtml" => "style=\"width:110px\"onclick=\"return btn_submit('read_before');\"" ) );

        $arg["btn_before"] = $objForm->ge("btn_before");

        //次のデータへボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_next",
                            "value"     => "次のデータ >>",
                            "extrahtml" => "style=\"width:110px\"onclick=\"return btn_submit('read_next');\"" ) );

        $arg["btn_next"] = $objForm->ge("btn_next");

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cntl_dt_year",
                            "value"     => $model->cntl_dt_year,
                            "extrahtml" => " onLoad=\"return closing_window();\"") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cntl_dt_key",
                            "value"     => $model->cntl_dt_key) );

        $arg["TOP"] = array("CONFIRMATION"  => "処理年度：".$model->cntl_dt_year."年度 ",
                            "GC"            => "年組");

        //----------------------以下、擬似フレーム内リスト表示----------------------

        $db = Query::dbCheckOut();
        $query = knjc030Query::get_class_data($model,$termIs);
        $result = $db->query($query);

        //SQL文発行(クラス情報を保持)
        $class_cnt = 0;
        while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $class_data[$class_cnt] = $Row["GR_CL"];
            $class_name_show[$Row["GR_CL"]] = $Row["HR_NAME"];
            $class_cnt++ ;
        }

        //SQL文発行(科目名を保持)
        $query = knjc030Query::find_class();
        $result = $db->query($query);
        while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $class_box[$Row["SECTIONCD"]] = $Row["SECTIONABBV"];
        }

        //SQL文発行(STAFFCDと名前、SECTIONNAME を保持)
        $query = knjc030Query::get_staff_data($model->cntl_dt_year);
        $result = $db->query($query);
        while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $staffcd[]    = $Row["STAFFCD"];
            $staff_name[$Row["STAFFCD"]] = $Row["SHOW_NAME"];
            $staff_job[$Row["STAFFCD"]]  = $Row["SECTIONNAME"];
        }

        //SQL文発行(全体の情報を保持)
        $query = knjc030Query::readQuery($model,$termIs);
        $result = $db->query($query);

        //変数初期化
        $data_all = array();
        $Jump_user = array();
        $multi_flg_tmp = array();
        $multi_flg = array();
        $subclassabbv_box = array();

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //名前を表示用(3文字)に修正
            $row["NAME_SHOW"] = trim($row["NAME_SHOW"]);
            $name_count = mbstrlen($row["NAME_SHOW"]);
            if($name_count >= 3){
                $row["NAME_SHOW"] = mbsubstr(trim($row["NAME_SHOW"]),0,3);
            }

            $row["NAME_SHOW"] = ":".(str_replace("　","",$row["NAME_SHOW"]));

            if(!isset($subclassabbv_box[$row["TARGETCLASS"]][$row["PERIODCD"]])){
                $subclassabbv_box[$row["TARGETCLASS"]][$row["PERIODCD"]] = "";
            }
            //データを配列に保持
            $subclassabbv_box[$row["TARGETCLASS"]][$row["PERIODCD"]] .= $row["SUBCLASSABBV"].$row["NAME_SHOW"]."<BR>";

            //初期化
            if(!isset($class_box[$row["SECTIONCD"]])){
                $class_box[$row["SECTIONCD"]] = "";
            }

            $data_all[$row["TARGETCLASS"]][] = array( "STAFFCD"        => $row["STAFFCD"],
                                                      "PERIODCD"       => $row["PERIODCD"],
                                                      "CHAIRCD"        => $row["CHAIRCD"],
                                                      "TARGETCLASS"    => $row["TARGETCLASS"],
                                                      "SUBCLASSABBV"   => $row["SUBCLASSABBV"],
                                                      "ATTENDCD"       => $row["ATTENDCD"],
                                                      "NAME_SHOW"      => $row["NAME_SHOW"],
                                                      "SUBJECT"        => $class_box[$row["SECTIONCD"]]);
            if($row["PERIODCD"] == 1){
                $Jump_user[$row["TARGETCLASS"]][] = array("STAFFCD" => $row["STAFFCD"]);
                $multi_flg_tmp[$row["TARGETCLASS"]][$row["STAFFCD"]][] = $row["CHAIRCD"];
                if(get_count($multi_flg_tmp[$row["TARGETCLASS"]][$row["STAFFCD"]]) >= 2){
                    $multi_flg[$row["TARGETCLASS"]][$row["STAFFCD"]] = "m";
                }
            
            }
        }

        //初期化
        $ay = array();
        $first = "true";
        $a = get_count($class_data);
        $Jump_target = "";

        //リンク先作成
        $query = knjc030Query::getPath();
        $link = $db->getOne($query);
        $link = REQUESTROOT.$link."/knjc010index.php";
        $link_multi = REQUESTROOT."/C/KNJC030_1/knjc030_1index.php";

        //表示用データ
        for ($i = 0; $i < $a; $i++){

            $list_tag["backColor"] = "";

            //配列を初期化
            $list_tag = array();
            for ($cnt=0; $cnt<10; $cnt++)
            {
                $list_tag["PERIODCD".$cnt] = "<td width=\"89\" height=\"40\" nowrap></td>";
            }

            $ay = (isset($data_all[$class_data[$i]]))?  $data_all[$class_data[$i]] : array() ;
            $b = get_count($ay);

            if($b == 0){
                $b ++;
                $list_tag["TARGETCLASS"] = $class_name_show[$class_data[$i]];
                $list_tag["SUBJECT"] = $staff_job[$staffcd[$i]];
            }else{

                $list_tag["TARGETCLASS"] = $class_name_show[$class_data[$i]];

                for ($ii = 0; $ii < $b; $ii++ )
                {
                    foreach( ($data_all[$class_data[$i]][$ii]) as $key => $val)
                    {
                        switch ($key)
                        {
                            case "PERIODCD":
                                $set_target = $key.$val;
                                $id_val = $val;
                                //SHRに授業がある場合のみ && 権限チェック
                                if($val == 1 && ($model->sec_competence == DEF_UPDATABLE || 
                                  ($model->sec_competence == DEF_UPDATE_RESTRICT &&
                                   STAFFCD == $Jump_user[$class_data[$i]][0]["STAFFCD"]))){
                                    $list_tag["Jump_target"] = ($Jump_target == "")? "<a name=\"Jump_here\"></a>" : "";
                                    $Jump_target = "off";
                                    $list_tag["TARGETCLASS"] = "<a href=\"#\" onclick=\"windowOpener('";

                                    if(isset($multi_flg[$class_data[$i]][STAFFCD]) && 
                                       $model->sec_competence == DEF_UPDATE_RESTRICT){

                                        $list_tag["TARGETCLASS"] .= $link_multi."','".$Jump_user[$class_data[$i]][0]["STAFFCD"]."','".$model->cntl_dt_key."','".$class_data[$i]."','multi');\">".$class_name_show[$class_data[$i]]."</a>";

                                    }else if(get_count($multi_flg_tmp[$class_data[$i]]) >= 2 && 
                                             $model->sec_competence == DEF_UPDATABLE){

                                        $list_tag["TARGETCLASS"] .= $link_multi."','".$Jump_user[$class_data[$i]][0]["STAFFCD"]."','".$model->cntl_dt_key."','".$class_data[$i]."','multiA');\">".$class_name_show[$class_data[$i]]."</a>";


                                    }else{
                                        $list_tag["TARGETCLASS"] .= $link."','".$Jump_user[$class_data[$i]][0]["STAFFCD"]."','".$model->cntl_dt_key."','','".$multi_flg_tmp[$class_data[$i]][$Jump_user[$class_data[$i]][0]["STAFFCD"]][0]."');\">".$class_name_show[$class_data[$i]]."</a>";
                                    }

                                }
                                $noFound = $subclassabbv_box[$class_data[$i]][$val];
                                break;

                            case "SUBCLASSABBV":
                                $subclassabbv = $val;
                                break;

                            case "TARGETCLASS":
                                $targetclass = $val;
                                break;

                            case "NAME_SHOW":
                                $name_show = "  ".$val;
                                break;

                            case "ATTENDCD":
                                if($model->staffcd == $staffcd[$i] && $first == "true")
                                {
                                    $first = "false" ;
                                    $first_id_val = $id_val.",".$staffcd[$i].",".$src_color ;
                                }

#                                if ($val == "0"){
#                                    $bgcolor = "bgcolor=\"#ff0099\"";
#                                }else{
#                                    $bgcolor = "bgcolor=\"#3399ff\"";

                                if ($val == "1"){
                                    $bgcolor = "bgcolor=\"#3399ff\"";
                                }else{
                                    $bgcolor = "bgcolor=\"#ff0099\"";
                                }

                            default:
                                $list_tag[$key] = $val;
                        }
                    }
                    $width_num = ($id_val == 9) ? "\"90\" " : "\"89\"";
                    $set_tag  = "<td nowrap width=".$width_num. $bgcolor ;
                    $set_tag .= " id=\"".$id_val.",".$staffcd[$i] ;
                    $set_tag .= "\" ><FONT SIZE=\"2\">".$noFound."</FONT></td>";

                    $list_tag["$set_target"] = $set_tag ;
                }
            }
            $arg["data"][] = $list_tag;
        }

        //フォーカス対象無し
        if($first == "true"){
            $first = "off";
        }else{
            $first = "first";
        }

        //終 了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => " 終 了 ",
                            "extrahtml" => " onclick=\"return closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        $objForm->ae( array("type"  => "hidden",
                            "name"  => "output_CtrlDate",
                            "value" => $model->attnd_cntl_dt));

        $objForm->ae( array("type"  => "hidden",
                            "name"  => "Security",
                            "value" => ($model->sec_competence == DEF_UPDATE_RESTRICT) ? 'update_restrict' : 'updatable' ) );

        $result->free();
        Query::dbCheckIn($db);

        //処理が完了、又は権限が無ければ閉じる。
        if($model->cntl_dt_year == ""){
            $arg["Closing"] = "  closing_window('year'); " ;
        }else if($model->sec_competence == DEF_NOAUTH){
            $arg["Closing"] = "  closing_window('cm'); " ;
        }

        $arg["jumping"]    = " Page_jumper('".REQUESTROOT."/C/KNJC030/knjc030index.php#Jump_here'); ";

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc030Form1.html", $arg);
    }
}
?>
