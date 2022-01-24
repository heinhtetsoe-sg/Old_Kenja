<?php

require_once('for_php7.php');

class knjf150aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf150aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjf150aQuery::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
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

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $cnt = get_count($db->getCol(knjf150aQuery::selectQuery($model)));
        if ($model->schregno && $cnt) {
            $result = $db->query(knjf150aQuery::selectQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["VISIT_TIME"] = str_replace("-", "/", $row["VISIT_DATE"]).' '.$row["VISIT_HOUR"].':'.$row["VISIT_MINUTE"];

                $row["VISIT_TIME"] = View::alink(
                    "knjf150aindex.php",
                    $row["VISIT_TIME"],
                    "target=_self tabindex=\"-1\"",
                    array("SCHREGNO"        => $row["SCHREGNO"],
                          "VISIT_DATE"      => $row["VISIT_DATE"],
                          "VISIT_HOUR"      => $row["VISIT_HOUR"],
                          "VISIT_MINUTE"    => $row["VISIT_MINUTE"],
                          "TYPE"            => $row["TYPE"],
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
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf150aForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //内科ボタン
    $extra = "style=\"height:35px;width:80px;background:#00FFFF;color:#000080;font:bold\" onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "内 科", $extra);
    //外科ボタン
    $extra = "style=\"height:35px;width:80px;background:#ADFF2F;color:#006400;font:bold\" onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "外 科", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}
