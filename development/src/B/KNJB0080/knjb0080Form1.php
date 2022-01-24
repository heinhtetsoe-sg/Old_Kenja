<?php

require_once('for_php7.php');

require_once("Date/Calc.php");
class knjb0080Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjb0080index.php", "", "edit");

        //時間割種別ラジオボタン（基本:1・通常:2）
        $opt_jtype[0]=1;
        $opt_jtype[1]=2;

        for ($i = 1; $i <= 2; $i++) {
            $name = "btn_jtype".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "jtype",
                                "extrahtml"  => "onclick=\"btn_submit('main')\" id=\"$name\"",
                                "value"      => isset($model->jtype) ? $model->jtype : 1,
                                "options"    => $opt_jtype));

            $arg[$name] = $objForm->ge("jtype",$i);
        }

        if (!isset($model->jtype)) $model->jtype = 1;

        //---------------------------------共通---------------------------------
        //ヘッダ
        $arg["hdr_year"] = CTRL_YEAR."年度";

        $opt_fac = array();
        //SQL文発行(施設を取得)
        $db     = Query::dbCheckOut();
        $result = $db->query(knjb0080Query::getFacility());
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");
            $opt_fac[]  = array("label" => $row["FACCD"]."：".$row["FACILITYNAME"],
                                "value" => $row["FACCD"]);
            if (!isset($model->faccd)) $model->faccd = $row["FACCD"];
        }
        $result->free();
        Query::dbCheckIn($db);

        //施設コンボボックス
        $objForm->ae( array("type"      => "select",
                            "name"      => "faccd",
                            "size"      => "1",
                            "extrahtml" => "onChange=\"btn_submit('main')\"",
                            "value"     => $model->faccd,
                            "options"   => $opt_fac));
        $arg["faccd"] = $objForm->ge("faccd");

        //---------------------------------基本---------------------------------
        if ($model->jtype==1){
            //ヘッダ
            $arg["title"] = "ＳＥＱ：タイトル";

            $opt_seme = array();
            $opt_seq  = array();
            //SQL文発行(学期を取得)
            $db     = Query::dbCheckOut();
            $result = $db->query(knjb0080Query::getSemester());
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");
                $opt_seme[] = array("label" => $row["SEMESTERNAME"],
                                    "value" => $row["SEMESTER"]);
                if (!isset($model->semester)) $model->semester = CTRL_SEMESTER;
            }

            //SQL文発行(ＳＥＱ：タイトルを取得)
            $result = $db->query(knjb0080Query::getSeqTitle($model->semester));
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");
                $opt_seq[]  = array("label" => $row["BSCSEQ"]."：".$row["TITLE"],
                                    "value" => $row["BSCSEQ"]);
                if (!isset($model->seq)) $model->seq = $row["BSCSEQ"];
            }
            $result->free();
            Query::dbCheckIn($db);

            //学期コンボボックス
            $objForm->ae( array("type"      => "select",
                                "name"      => "semester",
                                "size"      => "1",
                                "extrahtml" => "onChange=\"btn_submit('main')\"",
                                "value"     => $model->semester,
                                "options"   => $opt_seme));
            $arg["semester"] = $objForm->ge("semester");

            //タイトルコンボボックス
            $objForm->ae( array("type"      => "select",
                                "name"      => "seq",
                                "size"      => "1",
                                "extrahtml" => "onChange=\"btn_submit('main')\"",
                                "value"     => $model->seq,
                                "options"   => $opt_seq));
            $arg["seq"] = $objForm->ge("seq");
        //---------------------------------通常---------------------------------
        } else {
            //ヘッダ
            $arg["title"] = "実施期間";

            //指定日（初期値・ブランク・不正な日付の場合、学籍処理日をセット）
            if (!common::DateConv1($model->executedate,4)){
                $model->executedate = str_replace("-","/",CTRL_DATE);
            } else {
                $model->executedate = common::DateConv1($model->executedate,4);
                //年度内の処理のみを行う。（学籍処理日をセット）
                if (!$model->checkCtrlDay($model->executedate)){
                    $model->executedate = str_replace("-","/",CTRL_DATE);
                }
            }
            $arg["executedate"] = View::popUpCalendar($objForm, "executedate",$model->executedate,"reload=true");

            //指定日を含む指定週の開始日(月曜日)と終了日(日曜日)を取得
            common::DateConv2($model->executedate,$OutDate1,$OutDate2,0);
            $arg["date_end"] = "　～　".$OutDate2."（日）";

            //表示用（指定週の日付）
            $tmp = explode("/",$OutDate1);
            for($i=0;$i<7;$i++) 
                $arg["date_youbi".$i] = Date_Calc::daysToDate(Date_Calc::dateToDays((int)$tmp[2]+$i,$tmp[1],$tmp[0]), "%m/%d") ."<BR>";

            //指定日より学期を取得
            $db = Query::dbCheckOut();

            $query = knjb0080Query::getY2t(str_replace("/","-",$model->executedate));
            $y2t_seme = $db->getOne($query);

            $query = knjb0080Query::getY2t_name(str_replace("/","-",$model->executedate));
            $y2t_seme_name = $db->getOne($query);

            Query::dbCheckIn($db);

            $arg["hdr_seme"] = $y2t_seme_name;

            //確 定ボタン
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_read",
                                "value"     => " 確 定 ",
                                "extrahtml" => " onclick=\"btn_submit('main');\"" ) );
            $arg["btn_read"] = $objForm->ge("btn_read");
        }

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        //----------------------以下、擬似フレーム内リスト表示----------------------

        //---------------------------------共通---------------------------------
        $db     = Query::dbCheckOut();

        //SQL文発行(校時情報を保持)
        $query = knjb0080Query::getVnamemst();
        $result = $db->query($query);
        $period_cnt = 0;
        while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $period_data[$period_cnt] = $Row["NAMECD2"];
            $period_name[$Row["NAMECD2"]] = $Row["NAME1"];
            $period_cnt++ ;
        }
        //---------------------------------基本---------------------------------
        if ($model->jtype==1){

            //変数初期化
            $data_all = array();
            $hr_nameabbv_box = array();

            if (isset($model->seq)){
                //SQL文発行(全体の情報を保持)
                $result = $db->query(knjb0080Query::readQuery_Kihon($model));
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    if(!isset($hr_nameabbv_box[$row["NAMECD2"]][$row["DAYCD"]])){
                        $hr_nameabbv_box[$row["NAMECD2"]][$row["DAYCD"]] = "";
                    }
                    //データを配列に保持
                    $hr_nameabbv_box[$row["NAMECD2"]][$row["DAYCD"]] .= $row["HR_NAMEABBV"]." ".$row["SUBCLASSNAME"]."<BR>";

                    $data_all[$row["NAMECD2"]][] = array( "NAMECD2"     => $row["NAMECD2"],
                                                          "DAYCD"       => $row["DAYCD"],
                                                          "HR_NAMEABBV" => $row["HR_NAMEABBV"]." ".$row["SUBCLASSNAME"]);
                }
            }
        //---------------------------------通常---------------------------------
        } else {

            //変数初期化
            $data_all = array();
            $hr_nameabbv_box = array();

            //SQL文発行(全体の情報を保持)
            $result = $db->query(knjb0080Query::readQuery_Tujou($model,str_replace("/","-",$OutDate1),str_replace("/","-",$OutDate2),$y2t_seme));
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                //実施日付を曜日コードに変換
                $w_tmp = explode("-",$row["EXECUTEDATE"]);
                $w = Date_Calc::dateFormat($w_tmp[2],$w_tmp[1],$w_tmp[0], "%w");
                $row["DAYCD"] = (int)$w+1;

                if(!isset($hr_nameabbv_box[$row["NAMECD2"]][$row["DAYCD"]])){
                    $hr_nameabbv_box[$row["NAMECD2"]][$row["DAYCD"]] = "";
                }
                //データを配列に保持
                $hr_nameabbv_box[$row["NAMECD2"]][$row["DAYCD"]] .= $row["HR_NAMEABBV"]." ".$row["SUBCLASSNAME"]."<BR>";

                $data_all[$row["NAMECD2"]][] = array( "NAMECD2"     => $row["NAMECD2"],
                                                      "DAYCD"       => $row["DAYCD"],
                                                      "HR_NAMEABBV" => $row["HR_NAMEABBV"]." ".$row["SUBCLASSNAME"]);
            }
        }

        $result->free();
        Query::dbCheckIn($db);
        //---------------------------------共通---------------------------------
        //初期化
        $ay = array();
        $a = get_count($period_data);

        //表示用データ
        for ($i = 0; $i < $a; $i++){

            $list_tag["backColor"] = "";

            //配列を初期化
            $list_tag = array();
            for ($cnt=1; $cnt<8; $cnt++)
            {
                $list_tag["DAYCD".$cnt] = "<td width=\"116\" height=\"40\" nowrap></td>";
            }

            $ay = (isset($data_all[$period_data[$i]]))?  $data_all[$period_data[$i]] : array() ;
            $b = get_count($ay);

            if($b == 0){
                $b ++;
                $list_tag["TARGETCLASS"] = $period_name[$period_data[$i]];
            }else{

                $list_tag["TARGETCLASS"] = $period_name[$period_data[$i]];

                for ($ii = 0; $ii < $b; $ii++ )
                {
                    foreach( ($data_all[$period_data[$i]][$ii]) as $key => $val)
                    {
                        switch ($key)
                        {
                            case "DAYCD":
                                $set_target = $key.$val;
                                $id_val = $val;
                                $noFound = $hr_nameabbv_box[$period_data[$i]][$val];
                                break;

                            default:
                                $list_tag[$key] = $val;
                        }
                    }
                    $width_num = ($id_val == 1) ? "\"117\" " : "\"116\"";
                    $set_tag  = "<td nowrap class=\"stfn\" width=".$width_num ;
                    $set_tag .= " id=\"".$id_val ;
                    $set_tag .= "\" ><FONT SIZE=\"2\">".$noFound."</FONT></td>";

                    $list_tag["$set_target"] = $set_tag ;
                }
            }
            $arg["data"][] = $list_tag;
        }


        //終 了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => " 終 了 ",
                            "extrahtml" => " onclick=\"return closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");


        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjb0080Form1.html", $arg);
    }
}
?>
