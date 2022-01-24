<?php

require_once('for_php7.php');

class knja128pForm2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form2", "POST", "knja128pindex.php", "", "form2");

        // Add by HPA for textarea_cursor start 2020/01/20
        if ($model->message915 == "") {
            echo "<script>sessionStorage.removeItem(\"KNJA128PForm2_CurrentCursor915\");</script>";
        } else {
            echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJA128PForm2_CurrentCursor915\", x);</script>";
            $model->message915 = "";
        }
        // Add by HPA for textarea_cursor 2020-01-31 end

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //学年の取得
        $query = knja128pQuery::getGradecd($model);
        $model->grade_cd = $db->getOne($query);

        //特別活動の記録出力項目取得
        $query = knja128pQuery::getNameMst($model, "D034");
        $result = $db->query($query);
        $model->itemArray = array();
        while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->itemArray[$setItem["NAMECD2"]] = $setItem;
        }

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knja128pQuery::getBehavior($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $scd = $row["DIV"] .$row["CODE"];
            $Row["RECORD"][$scd] = $row["RECORD"];
        }
        $result->free();

        //特別活動の記録の観点取得
        $row = $db->getRow(knja128pQuery::getTrainRemarkData($model), DB_FETCHMODE_ASSOC);

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
            $row =& $model->field;
        }

        //行動の記録チェックボックス
        $checkboxName1 = array("基本的な生活習慣", "健康・体力の向上", "自主・自律", "責任感", "創意工夫", "思いやり・協力", "生命尊重・自然愛護", "勤労・奉仕", "公正・公平", "公共心・公徳心");
        for ($i=1; $i<11; $i++) {
            $ival = "3" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\"aria-label = \"".$checkboxName1[$i-1]."\"";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録名前表示
        $setData = array();
        foreach ($model->itemArray as $key => $val) {
            $setData["RECORD_NAME".$key] = $val["NAME1"];
        }
        $arg["data"][] = $setData;

        //特別活動の記録チェックボックス
        for ($i=1; $i < get_count($model->itemArray) + 1; $i++) {
            $ival = "4" . sprintf("%02d", $i);
            $count = sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            if ($model->grade_cd == '01') {
                if ($model->itemArray[$count]["ABBV1"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\" aria-label = \"学級活動\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '02') {
                if ($model->itemArray[$count]["ABBV2"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\" aria-label = \"児童会活動\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '03') {
                if ($model->itemArray[$count]["ABBV3"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\" aria-label = \"学校行事\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '04') {
                if ($model->itemArray[$count]["NAMESPARE1"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\" aria-label = \"クラブ活動\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '05') {
                if ($model->itemArray[$count]["NAMESPARE2"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '06') {
                if ($model->itemArray[$count]["NAMESPARE3"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '') {
                $extra = $check1." id=\"RECORD".$ival."\"";
            }
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録の観点
        $arg["SPECIALACTREMARK"] = getTextOrArea($objForm, "SPECIALACTREMARK", $model->specialactremark_moji, $model->specialactremark_gyou, $row["SPECIALACTREMARK"], $model);
        $arg["SPECIALACTREMARK_COMMENT"] = "(全角".$model->specialactremark_moji."文字X".$model->specialactremark_gyou."行まで)";

        //学校種別
        $schoolkind = $db->getOne(knja128pQuery::getSchoolKind($model));

        //更新ボタン
        $extra = (AUTHORITY < DEF_UPDATE_RESTRICT || $schoolkind != 'P') ? "disabled" : " id = \"update2\" onclick=\"current_cursor('update2');return btn_submit('update2')\" aria-label =\"更新\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "id = \"clear2\" onclick=\"current_cursor('clear2');return btn_submit('clear2')\" aria-label =\"取消\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $extra = " id = \"btn_back\" onclick=\"parent.current_cursor_focus();return top.main_frame.right_frame.closeit()\" aria-label =\"戻る\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja128pForm2.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = (int)$gyou % 5;
            $minus = ((int)$gyou / 5) > 1 ? ((int)$gyou / 5) * 6 : 5;
        }
        $height = (int)$gyou * 13.5 + ((int)$gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "aria-label = \"特別活動の記録の観点 全角12文字X10行まで\" id=\"".$name."\" style=\"height:".$height."px;\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, (int)($moji * 2), $moji, $extra);
    }
    return $retArg;
}
