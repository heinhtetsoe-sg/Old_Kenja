<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knjl010kForm1
{
    function main($model)
    {
        $objForm = new form;
        $db  = Query::dbCheckOut();

        //試験区分
        $opt_testdiv = array();
        //ADD 2005/12/29 by OCC
        //試験区分に空白行を追加
        if ($model->target == "01" || $model->target == "04" || $model->target == "06"){
            $opt_testdiv[] = array("label" => "",
                                   "value" => "");
        }
        $result = $db->query(knjl010kQuery::getExamName($model->year,"L003"));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_testdiv[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                                   "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTDIV",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $model->testdiv,
                            "options"     => $opt_testdiv));
        $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //受験科目
        $opt_testsubclass = array();
        //受験科目コンボ 可or不可
        $disabled = ($model->target == "08")? "": "disabled";
        $result = $db->query(knjl010kQuery::getExamName($model->year,"L009"));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_testsubclass[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                                        "value" => $row["NAMECD2"]."-".$row["NAME1"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTSUBCLASS",
                            "size"        => "1",
                            "extrahtml"   => $disabled,
                            "value"       => $model->testsubclass,
                            "options"     => $opt_testsubclass));
        $arg["data"]["TESTSUBCLASS"] = $objForm->ge("TESTSUBCLASS");

        Query::dbCheckIn($db);

        //対象年度
        $arg["data"]["YEAR"] = CTRL_YEAR + 1;

        //対象データコンボ
        $opt_target[] = array("label" => " 1：事前相談データ",                  "value" => 1);
        $opt_target[] = array("label" => " 2：志願者(速報)データ",              "value" => 2);
        $opt_target[] = array("label" => " 3：志願者(基礎)データ",              "value" => 3);
        $opt_target[] = array("label" => " 4：志願者(住所)データ",              "value" => 4);
        $opt_target[] = array("label" => " 5：志願者(内申)データ",              "value" => 5);
        $opt_target[] = array("label" => " 6：志願者(附属推薦または中高一貫)",  "value" => 6);
        $opt_target[] = array("label" => " 7：クラブ推薦者データ",              "value" => 7); //追加なし 2006.01.15 alp m-yama
        $opt_target[] = array("label" => " 8：得点データ",                      "value" => 8);
        $opt_target[] = array("label" => " 9：スカラ対象データ",                "value" => 9); //追加なし
        $opt_target[] = array("label" => "10：受験者出身塾データ",              "value" => 10); //追加なし

        $objForm->ae( array("type"        => "select",
                            "name"        => "TARGET",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('change_target')\";",
                            "value"       => $model->target,
                            "options"     => $opt_target));
        $arg["data"]["TARGET"] = $objForm->ge("TARGET");

        //対象ファイル
        $objForm->add_element(array("type"      => "file",
                                    "name"      => "FILE",
                                    "size"      => 2048000,
                                    "extrahtml" => "" ));
        $arg["data"]["FILE"] = $objForm->ge("FILE");

        //ヘッダ有無
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "HEADERCHECK",
                            "value"       => "1",
                            "extrahtml"   => ($model->headercheck == "1")? "checked" : "" ));
        $arg["data"]["HEADERCHECK"] = $objForm->ge("HEADERCHECK");

        //ラジオボタン初期状態と可or不可
        $divide =& $model->field;
        if($model->target == "07" || $model->target == "09" || $model->target == "10"){
            $disabled = "disabled";
            $divide["WHICH_WAY"] = 2;
        }else{
            $disabled = "";
            $divide["WHICH_WAY"] = 1;
        }
        //更新区分ラジオボタン
        $objForm->ae( array("type"        => "radio",
                            "name"        => "WHICH_WAY",
                            "extrahtml"   => $disabled,
                            "value"       => $divide["WHICH_WAY"] ));
        $arg["data"]["WHICH_WAY1"] = $objForm->ge("WHICH_WAY","1");
        $objForm->ae( array("type"        => "radio",
                            "name"        => "WHICH_WAY",
                            "value"       => $divide["WHICH_WAY"] ));
        $arg["data"]["WHICH_WAY2"] = $objForm->ge("WHICH_WAY","2");
        $arg["data"]["WHICH_WAY3"] = $objForm->ge("WHICH_WAY","3");

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ok",
                            "value"       => "実  行",
                            "extrahtml"   => "onclick=\"return btn_submit('execute');\"" ));
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_cancel",
                            "value"       => "終  了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_output",
                            "value"     => "テンプレート書出し",
                            "extrahtml" => "onclick=\"return btn_submit('output');\"" ));

        $arg["button"] = array("BTN_OK"     => $objForm->ge("btn_ok"),
                               "BTN_CLEAR"  => $objForm->ge("btn_cancel"),
                               "BTN_OUTPUT" => $objForm->ge("btn_output"));  
        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd") );

        $arg["start"]   = $objForm->get_start("main", "POST", "knjl010kindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl010kForm1.html", $arg); 
    }
}
?>
