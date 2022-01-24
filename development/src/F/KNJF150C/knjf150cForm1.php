<?php

require_once('for_php7.php');

class knjf150cForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf150cindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjf150cQuery::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //親画面からの呼び出しかチェック。メニューから直接呼び出された場合はelseの処理を実施。
        if ($model->sendSubmit != "") {
            $arg["img"] = "1";
            //学籍番号、診療科目が指定されていたら、直接呼び出す。念のため、呼び出し元PRGで制限。
            if ($model->sendPrgId == "KNJF150D" && ($model->type != "" && $model->type != "4") && $model->schregno != "") {
                $arg["jscript"] = "btn_submit('subform".$model->type."A');";
            }
        } else {
            $arg["dispData"] = "1";
            //顔写真
            $arg["img"]["SCH_LABEL"] = $model->sch_label;
            $arg["img"]["FACE_IMG"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$model->schregno.".".$model->control_data["Extension"];
            $arg["img"]["IMG_PATH"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$model->schregno.".".$model->control_data["Extension"];
            if ($model->Properties["useDispUnDispPicture"] === '1') {
                $arg["unDispPicture"] = "1";
            } else {
                $arg["dispPicture"] = "1";
            }

            //ALLチェック
            $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

            //質問内容表示（体調１～５）
            $view_html = "";
            $view_html_no = array("1" => "体調１", "2" => "体調２", "3" => "体調３", "4" => "体調４", "5" => "体調５");

            for ($i=1; $i<= get_count($view_html_no); $i++){
                $view_html .= "<th align=\"center\" nowrap width=\"150\" height=\"25\" rowspan=\"2\" onMouseOver=\"ViewcdMousein(event, ".$i.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$i]."</th>";
            }
            $arg["view_html"] = $view_html;

            //データを取得
            $setval = array();
            $firstflg = true;   //初回フラグ
            $cnt = get_count($db->getcol(knjf150cQuery::selectQuery($model)));
            if ($model->schregno && $cnt) {
                $result = $db->query(knjf150cQuery::selectQuery($model));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row["VISIT_TIME"] = str_replace("-", "/", $row["VISIT_DATE"]).' '.$row["VISIT_HOUR"].':'.$row["VISIT_MINUTE"];

                    $row["VISIT_TIME"] = View::alink("knjf150cindex.php", $row["VISIT_TIME"], "target=_self tabindex=\"-1\"",
                                                    array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "VISIT_DATE"      => $row["VISIT_DATE"],
                                                          "VISIT_HOUR"      => $row["VISIT_HOUR"],
                                                          "VISIT_MINUTE"    => $row["VISIT_MINUTE"],
                                                          "TYPE"            => $row["TYPE"],
                                                          "cmd"             => "subform".$row["TYPE"]."A"));
                    $row["OUT_TIME"] = $row["SEQ98_REMARK1"].':'.$row["SEQ98_REMARK2"];
                    if ($row["OUT_TIME"] == ":") {
                        $row["OUT_TIME"] = "";
                    }
                    $row["BODY_TEMPERATURE"] = $row["SEQ06_REMARK3"].'.'.$row["SEQ06_REMARK4"]."℃";
                    if (!$row["SEQ06_REMARK3"]) {
                        $row["BODY_TEMPERATURE"] = "";
                    }

                    $row["TREATMENT1"] = "";
                    $sep = "";
                    if ($row["SEQ09_REMARK1"] == "1") {
                        $row["TREATMENT1"] .= "休養";
                        $sep = ",";
                    }
                    if ($row["SEQ09_REMARK3"] == "1") {
                        $row["TREATMENT1"] .= $sep."早退";
                        $sep = ",";
                    }
                    if ($row["SEQ09_REMARK6"] == "1") {
                        $row["TREATMENT1"] .= $sep."医療機関";
                        $sep = ",";
                    }
                    if ($firstflg) {
                        $setval = $row;
                        $firstflg = false;
                    } else {
                        $visit = $setval["VISIT_DATE"].':'.$setval["VISIT_HOUR"].':'.$setval["VISIT_MINUTE"].':'.$setval["TYPE"];
                        $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $visit, "", "1");
                        $arg["data"][] = $setval;
                        $setval = $row;
                    }

                }
                $visit = $setval["VISIT_DATE"].':'.$setval["VISIT_HOUR"].':'.$setval["VISIT_MINUTE"].':'.$setval["TYPE"];
                $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED",  $visit, "", "1");

                $arg["data"][] = $setval;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //データを削除
        if ($model->sendSubmit == "") {
            $model->visit_date = "";
            $model->visit_hour = "";
            $model->visit_minute = "";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf150cForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //削除ボタンを作成する
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete');\"");
    //クリアボタンを作成する
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタンを作成する
    if ($model->sendSubmit != "") {
        $link = REQUESTROOT."/F/KNJF150D/knjf150dindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $name = "戻 る";
    } else {
        $extra = "onclick=\"return closeWin();\"";
        $name = "終 了";
    }
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", $name, $extra);

    if ($model->sendSubmit == "") {
        //内科ボタン
        $extra = "style=\"height:40px;background:#00FFFF;color:#000080;font:bold\" onclick=\"return btn_submit('subform1');\"";
        $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "内 科", $extra);
        //外科ボタン
        $extra = "style=\"height:40px;background:#ADFF2F;color:#006400;font:bold\" onclick=\"return btn_submit('subform2');\"";
        $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "外 科", $extra);
        //健康相談活動ボタン
        $extra = "style=\"height:40px;background:#FFA500;color:#8B4513;font:bold\" onclick=\"return btn_submit('subform5');\"";
        $arg["button"]["btn_subform5"] = KnjCreateBtn($objForm, "btn_subform5", "健康相談", $extra);
        //その他ボタン
        $extra = "style=\"height:40px;background:#FFFF00;color:#FF6347;font:bold\" onclick=\"return btn_submit('subform3');\"";
        $arg["button"]["btn_subform3"] = KnjCreateBtn($objForm, "btn_subform3", "その他", $extra);
        //保健調査情報ボタン
        $extra = "style=\"height:40px;background:#FFC0CB;color:#8A2BE2;font:bold\" ";
        $arg["button"]["btn_subform4"] = KnjCreateBtn($objForm, "btn_subform4", "保健調査情報", $extra);
    }
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}
?>
