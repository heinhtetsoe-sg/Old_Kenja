<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl040cForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //オブジェクト作成
        $objUp = new csvFile();
        $db           = Query::dbCheckOut();
        $divname = array();  //CSV書き出し時のコード名称をセット
        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //試験制度
        $opt = array();
        $result = $db->query(knjl040cQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->applicantdiv=="" && $row["NAMESPARE2"]=='1') $model->applicantdiv = $row["NAMECD2"];
            if ($model->applicantdiv == $row["NAMECD2"]) $divname["APPLICANTDIV"] = $row["NAME1"];
        }
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLICANTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\" tabindex=-1",
                            "value"      => $model->applicantdiv,
                            "options"    => $opt));
        $arg["TOP"]["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");

        //入試区分
        $opt = array();
        $result = $db->query(knjl040cQuery::GetName("L004",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[]    = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
            if ($model->testdiv == $row["NAMECD2"]) $divname["TESTDIV"] = $row["NAME1"];
        }

        if (!strlen($model->testdiv)) {
            $model->testdiv = $opt[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"  tabindex=-1",
                            "value"      => $model->testdiv,
                            "options"    => $opt));
        $arg["TOP"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."入試_受付データ.csv");

        //CSVヘッダ名
        $objUp->setHeader(array("入試年度",
                                "入試制度コード",
                                "入試制度名",
                                "入試区分コード",
                                "入試区分名",
                                "座席番号",
                                "受験番号",
                                "氏名",
                                "氏名かな",
                                "性別コード",
                                "性別"));

        //座席番号自
        $objForm->ae( array("type"        => "text",
                            "name"        => "RECEPTNO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"  tabindex=-1",
                            "value"       => $model->receptno));

        //座席番号至
        $arg["TOP"]["END_RECEPTNO"] = (strlen($model->receptno) ? $model->e_receptno : "     ");
        
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_read",
                            "value"       => "読込み",
                            "extrahtml"   => "onClick=\"btn_submit('read');\"  tabindex=-1" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => " << ",
                            "extrahtml"   => "onClick=\"btn_submit('back');\"  tabindex=-1" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => " >> ",
                            "extrahtml"   => "onClick=\"btn_submit('next');\"  tabindex=-1" ) );

        $arg["TOP"]["RECEPTNO"] = $objForm->ge("RECEPTNO");
        $arg["TOP"]["button"]   = $objForm->ge("btn_read")."　　".$objForm->ge("btn_back").$objForm->ge("btn_next");
        
        //試験会場（デフォルト表示）
        $hall_name = array();
        $result = $db->query(knjl040cQuery::getExamHall($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $hall_name[] = array("S_RECEPTNO"    => $row["S_RECEPTNO"],
                                 "E_RECEPTNO"    => $row["E_RECEPTNO"],
                                 "EXAMHALL_NAME" => $row["EXAMHALL_NAME"]);
        }

        //一覧表示(40行必ず作成する)
        if ($model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next")
        {
            for ($i = 0; $i < 40; $i++)
            {
                //座席番号を連番で作成
                $idx = sprintf("%04d",(int)$model->receptno + $i);

                //終了座席番号以降は作成しない(9999以降に対応)
                if ($idx > $model->e_receptno) break;

                $row["RECEPTNO"] = $idx;
                $row["BGCOLOR"]  ="#ccffcc";

                //CSV取り込み（この5つのキー値と同じレコードのみ取り込み）
                $key = array("入試年度"       => $model->ObjYear,
                             "入試制度コード" => $model->applicantdiv,
                             "入試区分コード" => $model->testdiv,
                             "座席番号"       => $idx);

                //入力エリアとキーをセットする
                $objUp->setElementsValue("EXAMNO[]", "受験番号", $key);

                //ゼロ埋めフラグ
               $flg = array("入試年度"       => array(false,4),
                            "入試制度コード" => array(false,1),
                            "入試区分コード" => array(false,1),
                            "座席番号"       => array(true,4),
                            "受験番号"       => array(true,4));

                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(6 => 'N'));
                $objUp->setSize(array(6 => 4));
                
                $objForm->ae( array("type"        => "text",
                                    "name"        => "EXAMNO",
                                    "extrahtml"   => "style=\"background-color:#ccffcc\" onblur=\"this.value=toInteger(this.value);\" OnChange=\"setName(this,".(int)$idx.",0);\"",
                                    "maxlength"   => "4",
                                    "size"        => "5",
                                    "value"       => ($model->isWarning() ? $model->examno[$i] : ""),
                                    "multiple"    => "1"));
                $row["EXAMNO"] = $objForm->ge("EXAMNO");

                $temp[$idx] = $row;
            }

            //データ取得
            $result    = $db->query(knjl040cQuery::SelectQuery($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                 array_walk($row, "htmlspecialchars_array");

                //書き出し用CSVデータ
                $csv = array($model->ObjYear,
                            $model->applicantdiv,
                            $divname["APPLICANTDIV"],
                            $model->testdiv,
                            $divname["TESTDIV"],
                            $row["RECEPTNO"],
                            $row["EXAMNO"],
                            $row["NAME"],
                            $row["NAME_KANA"],
                            $row["SEXCD"],
                            $row["SEX"]);
                $objUp->addCsvValue($csv);

                $row["BGCOLOR"] = "#ffffff";
                
                $objForm->ae( array("type"        => "text",
                                    "name"        => "EXAMNO",
                                    "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\" OnChange=\"setName(this,".(int)$row["RECEPTNO"].",1);\"",
                                    "maxlength"   => "4",
                                    "size"        => "5",
                                    "multiple"    => "1",
                                    "value"       => $row["EXAMNO"]));
                $row["EXAMNO"] = $objForm->ge("EXAMNO");

                //データがある座席番号の配列を上書きする
                $temp[$row["RECEPTNO"]] = $row;
            }

            //HTML出力用に配置し直す
            foreach ($temp as $val)
            {
                //試験会場
                $val["EXAMHALL_NAME"] = "";
                foreach ($hall_name as $v)
                {
                    if ((int)$val["RECEPTNO"] >= (int)$v["S_RECEPTNO"] && (int)$val["RECEPTNO"] <= (int)$v["E_RECEPTNO"]) {
                        $val["EXAMHALL_NAME"] = $v["EXAMHALL_NAME"];
                    }
                }
                //innerHTML用ID
                $val["ROWID"]  = "ROWID".(int)$val["RECEPTNO"];
                $val["NAMEID"]      = "NAMEID".(int)$val["RECEPTNO"];
                $val["NAME_KANAID"] = "NAME_KANAID".(int)$val["RECEPTNO"];
                $val["SEXID"]       = "SEXID".(int)$val["RECEPTNO"];

                $arg["data"][] = $val;
            }
        } 

        Query::dbCheckIn($db);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onClick=\"btn_submit('update');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onClick=\"btn_submit('reset');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        
        $arg["btn_update"] = $objForm->ge("btn_update");
        $arg["btn_reset"]  = $objForm->ge("btn_reset");
        $arg["btn_end"]    = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_APPLICANTDIV",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_TESTDIV",
                            "value"     => "") );

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl040cindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl040cForm1.html", $arg); 
    }
}
?>
