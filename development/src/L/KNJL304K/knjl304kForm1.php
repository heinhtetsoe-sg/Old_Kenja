<?php

require_once('for_php7.php');

/********************************************************************/
/* 氏名又は、氏名かな修正                           山城 2005/09/08 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 : 更新するデータ項目を選択可能にする。     山城 2005/10/28 */
/* NO002 : 氏名○又は、かな氏名を○×○と×○○変更 山城 2005/11/17 */
/* NO003 : 中学も共用する。                         山城 2005/12/30 */
/* NO004 : SQLを修正する。                          山城 2006/01/10 */
/* NO005 : 中高一貫者は除外。                       山城 2006/01/25 */
/* NO006 : 超複写は、頭に●をつける。               山城 2006/01/30 */
/* NO007 : 塾/学校受付番号でリンクしていた箇所を    山城 2006/01/30 */
/*         受付番号でリンクするよう修正                             */
/********************************************************************/

class knjl304kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //試験区分
        $opt = array();
        $result = $db->query(knjl304kQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[]  = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
        }

        if (!strlen($model->testdiv)){
            $model->testdiv = $opt[0]["value"];
        }

        $objForm->ae( array("type"      => "select",
                            "name"      => "TESTDIV",
                            "size"      => "1",
                            "extrahtml" => "Onchange=\"btn_submit('main');\"",
                            "value"     => $model->testdiv,
                            "options"   => $opt));
        $arg["TOP"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //中高判別フラグを作成する  NO003
        $jhflg = 0;
        $row = $db->getOne(knjl304kQuery::GetJorH());
        if ($row == 1){
            $jhflg = 1;
        }else {
            $jhflg = 2;
        }
        $objForm->ae( array("type" => "hidden",
                            "name" => "JHFLG",
                            "value"=> $jhflg ) );

        //リストタイトル用
        $arg["LEFT_TITLE"]  = "志願者データで更新";
        $arg["RIGHT_TITLE"] = "事前相談データで更新";

        //対象データコンボ NO001
        $opt_datacmb = array();
        if ($jhflg == "1"){ //NO003
            $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名－、試験区分－",
                                   "value" => "0");
//          $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名×、試験区分－",
//                                 "value" => "1");
            $opt_datacmb[] = array("label" => "漢字氏名○ OR かな氏名○、試験区分－",
                                   "value" => "1");
            $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名○、試験区分×",
                                   "value" => "2");
        }else {
            //NO004↓
            $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名×、出身学校○",
                                   "value" => "0");
            $opt_datacmb[] = array("label" => "漢字氏名×、かな氏名○、出身学校○",
                                   "value" => "1");
            $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名○、出身学校×",
                                   "value" => "2");
            $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名×、出身学校×",
                                   "value" => "3");
            $opt_datacmb[] = array("label" => "漢字氏名×、かな氏名○、出身学校×",
                                   "value" => "4");
            //NO004↑
            //      $opt_datacmb[] = array("label" => "漢字氏名×、かな氏名×、出身学校○",
            //                             "value" => "4");
        }
        if (!$model->center_title) $model->center_title = $opt_datacmb[0]["value"];

        $objForm->ae( array("type"      => "select",
                            "name"      => "CENTER_TITLE",
                            "size"      => "1",
                            "extrahtml" => "Onchange=\" return btn_submit('main');\"",
                            "value"     => $model->center_title,
                            "options"   => $opt_datacmb));

        $arg["data"]["CENTER_TITLE"] = $objForm->ge("CENTER_TITLE");

        if ($jhflg == "1"){
            $result = $db->query(knjl304kQuery::GetJsList($model));
        }else {
            $result = $db->query(knjl304kQuery::GetHsList($model));
        }

        $opt_right  = array();
        $opt_center = array();
        $opt_left   = array();
        $valueno    = 0;
        $umeji      = "";
        $Repetition = 0;
        $exambef = "*";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($exambef == $row["EXAMNO"]){
                if ($valueno < 10){
                    $umeji = "00";
                }else if($valueno < 100){
                    $umeji = "0";
                }else {
                    $umeji = "";
                }
                //NO006
                $opt_center[$Repetition-1] = array("label" => "●".$umeji.$valueno."(".$row["EXAMNO"]." ".$row["NAME"]." ".$row["NAME_KANA"].")-(重複データあり)",
                                                    "value" => $umeji.$valueno."-".$row["ERROR"]."-".$row["ERROR"]);
            }else {
                $valueno++;
                if ($valueno < 10){
                    $umeji = "00";
                }else if($valueno < 100){
                    $umeji = "0";
                }else {
                    $umeji = "";
                }
                if ($jhflg == "1"){ //NO003
                    //NO006
                    $opt_center[$Repetition] = array("label" => "　".$umeji.$valueno."(".$row["EXAMNO"]." ".$row["NAME"]." ".$row["NAME_KANA"].")-(".$row["ACCEPTNO"]." ".$row["NAME2"]." ".$row["KANA2"].")",
                                                     "value" => $umeji.$valueno."-".$row["EXAMNO"]."-".$row["TESTDIV1"]."-".$row["ACCEPTNO"]."-".$row["TESTDIV2"]);
                }else {
                    //NO006
                    $opt_center[$Repetition] = array("label" => "　".$umeji.$valueno."(".$row["EXAMNO"]." ".$row["NAME"]." ".$row["NAME_KANA"].")-(".$row["ACCEPTNO"]." ".$row["NAME2"]." ".$row["KANA2"].")",
                                                     "value" => $umeji.$valueno."-".$row["EXAMNO"]."-".$row["ACCEPTNO"]);
                }
                $Repetition++;
            }
            $exambef = $row["EXAMNO"];
        }
        //志願者データ
        $objForm->ae( array("type"      => "select",
                            "name"      => "LEFTLIST",
                            "size"      => "10",
                            "value"     => "left",
                            "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','LEFTLIST','CENTERLIST',1);\"",
                            "options"    => $opt_left));

        //対象者
        $objForm->ae( array("type"      => "select",
                            "name"      => "CENTERLIST",
                            "size"      => "10",
                            "value"     => "left",
                            "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                            "options"    => $opt_center));

        //事前相談データ
        $objForm->ae( array("type"      => "select",
                            "name"      => "RIGHTLIST",
                            "size"      => "10",
                            "value"     => "right",
                            "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','CENTERLIST','RIGHTLIST',1);\"",
                            "options"    => $opt_right));

        $result->free();
        Query::dbCheckIn($db);

        //追加ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "sel_add_all",
                            "value"     => "∧",
                            "extrahtml" => "onclick=\"return move3('sel_add_all','LEFTLIST','CENTERLIST',1);\"" ) );

        $objForm->ae( array("type"      => "button",
                            "name"      => "sel_add",
                            "value"     => "↑",
                            "extrahtml" => "onclick=\"return move3('left','LEFTLIST','CENTERLIST',1);\"" ) );

        $objForm->ae( array("type"      => "button",
                            "name"      => "sel_del",
                            "value"     => "↓",
                            "extrahtml" => "onclick=\"return move3('right','LEFTLIST','CENTERLIST',1);\"" ) );

        $objForm->ae( array("type"      => "button",
                            "name"      => "sel_del_all",
                            "value"     => "∨",
                            "extrahtml" => "onclick=\"return move3('sel_del_all','LEFTLIST','CENTERLIST',1);\"" ) );

        $objForm->ae( array("type"      => "button",
                            "name"      => "sel_add_all2",
                            "value"     => "∧",
                            "extrahtml" => "onclick=\"return move3('sel_add_all','CENTERLIST','RIGHTLIST',1);\"" ) );

        $objForm->ae( array("type"      => "button",
                            "name"      => "sel_add2",
                            "value"     => "↑",
                            "extrahtml" => "onclick=\"return move3('left','CENTERLIST','RIGHTLIST',1);\"" ) );

        $objForm->ae( array("type"      => "button",
                            "name"      => "sel_del2",
                            "value"     => "↓",
                            "extrahtml" => "onclick=\"return move3('right','CENTERLIST','RIGHTLIST',1);\"" ) );

        $objForm->ae( array("type"      => "button",
                            "name"      => "sel_del_all2",
                            "value"     => "∨",
                            "extrahtml" => "onclick=\"return move3('sel_del_all','CENTERLIST','RIGHTLIST',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"  => $objForm->ge("LEFTLIST"),
                                    "CENTER_PART"   => $objForm->ge("CENTERLIST"),
                                    "RIGHT_PART"    => $objForm->ge("RIGHTLIST"),
                                    "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                    "SEL_ADD"    => $objForm->ge("sel_add"),
                                    "SEL_DEL"    => $objForm->ge("sel_del"),
                                    "SEL_DEL_ALL" => $objForm->ge("sel_del_all"),
                                    "SEL_ADD_ALL2" => $objForm->ge("sel_add_all2"),
                                    "SEL_ADD2"   => $objForm->ge("sel_add2"),
                                    "SEL_DEL2"   => $objForm->ge("sel_del2"),
                                    "SEL_DEL_ALL2" => $objForm->ge("sel_del_all2")
                                    
                                    );

        //CSV用
        $objForm->ae( array("type"      => "file",
                            "name"      => "csvfile",
                            "size"      => "409600") );

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_csv",
                            "value"     => "実 行",
                            "extrahtml" => "onclick=\"return btn_submit('csv');\"" ) );

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "chk_header",
                            "extrahtml" => "checked",
                            "value"     => "1" ) );
        $arg["CSV_ITEM"] = $objForm->ge("csvfile").$objForm->ge("btn_csv").$objForm->ge("chk_header");

        //保存ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_keep",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return doSubmit();\"" ) );

        //取消ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_clear",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('clear');\"" ) );

        //終了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_OK"  =>$objForm->ge("btn_keep"),
                                "BTN_CLEAR" =>$objForm->ge("btn_clear"),
                                "BTN_END"   =>$objForm->ge("btn_end")); 

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );    

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );    

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata2"
                            ) );    

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata3"
                            ) );    
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl304kindex.php", "", "sel");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl304kForm1.html", $arg); 
    }
}
?>
