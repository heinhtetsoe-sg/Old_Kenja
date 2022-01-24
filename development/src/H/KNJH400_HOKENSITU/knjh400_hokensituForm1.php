<?php

require_once('for_php7.php');

class knjh400_hokensituForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_hokensituindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjh400_hokensituQuery::getHrName($model));
        $attendno = $db->getOne(knjh400_hokensituQuery::getAttendNo($model));
        $attendno = ($attendno) ? $attendno.'番' : "";
        $name = $db->getOne(knjh400_hokensituQuery::getName($model));
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

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

        for ($i=1; $i<= get_count($view_html_no); $i++) {
            $view_html .= "<th align=\"center\" nowrap width=\"150\" height=\"25\" rowspan=\"2\" onMouseOver=\"ViewcdMousein(event, ".$i.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$i]."</th>";
        }
        $arg["view_html"] = $view_html;

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $cnt = get_count($db->getcol(knjh400_hokensituQuery::selectQuery($model)));
        if ($model->schregno && $cnt) {
            $result = $db->query(knjh400_hokensituQuery::selectQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["VISIT_TIME"] = str_replace("-", "/", $row["VISIT_DATE"]).' '.$row["VISIT_HOUR"].':'.$row["VISIT_MINUTE"];

                $row["VISIT_TIME"] = View::alink(
                    "knjh400_hokensituindex.php",
                    $row["VISIT_TIME"],
                    "target=_self tabindex=\"-1\"",
                    array("SCHREGNO"        => $row["SCHREGNO"],
                                                      "VISIT_DATE"      => $row["VISIT_DATE"],
                                                      "VISIT_HOUR"      => $row["VISIT_HOUR"],
                                                      "VISIT_MINUTE"    => $row["VISIT_MINUTE"],
                                                      "TYPE"            => $row["TYPE"],
                                                      "ATTENDNO"        => $attendno,
                                                      "NAME"            => $name,
                                                      "HR_CLASS"        => $row['HR_CLASS'],
                                                      "GRADE"           => $row['GRADE'],
                                                      "cmd"             => "subform".$row["TYPE"]."A")
                );


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
            $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $visit, "", "1");

            $arg["data"][] = $setval;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //データを削除
        $model->visit_date = "";
        $model->visit_hour = "";
        $model->visit_minute = "";

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh400_hokensituForm1.html", $arg);
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
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

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
    //生徒以外ボタン
    $extra = "style=\"height:40px;background:#FFC0CB;color:#8A2BE2;font:bold\" onclick=\"return btn_submit('subform4');\"";
    $arg["button"]["btn_subform4"] = KnjCreateBtn($objForm, "btn_subform4", $model->sch_label."以外", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}
