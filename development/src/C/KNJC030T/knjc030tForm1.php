<?php

require_once('for_php7.php');

class knjc030tForm1
{
    function main(&$model)
    {
        # time start
        # $start = $model->getMicrotime();

        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjc030tindex.php", "", "edit");

        //年度内の処理のみを行う。
        if(!$model->checkCtrlDay($model->cntl_dt_key)){
            $model->cntl_dt_key=$model->cntl_dt_year."-04-01";
            $reset_day = knjc030tQuery::keyMoverQuery($model);
            $model->cntl_dt_key = ($reset_day != "")? $reset_day : $model->cntl_dt_year."-04-01" ;
        }

		/*** ADD 2006/01/30 by m-yama ***/
		$wday = array("(日)","(月)","(火)","(水)","(木)","(金)","(土)");
		$w = date("w",strtotime($model->cntl_dt_key));
		$arg["CNTL_DT_KEY"] = str_replace("-","/",$model->cntl_dt_key).$wday[$w];
		/*** ADD 2006/01/30 by m-yama ***/

        //選択日付を分解
        $thisMonth = explode("-",$model->cntl_dt_key);
        $this_month=$thisMonth[1];

        $db = Query::dbCheckOut();

        //SQL文発行(選択日付の学期を取得)
#        $query = knjc030tQuery::getTerm($model->cntl_dt_year,$this_month);             //2005/04/20 TERM_GET_OLD
        $query = knjc030tQuery::getTerm($model->cntl_dt_year,$model->cntl_dt_key);     //2005/04/20 TERM_GET_NEW
        $model->termIs = $db->getOne($query);

        //見出し行作成
        $query = knjc030tQuery::getIndexName();
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row["width_num"] = ($row["NAMECD2"] == "D") ? "*" : "110";
            $arg["title_index"][] = $row;
        }

        //表示項目取得
        $query = knjc030tQuery::getDispCol();
        $result = $db->query($query);
        $model->DispCol = $db->getOne($query);

        //カレンダーコントロール(カレンダーを作成)
        $arg["control"]["executedate"] = View::popUpCalendar($objForm, "executedate",str_replace("-","/",$model->cntl_dt_key),"reload=true");

        //前のデータへボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_before",
                            "value"       => "<< 前のデータ",
                            "extrahtml"   => "style=\"width:110px\"onclick=\"return btn_submit('read_before');\""
                         ));

        $arg["btn_before"] = $objForm->ge("btn_before");

        //次のデータへボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => "次のデータ >>",
                            "extrahtml"   => "style=\"width:110px\"onclick=\"return btn_submit('read_next');\""
                         ));

        $arg["btn_next"] = $objForm->ge("btn_next");

        //hiddenを作成する
        $objForm->ae( array("type"          => "hidden",
                          "name"          => "cmd" ) );

        $objForm->ae( array("type"          => "hidden",
                          "name"          => "cntl_dt_key",
                          "value"         => $model->cntl_dt_key ) );

        //処理年度を表示
        $arg["this_year"] = "処理年度:".CTRL_YEAR ."年";

        //初期化
        $first_time_flg = "off";

        //学年選択コンボ
        $query = knjc030tQuery::gradeCombo();
        $result = $db->query($query);

        $opt = array();
        //複数リストがある場合は先頭に空リストをセット
        if ($result->numRows() > 1) {
            $opt[] = array("label" => "","value" => "");
        } elseif($model->GRADE ==''){
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            $model->GRADE=$row["GRADE"];
        }      

        while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
          $grade=sprintf("%1d",$Row["GRADE"]);
          //コンボボックス用データ
          $opt[] = array("label" => "第".$grade."学年",
                         "value" => $Row["GRADE"]);
        }

        //2004/09/30 arakaki 表示するデータが無いとエラーになるので追加 近大-作業依頼書20040930-01.doc
        if(isset($opt)||$opt !=""){

            //学年コンボボックスを作成する
            $objForm->ae( array("type"        => "select",
                              "name"        => "GRADE",
                              "size"        => "1",
                              "value"       => $model->GRADE,
                              "options"     => $opt,
                              "extrahtml"   => "onChange=\"btn_submit('')\";"
                             ));
        }

        $arg["GRADE"] = "学年:".$objForm->ge("GRADE");

        //----------------------以下、擬似フレーム内リスト表示----------------------

        //クラス情報を保持
        $query = knjc030tQuery::get_class_data($model);
        $results = $db->query($query);
        //SQL文発行(クラス情報を保持)
        $class_cnt = 0;

        while( $Row = $results->fetchRow(DB_FETCHMODE_ASSOC)){
            $class[] =$Row["GR_CL"];
            $class_name_show[$Row["GR_CL"]] = $Row["HR_NAME"];
            $class_cnt++ ;
        }

        //SQL文発行(全体の出欠情報を保持)
        $query  = knjc030tQuery::readQuery($model);
        $result = $db->query($query);

        //変数初期化
        $data_all = array();
        $chairname_box = array();
        $model->data=array();
        $attendCnt   = array();
        $chairCnt    = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
          //データを配列に保持
#          $chairname_box[$row["TARGETCLASS"]][$row["PERIODCD"]] .= $row["SUBCLASSABBV"]."<BR>".$row["STAFFNAME"]."<BR>";
            if($model->DispCol == 1){
                $chairname_box[$row["TARGETCLASS"]][$row["PERIODCD"]] .= $row["SUBCLASSABBV"]."<BR>".$row["STAFFNAME"]."<BR>";
            }else{
                $chairname_box[$row["TARGETCLASS"]][$row["PERIODCD"]] .= $row["CHAIRNAME"]."<BR>".$row["STAFFNAME"]."<BR>";
            }
            //出欠と講座数
            $attendCnt[$row["TARGETCLASS"]][$row["PERIODCD"]] += $row["EXECUTED_CHK"];
            $chairCnt[$row["TARGETCLASS"]][$row["PERIODCD"]]++;

            //出欠済・未の判定
            /* 2004/11/18 arakaki
            if($row["SCD_EXECUTED"]==1){
                $select_executed=1;
            }else if ($row["SCD_EXECUTED"]!=1){
              if($row["EXECUTED"]==1){
                $select_executed=1;
              }else if($row["EXECUTED"]!=1 || !isset($row["EXECUTED"])){
                $select_executed=0;
              }
            }
            */


            /* 2005/01/10 minei
            //ＨＲ別出欠済・未の判定
            if($row["EXECUTED"]==0){            //出欠未
                $select_executed=0;
            }else if($row["EXECUTED"]==1){      //出欠済み
                $select_executed=1;
            //判定不能の場合は講座別を参照
            }else{
                if($row["SCD_EXECUTED"]==0){        //出欠未
                    $select_executed=0;
                }else if($row["SCD_EXECUTED"]==1){  //出欠済み
                    $select_executed=1;
                //判定不能の場合はデフォルト出欠未
                }else{
                    $select_executed=0;
                }
            }
            */

            //2005.01.10 minei [近大-作業依頼書20050110-01.doc]による修正
            //講座別、ＨＲ別の何れかにフラグが立っていれば出欠済みとする
            if($row["EXECUTED_CHK"]==0){  //出欠未
             $select_executed=0;
            }else{                        //出欠済み
             $select_executed=1;
            }

            $model->data["CHAIRCD"][]=$row["CHAIRCD"];
            $model->data["PERIODCD"][]=$row["PERIODCD"];
            $model->data["GRADE"][]=$row["GRADE"];
            $model->data["HR_CLASS"][]=$row["HR_CLASS"];


            $data_all[$row["TARGETCLASS"]][] = array( "TR_CD1"          => $row["TR_CD1"],
                                                      "PERIODCD"        => $row["PERIODCD"],
                                                      "CHAIRCD"         => $row["CHAIRCD"],
                                                      "GRADE"           => $row["GRADE"],
                                                      "HR_CLASS"        => $row["HR_CLASS"],
                                                      "TARGETCLASS"     => $row["TARGETCLASS"],
                                                      "STAFFNAME"       => $row["STAFFNAME"],
                                                      "SELECT_EXECUTED" => $select_executed
                                                    );
        }

        //初期化
        $ay     = array();
        $first  = "true"; //フォーカス対象
        $a      = $class_cnt; //表示学年数

        //リンク先作成
        #     2004/09/02 arakaki 近大-作業依頼書20040901-02.doc
        /*
        $query = knjc030tQuery::getPath();
        $jumping = $db->getOne($query);
        $jumping = REQUESTROOT.$jumping."/knjc010index.php";
        */
        $jumping = REQUESTROOT."/C/KNJC010/knjc010index.php";


        //表示用データを作成する
        for ($i = 0; $i < $a; $i++){

            //配列を初期化
            $list_tag["PERIODCD1"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD2"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD3"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD4"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD5"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD6"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD7"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD8"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD9"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCDA"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCDB"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCDC"] = "<td width=\"110\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCDD"] = "<td width=\"*\" height=\"40\">&nbsp;</td>";//SHR

            $list_tag["TARGETCLASS"] = $class_name_show[$class[$i]];
            $ay = $data_all[$class[$i]];
            $b = get_count($ay);

            for ($ii = 0; $ii < $b; $ii++ )
            {
                $attendSum = "";
                $chairSum  = "";
                $fontHead = "";
                $fontFoot = "";
                foreach( ($data_all[$class[$i]][$ii]) as $key => $val)
                {
                    switch ($key) {
                        case "PERIODCD":            //表示用配列作成 //校時コードを作成
                            $noFound = $chairname_box[$class[$i]][$val];
                            $attendSum = $attendCnt[$class[$i]][$val];
                            $chairSum  = $chairCnt[$class[$i]][$val];

                            if ($attendSum == $chairSum) {
                                $bgcolor = "bgcolor=\"#3399ff\"";
                            } else if ($attendSum == "0") {
                                $bgcolor = "bgcolor=\"#ff0099\"";
                            } else {
                                $bgcolor = "bgcolor=\"#ffff00\"";
                                $fontHead = "<font color=\"black\">";
                                $fontFoot = "</font>";
                            }

                            $set_target = $key.$val;
                            $id_val = $val;
                            break;
                        case "TARGETCLASS":         //学級名を作成
                            $targetclass = $val;
                            break;
                        case "CHAIRCD":             //講座名を作成
                            $chaircd = $val;
                            break;
                        case "GRADE":               //講座名を作成
                            $grade = $val;
                            break;
                        case "HR_CLASS":            //講座名を作成
                            $hr_class = $val;
                            break;
                        case "TR_CD1":              //職員コードを作成
                            $tr_cd1 = $val;
                            break;
                        case "SELECT_EXECUTED":
                            //出欠の色を作成
                            /* 2004/11/18 arakaki 未が1つでもあれば、未で表示。
                            if ($val == "1"){
                            $bgcolor = " bgcolor=\"#3399ff\"";
                            }else{
                            $bgcolor = " bgcolor=\"#ff0099\"";
                            }
                            */

                        default:
                            $list_tag[$key] = $val;
                    }   //end of switch
                }   //end of foreach

                //幅の設定(PERIODCDが9の時だけ*)
                $width_num = ($id_val == "D") ? "*" : "110";

                //タグの作成
                $set_tag  = "<td nowrap width=\"".$width_num."\" ";
                $set_tag .= " id=\"".$id_val.",".$class[$i] ;
                $set_tag .= "\"".$bgcolor." value=\"".$chaircd."\" onClick=\"celcolchan(this,'');\" ondblclick=\"IsUserOK_ToJump('$jumping','$model->cntl_dt_key','$id_val','$grade','$hr_class','$tr_cd1','".STAFFCD."');\">".$fontHead."<b>";
                $set_tag .= "<BR>".$noFound."</b>".$fontFoot."</td>";
                $list_tag["$set_target"] = $set_tag ;
            }
            $arg["data"][] = $list_tag;

        }   //end of for


        //フォーカス対象無し
        $first = ($first == "true")?"off":"first";

        //終 了ボタンを作成する
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_end",
                            "value"         => " 終 了 ",
                            "extrahtml"     => " onclick=\"return closeWin();\""
                           ));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //更新可のユーザは全員出席ボタン不可
        //      $ch=(AUTHORITY == DEF_UPDATABLE)?"disabled":"";     //2004/09/30 arakaki 近大-作業依頼書20040930-01.doc
        $ch=(AUTHORITY == DEF_UPDATABLE||$class_cnt == 0)?"disabled":"";


        //更 新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "全員出席",
                            "extrahtml"   => $ch." onclick=\"return btn_submit('update');\""
                           ));

        $arg["btn_update"] = $objForm->ge("btn_update");

        if(!isset($model->first_time_flg)) $model->first_time_flg = "off";

        //hiddenを作成する
        $objForm->ae( array("type"       => "hidden",
                            "name"       => "locker",
                            "value"      =>  $first
                           ));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ID_NO",
                            "value"     => $model->first_id
                           ));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "backupper",
                            "value"     => $model->color_bk
                           ));

        //ATTEND_DATに出欠データがある場合更新処理を続けるかを表示
        $attendexsits = $db->getOne(knjc030tQuery::getUpdateSelectData($model));
        $ex = (get_count($attendexsits)>0)?1:0;

        //制限処理月のチェック
        $monthch = $db->getOne(knjc030tQuery::ch_Control_Month($model));
        $mch = (get_count($monthch)>0)?1:0;

        //出席制御日付チェック 2005/06/01 arakaki
        $acd = IS_KANRISYA || $model->cntl_dt_key > $model->attnd_cntl_dt ? 1 : 0;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "attendexsits",
                            "value"     => $ex
                           ));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "monthch",
                            "value"     => $mch
                           ));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "attendctrldate",
                            "value"     => $acd
                           ));


        $result->free();
        Query::dbCheckIn($db);

        //権限が無ければ閉じる
        if(AUTHORITY <= 2){
        $arg["Closing"] = "  closing_window('cm'); " ;
        }

        //デバッグ------------------------
        //var_dump($model->reservation);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc030tForm1.html", $arg);

        # time end
        # $end = $model->getMicrotime();
        # echo $time = $end - $start;
        //echo "<BR> This Program took LoadingTime ".$time." sec(s) <BR>";

    }
}
?>
