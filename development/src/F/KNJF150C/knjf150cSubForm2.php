<?php

require_once('for_php7.php');

class knjf150cSubForm2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //エラーチェック用初期化。各項目作成時にセット
        $model->errorCheck = array();

        //種別区分（外科）
        $model->type ='2';

        //警告メッセージを表示しない場合
        if(($model->cmd == "subform2A") || ($model->cmd == "subform2_clear")){
            if (isset($model->schregno) && !isset($model->warning)){
                $row = $db->getRow(knjf150cQuery::getRow($model), DB_FETCHMODE_ASSOC);
            } else {
                $row =& $model->field;
            }
        } else {
            $row =& $model->field;
        }

        // 宮城県は「処置結果」「連絡」「医療機関」「体温」
        if ($model->schoolName == "miyagiken") {
            $arg["COLSPAN"] = "colspan=\"4\"";
        } else {
            $arg["not_miyagiken"] = "1";
        }

        //extra
        $extra_int = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $extra_flote = " onblur=\"this.value=toFlote(this.value)\"";

        //生徒情報
        $stdInfo = $db->getRow(knjf150cQuery::getHrName($model), DB_FETCHMODE_ASSOC);
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $stdInfo["HR_NAME"].$attendno.'　'.$name;

        //来室日付作成
        $value = ($row["VISIT_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $row["VISIT_DATE"]);
        $arg["data"]["SEQ01_REMARK1"] = View::popUpCalendar($objForm, "SEQ01_REMARK1", $value);

        //来室時間（時）
        $extra = "";
        $value = ($row["VISIT_HOUR"] == "") ? $model->hour : $row["VISIT_HOUR"];
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ01_REMARK2", $value, $extra, 1);

        //来室時間（分）
        $extra = "";
        $value = ($row["VISIT_MINUTE"] == "") ? $model->minute : $row["VISIT_MINUTE"];
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ01_REMARK3", $value, $extra, 1);

        //場合
        $query = knjf150cQuery::getNameMst("F224");
        makeCmb($objForm, $arg, $db, $query, "SEQ01_REMARK4", $row["SEQ01_REMARK4"], "", 1);

        //授業名
        $query = knjf150cQuery::getCreditMst($stdInfo);
        makeCmb($objForm, $arg, $db, $query, "SEQ01_REMARK5", $row["SEQ01_REMARK5"], "", 1);

        //けがの場所コンボ作成
        $query = knjf150cQuery::getNameMst('F206');
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SEQ03_REMARK1", $row["SEQ03_REMARK1"], $extra, 1);

        //来室理由コンボ作成
        $query = knjf150cQuery::getNameMst('F201');
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SEQ02_REMARK1", $row["SEQ02_REMARK1"], $extra, 1);

        //来室理由テキスト
        $extra = "";
        $arg["data"]["SEQ02_REMARK_L1"] = knjCreateTextBox($objForm, $row["SEQ02_REMARK_L1"], "SEQ02_REMARK_L1", 100, 100, $extra);
        $model->errorCheck["SEQ02_REMARK_L1"] = array("LABEL" => "来室理由", "LEN" => 150);

        //処置
        //授業
        $extra = ($row["SEQ08_REMARK1"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK1\"";
        $arg["data"]["SEQ08_REMARK1"] = knjCreateCheckBox($objForm, "SEQ08_REMARK1", "1", $extra, "");

        //洗浄・被覆材
        $extra = ($row["SEQ08_REMARK2"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK2\"";
        $arg["data"]["SEQ08_REMARK2"] = knjCreateCheckBox($objForm, "SEQ08_REMARK2", "1", $extra, "");

        //止血
        $extra = ($row["SEQ08_REMARK3"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK3\"";
        $arg["data"]["SEQ08_REMARK3"] = knjCreateCheckBox($objForm, "SEQ08_REMARK3", "1", $extra, "");

        //固定
        $extra = ($row["SEQ08_REMARK4"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK4\"";
        $arg["data"]["SEQ08_REMARK4"] = knjCreateCheckBox($objForm, "SEQ08_REMARK4", "1", $extra, "");

        //湿布
        $extra = ($row["SEQ08_REMARK5"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK5\"";
        $arg["data"]["SEQ08_REMARK5"] = knjCreateCheckBox($objForm, "SEQ08_REMARK5", "1", $extra, "");

        //ホットパック
        $extra = ($row["SEQ08_REMARK6"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK6\"";
        $arg["data"]["SEQ08_REMARK6"] = knjCreateCheckBox($objForm, "SEQ08_REMARK6", "1", $extra, "");

        //アイシング
        $extra = ($row["SEQ08_REMARK7"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK7\"";
        $arg["data"]["SEQ08_REMARK7"] = knjCreateCheckBox($objForm, "SEQ08_REMARK7", "1", $extra, "");

        //水分補給
        $extra = ($row["SEQ08_REMARK8"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK8\"";
        $arg["data"]["SEQ08_REMARK8"] = knjCreateCheckBox($objForm, "SEQ08_REMARK8", "1", $extra, "");

        //休養
        $extra = ($row["SEQ09_REMARK1"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ09_REMARK1\"";
        $arg["data"]["SEQ09_REMARK1"] = knjCreateCheckBox($objForm, "SEQ09_REMARK1", "1", $extra, "");
        //来室校時
        $query = knjf150cQuery::getNameMstPeriod();
        makeCmb($objForm, $arg, $db, $query, "SEQ09_REMARK2", $row["SEQ09_REMARK2"], "", 1);

        //早退
        $extra = ($row["SEQ09_REMARK3"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ09_REMARK3\"";
        $arg["data"]["SEQ09_REMARK3"] = knjCreateCheckBox($objForm, "SEQ09_REMARK3", "1", $extra, "");
        //時
        $extra = "";
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ09_REMARK4", $row["SEQ09_REMARK4"], $extra, 1);
        //分
        $extra = "";
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ09_REMARK5", $row["SEQ09_REMARK5"], $extra, 1);


        //医療機関
        $extra = ($row["SEQ09_REMARK6"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ09_REMARK6\"";
        $arg["data"]["SEQ09_REMARK6"] = knjCreateCheckBox($objForm, "SEQ09_REMARK6", "1", $extra, "");
        //病院名テキストボックス
        $arg["data"]["SEQ09_REMARK7"] = knjCreateTextBox($objForm, $row["SEQ09_REMARK7"], "SEQ09_REMARK7", 20, 20, "");
        $model->errorCheck["SEQ09_REMARK7"] = array("LABEL" => "病院名", "LEN" => 30);

        //その他
        $extra = ($row["SEQ09_REMARK8"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ09_REMARK8\"";
        $arg["data"]["SEQ09_REMARK8"] = knjCreateCheckBox($objForm, "SEQ09_REMARK8", "1", $extra, "");
        //その他テキストボックス
        $arg["data"]["SEQ09_REMARK9"] = knjCreateTextBox($objForm, $row["SEQ09_REMARK9"], "SEQ09_REMARK9", 20, 20, "");
        $model->errorCheck["SEQ09_REMARK9"] = array("LABEL" => "その他", "LEN" => 20);

        //退出時
        $extra = "";
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ98_REMARK1", $row["SEQ98_REMARK1"], $extra, 1);
        //退出分
        $extra = "";
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ98_REMARK2", $row["SEQ98_REMARK2"], $extra, 1);

        //学校管理下災害
        $extra = ($row["SEQ97_REMARK1"] == "1") ? "checked" : "";
        $extra = ($model->cmd == "subform2") ? "checked" : $extra;
        $extra .= " id=\"SEQ97_REMARK1\"";
        if ($row["SEQ97_REMARK3"] || $row["SEQ97_REMARK4"]) {
            $extra .= " disabled";
            knjCreateHidden($objForm, "SEQ97_REMARK1", $row["SEQ97_REMARK1"]);
            $arg["data"]["SEQ97_REMARK1"] = knjCreateCheckBox($objForm, "", "1", $extra, "");
            knjCreateHidden($objForm, "SEQ97_REMARK3", $row["SEQ97_REMARK3"]);
            knjCreateHidden($objForm, "SEQ97_REMARK4", $row["SEQ97_REMARK4"]);
        } else {
            $arg["data"]["SEQ97_REMARK1"] = knjCreateCheckBox($objForm, "SEQ97_REMARK1", "1", $extra, "");
        }

        //特記事項テキストボックス
        $arg["data"]["SEQ99_REMARK_L1"] = knjCreateTextBox($objForm, $row["SEQ99_REMARK_L1"], "SEQ99_REMARK_L1", 100, 100, "");
        $model->errorCheck["SEQ99_REMARK_L1"] = array("LABEL" => "特記事項", "LEN" => 150);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //file送受信
        updownPDF($objForm, $arg, $model, $row);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knjf150cindex.php", "", "subform2");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf150cSubForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成配列私
function makeCmbArray(&$objForm, &$arg, $dataArray, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
    foreach ($dataArray as $key => $val) {

        $opt[] = array('label' => $val["label"],
                       'value' => $val["value"]);

        if ($value === $val["value"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model)
{
    //登録ボタン
    $update = ($model->cmd == "subform2") ? "insert" : "update";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", "onclick=\"return btn_submit('".$update."');\"");
    $model->isDataAri = false;
    if($model->cmd != "subform2"){
        //取消ボタン
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('subform2_clear');\"");
        $model->isDataAri = true;
    }
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
    //終了ボタン
    if ($model->sendSubmit != "") {
        $link = REQUESTROOT."/F/KNJF150D/knjf150dindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
    } else {
        $extra = "onclick=\"return btn_submit('edit');\"";
    }
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

    //欠課仮登録ボタン
    if($model->Properties["useKnjf150_2Button"] == 1) {
        $extra = "onclick=\"loadwindow('../KNJF150C_2/knjf150c_2index.php?CALL_INFO=KNJF150C_2&SCHREGNO={$model->schregno}',0,0,800,600);return;\"";
        $arg["button"]["btn_knjf150c_2"] = KnjCreateBtn($objForm, "btn_knjf150c_2", "欠課仮登録", $extra);
    }
}

//PDF取込
function updownPDF(&$objForm, &$arg, &$model, $row) {

    //移動後のファイルパス単位
    $setImgHtml = "";
    $setPdfHtml = "";
    if ($model->schregno) {
        //ファイルパス取得
        $model->filePath = knjf150cModel::getFilePath($model->schregno, $model->type, $row);

        if (!is_dir($model->filePath)) {
            //echo "ディレクトリがありません。";
        } else if ($aa = opendir($model->filePath)) {
            $dispPath = str_replace("/usr/local", "", $model->filePath);
            $dispPath = str_replace("/src", "", $dispPath);
            $dataDir = $dispPath . "/";
            $imgCnt = 0;
            while (false !== ($filename = readdir($aa))) {
                $filedir = $dataDir . $filename;
                $info = pathinfo($filedir);
                //JPG
                if (($info["extension"] == "jpg" || $info["extension"] == "JPG")) {
                    if ($imgCnt > 5) {
                        $setImgHtml .= "<br>";
                        $imgCnt = 0;
                    }
                    $setFilename = mb_convert_encoding($filedir,"UTF-8", "SJIS-win");
                    $setImgHtml .= "<a title=\"拡大する\" onclick=\"clickPaste('{$filename}'); window.open('{$setFilename}','','width=665,height=504');";
                    $setImgHtml .= " return false;\" href=\"{$setFilename}\"  target=\"_blank\">";
                    $setImgHtml .= "<img border=\"0\" src=\"{$setFilename}\" width=\"120\" height=\"90\">";
                    $setImgHtml .= "</a>";
                    $imgCnt++;
                }
                //PDF
                if (($info["extension"] == "pdf" || $info["extension"] == "PDF")) {
                    $setFilename = mb_convert_encoding($filedir,"UTF-8", "SJIS-win");
                    $setPdfHtml .= "●<a title=\"拡大する\" onclick=\"clickPaste('{$filename}'); window.open('{$setFilename}','','width=665,height=504');";
                    $setPdfHtml .= " return false;\" href=\"{$setFilename}\"  target=\"_blank\">";
                    $setPdfHtml .= "{$filename}</a>　";
                }
            }
            closedir($aa);
        }
    }
    $setPdfHtml = $imgCnt > 0 ? "<BR><BR>".$setPdfHtml : $setPdfHtml;
    $arg["SET_IMG"] = $setImgHtml.$setPdfHtml."<BR><BR>";

    //textbox
    $extra = "";
    $arg["ZIP_PASS"] = knjCreateTextBox($objForm, $model->zipPass, "ZIP_PASS", 50, 100, $extra);
    //ファイルからの取り込み
    $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 10240000);
    //実行
    if ($model->isDataAri) {
        $extra = ($model->schregno) ? "onclick=\"return btn_submit('execute');\"" : "disabled";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delFile');\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    }
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "TYPE", $model->type);

    //印刷用
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJF150C");
    knjCreateHidden($objForm, "PRINT_VISIT_DATE", $model->visit_date);
    knjCreateHidden($objForm, "PRINT_VISIT_HOUR", $model->visit_hour);
    knjCreateHidden($objForm, "PRINT_VISIT_MINUTE", $model->visit_minute);
}
//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    for ($i = 1; $i <= $count; $i++) {
        if (is_array($extra)) $ext = $extra[$i-1];
        else $ext = $extra;

        $objForm->ae( array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $ext,
                            "multiple"  => $multi));

        $arg["data"][$name.$multi[$i]]  = $objForm->ge($name, $multi[$i]);
    }
}
?>
