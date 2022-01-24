<?php

require_once('for_php7.php');

class knje360jShinroSoudan
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("shinroSoudan", "POST", "knje360jindex.php", "", "shinroSoudan");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $info = $db->getRow(knje360jQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $query = knje360jQuery::getSubQuery4($model, 'list');
        $cnt = get_count($db->getcol($query));
        if ($model->schregno && $cnt) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["ENTRYDATE"] = str_replace("-", "/", $row["ENTRYDATE"]);
                $row["CONTENTS"] = (strlen($row["CONTENTS"]) > 90) ? substr($row["CONTENTS"], 0, 90).'...' : $row["CONTENTS"];

                if ($firstflg) {
                    $setval = $row;
                    $firstflg = false;
                } else {
                    $arg["data"][] = $setval;
                    $setval = $row;
                }
            }

            $arg["data"][] = $setval;
        }

        //警告メッセージを表示しない場合
        if ($model->cmd == "shinroSoudanA" || $model->cmd == "shinroSoudan_clear") {
            if (isset($model->schregno) && !isset($model->warning) && $model->entrydate && $model->seq) {
                $Row = $db->getRow(knje360jQuery::getSubQuery4($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //登録日
        $Row["ENTRYDATE"] = ($Row["ENTRYDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $Row["ENTRYDATE"]);
        $arg["data1"]["ENTRYDATE"] = View::popUpCalendar($objForm, "ENTRYDATE", $Row["ENTRYDATE"]);

        //相談件名
        $extra = "style=\"height:35px;\"";
        $arg["data1"]["TITLE"] = knjCreateTextArea($objForm, "TITLE", 2, 51, "soft", $extra, $Row["TITLE"]);

        //対応者
        $staffcd = ($Row["STAFFCD"]) ? $Row["STAFFCD"] : STAFFCD;
        $arg["data1"]["STAFFNAME"] = $db->getOne(knje360jQuery::getStaffName($staffcd));

        //相談内容
        $extra = "style=\"height:145px;\"";
        $arg["data1"]["CONTENTS"] = knjCreateTextArea($objForm, "CONTENTS", 10, 61, "soft", $extra, $Row["CONTENTS"]);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360jShinroSoudan.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //追加ボタンを作成する
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", "onclick=\"return btn_submit('shinroSoudan_insert');\"");
    //更新ボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('shinroSoudan_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('shinroSoudan_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    if ($model->type == "btn") {
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return top.main_frame.right_frame.closeit()\"");
    } elseif ($model->type == "main") {
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return window.open('knje360jindex.php?cmd=edit&SCHREGNO=$model->schregno','right_frame')\"");
    } else {
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");
    }
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEQ", $model->seq);
    knjCreateHidden($objForm, "cmd");

    $semes = $db->getRow(knje360jQuery::getSemesterMst(), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
    knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));
}
