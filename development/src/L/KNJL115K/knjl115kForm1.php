<?php

require_once('for_php7.php');

class knjl115kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl115kindex.php", "", "main");
        $db           = Query::dbCheckOut();

        if (isset($model->warning))
        {
            $Row = array("PASSCOUNT"      => $model->field["PASSCOUNT"],
                         "STARTNUMBER"    => $model->field["STARTNUMBER"] );
        }

        $Row["TESTDIV"]    = $model->field["TESTDIV"];
        $Row["OUTPUT"]     = $model->field["OUTPUT"];
        $Row["SUC_RADIO"]  = $model->field["SUC_RADIO"];
        $Row["PASSCOUNT"]  = $model->field["PASSCOUNT"];

        $arg["CTRL_YEAR"] = $model->year ."年度";

        //試験区分
        $result = $db->query( knjl115kQuery::getName("L003",$model->year) );
        $opt = array();

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".htmlspecialchars($row["NAME1"]), "value" => $row["NAMECD2"]);
            if($Row["TESTDIV"] == "") $Row["TESTDIV"] = $row["NAMECD2"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "onchange=\"return btn_submit('main')\"",
                            "value"      => $Row["TESTDIV"],
                            "options"    => $opt));

        $arg["TESTDIV"] = $objForm->ge("TESTDIV");

        //終了ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"return closeWin();\"" ) );

        $arg["btn_end"]  = $objForm->ge("btn_end");


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        
        if (!isset($model->jhflg)){
            $model->jhflg = $db->getOne(knjl115kQuery::GetJorH()); #中学・高校判定 2005/12/29
        }
        //合否区分ラジオボタン作成(合格者・不合格者)
        $opt_gouhi = array("0" => 1, "1" => 2 );
        if($Row["OUTPUT"]=="") $Row["OUTPUT"] = "1";

        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUT",
                            "value"      => $Row["OUTPUT"],
                            "extrahtml"  => " onClick=\"passCountCtrl(this);\" onChange=\"passCountCtrl(this);\" ",
                            "multiple"   => $opt_gouhi));

        $arg["OUTPUT1"] = $objForm->ge("OUTPUT",1);
        $arg["OUTPUT2"] = $objForm->ge("OUTPUT",2);


        //合格者ラジオボタン作成(一般・追加繰上・附属推薦)#2005/12/29
        $disabled = "onClick=\"passCountCtrl(this);\" onChange=\"passCountCtrl(this);\" ";
        $title3 = "";
        if($model->jhflg == "0"){
            $title3 = "中高一貫";
        }else{
            $title3 = "附属推薦";
        }
        $arg["TITLE3"] = $title3;

        $opt_suc_radio = array("0" => 11, "1" => 12, "2" => 13 );
        if($Row["SUC_RADIO"]=="" && $Row["OUTPUT"]=="1") $Row["SUC_RADIO"] = "11";



        $objForm->ae( array("type"       => "radio",
                            "name"       => "SUC_RADIO",
                            "value"      => $Row["SUC_RADIO"],
                            "extrahtml"  => "onClick=\"passCountCtrl(this);\" onChange=\"passCountCtrl(this);\" ",
                            "multiple"   => $opt_suc_radio));
        $arg["SUC_RADIO1"] = $objForm->ge("SUC_RADIO",11);
        $arg["SUC_RADIO2"] = $objForm->ge("SUC_RADIO",12);
        $arg["SUC_RADIO3"] = $objForm->ge("SUC_RADIO",13);

        //追加合格者回数combobox作成
        $disabled = ($Row["SUC_RADIO"] != 12)? " disabled " : "" ;
        $result = $db->query( knjl115kQuery::getJudgementGroupNo($model->year, $Row["TESTDIV"]) );
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["JUDGEMENT_GROUP_NO"], "value" => $row["JUDGEMENT_GROUP_NO"]);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "PASSCOUNT",
                            "value"      => $Row["PASSCOUNT"],
                            "options"    => $opt,
                            "extrahtml"   => $disabled ) );

        $arg["PASSCOUNT"] = $objForm->ge("PASSCOUNT");

        #一覧表
        $result = $db->query(knjl115kQuery::GetListDT($model->year, $title3));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row["TRNCEDATE"] = str_replace("-","/",$row["TRNCEDATE"]);

            $arg["data"][] = $row;
        }

        //ラベル合格最終番号作成 #2006/01/12
        $row = $db->getRow( knjl115kQuery::getNoticeNo($model->year),DB_FETCHMODE_ASSOC);
        $max_success_noticeno = $row["MAX_SUCCESS_NOTICENO"];
        $max_failure_noticeno = $row["MAX_FAILURE_NOTICENO"];

        //ラベル合格最終番号作成
        if ($max_success_noticeno=="" || $max_success_noticeno=="0"){
            if($model->jhflg == "0"){
                $max_success_noticeno='5001';
            }else{
                $max_success_noticeno='6001';
            }
        }else{
            $max_success_noticeno=$max_success_noticeno+1;
        }

        //ラベル不合格最終番号作成
        if ($max_failure_noticeno=="" || $max_failure_noticeno=="0"){
            if( "8000" < $max_success_noticeno){
                $max_failure_noticeno='9001';
            }else{
                $max_failure_noticeno='8001';
            }
        }else{
            $max_failure_noticeno=$max_failure_noticeno+1;
        }

        if($Row["OUTPUT"] == "1"){
            $Row["STARTNUMBER"] = $max_success_noticeno;
        }else{
            $Row["STARTNUMBER"] = $max_failure_noticeno;
        }

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "max_success_noticeno",
                            "value"      => $max_success_noticeno) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "max_failure_noticeno",
                            "value"      => $max_failure_noticeno) );

        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "text",
                            "name"       => "STARTNUMBER",
                            "value"      => $Row["STARTNUMBER"],
                            "size"       => 4,
                            "maxlength"  => 4,
                            "extrahtml"  => " onBlur=\"this.value=toInteger(this.value);\" " ) );

        $arg["STARTNUMBER"] = $objForm->ge("STARTNUMBER");

        //実行ボタン作成
        $objForm->ae( array("type"       => "button",
                            "name"       => "btn_doit",
                            "value"      => "採番実行",
                            "extrahtml"   => " onClick=\"btn_submit('update');\" " ) );

        $arg["btn_doit"] = $objForm->ge("btn_doit");

        //クリアボタン作成  #2005/12/30
        $objForm->ae( array("type"       => "button",
                            "name"       => "btn_clear",
                            "value"      => "番号クリア",
                            "extrahtml"   => " onClick=\"btn_submit('clear');\" " ) );


        $arg["btn_clear"] = $objForm->ge("btn_clear");


        //db close
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl115kForm1.html", $arg); 
    }
}
?>
