<?php

require_once('for_php7.php');

//タイトル行の行数
# define("DEF_LINE_COUNT",14);
define("DEF_LINE_COUNT",5);
class knjc030Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjc030index.php", "", "edit");

        switch(AUTHORITY){
            case DEF_UPDATE_RESTRICT: //制限付更新可
                $my_Class = knjc030Query::get_myClass($model->cntl_dt_key, $model->staffcd);    //自分の受け持っているクラスのみ！！
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
        /*** ADD 2005/11/04 by ameku ***/
        $wday = array("(日)","(月)","(火)","(水)","(木)","(金)","(土)");
        $w = date("w",strtotime($model->cntl_dt_key));
        $arg["CNTL_DT_KEY"] = str_replace("-","/",$model->cntl_dt_key).$wday[$w];
        /*** ADD 2005/11/04 by ameku ***/

        //学期取得
        $db = Query::dbCheckOut();
        //SQL文発行(科目名を保持)

        $query = knjc030Query::getTerm($model->cntl_dt_year,$model->cntl_dt_key);
        $termIs = $db->getOne($query);
        Query::dbCheckIn($db);

        //カレンダーコントロール
        $arg["control"]["executedate"] = View::popUpCalendar($objForm, "executedate",
                                                             str_replace("-","/",$model->cntl_dt_key),"reload=true");

        //前日へボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_before",
                            "value"     => "<< 前日",
                            "extrahtml" => "style=\"width:110px\"onclick=\"return btn_submit('read_before');\"" ) );

        $arg["btn_before"] = $objForm->ge("btn_before");

        //翌日へボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_next",
                            "value"     => "翌日 >>",
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


        $db = Query::dbCheckOut();

        //校時名称の取得
        $query = knjc030Query::getNamecd($model->cntl_dt_year, "B001");
        $result = $db->query($query);
        $i = 0;
        $title   = array();
        $title["TARGETCLASS"] = "<th width=\"60\" align=\"center\" nowrap class=\"no_search\">クラス</th>";
        $title_count = $result->numRows();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($i == 10) {
                //クラスを挿入
                $title["TARGETCLASS2"] = $title["TARGETCLASS"];
            }
            if ($i == $title_count-1){
                $width = 110;
            }else{
                $width = 89;
            }
            $title["PERIODCD".$i] = "<th width=\"".$width."\" nowrap align=\"center\" class=\"no_search\">".$row["NAME1"]."</th>";
            $period_cd[]    = $row["NAMECD2"];
            $lastid         = $row["NAMECD2"];
            $i++;
        }
        if ($title_count <= 10){
            $arg["DHEADER"] = true;
        }
        /*** ADD 2005/11/04 by ameku ***/
        $arg["TITLE"] = $title;
        /*** ADD 2005/11/04 by ameku ***/

        //----------------------以下、擬似フレーム内リスト表示----------------------

        //表示項目取得
        $query  = knjc030Query::getDispCol();
        $result = $db->query($query);
        $model->DispCol = $db->getOne($query);

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
        $query = knjc030Query::readQuery($model,$termIs,$model->Properties["useTestCountflg"]);
        $result = $db->query($query);

        //変数初期化
        $data_all           = array();
        $shr_chair      = array();
        $subclassabbv_box   = array();
        $attendCnt   = array();
        $chairCnt    = array();

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //名前を表示用(3文字)に修正
            $row["NAME_SHOW"]   = trim($row["NAME_SHOW"]);
            $name_count         = mb_strlen($row["NAME_SHOW"]);
            if($name_count >= 3){
                $row["NAME_SHOW"] = mb_substr(trim($row["NAME_SHOW"]),0,3);
            }

            $row["NAME_SHOW"] = ":".(str_replace("　","",$row["NAME_SHOW"]));

            if(!isset($subclassabbv_box[$row["TARGETCLASS"]][$row["PERIODCD"]])){
                $subclassabbv_box[$row["TARGETCLASS"]][$row["PERIODCD"]] = "";
            }
            //データを配列に保持
            if ($model->DispCol == 1) {
                $subclassabbv_box[$row["TARGETCLASS"]][$row["PERIODCD"]] .= $row["SUBCLASSABBV"].$row["NAME_SHOW"]."<BR>";
            } else {
                $subclassabbv_box[$row["TARGETCLASS"]][$row["PERIODCD"]] .= $row["CHAIRNAME"].$row["NAME_SHOW"]."<BR>";
            }
            //出欠と講座数
            $attendCnt[$row["TARGETCLASS"]][$row["PERIODCD"]] += $row["ATTENDCD"];
            $chairCnt[$row["TARGETCLASS"]][$row["PERIODCD"]]++;

            //初期化
            if(!isset($class_box[$row["SECTIONCD"]])){
                $class_box[$row["SECTIONCD"]] = "";
            }

            $data_all[$row["TARGETCLASS"]][] = array( "STAFFCD"        => $row["STAFFCD"],
                                                      "PERIODCD"       => $row["PERIODCD"],
                                                      "CHAIRCD"        => $row["CHAIRCD"],
                                                      "TARGETCLASS"    => $row["TARGETCLASS"],
                                                      "SUBCLASSABBV"   => ($model->DispCol == 1) ? $row["SUBCLASSABBV"] : $row["CHAIRNAME"],
                                                      "ATTENDCD"       => $row["ATTENDCD"],
                                                      "NAME_SHOW"      => $row["NAME_SHOW"],
                                                      "TR_CD1"         => $row["TR_CD1"],
                                                      "SUBJECT"        => $class_box[$row["SECTIONCD"]],
                                                      "SHR"            => $row["SHR"],
                                                      "COUNTFLG"       => $row["COUNTFLG"],
                                                      "LESSON_MODE"    => $row["LESSON_MODE"],
                                                      );


            if($row["SHR"] !== ""){
                $shr_chair[$row["TARGETCLASS"]]["STAFFCD"][] = $row["STAFFCD"]; //SHRのすべての講座担当職員
                $shr_chair[$row["TARGETCLASS"]]["CHAIRCD"][] = $row["CHAIRCD"]; //SHRのすべての講座コード
                $shr_chair[$row["TARGETCLASS"]]["CHAIR"][$row["STAFFCD"]] = $row["CHAIRCD"];
            }
        }

        //初期化
        $ay = array();
        $first = "true";
        $add_flg = 0;
        $a = get_count($class_data);
        $Jump_target = "";

        //リンク先作成
        $link = "/C/KNJC010";
        $link = REQUESTROOT.$link."/knjc010index.php";
        $link_multi = REQUESTROOT."/C/KNJC030_1/knjc030_1index.php";

        //表示用データ
        $periodcd_change = array();
        for ($i = 0; $i < $a; $i++){

            $list_tag = array();
            
           if ($title_count > 10 && ($add_flg == 0 || $add_flg == DEF_LINE_COUNT)) {
                //タイトルを挿入
                $arg["data"][] = $title;
                $add_flg = 1;
            }

            $list_tag["TARGETCLASS"] = "<td width=\"60\" nowrap align=\"center\"><font color=\"#000000\">".$class_name_show[$class_data[$i]]."</font></td>"; 

            //配列を初期化
            for ($ii = 0; $ii < get_count($period_cd); $ii++ ) {

                if($ii == 10) {
                    //クラスを挿入
                    $list_tag["TARGETCLASS2"] = $list_tag["TARGETCLASS"];
                }

                if ($period_cd[$ii] != $lastid) {
                    $list_tag["PERIODCD".$ii] = "<td width=\"89\" height=\"40\" nowrap></td>";
                } else {
                    $list_tag["PERIODCD".$ii] = "<td width=\"90\" height=\"40\" nowrap></td>";
                }
                $periodcd_change[$period_cd[$ii]] = $ii;
            }

            $ay = (isset($data_all[$class_data[$i]]))?  $data_all[$class_data[$i]] : array() ;
            $b = get_count($ay);

            $id_val2 = "*";
            if($b == 0){
                $b ++;
                $list_tag["SUBJECT"] = $staff_job[$staffcd[$i]];
            }else{

                for ($ii = 0; $ii < $b; $ii++ )
                {
                    $attendSum = "";
                    $chairSum  = "";
                    $fontcolor = "";
                    foreach( ($data_all[$class_data[$i]][$ii]) as $key => $val)
                    {
                        switch ($key)
                        {
                            case "PERIODCD":
                                $set_target = $key.$periodcd_change[$val];
                                if ($id_val2 != $val) {
                                    $setCountFlg = array("ON" => "0", "OFF" => "0");
                                    //Key:LESSON_MODE Val:1 (1コマのLESSON_MODE分配列が出来る)
                                    $setLessonModeData = array();
                                    //名称マスタにLESSON_MODEがない場合に ON
                                    $setLessonModeFlg = "";
                                }
                                $id_val = $val;
                                $id_val2 = $val;
                                $show_flg = "";
                                $periodcd="";

                                if ($data_all[$class_data[$i]][$ii]["SHR"] != "" && AUTHORITY == DEF_UPDATABLE) {                       //更新可ユーザの場合リンク文字にする
                                    if (get_count($shr_chair[$class_data[$i]]["CHAIRCD"]) > 1) {         //SHRの講座が複数ある場合、子画面表示
                                        $show_flg = "1";
                                        $periodcd = $data_all[$class_data[$i]][$ii]["PERIODCD"];
                                    } else {
                                        $show_flg = "2"; 
                                        $periodcd = $data_all[$class_data[$i]][$ii]["PERIODCD"];
                                        $l_chair = $shr_chair[$class_data[$i]]["CHAIRCD"][0];
                                    }
                                } else if ($data_all[$class_data[$i]][$ii]["SHR"] != ""  && AUTHORITY == DEF_UPDATE_RESTRICT) {          //更新制限付ユーザの場合リンク文字にする

                                    if (in_array(STAFFCD, $shr_chair[$class_data[$i]]["STAFFCD"])) { //担当講座がある場合、直リンク
                                        $show_flg = "2"; 
                                        $periodcd = $data_all[$class_data[$i]][$ii]["PERIODCD"];
                                        $l_chair = $shr_chair[$class_data[$i]]["CHAIR"][STAFFCD];

                                    } elseif (in_array($class_data[$i], $my_Class)) {                //担当クラスユーザの場合
                                        if (get_count($shr_chair[$class_data[$i]]["CHAIRCD"]) > 1) {     //講座が複数ある場合
                                            $show_flg = "1";
                                            $periodcd = $data_all[$class_data[$i]][$ii]["PERIODCD"];
                                        } else {
                                            $show_flg = "2"; 
                                            $periodcd = $data_all[$class_data[$i]][$ii]["PERIODCD"];
                                            $l_chair = $shr_chair[$class_data[$i]]["CHAIRCD"][0];
                                        }
                                    }
                                }

                                //子画面
                                if ($show_flg == "1") {
                                    $list_tag["Jump_target"] = ($Jump_target == "")? "<a name=\"Jump_here\"></a>" : "";
                                    $Jump_target = "off";
                                    $list_tag["TARGETCLASS"] = "<td width=\"60\" nowrap align=\"center\"><font color=\"#000000\">"; 
                                    $list_tag["TARGETCLASS"].= "<a href=\"#\" onclick=\"windowOpener('";
                                    $list_tag["TARGETCLASS"].= $link_multi."','','".$model->cntl_dt_key."','".$class_data[$i]."','chair','".$periodcd."');\">".$class_name_show[$class_data[$i]]."</a></font></td>";

                                //出欠入力直リンク
                                } elseif ($show_flg == "2") {
                                    $list_tag["Jump_target"] = ($Jump_target == "")? "<a name=\"Jump_here\"></a>" : "";
                                    $Jump_target = "off";
                                    $list_tag["TARGETCLASS"] = "<td width=\"60\" nowrap align=\"center\"><font color=\"#000000\">"; 
                                    $list_tag["TARGETCLASS"].= "<a href=\"#\" onclick=\"windowOpener('";
                                    $list_tag["TARGETCLASS"].= $link."','".STAFFCD."','".$model->cntl_dt_key."','".$data_all[$class_data[$i]][$ii]["TR_CD1"]."','".$l_chair."','".$periodcd."');\">".$class_name_show[$class_data[$i]]."</a></font></td>";
                                }
                                $noFound = $subclassabbv_box[$class_data[$i]][$val];
                                $attendSum = $attendCnt[$class_data[$i]][$val];
                                $chairSum  = $chairCnt[$class_data[$i]][$val];

                                if ($attendSum == $chairSum) {
                                    $bgcolor = "bgcolor=\"#3399ff\"";
                                } else if ($attendSum == "0") {
                                    $bgcolor = "bgcolor=\"#ff0099\"";
                                } else {
                                    $bgcolor = "bgcolor=\"#ffff00\"";
                                    $fontcolor = "color=\"black\"";
                                }

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

                            case "COUNTFLG":
                                if ($val == "1") {
                                    $setCountFlg["ON"] = "1";
                                } else if ($val == "0") {
                                    $setCountFlg["OFF"] = "1";
                                }
                                break;

                            case "LESSON_MODE":
                                $setLessonModeData[$val] = "1";
                                $setLessonModeFlg = $val ? $setLessonModeFlg : "ON";
                                break;

                            case "ATTENDCD":
                                if($model->staffcd == $staffcd[$i] && $first == "true")
                                {
                                    $first = "false" ;
                                    $first_id_val = $id_val.",".$staffcd[$i].",".$src_color ;
                                }

                            default:
                                $list_tag[$key] = $val;
                        }
                    }
                    $width_num = ($id_val == $lastid) ? "\"90\" " : "\"89\"";

                    $setMaruBatu = "";
                    if ($setCountFlg["ON"] == "1" && $setCountFlg["OFF"] == "0") {
                        $setMaruBatu = "○";
                    } else if ($setCountFlg["ON"] == "1" && $setCountFlg["OFF"] == "1") {
                        $setMaruBatu = "△";
                    } else {
                        $setMaruBatu = "×";
                    }

                    $setLessonMode = "";
                    if ($setLessonModeFlg == "ON") {
                        $setLessonMode = "※";
                    } else if (get_count($setLessonModeData) > 1) {
                        $setLessonMode = "混";
                    } else if (get_count($setLessonModeData) == 1) {
                        $query = knjc030Query::getLessonName("");
                        foreach ($setLessonModeData as $lessonMode => $lessonVal) {
                            $query = knjc030Query::getLessonName($lessonMode);
                            if ($lessonMode === "00") {
                                $setMaruBatu = "";
                            }
                        }
                        $setLessonMode = $db->getOne($query);
                    }

                    $set_tag  = "<td nowrap width=".$width_num. $bgcolor ;
                    $set_tag .= " id=\"".$id_val.",".$staffcd[$i] ;
                    $set_tag .= "\" ><FONT SIZE=\"2\" ".$fontcolor.">".$setMaruBatu.$setLessonMode."<BR>".$noFound."</FONT></td>";

                    $list_tag["$set_target"] = $set_tag ;
                }
            }

            if (strlen($list_tag["TARGETCLASS2"])) {
                $list_tag["TARGETCLASS2"] = $list_tag["TARGETCLASS"];
            }

            $arg["data"][] = $list_tag;
            $add_flg++;
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
                            "value" => (!IS_KANRISYA) ? 'update_restrict' : 'updatable' ) );

        $result->free();
        Query::dbCheckIn($db);

        //処理が完了、又は権限が無ければ閉じる。
        if($model->cntl_dt_year == ""){
            $arg["Closing"] = "  closing_window('year'); " ;
        }else if(AUTHORITY == DEF_NOAUTH){
            $arg["Closing"] = "  closing_window('cm'); " ;
        }

        $arg["jumping"] = " Page_jumper('".REQUESTROOT."/C/KNJC030/knjc030index.php#Jump_here'); ";

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc030Form1.html", $arg);
    }
}
?>
