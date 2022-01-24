<?php

require_once('for_php7.php');

class knjl020kForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $db = Query::dbCheckOut();

        //対象年度
        $arg["data"]["YEAR"] = $model->year;

        if ($model->isWarning()){
            $Row = $model->field;
            for($i = 0; $i < 5; $i++){
                $Row["FS_ITEM".($i+1)] = $model->field["FS_ITEM"][$i];
                $Row["PS_ITEM".($i+1)] = $model->field["PS_ITEM"][$i];
            }
        }else{
            //志願者基礎データを取得する
            $Row = $db->getRow(knjl020kQuery::selectQuery($model), DB_FETCHMODE_ASSOC);
            $disabled = "";
            if (!is_array($Row)){
                $disabled = "disabled";
                if ($model->cmd == "search"){
                    $model->field2 = '';
                    $model->setWarning("MSG303");
                }
            }else{
                //作成日付
                $model->create_date = $Row["CREATE_DATE"];
                $model->acceptno    = $Row["ACCEPTNO"];
            }
        }

        //登録日
        $arg["FS_UPDATED"] = $Row["FS_UPDATED"];
        $arg["PS_UPDATED"] = $Row["PS_UPDATED"];
        //受付NO
        $arg["ACCEPTNO2"]   = $Row["ACCEPTNO"];
        $arg["FS_ACCEPTNO"] = $Row["FS_ACCEPTNO"];
        $arg["PS_ACCEPTNO"] = $Row["PS_ACCEPTNO"];

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "FS_UPDATED",
                            "value"     => $Row["FS_UPDATED"]
                            ) );
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PS_UPDATED",
                            "value"     => $Row["PS_UPDATED"]
                            ) );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "FS_ACCEPTNO",
                            "value"     => $Row["FS_ACCEPTNO"]
                            ) );
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PS_ACCEPTNO",
                            "value"     => $Row["PS_ACCEPTNO"]
                            ) );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ACCEPTNO2",
                            "value"     => $Row["ACCEPTNO"]
                            ) );

        //ヘッダ
        $result = $db->query(knjl020kQuery::getName($model->year, array("L003","Z002","L006","L002")));
        $opt = array();
        $opt["L003"] = array();
        $opt["Z002"] = array();
        $opt["L006"] = array();
        $opt["L002"] = array();
        $opt["L006"][] = array("label"  =>  '　　　　', "value"  => 0);
        $opt["L002"][] = array("label"  =>  '　　　　', "value"  => 0);
        $opt["Z002"][] = array("label"  =>  '', "value"  => "");	//alp m-yama
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
            if (!isset($model->testdiv)) $model->testdiv = $row["NAMECD2"];
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "onchange=\"btn_submit('testDivChange');\"",
                            "value"      => $model->testdiv,
                            "options"    => $opt["L003"]));
        $arg["TESTDIV"] = $objForm->ge("TESTDIV");

	//↓受付Noの登録最大値と最小値を取得 2005/10/25 M.kuninaka
        $chip = $db->getRow(knjl020kQuery::GetAcceptno($model), DB_FETCHMODE_ASSOC);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ACCEPTNO_MIN",
                            "value"     => $chip["ACCEPTNO_MIN"]
                            ));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ACCEPTNO_MAX",
                            "value"     => $chip["ACCEPTNO_MAX"]
                            ));
	//↑ここまで

        $objForm->ae( array("type"       => "select",
                            "name"       => "SEX",
                            "size"       => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"      => $Row["SEX"],
                            "options"    => $opt["Z002"]));
        $arg["SEX"] = $objForm->ge("SEX");

        //**********************: 事前相談志望データ取得 **********************************/
        if ($model->isWarning()){
            for($i = 0; $i < 4; $i++){
                $Row2["1,".($i+1)]["COURSE"] = $model->field2["F_COURSE"][$i];
                $Row2["2,".($i+1)]["COURSE"] = $model->field2["P_COURSE"][$i];
                $Row2["1,".($i+1)]["SHDIV"] = $model->field2["F_SHDIV"][$i];
                $Row2["2,".($i+1)]["SHDIV"] = $model->field2["P_SHDIV"][$i];
                $Row2["1,".($i+1)]["JUDGEMENT"] = $model->field2["F_JUDGEMENT"][$i];
                $Row2["2,".($i+1)]["JUDGEMENT"] = $model->field2["P_JUDGEMENT"][$i];
            }
        }else{        
            $result = $result = $db->query(knjl020kQuery::selectQuery_Consultation_dat($model));
            $Row2 = array();
            $model->org = array();

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                //ORGを退避
                $model->org["ORG_SHDIV"][$row["DATADIV"].",".$row["WISHNO"]] = $row["ORG_SHDIV"];
                $model->org["ORG_MAJORCD"][$row["DATADIV"].",".$row["WISHNO"]] = $row["ORG_MAJORCD"];
                $model->org["ORG_JUDGEMENT"][$row["DATADIV"].",".$row["WISHNO"]] = $row["ORG_JUDGEMENT"];
                $Row2[$row["DATADIV"].",".$row["WISHNO"]] = $row;
            }
        }
        //コース
        $result = $db->query(knjl020kQuery::selectQueryCourse($model));
        $opt_course = array();
        $opt_course[] = array("label"  =>  '　　　　', "value"  => 0);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cd = $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"];
            
            $opt_course[] = array("label"  =>  $cd .":" .htmlspecialchars($row["EXAMCOURSE_MARK"]), "value"  => $cd);
        }
        $arg["F_SHDIV"] = array();
        $arg["P_SHDIV"] = array();
        $arg["F_JUDGEMENT"] = array();
        $arg["P_JUDGEMENT"] = array();
        $arg["F_COURSE"] = array();
        $arg["P_COURSE"] = array();
        for($i = 1; $i <= 4; $i++){
            //専併
            $objForm->ae( array("type"       => "select",
                                "name"       => "F_SHDIV[]",
                                "size"       => "1",
                                "extrahtml"   => "onChange=\"change_flg()\"",
                                "value"      => $Row2["1,".$i]["SHDIV"],
                                "options"    => $opt["L006"]));
            $arg["F_SHDIV"][] = $objForm->ge("F_SHDIV[]");

            $objForm->ae( array("type"       => "select",
                                "name"       => "P_SHDIV[]",
                                "size"       => "1",
                                "extrahtml"   => "onChange=\"change_flg()\"",
                                "value"      => $Row2["2,".$i]["SHDIV"],
                                "options"    => $opt["L006"]));
            $arg["P_SHDIV"][] = $objForm->ge("P_SHDIV[]");

            //判定
            $objForm->ae( array("type"       => "select",
                                "name"       => "F_JUDGEMENT[]",
                                "size"       => "1",
                                "extrahtml"   => "onChange=\"change_flg()\"",
                                "value"      => $Row2["1,".$i]["JUDGEMENT"],
                                "options"    => $opt["L002"]));
            $arg["F_JUDGEMENT"][] = $objForm->ge("F_JUDGEMENT[]");

            $objForm->ae( array("type"       => "select",
                                "name"       => "P_JUDGEMENT[]",
                                "size"       => "1",
                                "extrahtml"   => "onChange=\"change_flg()\"",
                                "value"      => $Row2["2,".$i]["JUDGEMENT"],
                                "options"    => $opt["L002"]));
            $arg["P_JUDGEMENT"][] = $objForm->ge("P_JUDGEMENT[]");

            //コース
            $objForm->ae( array("type"       => "select",
                                "name"       => "F_COURSE[]",
                                "size"       => "1",
                                "extrahtml"   => "onChange=\"change_flg()\"",
                                "value"      => $Row2["1,".$i]["COURSE"],
                                "options"    => $opt_course));
            $arg["F_COURSE"][] = $objForm->ge("F_COURSE[]");

            $objForm->ae( array("type"       => "select",
                                "name"       => "P_COURSE[]",
                                "size"       => "1",
                                "extrahtml"   => "onChange=\"change_flg()\"",
                                "value"      => $Row2["2,".$i]["COURSE"],
                                "options"    => $opt_course));
            $arg["P_COURSE"][] = $objForm->ge("P_COURSE[]");
        }

        //**********************: 事前相談志望データ取得 **********************************/

        //出身学校名称
        $result = $db->query(knjl020kQuery::selectQueryFinschool($model));
        $opt = array();
        $opt[] = array("label"  =>  '　　　　　　　　', "value"  => 0);
        
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  =>  $row["FS_CD"] .":" .htmlspecialchars($row["FINSCHOOL_NAME"]), "value"  => $row["FS_CD"]);
        }
        //出身学校名称
        $objForm->ae( array("type"       => "select",
                            "name"       => "FS_CD",
                            "size"       => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"      => $Row["FS_CD"],
                            "options"    => $opt));
        $arg["FS_CD"] = $objForm->ge("FS_CD");

        //出身塾名称
        $result = $db->query(knjl020kQuery::selectQueryPrischool($model));
        $opt = array();
        $opt[] = array("label"  =>  '　　　　　　　　', "value"  => 0);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
#            $opt[] = array("label"  =>  $row["PS_CD"] .":" .htmlspecialchars($row["PINSCHOOL_NAME"]), "value"  => $row["PS_CD"]);  2005/07/26
            $opt[] = array("label"  =>  $row["PS_CD"] .":" .htmlspecialchars($row["PRISCHOOL_NAME"]), "value"  => $row["PS_CD"]);
        }
        //出身学校名称
        $objForm->ae( array("type"       => "select",
                            "name"       => "PS_CD",
                            "size"       => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"      => $Row["PS_CD"],
                            "options"    => $opt));
        $arg["PS_CD"] = $objForm->ge("PS_CD");

        //受付N0
        $objForm->ae( array("type"        => "text",
                            "name"        => "ACCEPTNO",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "style=\"text-align:right\" onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => $model->acceptno
                            ));
        $arg["ACCEPTNO"] = $objForm->ge("ACCEPTNO");

        //氏名
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME",
                            "size"        => 30,
                            "maxlength"   => 63,
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["NAME"]
                            ));
        $arg["NAME"] = $objForm->ge("NAME");

        //氏名かな
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME_KANA",
                            "size"        => 30,
                            "maxlength"   => 243,
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["NAME_KANA"]
                            ));
        $arg["NAME_KANA"] = $objForm->ge("NAME_KANA");
        
        $arg["FS_ITEM"] = array();
        $arg["PS_ITEM"] = array();
        for($i = 1; $i <= 5; $i++){
            $objForm->ae( array("type"        => "text",
                                "name"        => "FS_ITEM",
                                "size"        => 5,
                                "multiple"    => true,
                                "maxlength"   => 5,
                                "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toFloat(this.value);\" onChange=\"change_flg()\"",
                                "value"       => $Row["FS_ITEM".$i]
                                ));

            $arg["FS_ITEM"][] = $objForm->ge("FS_ITEM");

            $objForm->ae( array("type"        => "text",
                                "name"        => "PS_ITEM",
                                "size"        => 5,
                                "multiple"    => true,
                                "maxlength"   => 5,
                                "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toFloat(this.value);\" onChange=\"change_flg()\"",
                                "value"       => $Row["PS_ITEM".$i]
                                ));

            $arg["PS_ITEM"][] = $objForm->ge("PS_ITEM");
        }
        Query::dbCheckIn($db);

        global $sess;
        //ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_copy1",
                            "value"     => "出身学校からコピー",
                            "extrahtml" => "$disabled onclick=\"return copy(1);\"" ));
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_copy2",
                            "value"     => "塾からコピー",
                            "extrahtml" => "$disabled onclick=\"return copy(2);\"" ));
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_search",
                            "value"     => "検 索",
                            "extrahtml" => "style=\"width:45px\" onclick=\"return btn_submit('search');\"" ) );
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_kana_reference",
                            "value"     => "かな検索",
                            "extrahtml" => "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL020K/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&acceptno='+document.forms[0]['ACCEPTNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => " << ",
                            "extrahtml" => "style=\"width:32px\" onClick=\"btn_submit('back1');\"" ) );
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_next",
                            "value"     => " >> ",
                            "extrahtml" => "style=\"width:32px\" onClick=\"btn_submit('next1');\"" ) );
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追 加",
                            "extrahtml" => "onclick=\"return btn_submit('insert');\"" ));
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_upd",
                            "value"     => "更 新",
                            "extrahtml" => "$disabled onclick=\"return btn_submit('update');\"" ));
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_preupd",
                            "value"     => "更新後前の相談者",
                            "extrahtml" => "style=\"width:130px\"$disabled onclick=\"return btn_submit('pre_update');\"" ));
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_nextupd",
                            "value"     => "更新後次の相談者",
                            "extrahtml" => "style=\"width:130px\"$disabled onclick=\"return btn_submit('next_update');\"" ));
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => "$disabled onclick=\"return btn_submit('delete');\"" ));
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_cancel",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('cancel');\"" ));
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_close",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ));
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_check",
                            "value"     => "チェックリスト",
                            "size"      => "",
                            "extrahtml" => " onClick=\" wopen('../KNJL300K/knjl300kindex.php?,','SUBWIN2',0,0,screen.availWidth,screen.availheight);\"" ));	//alp m-yama 2005/08/10
//                            "extrahtml" => "style=\"width:100px\" onclick=\"return btn_check('check');\"" ));
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_clear",
                            "value"     => "画面クリア",
                            "size"      => "",
                            "extrahtml" => "onclick=\"dispClear();\"" ));

        $arg["button"] = array("BTN_SEARCH"     => $objForm->ge("btn_search"),
                               "BTN_KANA_SEARCH"=> $objForm->ge("btn_kana_reference"),
                               "BTN_BACK"       => $objForm->ge("btn_back"),
                               "BTN_NEXT"       => $objForm->ge("btn_next"),
                               "BTN_CLEAR"      => $objForm->ge("btn_clear"),
                               "BTN_COPY1"      => $objForm->ge("btn_copy1"),
                               "BTN_COPY2"      => $objForm->ge("btn_copy2"),
                               "BTN_ADD"        => $objForm->ge("btn_add"),
                               "BTN_UPD"        => $objForm->ge("btn_upd"),
                               "BTN_PREUPD"     => $objForm->ge("btn_preupd"),
                               "BTN_NEXTUPD"    => $objForm->ge("btn_nextupd"),
                               "BTN_DEL"        => $objForm->ge("btn_del"),
                               "BTN_CANCEL"     => $objForm->ge("btn_cancel"),
                               "BTN_CLOSE"      => $objForm->ge("btn_close"),
                               "BTN_CHECK"      => $objForm->ge("btn_check"));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year) );

        $arg["start"]   = $objForm->get_start("main", "POST", "knjl020kindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjl020kForm1.html", $arg); 
    }
}
?>
