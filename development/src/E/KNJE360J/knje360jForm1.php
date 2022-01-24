<?php

require_once('for_php7.php');

class knje360jForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje360jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報取得
        $info = $db->getRow(knje360jQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //顔写真
        $arg["img"]["FACE_IMG"] = REQUESTROOT."/".$model->control["LargePhotoPath"]."/P".$model->schregno.".".$model->control["Extension"];
        $arg["img"]["IMG_PATH"] = REQUESTROOT."/".$model->control["LargePhotoPath"]."/P".$model->schregno.".".$model->control["Extension"];

        if ($model->Properties["useDispUnDispPicture"] === '1') {
            $arg["unDispPicture"] = "1";
        } else {
            $arg["dispPicture"] = "1";
        }

        //部クラブ名取得
        $clubname = $db->getOne(knje360jQuery::getClubName($model));
        $arg["data"]["CLUB"] = $clubname;

        //委員会取得
        $commi = $db->getRow(knje360jQuery::getCommitName($model), DB_FETCHMODE_ASSOC);
        if ($commi["COMMITTEENAME"] && $commi["CHARGENAME"]) {
            $arg["data"]["COMMI_CHARGE"] = $commi["COMMITTEENAME"]."<br>".$commi["CHARGENAME"];
        } else {
            $arg["data"]["COMMI_CHARGE"] = $commi["COMMITTEENAME"].$commi["CHARGENAME"];
        }

        //中高一貫校フラグ
        $jhflg = $db->getOne(knje360jQuery::getCombJHsch());

        //評定平均取得
        $average = $db->getOne(knje360jQuery::getValueAverage($model, $jhflg));
        $arg["data"]["AVERAGE"] = $average;

        //出身学校名取得
        $finschool = $db->getOne(knje360jQuery::getFinschoolName($model));
        $arg["data"]["FINSCHOOL"] = $finschool;

        //ALLチェック
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $cnt = get_count($db->getcol(knje360jQuery::selectQuery($model)));
        if ($model->schregno && $cnt) {
            $result = $db->query(knje360jQuery::selectQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["DATE"] = str_replace("-", "/", $row["ENTRYDATE"]);

                $row["DATE"] = View::alink(
                    "knje360jindex.php",
                    $row["DATE"],
                    "target=_self tabindex=\"-1\"",
                    array("SCHREGNO"        => $row["SCHREGNO"],
                                                  "ENTRYDATE"       => $row["ENTRYDATE"],
                                                  "SEQ"             => $row["SEQ"],
                                                  "cmd"             => $row["JUMP_NAME"]."A",
                                                  "TYPE"            => "list")
                );

                if ($firstflg) {
                    $setval = $row;
                    $firstflg = false;
                } else {
                    $entry = $setval["ENTRYDATE"].':'.$setval["SEQ"].':'.$setval["KINDCD"];
                    $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $entry, "", "1");
                    $arg["list"][] = $setval;
                    $setval = $row;
                }
            }
            $entry = $setval["ENTRYDATE"].':'.$setval["SEQ"].':'.$setval["KINDCD"];
            $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $entry, "", "1");

            $arg["list"][] = $setval;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360jForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $disabled = ($model->schregno) ? "": " disabled";
    if ($model->Properties["useKNJE360J_shinro"] == '1') {
        //進路調査ボタン
        $extra = "style=\"height:30px;background:#00FFFF;color:#000080;font:bold\" onclick=\"return btn_submit('shinro');\"";
        $arg["button"]["btn_shinro"] = KnjCreateBtn($objForm, "btn_shinro", "進路調査", $extra.$disabled);
    }
    //受験報告（進学）ボタン
    $extra = "style=\"height:30px;background:#ADFF2F;color:#006400;font:bold\" onclick=\"return btn_submit('shingaku');\"";
    $arg["button"]["btn_shingaku"] = KnjCreateBtn($objForm, "btn_shingaku", "受験報告（進学）", $extra.$disabled);
    //受験報告（就職）ボタン
    $extra = "style=\"height:30px;background:#FFFF00;color:#FF8C00;font:bold\" onclick=\"return btn_submit('syuusyoku');\"";
    $arg["button"]["btn_syuusyoku"] = KnjCreateBtn($objForm, "btn_syuusyoku", "受験報告（就職）", $extra.$disabled);
    //その他ボタン
    $extra = "style=\"height:30px;background:#C0FFFF;color:#1E90FF;font:bold\" onclick=\"return btn_submit('sonota');\"";
    $arg["button"]["btn_sonota"] = KnjCreateBtn($objForm, "btn_sonota", "その他", $extra.$disabled);

    //進路相談ボタン
    $extra = "style=\"height:30px;background:#FFE4E1;color:#FF0000;font:bold\" onclick=\"return btn_submit('shinroSoudan');\"";
    $arg["button"]["btn_shinroSoudan"] = KnjCreateBtn($objForm, "btn_shinroSoudan", "進路相談", $extra.$disabled);

    //削除ボタンを作成する
    $extra = "style=\"height:30px\" onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //終了ボタンを作成する
    $extra = "style=\"height:30px\" onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}
