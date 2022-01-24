<?php

require_once('for_php7.php');

class knjl130kForm1
{
    function main(&$model)
    {

        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl130kindex.php", "", "main");

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        $db = Query::dbCheckOut();

        //一覧表示
        //データを取得
        $Row = $db->getRow(knjl130kQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        $model->befJudgement = $Row["JUDGEMENT"];
        $disabled = "";

        if ($model->cmd == "back1" || $model->cmd == "next1" || $model->cmd == 'back2' ||$model->cmd == 'next2') {
            //前の志願者,次の志願者でデータが存在しない場合は現在の受験番号でデータを取得し直す
            if (!is_array($Row)) {
                if ($model->cmd == "back2" || $model->cmd == "next2") {
                    $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                }
                $model->cmd = "main";
                $Row = $db->getRow(knjl130kQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
            } else {
                $model->examno = $Row["EXAMNO"];
            }
        }

        if ((!isset($model->warning))) {
            if ($model->cmd != 'change' && $model->cmd != 'change_judge' && $model->cmd != 'change_testdiv') {
                $model->desirediv = $Row["DESIREDIV"];
                $model->judgement = $Row["JUDGEMENT"];
                $model->procedurediv = $Row["PROCEDUREDIV"];
            }
        }
        //データが無ければ更新ボタン等を押し不可にする
        if (!is_array($Row)) {
            $disabled = "disabled";
            if ($model->cmd == 'reference') {
                $model->setWarning("MSG303");
            }
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }                

        //警告メッセージがある場合はフォームの値を参照する
        if ((isset($model->warning)) || $model->cmd == 'change' || $model->cmd == 'change_judge' || $model->cmd == 'change_testdiv') {
            $Row["SEX"]                   =& $model->field["SEX"];   #2005/11/14 arakaki
            $Row["SHDIV"]                 =& $model->field["SHDIV"];   #2005/11/14 arakaki
            $Row["APPLICANTDIV"]          =& $model->field["APPLICANTDIV"];
            $Row["JUDGEMENT_GROUP_NO"]    =& $model->field["JUDGEMENT_GROUP_NO"];
            $Row["TESTDIV"]               =& $model->field["TESTDIV"];
            $Row["SUC_COURSECD"]          =& $model->field["SUC_COURSECD"];
            $Row["SUC_MAJORCD"]           =& $model->field["SUC_MAJORCD"];
            $Row["SUC_COURSECODE"]        =& $model->field["SUC_COURSECODE"];
            $Row["ENTDIV"]                =& $model->field["ENTDIV"];
            $Row["SUCCESS_NOTICENO"]      =& $model->field["SUCCESS_NOTICENO"];
            $Row["OLD_SUCCESS_NOTICENO"]  =& $model->field["OLD_SUCCESS_NOTICENO"]; //2005.12.30 minei
            $Row["FAILURE_NOTICENO"]      =& $model->field["FAILURE_NOTICENO"];
            $Row["INTERVIEW_ATTEND_FLG"]  =& $model->field["INTERVIEW_ATTEND_FLG"];
            $Row["SCALASHIPDIV"]          =& $model->field["SCALASHIPDIV"];
        }

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $disabled = "disabled";
            $disabled_c = "disabled";
        }
        
         //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->examno ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //名称マスタからデータを取得してコンボ用の配列にセット
        $result = $db->query(knjl130kQuery::getName($model->year, array("Z002","L003","L005","L006","L010","L008","L009")));
        $opt = array();
        $opt["Z002"] = array();
        $opt["Z002"][] = array("label"  =>  "", "value"  => "");	//性別コンボの先頭行に空白をセット 2005.11.11  m.kuninaka
        $opt["L003"] = array();
        $opt["L005"] = array();
        $opt["L005"][] = array("label"  =>  "", "value"  => "");	//出願区分コンボの先頭行に空白をセット  2005.11.11  m.kuninaka
        $opt["L006"] = array();
        $opt["L006"][] = array("label"  =>  "", "value"  => "");	//専併区分コンボの先頭行に空白をセット  2005.11.11  m.kuninaka
        $opt["L010"] = array();
        $opt["L010"][] = array("label"  =>  '　　　　', "value"  => "");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                                            "value"  =>  $row["NAMECD2"]);
            if ($row["NAMECD1"] == "L008") {
                $arg["data"]["CONFIDENTIAL".$row["NAMECD2"]] = $row["NAME1"];
            }
            if ($row["NAMECD1"] == "L009") {
                $arg["data"]["TESTSUBCLASSCD".$row["NAMECD2"]] = $row["NAME1"];
            }
        }
        $result->free();

        //---------------------------------志願者基礎データ----------------------------------
        $arg["data"]["NAME_KANA"]  = htmlspecialchars($Row["NAME_KANA"]);
        $arg["data"]["FS_GRDYEAR"] = $Row["FS_GRDYEAR"];
        $arg["data"]["BIRTHDAY"]   = $Row["BIRTHDAY"]? str_replace("-", "/", $Row["BIRTHDAY"]) : "";
        $arg["data"]["FORMNO"]     = $Row["FORMNO"];

        //試験区分コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('change_testdiv')\";",
                            "value"       => $model->testdiv,
                            "options"     => $opt["L003"] ) );
        $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //起動時に初期値設定
        if (!isset($model->testdiv)) {
            $model->testdiv = $opt["L003"][0]["value"];
        }

        //性別コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEX",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["SEX"],
                            "options"     => $opt["Z002"] ) );
        $arg["data"]["SEX"] = $objForm->ge("SEX");

        //出願区分コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "APPLICANTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["APPLICANTDIV"],
                            "options"     => $opt["L005"] ) );
        $arg["data"]["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");

        //専併区分コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "SHDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["SHDIV"],
                            "options"     => $opt["L006"] ) );
        $arg["data"]["SHDIV"] = $objForm->ge("SHDIV");

        //志望区分コンボ
        $result = $db->query(knjl130kQuery::getExamcourse($model));
        $examcourse = array();
        $desirediv = array();
        $i=0;

        #2005/11/14 arakaki
        $desirediv[] = array("label" => "",
                              "value" => "" );

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if($i==0)   $desire = $row["DESIREDIV"];

            //desirediv が同じなら配列に入れていく
            if($desire == $row["DESIREDIV"]){
                $examcourse[] = $row["EXAMCOURSE_NAME"];
                $desire = $row["DESIREDIV"];
                $i++;
            //desirediv が違ったらコンボ表示の配列に入れていく
            }else{
                $desirediv[] = array("label" => $desire."：".implode("/",$examcourse),
                                     "value" => $desire );
                $desire = $row["DESIREDIV"];
                $examcourse = array();
                $examcourse[] = $row["EXAMCOURSE_NAME"];
            }
        }
        if (isset($desire)) {
            $desirediv[] = array("label" => $desire."：".implode("/",$examcourse),
                                 "value" => $desire );
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "DESIREDIV",
                            "size"        => "1",
#                            "extrahtml"   => "onChange=\" return btn_submit('change');\"", #2005/11/14 arakaki
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $model->desirediv,
                            "options"     => $desirediv ) );
        $arg["data"]["DESIREDIV"] = $objForm->ge("DESIREDIV");
        
        //起動時に初期値設定
        if (!isset($model->desirediv)) {
            $model->desirediv = $desirediv[0]["value"];
        }
        $result->free();

        //判定コンボ
        $opt_judge = array();
        if ($model->befJudgement == "8") {
            for ($judcnt = 0; $judcnt < get_count($opt["L010"]); $judcnt++) {
                if ($opt["L010"][$judcnt]["value"] == "" || $opt["L010"][$judcnt]["value"] == 7 || $opt["L010"][$judcnt]["value"] == 8 || $opt["L010"][$judcnt]["value"] == 0) {
                    $opt_judge[] = $opt["L010"][$judcnt];
                }
            }
        } else {
            $opt_judge = $opt["L010"];
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "JUDGEMENT",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('change_judge')\";",
                            "value"       => $model->judgement,
                            "options"     => $opt_judge ) );
        $arg["data"]["JUDGEMENT"] = $objForm->ge("JUDGEMENT");

        if($model->judgement == 5 || $model->judgement == 6){
            $disabled_j = "";
        }else{
            $disabled_j = "disabled";
        }

        //追加繰上合格No.
        $objForm->ae( array("type"        => "text",
                            "name"        => "JUDGEMENT_GROUP_NO",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "$disabled_j onblur=\"this.value=toInteger(this.value)\", onChange=\"change_flg()\"",
                            "value"       => $Row["JUDGEMENT_GROUP_NO"] ));
        $arg["data"]["JUDGEMENT_GROUP_NO"] = $objForm->ge("JUDGEMENT_GROUP_NO");

        if(($model->judgement >= 1 && $model->judgement <= 6) || $model->judgement == 9){
                $arg["data"]["SUCCESSFAILURE"] = "※ 合否";
        }else{
                $arg["data"]["SUCCESSFAILURE"] = "　 合否";
        }

        $opt_cmcd       = array();                                                  //志望学科
        $opt_cmcd[]     = array("label"  =>  '　　　　　　　　　　　　　　　　　', "value"  => "");
        $opt["L011"]    = array();                                                  //手続区分
        $opt["L011"][]  = array("label"  =>  '　　', "value"  => "");
        $opt["L012"]    = array();                                                  //入学手続
        $opt["L012"][]  = array("label"  =>  '　　　', "value"  => "");        
        $opt["Z006"]    = array();                                                  //スカラシップ
        $opt["Z006"][]  = array("label"  =>  '　　　　　　　　　　　', "value"  => "");
        $opt_inflg      = array();                                                  //面接出欠
        $opt_inflg[]    = array("label"  =>  '　　　', "value"  => "");

        //判定で合格なら氏名を赤で表示し、各コンボに値を追加する
        if (($model->judgement >= 1 && $model->judgement <= 6) || $model->judgement == 9) {
            //氏名を赤字で設定
            $arg["data"]["NAME"] = "<font color=\"#ff0000\">".htmlspecialchars($Row["NAME"])."</font>";

            //志望学科に追加
            $result     = $db->query(knjl130kQuery::get_coursemajor($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt_cmcd[] = array("label" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"].":".$row["COURSENAME"],
                                    "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
            }
            $Row["COURSEMAJOR"] = $Row["SUC_COURSECD"].$Row["SUC_MAJORCD"].$Row["SUC_COURSECODE"];
            $result->free();

            //手続区分,スカラシップに追加
            $result = $db->query(knjl130kQuery::getName($model->year, array("L011","Z006")));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                                                "value"  =>  $row["NAMECD2"]);
            }
            $result->free();

            //面接出欠に追加
            $opt_inflg[] = array("label"  =>  '1:出席', "value"  => 1);
            $opt_inflg[] = array("label"  =>  '2:欠席', "value"  => 2);

            if ($model->cmd == 'change_judge' && $Row["SHDIV"] == 1 &&
                ($model->befJudgement == 7 || $model->befJudgement == 8 || $model->befJudgement == 0) && $model->judgement == 5) {
                $model->procedurediv = "2";
                $Row["ENTDIV"] = "2";
            }
            //手続区分が「済み」なら値を追加
            if($model->procedurediv == "2") {
                $result = $db->query(knjl130kQuery::getName($model->year, array("L012")));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                                                    "value"  =>  $row["NAMECD2"]);
                }
                $result->free();
            }
#            $result->free();   2005/08/10 arakaki
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }
        //志望学科コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "COURSEMAJOR",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["COURSEMAJOR"],
                            "options"     => $opt_cmcd ) );
        $arg["data"]["COURSEMAJOR"] = $objForm->ge("COURSEMAJOR");

        //合格通知NO.
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUCCESS_NOTICENO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\", onChange=\"change_flg()\"",
                            "value"       => $Row["SUCCESS_NOTICENO"] ));
        $arg["data"]["SUCCESS_NOTICENO"] = $objForm->ge("SUCCESS_NOTICENO");

        //旧合格通知NO. 2005.12.30 minei
        $arg["data"]["OLD_SUCCESS_NOTICENO"] = $Row["OLD_SUCCESS_NOTICENO"];

        #2006/01/10
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "OLD_SUCCESS_NOTICENO",
                            "value"     => $Row["OLD_SUCCESS_NOTICENO"]) );

        //不合格通知NO.
        $objForm->ae( array("type"        => "text",
                            "name"        => "FAILURE_NOTICENO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\", onChange=\"change_flg()\"",
                            "value"       => $Row["FAILURE_NOTICENO"] ));
        $arg["data"]["FAILURE_NOTICENO"] = $objForm->ge("FAILURE_NOTICENO");

        //手続区分コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "PROCEDUREDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('change')\";",
                            "value"       => $model->procedurediv,
                            "options"     => $opt["L011"] ));
        $arg["data"]["PROCEDUREDIV"] = $objForm->ge("PROCEDUREDIV");

        //入学手続コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "ENTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["ENTDIV"],
                            "options"     => $opt["L012"] ));
        $arg["data"]["ENTDIV"] = $objForm->ge("ENTDIV");

        //面接出欠コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "INTERVIEW_ATTEND_FLG",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["INTERVIEW_ATTEND_FLG"],
                            "options"     => $opt_inflg ));
        $arg["data"]["INTERVIEW_ATTEND_FLG"] = $objForm->ge("INTERVIEW_ATTEND_FLG");
        
        //スカラシップ区分
        $objForm->ae( array("type"        => "select",
                            "name"        => "SCALASHIPDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["SCALASHIPDIV"],
                            "options"     => $opt["Z006"] ));
        $arg["data"]["SCALASHIPDIV"] = $objForm->ge("SCALASHIPDIV");

        $arg["data"]["A_TOTAL"]      = $Row["A_TOTAL"];
        $arg["data"]["A_AVERAGE"]    = $Row["A_AVERAGE"];
        $arg["data"]["A_TOTAL_RANK"] = $Row["A_TOTAL_RANK"];
        $arg["data"]["A_DIV_RANK"]   = $Row["A_DIV_RANK"];
        $arg["data"]["B_TOTAL"]      = $Row["B_TOTAL"];
        $arg["data"]["B_AVERAGE"]    = $Row["B_AVERAGE"];
        $arg["data"]["B_TOTAL_RANK"] = $Row["B_TOTAL_RANK"];
        $arg["data"]["B_DIV_RANK"]   = $Row["B_DIV_RANK"];
        
        //学校コード
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "FS_CD",
                            "value"     => $Row["FS_CD"]) );
        //---------------------------------志願者基礎データ----------------------------------


        //---------------------------------事前判定----------------------------------        
        $result = $db->query(knjl130kQuery::get_judge_name($model->year, "L002"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($judge_name == "") {
                $judge_name = $row["NAME1"];
            } else {
                $judge_name .= "," . $row["NAME1"];
            }
        }

        $result = $db->query(knjl130kQuery::get_examcourse($model->year));
        $i = 0;
        while($Row2 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //コース記号
            $arg["data"]["EXAMCOURSE_MARK".$i] = $Row2["EXAMCOURSE_MARK"];

            $Row["JUDGEMENT_S"] = $db->getOne(knjl130kQuery::getCantcons($model, $Row2["COURSECD"], $Row2["MAJORCD"], $Row2["EXAMCOURSECD"], "1"));
            $Row["JUDGEMENT_H"] = $db->getOne(knjl130kQuery::getCantcons($model, $Row2["COURSECD"], $Row2["MAJORCD"], $Row2["EXAMCOURSECD"], "2"));

            //判定(専願)
            $objForm->ae( array("type"        => "text",
                                "name"        => "JUDGEMENT1_".$i,
                                "size"        => 1,
                                "maxlength"   => 1,
                                "extrahtml"   => "style=\"text-align:center\" onblur=\"toCheck(this, '".$judge_name."')\", onChange=\"change_flg()\"",
                                "value"       => $Row["JUDGEMENT_S"] ));
            $arg["data"]["JUDGEMENT1_".$i] = $objForm->ge("JUDGEMENT1_".$i);

            //判定(併願)
            $objForm->ae( array("type"        => "text",
                                "name"        => "JUDGEMENT2_".$i,
                                "size"        => 1,
                                "maxlength"   => 1,
                                "extrahtml"   => "style=\"text-align:center\" onblur=\"toCheck(this, '".$judge_name."')\", onChange=\"change_flg()\"",
                                "value"       => $Row["JUDGEMENT_H"] ));
            $arg["data"]["JUDGEMENT2_".$i] = $objForm->ge("JUDGEMENT2_".$i);
            
            //過程コード
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "COURSECD".$i,
                                "value"     => $Row2["COURSECD"]) );
            //学科コード
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "MAJORCD".$i,
                                "value"     => $Row2["MAJORCD"]) );
            //コースコード
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "EXAMCOURSECD".$i,
                                "value"     => $Row2["EXAMCOURSECD"]) );
            $i++;
        }
        $result->free();

        //---------------------------------志願者得点データ----------------------------------
        $result = $db->query(knjl130kQuery::getScore($model));
        while($Row3 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $subclasscd = $Row3["TESTSUBCLASSCD"];
            $arg["data"]["ATTEND".$subclasscd] = ($Row3["ATTEND_FLG"]=="1")? "○" : "";
            $arg["data"]["A_SCORE".$subclasscd]  = $Row3["A_SCORE"];
            $arg["data"]["A_STDSC".$subclasscd]  = $Row3["A_STD_SCORE"];
            $arg["data"]["A_RANK".$subclasscd]   = $Row3["A_RANK"];
            $arg["data"]["B_SCORE".$subclasscd]  = $Row3["B_SCORE"];
            $arg["data"]["B_STDSC".$subclasscd]  = $Row3["B_STD_SCORE"];
            $arg["data"]["B_RANK".$subclasscd]   = $Row3["B_RANK"];
        }
        $result->free();
        //---------------------------------志願者得点データ----------------------------------
        

        //---------------------------------志願者内申データ----------------------------------
        $arg["data"]["RPT01"] = $Row["CONFIDENTIAL_RPT01"];
        $arg["data"]["RPT02"] = $Row["CONFIDENTIAL_RPT02"];
        $arg["data"]["RPT03"] = $Row["CONFIDENTIAL_RPT03"];
        $arg["data"]["RPT04"] = $Row["CONFIDENTIAL_RPT04"];
        $arg["data"]["RPT05"] = $Row["CONFIDENTIAL_RPT05"];
        $arg["data"]["RPT06"] = $Row["CONFIDENTIAL_RPT06"];
        $arg["data"]["RPT07"] = $Row["CONFIDENTIAL_RPT07"];
        $arg["data"]["RPT08"] = $Row["CONFIDENTIAL_RPT08"];
        $arg["data"]["RPT09"] = $Row["CONFIDENTIAL_RPT09"];
        $arg["data"]["RPT10"] = $Row["CONFIDENTIAL_RPT10"];
        $arg["data"]["RPT11"] = $Row["CONFIDENTIAL_RPT11"];
        $arg["data"]["RPT12"] = $Row["CONFIDENTIAL_RPT12"];
        $arg["data"]["TOTAL_REPORT"]  = $Row["TOTAL_REPORT"];
        $arg["data"]["AVERAGE5"]      = $Row["AVERAGE5"];
        $arg["data"]["AVERAGE_ALL"]   = $Row["AVERAGE_ALL"];
        //---------------------------------志願者内申データ----------------------------------

        //試験会場
        $arg["data"]["EXAMHALL_NAME"]   = $Row["EXAMHALL_NAME"];

        Query::dbCheckIn($db);

        //-------------------------------- ボタン作成 ------------------------------------
        //検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reference",
                            "value"     => "検 索",
                            "extrahtml" => "onclick=\"btn_submit('reference');\"" ) );
        $arg["button"]["btn_reference"] = $objForm->ge("btn_reference");

        global $sess;
        //かな検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_kana_reference",
                            "value"       => "かな検索",
                            "extrahtml"   => "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL130K/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
        $arg["button"]["btn_kana_reference"] = $objForm->ge("btn_kana_reference");

        //前の志願者検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => " << ",
                            "extrahtml"   => "onClick=\"btn_submit('back1');\"" ) );
        
        //次の志願者検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => " >> ",
                            "extrahtml"   => "onClick=\"btn_submit('next1');\"" ) );
        $arg["button"]["btn_back_next"] = $objForm->ge("btn_back").$objForm->ge("btn_next");
                            
        //参照ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_open",
                            "value"       => " 参照 ",
                            "extrahtml"   => "$disabled style=\"width:50px\" onclick=\" loadwindow('knjl130kindex.php?cmd=open&EXAMNO='+document.forms[0]['EXAMNO'].value+'&TESTDIV='+document.forms[0]['TESTDIV'].value+'&fs_cd='+document.forms[0]['FS_CD'].value,body.clientWidth/2-(-40),body.clientHeight/2-145,450, 240);\"") );
        $arg["button"]["btn_open"] = $objForm->ge("btn_open");

        //更新ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "$disabled onclick=\"btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //更新ボタン(更新後前の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の志願者",
                            "extrahtml" => "$disabled style=\"width:150px\" onclick=\"btn_submit('back2');\"" ) );
        //更新ボタン(更新後次の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の志願者",
                            "extrahtml" =>  "$disabled style=\"width:150px\" onclick=\"btn_submit('next2');\"" ) );
        $arg["button"]["btn_up_next"] = $objForm->ge("btn_up_pre") . $objForm->ge("btn_up_next");

        //取消ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "$disabled_c onclick=\"btn_submit('reset');\""  ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        //入試年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cflg",
                            "value"     => $model->cflg) );


        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl130kForm1.html", $arg);
    }
}
?>