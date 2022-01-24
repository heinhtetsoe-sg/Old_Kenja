<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl050jForm1
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

        //入試区分
        $opt = array();
        $result = $db->query(knjl050jQuery::GetName("L004",$model->ObjYear));
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

        //受験型毎の受験科目コードを配列にセット
        $testsubclass = array();
        if ($model->testdiv == "1") $testsubclass = array("2","3","4","5");
        if ($model->testdiv == "2") $testsubclass = array("2","3");
        if ($model->testdiv == "3") $testsubclass = array("2","3","4","5");
        if ($model->testdiv == "4") $testsubclass = array("2","3","4","5");
        if ($model->testdiv == "5") $testsubclass = array("1","2","3");
        if ($model->testdiv == "6") $testsubclass = array("2","3","4","5");

        //受験科目
        $opt = array();
        $result = $db->query(knjl050jQuery::GetName("L009",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //受験型毎の受験科目
            if (in_array($row["NAMECD2"], $testsubclass)) {
                $opt[]    = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
                if (!strlen($model->testsubclasscd) || !in_array($model->testsubclasscd, $testsubclass)) {
                    $model->testsubclasscd = $row["NAMECD2"];
                }
                if ($model->testsubclasscd == $row["NAMECD2"]) $divname["TESTSUBCLASSCD"] = $row["NAME1"];
            }
        }


        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTSUBCLASSCD",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"  tabindex=-1",
                            "value"      => $model->testsubclasscd,
                            "options"    => $opt));
        $arg["TOP"]["TESTSUBCLASSCD"] = $objForm->ge("TESTSUBCLASSCD");

        //受験科目毎の満点
        $perfect = ($model->testsubclasscd < 4) ? 100 : 50;

        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."入試_得点データ.csv");

        //CSVヘッダ名
        $objUp->setHeader(array("入試年度",
                                "入試制度コード",
                                "入試制度名",
                                "入試区分コード",
                                "入試区分名",
                                "受験型",
                                "受験型名",
                                "受験科目",
                                "受験科目名",
                                "座席番号",
                                "受験番号",
                                "氏名",
                                "氏名かな",
                                "性別コード",
                                "性別",
                                "得点"));

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
                            "extrahtml"   => "onClick=\"btn_submit('read');\" tabindex=-1" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => " << ",
                            "extrahtml"   => "onClick=\"btn_submit('back');\" tabindex=-1" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => " >> ",
                            "extrahtml"   => "onClick=\"btn_submit('next');\" tabindex=-1" ) );

        $arg["TOP"]["RECEPTNO"] = $objForm->ge("RECEPTNO");
        $arg["TOP"]["button"]   = $objForm->ge("btn_read")."　　".$objForm->ge("btn_back").$objForm->ge("btn_next");

        //一覧表示
        $arr_receptno = array();
        $model->applicantdiv = $model->exam_type = array();
        //更新日付チェック用
        if (!$model->isWarning()) {
            $model->scoreUpdated  = array();
            $model->remarkUpdated = array();
        }
        if ($model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next")
        {
            //データ取得
            $result    = $db->query(knjl050jQuery::SelectQuery($model));

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303","\\n座席番号登録が行われていないか、志願者数確定処理が行われていません。");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                 array_walk($row, "htmlspecialchars_array");

                //書き出し用CSVデータ
                $csv = array($model->ObjYear,
                            $row["APPLICANTDIV"],
                            $row["APPLICANTDIV_NAME"],
                            $model->testdiv,
                            $divname["TESTDIV"],
                            $row["EXAM_TYPE"],
                            $row["EXAM_TYPE_NAME"],
                            $model->testsubclasscd,
                            $divname["TESTSUBCLASSCD"],
                            $row["RECEPTNO"],
                            $row["EXAMNO"],
                            $row["NAME"],
                            $row["NAME_KANA"],
                            $row["SEXCD"],
                            $row["SEX"],
                            $row["SCORE"]);
                $objUp->addCsvValue($csv);

                //CSV取り込み（この7つのキー値と同じレコードのみ取り込み）
                $key = array("入試年度"       => $model->ObjYear,
                             "入試制度コード" => $row["APPLICANTDIV"],
                             "入試区分コード" => $model->testdiv,
                             "受験型"         => $row["EXAM_TYPE"],
                             "受験科目"       => $model->testsubclasscd,
                             "座席番号"       => $row["RECEPTNO"],
                             "受験番号"       => $row["EXAMNO"]);

                //入力エリアとキーをセットする
                $objUp->setElementsValue("SCORE[]", "得点", $key);

                //ゼロ埋めフラグ
               $flg = array("入試年度"       => array(false,4),
                            "入試制度コード" => array(false,1),
                            "入試区分コード" => array(false,1),
                            "受験型"         => array(false,1),
                            "受験科目"       => array(false,1),
                            "座席番号"       => array(true,4),
                            "受験番号"       => array(true,5));

                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(15 => 'N'));
                $objUp->setSize(array(15 => 3));

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];
                $model->applicantdiv[$row["RECEPTNO"]] = $row["APPLICANTDIV"];
                $model->exam_type[$row["RECEPTNO"]] = $row["EXAM_TYPE"];
                if (!$model->isWarning()) {
                    $model->scoreUpdated[$row["RECEPTNO"]]  = $row["SCORE_UPDATED"];
                    $model->remarkUpdated[$row["RECEPTNO"]] = $row["REMARK_UPDATED"];
                }

                //満点チェック用
                $perfect = $row["PERFECT"];
                $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$perfect);

                $objForm->ae( array("type"        => "text",
                                    "name"        => "SCORE",
                                    "extrahtml"   => "  OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this);\"",
                                    "maxlength"   => "3",
                                    "size"        => "3",
                                    "multiple"    => "1",
                                    "value"       => ($model->isWarning() ? $model->score[$row["RECEPTNO"]] : $row["SCORE"])));
                $row["SCORE"] = $objForm->ge("SCORE");

                $objForm->ae( array("type"        => "text",
                                    "name"        => "REMARK",
                                    "extrahtml"   => "  OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\"",
                                    "maxlength"   => "40",
                                    "size"        => "40",
                                    "multiple"    => "1",
                                    "value"       => ($model->isWarning() ? $model->remark[$row["RECEPTNO"]] : $row["REMARK"])));
                $row["REMARK"] = $objForm->ge("REMARK");

                $arg["data"][] = $row;
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
                            "name"      => "HID_RECEPTNO",
                            "value"     => implode(",",$arr_receptno)) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_TESTDIV",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_TESTSUBCLASSCD",
                            "value"     => "") );
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl050jindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl050jForm1.html", $arg); 
    }
}
?>
