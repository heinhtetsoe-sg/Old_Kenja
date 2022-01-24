<?php

require_once('for_php7.php');
//タイトル行の行数
# define("DEF_LINE_COUNT",13);
define("DEF_LINE_COUNT",13);
class knjc020Form1
{
    function main(&$model)
    {

        #time start
        $start = $model->getMicrotime();

        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjc020index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度内の処理のみを行う。
        if(!$model->checkCtrlDay($model->cntl_dt_key)){
            $reset_day = knjc020Query::keyMoverQuery($model->cntl_dt_year."-04-01");
            $model->cntl_dt_key = ($reset_day != "")? $reset_day : $model->cntl_dt_year."-04-01" ;
        }
        $thisMonth = explode("-",$model->cntl_dt_key);
        /*** ADD 2005/11/04 by ameku ***/
        $wday = array("(日)","(月)","(火)","(水)","(木)","(金)","(土)");
        $w = date("w",strtotime($model->cntl_dt_key));
        $arg["CNTL_DT_KEY"] = str_replace("-","/",$model->cntl_dt_key).$wday[$w];
        /*** ADD 2005/11/04 by ameku ***/

        //学期取得
        //SQL文発行(学期を取得)
//        $query = knjc020Query::getTerm($model->cntl_dt_year,$thisMonth[1]);
        $query = knjc020Query::getTerm($model->cntl_dt_year,$model->cntl_dt_key);
        $model->termIs = $db->getOne($query);

        //カレンダーコントロール
        $arg["control"]["executedate"] = View::popUpCalendar($objForm, "executedate",
                                                             str_replace("-","/",$model->cntl_dt_key),"reload=true");

        //前日へボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_before",
                            "value"       => "<< 前日",
                            "extrahtml"   => "style=\"width:110px\"onclick=\"return btn_submit('read_before');\"" ) );

        $arg["btn_before"] = $objForm->ge("btn_before");

        //翌日へボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => "翌日 >>",
                            "extrahtml"   => "style=\"width:110px\"onclick=\"return btn_submit('read_next');\"" ) );

        $arg["btn_next"] = $objForm->ge("btn_next");

        //hiddenを作成する
        $objForm->ae( array("type"          => "hidden",
                            "name"          => "cmd" ) );

        $objForm->ae( array("type"          => "hidden",
                            "name"          => "cntl_dt_key",
                            "value"         => $model->cntl_dt_key ) );

        $objForm->ae( array("type"          => "hidden",
                            "name"          => "dbname",
                            "value"         => DB_DATABASE ) );

        $objForm->ae( array("type"          => "hidden",
                            "name"          => "Security",
                            "value"         => AUTHORITY.",".$model->staffcd ) );

        //処理年度を表示
        $ary = array();
        $ary = explode("-",$model->cntl_dt_key);
        $show_year = $ary[0]."年".$ary[1]."月".$ary[2]."日";

        $arg["TOP"] = array("CONFIRMATION"  => "処理日：".$show_year,
                            "GC"            => "年組",
                            "THIS_YEAR"     => "処理年度:".$model->cntl_dt_year ."年");

        //校時名称の取得
        $query = knjc020Query::getNamecd($model->cntl_dt_year, "B001");
        $result = $db->query($query);
        $i = 0;
        $title_count = $result->numRows();
        $title   = array();
        $title["SUBJECT"]   = "<th width=\"85\" nowrap class=\"no_search\">所属</th>\n";
        $title["NAME_SHOW"] = "<th width=\"110\" nowrap class=\"no_search\">職員氏名</th>\n";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($i == 11) {
                //所属・職員氏名を挿入
                $title["SUBJECT2"]   = $title["SUBJECT"];
                $title["NAME_SHOW2"] = $title["NAME_SHOW"];
            }
            if ($i == $title_count-1){
                $width = 85;
            }else{
                $width = 70;
            }
            $title["PERIODCD".$i] = "<th width=\"".$width."\" nowrap class=\"no_search\">".$row["NAME1"]."</th>\n";
            $period_cd[]    = $row["NAMECD2"];
            $lastid         = $row["NAMECD2"];
            $i++;
        }
        if ($title_count <= 10){
            $arg["DHEADER"] = true;
        }
        //----------------------以下、擬似フレーム内リスト表示----------------------
        //初期化
        //$first_time_flg = "off";
        $caption = "全員出席";

        //表示項目取得
        $query  = knjc020Query::getDispCol();
        $result = $db->query($query);
        $model->DispCol = $db->getOne($query);

        //SQL文発行(科目名を保持)
        $query = knjc020Query::find_class();
        $result = $db->query($query);

        //コンボボックス用データに"すべて"を追加
        $opt[] = array("label" => "--- すべて ---",
                       "value" => -1);

        while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $class_box[$Row["SECTIONCD"]] = $Row["SECTIONABBV"];

            //コンボボックス用データ
            $opt[] = array("label" => htmlspecialchars($Row["SECTIONCD"]."：".$Row["SECTIONABBV"]),
                           "value" => $Row["SECTIONCD"]);
        }

        //コンボボックスの初期値を作成
        if($model->SUBJECT == ""){
            $query = knjc020Query::getLoginStaffInfo();
            $model->SUBJECT = $db->getOne($query);
        }

        //科目コンボボックスを作成する
        $objForm->ae( array("type"        => "select",
                            "name"        => "sub_combo",
                            "size"        => "1",
                            "value"       => $model->SUBJECT,
                            "options"     => $opt,
                            "extrahtml"   => "onChange=\"btn_submit('')\";" ) );

        $arg["sub_combo"] = " 所属選択:".$objForm->ge("sub_combo");

        //SQL文発行(STAFFCDと名前、SECTIONNAME を保持)
        $query  = knjc020Query::get_staff_data($model);
        $result = $db->query($query);
        while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $staffcd[]    = $Row["STAFFCD"];
            if ('1' == $model->Properties["notShowStaffcd"]) {
                $staff_name[$Row["STAFFCD"]] = $Row["SHOW_NAME"];
            } else {
                $staff_name[$Row["STAFFCD"]] = $Row["STAFFCD"]."<br>".$Row["SHOW_NAME"];
            }
            $staff_job[$Row["STAFFCD"]]  = $Row["SECTIONNAME"];
        }

        //SQL文発行(全体の情報を保持)
        $query  = knjc020Query::readQuery($model,$model->termIs,$model->Properties["useTestCountflg"]);
        $result = $db->query($query);
        //$data_allを初期化
        $data_all = array();
        $class_box = array();
        $mul_chk = $mul_flg = array();

        //データを取得
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //無ければ初期化
            if(!isset($class_box[$row["STAFFCD"]])) $class_box[$row["STAFFCD"]] = array();
            if(!isset($class_box[$row["STAFFCD"]][$row["PERIODCD"]])){
                $class_box[$row["STAFFCD"]][$row["PERIODCD"]]  = $row["TARGETCLASS"];
            }else{
                $class_box[$row["STAFFCD"]][$row["PERIODCD"]] .= $row["TARGETCLASS"];
            }

            //データを配列に保持
            $data_all[$row["STAFFCD"]][] = array("STAFFCD"        => $row["STAFFCD"],
                                                 "CHAIRCD"        => $row["CHAIRCD"],
                                                 "PERIODCD"       => $row["PERIODCD"],
                                                 "GROUPCD"        => $row["GROUPCD"],
                                                 "TARGETCLASS"    => $row["TARGETCLASS"],
                                                 "SUBCLASSABBV"   => ($model->DispCol == 1) ? $row["SUBCLASSABBV"] : $row["CHAIRNAME"],
                                                 "ATTENDCD"       => $row["ATTENDCD"],
                                                 "CHARGEDIV"      => $row["CHARGEDIV"],
                                                 "COUNTFLG"       => $row["COUNTFLG"],
                                                 "LESSON_MODE"    => $row["LESSON_MODE"],
                                                 );

            $mul_chk[$row["STAFFCD"]][$row["PERIODCD"]][$row["CHAIRCD"]] = $row["ATTENDCD"];

            if(get_count($mul_chk[$row["STAFFCD"]][$row["PERIODCD"]] ) >= 2 ){
                $colorcheck = array_values($mul_chk[$row["STAFFCD"]][$row["PERIODCD"]]);
                $colorcheck = array_unique($colorcheck);
 
                if(in_array("0",$colorcheck) || in_array("",$colorcheck)){
                    $colorcheck = " bgcolor=\"#ff0099\"";
                }else{
                    $colorcheck = " bgcolor=\"#3399ff\"";
                }
                $mul_flg[$row["STAFFCD"]][$row["PERIODCD"]] = $colorcheck;
                unset($colorcheck);
            }
        }

        $attendDataCntAll = array();
        if ($model->Properties["unUseAttendDialog"] == "1") {
        } else {
            $query = knjc020Query::getAttendCntAll($model); 
            $result = $db->query($query);
            while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $attendDataCntAll[$Row["PERIODCD"]."|".$Row["CHAIRCD"]] = $Row["CNT"];
            }
        }

        //初期化
        unset($mul_chk);
        $ay = array();
        $first = "true";
        $add_flg = 0;
        $a = get_count($staffcd);

        //リンク先作成
#        $query = knjc020Query::getPath();
#        $jumping = $db->getOne($query);
        $jumping = "/C/KNJC010";
        $jumping = REQUESTROOT.$jumping."/knjc010index.php";
        /*** ADD 2005/11/04 by ameku ***/
        $arg["TITLE"] = $title;
        /*** ADD 2005/11/04 by ameku ***/
        //表示用データを作成する
        $periodcd_change = array();
        for ($i = 0; $i < $a; $i++){
            $list_tag = array();

            if ($title_count > 10 && ($add_flg == 0 || $add_flg == DEF_LINE_COUNT)) {
                //タイトルを挿入
                $arg["data"][] = $title;
                $add_flg = 1;
            }

            //login者を強調
            if($model->staffcd == $staffcd[$i]) {
                $list_tag["stfn"] = "rstfn";
                //初期位置
                if($model->set_target == "off") $list_tag["Jump_target"] = "<a name=\"Target\"></a>";
            }else{
                $list_tag["stfn"] = "stfn";
            }

            $list_tag["SUBJECT"]   = "<td width=\"85\" nowrap class=\"subtitle\">".$staff_job[$staffcd[$i]]."</td>\n"; 
            $list_tag["NAME_SHOW"] = "<td width=\"110\" nowrap class=\"".$list_tag["stfn"]."\">".$staff_name[$staffcd[$i]]."</td>\n";

            //配列を初期化
            for ($ii = 0; $ii < get_count($period_cd); $ii++ ) {

                if($ii == 11) {
                    //所属・職員氏名を挿入
                    $list_tag["SUBJECT2"]   = $list_tag["SUBJECT"];
                    $list_tag["NAME_SHOW2"] = $list_tag["NAME_SHOW"];
                }

                if ($period_cd[$ii] != $lastid) {
                    $list_tag["PERIODCD".$ii] = "<td width=\"70\" height=\"40\" nowrap></td>\n";
                } else {
                    $list_tag["PERIODCD".$ii] = "<td width=\"85\" height=\"40\" nowrap></td>\n";
                }
                $periodcd_change[$period_cd[$ii]] = $ii;
            }

            if($model->set_target == $staffcd[$i]){
                $list_tag["Jump_target"] = "<a name=\"Target\"></a>";
            }

            if( !isset($data_all[$staffcd[$i]]) ){
                $data_all[$staffcd[$i]] = array();
            }

            $ay = $data_all[$staffcd[$i]];
            $b = get_count($ay);
            $id_val2 = "*";

           //同一の担当職員の回数分
            for ($ii = 0; $ii < $b; $ii++ )
            {
                $fontHead = "";
                $fontFoot = "";
                //職員に設定されている講座の回数分
                foreach( $data_all[$staffcd[$i]][$ii] as $key => $val)
                {
                    $list_tag["STAFFCD"] = $staffcd[$i];
                    switch ($key) {
                        case "PERIODCD":
                            if( isset($mul_flg[$staffcd[$i]][$val]) ){
                                $multimode["mode"] = "on";
                                $multimode["color"] = $mul_flg[$staffcd[$i]][$val];
                            }
                            //表示用年組を作成
                            $set_target = $key.$periodcd_change[$val];

                            if ($id_val2 != $val) {
                                $setColor = array("red" => "0", "blue" => "0", "yellow" => "0");
                                $setCountFlg = array("ON" => "0", "OFF" => "0");
                                //Key:LESSON_MODE Val:1 (1コマのLESSON_MODE分配列が出来る)
                                $setLessonModeData = array();
                                //名称マスタにLESSON_MODEがない場合に ON
                                $setLessonModeFlg = "";
                            }
                            $id_val = $val;
                            $id_val2 = $val;
                            break;
                        case "SUBCLASSABBV":
                            $subclassabbv = $val;
                            break;
                        case "TARGETCLASS":
                           #2005/09/16 arakaki $targetclassのクリア処理追加
                           if( $data_all[$staffcd[$i]][$ii]["GROUPCD"] == "0000"){  #HR単位の講座
//                               $targetclass = $val; NO001
                               if( $wk_id_val != $list_tag["STAFFCD"].$id_val){     #先生,校時が変わった
                                   $wk_id_val  = $list_tag["STAFFCD"].$id_val;
	                               $targetclassval = $val;	//NO001
	                               $targetclass    = $val;	//NO002
							   }
                            }else{                                                  #選択群講座
                               if( $wk_id_val != $list_tag["STAFFCD"].$id_val){     #先生,校時が変わった
                                   $wk_id_val  = $list_tag["STAFFCD"].$id_val;
                                   $targetclassval = $val."*";
	                               $targetclass    = $val;	//NO002
                               }else{
                                   $targetclass = ($targetclassval < $val."*")? $targetclassval : $val."*" ;     #ソートして小さい名称を表示
                               }
                            }
                            break;
                        case "CHAIRCD":
                            $chaircd = $val;
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
                                $id_reserver = $i.",".($id_val + 2);
                                $first_id    = $id_val.",".$staffcd[$i].",".$model->staffcd ;    //2005/05/06 賢者-作業依頼書20050506_01
                                if($multimode["mode"] == "on"){
                                    $first_id .= ",m";
                                }
                                $first_id_val = $first_id.",".$src_color ;
                                $first_chaircd = $chaircd;
                            }

                            if ($val == "1") {
                                $setColor["blue"] = "1";
                            } else {
                                $colorRow = $db->getRow(knjc020Query::getGroupCd($model->cntl_dt_key, $id_val, $chaircd, $model->cntl_dt_year), DB_FETCHMODE_ASSOC);
                                $classCnt = $db->getOne(knjc020Query::getChrClass($colorRow, "COLOR"));
                                $hrateAry = $db->getRow(knjc020Query::getHrateClass($row, $model->cntl_dt_key, $id_val, $chaircd), DB_FETCHMODE_ASSOC);
                                if ($classCnt == 0 || $classCnt != $hrateAry["CNT"] ||
                                    $hrateAry["SUMEXECUTED"] == 0) {
                                    //講座クラスなし
                                    //講座クラスとHR出席クラス不一致
                                    //HR出席が全て未
                                    $setColor["red"] = "1";
                                } else if ($hrateAry["SUMEXECUTED"] == $hrateAry["CNT"]) {
                                    //HR出席が全て済み
                                    $setColor["blue"] = "1";
                                } else {
                                    //HR出席が未と済みの混合
                                    $setColor["yellow"] = "1";
                                }
                            }

                        default:
                            $list_tag[$key] = $val;
                    }
                }

                $width_num = ($id_val == $lastid) ? "85" : "70";

                //ATTEND_DATのデータ数(ポップアップ判定用)
                if ($model->Properties["unUseAttendDialog"] == "1") {
                    $attendDataCnt = 0;
                } else {
                    $attendDataCnt = $attendDataCntAll[($id_val."|".$chaircd)];
                }

                $set_tag  = "<td nowrap width=\"".$width_num."\" ";
                $set_tag .= " id=\"".$id_val.",".$staffcd[$i].",".$model->staffcd ;    //2005/05/06 賢者-作業依頼書20050506_01

                if ($setColor["red"] == "1" && $setColor["blue"] == "0" && $setColor["yellow"] == "0") {
                    $bgcolor = " bgcolor=\"#ff0099\"";
                } else if ($setColor["blue"] == "1" && $setColor["red"] == "0" && $setColor["yellow"] == "0") {
                    $bgcolor = " bgcolor=\"#3399ff\"";
                } else {
                    $bgcolor = " bgcolor=\"#ffff00\"";
                    $fontHead = "<font color=\"black\">";
                    $fontFoot = "</font>";
                }

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
                    $query = knjc020Query::getLessonName("");
                    foreach ($setLessonModeData as $lessonMode => $lessonVal) {
                        $query = knjc020Query::getLessonName($lessonMode);
                        if ($lessonMode === "00") {
                            $setMaruBatu = "";
                        }
                    }
                    $setLessonMode = $db->getOne($query);
                }
                $setLessonMode = $setLessonMode ? $setLessonMode.":" : $setLessonMode;
                if($multimode["mode"] == "on"){
                    $set_tag .= ",m\" id2=\"{$attendDataCnt}\"".$bgcolor." value=\"".$chaircd."\" onClick=\"celcolchan(this,'');\" ondblclick=\"IsUserOK_ToJump('$jumping','$model->cntl_dt_key','$id_val','$staffcd[$i]','$model->staffcd');\">".$fontHead."<b>";  //2005/05/06 賢者-作業依頼書20050506_01
                    $set_tag .= $setMaruBatu.$setLessonMode."(複数)"."</b>".$fontFoot."</td>\n";
                }else{
                    $set_tag .= "\" id2=\"{$attendDataCnt}\"".$bgcolor." value=\"".$chaircd."\" onClick=\"celcolchan(this,'');\" ondblclick=\"IsUserOK_ToJump('$jumping','$model->cntl_dt_key','$id_val','$staffcd[$i]','$model->staffcd');\">".$fontHead."<b>";  //2005/05/06 賢者-作業依頼書20050506_01
                    $set_tag .= $setMaruBatu.$setLessonMode.$subclassabbv."<BR>".$targetclass."</b>".$fontFoot."</td>\n";
                }

                $multimode = array();
                $list_tag["$set_target"] = $set_tag ;

            }

            $arg["data"][] = $list_tag;
            $add_flg++;
        }

        //ジャンプするターゲットの初期化
        $model->set_target = "off";

        //フォーカス対象無し
        if($first == "true"){
            $first = "off";
        }else{
            $first = "first";
        }

        //詳 細ボタンを作成する
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_detail",
                            "value"         => " 詳 細 ",
                            "extrahtml"     => " onclick=\"return detailopen('');\"" ) );

        $arg["btn_detail"] = $objForm->ge("btn_detail");

        //終 了ボタンを作成する
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_end",
                            "value"         => " 終 了 ",
                            "extrahtml"     => " onclick=\"return closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //確認済ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_confirm",
                            "value"       => "確認済",
                            "extrahtml"   => " onclick=\"return btn_submit('confirm');\"" ) );

        $arg["btn_confirm"] = $objForm->ge("btn_confirm");


        //更 新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => $caption,
                            "extrahtml"   => " onclick=\"return btn_submit('update');\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_update");

        //リンクボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_jump",
                            "value"       => "出欠入力画面へ",
                            "extrahtml"   => " onClick=\" Page_jumper('".$jumping."','1');\"" ) );

        $arg["btn_jump"] = $objForm->ge("btn_jump");

        if(!isset($model->first_time_flg)) $model->first_time_flg = "off";

        //hiddenを作成する
        $objForm->ae( array("type"       => "hidden",
                            "name"       => "locker",
                            "value"      =>  $first  ) );

        $objForm->ae( array("type"       => "hidden",
                            "name"       => "id_value",
                            "value"      => (isset($id_reserver))? $id_reserver : "" ) );

        //hiddenの初期化
        if($model->chosen_id == "") $model->chosen_id = (isset($first_id_val))? $first_id_val : "" ;
        if($model->first_id == "")  $model->first_id  = (isset($first_id))? $first_id : "" ;

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "chosen_id",
                            "value"     => $model->chosen_id ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ID_NO",
                            "value"     => $model->first_id  ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "stock_chaircd",
                            "value"     => (isset($first_chaircd))? $first_chaircd : "" ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "update_flg",
                            "value"     => $model->update_flg ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "backupper",
                            "value"     => $model->color_bk ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "first_idno",
                            "value"     => (isset($first_id))? $first_id : ""  ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "output_CtrlDate",
                            "value"     => str_replace("-","/",$model->attnd_cntl_dt)));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "multi",
                            "value"     => "off"));

        //特殊ダイアログ出力用
        knjCreateHidden($objForm, "showDialog", "0");
        knjCreateHidden($objForm, "btnShukketsu", "0");

        $result->free();

        //処理が完了、又は権限が無ければ閉じる。
        if($model->cntl_dt_year == ""){
            $arg["Closing"] = "  closing_window('year'); " ;
        }else if(AUTHORITY == DEF_NOAUTH){
            $arg["Closing"] = "  closing_window('cm'); " ;
        }else if($model->check_staff_dat != "ok" ){
            $arg["Closing"] = "  closing_window('sf'); " ;
        }

        //リンク先
        $arg["jumping"]    = " Page_jumper('".REQUESTROOT. "/C/KNJC020/knjc020index.php#Target','0'); ";
        $arg["celcolchan"] = " celcolchan('','". $model->first_id ."'); ";
        $arg["update_chk"] = " Update_check('".$model->update_flg."'); ";
        $arg["URLS"] = REQUESTROOT."/C/KNJC020_1/knjc020_1index.php";

        //DB切断
        Query::dbCheckIn($db);

        //デバッグ------------------------
        //var_dump($model->reservation);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML2($model, "knjc020Form1.html", $arg);

        #time end
        $end = $model->getMicrotime();
        $time = $end - $start;
        //echo "<BR> This Program took LoadingTime ".$time." sec(s) <BR>";

    }
}
?>
