<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl071cForm1
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

        //入試制度
        $opt = array();
        $result = $db->query(knjl071cQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->applicantdiv=="" && $row["NAMESPARE2"]=='1') $model->applicantdiv = $row["NAMECD2"];
            if ($model->applicantdiv == $row["NAMECD2"]) $divname["APPLICANTDIV"] = $row["NAME1"];
        }
        $extra = "Onchange=\"btn_submit('main');\" tabindex=-1";
        $arg["TOP"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->applicantdiv, $opt, $extra, 1);

        //五條中学は、順位の次に「専願」欄を追加し、専願なら○表示
        if ($model->isGojou && $model->applicantdiv == "1" || $model->isCollege && $model->applicantdiv == "2") {
            $arg["isGojou"] = "1";
        }

        //カレッジ中学は、専願の次に志望コース欄を追加
        if ($model->isCollege) {
            $arg["isCollege"] = "1";
        }

        //入試区分
        $opt = array();
        $result = $db->query(knjl071cQuery::GetName("L004",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
            if ($model->testdiv == $row["NAMECD2"]) $divname["TESTDIV"] = $row["NAME1"];
        }
        $extra = "Onchange=\"btn_submit('main');\" tabindex=-1";
        $arg["TOP"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->testdiv, $opt, $extra, 1);

        //カレッジ中学A日程の場合、総合得点ではなく得点率を表示
        if ($model->isCollege && $model->applicantdiv == "1" && $model->testdiv == "1") {
            $arg["TOP"]["TOTAL_NAME"] = "得点率";
        } else {
            $arg["TOP"]["TOTAL_NAME"] = "総合得点";
        }

        //和歌山中学後期の場合、「前期判定」欄を表示
        if (!$model->isGojou && $model->applicantdiv == "1" && $model->testdiv == "2") {
            $arg["isWakaZenkiHantei"] = "1";
        }

        //五條中学は、「専願」コンボをグレーアウトにする
        $disShdiv = ($model->isGojou && $model->applicantdiv == "1" || $model->isCollege && $model->applicantdiv == "2") ? " disabled" : "";

        //専併区分
        $opt = array();
        $result = $db->query(knjl071cQuery::GetName("L006",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
        }
        $extra = "Onchange=\"btn_submit('main');\" tabindex=-1" .$disShdiv;
        $arg["TOP"]["SHDIV"] = knjCreateCombo($objForm, "SHDIV", $model->shdiv, $opt, $extra, 1);

        //判定名
        //JAVASCRIPTで変更時にラベル表示する用。
        $arrJudgeName = array();
        $judgediv_name = $seq = "";
        $result = $db->query(knjl071cQuery::GetName("L013",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $arrJudgeName[$row["NAMECD2"]] = $row["NAME1"];
            $arg["data2"][] = array("judgediv_cd" => $row["NAMECD2"], "judgediv_name" => $row["NAME1"]);
            $judgediv_name .= $seq .$row["NAMECD2"].":".$row["ABBV1"];
            $seq = ",";
        }
        $arg["TOP"]["JUDGE"] = $judgediv_name;

        //受験番号自
        $extra = "onblur=\"this.value=toInteger(this.value);\" tabindex=-1";
        $arg["TOP"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 5, 4, $extra);

        //受験番号至
        $arg["TOP"]["END_EXAMNO"] = (strlen($model->examno) ? $model->e_examno : "     ");

        //表示順序ラジオボタン 1:受験番号順で表示 2:成績順で表示
        $opt_sort = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, "", $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //読込みボタン
        $extra = "onClick=\"btn_submit('read');\" tabindex=-1";
        $arg["TOP"]["btn_read"] = knjCreateBtn($objForm, 'btn_read', "読込み", $extra);

        //<<ボタン
        $extra = "onClick=\"btn_submit('back');\" tabindex=-1";
        $arg["TOP"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', " << ", $extra);

        //>>ボタン
        $extra = "onClick=\"btn_submit('next');\" tabindex=-1";
        $arg["TOP"]["btn_next"] = knjCreateBtn($objForm, 'btn_next', " >> ", $extra);

        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."入試_合否データ.csv");

        //CSVヘッダ名
        //カレッジ中学は、順位の次に「専願」欄を追加し、専願なら○表示
        //カレッジ中学は、専願の次に志望コース欄を追加
        //カレッジ中学A日程の場合、総合得点ではなく得点率を表示
        if ($model->isCollege && $model->applicantdiv == "1" && $model->testdiv == "1") {
            $csvhead = array("入試年度",
                             "入試制度コード",
                             "入試制度名",
                             "入試区分コード",
                             "入試区分名",
                             "座席番号",
                             "受験番号",
                             "得点率",
                             "順位",
                             "専願",
                             "志望コース",
                             "判定コード",
                             "判定名");
        } else if ($model->isCollege) {
            $csvhead = array("入試年度",
                             "入試制度コード",
                             "入試制度名",
                             "入試区分コード",
                             "入試区分名",
                             "座席番号",
                             "受験番号",
                             "総合得点",
                             "順位",
                             "専願",
                             "志望コース",
                             "判定コード",
                             "判定名");
        //五條中学は、順位の次に「専願」欄を追加し、専願なら○表示
        } else if ($model->isGojou && $model->applicantdiv == "1") {
            $csvhead = array("入試年度",
                             "入試制度コード",
                             "入試制度名",
                             "入試区分コード",
                             "入試区分名",
                             "座席番号",
                             "受験番号",
                             "総合得点",
                             "順位",
                             "専願",
                             "判定コード",
                             "判定名");
        } else {
            $csvhead = array("入試年度",
                             "入試制度コード",
                             "入試制度名",
                             "入試区分コード",
                             "入試区分名",
                             "座席番号",
                             "受験番号",
                             "総合得点",
                             "順位",
                             "判定コード",
                             "判定名");
        }
        $objUp->setHeader($csvhead);

        //一覧表示
        $tmp_examno = array();
        if ($model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next")
        {
            if (!$model->isWarning()) $model->score = array();

            $query  = knjl071cQuery::SelectQuery($model);
            $result = $db->query($query);

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                //表示される受験番号を保持
                $tmp_examno[] = $row["EXAMNO"];

                //書き出し用CSVデータ
                //カレッジ中学は、順位の次に「専願」欄を追加し、専願なら○表示
                //カレッジ中学は、専願の次に志望コース欄を追加
                if ($model->isCollege) {
                    $row["SHDIV_MARK"] = "";
                    if ($row["SHDIV"] == "1") $row["SHDIV_MARK"] = "○";
                    if ($row["SHDIV"] == "6") $row["SHDIV_MARK"] = "○";
                    if ($row["SHDIV"] == "7") $row["SHDIV_MARK"] = "○";
                    if ($row["SHDIV"] == "8") $row["SHDIV_MARK"] = "○";
                    if ($row["SHDIV"] == "3") $row["SHDIV_MARK"] = "○";
                    if ($row["SHDIV"] == "4") $row["SHDIV_MARK"] = "○";
                    if ($row["SHDIV"] == "5") $row["SHDIV_MARK"] = "○";
                    $row["SHDIV_COURSE"] = "";
                    if ($row["SHDIV"] == "6" || $row["SHDIV"] == "9") $row["SHDIV_COURSE"] = "G";
                    if ($row["SHDIV"] == "7" || $row["SHDIV"] == "A") $row["SHDIV_COURSE"] = "S";
                    if ($row["SHDIV"] == "8" || $row["SHDIV"] == "B") $row["SHDIV_COURSE"] = "S/G";
                    if ($row["SHDIV"] == "3") $row["SHDIV_COURSE"] = "EA";
                    if ($row["SHDIV"] == "4") $row["SHDIV_COURSE"] = "ES";
                    if ($row["SHDIV"] == "5") $row["SHDIV_COURSE"] = "EA/ES";
                    $csv = array($model->ObjYear,
                                 $model->applicantdiv,
                                 $divname["APPLICANTDIV"],
                                 $model->testdiv,
                                 $divname["TESTDIV"],
                                 $row["RECEPTNO"],
                                 $row["EXAMNO"],
                                 $row["TOTAL4"],
                                 $row["TOTAL_RANK4"],
                                 $row["SHDIV_MARK"],
                                 $row["SHDIV_COURSE"],
                                 $row["JUDGEDIV"],
                                 $row["JUDGEDIV_ABBV"]);
                //五條中学は、順位の次に「専願」欄を追加し、専願なら○表示
                } else if ($model->isGojou && $model->applicantdiv == "1") {
                    $row["SHDIV_MARK"] = "";
                    if ($row["SHDIV"] == "1") $row["SHDIV_MARK"] = "○";
                    if ($row["SHDIV"] == "6") $row["SHDIV_MARK"] = "Ⅰ";
                    if ($row["SHDIV"] == "7") $row["SHDIV_MARK"] = "Ⅱ";
                    if ($row["SHDIV"] == "8") $row["SHDIV_MARK"] = "Ⅲ";
                    $csv = array($model->ObjYear,
                                 $model->applicantdiv,
                                 $divname["APPLICANTDIV"],
                                 $model->testdiv,
                                 $divname["TESTDIV"],
                                 $row["RECEPTNO"],
                                 $row["EXAMNO"],
                                 $row["TOTAL4"],
                                 $row["TOTAL_RANK4"],
                                 $row["SHDIV_MARK"],
                                 $row["JUDGEDIV"],
                                 $row["JUDGEDIV_ABBV"]);
                } else {
                    $csv = array($model->ObjYear,
                                 $model->applicantdiv,
                                 $divname["APPLICANTDIV"],
                                 $model->testdiv,
                                 $divname["TESTDIV"],
                                 $row["RECEPTNO"],
                                 $row["EXAMNO"],
                                 $row["TOTAL4"],
                                 $row["TOTAL_RANK4"],
                                 $row["JUDGEDIV"],
                                 $row["JUDGEDIV_ABBV"]);
                }
                $objUp->addCsvValue($csv);

                //CSV取り込み（この４つのキー値と同じレコードのみ取り込み）
                $key = array("入試年度"       => $model->ObjYear,
                             "入試制度コード" => $model->applicantdiv,
                             "入試区分コード" => $model->testdiv,
                             "受験番号"       => $row["EXAMNO"]);

                //ゼロ埋めフラグ
                $flg = array("入試年度"       => array(false,4),
                             "入試制度コード" => array(false,1),
                             "入試区分コード" => array(false,1),
                             "受験番号"       => array(true,4));

                $objUp->setEmbed_flg($flg);
                if ($model->isCollege) {
                    $objUp->setType(array(11=>'S'));
                    $objUp->setSize(array(11=>1));
                } else if ($model->isGojou && $model->applicantdiv == "1") {
                    $objUp->setType(array(10=>'S'));
                    $objUp->setSize(array(10=>1));
                } else {
                    $objUp->setType(array(9=>'S'));
                    $objUp->setSize(array(9=>1));
                }


                //判定コード
                $objForm->ae( array("type"        => "text",
                                    "name"        => "JUDGEDIV",
                                    "extrahtml"   => " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:right;\" onblur=\"this.value = toAlphaNumber(this.value);setName(this,".$row["EXAMNO"].",'0');\"",
                                    "maxlength"   => "1",
                                    "size"        => "2",
                                    "multiple"    => "1",
                                    "value"       => ($model->isWarning() ? $model->score[$row["EXAMNO"]]["JUDGEDIV"] : $row["JUDGEDIV"])));
                $row["JUDGEDIV"] = $objForm->ge("JUDGEDIV");
                $objUp->setElementsValue("JUDGEDIV[]","判定コード", $key);

                //innerHTML用ID
                $row["JUDGEDIV_ID"] = "JUDGEDIV_NAME" .$row["EXAMNO"];

                if ($model->isWarning()) $row["JUDGEDIV_NAME"] = $arrJudgeName[$model->score[$row["EXAMNO"]]["JUDGEDIV"]];

                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //ボタン作成
        $extra = "onClick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        $extra = "onClick=\"btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        makeHidden($objForm, $tmp_examno);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl071cindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl071cForm1.html", $arg); 
    }
}


//hidden作成
function makeHidden(&$objForm, $tmp_examno) {
    knjCreateHidden($objForm, "cmd", "");
    knjCreateHidden($objForm, "HID_APPLICANTDIV", "");
    knjCreateHidden($objForm, "HID_TESTDIV", "");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$tmp_examno));
}


?>
