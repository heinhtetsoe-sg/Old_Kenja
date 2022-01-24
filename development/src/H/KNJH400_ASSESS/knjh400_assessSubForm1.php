<?php

require_once('for_php7.php');

class knjh400_assessSubForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjh400_assessindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $model->hrname.$attendno.'　'.$name;
        
        //年度切替
        $arg["data"]["YEAR"] = $model->year."年度";
        $backYear = $model->year - 1;
        $nextYear = $model->year + 1;
        //その年度の最大のSEMESTERを取得したい
        $query = knjh400_assessQuery::getSemester($model->schregno, $backYear, $nextYear);
        $result = $db->query($query);
        $semes = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $semes[$row["YEAR"]] = $row["SEMESTER"];
        }
        //前年
        $query = knjh400_assessQuery::getDefYearData($model->schregno, $backYear, "ASSESSMENT_ANS_DAT");
        $cnt = $db->getOne($query);
        if($cnt > 0){
            $arg["data"]["BACK"] = "<a href=\"knjh400_assessindex.php?cmd=edit&SCHREGNO=".$model->schregno."&EXP_YEAR=".$backYear."&EXP_SEMESTER=".$semes[$backYear]."\" target=\"_self\"><<前年</a>";
        }
        //次年
        $query = knjh400_assessQuery::getDefYearData($model->schregno, $nextYear, "ASSESSMENT_ANS_DAT");
        $cnt = $db->getOne($query);
        if($cnt > 0){
            $arg["data"]["NEXT"] = "<a href=\"knjh400_assessindex.php?cmd=edit&SCHREGNO=".$model->schregno."&EXP_YEAR=".$nextYear."&EXP_SEMESTER=".$semes[$nextYear]."\" target=\"_self\">次年>></a>";
        }

        //アセスメント情報
        $model->assess = array();
        $model->assess["01"] = array("check" => 1, "moji" => 25, "gyo" => 1,  "title" => "支援が必要な項目");
        $model->assess["02"] = array("check" => 1, "moji" => 25, "gyo" => 12, "title" => "学習面");
        $model->assess["03"] = array("check" => 1, "moji" => 25, "gyo" => 7,  "title" => "生活・行動面");
        $model->assess["04"] = array("check" => 1, "moji" => 25, "gyo" => 12, "title" => "社会性・対人関係");
        $model->assess["05"] = array("check" => 0, "moji" => 50, "gyo" => 5,  "title" => "本人の良さ・得意なことを往かした支援の経過");
        $model->assess["06"] = array("check" => 0, "moji" => 50, "gyo" => 5,  "title" => "本人が目指す自己像");
        $model->assess["07"] = array("check" => 0, "moji" => 50, "gyo" => 5,  "title" => "本人が困っている事・解決したい事");
        $model->assess["08"] = array("check" => 0, "moji" => 50, "gyo" => 5,  "title" => "本人が自分で努力できること、しようとしていること");
        $model->assess["09"] = array("check" => 0, "moji" => 50, "gyo" => 5,  "title" => "保護者の希望");
        $model->assess["10"] = array("check" => 0, "moji" => 50, "gyo" => 5,  "title" => "進路について");

        //支援が必要な項目一覧
        $list = array();
        $list[1]  = "進級";
        $list[2]  = "学習";
        $list[3]  = "提出物"  ;
        $list[4]  = "集団参加";
        $list[5]  = "遅刻";
        $list[6]  = "欠課";
        $list[7]  = "登校";
        $list[8]  = "対人関係・社会性";
        $list[9]  = "コミュニケーション";
        $list[10] = "その他";

        foreach ($model->assess as $key => $val) {
            //項目名
            $arg["data"][$key."_TITLE"] = $val["title"];

            if (isset($model->schregno) && !isset($model->warning)) {
                //アセスメント解答データ取得
                $query = knjh400_assessQuery::getAssessmentAnsDat($model, $key);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //設問
                    for ($i=1; $i <= 15; $i++) {
                        $answer[$key."_QUESTION".$i] = $row["QUESTION".$i];
                    }
                    //備考
                    $answer[$key."_REMARK1"] = $row["REMARK1"];
                    $answer[$key."_REMARK2"] = $row["REMARK2"];
                }
            } else {
                $answer =& $model->field;
            }

            //備考入力
            if ($key == "01") {
                $name = $key."_REMARK2";
                //$arg["data"][$key."_REMARK"] = knjCreateTextBox($objForm, $answer[$name], $name, ($val["moji"] * 2), ($val["moji"] * 2), $extra);
                if($answer[$key."_QUESTION10"]){
                    $arg["data"][$key."_REMARK"] = "(".$answer[$name].")";
                }
            } else {
                $name = $key."_REMARK1";
                $height = $val["gyo"] * 13.5 + ($val["gyo"] - 1) * 3 + 5;
                //$arg["data"][$key."_REMARK"] = KnjCreateTextArea($objForm, $name, $val["gyo"], ($val["moji"] * 2 + 1), "soft", "style=\"height:{$height}px;\"", $answer[$name]);
                //$arg["data"][$key."_REMARK_COMMENT"] = "(全角{$val["moji"]}文字X{$val["gyo"]}行まで)";
                $arg["data"][$key."_REMARK"] = $answer[$name];
            }

            //設問チェックボックス
            if ($val["check"] == "1") {
                if ($key == "01") {
                    $question = "";
                    $sep = "";
                    foreach ($list as $cd => $label) {
                        $name = $key."_QUESTION".$cd;
                        if($answer[$name] == "1"){
                            $question .= $sep."・".$label;
                        }
                        $sep = ($cd == "8") ? "<br>" : "&nbsp;&nbsp;";
                    }

                    $arg["data"][$key."_QUESTION"] = $question;

                } else {
                    $question = array();
                    $query = knjh400_assessQuery::getAssessmentQMst($key);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $name = $key."_QUESTION".$row["ASSESS_CD"];
                        if($answer[$name] == "1"){
                            $question[$key][] = "・".$row["QUESTION"];
                        }
                    }

                    $arg["data"][$key."_QUESTION"] = (is_array($question[$key])) ? implode("<br>", $question[$key]) : $question[$key];
                }
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh400_assessSubForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    //$arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('subform1_update');\"");
    //取消ボタン
    //$arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('subform1_clear');\"");
    //戻るボタン
    //$arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");

    //諸機関との連携歴等ボタン
    $extra = "style=\"height:30px;background:#ADFF2F;color:#006400;font:bold\" onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "諸機関との連携歴等", $extra);

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"btn_reset();\"");

}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}
?>
